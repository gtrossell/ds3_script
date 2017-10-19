package com.spectralogic.dsl.commands

class QuitCommand implements ShellCommand {

  @Override
  CommandResponse run(List args) {
    def response = new CommandResponse()
    response.exit = true
    return response
  }

  @Override
  String[] commandNames() {
    return [':quit', ':q']
  }

}