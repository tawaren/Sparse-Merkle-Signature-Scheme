package suit.tools.arrays

import org.scalatest._
import util.Random
import suit.tools.arrays.DataArray.Endianess
import org.scalatest.matchers.ShouldMatchers
import suit.tools.MathHelper

class DataArrayTest extends FunSuite with ShouldMatchers{

   test("ByteArray copy rounds-trip test"){

     val rnd = new Random()
     val arrB = new Array[Byte](64)
     rnd.nextBytes(arrB)
     val data = new ByteDataArray(arrB)

     new ShortDataArray(data.asShortArray(Endianess.BIG),Endianess.BIG).asByteArray() should equal(arrB)
     new ShortDataArray(data.asShortArray(Endianess.LITTLE),Endianess.LITTLE).asByteArray() should equal(arrB)

     new IntDataArray(data.asIntArray(Endianess.BIG),Endianess.BIG).asByteArray() should equal(arrB)
     new IntDataArray(data.asIntArray(Endianess.LITTLE),Endianess.LITTLE).asByteArray() should equal(arrB)

     new LongDataArray(data.asLongArray(Endianess.BIG),Endianess.BIG).asByteArray() should equal(arrB)
     new LongDataArray(data.asLongArray(Endianess.LITTLE),Endianess.LITTLE).asByteArray() should equal(arrB)

   }


  test("ShortArray copy rounds-trip test"){
    val rnd = new Random()
    val arrB = new Array[Byte](64)
    rnd.nextBytes(arrB)
    val dataB = new ShortDataArray(new ByteDataArray(arrB).asShortArray(),Endianess.BIG)
    val dataL = new ShortDataArray(new ByteDataArray(arrB).asShortArray(),Endianess.LITTLE)

    new ByteDataArray(dataB.asByteArray()).asShortArray(Endianess.BIG) should equal(dataB.asShortArray(Endianess.BIG))
    new ByteDataArray(dataB.asByteArray()).asShortArray(Endianess.LITTLE) should equal(dataB.asShortArray(Endianess.LITTLE))

    new ByteDataArray(dataL.asByteArray()).asShortArray(Endianess.LITTLE) should equal(dataL.asShortArray(Endianess.LITTLE))
    new ByteDataArray(dataL.asByteArray()).asShortArray(Endianess.BIG) should equal(dataL.asShortArray(Endianess.BIG))

    new IntDataArray(dataB.asIntArray(Endianess.BIG),Endianess.BIG).asShortArray(Endianess.BIG) should equal(dataB.asShortArray(Endianess.BIG))
    new IntDataArray(dataB.asIntArray(Endianess.LITTLE),Endianess.LITTLE).asShortArray(Endianess.BIG) should equal(dataB.asShortArray(Endianess.BIG))
    new IntDataArray(dataB.asIntArray(Endianess.BIG),Endianess.BIG).asShortArray(Endianess.LITTLE) should equal(dataB.asShortArray(Endianess.LITTLE))
    new IntDataArray(dataB.asIntArray(Endianess.LITTLE),Endianess.LITTLE).asShortArray(Endianess.LITTLE) should equal(dataB.asShortArray(Endianess.LITTLE))

    new IntDataArray(dataL.asIntArray(Endianess.BIG),Endianess.BIG).asShortArray(Endianess.LITTLE) should equal(dataL.asShortArray(Endianess.LITTLE))
    new IntDataArray(dataL.asIntArray(Endianess.LITTLE),Endianess.LITTLE).asShortArray(Endianess.LITTLE) should equal(dataL.asShortArray(Endianess.LITTLE))
    new IntDataArray(dataL.asIntArray(Endianess.BIG),Endianess.BIG).asShortArray(Endianess.BIG) should equal(dataL.asShortArray(Endianess.BIG))
    new IntDataArray(dataL.asIntArray(Endianess.LITTLE),Endianess.LITTLE).asShortArray(Endianess.BIG) should equal(dataL.asShortArray(Endianess.BIG))

    new LongDataArray(dataB.asLongArray(Endianess.BIG),Endianess.BIG).asShortArray(Endianess.BIG) should equal(dataB.asShortArray(Endianess.BIG))
    new LongDataArray(dataB.asLongArray(Endianess.LITTLE),Endianess.LITTLE).asShortArray(Endianess.BIG) should equal(dataB.asShortArray(Endianess.BIG))
    new LongDataArray(dataB.asLongArray(Endianess.BIG),Endianess.BIG).asShortArray(Endianess.LITTLE) should equal(dataB.asShortArray(Endianess.LITTLE))
    new LongDataArray(dataB.asLongArray(Endianess.LITTLE),Endianess.LITTLE).asShortArray(Endianess.LITTLE) should equal(dataB.asShortArray(Endianess.LITTLE))

    new LongDataArray(dataL.asLongArray(Endianess.BIG),Endianess.BIG).asShortArray(Endianess.LITTLE) should equal(dataL.asShortArray(Endianess.LITTLE))
    new LongDataArray(dataL.asLongArray(Endianess.LITTLE),Endianess.LITTLE).asShortArray(Endianess.LITTLE) should equal(dataL.asShortArray(Endianess.LITTLE))
    new LongDataArray(dataL.asLongArray(Endianess.BIG),Endianess.BIG).asShortArray(Endianess.BIG) should equal(dataL.asShortArray(Endianess.BIG))
    new LongDataArray(dataL.asLongArray(Endianess.LITTLE),Endianess.LITTLE).asShortArray(Endianess.BIG) should equal(dataL.asShortArray(Endianess.BIG))
  }

