package com.spectralogic.dsl

import ch.qos.logback.classic.Level
import com.spectralogic.dsl.commands.ShellCommandFactory
import com.spectralogic.dsl.exceptions.ExceptionHandler
import com.spectralogic.dsl.helpers.DslCompleter
import com.spectralogic.dsl.helpers.Globals
import com.spectralogic.dsl.helpers.LogRecorder
import com.spectralogic.ds3client.utils.Guard
import com.spectralogic.dsl.helpers.MultilineShellHelper
import jline.console.ConsoleReader
import jline.console.UserInterruptException
import org.codehaus.groovy.runtime.InvokerHelper
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * This is the main class for the Spectra DSL tool.
 * It Handles the terminal and handles all user interaction
 */
class Tool extends Script {
    private final static ConsoleReader console = new ConsoleReader()
    private final static Logger LOG = LoggerFactory.getLogger(Tool.class)
    private static MultilineShellHelper multiline = new MultilineShellHelper()

    static void main(String[] args) {
        System.addShutdownHook {
            Globals.saveHistory(console.history)
            LOG.info(Globals.getString('exit_message'))
        }

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
        exceptionHandler.addHandler((UserInterruptException.class), { exit() })

        while (true) {
            try {
                def line = console.readLine()
                LOG.info(Globals.PROMPT + line)

                def result = evaluate(shell, line, commandFactory)
                printResult(result)
            } catch (Throwable e) {
                exceptionHandler.handle(e)
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
        }

        if (options.h) {
            cli.usage()
            exit()
        }

        if (options.v) {
            def properties = new Properties()
            this.class.classLoader.getResource("version.properties").withInputStream { properties.load(it) }
            println "bpsh ${properties.version}"
            exit()
        }

        if (options.l) {
            setupLogger(options.l)
        }

        if (options.d) {
            Globals.debug = true
        }

        /* Run script passed in */
        def arguments = options.arguments()
        if (arguments.size() > 0) {
            def scriptName = arguments[0]
            if (!scriptName.endsWith('.groovy')) scriptName += '.groovy'

            LOG.info("Running script $scriptName")

            def scriptArgs = arguments.size() > 1 ? arguments[1..-1] : []

            def stringWriter = new StringWriter()
            shell.context.setProperty('out', new PrintWriter(stringWriter))

            def logOutput = { output -> LOG.info("Script output:\n$output") }

            try {
                def file = new File(scriptName)
                shell.setVariable('args', scriptArgs)
                shell.evaluate(file)

                println stringWriter.toString()
                logOutput(stringWriter.toString())
            } catch (Throwable e) {
                println stringWriter.toString()
                println e

                logOutput(stringWriter.toString())
                LOG.error(e.message)

                if (Globals.debug) {
                    def stackTrace = e.stackTrace.join('\n')
                    println stackTrace
                    LOG.error(stackTrace)
                }
            } finally {
                exit()
            }
        }
    }

    private printResult(String text) {
        if (!multiline.isMultiline()) {
            LOG.info(Globals.RETURN_PROMPT + text)
            console.println(Globals.RETURN_PROMPT + text)
        }
    }

    private setupLogger(logDir) {
        if (logDir instanceof String) {
            try {
                Globals.logDir = logDir as String
            } catch (FileNotFoundException e) {
                println e
                exit()
            }
        }
        LogRecorder.configureLogging(Level.ALL)
    }

    /** Logic for parsing and evaluating a line */
    private static String evaluate(shell, line, commandFactory) {
        if (Guard.isStringNullOrEmpty(line)) {
            multiline.reset()
            return ''
        }

        if (line.startsWith(':')) {
            /* command */
            def args = line.split(' ')
            def command = args[0]
            args = 1 < args.size() ? args[1..-1] : []
            return commandFactory.runCommand(command, args).getMessage()
        } else if (multiline.isMultiline() || multiline.startMultiline(line)) {
            multiline.addLine(line)
            if (multiline.isComplete()) {
                return shell.evaluate(multiline.getExpression())
            }
            return ''
        } else {
            /* shell evaluation */
            return shell.evaluate(line)
        }
    }

    static exit(Integer status=0) { System.exit(status) }

}
