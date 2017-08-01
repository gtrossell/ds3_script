import groovy.util.GroovyTestSuite 
import junit.framework.Test 
import junit.textui.TestRunner 

import spectra.test.models.BpBucketTest
import spectra.test.models.BpClientTest
import spectra.test.models.BpObjectTest
import spectra.test.SpectraDSLTest

class Test {
  static void main(String... args) {
    def allTests = new GroovyTestSuite()
    allTests.addTestSuite(BpBucketTest.class)
    allTests.addTestSuite(BpClientTest.class)
    allTests.addTestSuite(BpObjectTest.class)
    allTests.addTestSuite(SpectraDSLTest.class)
    TestRunner.run(allTests)
  }
}