  test("IntArray copy rounds-trip test"){
    val rnd = new Random()
    val arrB = new Array[Byte](64)
    rnd.nextBytes(arrB)
    val dataB = new IntDataArray(new ByteDataArray(arrB).asIntArray(),Endianess.BIG)
    val dataL = new IntDataArray(new ByteDataArray(arrB).asIntArray(),Endianess.LITTLE)

    new ByteDataArray(dataB.asByteArray()).asIntArray(Endianess.BIG) should equal(dataB.asIntArray(Endianess.BIG))
    new ByteDataArray(dataB.asByteArray()).asIntArray(Endianess.LITTLE) should equal(dataB.asIntArray(Endianess.LITTLE))

    new ByteDataArray(dataL.asByteArray()).asIntArray(Endianess.LITTLE) should equal(dataL.asIntArray(Endianess.LITTLE))
    new ByteDataArray(dataL.asByteArray()).asIntArray(Endianess.BIG) should equal(dataL.asIntArray(Endianess.BIG))


    new ShortDataArray(dataB.asShortArray(Endianess.BIG),Endianess.BIG).asIntArray(Endianess.BIG) should equal(dataB.asIntArray(Endianess.BIG))
    new ShortDataArray(dataB.asShortArray(Endianess.LITTLE),Endianess.LITTLE).asIntArray(Endianess.BIG) should equal(dataB.asIntArray(Endianess.BIG))
    new ShortDataArray(dataB.asShortArray(Endianess.BIG),Endianess.BIG).asIntArray(Endianess.LITTLE) should equal(dataB.asIntArray(Endianess.LITTLE))
    new ShortDataArray(dataB.asShortArray(Endianess.LITTLE),Endianess.LITTLE).asIntArray(Endianess.LITTLE) should equal(dataB.asIntArray(Endianess.LITTLE))

    new ShortDataArray(dataL.asShortArray(Endianess.BIG),Endianess.BIG).asIntArray(Endianess.LITTLE) should equal(dataL.asIntArray(Endianess.LITTLE))
    new ShortDataArray(dataL.asShortArray(Endianess.LITTLE),Endianess.LITTLE).asIntArray(Endianess.LITTLE) should equal(dataL.asIntArray(Endianess.LITTLE))
    new ShortDataArray(dataL.asShortArray(Endianess.BIG),Endianess.BIG).asIntArray(Endianess.BIG) should equal(dataL.asIntArray(Endianess.BIG))
    new ShortDataArray(dataL.asShortArray(Endianess.LITTLE),Endianess.LITTLE).asIntArray(Endianess.BIG) should equal(dataL.asIntArray(Endianess.BIG))


    new LongDataArray(dataB.asLongArray(Endianess.BIG),Endianess.BIG).asIntArray(Endianess.BIG) should equal(dataB.asIntArray(Endianess.BIG))
    new LongDataArray(dataB.asLongArray(Endianess.LITTLE),Endianess.LITTLE).asIntArray(Endianess.BIG) should equal(dataB.asIntArray(Endianess.BIG))
    new LongDataArray(dataB.asLongArray(Endianess.BIG),Endianess.BIG).asIntArray(Endianess.LITTLE) should equal(dataB.asIntArray(Endianess.LITTLE))
    new LongDataArray(dataB.asLongArray(Endianess.LITTLE),Endianess.LITTLE).asIntArray(Endianess.LITTLE) should equal(dataB.asIntArray(Endianess.LITTLE))

    new LongDataArray(dataL.asLongArray(Endianess.BIG),Endianess.BIG).asIntArray(Endianess.LITTLE) should equal(dataL.asIntArray(Endianess.LITTLE))
    new LongDataArray(dataL.asLongArray(Endianess.LITTLE),Endianess.LITTLE).asIntArray(Endianess.LITTLE) should equal(dataL.asIntArray(Endianess.LITTLE))
    new LongDataArray(dataL.asLongArray(Endianess.BIG),Endianess.BIG).asIntArray(Endianess.BIG) should equal(dataL.asIntArray(Endianess.BIG))
    new LongDataArray(dataL.asLongArray(Endianess.LITTLE),Endianess.LITTLE).asIntArray(Endianess.BIG) should equal(dataL.asIntArray(Endianess.BIG))

  }

