package spectra.helpers

class LogRecorder {
  String logDir
  Date now

  def LogRecorder() {
    now = new Date()
    logDir = Config.logDir
    if (logDir[logDir.size()-1] != '/') {
      logDir += '/'
    }

    /* create log dir */
    def dir = new File(logDir)
    if (!dir.exists()) dir.mkdirs()
  }

  /** records a command and response to the current log file */
  def record(String input="", String output="") {
    getCurrentLogFile() << getInputLinePrefix() + input + '\n'
    getCurrentLogFile() << getOutputLinePrefix() + output + '\n'
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

  private getCurrentLogFile() {
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
