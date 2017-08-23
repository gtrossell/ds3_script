package com.spectralogic.dsl.commands

import org.slf4j.Logger
import org.slf4j.LoggerFactory

class CommandResponse {
  private final logger
  private String message
  private List<String> info
  private List<String> warn
  private List<String> error
  Boolean exit

  CommandResponse() {
    message = ''
    info    = []
    warn    = []
    error   = []
    exit    = false
    logger  = LoggerFactory.getLogger(CommandResponse.class)
  }

  CommandResponse setMessage(String message) {
    this.message = message
    return this
  }

  CommandResponse addInfo(String message) {
    info << message
    return this
  }

  CommandResponse addWarn(String message) {
    warn << message
    return this
  }

  CommandResponse addError(String message) {
    error << message
    return this
  }

  Boolean isEmpty() {
    return !info && !warn && !error && !message
  }

  String getMessage() {
    return message
  }

  void log() {
    info.each { logger.info(it) }
    warn.each { logger.warn(it) }
    error.each { logger.error(it) }
  }

}