  test("LongArray copy rounds-trip test"){
    val rnd = new Random()
    val arrB = new Array[Byte](64)
    rnd.nextBytes(arrB)
    val dataB = new LongDataArray(new ByteDataArray(arrB).asLongArray(),Endianess.BIG)
    val dataL = new LongDataArray(new ByteDataArray(arrB).asLongArray(),Endianess.LITTLE)

    new ByteDataArray(dataB.asByteArray()).asLongArray(Endianess.BIG) should equal(dataB.asLongArray(Endianess.BIG))
    new ByteDataArray(dataB.asByteArray()).asLongArray(Endianess.LITTLE) should equal(dataB.asLongArray(Endianess.LITTLE))

    new ByteDataArray(dataL.asByteArray()).asLongArray(Endianess.LITTLE) should equal(dataL.asLongArray(Endianess.LITTLE))
    new ByteDataArray(dataL.asByteArray()).asLongArray(Endianess.BIG) should equal(dataL.asLongArray(Endianess.BIG))


    new ShortDataArray(dataB.asShortArray(Endianess.BIG),Endianess.BIG).asLongArray(Endianess.BIG) should equal(dataB.asLongArray(Endianess.BIG))
    new ShortDataArray(dataB.asShortArray(Endianess.LITTLE),Endianess.LITTLE).asLongArray(Endianess.BIG) should equal(dataB.asLongArray(Endianess.BIG))
    new ShortDataArray(dataB.asShortArray(Endianess.BIG),Endianess.BIG).asLongArray(Endianess.LITTLE) should equal(dataB.asLongArray(Endianess.LITTLE))
    new ShortDataArray(dataB.asShortArray(Endianess.LITTLE),Endianess.LITTLE).asLongArray(Endianess.LITTLE) should equal(dataB.asLongArray(Endianess.LITTLE))

    new ShortDataArray(dataL.asShortArray(Endianess.BIG),Endianess.BIG).asLongArray(Endianess.LITTLE) should equal(dataL.asLongArray(Endianess.LITTLE))
    new ShortDataArray(dataL.asShortArray(Endianess.LITTLE),Endianess.LITTLE).asLongArray(Endianess.LITTLE) should equal(dataL.asLongArray(Endianess.LITTLE))
    new ShortDataArray(dataL.asShortArray(Endianess.BIG),Endianess.BIG).asLongArray(Endianess.BIG) should equal(dataL.asLongArray(Endianess.BIG))
    new ShortDataArray(dataL.asShortArray(Endianess.LITTLE),Endianess.LITTLE).asLongArray(Endianess.BIG) should equal(dataL.asLongArray(Endianess.BIG))


    new IntDataArray(dataB.asIntArray(Endianess.BIG),Endianess.BIG).asLongArray(Endianess.BIG) should equal(dataB.asLongArray(Endianess.BIG))
    new IntDataArray(dataB.asIntArray(Endianess.LITTLE),Endianess.LITTLE).asLongArray(Endianess.BIG) should equal(dataB.asLongArray(Endianess.BIG))
    new IntDataArray(dataB.asIntArray(Endianess.BIG),Endianess.BIG).asLongArray(Endianess.LITTLE) should equal(dataB.asLongArray(Endianess.LITTLE))
    new IntDataArray(dataB.asIntArray(Endianess.LITTLE),Endianess.LITTLE).asLongArray(Endianess.LITTLE) should equal(dataB.asLongArray(Endianess.LITTLE))

    new IntDataArray(dataL.asIntArray(Endianess.BIG),Endianess.BIG).asLongArray(Endianess.LITTLE) should equal(dataL.asLongArray(Endianess.LITTLE))
    new IntDataArray(dataL.asIntArray(Endianess.LITTLE),Endianess.LITTLE).asLongArray(Endianess.LITTLE) should equal(dataL.asLongArray(Endianess.LITTLE))
    new IntDataArray(dataL.asIntArray(Endianess.BIG),Endianess.BIG).asLongArray(Endianess.BIG) should equal(dataL.asLongArray(Endianess.BIG))
    new IntDataArray(dataL.asIntArray(Endianess.LITTLE),Endianess.LITTLE).asLongArray(Endianess.BIG) should equal(dataL.asLongArray(Endianess.BIG))

  }

  test("Little endian Journey test"){
    val rnd = new Random()
    val arrB = new Array[Byte](64)
    rnd.nextBytes(arrB)
    val byteData = new ByteDataArray(arrB)

    val shortData = new ShortDataArray(byteData.asShortArray(Endianess.LITTLE),Endianess.LITTLE)
    val intData =  new IntDataArray(shortData.asIntArray(Endianess.LITTLE),Endianess.LITTLE)
    val longData = new LongDataArray(intData.asLongArray(Endianess.LITTLE),Endianess.LITTLE)
    val newByteData = new ByteDataArray(longData.asByteArray())
    byteData should equal (newByteData)
  }

  test("Big endian Journey test"){
    val rnd = new Random()
    val arrB = new Array[Byte](64)
    rnd.nextBytes(arrB)
    val byteData = new ByteDataArray(arrB)

    val shortData = new ShortDataArray(byteData.asShortArray(Endianess.BIG),Endianess.BIG)
    val intData =  new IntDataArray(shortData.asIntArray(Endianess.BIG),Endianess.BIG)
    val longData = new LongDataArray(intData.asLongArray(Endianess.BIG),Endianess.BIG)
    val newByteData = new ByteDataArray(longData.asByteArray())
    byteData should equal (newByteData)
  }

  test("Mixed endian Journey test"){
    val rnd = new Random()
    val arrB = new Array[Byte](64)
    rnd.nextBytes(arrB)
    val byteData = new ByteDataArray(arrB)

    val shortData = new ShortDataArray(byteData.asShortArray(Endianess.BIG),Endianess.BIG)
    val intData =  new IntDataArray(shortData.asIntArray(Endianess.LITTLE),Endianess.LITTLE)
    val longData = new LongDataArray(intData.asLongArray(Endianess.BIG),Endianess.BIG)
    val newByteData = new ByteDataArray(longData.asByteArray())
    byteData should equal (newByteData)

    val shortData2 = new ShortDataArray(byteData.asShortArray(Endianess.LITTLE),Endianess.LITTLE)
    val intData2 =  new IntDataArray(shortData2.asIntArray(Endianess.BIG),Endianess.BIG)
    val longData2 = new LongDataArray(intData2.asLongArray(Endianess.LITTLE),Endianess.LITTLE)
    val newByteData2 = new ByteDataArray(longData2.asByteArray())
    byteData should equal (newByteData2)
  }

