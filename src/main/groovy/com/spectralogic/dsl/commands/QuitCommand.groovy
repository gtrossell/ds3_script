package com.spectralogic.dsl.commands

import jline.console.UserInterruptException

class QuitCommand implements ShellCommand {

  @Override
  CommandResponse run(List args) {
    throw new UserInterruptException('')
  }

  @Override
  String[] commandNames() {
    return [':quit', ':q']
  }

}