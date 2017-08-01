package spectra

@GrabResolver(name='Spectra-Github', root='http://dl.bintray.com/spectralogic/ds3/')
@Grapes([
  @Grab(group='com.spectralogic.ds3', module='ds3-sdk', version='3.4.0'),
  @Grab(group='org.slf4j', module='slf4j-simple', version='1.7.21'),
  @GrabConfig(systemClassLoader = true)
])

import org.codehaus.groovy.runtime.InvokerHelper
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ImportCustomizer

import jline.TerminalFactory
import jline.console.ConsoleReader
import jline.console.completer.FileNameCompleter

import java.io.File
import java.io.IOException

import spectra.helpers.Environment
import spectra.helpers.Globals
import spectra.SpectraDSL

class Tool extends Script {

  /** Logic for parsing and evaluating a line */
  def evaluate(GroovyShell shell, String line) {
    if (line in [null, '']) return true
    if (line in ['exit', 'quit']) return false
    try {
      println Globals.RETURN_PROMPT + shell.evaluate(line)
    } catch (Exception e) {
      e.printStackTrace()
    }
    return true
  }

  def run() {
    def shell = new GroovyShell(this.class.classLoader, buildBinding(), buildConfig())
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

      while (evaluate(shell, console.readLine())) { }

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
    importCustomizer.addImport('com.spectralogic.ds3client.models.common.Credentials')
    importCustomizer.addImport('com.spectralogic.ds3client.Ds3ClientBuilder')
    importCustomizer.addStarImports('com.spectralogic.ds3client.commands')
    
    return importCustomizer
  }

  /** Builds shell configuration */
  private buildConfig() {
    def config = new CompilerConfiguration()
    config.addCompilationCustomizers(buildImportCustomizer())
    config.scriptBaseClass = 'spectra.SpectraDSL'
    return config
  }

  /** Builds shell binding */
  private buildBinding() {
    def binding = new Binding()
    if (new Environment().ready()) 
      binding.setVariable('client', Globals.createBpClient())

    binding
  }

}