  test("Byte order test"){
    val arrB = Array[Byte](1,2,3,4,5,6,7,8)
    val byteData = new ByteDataArray(arrB)

    val arrSB = Array[Short](
      ((1 << 8) | (2 & 0xFF)).asInstanceOf[Short],
      ((3 << 8) | (4 & 0xFF)).asInstanceOf[Short],
      ((5 << 8) | (6 & 0xFF)).asInstanceOf[Short],
      ((7 << 8) | (8 & 0xFF)).asInstanceOf[Short]
    )
    val shortDataB = new ShortDataArray(arrSB, Endianess.BIG)

    val arrSL = Array[Short](
      ((2 << 8) | (1 & 0xFF)).asInstanceOf[Short],
      ((4 << 8) | (3 & 0xFF)).asInstanceOf[Short],
      ((6 << 8) | (5 & 0xFF)).asInstanceOf[Short],
      ((8 << 8) | (7 & 0xFF)).asInstanceOf[Short]
    )
    val shortDataL = new ShortDataArray(arrSL, Endianess.LITTLE)


    val arrIB = Array[Int](
      ((1 << 24) | ((2 & 0xFF) << 16) | ((3 & 0xFF) << 8) | ((4 & 0xFF))),
      ((5 << 24) | ((6 & 0xFF) << 16) | ((7 & 0xFF) << 8) | ((8 & 0xFF)))
    )
    val intDataB = new IntDataArray(arrIB, Endianess.BIG)

    val arrIL = Array[Int](
      ((4 << 24) | ((3 & 0xFF) << 16) | ((2 & 0xFF) << 8) | ((1 & 0xFF))),
      ((8 << 24) | ((7 & 0xFF) << 16) | ((6 & 0xFF) << 8) | ((5 & 0xFF)))
    )
    val intDataL = new IntDataArray(arrIL, Endianess.LITTLE)

    val arrLB = Array[Long](
      (((1  & 0xFFL) << 56) | ((2 & 0xFFL) << 48) | ((3 & 0xFFL) << 40) | ((4 & 0xFFL) << 32) | ((5 & 0xFFL) << 24) | ((6 & 0xFFL) << 16) | ((7 & 0xFFL) << 8) | ((8 & 0xFFL)))
    )
    val longDataB = new LongDataArray(arrLB, Endianess.BIG)

    val arrLL = Array[Long](
      (((8  & 0xFFL) << 56) | ((7 & 0xFFL) << 48) | ((6 & 0xFFL) << 40) | ((5 & 0xFFL) << 32) | ((4 & 0xFFL) << 24) | ((3 & 0xFFL) << 16) | ((2 & 0xFFL) << 8) | ((1 & 0xFFL)))
    )
    val longDataL = new LongDataArray(arrLL, Endianess.LITTLE)


    byteData.asShortArray(Endianess.BIG) should equal(arrSB)
    byteData.asShortArray(Endianess.LITTLE) should equal(arrSL)

    byteData.asIntArray(Endianess.BIG) should equal(arrIB)
    byteData.asIntArray(Endianess.LITTLE) should equal(arrIL)

    byteData.asLongArray(Endianess.BIG) should equal(arrLB)
    byteData.asLongArray(Endianess.LITTLE) should equal(arrLL)

    shortDataB.asByteArray() should equal(arrB)

    shortDataL.asByteArray() should equal(arrB)

    shortDataB.asShortArray(Endianess.BIG) should equal(arrSB)
    shortDataB.asShortArray(Endianess.LITTLE) should equal(arrSL)

    shortDataL.asShortArray(Endianess.BIG) should equal(arrSB)
    shortDataL.asShortArray(Endianess.LITTLE) should equal(arrSL)

    shortDataB.asIntArray(Endianess.BIG) should equal(arrIB)
    shortDataB.asIntArray(Endianess.LITTLE) should equal(arrIL)

    shortDataL.asIntArray(Endianess.BIG) should equal(arrIB)
    shortDataL.asIntArray(Endianess.LITTLE) should equal(arrIL)

    shortDataB.asLongArray(Endianess.BIG) should equal(arrLB)
    shortDataB.asLongArray(Endianess.LITTLE) should equal(arrLL)

    shortDataL.asLongArray(Endianess.BIG) should equal(arrLB)
    shortDataL.asLongArray(Endianess.LITTLE) should equal(arrLL)

    intDataB.asByteArray() should equal(arrB)

    intDataL.asByteArray() should equal(arrB)

    intDataB.asShortArray(Endianess.BIG) should equal(arrSB)
    intDataB.asShortArray(Endianess.LITTLE) should equal(arrSL)

    intDataL.asShortArray(Endianess.BIG) should equal(arrSB)
    intDataL.asShortArray(Endianess.LITTLE) should equal(arrSL)

    intDataB.asIntArray(Endianess.BIG) should equal(arrIB)
    intDataB.asIntArray(Endianess.LITTLE) should equal(arrIL)

    intDataL.asIntArray(Endianess.BIG) should equal(arrIB)
    intDataL.asIntArray(Endianess.LITTLE) should equal(arrIL)

    intDataB.asLongArray(Endianess.BIG) should equal(arrLB)
    intDataB.asLongArray(Endianess.LITTLE) should equal(arrLL)

    intDataL.asLongArray(Endianess.BIG) should equal(arrLB)
    intDataL.asLongArray(Endianess.LITTLE) should equal(arrLL)

    longDataB.asByteArray() should equal(arrB)

    longDataL.asByteArray() should equal(arrB)

    longDataB.asShortArray(Endianess.BIG) should equal(arrSB)
    longDataB.asShortArray(Endianess.LITTLE) should equal(arrSL)

    longDataL.asShortArray(Endianess.BIG) should equal(arrSB)
    longDataL.asShortArray(Endianess.LITTLE) should equal(arrSL)

    longDataB.asIntArray(Endianess.BIG) should equal(arrIB)
    longDataB.asIntArray(Endianess.LITTLE) should equal(arrIL)

    longDataL.asIntArray(Endianess.BIG) should equal(arrIB)
    longDataL.asIntArray(Endianess.LITTLE) should equal(arrIL)

    longDataB.asLongArray(Endianess.BIG) should equal(arrLB)
    longDataB.asLongArray(Endianess.LITTLE) should equal(arrLL)

    longDataL.asLongArray(Endianess.BIG) should equal(arrLB)
    longDataL.asLongArray(Endianess.LITTLE) should equal(arrLL)

  }


