package spectra.commands

class HelpCommand implements ShellCommand {

  String run(args) {
    if (args) {
      "nothing atm\n"
    } else {
      "The available commands are:\n" +
      "\t- :help\n" +
      "\t- :record\n" +
      "\n" +
      ":help <command>\n"
    }
  }

  String help() {

  }

  String[] commandNames() { [':help', ':h'] }

}
