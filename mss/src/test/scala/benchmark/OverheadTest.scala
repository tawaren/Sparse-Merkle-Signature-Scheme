package benchmark

import suit.tools.arrays.DataArray
import org.scalatest.matchers.should
import suit.config.interfaces.ITreeSignatureAlgorithmConf
import suit.algorithms.interfaces.{IDetOneTimeSigGenerator, ISignatureTree}
import suit.algorithms.prng.RandomAccessHashPRNG
import suit.tools.arrays.factory.IDataArrayFactory
import suit.algorithms.generate.tree.single.sparse.signature.SparseSignatureTree
import suit.interfaces.{ILeaveAuth, ITreeSig}
import suit.algorithms.generate.tree.TreeSig
import suit.algorithms.generate.tree.single.fractal.signature.FractalSignatureTree
import suit.algorithms.generate.tree.single.advanced.signature.LogSpaceTimeSignatureTree
import benchmark.helper.{NullHash, RoundRecorder, Timer}
import suit.config.helper.FractalTreeHeightCalc
import classes.DummyPrng
import org.scalatest.funsuite.AnyFunSuite

class OverheadTest extends AnyFunSuite with should.Matchers{

   val minH = 16
   val maxH = 16 //22
   val warmRunsBase = 1// 2 << (22-12)  //2
   val runsBase = 1//2 << (22-12)     //2
   val fixRounds = true
   val blockBase = 6

   val blake = new NullHash
   val fact:IDataArrayFactory = blake.getBackingFactory
   val dummyData = fact.create()
   val dummyArray = Array[DataArray]()
   val dummyPrng = new DummyPrng(dummyData)

   val testTreeConf = new ITreeSignatureAlgorithmConf {

     def getLeaveCalc = getOneTimeSignatureGenerator

     val getOneTimeSignatureGenerator = new IDetOneTimeSigGenerator{
       def calcCommitmentLeave(Sk:DataArray) = dummyData
       def calcSignature(Sk:DataArray, msg: DataArray) = dummyArray//Array(calcCommitmentLeave(Sk))
       def calcSignature(auth: ILeaveAuth, msg: DataArray)  = new TreeSig(dummyArray,null)
       def getBackingFactory = fact
     }

     def getHashFun = blake
   }

  def doTreeTest(algoName:String, coName:String, coparam: Int => Int ,build: (Int, Int) => ISignatureTree){
    for(h <- minH to maxH){
      val runsW = if(fixRounds) warmRunsBase else warmRunsBase << (maxH-h)
      val runsR = if(fixRounds) runsBase else runsBase << (maxH-h)
      val p = coparam(h)
      val blockSize = if(h < blockBase) 1 else 1 << (h-blockBase)
      Timer.measureTreeRounds(runsW,runsR,dummyData){build(h,p)}(new RoundRecorder(s"""overhead-$algoName-${blockSize}blocks-$h-$p""",blockSize))
    }
  }


  test("Advanced Fractal Tree overhead Test l=h/2"){
    doTreeTest("sparse","L", h => h/2, (h,l) => new SparseSignatureTree(testTreeConf,l,h,dummyPrng))
  }

  ignore("Advanced Fractal Tree overhead Test l=h/4"){
    doTreeTest("sparse","L", h => h/4, (h,l) => new SparseSignatureTree(testTreeConf,l,h,dummyPrng))
  }

  ignore("Advanced Fractal Tree overhead Test l=h/6"){
    doTreeTest("sparse","L", h => h/6, (h,l) => new SparseSignatureTree(testTreeConf,l,h,dummyPrng))
  }

  ignore("Advanced Fractal Tree overhead Test l=h/8"){
    doTreeTest("sparse","L", h => h/8, (h,l) => new SparseSignatureTree(testTreeConf,l,h,dummyPrng))
  }

  test("Fractal Tree overhead Test l=h/2"){
    doTreeTest("fractal","L", h => h/2, (h,l) => new FractalSignatureTree(testTreeConf,l,h,dummyPrng))
  }

  ignore("Fractal Tree overhead Testl=h/4"){
    doTreeTest("fractal","L", h => h/4, (h,l) => new FractalSignatureTree(testTreeConf,l,h,dummyPrng))
  }

  ignore("Fractal Tree overhead Testl=h/6"){
    doTreeTest("fractal","L", h => h/6, (h,l) => new FractalSignatureTree(testTreeConf,l,h,dummyPrng))
  }

  ignore("Fractal Tree overhead Testl=h/8"){
    doTreeTest("fractal","L", h => h/8, (h,l) => new FractalSignatureTree(testTreeConf,l,h,dummyPrng))
  }

  ignore("Log Tree overhead Test K = 2"){
    doTreeTest("log","K", h => 2, (h,k) => new LogSpaceTimeSignatureTree(h,k,testTreeConf,dummyPrng))
  }

  test("Log Tree overhead Test K = 4"){
    doTreeTest("log","K", h => 4, (h,k) => new LogSpaceTimeSignatureTree(h,k,testTreeConf,dummyPrng))
  }

  ignore("Log Tree overhead Test K = 6"){
    doTreeTest("log","K", h => 6, (h,k) => new LogSpaceTimeSignatureTree(h,k,testTreeConf,dummyPrng))
  }

  ignore("Log Tree overhead Test K = 8"){
    doTreeTest("log","K", h => 8, (h,k) => new LogSpaceTimeSignatureTree(h,k,testTreeConf,dummyPrng))
  }

  ignore("Log Tree overhead Test K = 10"){
    doTreeTest("log","K", h => 10, (h,k) => new LogSpaceTimeSignatureTree(h,k,testTreeConf,dummyPrng))
  }

}
