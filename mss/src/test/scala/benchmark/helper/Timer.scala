package benchmark.helper

import suit.algorithms.interfaces.{IMerkleTree, ISignatureTree}
import suit.interfaces.{ILeaveAuth, ITreeSig}
import benchmark.helper.RealTimeRecorder
import suit.tools.arrays.DataArray

//Makro Benchmark    (Not suitable for Mikro)
object Timer {

  class Result(jitElimBarrier:Int, passedNanosWarmup:Long, warmupRuns:Int, passedNanosRuns:Long,  realRuns:Int){
     private def fact = 1000000.0
     def passedTotalNano =  passedNanosWarmup + passedNanosRuns
     def passedTotalAverageNano = passedTotalNano / (warmupRuns + realRuns)
     def passedWarmupNano =  passedNanosWarmup
     def passedWarmupAverageNano = passedWarmupNano / warmupRuns
     def passedNano =  passedNanosRuns
     def passedAverageNano = passedNano / realRuns
     def passedTotalMilis=  passedTotalNano  / fact
     def passedTotalAverageMilis = passedTotalAverageNano  / fact
     def passedWarmupMilis =  passedWarmupNano  / fact
     def passedWarmupAverageMilis = passedWarmupAverageNano  / fact
     def passedMilis =  passedNano  / fact
     def passedAverageMilis = passedAverageNano  / fact
  }

  def measure[T <: AnyRef](warmup:Int, runs:Int)(bodyW: => T, bodyR: => T):Result = {
    var res = 0 //for preventing dead code elim of JIT
    val t0 = System.nanoTime()
    for(i <- 0 until warmup) res |= System.identityHashCode(bodyW)   //JIT DEADCODE ELIM
    val t1 = System.nanoTime()
    for(i <- 0 until runs) res |= System.identityHashCode(bodyR)   //JIT DEADCODE ELIM
    val t2 = System.nanoTime()
    new Result(res, t1-t0, warmup, t2-t1, runs)
  }

  def measurePart[T <: AnyRef, V <: AnyRef](warmup:Int, runs:Int)(prep: => T, doIt: T => V):Result = {
    var res = 0 //for preventing dead code elim of JIT
    var tw = 0L
    for(i <- 0 until warmup) {
      val mid = prep
      val t0 = System.nanoTime()
      res |= System.identityHashCode(doIt(mid))     //JIT DEADCODE ELIM
      tw += (System.nanoTime()-t0)
    }
    var tr = 0L
    for(i <- 0 until runs){
      val mid = prep
      val t0 = System.nanoTime()
      res |= System.identityHashCode(doIt(mid))     //JIT DEADCODE ELIM
      tr += (System.nanoTime()-t0)
    }
    new Result(res, tw, warmup, tr, runs)
  }

  def measureSame[T <: AnyRef](warmup:Int, runs:Int)(body: => T):Result = measure(warmup, runs)(body, body)
  def measureAuth[T <: AnyRef](warmups:Int, runs:Int, dummy:DataArray)(treeBuild: => ISignatureTree) =  measurePart(warmups,runs)(initTree(treeBuild),authTree(dummy))
  def measureAuth2[T <: AnyRef](warmups:Int, runs:Int)(treeBuild: => IMerkleTree) =  measurePart(warmups,runs)(initTree2(treeBuild),authTree2)

  def measureTree(warmups:Int, runs:Int, dummy:DataArray)(treeBuild: => ISignatureTree) =  measureSame(warmups,runs){runTree(dummy)(treeBuild)}
  def measureTreeRounds(warmups:Int, runs:Int, dummy:DataArray)(treeBuild: => ISignatureTree)(recorder:RealTimeRecorder):Unit = measure(warmups,runs)(runTree(dummy)(treeBuild),runTreeRec(dummy)(treeBuild)(recorder))

  def runTreeRec(dummyData:DataArray)(treeBuild: => ISignatureTree)(recorder: RealTimeRecorder) = {
    val tree = treeBuild
    recorder.startInitPhase()
    var tlast = System.nanoTime()
    while(tree.initStep()){
      val tn = System.nanoTime()
      recorder.initPhaseMeasure(tn - tlast)
      tlast = System.nanoTime()
    }
    tree.getRootPk
    var lastAuth: ITreeSig = null //Dead code Elim Barrier (not really necessary but sure is sure)
    recorder.startAuthPhase()
    tlast = System.nanoTime()
    while(tree.canAuthMore){
      //Alternatives
      // lastAuth = tree.createSignature(averageData)   //average
      // lastAuth = tree.createNextAuth                 //No signing
      lastAuth = tree.createSignature(dummyData)        //overhead
      tree.updateStep()
      val tn = System.nanoTime()
      recorder.authPhaseMeasure(tn - tlast)
      tlast = System.nanoTime()
    }
    recorder.finished()
    lastAuth
  }

  def initTree(treeBuild: => ISignatureTree) = {
    val tree = treeBuild
    tree.init()
    tree.getRootPk
    tree
  }

  def initTree2(treeBuild: => IMerkleTree) = {
    val tree = treeBuild
    tree.init()
    tree.getRootPk
    tree
  }

  def authTree(dummyData:DataArray)(tree:ISignatureTree) = {
    var lastAuth: ITreeSig = null //Dead code Elim Barrier (not really necessary but sure is sure)
    while(tree.canAuthMore){
      lastAuth = tree.createSignature(dummyData)
      tree.updateStep()
    }
    lastAuth
  }

  def authTree2(tree:IMerkleTree) = {
    var lastAuth: ILeaveAuth = null //Dead code Elim Barrier (not really necessary but sure is sure)
    while(tree.canAuthMore){
      lastAuth = tree.createNextAuth()
      tree.updateStep()
    }
    lastAuth
  }

   def runTree(dummyData:DataArray)(treeBuild: => ISignatureTree) = {
    val tree = treeBuild
    tree.init()
    tree.getRootPk
    var lastAuth: ITreeSig = null //Dead code Elim Barrier (not really necessary but sure is sure)
    while(tree.canAuthMore){
      lastAuth = tree.createSignature(dummyData)
      tree.updateStep()
    }
    lastAuth
  }


}
