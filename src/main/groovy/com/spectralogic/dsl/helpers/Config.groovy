package com.spectralogic.dsl.helpers

/** Config keeps the location of important directories */
class Config {
  private static String homeDir
  private static String logDir
  private static String scriptDir

  def static getHomeDir() {
    if (!homeDir) homeDir = new File("").getAbsoluteFile().toString()
    return addTrailingSlash(homeDir)
  }

  def static getScriptDir() {
    if (!scriptDir) scriptDir = getHomeDir() + "scripts/"
    return addTrailingSlash(scriptDir)
  }

  def static getLogDir() {
    if (!logDir) logDir = getHomeDir() + "log/"
    return addTrailingSlash(logDir)
  }

  private static addTrailingSlash(path) {
    if (path[path.size() - 1] != '/') path += '/'
    return path
  }

}