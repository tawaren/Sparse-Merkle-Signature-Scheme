package benchmark

import _root_.helper.Timer
import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import suit.config.interfaces.{ITreeAlgorithmConf, ITreeSignatureAlgorithmConf}
import suit.algorithms.interfaces.{IMerkleTree, ISignatureTree}
import suit.algorithms.prng.RandomAccessHashPRNG
import suit.tools.arrays.factory.IDataArrayFactory
import suit.algorithms.generate.tree.single.sparse.signature.SparseSignatureTree
import suit.algorithms.generate.tree.single.fractal.signature.FractalSignatureTree
import suit.algorithms.generate.tree.single.advanced.signature.LogSpaceTimeSignatureTree
import benchmark.helper.RoundRecorder
import suit.config.helper.FractalTreeHeightCalc
import suit.algorithms.hash.blake.blake2b.Blake2b
import suit.algorithms.generate.signature.winterlitz.WinterlitzSigGen
import suit.algorithms.hash.blake.blake512.Blake512
import suit.tools.arrays.DataArray
import suit.algorithms.generate.tocken.TockenCalc
import suit.algorithms.generate.tree.single.sparse.SparseFractalTree
import suit.algorithms.generate.tree.single.fractal.FractalTree
import suit.algorithms.generate.tree.single.advanced.LogSpaceTimeTree

class DiffHashTest  extends FunSuite with ShouldMatchers{

   val minH = 8
   val maxH = 8 //22
   val warmRunsBase = 100// 2 << (22-12)  //2
   val runsBase = 100//2 << (22-12)     //2



  val blake2b = new Blake2b()
  val fact2b = blake2b.getBackingFactory
  val prng2b = new RandomAccessHashPRNG( blake2b,blake2b.hash(fact2b.create(4242)))
  val dummyData2b = blake2b.hash(fact2b.create(0)) //hash it so to bring better diversity into winterlitz

  val blake512 = new Blake512()
  val fact512  = blake512 .getBackingFactory
  val prng512  = new RandomAccessHashPRNG( blake512 ,blake512 .hash(fact512 .create(4242)))
  val dummyData512 = blake512.hash(fact512.create(0)) //hash it so to bring better diversity into winterlitz


  val testTreeConf2b = new ITreeSignatureAlgorithmConf {
    def getHashFun = blake2b
    val getLeaveCalc = new WinterlitzSigGen(2,getHashFun)
    def getOneTimeSignatureGenerator = getLeaveCalc
  }

  val testTreeConf512= new ITreeSignatureAlgorithmConf {
    def getHashFun = blake512
    val getLeaveCalc = new WinterlitzSigGen(2,getHashFun)
    def getOneTimeSignatureGenerator = getLeaveCalc
  }

  val testTreeConf2bTok = new ITreeAlgorithmConf {
    def getHashFun = blake2b
    val getLeaveCalc = new TockenCalc(getHashFun)
  }

  val testTreeConf512Tok= new ITreeAlgorithmConf {
    def getHashFun = blake512
    val getLeaveCalc = new TockenCalc(getHashFun)
  }


  def doTreeTest(dummyData:DataArray, algoName:String, coName:String, coparam: Int => Int ,build: (Int, Int) => ISignatureTree){
    for(h <- minH to maxH){
      val p = coparam(h)
      println(s"""$algoName-$h-$p:${Timer.measureAuth(warmRunsBase,runsBase,dummyData){build(h,p)}.passedNano}""")
    }
  }

  def doTreeTest2(algoName:String, coName:String, coparam: Int => Int ,build: (Int, Int) => IMerkleTree){
    for(h <- minH to maxH){
      val p = coparam(h)
      println(s"""$algoName-$h-$p:${Timer.measureAuth2(warmRunsBase,runsBase){build(h,p)}.passedNano}""")
    }
  }

