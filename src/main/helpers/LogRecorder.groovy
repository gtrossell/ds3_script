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
    getCurrentLogFile() << getLinePrefix() + input + '\n'
    getCurrentLogFile() << getLinePrefix() + output + '\n'
  }

  def init() {
    // TODO: init message
  }

  def close() {
    // TODO: end message
  }

  private getLinePrefix() {
    ''
  }

  private getCurrentLogFile() {
    def day = now.date + 1900
    def month = now.month + 1
    def year = now.year
    def logFile = new File("${logDir}${year}-${month}-${day}.log")
    if (!logFile.exists()) logFile.write('')
    logFile
  }

}
