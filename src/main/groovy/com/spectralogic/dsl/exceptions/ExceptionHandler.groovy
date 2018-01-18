package com.spectralogic.dsl.exceptions

import com.spectralogic.dsl.helpers.Globals
import com.spectralogic.dsl.helpers.LogRecorder
import jline.console.ConsoleReader

class ExceptionHandler {
    private final ConsoleReader console
    private handlers = [:]

    ExceptionHandler(ConsoleReader console) {
        this.console = console
        addHandler(Throwable.class, this.&defaultHandler)
    }

    /** Handles any error/exception, finding the nearest extended class that knows how to be handled */
    def handle(Throwable e) {
        def clazz = e.class
        while (true) {
            if (this.handlers[clazz]) {
                this.handlers[e.class] = this.handlers[clazz]
                this.handlers[e.class](e)
                return
            } else {
                clazz = clazz.superclass
            }
        }
    }

    /* Handler must accept specified throwable type as the parameter */
    def addHandler(Class<Throwable> throwableClass, Closure handler) {
        handlers[throwableClass] = handler
    }

    /* Prints exception message and trace if debug option is set */
    private defaultHandler(Throwable e) {
        console.println(Globals.RETURN_PROMPT + e.toString())
        LogRecorder.LOGGER.error(e.toString())

        if (Globals.debug) {
            def traceMessage = e.getStackTrace().join('\n')
            console.println(traceMessage)
            LogRecorder.LOGGER.trace(traceMessage)
        }
    }

}
