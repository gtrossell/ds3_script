package spectra

/**
 * Config keeps all of the hardcoded constants
 */
class Globals {
  final static def PROMPT = 'spectra> '
  final static def init_message(width) {
    """
Welcome to the Spectra DSL for BlackPearl!
${'=' * width}"""
  }
}