package com.spectralogic.dsl.helpers

import com.spectralogic.ds3client.Ds3Client
import com.spectralogic.ds3client.Ds3ClientBuilder
import com.spectralogic.ds3client.models.common.Credentials
import com.spectralogic.dsl.helpers.Environment
import com.spectralogic.dsl.models.BpClient

/**
 * Globals keeps all fields and functions that need to be accessed multiple
 * places or are constants
 */
class Globals {
  final static String PROMPT = 'spectra> '
  final static String RETURN_PROMPT = '===> '
  private final static logger
  
  static def initMessage(width) {
    """
Welcome to the Spectra DSL for BlackPearl!
Use the ':help' command to get started
${'=' * width}"""
  }

  /** Returns a BP client */
  static def createBpClient(String endpoint="", String accessId="", 
                      String secretKey="", Boolean https=false,
                      Environment environment=null) {
    // TODO: add real logging
    if (!environment) environment = new Environment()
    if (!(environment in Environment)) {
      logger.warn("Variable 'environment' should be of type spectra.helpers.Environment")
      environment = new Environment()
    }

    endpoint = endpoint ?: environment.getEndpoint()
    accessId = accessId ?: environment.getAccessKey()
    secretKey = secretKey ?: environment.getSecretKey()
    if (!endpoint || !accessId || !secretKey) {
      logger.error("Endpoint, Access ID, and/or Sectret Key is not set! " +
                    "Try setting the environment or method variable(s)")
      return null
    }
    
    def cred = new Credentials(accessId, secretKey)
    new BpClient(Ds3ClientBuilder.create(endpoint, cred).withHttps(https).build())
  }
}