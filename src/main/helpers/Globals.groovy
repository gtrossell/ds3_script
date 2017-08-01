package spectra.helpers

import com.spectralogic.ds3client.models.common.Credentials

import spectra.helpers.Environment
import spectra.models.BpClient
import com.spectralogic.ds3client.Ds3Client
import com.spectralogic.ds3client.Ds3ClientBuilder

/**
 * Globals keeps all fields and functions that need to be accessed multiple
 * places or are constants
 */
class Globals {
  final static def PROMPT = 'spectra> '
  static def init_message(width) {
    """
Welcome to the Spectra DSL for BlackPearl!
${'=' * width}"""
  }

  /** Returns a BP client */
  static def createBpClient(String endpoint="", String accessId="", 
                      String secretKey="", Boolean https=false) {
    // TODO: add real logging
    def environment = new Environment()
    endpoint = endpoint ?: environment.getEndpoint()
    accessId = accessId ?: environment.getAccessKey()
    secretKey = secretKey ?: environment.getSecretKey()
    if (!endpoint || !accessId || !secretKey) {
      println "[Error] Endpoint, Access ID, and/or Sectret Key is not set!\n" +
              "\tTry setting the environment or method variable(s)"
      return null
    }
    
    def cred = new Credentials(accessId, secretKey)
    new BpClient(Ds3ClientBuilder.create(endpoint, cred).withHttps(https).build())
  }
}