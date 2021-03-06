package suit.algorithms.traverse.fractal

import suit.tools.arrays.DataArray
import org.scalatest.matchers.should
import suit.config.interfaces.ITreeSignatureAlgorithmConf
import suit.algorithms.interfaces.IDetOneTimeSigGenerator
import suit.algorithms.hash.blake.blake2b.Blake2b
import suit.algorithms.generate.tree.hash.RootBuilder
import suit.algorithms.prng.RandomAccessHashPRNG
import suit.tools.arrays.factory.{IDataArrayFactory, LongDataFactory, RefCountFactory}
import suit.interfaces.ILeaveAuth
import suit.algorithms.generate.tree.TreeSig
import test.measure.MeasureToolbox
import suit.algorithms.generate.tree.single.fractal.signature.FractalSignatureTree
import test.measure.recorder.{IRecorder, IRecorderResult}
import test.measure.recorder.impl._
import test.measure.logger.impl.CSVLogger
import classes.TestPrng
import org.scalatest.funsuite.AnyFunSuite

import scala.collection.convert.ImplicitConversions.`iterable AsScalaIterable`

class FractalTreeTest extends AnyFunSuite with should.Matchers{

  def measure[T <: IRecorderResult](recorder:IRecorder[T])(body: => Unit)= MeasureToolbox.measureWith( new Runnable() { def run() { body } }, recorder)
  //def rec(l:Int, h:Int) = new LeafDistributionRecorder(new CVSLogger(s"""fract-distro-$l-$h.csv"""))

  def rec(l:Int, h:Int) = new BlockRecorder(16,new SplitRecorder(new TimeRecorder(new CSVLogger(s"""fractal-time-$l-$h.csv""")),new DynSpaceRecorder(new CSVLogger(s"""fractal-space-$l-$h.csv"""))))

  val h = 12
  val l = 4
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

  test("Fractal Tree root calc"){
    val hashGen = new RootBuilder(testTreeConf,h,dummyPrng)
    val fractA = new FractalSignatureTree(testTreeConf,l,h,dummyPrng)
    fractA.init()
    fractA.getRootPk should equal (hashGen.calcRootHash())
  }


  test("Fractal Tree auths correct "){
    val fractA = new FractalSignatureTree(testTreeConf,l,h,dummyPrng)
    //init
    //println("Levels:"+fractA.getLevels)
    fractA.init()
    val rootH = fractA.getRootPk
    var leave = 0L
    println(measure(rec(l, h)){
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

  test("Fractal Tree auths correct max h"){
    val fractA = new FractalSignatureTree(testTreeConf,h,h,dummyPrng)
    //init
    fractA.init()
    val rootH = fractA.getRootPk
    var leave = 0L
    println(measure(new DynSpaceRecorder){
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

  test("Advanced Fractal Tree auths correct Wirdo"){
    val hs =  Array[Byte](3,1,2,4)
    val fractA = new FractalSignatureTree(testTreeConf,hs,dummyPrng)
    //init
    fractA.init()
    val rootH = fractA.getRootPk
    var leave = 0L
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

    leave should equal(math.pow(2,hs.sum))

  }
}
