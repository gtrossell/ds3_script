package com.spectralogic.dsl.test.helpers

import com.spectralogic.dsl.helpers.DslCompleter
import com.spectralogic.dsl.models.BpBucket
import org.junit.Test

class DslCompleterTest extends GroovyTestCase {

    @Test
    void testFindElements() {
        def shell = [context: [:]] as GroovyShell
        def completer = new DslCompleter(shell)

        assertEquals "test", completer.findElements("test")
        assertEquals "test", completer.findElements("this = test")
        assertEquals "test", completer.findElements("this=test")
        assertEquals "string.of.fields", completer.findElements("test = string.of.fields")
        assertEquals "createBpClient().toString().", completer.findElements("test = createBpClient().toString().")
        assertEquals "", completer.findElements("test = createBpClient().toString()")
        assertEquals "createBpClient(\"test\").toString().", completer.findElements("test = createBpClient(\"test\").toString().")
        assertEquals "createBpClient(method()).toString().", completer.findElements("test = createBpClient(method()).toString().")
        assertEquals "test[1].", completer.findElements("t = test[1].")
        assertEquals "", completer.findElements("t = test[1]")

        /* this returns empty because before findElements is called, the trailing '(' is removed */
        assertEquals "", completer.findElements("test = createBpClient(")
    }

    @Test
    void testSplitElements() {
        def shell = [context: [:]] as GroovyShell
        def completer = new DslCompleter(shell)

        def l = ["test"]
        assertEquals l, completer.splitElements("test")
        l = ["test", ""]
        assertEquals l, completer.splitElements("test.")
        l = ["string", "of", "fields"]
        assertEquals l, completer.splitElements("string.of.fields")
        l = ["test()", ""]
        assertEquals l, completer.splitElements("test(param).")
        assertEquals l, completer.splitElements("test(param1, param2).")
        assertEquals l, completer.splitElements("test(\"str\").")
        assertEquals l, completer.splitElements("test(method()).")
        l = ["test[1]", ""]
        assertEquals l, completer.splitElements("test[1].")
    }

    @Test
    void testFindMatchingFieldsAndMethods() {
        def shell = [context: [:]] as GroovyShell
        def completer = new DslCompleter(shell)

        def r = ["charAt(": char]
        assertEquals r, completer.findMatchingFieldsAndMethods("char", String.class)
        assertEquals r, completer.findMatchingFieldsAndMethods("charAt(", String.class)
        r = ["name": String]
        assertEquals r, completer.findMatchingFieldsAndMethods("na", BpBucket.class)
    }

    @Test
    void testGetExactMatches() {
        def shell = [context: [:]] as GroovyShell
        def completer = new DslCompleter(shell)

        def matching = ["test": String.class, "tester": String.class]
        def match = ["test": String.class]
        assertEquals match,  completer.getExactMatches("test", matching)
        assertEquals [:], completer.getExactMatches("abc", matching)
    }

    @Test
    void testFindMatchingGlobals() {
        def shell = [context: ["testVar": "test", "test2Var": "test"]] as GroovyShell
        def completer = new DslCompleter(shell)

        def match = ["testVar": String.class]
        assertEquals match, completer.findMatchingGlobals("testV")
        match = ["testVar": String.class, "test2Var": String.class]
        assertEquals match, completer.findMatchingGlobals("t")
        assertEquals [:], completer.findMatchingGlobals("abc")
    }

    @Test
    void testCleanseDuplicatesAndSort() {
        def shell = [context: [:]] as GroovyShell
        def completer = new DslCompleter(shell)

        def candidates = ["cab", "bca", "abc"]
        def sorted = ["abc", "bca", "cab"]
        assertEquals sorted, completer.cleanseDuplicatesAndSort(candidates)
    }

}
