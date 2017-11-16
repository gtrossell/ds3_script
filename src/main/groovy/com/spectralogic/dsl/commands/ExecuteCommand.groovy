package com.spectralogic.dsl.commands

import com.spectralogic.dsl.helpers.CommandHelper
import com.spectralogic.dsl.helpers.Globals
import groovy.io.FileType
import org.apache.commons.io.FilenameUtils

class ExecuteCommand implements ShellCommand {
    private final GroovyShell shell
    private final CliBuilder cli

    ExecuteCommand(GroovyShell shell) {
        this.shell = shell

        cli = new CliBuilder(usage: ':execute, :e <script>')
        cli.header = 'Execute a script'
        cli.h('display this message', longOpt: 'help')
        cli.l('list scripts in script folder', longOpt: 'list')
        cli.d('delete script in script folder', longOpt: 'delete', args: 1, argName: 'script')
    }

    @Override
    String[] commandNames() {
        return [':execute', ':e']
    }

    @Override
    CommandResponse run(List args) {
        def response = new CommandResponse()
        if (!commandOptions(args, response).isEmpty()) return response

        String scriptName = args[0]
        if (FilenameUtils.getExtension(scriptName) == '') {
            scriptName += '.groovy'
        }

        def script = new CommandHelper().getScriptFromString(scriptName)
        if (!script.exists()) {
            return response.addError("The script '$script' does not exists!")
        }

        def scriptArgs = args.size() > 1 ? args[1..-1] : []
        shell.run(script.text, script.name, scriptArgs)

        return response
    }

    /** @return help message if requested or error message  */
    private CommandResponse commandOptions(List args, CommandResponse response) {
        def stringWriter = new StringWriter()
        cli.writer = new PrintWriter(stringWriter)
        def options = cli.parse(args)
        if (stringWriter.toString()) {
            return response.addInfo(stringWriter.toString())
        }

        if (!options) {
            return response
        } else if (options.h || args.size() < 1) {
            cli.usage()
            return response.setMessage(stringWriter.toString())
        } else if (options.l) {
            return response.setMessage(listScripts())
        } else if (options.d) {
            return response.addInfo(deleteScript(options.d as String))
        } else {
            return response
        }
    }

    private listScripts() {
        def scripts = []
        new File(Globals.SCRIPT_DIR).eachFile(FileType.FILES) { file ->
            if (file.name.endsWith('.groovy')) {
                scripts << ' - ' + FilenameUtils.removeExtension(file.getName())
            }
        }
        return 'Available scripts:\n' + scripts.join('\n')
    }

    private deleteScript(String scriptName) {
        if (FilenameUtils.getExtension(scriptName) == '') scriptName += '.groovy'
        def script = new CommandHelper().getScriptFromString(scriptName)
        if (script.exists()) {
            script.delete()
            return "Deleted script $scriptName"
        } else {
            return "No script named $scriptName"
        }
    }

}
