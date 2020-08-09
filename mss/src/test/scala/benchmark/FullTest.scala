package benchmark

import org.scalatest.matchers.should
import suit.config.interfaces.ITreeSignatureAlgorithmConf
import suit.algorithms.interfaces.ISignatureTree
import suit.algorithms.prng.Hash_DBRG
import suit.tools.arrays.factory.IDataArrayFactory
import suit.algorithms.generate.tree.single.sparse.signature.SparseSignatureTree
import suit.algorithms.generate.tree.single.fractal.signature.FractalSignatureTree
import suit.algorithms.generate.tree.single.advanced.signature.LogSpaceTimeSignatureTree
import benchmark.helper.{RoundRecorder, Timer}
import org.scalatest.funsuite.AnyFunSuite
import suit.algorithms.generate.signature.winterlitz.WinterlitzSigGen
import suit.algorithms.hash.blake.blake512.Blake512

class FullTest extends AnyFunSuite with should.Matchers{

   val minH = 12
   val maxH = 12 //22
   val warmRunsBase = 1// 2 << (22-12)  //2
   val runsBase = 1//2 << (22-12)     //2
   val fixRounds = true
   val blockBase = 6

   val blake = new Blake512()
   val fact:IDataArrayFactory = blake.getBackingFactory
   val dummyData = blake.hash(fact.create(0)) //hash it so to bring better diversity into winterlitz
   val prng = new Hash_DBRG(blake,blake.hash(fact.create(4242)))


  val testTreeConf = new ITreeSignatureAlgorithmConf {
    def getHashFun = blake
    val getLeaveCalc = new WinterlitzSigGen(2,getHashFun)
    def getOneTimeSignatureGenerator = getLeaveCalc
  }

  def doTreeTest(algoName:String, coName:String, coparam: Int => Int ,build: (Int, Int) => ISignatureTree){
    for(h <- minH to maxH){
      val runsW = if(fixRounds) warmRunsBase else warmRunsBase << (maxH-h)
      val runsR = if(fixRounds) runsBase else runsBase << (maxH-h)
      val p = coparam(h)
      val blockSize = if(h < blockBase) 1 else 1 << (h-blockBase)
      Timer.measureTreeRounds(runsW,runsR,dummyData){build(h,p)}(new RoundRecorder(s"""full-$algoName-${blockSize}blocks-$h-$p""",blockSize))
    }
  }

  test("Advanced Fractal Tree Test l=h/2"){
    doTreeTest("sparse","L", h => h/2, (h,l) => new SparseSignatureTree(testTreeConf,l,h,prng))
  }

  ignore("Advanced Fractal Tree Test l=h/4"){
    doTreeTest("sparse","L", h => h/4, (h,l) => new SparseSignatureTree(testTreeConf,l,h,prng))
  }

  ignore("Advanced Fractal Tree Test l=h/6"){
    doTreeTest("sparse","L", h => h/6, (h,l) => new SparseSignatureTree(testTreeConf,l,h,prng))
  }

  ignore("Advanced Fractal Tree Test l=h/8"){
    doTreeTest("sparse","L", h => h/8, (h,l) => new SparseSignatureTree(testTreeConf,l,h,prng))
  }

  test("Fractal Tree Test l =  l=h/2"){
    doTreeTest("fractal","L", h => h/2, (h,l) => new FractalSignatureTree(testTreeConf,l,h,prng))
  }

  ignore("Fractal Tree Test l =  l=h/4"){
    doTreeTest("fractal","L", h => h/4, (h,l) => new FractalSignatureTree(testTreeConf,l,h,prng))
  }

  ignore("Fractal Tree Test l =  l=h/6"){
    doTreeTest("fractal","L", h => h/6, (h,l) => new FractalSignatureTree(testTreeConf,l,h,prng))
  }

  ignore("Fractal Tree Test l =  l=h/8"){
    doTreeTest("fractal","L", h => h/8, (h,l) => new FractalSignatureTree(testTreeConf,l,h,prng))
  }

  ignore("Log Tree  Test K = 2"){
    doTreeTest("log","K", h => 2, (h,k) => new LogSpaceTimeSignatureTree(h,k,testTreeConf,prng))
  }

  test("Log Tree  Test K = 4"){
    doTreeTest("log","K", h => 4, (h,k) => new LogSpaceTimeSignatureTree(h,k,testTreeConf,prng))
  }

  ignore("Log Tree  Test K = 6"){
    doTreeTest("log","K", h => 6, (h,k) => new LogSpaceTimeSignatureTree(h,k,testTreeConf,prng))
  }

  ignore("Log Tree  Test K = 8"){
    doTreeTest("log","K", h => 8, (h,k) => new LogSpaceTimeSignatureTree(h,k,testTreeConf,prng))
  }

  ignore("Log Tree  Test K = 10"){
    doTreeTest("log","K", h => 10, (h,k) => new LogSpaceTimeSignatureTree(h,k,testTreeConf,prng))
  }
}
