package com.spectralogic.dsl.helpers

/**
 * Helper class to handle multiline expressions for the shell
 */
class MultilineShellHelper {
    private List<String> lineStack
    private Map<String, Integer> stack

    MultilineShellHelper() {
        lineStack = []
        stack = emptyStack()
    }

    /**
     * returns true if the shell is currently in a multiline expression
     */
    def isMultiline() {
        return lineStack.size() > 0
    }

    def isComplete() {
        return !stack.values().any { it > 0 } && lineStack.size() > 0
    }

    /**
     * returns true if the line ends with something to start a multiline expression
     */
    def startMultiline(String line) {
        return stackUpdate(line).values().any { it > 0 }
    }

    def addLine(String line) {
        lineStack.add(line)
        stackUpdate(line).each { k, v -> stack[k] += v }
    }

    /**
     * returns full expression and resets helper
     */
    def getExpression() {
        def exp = lineStack.join('\n')
        reset()
        return exp
    }

    /**
     * returns Map of stacks that need to be updated
     */
    Map<String, Integer> stackUpdate(String line) {
        def cleanLine = line.replaceAll("\".*?\"", "")
        cleanLine = cleanLine.replaceAll("'.*?'", "")

        def update = emptyStack()
        for (c in cleanLine) {
            switch (c) {
                case '(':
                    update['parenthesis'] += 1
                    break
                case ')':
                    update['parenthesis'] -= 1
                    break
                case '{':
                    update['brackets'] += 1
                    break
                case '}':
                    update['brackets'] -= 1
                    break
            }
        }

        return update
    }

    private emptyStack() {
        return ['parenthesis': 0, 'brackets': 0]
    }

    def reset() {
        lineStack = []
        stack = emptyStack()
    }
}
