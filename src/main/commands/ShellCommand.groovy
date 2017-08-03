package spectra.commands

/** Interface for all shell commands */
interface ShellCommand {
  /** @return true if run was successful */
  // TODO: make enum or response object
  String run(args)

  /** @return statement to help the user with the command */
  String help()

  /** @return array of strings that this command will run on */
  String[] commandNames()

}
