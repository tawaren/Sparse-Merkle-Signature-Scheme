package suit.algorithms.hash.blake

import org.scalatest._
import org.scalatest.matchers.ShouldMatchers
import suit.algorithms.hash.blake.blake2b.Blake2b
import suit.algorithms.hash.blake.blake2s.Blake2s

class BlakeTest extends FunSuite with ShouldMatchers{

  test("""Blake2b "" test"""){
    val blake2b = new Blake2b()
    val blakeHash = blake2b.hash(blake2b.getBackingFactory.create())
    blakeHash.toString should equal ("786A02F742015903C6C6FD852552D272912F4740E15847618A86E217F71F5419D25E1031AFEE585313896444934EB04B903A685B1448B755D56F701AFE9BE2CE")
    val hasher = blake2b.createMessageHasher()
    hasher.update(Array[Byte]())
    hasher.finalStep() should equal(blakeHash)
  }

  test("""Balake2s "" test"""){
    val blake2s = new Blake2s()
    val blakeHash = blake2s.hash(blake2s.getBackingFactory.create())
    blakeHash.toString should equal ("69217A3079908094E11121D042354A7C1F55B6482CA1A51E1B250DFD1ED0EEF9")
    val hasher = blake2s.createMessageHasher()
    hasher.update(Array[Byte]())
    hasher.finalStep() should equal(blakeHash)
  }

  test("""Blake2b "The quick brown fox jumps over the lazy dog" test"""){
    val blake2b = new Blake2b()
    val hasher = blake2b.createMessageHasher()
    hasher.update("The quick brown fox jumps over the lazy dog".getBytes)
    hasher.finalStep().toString  should equal("A8ADD4BDDDFD93E4877D2746E62817B116364A1FA7BC148D95090BC7333B3673F82401CF7AA2E4CB1ECD90296E3F14CB5413F8ED77BE73045B13914CDCD6A918")
  }

  test("""Balake2s "The quick brown fox jumps over the lazy dog" test"""){
    val blake2s = new Blake2s()
    val hasher = blake2s.createMessageHasher()
    hasher.update("The quick brown fox jumps over the lazy dog".getBytes)
    hasher.finalStep().toString  should equal("606BEEEC743CCBEFF6CBCDF5D5302AA855C256C29B88C8ED331EA1A6BF3C8812")
  }

  test("""Balake2s alig Test"""){
    val blake2s = new Blake2s()
    val blakeHash = blake2s.hash(blake2s.getBackingFactory.create("ABCDEFGH".getBytes))
    val hasher = blake2s.createMessageHasher()
    hasher.update("ABCDEFGH".getBytes)
    hasher.finalStep() should equal(blakeHash)
  }

  test("""Balake2b alig Test"""){
    val blake2b = new Blake2b()
    val blakeHash = blake2b.hash(blake2b.getBackingFactory.create("ABCDEFGH".getBytes))
    val hasher = blake2b.createMessageHasher()
    hasher.update("ABCDEFGH".getBytes)
    hasher.finalStep() should equal(blakeHash)
  }

  test("Blake2b combine test"){
    val blake2b = new Blake2b()
    val blakeHash1 = blake2b.hash(blake2b.getBackingFactory.create())
    val hasher = blake2b.createMessageHasher()
    hasher.update("The quick brown fox jumps over the lazy dog".getBytes)
    val blakeHash2 = hasher.finalStep()
    val cHash = blake2b.combineHashs(blakeHash1,blakeHash2)
    val c2Hash = blake2b.hash(blake2b.getBackingFactory.concat(blakeHash1,blakeHash2))
    cHash should equal(c2Hash)
  }

  test("Balake2s combine test"){
    val blake2s = new Blake2s()
    val blakeHash1 = blake2s.hash(blake2s.getBackingFactory.create())
    val hasher = blake2s.createMessageHasher()
    hasher.update("The quick brown fox jumps over the lazy dog".getBytes)
    val blakeHash2 = hasher.finalStep()
    val cHash = blake2s.combineHashs(blakeHash1,blakeHash2)
    val c2Hash = blake2s.hash(blake2s.getBackingFactory.concat(blakeHash1,blakeHash2))
    cHash should equal(c2Hash)
  }

  test("Blake2b iterative test"){
    val blake2b = new Blake2b()
    val blakeHash2 = blake2b.hash(blake2b.hash(blake2b.getBackingFactory.create()))
    val i2Hash = blake2b.iterativeHash(blake2b.getBackingFactory.create(),2)
    blakeHash2 should equal(i2Hash)

    val blakeHash5 = blake2b.hash(blake2b.hash(blake2b.hash(blake2b.hash(blake2b.hash(blake2b.getBackingFactory.create())))))
    val i5Hash = blake2b.iterativeHash(blake2b.getBackingFactory.create(),5)
    blakeHash5 should equal(i5Hash)
  }

  test("Balake2s iterative test"){
    val blake2s = new Blake2s()
    val blakeHash2 = blake2s.hash(blake2s.hash(blake2s.getBackingFactory.create()))
    val i2Hash = blake2s.iterativeHash(blake2s.getBackingFactory.create(),2)
    blakeHash2 should equal(i2Hash)

    val blakeHash5 = blake2s.hash(blake2s.hash(blake2s.hash(blake2s.hash(blake2s.hash(blake2s.getBackingFactory.create())))))
    val i5Hash = blake2s.iterativeHash(blake2s.getBackingFactory.create(),5)
    blakeHash5 should equal(i5Hash)
  }

  test("Blake2b mac test"){
    val blake2b = new Blake2b()
    val blakeHash = blake2b.hash(blake2b.getBackingFactory.concat(blake2b.getBackingFactory.create(42),blake2b.getBackingFactory.create()))
    val macHash = blake2b.mac(blake2b.getBackingFactory.create(42),blake2b.getBackingFactory.create())
    blakeHash should equal(macHash)
  }

  test("Balake2s mac test"){
    val blake2s = new Blake2s()
    val blakeHash = blake2s.hash(blake2s.getBackingFactory.concat(blake2s.getBackingFactory.create(42),blake2s.getBackingFactory.create()))
    val macHash = blake2s.mac(blake2s.getBackingFactory.create(42),blake2s.getBackingFactory.create())
    blakeHash should equal(macHash)
  }

}
