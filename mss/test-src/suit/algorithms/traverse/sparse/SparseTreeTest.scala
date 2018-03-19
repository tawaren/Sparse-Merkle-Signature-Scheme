package suit.algorithms.traverse.sparse

import suit.tools.arrays.DataArray
import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import suit.config.interfaces.ITreeSignatureAlgorithmConf
import suit.algorithms.interfaces.IDetOneTimeSigGenerator
import suit.algorithms.hash.blake.blake2b.Blake2b
import suit.algorithms.generate.tree.hash.RootBuilder

import suit.algorithms.prng.RandomAccessHashPRNG
import suit.tools.arrays.factory.{LongDataFactory, RefCountFactory, IDataArrayFactory}
import suit.algorithms.generate.tree.single.sparse.signature.SparseSignatureTree
import suit.interfaces.ILeaveAuth
import suit.algorithms.generate.tree.TreeSig
import test.measure.MeasureToolbox
import test.measure.recorder.{IRecorderResult, IRecorder}
import test.measure.recorder.impl._
import test.measure.logger.impl.{CSVLogger, ConsoleLogger}
import classes.TestPrng

class SparseTreeTest  extends FunSuite with ShouldMatchers{

   def measure[T <: IRecorderResult](recorder:IRecorder[T])(body: => Unit)= MeasureToolbox.measureWith( new Runnable() { def run() { body } }, recorder)

  //def rec(l:Int, h:Int) = new LeafDistributionRecorder(new CVSLogger(s"""sparse-distro-$l-$h.csv"""))
  def rec(l:Int, h:Int) = new BlockRecorder(16,new SplitRecorder(new TimeRecorder(new CSVLogger(s"""sparse-time-$l-$h.csv""")),new DynSpaceRecorder(new CSVLogger(s"""sparse-space-$l-$h.csv"""))))


   val h = 12
   val l = 3
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

     def getCRPNG(seed: DataArray) = new TestPrng(0,factLong)

     def getHashFun = blake
   }

  test("Sparse Tree root calc"){
    val hashGen = new RootBuilder(testTreeConf,h,dummyPrng)
    val fractA = new SparseSignatureTree(testTreeConf,l,h,dummyPrng)
    fractA.init()
    fractA.getRootPk should equal (hashGen.calcRootHash())
  }


  test("Sparse Tree auths correct "){
    val fractA = new SparseSignatureTree(testTreeConf,l,h,dummyPrng)
    import scala.collection.JavaConversions._
    //init
    println("Levels:"+fractA.getLevels)
    fractA.init()
    val rootH = fractA.getRootPk
    var leave = 0L
    println(measure(rec(l,h)){
      while(fractA.canAuthMore){
        val sig = fractA.createSignature(dummyData)
        //println(leave+" : "+auth)
        val res = sig.getAuthPath.foldLeft(sig.getSig()(0)){
          case (hash,entry) => if(entry.isLeftSilbling){
            blake.combineHashs(entry.getHash,hash)
          } else {
            blake.combineHashs(hash, entry.getHash)
          }
        }
        fractA.updateStep()
        if(res != rootH){           //prevent slow toString if not necessary
          res should equal(rootH)
        }
        leave+=1
      }
    })
    leave should equal(math.pow(2,h))

  }

  test("Sparse Tree auths correct best space"){
    val fractA = new SparseSignatureTree(testTreeConf,h,h,dummyPrng)
    import scala.collection.JavaConversions._
    //init
    fractA.init()
    val rootH = fractA.getRootPk
    var leave = 0L


    println(measure(rec(h,h)){
      while(fractA.canAuthMore){
        fractA.updateStep()
        val sig = fractA.createSignature(dummyData)
        //println(leave+" : "+auth)
        val res = sig.getAuthPath.foldLeft(sig.getSig()(0)){
          case (hash,entry) => if(entry.isLeftSilbling){
            blake.combineHashs(entry.getHash,hash)
          } else {
            blake.combineHashs(hash, entry.getHash)
          }
        }
        if(res != rootH){           //prevent slow toString if not necessary
          res should equal(rootH)
        }                                              //assert used to prevent slow toString invocation (slowdown factor 9 over 2 spots)
        leave+=1
      }
    })
    leave should equal(math.pow(2,h))
  }

  test("Sparse Tree auths correct Wirdo"){
    val hs =  Array[Byte](3,1,2,4)
    val fractA = new SparseSignatureTree(testTreeConf,hs,dummyPrng)
    import scala.collection.JavaConversions._
    //init
    fractA.init()
    val rootH = fractA.getRootPk
    var leave = 0L
    println(measure(new DynSpaceRecorder){
      while(fractA.canAuthMore){
        val sig = fractA.createSignature(dummyData)
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


    leave should equal(math.pow(2,hs.sum))

  }
}
