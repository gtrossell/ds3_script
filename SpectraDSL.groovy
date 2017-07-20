package spectra

import com.spectralogic.ds3client.models.Bucket
import com.spectralogic.ds3client.Ds3Client
import com.spectralogic.ds3client.models.common.Credentials
import groovy.transform.*

import static spectra.ClientCommands.*

/**
 * This is the customized SpectraShell that contains the DSL and acts similar
 * to GroovyShell
 */
abstract class SpectraDSL extends Script {
  // def show = { println it }
  // def square_root = { Math.sqrt(it) }
  def all = { Integer.MAX_VALUE }
  def the = { 1 }
  def a   = { 1 }
  def clientBuilder
  def client
  def status = { client -> client.status() }

  def SpectraDSL() {
    clientBuilder = new ClientBuilder()
  }



  // please show      the   square_root of    100
  // please {action}  {the} {what}      {of}  {n}
  // def please(action) {
  //   [the: { what ->
  //     [of: { n -> action(what(n)) }]
  //   },
  //   a: {
  //     println "test"
  //   }]
  // }

  // def show(count) {
  //   ['client': { test ->
  //     println "HERE"
  //   }]
  // }

  def create() {

  }
  
  def tester = {
    println "IM HERE"
  }

  // enum ClientCommands {
  //   INFO({ ClientBuilder cb -> cb.toString() })
  //   ClientCommands(Closure action) {
  //     this.action = action
  //   }
  //   private final Closure action
  //   Closure getAction() {
  //     this.action
  //   }
  // }
  def show(count) {
    def action = { println it }
    [
      client:   { command -> command.show() }
      // buckets:  { comm ->  }
    ]
  }
  
  def newer = { date -> println "newer than $date" }
  def older = { date -> println "older than $date" }
  def larger = { size -> println "larger than $size" }
  def smaller = { size -> println "smaller than $size" }
  def with = { name='', id=-1 -> println "with name: $name and id: $id" }
  // list(count).buckets(descriptor).than(date)
  // list <all/{number}> <buckets/folders> <newer/older> <than> {date}
  // list <all/{number}> <buckets/folders> <with> <name> {name}
  def list(count) {
    [buckets: { descriptor ->
      [
        than: { date -> descriptor(date) },
        name: { name -> descriptor(name: name) },
        id:   { id -> descriptor(id: id) }
      ]
    }]
  }

  /** @return List of the first {count} buckets in {buckets} */
  def listBuckets(count, Bucket[] buckets) {

  }
}

/**
 * Methods for managing and creating the client object in the spectra shell
 */
@ToString(includeNames=true,includeSuper=false)
class ClientBuilder {
  String endpoint
  String accessKey
  String secretKey

  /** Creates client for building Ds3Clients */
  def ClientBuilder() {
    def env = System.getenv()
    this.endpoint = env["DS3_ENDPOINT"]
    this.accessKey = env["DS3_ACCESS_KEY"]
    this.secretKey = env["DS3_SECRET_KEY"]
  }

  def status() {
    "OK"
  }

  /** 
   * Will build and return a client with conf values taking precidence over 
   * set fields (the fields default on enviroment variables).
   */
  def build(Map conf) {
    if (conf.endpoint) this.endpoint = conf.endpoint
    if (conf.accessKey) this.accessKey = conf.accessKey
    if (conf.secretKey) this.sectretKey = conf.secretKey
    // TODO: throw error if something is not set?
    def cred = new Credentials(this.accessKey, this.secretKey)
    return Ds3ClientBuilder.create(this.endpoint, cred).withHttps(false).build()
  }
}
