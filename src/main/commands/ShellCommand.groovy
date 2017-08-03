package spectra.commands

/** Interface for all shell commands */
interface ShellCommand {
  /** @return true if run was successful */
  // TODO: make enum or response object
  String run(args)

}
