package com.spectralogic.dsl.helpers

import com.spectralogic.ds3client.utils.Guard
import com.spectralogic.dsl.SpectraDSL
import jline.console.completer.Completer

import java.lang.reflect.Modifier

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

    def elementList = splitElements(findElements(buffer[0..cursor - 1]))
    def matching = findMatchingGlobals(elementList[0])
    for (def i = 1; i < elementList.size() && matching.size() == 1; i++) {
      matching = findMatchingFieldsAndMethods(elementList[i], matching.values()[0] as Class)

      if (i < elementList.size() - 1) {
        matching = getExactMatches(elementList[i].replaceAll("[()]", ""), matching)
      }

    }

    candidates.addAll(cleanseDuplicatesAndSort(matching.keySet().toList()))



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
  private Map findMatchingGlobals(String prefix) {
    if (prefix == null) return [:]

    def matching = [:]
    matching << shell.context.variables.findAll { it.key.toString().startsWith(prefix) }.collectEntries {
                  [(it.key) : it.value.class]
                }
    matching << SpectraDSL.class.declaredMethods.findAll {
                  Modifier.isPublic(it.modifiers) && it.name.startsWith(prefix)
                }.collectEntries {
                  [(it.name + (it.parameterTypes.length == 0 ? "()" : "(")) : it.returnType ]
                }

    return matching
  }

  /** Finds matching fields and methods of a class and their respective class or return type */
  private Map findMatchingFieldsAndMethods(String prefix, Class clazz) {
    if (prefix.endsWith('(') || prefix.endsWith(')')) {
      return findMatchingMethods(prefix.replaceAll("[()]", ""), clazz)
    } else {
      def matching = [:]
      matching << findMatchingMethods(prefix, clazz)
      matching << findMatchingFields(prefix, clazz)
      return matching
    }
  }

  private Map findMatchingFields(String prefix, Class clazz) {
    def matchingMethods = findMatchingMethods(prefix, clazz)
    /* Public fields are still private but have a getter. */
    return clazz.declaredFields.findAll { it.name.startsWith(prefix) && !it.synthetic &&
            matchingMethods.containsKey(fieldToGetter(it.name)) }.collectEntries {
              [(it.name) : it.class]
            }
  }

  private Map findMatchingMethods(String prefix, Class clazz) {
    return clazz.declaredMethods.findAll { !it.synthetic && Modifier.isPublic(it.modifiers) &&
            it.name.startsWith(prefix) }.collectEntries {
              [(it.name + (it.parameterTypes.length == 0 ? "()" : "(")) : it.returnType]
            }
  }

  /** Takes a map created by the above methods and returns methods/fields/vars that match exactly */
  private Map getExactMatches(String name, Map<String, Class> matching) {
    return matching.findAll { it.key.toString().replaceAll("[()]", "") == name }
  }

  /** convert a field's name to it's getter name */
  private String fieldToGetter(String field) {
    if (Guard.isStringNullOrEmpty(field)) return ""

    return "get" + field.replaceFirst(field[0], field[0].toUpperCase()) + "()"
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

    /* used to ignore everything in parenthesis */
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
