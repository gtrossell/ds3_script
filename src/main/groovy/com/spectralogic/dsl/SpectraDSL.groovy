package com.spectralogic.dsl

import com.spectralogic.dsl.models.BpClient
import com.spectralogic.dsl.models.BpClientBuilder

/** functions available to the user from the shell */
abstract class SpectraDSL extends Script {

  /** @return BpClient with given attributes or environment variables */
  BpClient createBpClient(String endpoint="", String accessId="",
                          String secretKey="", Boolean https=false) {
    return new BpClientBuilder().create(endpoint, accessId, secretKey, https, environment)
  }

}