  test("unaligned copy test"){
    val arrB = Array[Byte](1,2,3,4,5,6,7)
    val arrS = Array[Short](
      ((1 << 8) | (2 & 0xFF)).asInstanceOf[Short],
      ((3 << 8) | (4 & 0xFF)).asInstanceOf[Short],
      ((5 << 8) | (6 & 0xFF)).asInstanceOf[Short]
    )
    val arrI = Array[Int](
      ((1 << 24) | ((2 & 0xFF) << 16) | ((3 & 0xFF) << 8) | ((4 & 0xFF)))
    )

    val arrSB = Array[Short](
      ((1 << 8) | (2 & 0xFF)).asInstanceOf[Short],
      ((3 << 8) | (4 & 0xFF)).asInstanceOf[Short],
      ((5 << 8) | (6 & 0xFF)).asInstanceOf[Short],
      ((7 << 8) | (8 & 0xFF)).asInstanceOf[Short]
    )
    val shortDataB = new ShortDataArray(arrSB, Endianess.BIG)

    val arrB2 = new Array[Byte](7)
    shortDataB.copyTo(arrB2)
    arrB2 should equal(arrB)

    val arrSL = Array[Short](
      ((2 << 8) | (1 & 0xFF)).asInstanceOf[Short],
      ((4 << 8) | (3 & 0xFF)).asInstanceOf[Short],
      ((6 << 8) | (5 & 0xFF)).asInstanceOf[Short],
      ((8 << 8) | (7 & 0xFF)).asInstanceOf[Short]
    )
    val shortDataL = new ShortDataArray(arrSL, Endianess.LITTLE)

    val arrB3 = new Array[Byte](7)
    shortDataL.copyTo(arrB3)
    arrB3 should equal(arrB)

    val arrIB = Array[Int](
      ((1 << 24) | ((2 & 0xFF) << 16) | ((3 & 0xFF) << 8) | ((4 & 0xFF))),
      ((5 << 24) | ((6 & 0xFF) << 16) | ((7 & 0xFF) << 8) | ((8 & 0xFF)))
    )
    val intDataB = new IntDataArray(arrIB, Endianess.BIG)

    val arrB4 = new Array[Byte](7)
    intDataB.copyTo(arrB4)
    arrB4 should equal(arrB)

    val arrS2 = new Array[Short](3)
    intDataB.copyTo(arrS2, Endianess.BIG)
    arrS2 should equal(arrS)

    val arrIL = Array[Int](
      ((4 << 24) | ((3 & 0xFF) << 16) | ((2 & 0xFF) << 8) | ((1 & 0xFF))),
      ((8 << 24) | ((7 & 0xFF) << 16) | ((6 & 0xFF) << 8) | ((5 & 0xFF)))
    )
    val intDataL = new IntDataArray(arrIL, Endianess.LITTLE)

    val arrB5 = new Array[Byte](7)
    intDataL.copyTo(arrB5)
    arrB5 should equal(arrB)

    val arrS3 = new Array[Short](3)
    intDataL.copyTo(arrS3, Endianess.BIG)
    arrS3 should equal(arrS)

    val arrLB = Array[Long](
      (((1  & 0xFFL) << 56) | ((2 & 0xFFL) << 48) | ((3 & 0xFFL) << 40) | ((4 & 0xFFL) << 32) | ((5 & 0xFFL) << 24) | ((6 & 0xFFL) << 16) | ((7 & 0xFFL) << 8) | ((8 & 0xFFL)))
    )
    val longDataB = new LongDataArray(arrLB, Endianess.BIG)

    val arrB6 = new Array[Byte](7)
    longDataB.copyTo(arrB6)
    arrB6 should equal(arrB)

    val arrS4 = new Array[Short](3)
    longDataB.copyTo(arrS4, Endianess.BIG)
    arrS4 should equal(arrS)

    val arrI2 = new Array[Int](1)
    longDataB.copyTo(arrI2, Endianess.BIG)
    arrI2 should equal(arrI)

    val arrLL = Array[Long](
      (((8  & 0xFFL) << 56) | ((7 & 0xFFL) << 48) | ((6 & 0xFFL) << 40) | ((5 & 0xFFL) << 32) | ((4 & 0xFFL) << 24) | ((3 & 0xFFL) << 16) | ((2 & 0xFFL) << 8) | ((1 & 0xFFL)))
    )
    val longDataL = new LongDataArray(arrLL, Endianess.LITTLE)

    val arrB7 = new Array[Byte](7)
    longDataL.copyTo(arrB7)
    arrB7 should equal(arrB)

    val arrS5 = new Array[Short](3)
    longDataL.copyTo(arrS5, Endianess.BIG)
    arrS5 should equal(arrS)

    val arrI3 = new Array[Int](1)
    longDataL.copyTo(arrI3, Endianess.BIG)
    arrI3 should equal(arrI)

  }

