package spectra.commands

import spectra.helpers.LogRecorder

class ShellCommandFactory {
  LogRecorder recorder
  RecordScript recordCommand

  ShellCommandFactory(LogRecorder recorder) {
    this.recorder = recorder
    recordCommand = new RecordScript(recorder)
  }

  def runCommand(command, args) {
    def commandObj = null
    switch (command) {
      case [':record', ':r']:
        commandObj = recordCommand
        break
      default:
        // TODO: add help command
        println "Command $command is unknown."
        break
    }

    return commandObj.run(args)
  }

}
