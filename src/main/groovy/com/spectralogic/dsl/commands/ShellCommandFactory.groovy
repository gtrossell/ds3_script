package com.spectralogic.dsl.commands

import com.spectralogic.dsl.helpers.Environment
import com.spectralogic.dsl.helpers.LogRecorder
import jline.console.ConsoleReader

class ShellCommandFactory {
  private List<ShellCommand> commands

  ShellCommandFactory(GroovyShell shell, ConsoleReader console) {
    commands = []
    commands << new HelpCommand()
    commands << new RecordCommand(shell.getVariable('environment') as Environment)
    commands << new ExecuteCommand(shell)
    commands << new ExitCommand()
    commands << new ClearCommand(console)
    commands << new LogCommand()
  }

  def runCommand(commandName, List args) {
    def command = commands.find { commandName.toLowerCase() in it.commandNames() }
    return command ? command.run(args) : new HelpCommand().run(args)
  }

}