  test("partial copy test"){
    //we leave others out for now
    var targetB = new Array[Byte](8)

    val arrSB = Array[Short](
      ((1 << 8) | (2 & 0xFF)).asInstanceOf[Short],
      ((3 << 8) | (4 & 0xFF)).asInstanceOf[Short],
      ((5 << 8) | (6 & 0xFF)).asInstanceOf[Short],
      ((7 << 8) | (8 & 0xFF)).asInstanceOf[Short]
    )
    val shortDataB = new ShortDataArray(arrSB, Endianess.BIG)

    shortDataB.copyTo(targetB,4,4)
    targetB(4) should equal(1)
    targetB(5) should equal(2)
    targetB(6) should equal(3)
    targetB(7) should equal(4)

    val arrSL = Array[Short](
      ((2 << 8) | (1 & 0xFF)).asInstanceOf[Short],
      ((4 << 8) | (3 & 0xFF)).asInstanceOf[Short],
      ((6 << 8) | (5 & 0xFF)).asInstanceOf[Short],
      ((8 << 8) | (7 & 0xFF)).asInstanceOf[Short]
    )
    val shortDataL = new ShortDataArray(arrSL, Endianess.LITTLE)

    targetB = new Array[Byte](8)
    shortDataL.copyTo(targetB,4,4)
    targetB(4) should equal(1)
    targetB(5) should equal(2)
    targetB(6) should equal(3)
    targetB(7) should equal(4)

    val arrIB = Array[Int](
      ((1 << 24) | ((2 & 0xFF) << 16) | ((3 & 0xFF) << 8) | ((4 & 0xFF))),
      ((5 << 24) | ((6 & 0xFF) << 16) | ((7 & 0xFF) << 8) | ((8 & 0xFF)))
    )
    val intDataB = new IntDataArray(arrIB, Endianess.BIG)

    targetB = new Array[Byte](8)
    intDataB.copyTo(targetB,4,4)
    targetB(4) should equal(1)
    targetB(5) should equal(2)
    targetB(6) should equal(3)
    targetB(7) should equal(4)


    val arrIL = Array[Int](
      ((4 << 24) | ((3 & 0xFF) << 16) | ((2 & 0xFF) << 8) | ((1 & 0xFF))),
      ((8 << 24) | ((7 & 0xFF) << 16) | ((6 & 0xFF) << 8) | ((5 & 0xFF)))
    )
    val intDataL = new IntDataArray(arrIL, Endianess.LITTLE)
    targetB = new Array[Byte](8)
    intDataL.copyTo(targetB,4,4)
    targetB(4) should equal(1)
    targetB(5) should equal(2)
    targetB(6) should equal(3)
    targetB(7) should equal(4)

    val arrLB = Array[Long](
      (((1  & 0xFFL) << 56) | ((2 & 0xFFL) << 48) | ((3 & 0xFFL) << 40) | ((4 & 0xFFL) << 32) | ((5 & 0xFFL) << 24) | ((6 & 0xFFL) << 16) | ((7 & 0xFFL) << 8) | ((8 & 0xFFL)))
    )
    val longDataB = new LongDataArray(arrLB, Endianess.BIG)
    targetB = new Array[Byte](8)
    longDataB.copyTo(targetB,4,4)
    targetB(4) should equal(1)
    targetB(5) should equal(2)
    targetB(6) should equal(3)
    targetB(7) should equal(4)

    val arrLL = Array[Long](
      (((8  & 0xFFL) << 56) | ((7 & 0xFFL) << 48) | ((6 & 0xFFL) << 40) | ((5 & 0xFFL) << 32) | ((4 & 0xFFL) << 24) | ((3 & 0xFFL) << 16) | ((2 & 0xFFL) << 8) | ((1 & 0xFFL)))
    )
    val longDataL = new LongDataArray(arrLL, Endianess.LITTLE)
    targetB = new Array[Byte](8)
    longDataB.copyTo(targetB,4,4)
    targetB(4) should equal(1)
    targetB(5) should equal(2)
    targetB(6) should equal(3)
    targetB(7) should equal(4)
  }


  private def testExtractAlign(arr:DataArray, alig:Byte)={
    var i = 0

    val startmask = {
      var mask =  1
      for(e <- 1 until alig){
        mask = ((mask << 1) | 1)
      }
      mask << (8-alig)
    }

    def calcRes(index:Int) = ((((index/8)+1) & (startmask >>> (index%8))) >>> (8-(index%8)-alig))

    while((i+alig) <= (arr.getByteSize << 3)){
      arr.extractBits(i,alig,Endianess.BIG) should equal(calcRes(i))
      i+= alig
    }

  }

  private def testExtractBorder(arr:DataArray, alig:Byte)={
    var i = alig/2

    val startmask = {
      var mask =  1
      for(e <- 1 until alig/2){
        mask = ((mask << 1) | 1)
      }
      mask << (8-alig/2)
    }

    def calcRes(index:Int) = ((((index/8)+1) & (startmask >>> (index%8))) >>> (8-(index%8)-(alig/2)))
    def calcOverlapRes(index:Int)= ((calcRes(index) << alig/2) | calcRes(index+alig/2))

    while((i+alig) <= (arr.getByteSize << 3)){
      arr.extractBits(i,alig,Endianess.BIG) should equal(calcOverlapRes(i))
      i+= alig
    }
  }

  private def testArrExtr(arr:DataArray){
    testExtractAlign(arr,8)
    testExtractAlign(arr,4)
    testExtractAlign(arr,2)
    testExtractAlign(arr,1)

    testExtractBorder(arr,8)
    testExtractBorder(arr,4)
    testExtractBorder(arr,2)

    //test gmss stuff
  }

