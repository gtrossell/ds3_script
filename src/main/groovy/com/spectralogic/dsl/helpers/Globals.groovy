package com.spectralogic.dsl.helpers

import sun.util.logging.PlatformLogger

import java.util.prefs.Preferences

/**
 * Globals keeps all fields and functions that need to be accessed multiple
 * places or are constants
 */
class Globals {
  static debug = false
  final static PROMPT = 'spectra> '
  final static RETURN_PROMPT = '===> '
  final static MAX_BULK_LOAD = 200_000 // TODO: might not be the best place for this
  final static String HOME_DIR
  final static String SCRIPT_DIR
  private final static PREF_NODE = "ds3_script.preferences"
  private final static LOG_PREF_KEY = "LOG_DIR"
  private final static Preferences PREFS

  static {
    HOME_DIR = new File("").getAbsoluteFile().toString() + '/'
    SCRIPT_DIR = HOME_DIR

    /* There is a bug in Java 8 where the Preferences API doesn't work in Windows 8+, this line seems to fix it */
    PlatformLogger.getLogger("java.util.prefs").setLevel(PlatformLogger.Level.OFF)
    PREFS = Preferences.userRoot().node(PREF_NODE)
    if (!PREFS.keys().contains(LOG_PREF_KEY)) {
      PREFS.put(LOG_PREF_KEY, "")
    }
  }

  static String setLogDir(String logDir) {
    logDir = logDir.replaceAll('\\\\', '/')
    if (logDir[-1] != '/') logDir += '/'

    if (!new File(logDir).exists()) throw new FileNotFoundException(logDir)

    PREFS.put(LOG_PREF_KEY, logDir)

    LogRecorder.configureLogging(LogRecorder.LOGGER.level)
  }

  static String getLogDir() {
    return PREFS.get(LOG_PREF_KEY, "")
  }

  static initMessage(Integer width) {
    """
Welcome to the Spectra DSL for BlackPearl!
Use the ':help' command to get started.
${LogRecorder.loggerStatus()}
${'=' * (width - 1)}"""
  }

}