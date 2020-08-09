package benchmark

import suit.tools.arrays.DataArray
import org.scalatest.matchers.should
import suit.config.interfaces.ITreeSignatureAlgorithmConf
import suit.algorithms.interfaces.{IDetOneTimeSigGenerator, ISignatureTree}
import suit.tools.arrays.factory.{IDataArrayFactory, LongDataFactory, RefCountFactory}
import suit.algorithms.generate.tree.single.sparse.signature.SparseSignatureTree
import suit.interfaces.ILeaveAuth
import suit.algorithms.generate.tree.TreeSig
import suit.algorithms.generate.tree.single.fractal.signature.FractalSignatureTree
import suit.algorithms.generate.tree.single.advanced.signature.LogSpaceTimeSignatureTree
import suit.config.helper.FractalTreeHeightCalc
import test.measure.recorder.{IRecorder, IRecorderResult}
import test.measure.MeasureToolbox
import test.measure.recorder.impl._
import test.measure.logger.impl.CSVLogger
import suit.algorithms.hash.blake.blake2b.Blake2b
import classes.TestPrng
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.funsuite.AnyFunSuite
import suit.algorithms.hash.blake.blake512.Blake512
import suit.algorithms.prng.Hash_DBRG

class AbstractTest extends AnyFunSuite with should.Matchers{

  def measure[T <: IRecorderResult](recorder:IRecorder[T])(body: => Unit)= MeasureToolbox.measureWith( new Runnable() { def run() { body } }, recorder)

  //def rec(l:Int, h:Int) = new LeafDistributionRecorder(new CVSLogger(s"""sparse-distro-$l-$h.csv"""))
  def rec(algoName:String, h:Int, p:Int, blockSize:Int) = new BlockRecorder(blockSize,
    new SplitRecorder(
      new SplitRecorder(
        new TimeRecorder(new CSVLogger(s"""abstrsct-time-$algoName-${blockSize}blocks-$h-$p.csv""")),
        new DynSpaceRecorder(new CSVLogger(s"""abstrsct-space-$algoName-${blockSize}blocks-$h-$p.csv"""))
      ),
      new LeafDistributionRecorder(new CSVLogger(s"""abstrsct-leaf-$algoName-${blockSize}blocks-$h-$p.csv"""))
    )
  )


  def testDef(x:String)(y: => Unit) = test(x)(y)
  val minH = 8
  val maxH = 16 //22
  val warmRunsBase = 1// 2 << (22-12)  //2
  val runsBase = 1//2 << (22-12)     //2
  val fixRounds = true
  val blockBase = 6

  val blake = new Blake512(new RefCountFactory(new LongDataFactory(DataArray.Endianess.LITTLE)))
  val factLong:IDataArrayFactory = blake.getBackingFactory
  val dummyData = factLong.create(0)
  val dummyPrng = new Hash_DBRG(blake,dummyData)



  val testTreeConf = new ITreeSignatureAlgorithmConf {
    def getHashFun = blake
    def getLeaveCalc = getOneTimeSignatureGenerator
    def getOneTimeSignatureGenerator = new IDetOneTimeSigGenerator{
      def calcCommitmentLeave(Sk:DataArray) = Sk
      def calcSignature(Sk:DataArray, msg: DataArray) = Array(calcCommitmentLeave(Sk))
      def calcSignature(auth: ILeaveAuth, msg: DataArray)  = new TreeSig(Array(calcCommitmentLeave(auth.getLeaveSk)),auth.getAuthPath)
      def getBackingFactory = factLong
    }
  }

  def doTreeTest[T <: IRecorderResult](coparam: Int => Int ,build: (Int, Int) => ISignatureTree, rec: (Int, Int, Int) => IRecorder[T]){
    for(h <- minH to maxH){
      val p = coparam(h)
      val blockSize = if(h < blockBase) 1 else 1 << (h-blockBase)
      doTest(build(h,p),rec(h,p,blockSize))
    }
  }

  def doTest[T <: IRecorderResult](tree:ISignatureTree, recorder:IRecorder[T])={
    tree.init()
    tree.getRootPk
    measure(recorder){
      while(tree.canAuthMore){
        tree.createSignature(dummyData)
        tree.updateStep()
      }
    }
  }

  testDef("Advanced Fractal Tree abstract Test l=h"){
    doTreeTest((h) => h,(h,l) => new SparseSignatureTree(testTreeConf,l,h,dummyPrng),(h,l, blockSize) => rec("sparse",h,l,blockSize))
  }

  testDef("Advanced Fractal Tree abstract Test l=h/2"){
    if(minH >= 2) doTreeTest((h) => h/2,(h,l) => new SparseSignatureTree(testTreeConf,l,h,dummyPrng),(h,l, blockSize) => rec("sparse",h,l,blockSize))
  }

  testDef("Advanced Fractal Tree abstract Test l=h/3"){
    if(minH >= 3)doTreeTest((h) => h/3,(h,l) => new SparseSignatureTree(testTreeConf,l,h,dummyPrng),(h,l, blockSize) => rec("sparse",h,l,blockSize))
  }

  testDef("Advanced Fractal Tree abstract Test l=h/4"){
    if(minH >= 4)doTreeTest((h) => h/4,(h,l) => new SparseSignatureTree(testTreeConf,l,h,dummyPrng),(h,l, blockSize) => rec("sparse",h,l,blockSize))
  }

  testDef("Advanced Fractal Tree abstract Test l=h/6"){
    if(minH >= 6)doTreeTest((h) => h/6,(h,l) => new SparseSignatureTree(testTreeConf,l,h,dummyPrng),(h,l, blockSize) => rec("sparse",h,l,blockSize))
  }

