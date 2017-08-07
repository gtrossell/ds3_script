package spectra.helpers

/** Provides methods to help the command classes */
class CommandHelper {

  /** @return script file from a string by parsing a path, '.', or no given path */
  File getScriptFromString(String pathStr) {
    def file
    if (!pathStr.contains('/')) {
      file = new File(Config.getScriptDir(), pathStr)
    } else if (pathStr[0] == '.') {
      file = new File(Config.getHomeDir(), pathStr.substring(1))
    } else {
      file = new File(pathStr)
    }
    return file
  }

}