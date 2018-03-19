package suit.algorithms.tree.hash

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import suit.tools.arrays.{LongDataArray, DataArray}
import suit.tools.arrays.DataArray.Endianess
import suit.algorithms.hash.blake.blake2b.Blake2b
import suit.algorithms.generate.tree.hash.TreeHash
import suit.algorithms.generate.tree.interfaces.{IFullCalcController, ITreeTraverserController}
import suit.tools.arrays.factory.IDataArrayFactory
import classes.TestPrng
import suit.algorithms.interfaces.ICryptPrng

class TreeHashgenTest extends FunSuite with ShouldMatchers{

  val h = 10
  val blake = new Blake2b()
  val factLong:IDataArrayFactory = blake.getBackingFactory


  test("root hash calc" ){
    val hash = calcRefHash()
    doWithHashStack(hash)
  }


  test("root hash Serial"){
    val hash = calcRefHash()
    val t0 = System.currentTimeMillis()
    doWithHashStack(hash)
    println("Ser: "+(System.currentTimeMillis()-t0))
  }

  def doWithHashStack(hash:DataArray){
    val hashGen = new TreeHash(new IFullCalcController {
      def calcLeave(leaveIndex: Long) =  factLong.create(leaveIndex)

      def calcLeave(localPrng: ICryptPrng) =  localPrng.current()

      def calcInnerNode(left: DataArray, right: DataArray) = blake.combineHashs(left,right)


      def provide(value: DataArray, level: Int, levelIndex: Long) {}
      def unMark() {}
      def markAndCount() = 0
    }, new TestPrng(0,factLong), h)
    hashGen.init()
    hash should equal(hashGen.getRootHash)
  }

  def calcRefHash()={
    def doLevel(level:Seq[DataArray]):DataArray = {
      if(level.size == 1){
        level(0)
      }else{
        level.size & (level.size - 1) should equal(0)
        val nextLevel = level.grouped(2).map{
          case Seq(a,b) => blake.combineHashs(a,b)
        }
        doLevel(nextLevel.toSeq)
      }
    }


    val startLevel = (0 until math.pow(2,h).toInt).map(x => factLong.create(x.toLong)).toIndexedSeq
    doLevel(startLevel)
  }
}