  test("bit access Test"){
    val arrB = Array[Byte](1,2,3,4,5,6,7,8)
    val byteData = new ByteDataArray(arrB)

    testArrExtr(byteData)

    //this seems Wrong
    val arrSB = Array[Short](
      ((1 << 8) | (2 & 0xFF)).asInstanceOf[Short],
      ((3 << 8) | (4 & 0xFF)).asInstanceOf[Short],
      ((5 << 8) | (6 & 0xFF)).asInstanceOf[Short],
      ((7 << 8) | (8 & 0xFF)).asInstanceOf[Short]
    )
    val shortDataB = new ShortDataArray(arrSB, Endianess.BIG)

    testArrExtr(shortDataB)

    val arrSL = Array[Short](
      ((2 << 8) | (1 & 0xFF)).asInstanceOf[Short],
      ((4 << 8) | (3 & 0xFF)).asInstanceOf[Short],
      ((6 << 8) | (5 & 0xFF)).asInstanceOf[Short],
      ((8 << 8) | (7 & 0xFF)).asInstanceOf[Short]
    )
    val shortDataL = new ShortDataArray(arrSL, Endianess.LITTLE)

    testArrExtr(shortDataL)

    val arrIB = Array[Int](
      ((1 << 24) | ((2 & 0xFF) << 16) | ((3 & 0xFF) << 8) | ((4 & 0xFF))),
      ((5 << 24) | ((6 & 0xFF) << 16) | ((7 & 0xFF) << 8) | ((8 & 0xFF)))
    )
    val intDataB = new IntDataArray(arrIB, Endianess.BIG)

    testArrExtr(intDataB)

    val arrIL = Array[Int](
      ((4 << 24) | ((3 & 0xFF) << 16) | ((2 & 0xFF) << 8) | ((1 & 0xFF))),
      ((8 << 24) | ((7 & 0xFF) << 16) | ((6 & 0xFF) << 8) | ((5 & 0xFF)))
    )
    val intDataL = new IntDataArray(arrIL, Endianess.LITTLE)

    testArrExtr(intDataL)

    val arrLB = Array[Long](
      (((1  & 0xFFL) << 56) | ((2 & 0xFFL) << 48) | ((3 & 0xFFL) << 40) | ((4 & 0xFFL) << 32) | ((5 & 0xFFL) << 24) | ((6 & 0xFFL) << 16) | ((7 & 0xFFL) << 8) | ((8 & 0xFFL)))
    )
    val longDataB = new LongDataArray(arrLB, Endianess.BIG)

    testArrExtr(longDataB)

    val arrLL = Array[Long](
      (((8  & 0xFFL) << 56) | ((7 & 0xFFL) << 48) | ((6 & 0xFFL) << 40) | ((5 & 0xFFL) << 32) | ((4 & 0xFFL) << 24) | ((3 & 0xFFL) << 16) | ((2 & 0xFFL) << 8) | ((1 & 0xFFL)))
    )
    val longDataL = new LongDataArray(arrLL, Endianess.LITTLE)

    testArrExtr(longDataL)


  }

