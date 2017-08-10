package com.spectralogic.dsl.helpers

/** Manages access to environment variables */
class Environment {
  private env

  Environment() { this.env = System.getenv() }
  
  Environment(Map env) { 
    def sysEnv = System.getenv()
    this.env = [:]
    this.env['DS3_ENDPOINT'] = env['DS3_ENDPOINT'] ?: sysEnv['DS3_ENDPOINT']
    this.env['DS3_ACCESS_KEY'] = env['DS3_ACCESS_KEY'] ?: sysEnv['DS3_ACCESS_KEY']
    this.env['DS3_SECRET_KEY'] = env['DS3_SECRET_KEY'] ?: sysEnv['DS3_SECRET_KEY']
  }

  def getEndpoint() { env['DS3_ENDPOINT'] }
  def getAccessKey() { env['DS3_ACCESS_KEY'] }
  def getSecretKey() { env['DS3_SECRET_KEY'] }
  def getEnvironment() {
    [
      'DS3_ENDPOINT':   getEndpoint(),
      'DS3_ACCESS_KEY': getAccessKey(),
      'DS3_SECRET_KEY': getAccessKey()
    ]
  }

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
