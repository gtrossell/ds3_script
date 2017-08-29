package com.spectralogic.dsl.commands

import com.spectralogic.dsl.helpers.CommandHelper
import com.spectralogic.dsl.helpers.Environment
import com.spectralogic.dsl.helpers.Globals
import com.spectralogic.dsl.helpers.LogRecorder
import org.apache.commons.io.FilenameUtils

class RecordCommand implements ShellCommand {
  CliBuilder cli
  LogRecorder recorder
  Environment environment
  String recordId
  File scriptFile
  String scriptDesc
  Boolean isRecording
  Boolean isRecordEnv

  RecordCommand(LogRecorder recorder, Environment environment) {
    this.recorder = recorder
    this.environment = environment
    init()

    cli = new CliBuilder(usage:':record, :r <name> [options]')
    cli.header = 'First usage starts recording, second saves'
    cli.e('save environment in script', longOpt: 'environment')
    cli.d('script description', longOpt: 'desc', args:1, argName:'description')
    cli.h('display this message', longOpt:'help')
  }

  CommandResponse run(args) {
    def response = new CommandResponse()
    if (!commandOptions(args, response).isEmpty()) return response

    if (!isRecording) {
      /* start recording */
      recordId = createScriptId()
      recorder.writeLine(startLine())
      isRecording = true
      return response.addInfo("Started recording script")
    } else {
      /* end recording */
      recorder.writeLine(endLine())
      def scriptLines = []
      def isScript = false
      recorder.getLogFile().eachLine { String line ->
        if (line == endLine()) return response
        if (isScript && line.startsWith(Globals.PROMPT)) {
          line = line.substring(Globals.PROMPT.size())
          if (!line.startsWith(':')) scriptLines << line
        }
        if (line == startLine()) isScript = true
      }

      def scriptLoc = saveScript(scriptLines)
      init()
      return response.addInfo("Saved script to '$scriptLoc'")
    }
  }

  String[] commandNames() {
    return [':record', ':r']
  }

  /** Builds script lines and saves it to set or given location */
  private String saveScript(scriptLines) {
    if (!scriptFile)
      scriptFile = new File(Globals.SCRIPT_DIR, "${recordId}.groovy")

    /* set environment vars */
    if (isRecordEnv) {
      def tmpEnv = "tmpEnvironmentFor_$recordId"
      def saveEnvLine = "$tmpEnv = environment"
      def envLineBuilder = new StringBuilder('environment = new Environment([')
      environment.getEnvironment().each{ key, val -> 
        envLineBuilder.append("'$key':'$val',")
      }
      envLineBuilder.setCharAt(envLineBuilder.length() - 1, ']' as char)
      envLineBuilder.append(')\n')

      scriptLines.add(0, envLineBuilder.toString())
      scriptLines.add(0, saveEnvLine)
      scriptLines << "\nenvironment = $tmpEnv"
    }

    /* create title comment */
    def scriptName = scriptFile.getName()
    if (scriptName.contains('.')) scriptName = scriptName.split('\\.')[0]
    scriptDesc = scriptDesc ?: 'Spectra BlackPearl DSL script'

    def final lineLength = 30
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

    scriptFile.toString()
  }

  /** Pads/divides a comment to keep the comment body uniform length */
  private String padComment(lineLength, text) {
    if (text.size() > lineLength) {
      /** split line up into shorter lines while not splitting words */
      def textLines = ['']
      def words = text.split(' ')
      words.each { word ->
        if (textLines[-1].size() + 1 + word.size() > lineLength) {
          textLines << word
        } else {
          textLines[-1] = textLines[-1] + ' ' + word
        }
      }

      return textLines.collect { padComment(lineLength, it.trim()) }.join()
    } else {
      def padding = lineLength - text.size()
      return "/* " + text + (' ' * padding) + " */\n"
    }
  }

  /** @return help message if requested or error message */
  private CommandResponse commandOptions(args, response) {
    def stringWriter = new StringWriter()
    cli.writer = new PrintWriter(stringWriter)
    def options = cli.parse(args)
    if (stringWriter.toString()) {
      return response.addInfo(stringWriter.toString())
    }

    if (options.arguments()) {
      def file = new CommandHelper().getScriptFromString(options.arguments()[0])
      file = ensureExtension(file)
      if (errorCheckFile(file, response).error) return response
      this.scriptFile = file
    }

    if (!options) {
      return response
    }
    if (options.h) {
      cli.usage()
      return response.setMessage(stringWriter.toString())
    }
    if (options.d) {
      this.scriptDesc = options.d
    }
    if (options.e) {
      this.isRecordEnv = true
    }
    return response
  }

  /** @return Error message if there is something wrong with the file location */
  private CommandResponse errorCheckFile(File file, CommandResponse response) {
    if (file.exists()) {
      return response.addError("The file $file already exists!")
    } else if (!file.getParentFile().exists()) {
      return response.addError("The directory ${file.getParent()} does not exist!")
    } else if (!(FilenameUtils.getExtension(file.toString()) in ['','groovy'])) {
      return response.addError("The script extension must be 'groovy' or none.")
    } else {
      return response
    }
  }

  /** @return same file but with .groovy extension if none is given */
  private File ensureExtension(File file) {
    if (!FilenameUtils.getExtension(file.toString())) {
      file = new File(file.toString() + '.groovy')
    }
    return file
  }

  /** sets fields to their default values */
  private void init() {
    this.recordId     = ''
    this.scriptDesc   = ''
    this.scriptFile   = null
    this.isRecording  = false
    this.isRecordEnv  = false

    File scriptDir = new File(Globals.SCRIPT_DIR)
    if (!scriptDir.exists()) scriptDir.mkdirs()
  }

  private startLine() { 
    return "[START] $recordId" 
  }

  private endLine() { 
    return "[END] $recordId"
  }

  /** Creates unique name for a recording session  */
  private createScriptId() {
    'script_' + (new Random().nextInt(10 ** 6))
  }
}
