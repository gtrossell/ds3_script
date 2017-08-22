package com.spectralogic.dsl

import com.spectralogic.dsl.models.BpClientBuilder
import java.nio.file.Path
import java.nio.file.Paths

/** functions available to the user from the shell */
abstract class SpectraDSL extends Script {

  /** @return BpClient with given attributes or environment variables */
  def createBpClient(String endpoint="", String accessId="", 
                      String secretKey="", Boolean https=false) {
    return new BpClientBuilder().createBpClient(endpoint, accessId, secretKey, https, environment)
  }

}
