package com.spectralogic.dsl.helpers

import java.nio.file.Path
import java.nio.file.Paths

/** Provides methods to help the command classes */
class CommandHelper {

    /** @return script file from a string by parsing a path, '.', or no given path  */
    Path getScriptFromString(String pathStr) {
        def file
        if (!pathStr.contains('/')) {
            file = Paths.get(Globals.SCRIPT_DIR, pathStr)
        } else if (pathStr[0] == '.') {
            file = Paths.get(Globals.HOME_DIR, pathStr.substring(1))
        } else {
            file = Paths.get(pathStr)
        }

        return file
    }

}