package com.spectralogic.dsl.commands

import ch.qos.logback.classic.Level
import com.spectralogic.dsl.helpers.Globals
import com.spectralogic.dsl.helpers.LogRecorder

class LogCommand implements ShellCommand {
    private final CliBuilder cli

    LogCommand() {
        cli = new CliBuilder(usage: ':log, :l [options]')
        cli.header = 'Execute a script'
        cli.h('display this message', longOpt: 'help')
        cli.e('enables logging', longOpt: 'enable')
        cli.d('disables logging', longOpt: 'disable')
        cli.s('prints logger status', longOpt: 'status')
        cli.l('set/print log directory', longOpt: 'logdir', args: 1, argName: 'dir', optionalArg: true)
    }

    @Override
    String[] commandNames() {
        return [':log', ':l']
    }

    @Override
    CommandResponse run(List args) {
        def response = new CommandResponse()

        def stringWriter = new StringWriter()
        cli.writer = new PrintWriter(stringWriter)
        def options = cli.parse(args)
        if (stringWriter.toString()) {
            return response.addInfo(stringWriter.toString())
        }

        if (!options) {
            return response
        } else if (options.h || args.size() < 1 || options.arguments()) {
            cli.usage()
            return response.setMessage(stringWriter.toString())
        } else if (options.e) {
            LogRecorder.configureLogging(Level.ALL)
            response.addInfo(LogRecorder.loggerStatus())
        } else if (options.d) {
            LogRecorder.configureLogging(Level.OFF)
            response.addInfo(LogRecorder.loggerStatus())
        } else if (options.s) {
            response.addInfo(LogRecorder.loggerStatus())
        } else if (options.l) {
            if (options.l instanceof String) {
                Globals.logDir = options.l as String
            }
            response.addInfo("Log directory set to ${Globals.logDir}")
        }

        return response
    }

}
