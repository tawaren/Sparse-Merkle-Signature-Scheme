package suit.algorithms.signature.winterlitz

import org.scalatest.funsuite.AnyFunSuite
import suit.tools.arrays.DataArray
import org.scalatest.matchers.should
import suit.algorithms.hash.blake.blake2b.Blake2b
import suit.algorithms.generate.signature.winterlitz.WinterlitzSigGen
import suit.algorithms.prng.RandomAccessHashPRNG
import suit.algorithms.verify.signature.winterlitz.WinterlitzSigVerify
import suit.tools.arrays.factory.IDataArrayFactory

class WinterlitzTest extends AnyFunSuite with should.Matchers{

  val hash = new Blake2b()
  val factLong:IDataArrayFactory = hash.getBackingFactory




  def testWithMsg(msg:DataArray){
    val prng = new RandomAccessHashPRNG(new Blake2b(), factLong.create(42))

    val gen1 = new WinterlitzSigGen(1,hash)
    val pk1 = gen1.calcCommitmentLeave(prng.current())
    val sig1 = gen1.calcSignature(prng.current(),msg)
    val ver1 = new WinterlitzSigVerify(1,hash)
    ver1.verifySignature(msg,sig1) should equal(pk1)

    prng.next()

    val gen2 = new WinterlitzSigGen(2,hash)
    val pk2 = gen2.calcCommitmentLeave(prng.current())
    val sig2 = gen2.calcSignature(prng.current(),msg)
    val ver2 = new WinterlitzSigVerify(2,hash)
    ver2.verifySignature(msg,sig2) should equal(pk2)

    prng.next()

    val gen3 = new WinterlitzSigGen(3,hash)
    val pk3 = gen3.calcCommitmentLeave(prng.current())
    val sig3 = gen3.calcSignature(prng.current(),msg)
    val ver3 = new WinterlitzSigVerify(3,hash)
    ver3.verifySignature(msg,sig3) should equal(pk3)

    prng.next()

    val gen4 = new WinterlitzSigGen(4,hash)
    val pk4 = gen4.calcCommitmentLeave(prng.current())
    val sig4 = gen4.calcSignature(prng.current(),msg)
    val ver4 = new WinterlitzSigVerify(4,hash)
    ver4.verifySignature(msg,sig4) should equal(pk4)

    prng.next()

    val gen5 = new WinterlitzSigGen(5,hash)
    val pk5 = gen5.calcCommitmentLeave(prng.current())
    val sig5 = gen5.calcSignature(prng.current(),msg)
    val ver5 = new WinterlitzSigVerify(5,hash)
    ver5.verifySignature(msg,sig5) should equal(pk5)

    prng.next()

    val gen7 = new WinterlitzSigGen(7,hash)
    val pk7 = gen7.calcCommitmentLeave(prng.current())
    val sig7 = gen7.calcSignature(prng.current(),msg)
    val ver7 = new WinterlitzSigVerify(7,hash)
    ver7.verifySignature(msg,sig7) should equal(pk7)

    prng.next()

    val gen10 = new WinterlitzSigGen(10,hash)
    val pk10 = gen10.calcCommitmentLeave(prng.current())
    val sig10 = gen10.calcSignature(prng.current(),msg)
    val ver10 = new WinterlitzSigVerify(10,hash)
    ver10.verifySignature(msg,sig10) should equal(pk10)
  }

  test("""Winterlitz "The quick brown fox jumps over the lazy dog" test"""){
    testWithMsg(factLong.create("The quick brown fox jumps over the lazy dog".getBytes))
  }

  test("""Winterlitz "" test"""){
    testWithMsg(factLong.create(0))
  }

}
