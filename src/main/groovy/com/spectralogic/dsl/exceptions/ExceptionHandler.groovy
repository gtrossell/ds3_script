package com.spectralogic.dsl.exceptions

import com.spectralogic.ds3client.networking.FailedRequestException
import com.spectralogic.dsl.Tool
import com.spectralogic.dsl.helpers.Globals
import com.spectralogic.dsl.helpers.LogRecorder
import jline.console.ConsoleReader
import jline.console.UserInterruptException
import org.apache.http.conn.ConnectTimeoutException

import java.nio.file.FileAlreadyExistsException
import java.nio.file.NoSuchFileException

class ExceptionHandler {
    private final ConsoleReader console
    private handlers = [
            (Throwable.class)                 : this.&printThrowable,
            (UserInterruptException.class)    : this.&userInterrupt,
            (BpException.class)               : this.&printMessage,
            (FailedRequestException.class)    : this.&printMessage,
            (FileNotFoundException.class)     : this.&printMessage,
            (ConnectTimeoutException.class)   : this.&printMessage,
            (MissingPropertyException.class)  : this.&printMessage,
            (AssertionError.class)            : this.&printMessage,
            (NoSuchFileException.class)       : this.&printMessage,
            (FileAlreadyExistsException.class): this.&printMessage
    ]

    ExceptionHandler(ConsoleReader console) {
        this.console = console
    }

    /** Handles any error, finding the nearest extended class that knows how to be handled */
    def handleAll(Throwable e) {
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

    private userInterrupt(Throwable e) { Tool.exit() }

    private printMessage(Throwable e) { printThrowable(e, false) }

    private printThrowable(Throwable e, trace=true) {
        console.println(Globals.RETURN_PROMPT + e.toString())
        LogRecorder.LOGGER.error(e.toString())

        if (trace) {
            def traceMessage = e.getStackTrace().join('\n')
            console.println(traceMessage)
            LogRecorder.LOGGER.trace(traceMessage)
        }
    }

}
