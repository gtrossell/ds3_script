package com.spectralogic.dsl.helpers

import com.spectralogic.ds3client.utils.Guard
import com.spectralogic.dsl.SpectraDSL
import jline.console.completer.Completer

import java.lang.reflect.Modifier

/**
 * JLine Auto completer for the DSL shell
 * Currently is able to parse methods, variables, parameters, and method parameter classes
 *
 * TODO: parse strings
 * TODO: parse array elements (ie arr[i])
 * TODO: parse map elements
 * TODO: autocomplete groovy collections
 */
class DslCompleter implements Completer {
    private final GroovyShell shell

    DslCompleter(GroovyShell shell) {
        this.shell = shell
    }

    @Override
    int complete(String buffer, int cursor, List<CharSequence> candidates) {
        if (Guard.isStringNullOrEmpty(buffer) || cursor == 0) {
            candidates.addAll(cleanseDuplicatesAndSort(findMatchingGlobals('').keySet().toList()))
            return 0
        }

        /* tabbing a method for parameter options */
        def paramOptions = buffer[cursor - 1] == '('

        def bufferEnd = paramOptions ? cursor - 2 : cursor - 1
        def elementList = splitElements(findElements(buffer[0..bufferEnd]))

        if (elementList.empty) {
            candidates.addAll(cleanseDuplicatesAndSort(findMatchingGlobals('').keySet().toList()))
            return cursor
        }

        def matching = findMatchingGlobals(elementList[0])
        Class prevMatchingClass = SpectraDSL.class
        for (def i = 1; i < elementList.size() && matching.size() == 1; i++) {
            prevMatchingClass = matching.values().first()
            matching = findMatchingFieldsAndMethods(elementList[i], matching.values()[0])

            if (i < elementList.size() - 1) {
                matching = getExactMatches(elementList[i].replaceAll("[()]", ""), matching)
            }
        }

        /* limit candidates to exact method match if tabbing a method for parameter options */
        if (paramOptions) {
            elementList[-1] = elementList[-1] + "(" /* makes sure cursor return is correct */
            matching = matching.findAll { it.key.toString().startsWith(elementList[-1]) }
        }

        candidates.addAll(cleanseDuplicatesAndSort(matching.keySet().toList()))

        /* if only one candidate and it's a method, add candidates that describe parameter options */
        if (candidates.size() == 1 && candidates.first().toString().endsWith('(')) {
            def methods = prevMatchingClass.methods.findAll {
                it.name == candidates.first().toString().replaceAll("[()]", "")
            }

            candidates.addAll(
                    methods.collect { method ->
                        def params = method.parameters.collect {
                            if (it.type.isArray()) {
                                return arrayToString(it.type)
                            } else {
                                return it.type.name.split('\\.')[-1].replace(';', '')
                            }
                        }
                        return "${candidates.first().toString()}${params.join(', ')})"
                    }.sort { it.size() }
            )

            if (1 < candidates.size() && candidates[1] == candidates[0] + ')') candidates.remove(0)
        }

        return 0 < elementList.size() ? cursor - elementList[-1].size() : cursor
    }

    /** Prevent duplicates and sort the candidates */
    private List<CharSequence> cleanseDuplicatesAndSort(List<String> candidates) {
        if (Guard.isNullOrEmpty(candidates)) return []

        candidates = candidates.sort()
        List<String> cleaned = [candidates[0]]
        for (candidate in candidates) {
            if (!candidate.startsWith(cleaned[-1])) cleaned << candidate
        }

        return cleaned
    }

    /** Find matching global variables and methods and their respective class or return type */
    private Map<String, Class> findMatchingGlobals(String prefix) {
        if (prefix == null) return [:]

        if (prefix.endsWith('(')) {
            return findMatchingGlobalMethods(prefix.replaceAll("[(]", ""))
        } else if (prefix.endsWith(')')) {
            def entry = findMatchingGlobalMethods(prefix.replaceAll("[()]", "")).find { it.key.endsWith(')') }

            return [(entry.key): entry.value]
        } else if (prefix.endsWith(']')) {
            try {
                def variables = shell.context.variables
                def varName = prefix.split('\\[')[0]
                def index = prefix.split('\\[')[-1].split(']')[0]

            if (index.isInteger()) {
                return ["": variables.get(varName)[index.toInteger()].class]
            } else {
                return ["": variables.get(varName)[index[1..-2]].class]
            }
            } catch (Throwable e) {
                return [:]
            }
        } else{
            def matching = [:]
            matching << findMatchingGlobalMethods(prefix)
            matching << shell.context.variables.findAll {
                it.value != null && it.key.toString().startsWith(prefix)
            }.collectEntries {
                [(it.key): it.value.class]
            }

            return matching
        }
    }

