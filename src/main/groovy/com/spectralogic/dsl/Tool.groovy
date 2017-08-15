package com.spectralogic.dsl

import com.spectralogic.dsl.commands.ShellCommandFactory
import com.spectralogic.dsl.helpers.Environment
import com.spectralogic.dsl.helpers.Globals
import com.spectralogic.dsl.helpers.LogRecorder
import java.io.File
import java.io.IOException
import jline.console.ConsoleReader
import jline.TerminalFactory
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ImportCustomizer
import org.codehaus.groovy.runtime.InvokerHelper

/** 
 * This is the main class for the Spectra DSL tool.
 * It Handles the terminal and handles all user interaction
 */
class Tool extends Script {
  ShellCommandFactory commandFactory

  /** Logic for parsing and evaluating a line */
  def evaluate(GroovyShell shell, String line) {
    if (line in [null, '']) return true
    
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
      shell.evaluate(line)
    } catch (Exception e) {
      e.printStackTrace()
    }
  }

  def run() {
    def shell = new GroovyShell(this.class.classLoader, buildBinding(), buildConfig())
    def recorder = new LogRecorder()
    commandFactory = new ShellCommandFactory(shell, recorder)
    
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
      while (true) {
        line = console.readLine()
        result = evaluate(shell, line)
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

  /** Builds object to pass imports into the shell */
  private buildImportCustomizer() {
    def importCustomizer = new ImportCustomizer()
    importCustomizer.addImport('com.spectralogic.dsl.helpers.Environment')
    return importCustomizer
  }

  /** Builds shell configuration */
  private buildConfig() {
    def config = new CompilerConfiguration()
    config.addCompilationCustomizers(buildImportCustomizer())
    config.scriptBaseClass = 'com.spectralogic.dsl.SpectraDSL'
    return config
  }

  /** Builds shell binding */
  private buildBinding() {
    def binding = new Binding()
    def environment = new Environment()
    if (environment.ready()) {
      binding.setVariable('client', Globals.createBpClient())
    }
    binding.setVariable('environment', environment)
    return binding
  }

}
