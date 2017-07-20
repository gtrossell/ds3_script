package spectra

@GrabResolver(name='Spectra-Github', root='http://dl.bintray.com/spectralogic/ds3/')
@Grapes([
  @Grab(group='com.spectralogic.ds3', module='ds3-sdk', version='3.4.0'),
  @Grab(group='org.slf4j', module='slf4j-simple', version='1.7.21')
])

import org.codehaus.groovy.runtime.InvokerHelper
import org.codehaus.groovy.control.CompilerConfiguration

import java.io.IOException

import jline.TerminalFactory
import jline.console.ConsoleReader
import jline.console.completer.FileNameCompleter

import spectra.*
import static spectra.ClientCommands.*

class Tool extends Script {
  def run() {
    def binding = new Binding()
    def config = new CompilerConfiguration()
    config.scriptBaseClass = 'SpectraDSL'
    def shell = new GroovyShell(this.class.classLoader, binding, config)
    
    // Set enums as shell variables
    binding.setVariable('info', info)

    try {
      def console = new ConsoleReader()
      // console.addCompleter(new FileNameCompleter())
      console.setPrompt(Globals.PROMPT)
      println Globals.init_message(console.getTerminal().getWidth())
      def line
      // shell.run("show the client info")
      while (line = console.readLine()) {
        // if (line.split(' ').length % 2 == 1) line = line.trim() + '()'
        println shell.evaluate(line).call()
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
}
