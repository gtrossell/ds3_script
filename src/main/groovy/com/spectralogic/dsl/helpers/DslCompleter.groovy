package com.spectralogic.dsl.helpers

import com.spectralogic.ds3client.utils.Guard
import com.spectralogic.dsl.SpectraDSL
import jline.console.completer.Completer

import java.lang.reflect.Modifier

/**
 * JLine Auto completer for the DSL shell
 * Currently is able to parse methods, variables, and fields
 *
 * TODO: parse strings
 * TODO: parse array elements (ie arr[i])
 * TODO: parse map elements
 * TODO: fields aren't working again
 * TODO: createBpClient(, createBpClient() is redundant
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

    /* for tabbing a method for parameter options */
    def paramOptions = buffer[cursor - 1] == '('
    def bufferEnd = paramOptions ? cursor - 2 : cursor - 1
    def elementList = splitElements(findElements(buffer[0..bufferEnd]))
    def matching = findMatchingGlobals(elementList[0])
    Class prevMatchingClass = SpectraDSL.class
    for (def i = 1; i < elementList.size() && matching.size() == 1; i++) {
      prevMatchingClass = matching.values().first()
      matching = findMatchingFieldsAndMethods(elementList[i], matching.values()[0])

      if (i < elementList.size() - 1) {
        matching = getExactMatches(elementList[i].replaceAll("[()]", ""), matching)
      }
    }

    /* limit candidates to exact method match */
    if (paramOptions) {
      elementList[-1] = elementList[-1] + "(" /* makes sure cursor return is correct */
      matching = matching.findAll { it.key.toString().startsWith(elementList[-1]) }
    }

    candidates.addAll(cleanseDuplicatesAndSort(matching.keySet().toList()))

    /* In case of a single method option, add candidates that describe parameter options */
    if (candidates.size() == 1 && candidates.first().toString().endsWith('(')) {
      def methods = prevMatchingClass.methods.findAll {
        it.name == candidates.first().toString().replaceAll("[()]", "")
      }

      candidates.addAll(methods.collect { method ->
        def params = method.parameters.collect { it.type.name.split('\\.')[-1].replace(';', '') }
        return "${candidates.first().toString()}${params.join(', ')})"
      }.sort { it.size() })
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
      def entry =  findMatchingGlobalMethods(prefix.replaceAll("[()]", "")).find { it.key.endsWith(')') }
      return [(entry.key): entry.value]
    } else {
      def matching = [:]
      matching << findMatchingGlobalMethods(prefix)
      matching << shell.context.variables.findAll { it.key.toString().startsWith(prefix) }.collectEntries {
        [(it.key) : it.value.class]
      }
      return matching
    }
  }

  private Map<String, Class> findMatchingGlobalMethods(String prefix) {
    return SpectraDSL.class.declaredMethods.findAll {
      Modifier.isPublic(it.modifiers) && it.name.startsWith(prefix)
    }.collectEntries {
      [(it.name + (it.parameterTypes.length == 0 ? "()" : "(")) : it.returnType ]
    }
  }

  /** Finds matching fields and methods of a class and their respective class or return type */
  private Map<String, Class> findMatchingFieldsAndMethods(String prefix, Class clazz) {
    if (prefix.endsWith('(') || prefix.endsWith(')')) {
      return findMatchingMethods(prefix.replaceAll("[()]", ""), clazz)
    } else {
      def matching = [:]
      matching << findMatchingMethods(prefix, clazz)
      matching << findMatchingFields(prefix, clazz)
      return matching
    }
  }

  /** Finds public fields by matching them to getter methods */
  private Map<String, Class> findMatchingFields(String prefix, Class clazz) {
    def classMethodNames = clazz.methods.collect { it.name.toLowerCase() }
    return clazz.declaredFields.findAll { it.name.startsWith(prefix) &&
            classMethodNames.contains("get" + it.name) }.collectEntries {
      [(it.name) : it.type]
    }
  }

  private Map<String, Class> findMatchingMethods(String prefix, Class clazz) {
    return clazz.declaredMethods.findAll { !it.synthetic && Modifier.isPublic(it.modifiers) &&
            it.name.startsWith(prefix) }.collectEntries {
              [(it.name + (it.parameterTypes.length == 0 ? "()" : "(")) : it.returnType]
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

    if (0 < element.size()) {
      elementList << element
    }

    return elements[-1] == '.' ? elementList << '' : elementList
  }

  /**
   * @param buffer up to where the cursor is
   * @return chain of methods/objects that the cursor is on
   */
  private String findElements(String buffer) {
    if (!isElementCharacter(buffer[-1].toCharacter())) return ''

    /* ignore everything in parenthesis */
    def parenthesisStack = 0
    for (def i = buffer.size() - 1; 0 < i; i--) {
      if (buffer[i] == ')') parenthesisStack++

      if (parenthesisStack == 0 && !isElementCharacter(buffer.charAt(i))) {
        return buffer[++i..-1]
      }

      if (buffer[i] == '(') parenthesisStack--
    }
    return buffer
  }

  private Boolean isElementCharacter(Character c) {
    return c == '.'.toCharacter() || Character.isJavaIdentifierPart(c)
  }

}
