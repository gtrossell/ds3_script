package spectra.commands

class HelpCommand implements ShellCommand {

  String run(args) {
    "The available commands are:\n" +
    "\t* :help\n" +
    "\t* :record\n" +
    "\n" +
    "Use '<command> -h' for help on a specific command"
  }

  String[] commandNames() { [':help', ':h'] }

}
