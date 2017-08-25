package com.spectralogic.dsl.helpers

/** Config keeps the location of important directories */
class Config {
  private static String homeDir
  private static String logDir
  private static String scriptDir

  static String getHomeDir() {
    if (!homeDir) homeDir = new File("").getAbsoluteFile().toString()
    return addTrailingSlash(homeDir)
  }

  static String getScriptDir() {
    if (!scriptDir) scriptDir = getHomeDir() + "scripts/"
    return addTrailingSlash(scriptDir)
  }

  static String getLogDir() {
    if (!logDir) logDir = getHomeDir() + "log/"
    return addTrailingSlash(logDir)
  }

  static String addTrailingSlash(String path) {
    if (path[path.size() - 1] != '/') path += '/'
    return path
  }

}