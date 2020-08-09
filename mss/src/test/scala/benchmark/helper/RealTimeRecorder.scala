package benchmark.helper

trait RealTimeRecorder {
  def startInitPhase()
  def initPhaseMeasure(passedNs:Long)
  def startAuthPhase()
  def authPhaseMeasure(passedNs:Long)
  def finished()
}
