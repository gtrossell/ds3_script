package spectra.helpers

/**
 * Config keeps all of the strings and options that are able to be changed
 * This might be merged with Environment in the future so that configuration
 * can come from environment variables
 */
class Config {
  static String homeDir = ''
  static String logDir = ''
  static String scriptDir = ''

  def static getHomeDir() {
    if (!homeDir)
      homeDir = new File("").getAbsoluteFile().toString()
    return addTrailingSlash(homeDir)
  }

  def static getScriptDir() {
    if (!scriptDir)
      scriptDir = getHomeDir() + "scripts/"
    return addTrailingSlash(scriptDir)
  }

  def static getLogDir() {
    if (!logDir)
      logDir = getHomeDir() + "log/"
    return addTrailingSlash(logDir)
  }

  private static addTrailingSlash(path) {
    if (path[path.size() - 1] != '/')
      path += '/'
    path
  }

}