package benchmark.helper

import test.measure.logger.impl.CSVLogger

/*blockSize should be power of 2*/
class RoundRecorder(fileBaseName:String, blockSize:Int) extends RealTimeRecorder{
  var logger: CSVLogger = null
  var count = 0
  var blockCount = blockSize
  var nanoSum = 0L

  private def processBlock(passedNs: Long){
    nanoSum += passedNs
    blockCount -= 1
    if(blockCount == 0){
      logger.log((nanoSum/blockSize):java.lang.Long,nanoSum:java.lang.Long)
      blockCount = blockSize
      nanoSum = 0
    }
  }

  def startInitPhase() = {
    if(logger != null)logger.finish()
    logger = new CSVLogger(fileBaseName+"-init-"+count+".csv")
    logger.init("timePerInitStep")
    blockCount = blockSize
    nanoSum = 0
  }

  def initPhaseMeasure(passedNs: Long) = processBlock(passedNs)

  def startAuthPhase() = {
    if(logger != null)logger.finish()
    logger = new CSVLogger(fileBaseName+"-auth-"+count+".csv")
    logger.init("timePerAuthStepAvg", "timePerAuthStepSum")
    blockCount = blockSize
    nanoSum = 0

  }

  def authPhaseMeasure(passedNs: Long) =  processBlock(passedNs)

  def finished() = {
    if(logger != null)logger.finish()
    count+=1
  }
}
