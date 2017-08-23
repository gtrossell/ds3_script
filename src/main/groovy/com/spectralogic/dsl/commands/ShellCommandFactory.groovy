package com.spectralogic.dsl.commands

import com.spectralogic.dsl.helpers.LogRecorder

class ShellCommandFactory {
  private List<ShellCommand> commands

  ShellCommandFactory(GroovyShell shell, LogRecorder recorder) {
    commands = []
    commands << new HelpCommand()
    commands << new RecordCommand(recorder, shell.getVariable('environment'))
    commands << new ExecuteCommand(shell)
    commands << new ExitCommand()
  }

  def runCommand(commandName, args) {
    def command = commands.find { commandName.toLowerCase() in it.commandNames() }
    return command ? command.run(args) : new HelpCommand().run()
  }

}
