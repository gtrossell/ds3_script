package spectra

enum BucketCommands {
  info({'show buckets info'}, {'return buckets info'}, {'set bucket settings'})

  private final Closure show
  private final Closure get
  private final Closure set

  BucketCommands(Closure show, Closure get, Closure set) {
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