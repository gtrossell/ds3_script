package spectra.commands

import java.util.Random

import spectra.helpers.Globals
import spectra.helpers.LogRecorder

class RecordScript implements ShellCommand {
  CliBuilder cli
  LogRecorder recorder
  String recordId
  String scriptLoc
  String scriptDesc
  Boolean isRecording
  Boolean isRecordEnv

  def RecordScript(LogRecorder recorder) {
    this.recorder = recorder
    initFields()

    cli = new CliBuilder(usage:':record, :r [options]')
    cli.e('record environment variables to script', longOpt: 'environment')
    cli.f('file name and/or location', longOpt: 'file', args:1, argName:'name')
    cli.d('script description', longOpt: 'desc', args:1, argName:'description')
    cli.h('display this message', longOpt:'help')
  }

  String run(args) {
    def message = commandOptions(args)
    if (message) return message

    if (!isRecording) {
      /* start recording */
      recordId = createSessionId()
      recorder.writeLine(startLine())
      isRecording = true
      return 'Starting recording'
    } else {
      /* end recording */
      recorder.writeLine(endLine())
      def scriptLines = []
      def isScript = false
      recorder.getLogFile().eachLine { String line ->
        if (line == endLine()) return
        if (isScript && line.startsWith(Globals.PROMPT) && !line.startsWith(':'))
          scriptLines << line.substring(Globals.PROMPT.size()-1)
        if (line == startLine()) isScript = true
      }

      saveScript(scriptLines)
      initFields()
      return 'Finished recording'
    }
  }

  private saveScript(scriptLines) {
    println "Saving script"
  }

  /** @return help message if requested or error message */
  private String commandOptions(args) {
    def options = cli.parse(args)
    if (!options) return ''
    if (options.h) {
      def stringWriter = new StringWriter()
      cli.writer = new PrintWriter(stringWriter)
      cli.usage()
      return stringWriter.toString()
    }
    if (options.f) {
      this.scriptLoc = options.f
    }
    if (options.d) {
      this.scriptDesc = options.d
    }
    if (options.e) {
      this.isRecordEnv = true
    }
    return ''
  }

  /** sets fields to their default values */
  private void initFields() {
    this.recordId     = ''
    this.scriptLoc    = ''
    this.scriptDesc   = ''
    this.isRecording  = false
    this.isRecordEnv  = false
  }

  private startLine() { "[START] $recordId" }
  private endLine() { "[END] $recordId" }

  /** Creates unique name for a recording session  */
  private createSessionId() {
    'recording_session_' + (new Random().nextInt(10 ** 6))
  }
}
