package com.spectralogic.dsl.commands

/** Interface for all shell commands */
interface ShellCommand {

  /** main method */
  CommandResponse run(List args)

  /** @return array of strings that this command will run on */
  String[] commandNames()

}
