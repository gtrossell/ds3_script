package com.spectralogic.dsl.helpers

/**
 * Globals keeps all fields and functions that need to be accessed multiple
 * places or are constants
 */
class Globals {
  final static String PROMPT = 'spectra> '
  final static String RETURN_PROMPT = '===> '
  private final static logger
  
  static def initMessage(width) {
    """
Welcome to the Spectra DSL for BlackPearl!
Use the ':help' command to get started
${'=' * width}"""
  }

}