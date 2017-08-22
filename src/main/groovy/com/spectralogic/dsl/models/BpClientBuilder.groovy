package com.spectralogic.dsl.models

import com.spectralogic.dsl.exceptions.BpException
import com.spectralogic.ds3client.Ds3Client
import com.spectralogic.ds3client.Ds3ClientBuilder
import com.spectralogic.ds3client.models.common.Credentials
import com.spectralogic.dsl.helpers.Environment
import com.spectralogic.dsl.models.BpClient
import org.slf4j.LoggerFactory

class BpClientBuilder {
  private final static logger = LoggerFactory.getLogger(BpClientBuilder.class)

  /** Returns a BP client */
  BpClient create(String endpoint="", String accessId="", String secretKey="", Boolean https=false,
                    Environment environment=null) {

    if (!environment) {
      environment = new Environment()
    }
    if (!(environment in Environment)) {
      logger.warn("Variable 'environment' should be of type spectra.helpers.Environment")
      environment = new Environment()
    }

    endpoint = endpoint ?: environment.getEndpoint()
    accessId = accessId ?: environment.getAccessKey()
    secretKey = secretKey ?: environment.getSecretKey()
    if (!endpoint || !accessId || !secretKey) {
      return new BpException("Endpoint, Access ID, and/or Sectret Key is not set! " +
                              "Try setting the environment or method variable(s)")
    }
    
    def cred = new Credentials(accessId, secretKey)
    new BpClient(Ds3ClientBuilder.create(endpoint, cred).withHttps(https).build())
  }

}
