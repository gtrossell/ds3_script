package com.spectralogic.dsl

import com.spectralogic.dsl.models.BpClientFactory
import java.nio.file.Path
import java.nio.file.Paths

/**
 * This is the customized SpectraShell that contains the DSL and acts similar
 * to GroovyShell
 */
abstract class SpectraDSL extends Script {

  /**
   * Points to a Global function since functions in an abstract class cannot be
   * be directly referenced and this function is used before init 
   * @return BpClient with given attributes or environment variables
   */
  def createBpClient(String endpoint="", String accessId="", 
                      String secretKey="", Boolean https=false) {
    return new BpClientFactory().createBpClient(endpoint, accessId, secretKey, https, environment)
  }

}
