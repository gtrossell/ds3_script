package spectra

enum ClientCommands {
  info({'show client info'}, {'return client info'}, {'set client settings'})

  private final Closure show
  private final Closure get
  private final Closure set

  ClientCommands(Closure show, Closure get, Closure set) {
    this.show = show
    this.get  = get
    this.set  = set
  }

  def getShow() { this.show }
  def getGet()  { this.get }
  def getSet()  { this.set }

  public void call() {
    println "TODO: write summary function?"
  }

  public String toString() {
    name() + " = " + action
  }
}