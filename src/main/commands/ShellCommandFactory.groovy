package spectra.commands

import spectra.helpers.LogRecorder

class ShellCommandFactory {
  LogRecorder recorder
  List<ShellCommand> commands

  ShellCommandFactory(LogRecorder recorder) {
    this.recorder = recorder
    commands = []
    commands << new HelpCommand()
    commands << new RecordCommand(recorder)
  }

  def runCommand(commandName, args) {
    def execCommand = { name -> 
      for (def i = 0; i < commands.size(); i++)
        if (name in commands[i].commandNames()) return commands[i].run(args)
      return null
    }

    return execCommand(commandName) ?: execCommand(':help')
  }

}
