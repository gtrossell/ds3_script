package com.spectralogic.dsl.commands

class HelpCommand implements ShellCommand {

  @Override
  CommandResponse run(List args) {
    return new CommandResponse().setMessage("The available commands are:\n" +
      "\t* :help, :h\n" +
      "\t* :record, :r\n" +
      "\t* :execute, :e\n" +
      "\t* :clear, :c\n" +
      "\t* :exit\n" +
      "\n" +
      "Use '<command> -h' for help on a specific command\n" +
      "\n" +
      "The default variables are:\n" +
      "\t* client -> client created from environment variables\n" +
      "\t* environment -> environment created from environment variables\n")
  }

  @Override
  String[] commandNames() {
    return [':help', ':h']
  }

}