  testDef("Advanced Fractal Tree abstract Test l=h/8"){
    if(minH >= 8)doTreeTest((h) => h/8,(h,l) => new SparseSignatureTree(testTreeConf,l,h,dummyPrng),(h,l, blockSize) => rec("sparse",h,l,blockSize))
  }

  testDef("Advanced Fractal Tree abstract Test l=h/12"){
    if(minH >= 12)doTreeTest((h) => h/12,(h,l) => new SparseSignatureTree(testTreeConf,l,h,dummyPrng),(h,l, blockSize) => rec("sparse",h,l,blockSize))
  }

  testDef("Advanced Fractal Tree abstract Test l=h/16"){
    if(minH >= 16)doTreeTest((h) => h/16,(h,l) => new SparseSignatureTree(testTreeConf,l,h,dummyPrng),(h,l, blockSize) => rec("sparse",h,l,blockSize))
  }


  testDef("Advanced Fractal Tree Test l = ~H/log(H)"){
    doTreeTest((h) => FractalTreeHeightCalc.calcOptHight(h).length,(h,l) => new SparseSignatureTree(testTreeConf,l,h,dummyPrng),(h,l, blockSize) => rec("sparse",h,l,blockSize))
  }

  testDef("Fractal Tree Test l = ~H/log(H)"){
    doTreeTest((h) => FractalTreeHeightCalc.calcOptHight(h).length,(h,l) => new FractalSignatureTree(testTreeConf,l,h,dummyPrng),(h,l, blockSize) => rec("fractal",h,l,blockSize))
  }

  testDef("Fractal Tree abstract Test l=h"){
    doTreeTest((h) => h,(h,l) => new FractalSignatureTree(testTreeConf,l,h,dummyPrng),(h,l, blockSize) => rec("fractal",h,l,blockSize))
  }

  testDef("Fractal Tree abstract Test l=h/2"){
    if(minH >= 2) doTreeTest((h) => h/2,(h,l) => new FractalSignatureTree(testTreeConf,l,h,dummyPrng),(h,l, blockSize) => rec("fractal",h,l,blockSize))
  }

  testDef("Fractal Tree abstract Test l=h/3"){
    if(minH >= 3)doTreeTest((h) => h/3,(h,l) => new FractalSignatureTree(testTreeConf,l,h,dummyPrng),(h,l, blockSize) => rec("fractal",h,l,blockSize))
  }

  testDef("Fractal Tree abstract Test l=h/4"){
    if(minH >= 4)doTreeTest((h) => h/4,(h,l) => new FractalSignatureTree(testTreeConf,l,h,dummyPrng),(h,l, blockSize) => rec("fractal",h,l,blockSize))
  }

  testDef("Fractal Tree abstract Test l=h/6"){
    if(minH >= 6)doTreeTest((h) => h/6,(h,l) => new FractalSignatureTree(testTreeConf,l,h,dummyPrng),(h,l, blockSize) => rec("fractal",h,l,blockSize))
  }

  testDef("Fractal Tree abstract Test l=h/8"){
    if(minH >= 8)doTreeTest((h) => h/8,(h,l) => new FractalSignatureTree(testTreeConf,l,h,dummyPrng),(h,l, blockSize) => rec("fractal",h,l,blockSize))
  }

  testDef("Fractal Tree abstract Test l=h/12"){
    if(minH >= 12)doTreeTest((h) => h/12,(h,l) => new FractalSignatureTree(testTreeConf,l,h,dummyPrng),(h,l, blockSize) => rec("fractal",h,l,blockSize))
  }

  testDef("Fractal Tree abstract Test l=h/16"){
    if(minH >= 16)doTreeTest((h) => h/16,(h,l) => new FractalSignatureTree(testTreeConf,l,h,dummyPrng),(h,l, blockSize) => rec("fractal",h,l,blockSize))
  }

  testDef("Log Tree Test k = 2/3"){
    if(minH >= 2) doTreeTest(h => if((h & 1) == 0) 2 else 3,(h,k) => new LogSpaceTimeSignatureTree(h,k,testTreeConf,dummyPrng),(h,k, blockSize) => rec("log",h,k,blockSize))
  }

  testDef("Log Tree Test k = 4/5"){
    if(minH >= 4) doTreeTest(h => if((h & 1) == 0) 4 else 5,(h,k) => new LogSpaceTimeSignatureTree(h,k,testTreeConf,dummyPrng),(h,k, blockSize) => rec("log",h,k,blockSize))
  }

  testDef("Log Tree Test k = 6/7"){
    if(minH >= 6) doTreeTest(h => if((h & 1) == 0) 6 else 7,(h,k) => new LogSpaceTimeSignatureTree(h,k,testTreeConf,dummyPrng),(h,k, blockSize) => rec("log",h,k,blockSize))
  }

  testDef("Log Tree Test k = 8/9"){
    if(minH >= 8) doTreeTest(h => if((h & 1) == 0) 8 else 9,(h,k) => new LogSpaceTimeSignatureTree(h,k,testTreeConf,dummyPrng),(h,k, blockSize) => rec("log",h,k,blockSize))
  }

  testDef("Log Tree Test k = ~h/2"){
    doTreeTest( h => if(((h & 1) == 0) != ((h & 2) == 0)) (h/2)-1 else h / 2,(h,k) => new LogSpaceTimeSignatureTree(h,k,testTreeConf,dummyPrng),(h,k, blockSize) => rec("log",h,k,blockSize))
  }


}
