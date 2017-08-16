package com.spectralogic.dsl

import com.spectralogic.dsl.commands.ShellCommandFactory
import com.spectralogic.dsl.helpers.Globals
import com.spectralogic.dsl.helpers.LogRecorder
import com.spectralogic.ds3client.utils.Guard
import java.io.File
import java.io.IOException
import jline.console.ConsoleReader
import jline.TerminalFactory
import org.codehaus.groovy.runtime.InvokerHelper

/** 
 * This is the main class for the Spectra DSL tool.
 * It Handles the terminal and handles all user interaction
 */
class Tool extends Script {

  /** Logic for parsing and evaluating a line */
  def evaluate(shell, line, commandFactory) { // TODO: return string only?
    if (new Guard().isStringNullOrEmpty(line)) return true
    
    /* command */
    if (line[0] == ':') {
      def args = line.split(' ')
      def command = args[0]
      args = 1 < args.size() ? args[1..args.size()-1] : []
      def response = commandFactory.runCommand(command, args)
      response.log()
      if (response.exit) System.exit(0) // TODO
      return ''
    }

    /* shell evaluation */
    try {
      return shell.evaluate(line)
    } catch (Exception e) {
      e.printStackTrace()
    }
  }

  def run() {
    def shell = new ShellBuilder().build(this.class.classLoader)
    def recorder = new LogRecorder()
    def commandFactory = new ShellCommandFactory(shell, recorder)
    
    recorder.init()
    // TODO: add autocomplete for file paths
    try {
      def console = new ConsoleReader()
      console.setPrompt(Globals.PROMPT)
      println Globals.initMessage(console.getTerminal().getWidth())

      /* Run script passed in */
      if (args.size() > 0) {
        def scriptArgs = args.size() > 1 ? args[1..args.size()-1] : []
        shell.run(new File(args[0]), scriptArgs)
      }

      def line
      def result
      while (true) { //try catch
        line = console.readLine()
        result = evaluate(shell, line, commandFactory)
        println Globals.RETURN_PROMPT + result
        recorder.record(line, result.toString())
      }
      recorder.close()

    } catch (IOException e) {
      e.printStackTrace()
    } finally {
      try {
        TerminalFactory.get().restore()
      } catch (Exception e) {
        e.printStackTrace()
      }
    }
  }

  static void main(String[] args) {
    InvokerHelper.runScript(Tool, args)
  }

}
