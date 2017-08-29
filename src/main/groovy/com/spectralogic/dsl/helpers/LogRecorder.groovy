package com.spectralogic.dsl.helpers

class LogRecorder {
  private final String logDir
  private final Date now

  LogRecorder() {
    now = new Date()
    logDir = Globals.LOG_DIR

    /* create log dir */
    def dir = new File(logDir)
    if (!dir.exists()) dir.mkdirs()
  }

  /** records a command and response to the current log file */
  def record(String input="", String output="") {
    def file = getLogFile()
    file << getInputLinePrefix() + input + '\n'
    file << getOutputLinePrefix() + output + '\n'
  }

  def writeLine(String line) {
    getLogFile() << "$line\n"
  }

  def init() {
    // TODO: init message
  }

  def close() {
    // TODO: end message
  }

  def getInputLinePrefix() {
    Globals.PROMPT
  }

  def getOutputLinePrefix() {
    Globals.RETURN_PROMPT
  }

  def getLogFile() {
    def day = now.date.toString()
    day = day.size() < 2 ? '0' + day : day
    def month = (now.month + 1).toString()
    month = month.size() < 2 ? '0' + month : month
    def year = now.year + 1900
    def logFile = new File("${logDir}${year}-${month}-${day}.log")
    if (!logFile.exists()) logFile.write('')
    logFile
  }

}
