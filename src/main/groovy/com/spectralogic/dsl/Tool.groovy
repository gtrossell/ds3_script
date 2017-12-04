package com.spectralogic.dsl

import ch.qos.logback.classic.Level
import com.spectralogic.dsl.commands.ShellCommandFactory
import com.spectralogic.dsl.exceptions.ExceptionHandler
import com.spectralogic.dsl.helpers.DslCompleter
import com.spectralogic.dsl.helpers.Globals
import com.spectralogic.dsl.helpers.LogRecorder
import com.spectralogic.ds3client.utils.Guard
import jline.console.ConsoleReader
import org.codehaus.groovy.runtime.InvokerHelper

/**
 * This is the main class for the Spectra DSL tool.
 * It Handles the terminal and handles all user interaction
 */
class Tool extends Script {
    private final static ConsoleReader console = new ConsoleReader()

    static void main(String[] args) {
        InvokerHelper.runScript(Tool, args)
    }

    /** REPL handler */
    def run() {
        LogRecorder.configureLogging(Level.OFF)
        final shell = new ShellBuilder().build(this.class.classLoader)

        argsOptions(shell)

        console.setPrompt(Globals.PROMPT)
        console.setHandleUserInterrupt(true)
        console.addCompleter(new DslCompleter(shell))
        console.println(Globals.initMessage(console.getTerminal().getWidth()))
        console.setHistory(Globals.fetchHistory())

        def commandFactory = new ShellCommandFactory(shell, console)
        def exceptionHandler = new ExceptionHandler(console)

        while (true) {
            try {
                def line = console.readLine()
                LogRecorder.LOGGER.info(Globals.PROMPT + line)

                def result = evaluate(shell, line, commandFactory)
                printResult(result)
            } catch (Throwable e) {
                exceptionHandler.handleAll(e)
            }
        }
    }

    private void argsOptions(GroovyShell shell) {
        /* Cli to run a script, turn on logging, log directory, print version */
        def cli = new CliBuilder(usage: 'bpsh <script>', stopAtNonOption: false)
        cli.l('enable logging', longOpt: 'log', args: 1, argName: 'log directory', optionalArg: true)
        cli.v('version', longOpt: 'version')
        cli.h('display this message', longOpt: 'help')
        cli.d('enable debugging', longOpt: 'debug')
        def options = cli.parse(args)

        if (!options) {
            return
        } else if (options.h) {
            cli.usage()
            exit()
        } else if (options.v) {
            def properties = new Properties()
            this.class.classLoader.getResource("version.properties").withInputStream { properties.load(it) }
            println "bpsh ${properties.version}"
            exit()
        }

        if (options.l) {
            if (options.l instanceof String) {
                try {
                    Globals.logDir = options.l as String
                } catch (FileNotFoundException e) {
                    println e
                    exit()
                }
            }
            LogRecorder.configureLogging(Level.ALL)
        }

        if (options.d) {
            Globals.debug = true
        }

        /* Run script passed in */
        def arguments = options.arguments()
        if (arguments.size() > 0) {
            if (!arguments[0].endsWith('.groovy')) arguments[0] += '.groovy'
            def scriptArgs = arguments.size() > 1 ? arguments[1..-1] : []

            try {
                def file = new File(arguments[0])
                shell.run(file, scriptArgs)
            } catch (FileNotFoundException e) {
                println e
            } finally {
                exit()
            }
        }
    }

    private printResult(String text) {
        LogRecorder.LOGGER.info(Globals.RETURN_PROMPT + text)
        console.println(Globals.RETURN_PROMPT + text)
    }

    private printThrowable(Throwable e, trace = false) {
        console.println(Globals.RETURN_PROMPT + e.toString())
        LogRecorder.LOGGER.error(e.toString())

        if (trace) {
            def traceMessage = e.getStackTrace().join('\n')
            console.println(traceMessage)
            LogRecorder.LOGGER.trace(traceMessage)
        }
    }

    private printAssertionError(AssertionError e) {
        def consoleMessage = e.message.replaceAll('\n', "\n${' ' * Globals.RETURN_PROMPT.length()}")
        console.println(Globals.RETURN_PROMPT + consoleMessage)
        LogRecorder.LOGGER.error(e.message)
    }

    /** Logic for parsing and evaluating a line */
    private static String evaluate(shell, line, commandFactory) {
        if (Guard.isStringNullOrEmpty(line)) return ''

        if (line.startsWith(':')) {
            /* command */
            def args = line.split(' ')
            def command = args[0]
            args = 1 < args.size() ? args[1..-1] : []
            return commandFactory.runCommand(command, args).getMessage()
        } else {
            /* shell evaluation */
            return shell.evaluate(line)
        }
    }

    static exit() {
        Globals.saveHistory(console.history)
        LogRecorder.LOGGER.info(Globals.getString('exit_message'))
        System.exit(0)
    }

}
