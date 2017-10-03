package com.spectralogic.dsl.helpers

/**
 * Globals keeps all fields and functions that need to be accessed multiple
 * places or are constants
 */
class Globals {
  final static String PROMPT = 'spectra> '
  final static String RETURN_PROMPT = '===> '
  final static MAX_BULK_LOAD = 200_000
  final static HOME_DIR = new File("").getAbsoluteFile().toString() + '/'
  final static LOG_DIR = HOME_DIR + 'log/'
  final static SCRIPT_DIR = HOME_DIR

  static initMessage(width) {
    """
Welcome to the Spectra DSL for BlackPearl!
Use the ':help' command to get started
${'=' * (width - 1)}"""
  }

}