  /*test("Advanced Fractal Tree hash Test l=h"){
    doTreeTest2("sparse2b","L", identity, (h,l) => new SparseFractalTree(testTreeConf2bTok,l,h,prng2b))
    doTreeTest2("sparse512","L", identity, (h,l) => new SparseFractalTree(testTreeConf512Tok,l,h,prng512))
    doTreeTest2("sparseMix","L", identity, (h,l) => new SparseFractalTree(testTreeConf2bTok,l,h,prng512))


    doTreeTest(dummyData2b, "sparse2b-sig","L", identity, (h,l) => new SparseSignatureTree(testTreeConf2b,l,h,prng2b))
    doTreeTest(dummyData512, "sparse512-sig","L", identity, (h,l) => new SparseSignatureTree(testTreeConf512,l,h,prng512))
    doTreeTest(dummyData2b, "sparseMix-sig","L", identity, (h,l) => new SparseSignatureTree(testTreeConf2b,l,h,prng512))
  } */

  test("Advanced Fractal Tree hash Test l=h/2"){
    //doTreeTest2("sparse2b","L", h => h/2, (h,l) => new SparseFractalTree(testTreeConf2bTok,l,h,prng2b))
    //doTreeTest2("sparse512","L", h => h/2, (h,l) => new SparseFractalTree(testTreeConf512Tok,l,h,prng512))
    //doTreeTest2("sparseMix","L", h => h/2, (h,l) => new SparseFractalTree(testTreeConf2bTok,l,h,prng512))


    doTreeTest(dummyData2b, "sparse2b-sig","L", h => h/2, (h,l) => new SparseSignatureTree(testTreeConf2b,l,h,prng2b))
    doTreeTest(dummyData512, "sparse512-sig","L", h => h/2, (h,l) => new SparseSignatureTree(testTreeConf512,l,h,prng512))
    doTreeTest(dummyData2b, "sparseMix-sig","L", h => h/2, (h,l) => new SparseSignatureTree(testTreeConf2b,l,h,prng512))
  }

  test("Advanced Fractal Tree hash Test l=h/4"){
    //doTreeTest2("sparse2b","L", h => h/2, (h,l) => new SparseFractalTree(testTreeConf2bTok,l,h,prng2b))
    //doTreeTest2("sparse512","L", h => h/2, (h,l) => new SparseFractalTree(testTreeConf512Tok,l,h,prng512))
    //doTreeTest2("sparseMix","L", h => h/2, (h,l) => new SparseFractalTree(testTreeConf2bTok,l,h,prng512))


    doTreeTest(dummyData2b, "sparse2b-sig","L", h => h/4, (h,l) => new SparseSignatureTree(testTreeConf2b,l,h,prng2b))
    doTreeTest(dummyData512, "sparse512-sig","L", h => h/4, (h,l) => new SparseSignatureTree(testTreeConf512,l,h,prng512))
    doTreeTest(dummyData2b, "sparseMix-sig","L", h => h/4, (h,l) => new SparseSignatureTree(testTreeConf2b,l,h,prng512))
  }