    private Map<String, Class> findMatchingGlobalMethods(String prefix) {
        return SpectraDSL.class.declaredMethods.findAll {
            Modifier.isPublic(it.modifiers) && it.name.startsWith(prefix)
        }.collectEntries {
            [(it.name + (it.parameterTypes.length == 0 ? "()" : "(")): it.returnType]
        }
    }

    /** Finds matching fields and methods of a class and their respective class or return type */
    private Map<String, Class> findMatchingFieldsAndMethods(String prefix, Class clazz) {
        if (prefix.endsWith('(') || prefix.endsWith(')')) {
            return findMatchingMethods(prefix.replaceAll("[()]", ""), clazz)
        } else {
            def matching = [:]
            matching << findMatchingFields(prefix, clazz)
            matching << findMatchingMethods(prefix, clazz).findAll { method ->
                if (method.key.startsWith("get") && method.key.endsWith("()")) {
                    return !matching.keySet().contains(method.key[3..-3].uncapitalize())
                } else {
                    return true
                }
            }

            return matching
        }
    }

    /** Finds public fields by matching them to getter methods */
    private Map<String, Class> findMatchingFields(String prefix, Class clazz) {
        def classMethodNames = clazz.methods.collect { it.name.toLowerCase() }
        return clazz.declaredFields.findAll {
            it.name.startsWith(prefix) && classMethodNames.contains("get" + it.name)
        }.collectEntries {
            [(it.name): it.type]
        }
    }

    private Map<String, Class> findMatchingMethods(String prefix, Class clazz) {
        return clazz.declaredMethods.findAll {
            !it.synthetic && Modifier.isPublic(it.modifiers) && it.name.startsWith(prefix)
        }.collectEntries {
            [(it.name + (it.parameterTypes.length == 0 ? "()" : "(")): it.returnType]
        }
    }

    /** Takes a map created by the above methods and returns methods/fields/vars that match exactly */
    private Map<String, Class> getExactMatches(String name, Map<String, Class> matching) {
        return matching.findAll { it.key.toString().replaceAll("[()]", "") == name }
    }

    /** splits the elements into raw method/variable names ('test.method(arg)' -> ['test', 'method()']) */
    private List<String> splitElements(String elements) {
        if (Guard.isStringNullOrEmpty(elements)) return []

        def elementList = []
        def element = ""
        def parenthesisStack = 0
        for (character in elements) {
            if (character == '(') {
                parenthesisStack++
                if (!element.isEmpty()) {
                    elementList << element + "()"
                    element = ""
                }
            }

            if (parenthesisStack == 0) {
                if (character == '.' && !element.isEmpty()) {
                    elementList << element
                    element = ""
                } else if (character != '.') {
                    element += character
                }
            }

            if (character == ')') parenthesisStack--
        }

        if (0 < element.size()) elementList << element

        return elements[-1] == '.' ? elementList << '' : elementList
    }

    /**
     * @param buffer up to where the cursor is
     * @return chain of methods/objects that the cursor is on
     */
    private String findElements(String buffer) {
        def isElementCharacter = { Character c -> c == '.'.toCharacter() || Character.isJavaIdentifierPart(c) }

        if (!isElementCharacter(buffer[-1].toCharacter())) return ''

        /* ignore everything in parenthesis */
        def parenthesisStack = 0
        def squareBracketsStack = 0
        for (def i = buffer.size() - 1; 0 < i; i--) {
            // TODO: make sure parenthesis and square brackets are not inside strings
            if (buffer[i] == ')') parenthesisStack++
            if (buffer[i] == ']') squareBracketsStack++

            if (parenthesisStack + squareBracketsStack == 0 && !isElementCharacter(buffer.charAt(i))) {
                return buffer[++i..-1]
            }

            if (buffer[i] == '(') parenthesisStack--
            if (buffer[i] == '[') squareBracketsStack--
        }

        return buffer
    }

    private String arrayToString(Class clazz) {
        def options = [:].withDefault { 'array[]' }
        options.put(char[].class, 'char[]')
        options.put(int[].class, 'int[]')
        options.put(boolean[].class, 'boolean[]')
        options.put(byte[].class, 'byte[]')
        options.put(short[].class, 'short[]')
        options.put(long[].class, 'long[]')
        options.put(float[].class, 'float[]')
        options.put(double[].class, 'double[]')
        return options[clazz]
    }

}
