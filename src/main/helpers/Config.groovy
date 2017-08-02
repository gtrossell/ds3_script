package spectra.helpers

/**
 * Config keeps all of the strings and options that are able to be changed
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