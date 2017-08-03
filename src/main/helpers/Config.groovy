package spectra.helpers

/**
 * Config keeps all of the strings and options that are able to be changed
 * This might be merged with Environment in the future so that configuration
 * can come from environment variables
 */
class Config {
  static String logDir = ''

  def static getLogDir() {
    if (!logDir) {
      logDir = new File("").getAbsoluteFile().toString() + "/log"
    }
    logDir
  }

}