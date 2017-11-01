package com.spectralogic.dsl.commands

class CommandResponse {
  private message = ''
  Boolean hasErrors = false

  String getMessage() {
    return message
  }

  CommandResponse setMessage(String message) {
    this.message = message
    return this
  }

  CommandResponse appendMessage(String message) {
    this.message += "\n$message"
    return this
  }

  CommandResponse addInfo(String info) {
    appendMessage("[INFO] $info")
  }

  CommandResponse addError(String error) {
    hasErrors = true
    appendMessage("[ERROR] $error")
  }

  Boolean isEmpty() {
    return !message
  }

}
