package spectra.commands

import org.apache.commons.io.FilenameUtils

import spectra.helpers.CommandHelper

class ExecuteCommand implements ShellCommand {
  GroovyShell shell
  CliBuilder cli

  ExecuteCommand(GroovyShell shell) {
    this.shell = shell

    cli = new CliBuilder(usage:':execute, :e <script>')
    cli.header = 'Execute a script'
    cli.h('display this message', longOpt:'help')
  }

  String[] commandNames() { [':execute', ':e'] }

  String run(args) {
    def message = commandOptions(args)
    if (message) return message

    def scriptName = args[0]
    if (FilenameUtils.getExtension(scriptName) == '') scriptName += '.groovy'

    def script = new CommandHelper().getScriptFromString(scriptName)
    if (!script.exists()) return "[Error] The script $script does not exists!\n"
    
    // TODO: fix bug where if script is edited, the shell doesn't realize it
    def scriptArgs = args.size() > 1 ? args[1..args.size()-1] : []
    shell.run(script, scriptArgs)
    return true
  }

  /** @return help message if requested or error message */
  private commandOptions(args) {
    def stringWriter = new StringWriter()
    cli.writer = new PrintWriter(stringWriter)
    def options = cli.parse(args)
    if (stringWriter.toString()) return stringWriter.toString()

    if (!options) return ''
    if (options.h || args.size() < 1) {
      cli.usage()
      return stringWriter.toString()
    }
  }
}
