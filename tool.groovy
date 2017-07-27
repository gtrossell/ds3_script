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

import java.io.IOException

import jline.TerminalFactory
import jline.console.ConsoleReader
import jline.console.completer.FileNameCompleter

import spectra.*

class Tool extends Script {
  def run() {
    def shell = new GroovyShell(this.class.classLoader, buildBinding(), buildConfig())
    // TODO: add autocomplete for file paths
    try {
      def console = new ConsoleReader()
      console.setPrompt(Globals.PROMPT)
      println Globals.init_message(console.getTerminal().getWidth())
      def line
      while (line = console.readLine()) {
        try {
          println shell.evaluate(line)
        } catch (Exception e) {
          e.printStackTrace()
        }
      }
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
    config.scriptBaseClass = 'SpectraDSL'
    return config
  }

  /** Builds shell binding */
  private buildBinding() {
    new Binding()
  }

}
