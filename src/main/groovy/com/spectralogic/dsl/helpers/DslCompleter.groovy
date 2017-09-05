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
      return -1
    }

    def elementList = splitElements(findElements(buffer[0..cursor - 1]))
    def matching = findMatchingGlobals(elementList[0])
    for (def i = 1; i < elementList.size() && matching.size() == 1; i++) {
      matching = findMatchingFieldsAndMethods((matching.keySet() as String[])[0], (matching.values() as Class[])[0])
    }

    candidates.addAll(cleanseDuplicatesAndSort(matching.keySet().toList()))

    return cursor
  }

  /** Prevent duplicates and sort the candidates */
  private List<CharSequence> cleanseDuplicatesAndSort(List<String> candidates) {
    def cleaned = []
    candidates = candidates.sort()
    for (def i = 0; i < candidates.size() - 1; i++) {
      cleaned << candidates[i]
      if (candidates[i + 1].startsWith(candidates[i])) i++
    }

    return (cleaned << candidates.last()).unique()
  }

  /** Find matching global variables and methods and their respective class or return type */
  private Map findMatchingGlobals(String prefix) {
    def matching = [:]
    matching << shell.context.variables.findAll { it.key.toString().startsWith(prefix) }.collectEntries {
                  [(it.key) : it.class] }
    matching << SpectraDSL.class.declaredMethods.findAll {
                  Modifier.isPublic(it.modifiers) && it.name.startsWith(prefix)
                }.collectEntries {
                  [(it.name + (it.parameterTypes.length == 0 ? "()" : "(")) : it.returnType ]
                }

    return matching
  }

  /** Finds matching fields and methods of a class and their respective class or return type */
  private Map findMatchingFieldsAndMethods(String prefix, Class aClass) {
    def matching = [:]
    matching << aClass.fields.findAll { it.name.startsWith(prefix) }.collectEntries { [(it.name) : it.class] }
    matching << aClass.methods.findAll { it.name.startsWith(prefix) }.collectEntries {
                  [(it.name + (it.parameterTypes.length == 0 ? "()" : "(")) : it.returnType] }

    return matching
  }

  /** splits the elements into raw method/variable names ('test.method(arg)' -> ['test', 'method()']) */
  private List<String> splitElements(String elements) {
    def elementList = []
    elements.split('\\.').each { part ->\
      def chars = part.toCharArray()
      for (def i = 0; i < chars.size(); i++) {
        if (!Character.isJavaIdentifierPart(chars[i]) || i == chars.size() - 1) {
          elementList << part[0..i]
        }
      }
    }

    return elementList
  }

  /**
   * @param buffer up to where the cursor is
   * @return chain of methods/objects that the cursor is on
   */
  private String findElements(String buffer) {
    if (!(buffer[-1] == '.' || Character.isJavaIdentifierPart(buffer[-1].toCharacter()))) return ''

    for (def i = buffer.size() - 1; i > 0; i--) {
      if (!(buffer[i] == '.' || Character.isJavaIdentifierPart(buffer.charAt(i)))) {
        return buffer[++i..-1]
      }
    }
    return buffer
  }

}
