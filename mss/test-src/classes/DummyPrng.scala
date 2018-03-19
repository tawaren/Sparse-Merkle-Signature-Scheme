package classes

import suit.algorithms.interfaces.ICryptPrng
import suit.tools.arrays.{RefCountDataArray, DataArray}

class DummyPrng(dummyData:DataArray, var index:Long = 0L) extends ICryptPrng{
  def unMark() {}
  def markAndCount: Int = 0
  def next(){index +=1}
  def current() = dummyData
  def capture() = new DummyPrng(dummyData,index)

}