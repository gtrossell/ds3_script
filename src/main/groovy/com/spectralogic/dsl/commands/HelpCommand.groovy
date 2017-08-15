package com.spectralogic.dsl.commands

class HelpCommand implements ShellCommand {

  CommandResponse run(args) {
    new CommandResponse().addInfo("The available commands are:\n" +
      "\t* :help, :h\n" +
      "\t* :record, :r\n" +
      "\t* :execute, :e\n" +
      "\t* :exit\n"
      "\n" +
      "Use '<command> -h' for help on a specific command\n" +
      "\n" +
      "The default variables are:\n" +
      "\t* client -> client created from environment variables\n" +
      "\t* environment -> environment created from environment variables\n")
  }

  String[] commandNames() {
    [':help', ':h']
  }

}
