package com.spectralogic.dsl.helpers

/** Provides methods to help the command classes */
class CommandHelper {

    /** @return script file from a string by parsing a path, '.', or no given path  */
    File getScriptFromString(String pathStr) {
        def file
        if (!pathStr.contains('/')) {
            file = new File(Globals.SCRIPT_DIR, pathStr)
        } else if (pathStr[0] == '.') {
            file = new File(Globals.HOME_DIR, pathStr.substring(1))
        } else {
            file = new File(pathStr)
        }
        return file
    }

}