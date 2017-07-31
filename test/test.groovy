import groovy.util.GroovyTestSuite 
import junit.framework.Test 
import junit.textui.TestRunner 

import spectra.test.BpBucketTest
import spectra.test.BpClientTest
import spectra.test.BpObjectTest
import spectra.test.SpectraDSLTest

class AllTests { 
   static Test suite() { 
      def allTests = new GroovyTestSuite()
      allTests.addTestSuite(BpBucketTest.class)
      allTests.addTestSuite(BpClientTest.class)
      allTests.addTestSuite(BpObjectTest.class)
      allTests.addTestSuite(SpectraDSLTest.class)
      return allTests
   }
}

TestRunner.run(AllTests.suite())
