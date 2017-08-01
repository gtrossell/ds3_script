package spectra

/** Manages access to environment variables */
class Environment {
  private env

  def Environment() { this.env = System.getenv() }
  def getEndpoint() { env['DS3_ENDPOINT'] }
  def getAccessKey() { env['DS3_ACCESS_KEY'] }
  def getSecretKey() { env['DS3_SECRET_KEY'] }

  /** @return true if all variables are non-empty */
  def ready() {
    getEndpoint() && getAccessKey() && getSecretKey()
  }

  String toString() {
    "endpoint: ${getEndpoint()}, " +
    "access key: ${getAccessKey()}, " +
    "secret key: ${getSecretKey()}"
  }

}
