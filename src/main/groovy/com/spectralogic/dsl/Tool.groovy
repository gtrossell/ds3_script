package com.spectralogic.dsl

import ch.qos.logback.classic.Level
import com.spectralogic.ds3client.networking.FailedRequestException
import com.spectralogic.dsl.commands.ShellCommandFactory
import com.spectralogic.dsl.exceptions.BpException
import com.spectralogic.dsl.helpers.DslCompleter
import com.spectralogic.dsl.helpers.Globals
import com.spectralogic.dsl.helpers.LogRecorder
import com.spectralogic.ds3client.utils.Guard
import jline.console.ConsoleReader
import jline.console.UserInterruptException
import org.apache.http.conn.ConnectTimeoutException
import org.codehaus.groovy.runtime.InvokerHelper

/**
 * This is the main class for the Spectra DSL tool.
 * It Handles the terminal and handles all user interaction
 */
class Tool extends Script {
//  private final static logger = LoggerFactory.getLogger(Tool.class)
//  private final static recorder = new LogRecorder()

  static void main(String[] args) {
    InvokerHelper.runScript(Tool, args)
  }

  /** REPL handler */
  def run() {

    /* Cli to pass a script, turn on logging, or set log directory */
    def cli = new CliBuilder(usage:'bpsh <scripts> -l <log directory>', stopAtNonOption: false)
    cli.l('display all files', longOpt:'log', args:1, argName:'log file directory', optionalArg:true)
    def options = cli.parse(args)

    if (options.l) {
      if (options.l instanceof String) {
        Globals.logDir = options.l as String
      }
      LogRecorder.configureLogging(Level.ALL)
    } else {
      LogRecorder.configureLogging(Level.OFF)
    }

    def shell = new ShellBuilder().build(this.class.classLoader)

    /* Run script passed in */
    def arguments = options.arguments()
    if (arguments.size() > 0) {
      if (!arguments[0].endsWith('.groovy')) arguments[0] += '.groovy'
      def scriptArgs = arguments.size() > 1 ? arguments[1..-1] : []
      shell.run(new File(arguments[0]), scriptArgs)
      exit()
    }

    def console = new ConsoleReader()
    console.setPrompt(Globals.PROMPT)
    console.setHandleUserInterrupt(true)
    console.addCompleter(new DslCompleter(shell))
    println Globals.initMessage(console.getTerminal().getWidth())

    def commandFactory = new ShellCommandFactory(shell, console)

    try {
      while (true) {
        try {
          def line = console.readLine()

          LogRecorder.LOGGER.info("${Globals.PROMPT} $line")

          def result = evaluate(shell, line, commandFactory)

          LogRecorder.LOGGER.info("${Globals.RETURN_PROMPT} $result")
          console.println(Globals.RETURN_PROMPT + result)
        } catch (BpException | RuntimeException | FailedRequestException | ConnectTimeoutException | FileNotFoundException e) {
          if (e in UserInterruptException) exit()

          console.println(Globals.RETURN_PROMPT + e)
          console.println(e.getStackTrace().join('\n'))

          LogRecorder.LOGGER.error(e.toString())
          LogRecorder.LOGGER.error(e.getStackTrace().join('\n'))
        }
      }
    } catch (Exception e) {
      e.printStackTrace()
    } finally {
      exit()
    }
  }

  /** Logic for parsing and evaluating a line */
  private static String evaluate(shell, line, commandFactory) {
    if (Guard.isStringNullOrEmpty(line)) return ''
    
    if (line.startsWith(':')) {
      /* command */
      def args = line.split(' ')
      def command = args[0]
      args = 1 < args.size() ? args[1..-1] : []
      def response = commandFactory.runCommand(command, args)
//      response.log()
      
      if (response.exit) {
        exit()
      }

      return response.getMessage()
    } else {
      /* shell evaluation */
      return shell.evaluate(line)
    }
  }

  private static void exit() {
    LogRecorder.LOGGER.info("Exited normally.")
    System.exit(0)
  }

}
