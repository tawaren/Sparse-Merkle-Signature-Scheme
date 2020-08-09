package suit.algorithms.traverse.advanced

import suit.tools.arrays.DataArray
import org.scalatest.matchers.should
import suit.config.interfaces.ITreeSignatureAlgorithmConf
import suit.algorithms.interfaces.IDetOneTimeSigGenerator
import suit.algorithms.hash.blake.blake2b.Blake2b
import suit.algorithms.generate.tree.hash.RootBuilder
import suit.algorithms.prng.RandomAccessHashPRNG
import suit.tools.arrays.factory.{IDataArrayFactory, LongDataFactory, RefCountFactory}
import suit.algorithms.generate.tree.single.sparse.signature.SparseSignatureTree
import suit.interfaces.ILeaveAuth
import suit.algorithms.generate.tree.TreeSig
import suit.algorithms.generate.tree.single.advanced.LogSpaceTimeTree
import test.measure.MeasureToolbox
import suit.algorithms.generate.tree.single.advanced.signature.LogSpaceTimeSignatureTree
import test.measure.recorder.{IRecorder, IRecorderResult}
import test.measure.recorder.impl._
import test.measure.logger.impl.CSVLogger
import classes.TestPrng
import org.scalatest.funsuite.AnyFunSuite

import scala.collection.convert.ImplicitConversions.`iterable AsScalaIterable`

class AdvancedLogTreeTest extends AnyFunSuite with should.Matchers{

   def measure[T <: IRecorderResult](recorder:IRecorder[T])(body: => Unit)= MeasureToolbox.measureWith( new Runnable() { def run() { body } }, recorder)
   //def rec(l:Int, h:Int) = new LeafDistributionRecorder(new CVSLogger(s"""log-distro-$l-$h.csv"""))

   def rec(k:Int, h:Int) = new BlockRecorder(16,new SplitRecorder(new TimeRecorder(new CSVLogger(s"""log-time-$k-$h.csv""")),new DynSpaceRecorder(new CSVLogger(s"""log-space-$k-$h.csv"""))))

   val h = 12
   val k = 2
   val blake = new Blake2b(new RefCountFactory(new LongDataFactory(DataArray.Endianess.LITTLE)))
   val factLong:IDataArrayFactory = blake.getBackingFactory
   assert(factLong.isInstanceOf[RefCountFactory])
   val dummyData = factLong.create(0)
   def dummyPrng = new RandomAccessHashPRNG(blake,dummyData)


   val testTreeConf = new ITreeSignatureAlgorithmConf {

     def getLeaveCalc = getOneTimeSignatureGenerator

     def getOneTimeSignatureGenerator = new IDetOneTimeSigGenerator{
       def calcCommitmentLeave(Sk:DataArray) = Sk

       def calcSignature(Sk:DataArray, msg: DataArray) = Array(calcCommitmentLeave(Sk))
       def verifySignature(msg: DataArray, sig: Array[DataArray]) = sig(0)

       def calcSignatureStepwise(Sk:DataArray, msg: DataArray) = ???

       def calcSignature(auth: ILeaveAuth, msg: DataArray)  = new TreeSig(Array(calcCommitmentLeave(auth.getLeaveSk)),auth.getAuthPath)

       def getBackingFactory = factLong
     }

     def getCRPNG(seed: DataArray) = new TestPrng(0, factLong)

     def getHashFun = blake
   }

  test("Advanced Log Tree root calc"){
    val hashGen = new RootBuilder(testTreeConf,h,dummyPrng)
    val logA = new LogSpaceTimeTree(h,k,testTreeConf,dummyPrng)
    logA.init()
    logA.getRootPk should equal (hashGen.calcRootHash())
  }


  test("Advanced Log Tree auths compare "){
    val fractA = new SparseSignatureTree(testTreeConf,h,h,dummyPrng)
    val logA = new LogSpaceTimeSignatureTree(h,k,testTreeConf,dummyPrng)
    //init
    logA.init()
    fractA.init()
    val rootH = logA.getRootPk
    assert(fractA.getRootPk.equals(rootH))
    var leave = 0L
    while(logA.canAuthMore){
      assert(fractA.canAuthMore)
      val sig = logA.createSignature(dummyData)
      val sigF = fractA.createSignature(dummyData)
      assert(sig.equals(sigF))
      logA.updateStep()
      fractA.updateStep()
      leave+=1
    }
    leave should equal(math.pow(2,h))
  }

  test("Advanced Log Tree auths correct "){

    val logA = new LogSpaceTimeSignatureTree(h,k,testTreeConf,dummyPrng)
    //init
    logA.init()
    val rootH = logA.getRootPk
    var leave = 0L
    println(measure(rec(k, h)){
      while(logA.canAuthMore){
        val sig = logA.createSignature(dummyData)
        val res = sig.getAuthPath.foldLeft(sig.getSig()(0)){
          case (hash,entry) => if(entry.isLeftSilbling){
            blake.combineHashs(entry.getHash,hash)
          } else {
            blake.combineHashs(hash, entry.getHash)
          }
        }
        logA.updateStep()
        if(res != rootH){           //prevent slow toString if not necessary
          res should equal(rootH)
        }
        leave+=1
      }
    })

    leave should equal(math.pow(2,h))

  }

  test("Advanced Fractal Tree auths correct higher K"){
    val logA = new LogSpaceTimeSignatureTree(h,k*3,testTreeConf,dummyPrng)
    //init
    logA.init()
    val rootH = logA.getRootPk
    var leave = 0L
    println(measure(new DynSpaceRecorder){
      while(logA.canAuthMore){
        val sig = logA.createSignature(dummyData)
        val res = sig.getAuthPath.foldLeft(sig.getSig()(0)){
          case (hash,entry) => if(entry.isLeftSilbling){
            blake.combineHashs(entry.getHash,hash)
          } else {
            blake.combineHashs(hash, entry.getHash)
          }
        }
        if(res != rootH){           //prevent slow toString if not necessary
          res should equal(rootH)
        }
        leave+=1
      }
    })
    leave should equal(math.pow(2,h))
  }
}
