package com.spectralogic.dsl.commands

class ExitCommand implements ShellCommand {

  CommandResponse run(args) {
    def response = new CommandResponse()
    response.exit = true
    response
  }

  String[] commandNames() {
    [':exit'] 
  }

}