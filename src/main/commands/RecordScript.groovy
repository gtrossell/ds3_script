package spectra.commands

import java.util.Random

import spectra.helpers.Config
import spectra.helpers.Globals
import spectra.helpers.LogRecorder

class RecordScript implements ShellCommand {
  CliBuilder cli
  LogRecorder recorder
  String recordId
  File scriptFile
  String scriptName
  String scriptDesc
  Boolean isRecording
  Boolean isRecordEnv

  RecordScript(LogRecorder recorder) {
    this.recorder = recorder
    init()

    cli = new CliBuilder(usage:':record, :r [options]')
    cli.header = 'First usage starts recording, second saves'
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
      recordId = createScriptId()
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
        if (isScript && line.startsWith(Globals.PROMPT)) {
          line = line.substring(Globals.PROMPT.size())
          if (!line.startsWith(':')) scriptLines << line
        }
        if (line == startLine()) isScript = true
      }

      saveScript(scriptLines)
      init()
      return 'Finished recording'
    }
  }

  private saveScript(scriptLines) {
    if (!scriptFile)
      scriptFile = new File(Config.getScriptDir(), "${recordId}.groovy")

    /* set environment vars */
    // TODO: use the :env command

    /* create title comment */
    scriptName = scriptFile.getName()
    if (scriptName.contains('.')) scriptName = scriptName.split('\\.')[0]
    scriptDesc = scriptDesc ?: 'Spectra BlackPearl DSL script'

    def lineLength = 30
    def lineComment = { c -> "/*" + c * (lineLength + 2) + "*/\n" }
    def comment = (
      lineComment('*') +
      padComment(lineLength, scriptName) +
      lineComment('-') +
      padComment(lineLength, scriptDesc) +
      lineComment(' ') +
      padComment(lineLength, "Created on ${new Date().format('MM-dd-yyyy')}") +
      lineComment('*')
    )
    scriptLines.add(0, comment)

    scriptFile.write(scriptLines.join('\n'))
  }

  /** Pads/divides a comment to keep the comment body uniform length */
  private String padComment(Integer lineLength, String text) {
    if (text.size() > lineLength) {
      // split line up into shorter lines while not splitting words
      def textLines = ['']
      def words = text.split(' ')
      words.each { String word ->
        if (textLines[-1].size() + 1 + word.size() > lineLength) {
          textLines << word
        } else {
          textLines[-1] = textLines[-1] + ' ' + word
        }
      }

      def commentLines = []
      textLines.each { String line ->
        commentLines << padComment(lineLength, line.trim())
      }
      return commentLines.join()
    } else {
      def padding = lineLength - text.size()
      return "/* " + text + (' ' * padding) + " */\n"
    }
  }

  /** @return help message if requested or error message */
  private String commandOptions(args) {
    def options = cli.parse(args)
    def message = '' /* only used to store help/error messages */
    if (!options) return message
    if (options.h) {
      def stringWriter = new StringWriter()
      cli.writer = new PrintWriter(stringWriter)
      cli.usage()
      message += stringWriter.toString()
    }
    if (options.f) {
      def pathStr = options.f
      // TODO: make sure it ends in .groovy?
      if (!pathStr.contains('/')) {
        this.scriptFile = new File(Config.getScriptDir(), pathStr)
      } else if (pathStr[0] == '.') {
        this.scriptFile = new File(Config.getHomeDir(), pathStr.substring(1))
      } else {
        this.scriptFile = new File(pathStr)
      }
      // TODO: parse name
      // TODO: check if directory exists and throw an error if it doesn't
      // TODO: error if file exists already
    }
    if (options.d) {
      this.scriptDesc = options.d
    }
    if (options.e) {
      this.isRecordEnv = true
    }
    return message
  }

  /** sets fields to their default values */
  private void init() {
    this.recordId     = ''
    this.scriptDesc   = ''
    this.scriptName   = ''
    this.scriptFile   = null
    this.isRecording  = false
    this.isRecordEnv  = false

    File scriptDir = new File(Config.getScriptDir())
    if (!scriptDir.exists()) scriptDir.mkdirs()
  }

  private startLine() { "[START] $recordId" }
  private endLine() { "[END] $recordId" }

  /** Creates unique name for a recording session  */
  private createScriptId() {
    'script_' + (new Random().nextInt(10 ** 6))
  }
}
