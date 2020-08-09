package classes

import suit.algorithms.interfaces.ICryptPrng
import suit.tools.arrays.factory.IDataArrayFactory

class TestPrng(var index:Long = 0L, getBackingFactory:IDataArrayFactory) extends ICryptPrng{
  def unMark() {}
  def markAndCount: Int = 0
  def next() {index+=1}
  def current() =  getBackingFactory.create(index)
  def capture() =  new TestPrng(index,getBackingFactory)
}