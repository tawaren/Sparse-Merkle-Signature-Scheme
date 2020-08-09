package suit.algorithms.prng

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should
import suit.algorithms.hash.blake.blake2b.Blake2b
import suit.tools.arrays.factory.{IDataArrayFactory, LongDataFactory}
import suit.tools.arrays.DataArray.Endianess
import suit.algorithms.hash.hmac.HMAC

//We  could not really test prng fonction in an efficient way, because they should produce hard to predict values
class RandTest extends AnyFunSuite with should.Matchers{

  val factLong:IDataArrayFactory = new LongDataFactory(Endianess.LITTLE)


  test("""Continous Random Hash Prng Det"""){
    val sk = (new Blake2b()).hash(factLong.create(0))
    val prng1 = new RandomAccessHashPRNG( new Blake2b(),sk)
    val prng2 = new RandomAccessHashPRNG( new Blake2b(),sk)
    for(i <- 0 to 100){
      prng1.current() should equal (prng2.current())
      prng1.next()
      prng2.next()
    }
  }

  test("""HMAC DBRG Hash Prng Det"""){
    val sk = (new Blake2b()).hash(factLong.create(0))
    val prng1 = new Keyed_DBRG( new HMAC(new Blake2b()),sk)
    val prng2 = new Keyed_DBRG( new HMAC(new Blake2b()),sk)
    for(i <- 0 to 100){
      prng1.current() should equal (prng2.current())
      prng1.next()
      prng2.next()
    }
  }

  test("""Hash DBRG Hash Prng Det"""){
    val sk = (new Blake2b()).hash(factLong.create(0))
    val prng1 = new Hash_DBRG( new Blake2b(),sk)
    val prng2 = new Hash_DBRG( new Blake2b(),sk)
    for(i <- 0 to 100){
      prng1.current() should equal (prng2.current())
      prng1.next()
      prng2.next()
    }
  }


}
