package com.spectralogic.dsl.test.helpers

import com.spectralogic.dsl.helpers.DslCompleter
import com.spectralogic.dsl.models.BpBucket
import com.spectralogic.dsl.models.BpClientBuilder
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
        assertEquals '"test".ch', completer.findElements('test = "test".ch')

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
        l = ['"test"', "ch"]
        assertEquals l, completer.splitElements('"test".ch')
        l = ["'test'", "ch"]
        assertEquals l, completer.splitElements("'test'.ch")
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
        match = ['"test"': String.class]
        assertEquals match, completer.findMatchingGlobals('"test"')
        match = ["'test'": String.class]
        assertEquals match, completer.findMatchingGlobals("'test'")
    }

    @Test
    void testCleanseDuplicatesAndSort() {
        def shell = [context: [:]] as GroovyShell
        def completer = new DslCompleter(shell)

        def candidates = ["cab", "bca", "abc", "metaClass"]
        def sorted = ["abc", "bca", "cab"]
        assertEquals sorted, completer.cleanseDuplicatesAndSort(candidates)
    }

    @Test
    void testCompleter() {
        /* Use a getBucket as a test object */
        def client = new BpClientBuilder().create()
        def bucketName = 'test_bucket_' + (new Random().nextInt(10**4))
        def bucket = client.createBucket(bucketName)

        def shell = [context: ["testVar": "test", "getBucket": bucket, "testList": [1,2,3]]] as GroovyShell
        def completer = new DslCompleter(shell)
        def candidates = []
        def r

        try {
            completer.complete("testV", 5, candidates)
            r = ["testVar"]
            assertEquals r, candidates

            /* BpBucket candidates */
            candidates = []
            completer.complete("getBucket.", 7, candidates)
            r = ["delete()", "deleteAllObjects()", "deleteObject(", "deleteObjects(", "getBulk(", "getName()", "name",
                 "object(", "objects(", "putBulk(", "reload()","size()", "toString()"]
//            assertEquals r, candidates

            /* Indexed List candidates */
            candidates = []
            completer.complete("testList[1].", 12, candidates)
            r = ["BYTES", "MAX_VALUE", "MIN_VALUE", "SIZE", "TYPE", "bitCount(", "byteValue()", "compare(", "compareTo(", "compareUnsigned(", "decode(", "divideUnsigned(",
                 "doubleValue()", "equals(", "floatValue()", "getInteger(", "hashCode(", "highestOneBit(", "intValue()",
                 "longValue()", "lowestOneBit(", "max(", "min(", "numberOfLeadingZeros(", "numberOfTrailingZeros(",
                 "parseInt(", "parseUnsignedInt(", "remainderUnsigned(", "reverse(", "reverseBytes(", "rotateLeft(",
                 "rotateRight(", "shortValue()", "signum(", "sum(", "toBinaryString(", "toHexString(", "toOctalString(",
                 "toString(", "toUnsignedLong(", "toUnsignedString(", "valueOf("]
            assertEquals r, candidates

            /* String candidates */
            candidates = []
            completer.complete("'string'.ch", 11, candidates)
            r = ["charAt(", "charAt(int)"]
            assertEquals r, candidates

            /* Array candidates TODO */
        } finally {
            bucket.delete()
        }
    }

}