  test("add Test"){
     //byte
     val bb1 = new ByteDataArray(Array[Byte](-1,0))
     val bb2 = new ByteDataArray(Array[Byte](-1,0))
     val bbr = new ByteDataArray(Array[Byte](-2,1))
     bb1.add(bb2) should equal (bbr)
     bb2.add(bb1) should equal (bbr)

     val b1 = new ByteDataArray(Array[Byte](-1))
     val b2 = new ByteDataArray(Array[Byte](-1))
     val br = new ByteDataArray(Array[Byte](-2))
     b1.add(b2) should equal (br)
     b2.add(b1) should equal (br)

     val bbb1 = new ByteDataArray(Array[Byte](1,-1,0))
     val bbb2 = new ByteDataArray(Array[Byte](2,-1,2))
     val bbbr = new ByteDataArray(Array[Byte](3,-2,3))
     bbb1.add(bbb2) should equal (bbbr)
     bbb2.add(bbb1) should equal (bbbr)

    //short big
    val ss1 = new ShortDataArray(Array[Short](-1,0),Endianess.BIG)
    val ss2 = new ShortDataArray(Array[Short](-1,0),Endianess.BIG)
    val ssr = new ShortDataArray(Array[Short](-2,1),Endianess.BIG)
    ss1.add(ss2) should equal (ssr)
    ss2.add(ss1) should equal (ssr)

    val s1 = new ShortDataArray(Array[Short](-1),Endianess.BIG)
    val s2 = new ShortDataArray(Array[Short](-1),Endianess.BIG)
    val sr = new ShortDataArray(Array[Short](-2),Endianess.BIG)
    s1.add(s2) should equal (sr)
    s2.add(s1) should equal (sr)

    val sss1 = new ShortDataArray(Array[Short](1,-1,0),Endianess.BIG)
    val sss2 = new ShortDataArray(Array[Short](2,-1,2),Endianess.BIG)
    val sssr = new ShortDataArray(Array[Short](3,-2,3),Endianess.BIG)
    sss1.add(sss2) should equal (sssr)
    sss2.add(sss1) should equal (sssr)

    //short little
    val _ss1 = new ShortDataArray(Array[Short](-1,0),Endianess.LITTLE)
    val _ss2 = new ShortDataArray(Array[Short](-1,0),Endianess.LITTLE)
    val _ssr = new ShortDataArray(Array[Short](-2,1),Endianess.LITTLE)
    _ss1.add(_ss2) should equal (_ssr)
    _ss2.add(_ss1) should equal (_ssr)

    val _s1 = new ShortDataArray(Array[Short](-1),Endianess.LITTLE)
    val _s2 = new ShortDataArray(Array[Short](-1),Endianess.LITTLE)
    val _sr = new ShortDataArray(Array[Short](-2),Endianess.LITTLE)
    _s1.add(_s2) should equal (_sr)
    _s2.add(_s1) should equal (_sr)

    val _sss1 = new ShortDataArray(Array[Short](1,-1,0),Endianess.LITTLE)
    val _sss2 = new ShortDataArray(Array[Short](2,-1,2),Endianess.LITTLE)
    val _sssr = new ShortDataArray(Array[Short](3,-2,3),Endianess.LITTLE)
    _sss1.add(_sss2) should equal (_sssr)
    _sss2.add(_sss1) should equal (_sssr)

    //int big
    val ii1 = new IntDataArray(Array[Int](-1,0),Endianess.BIG)
    val ii2 = new IntDataArray(Array[Int](-1,0),Endianess.BIG)
    val iir = new IntDataArray(Array[Int](-2,1),Endianess.BIG)
    ii1.add(ii2) should equal (iir)
    ii2.add(ii1) should equal (iir)

    val i1 = new IntDataArray(Array[Int](-1),Endianess.BIG)
    val i2 = new IntDataArray(Array[Int](-1),Endianess.BIG)
    val ir = new IntDataArray(Array[Int](-2),Endianess.BIG)
    i1.add(i2) should equal (ir)
    i2.add(i1) should equal (ir)

    val iii1 = new IntDataArray(Array[Int](1,-1,0),Endianess.BIG)
    val iii2 = new IntDataArray(Array[Int](2,-1,2),Endianess.BIG)
    val iiir = new IntDataArray(Array[Int](3,-2,3),Endianess.BIG)
    iii1.add(iii2) should equal (iiir)
    iii2.add(iii1) should equal (iiir)

    //int little
    val _ii1 = new IntDataArray(Array[Int](-1,0),Endianess.LITTLE)
    val _ii2 = new IntDataArray(Array[Int](-1,0),Endianess.LITTLE)
    val _iir = new IntDataArray(Array[Int](-2,1),Endianess.LITTLE)
    _ii1.add(_ii2) should equal (_iir)
    _ii2.add(_ii1) should equal (_iir)

    val _i1 = new IntDataArray(Array[Int](-1),Endianess.LITTLE)
    val _i2 = new IntDataArray(Array[Int](-1),Endianess.LITTLE)
    val _ir = new IntDataArray(Array[Int](-2),Endianess.LITTLE)
    _i1.add(_i2) should equal (_ir)
    _i2.add(_i1) should equal (_ir)

    val _iii1 = new IntDataArray(Array[Int](1,-1,0),Endianess.LITTLE)
    val _iii2 = new IntDataArray(Array[Int](2,-1,2),Endianess.LITTLE)
    val _iiir = new IntDataArray(Array[Int](3,-2,3),Endianess.LITTLE)
    _iii1.add(_iii2) should equal (_iiir)
    _iii2.add(_iii1) should equal (_iiir)

    //long big
    val ll1 = new LongDataArray(Array[Long](-1,0),Endianess.BIG)
    val ll2 = new LongDataArray(Array[Long](-1,0),Endianess.BIG)
    val llr = new LongDataArray(Array[Long](-2,1),Endianess.BIG)
    ll1.add(ll2) should equal (llr)
    ll2.add(ll1) should equal (llr)

    val l1 = new LongDataArray(Array[Long](-1),Endianess.BIG)
    val l2 = new LongDataArray(Array[Long](-1),Endianess.BIG)
    val lr = new LongDataArray(Array[Long](-2),Endianess.BIG)
    l1.add(l2) should equal (lr)
    l2.add(l1) should equal (lr)

    val lll1 = new LongDataArray(Array[Long](1,-1,0),Endianess.BIG)
    val lll2 = new LongDataArray(Array[Long](2,-1,2),Endianess.BIG)
    val lllr = new LongDataArray(Array[Long](3,-2,3),Endianess.BIG)
    lll1.add(lll2) should equal (lllr)
    lll2.add(lll1) should equal (lllr)

    //long little
    val _ll1 = new LongDataArray(Array[Long](-1,0),Endianess.LITTLE)
    val _ll2 = new LongDataArray(Array[Long](-1,0),Endianess.LITTLE)
    val _llr = new LongDataArray(Array[Long](-2,1),Endianess.LITTLE)
    _ll1.add(_ll2) should equal (_llr)
    _ll2.add(_ll1) should equal (_llr)

    val _l1 = new LongDataArray(Array[Long](-1),Endianess.LITTLE)
    val _l2 = new LongDataArray(Array[Long](-1),Endianess.LITTLE)
    val _lr = new LongDataArray(Array[Long](-2),Endianess.LITTLE)
    _l1.add(_l2) should equal (_lr)
    _l2.add(_l1) should equal (_lr)

    val _lll1 = new LongDataArray(Array[Long](1,-1,0),Endianess.LITTLE)
    val _lll2 = new LongDataArray(Array[Long](2,-1,2),Endianess.LITTLE)
    val _lllr = new LongDataArray(Array[Long](3,-2,3),Endianess.LITTLE)
    _lll1.add(_lll2) should equal (_lllr)
    _lll2.add(_lll1) should equal (_lllr)


  }
}
