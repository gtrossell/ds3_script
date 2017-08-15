package com.spectralogic.dsl.commands

import com.spectralogic.dsl.helpers.LogRecorder

class ShellCommandFactory {
  List<ShellCommand> commands

  ShellCommandFactory(GroovyShell shell, LogRecorder recorder) {
    commands = []
    commands << new HelpCommand()
    commands << new RecordCommand(recorder, shell.getVariable('environment'))
    commands << new ExecuteCommand(shell)
    commands << new ExitCommand()
  }

  def runCommand(commandName, args) {
    def execCommand = { name ->
      name = name.toLowerCase()
      def command = commands.find{ name in it.commandNames() }
      return command ? command.run(args) : null
    }

    return execCommand(commandName) ?: execCommand(':help')
  }

}
