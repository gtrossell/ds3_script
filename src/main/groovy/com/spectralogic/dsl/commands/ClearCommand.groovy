package com.spectralogic.dsl.commands

import jline.console.ConsoleReader

class ClearCommand implements ShellCommand {
    private final console

    ClearCommand(ConsoleReader console) {
        this.console = console
    }

    @Override
    CommandResponse run(List args) {
        console.clearScreen()
        return new CommandResponse()
    }

    @Override
    String[] commandNames() {
        return [':clear', ':c']
    }
}
