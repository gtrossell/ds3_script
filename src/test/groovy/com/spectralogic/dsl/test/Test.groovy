import groovy.util.GroovyTestSuite 
import junit.framework.Test 
import junit.textui.TestRunner 

import com.spectralogic.dsl.test.models.BpBucketTest
import com.spectralogic.dsl.test.models.BpClientTest
import com.spectralogic.dsl.test.models.BpObjectTest
import com.spectralogic.dsl.test.SpectraDSLTest

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
