package com.spectralogic.dsl.commands

import org.slf4j.Logger
import org.slf4j.LoggerFactory

class CommandResponse {
  private final logger
  List<String> info
  List<String> warn
  List<String> error
  Boolean exit

  CommandResponse() {
    info = []
    warn = []
    error = []
    exit = false
    logger = LoggerFactory.getLogger(CommandResponse.class)
  }

  def CommandResponse addInfo(String message) {
    info << message
    this
  }

  def CommandResponse addWarn(String message) {
    warn << message
    this
  }

  def CommandResponse addError(String message) {
    error << message
  }

  def Boolean isEmpty() {
    !info && !warn && !error
  }

  def log() {
    info.each { logger.info(it) }
    warn.each { logger.warn(it) }
    error.each { logger.error(it) }
  }

}