  /*test("Advanced Fractal Tree hash Test l = ~H/log(H)"){
    doTreeTest2("sparse2b","L", h =>  FractalTreeHeightCalc.calcOptHight(h).length, (h,l) => new SparseFractalTree(testTreeConf2bTok,l,h,prng2b))
    doTreeTest2("sparse512","L", h =>  FractalTreeHeightCalc.calcOptHight(h).length, (h,l) => new SparseFractalTree(testTreeConf512Tok,l,h,prng512))
    doTreeTest2("sparseMix","L", h =>  FractalTreeHeightCalc.calcOptHight(h).length, (h,l) => new SparseFractalTree(testTreeConf2bTok,l,h,prng512))

    doTreeTest(dummyData2b,"sparse2b-sig","L", h =>  FractalTreeHeightCalc.calcOptHight(h).length, (h,l) => new SparseSignatureTree(testTreeConf2b,l,h,prng2b))
    doTreeTest(dummyData512, "sparse512-sig","L", h =>  FractalTreeHeightCalc.calcOptHight(h).length, (h,l) => new SparseSignatureTree(testTreeConf512,l,h,prng512))
    doTreeTest(dummyData2b,"sparseMix-sig","L", h =>  FractalTreeHeightCalc.calcOptHight(h).length, (h,l) => new SparseSignatureTree(testTreeConf2b,l,h,prng512))

  }

  test("Fractal Tree hash Test l = ~H/log(H)"){
    doTreeTest2("fractal2b","L", h => FractalTreeHeightCalc.calcOptHight(h).length, (h,l) => new FractalTree(testTreeConf2bTok,l,h,prng2b))
    doTreeTest2("fractal512","L", h => FractalTreeHeightCalc.calcOptHight(h).length, (h,l) => new FractalTree(testTreeConf512Tok,l,h,prng512))
    doTreeTest2("fractalMix","L", h => FractalTreeHeightCalc.calcOptHight(h).length, (h,l) => new FractalTree(testTreeConf2bTok,l,h,prng512))

    doTreeTest(dummyData2b,"fractal2b-sig","L", h => FractalTreeHeightCalc.calcOptHight(h).length, (h,l) => new FractalSignatureTree(testTreeConf2b,l,h,prng2b))
    doTreeTest(dummyData512, "fractal512-sig","L", h => FractalTreeHeightCalc.calcOptHight(h).length, (h,l) => new FractalSignatureTree(testTreeConf512,l,h,prng512))
    doTreeTest(dummyData2b,"fractalMix-sig","L", h => FractalTreeHeightCalc.calcOptHight(h).length, (h,l) => new FractalSignatureTree(testTreeConf2b,l,h,prng512))

  }

  test("Log Tree hash Test k = 2/3"){
    doTreeTest2("log2b","K", h => if((h & 1) == 0) 2 else 3, (h,k) => new LogSpaceTimeTree(h,k,testTreeConf2bTok,prng2b))
    doTreeTest2("log512","K", h => if((h & 1) == 0) 2 else 3, (h,k) => new LogSpaceTimeTree(h,k,testTreeConf512Tok,prng512))
    doTreeTest2("logMix","K", h => if((h & 1) == 0) 2 else 3, (h,k) => new LogSpaceTimeTree(h,k,testTreeConf2bTok,prng512))

    doTreeTest(dummyData2b,"log2b-sig","K", h => if((h & 1) == 0) 2 else 3, (h,k) => new LogSpaceTimeSignatureTree(h,k,testTreeConf2b,prng2b))
    doTreeTest(dummyData512, "log512-sig","K", h => if((h & 1) == 0) 2 else 3, (h,k) => new LogSpaceTimeSignatureTree(h,k,testTreeConf512,prng512))
    doTreeTest(dummyData2b,"logMix-sig","K", h => if((h & 1) == 0) 2 else 3, (h,k) => new LogSpaceTimeSignatureTree(h,k,testTreeConf2b,prng512))
  }

  test("Log Tree hash Test k = ~h/2"){
    doTreeTest2("log2b","K", h => if(((h & 1) == 0) != ((h & 2) == 0)) (h/2)-1 else h / 2, (h,k) => new LogSpaceTimeTree(h,k,testTreeConf2bTok,prng2b))
    doTreeTest2("log512","K", h => if(((h & 1) == 0) != ((h & 2) == 0)) (h/2)-1 else h / 2, (h,k) => new LogSpaceTimeTree(h,k,testTreeConf512Tok,prng512))
    doTreeTest2("logMix","K", h => if(((h & 1) == 0) != ((h & 2) == 0)) (h/2)-1 else h / 2, (h,k) => new LogSpaceTimeTree(h,k,testTreeConf2bTok,prng512))

    doTreeTest(dummyData2b,"log2b-sig","K", h => if(((h & 1) == 0) != ((h & 2) == 0)) (h/2)-1 else h / 2, (h,k) => new LogSpaceTimeSignatureTree(h,k,testTreeConf2b,prng2b))
    doTreeTest(dummyData512, "log512-sig","K", h => if(((h & 1) == 0) != ((h & 2) == 0)) (h/2)-1 else h / 2, (h,k) => new LogSpaceTimeSignatureTree(h,k,testTreeConf512,prng512))
    doTreeTest(dummyData2b,"logMix-sig","K", h => if(((h & 1) == 0) != ((h & 2) == 0)) (h/2)-1 else h / 2, (h,k) => new LogSpaceTimeSignatureTree(h,k,testTreeConf2b,prng512))

  }
   */
}
