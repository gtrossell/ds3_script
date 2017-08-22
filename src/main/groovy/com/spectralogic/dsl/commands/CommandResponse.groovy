package com.spectralogic.dsl.commands

import org.slf4j.Logger
import org.slf4j.LoggerFactory

class CommandResponse {
  private final logger
  private List<String> message
  private List<String> info
  private List<String> warn
  private List<String> error
  Boolean exit

  CommandResponse() {
    message = []
    info    = []
    warn    = []
    error   = []
    exit    = false
    logger  = LoggerFactory.getLogger(CommandResponse.class)
  }

  CommandResponse addMessage(String message) {
    this.message << message
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
    !info && !warn && !error
  }

  void log() {
    message.each { println it }
    info.each { logger.info(it) }
    warn.each { logger.warn(it) }
    error.each { logger.error(it) }
  }

}
