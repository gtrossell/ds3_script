package com.spectralogic.dsl.commands

import com.spectralogic.dsl.helpers.CommandHelper
import com.spectralogic.dsl.helpers.Environment
import com.spectralogic.dsl.helpers.Globals
import jline.console.ConsoleReader
import org.apache.commons.io.FilenameUtils

import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.Files

class RecordCommand implements ShellCommand {
    private CliBuilder cli
    private Environment environment
    private int startLineIndex
    private String scriptName
    private Path scriptFile
    private String scriptDesc
    private Boolean isRecording
    private Boolean isRecordEnv
    private Boolean overwrite
    private ConsoleReader console

    RecordCommand(Environment environment, console) {
        this.environment = environment
        this.console = console
        init()

        cli = new CliBuilder(usage: ':record, :r <name> [options]', stopAtNonOption: false)
        cli.header = 'First usage starts recording, second saves'
        cli.e('save environment in script', longOpt: 'environment')
        cli.d('script description', longOpt: 'desc', args: 1, argName: 'description')
        cli.h('display this message', longOpt: 'help')
        cli.o('overwrite script with the same name', longOpt: 'overwrite')
    }

    @Override
    String[] commandNames() {
        return [':record', ':r']
    }

    @Override
    CommandResponse run(List args) {
        def response = new CommandResponse()
        if (!commandOptions(args, response).isEmpty()) return response

        if (!isRecording) /* start recording */ {
            startLineIndex = console.history.size()
            isRecording = true
            return response.addInfo("Started recording script")
        } else /* end recording */ {
            if (scriptName.empty) {
                return response.addError("Must give script a name using :record <name>")
            }

            def endLineIndex = console.history.entries().size() - 1
            def scriptLines = console.history.entries().findAll {
                startLineIndex <= it.index() && it.index() < endLineIndex && !it.value().toString().startsWith(':')
            }.collect { it.value() }

            def scriptLoc = saveScript(scriptLines)
            init()
            return response.addInfo("Saved script to '$scriptLoc'")
        }
    }

    /** sets fields to their default values */
    private void init() {
        this.scriptDesc = ''
        this.startLineIndex = -1
        this.scriptName = ''
        this.scriptFile = null
        this.isRecording = false
        this.isRecordEnv = false
        this.overwrite = false

        def scriptDir = Paths.get(Globals.SCRIPT_DIR)
        if (!Files.exists(scriptDir)) Files.createDirectory(scriptDir)
    }

    /** Builds script lines and saves it to set or given location */
    private String saveScript(List scriptLines) {

        /* set environment vars */
        if (isRecordEnv) {
            def tmpEnv = "tmpEnvironmentFor_$scriptName"
            def saveEnvLine = "$tmpEnv = environment"
            def envLineBuilder = new StringBuilder('environment = new Environment([')
            environment.getEnvironment().each { key, val ->
                envLineBuilder.append("'$key':'$val',")
            }
            envLineBuilder.setCharAt(envLineBuilder.length() - 1, ']' as char)
            envLineBuilder.append(')\n')

            scriptLines.add(0, envLineBuilder.toString())
            scriptLines.add(0, saveEnvLine)
            scriptLines << "\nenvironment = $tmpEnv"
        }

        /* create title comment */
        def scriptName = scriptFile.fileName.toString()
        if (scriptName.contains('.')) scriptName = scriptName.split('\\.')[0]
        scriptDesc = scriptDesc ?: 'Spectra BlackPearl DSL script'

        def final lineLength = 30
        def lineComment = { String c -> "/*" + c * (lineLength + 2) + "*/\n" }
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
    private String padComment(Integer lineLength, String text) {
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

            return textLines.collect { padComment(lineLength, it.trim()) }.join('')
        } else {
            def padding = lineLength - text.size()
            return "/* " + text + (' ' * padding) + " */\n"
        }
    }

    /** @return help message if requested or error message  */
    private CommandResponse commandOptions(args, CommandResponse response) {
        def stringWriter = new StringWriter()
        cli.writer = new PrintWriter(stringWriter)
        def options = cli.parse(args)
        if (stringWriter.toString()) {
            return response.addInfo(stringWriter.toString())
        }

        if (options.o) {
            this.overwrite = true
        }

        if (options.arguments()) {
            def file = new CommandHelper().getScriptFromString(options.arguments()[0])
            file = ensureExtension(file)
            if (errorCheckFile(file, response).hasErrors) return response
            this.scriptFile = file
            this.scriptName = options.arguments()[0]
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

    /** @return Error message if there is something wrong with the file location  */
    private CommandResponse errorCheckFile(Path file, CommandResponse response) {
        if (!overwrite && Files.exists(file)) {
            return response.addError("The file $file already exists!")
        } else if (!Files.exists(file.parent)) {
            return response.addError("The directory ${file.getParent()} does not exist!")
        } else if (!(FilenameUtils.getExtension(file.toString()) in ['', 'groovy'])) {
            return response.addError("The script extension must be 'groovy' or none.")
        } else {
            return response
        }
    }

    /** @return same file but with .groovy extension if none is given  */
    private Path ensureExtension(Path file) {
        if (!FilenameUtils.getExtension(file.toString())) {
            file = Paths.get(file.toString() + '.groovy')
        }
        return file
    }

}
