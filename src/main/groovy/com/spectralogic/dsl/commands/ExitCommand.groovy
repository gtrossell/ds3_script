package com.spectralogic.dsl.commands

class ExitCommand implements ShellCommand {

  CommandResponse run(List args) {
    def response = new CommandResponse()
    response.exit = true
    return response
  }

  String[] commandNames() {
    return [':exit'] 
  }

}