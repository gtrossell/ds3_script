package spectra.commands

import spectra.helpers.LogRecorder

class ShellCommandFactory {
  List<ShellCommand> commands

  ShellCommandFactory(GroovyShell shell, LogRecorder recorder) {
    commands = []
    commands << new HelpCommand()
    commands << new RecordCommand(recorder, shell.getVariable('environment'))
    commands << new ExecuteCommand(shell)
  }

  def runCommand(commandName, args) {
    def execCommand = { name ->
      name = name.toLowerCase()
      for (def i = 0; i < commands.size(); i++)
        if (name in commands[i].commandNames()) return commands[i].run(args)
      return null
    }

    return execCommand(commandName) ?: execCommand(':help')
  }

}
