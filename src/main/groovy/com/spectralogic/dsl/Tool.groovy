package com.spectralogic.dsl

import com.spectralogic.dsl.commands.ShellCommandFactory
import com.spectralogic.dsl.exceptions.BpException
import com.spectralogic.dsl.helpers.Globals
import com.spectralogic.dsl.helpers.LogRecorder
import com.spectralogic.ds3client.utils.Guard
import groovy.lang.MissingMethodException
import groovy.lang.MissingPropertyException
import java.io.File
import java.io.IOException
import jline.console.ConsoleReader
import jline.TerminalFactory
import org.apache.http.conn.ConnectTimeoutException
import org.codehaus.groovy.runtime.InvokerHelper

/** 
 * This is the main class for the Spectra DSL tool.
 * It Handles the terminal and handles all user interaction
 */
class Tool extends Script {
  private final recorder

  static void main(String[] args) {
    InvokerHelper.runScript(Tool, args)
  }

  /** REPL handler */
  def run() {
    def shell = new ShellBuilder().build(this.class.classLoader)
    def commandFactory = new ShellCommandFactory(shell, recorder)
    recorder = new LogRecorder()
    
    recorder.init()
    // TODO: add autocomplete for file paths
    def console = new ConsoleReader()
    console.setPrompt(Globals.PROMPT)
    println Globals.initMessage(console.getTerminal().getWidth())

    try {
      /* Run script passed in */
      if (args.size() > 0) {
        // TODO: add groovy extension?
        def scriptArgs = args.size() > 1 ? args[1..args.size()-1] : []
        shell.run(new File(args[0]), scriptArgs)
      }

      def line
      def result
      while (true) {
        try {
          line = console.readLine()
          result = evaluate(shell, line, commandFactory)
          println Globals.RETURN_PROMPT + result
          recorder.record(line, result.toString())
        } catch (BpException | ConnectTimeoutException | MissingMethodException | 
                  MissingPropertyException | RuntimeException e) {
          logger.error(e)
        }
      }

    } catch (Exception e) {
      e.printStackTrace()
    } finally {
      exit()
    }
  }

  /** Logic for parsing and evaluating a line */
  private String evaluate(shell, line, commandFactory) { // TODO: return string only?
    if (new Guard().isStringNullOrEmpty(line)) return ''
    
    /* command */
    if (line.startsWith(':')) {
      def args = line.split(' ')
      def command = args[0]
      args = 1 < args.size() ? args[1..-1] : []
      def response = commandFactory.runCommand(command, args)
      response.log()
      
      if (response.exit) exit()
      return ''
    }

    /* shell evaluation */
    return shell.evaluate(line)
  }

  private void exit() {
    recorder.close()
    System.exit(0)
  }

}
