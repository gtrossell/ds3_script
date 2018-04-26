package com.spectralogic.dsl.helpers

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import jline.console.history.History
import jline.console.history.MemoryHistory
import sun.util.logging.PlatformLogger

import java.nio.file.Files
import java.nio.file.Paths
import java.util.prefs.Preferences

/**
 * Globals keeps all fields and functions that need to be accessed multiple
 * places or are constants
 */
class Globals {
    static debug = false
    final static MAX_BULK_LOAD = 100_000
    final static OBJECT_PAGE_SIZE = 1_000
    final static String PROMPT
    final static String RETURN_PROMPT
    final static String HOME_DIR
    final static String SCRIPT_DIR
    private final static Preferences PREFS
    private final static PREF_NODE = "ds3_script.preferences"
    private final static LOG_PREF_KEY = "LOG_DIR"
    private final static HISTORY_PREF_KEY = "HISTORY"
    private final static HISTORY_LIMIT = 100
    private final static STRINGS_BUNDLE = ResourceBundle.getBundle('strings')

    static {
        HOME_DIR = new File("").getAbsoluteFile().toString() + '/'
        SCRIPT_DIR = HOME_DIR

        /* There is a bug in Java 8 where the Preferences API doesn't work in Windows 8+, this line seems to fix it */
        PlatformLogger.getLogger("java.util.prefs").setLevel(PlatformLogger.Level.OFF)
        PREFS = Preferences.userRoot().node(PREF_NODE)
        if (!PREFS.keys().contains(LOG_PREF_KEY)) {
            PREFS.put(LOG_PREF_KEY, "")
        }

        if (!PREFS.keys().contains(HISTORY_PREF_KEY) || ['', '{}'].contains(PREFS.get(HISTORY_PREF_KEY, ''))) {
            PREFS.put(HISTORY_PREF_KEY, "[]")
        }

        /* Set Global strings by locale */
        PROMPT = getString('prompt')
        RETURN_PROMPT = getString('return_prompt')
    }

    static String getString(String key) {
        return STRINGS_BUNDLE.getString(key)
    }

    static History fetchHistory() {
        def history = new MemoryHistory()
        new JsonSlurper().parseText(PREFS.get(HISTORY_PREF_KEY, '[]')).each { history.add(it.toString()) }
        return history
    }

    static void saveHistory(History history) {
        def historyLines =  history.collect { it.value() }.takeRight(HISTORY_LIMIT)
        PREFS.put(HISTORY_PREF_KEY, JsonOutput.toJson(historyLines))
        PREFS.flush()
    }

    static String setLogDir(String logDir) {
        // TODO: test
        logDir = logDir.replaceAll('\\\\', '/') // TODO: DELETE
        if (logDir[-1] != '/') logDir += '/'

        def path = Paths.get(logDir)
        if (!Files.exists(path)) throw new FileNotFoundException(logDir)

        PREFS.put(LOG_PREF_KEY, logDir)
        PREFS.flush()

        LogRecorder.configureLogging()
    }

    static String getLogDir() {
        return PREFS.get(LOG_PREF_KEY, "")
    }

    static initMessage(Integer width) {
        def message = """
${getString('welcome_message')}
${LogRecorder.loggerStatus()}"""

        if (!new Environment().ready()) {
            message += "\n${getString('set_env_message')}"
        }

        return "$message\n${'=' * (width - 1)}"
    }

}