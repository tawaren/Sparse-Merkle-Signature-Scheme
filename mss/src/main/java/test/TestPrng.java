package test;

import suit.algorithms.interfaces.ICryptPrng;
import suit.tools.arrays.DataArray;
import suit.tools.arrays.factory.IDataArrayFactory;


public class TestPrng implements  ICryptPrng {

    long index;
    IDataArrayFactory fact;

    @Override
    public void unMark() {}

    @Override
    public int markAndCount() {return 0;}

    TestPrng(long index, IDataArrayFactory fact) {
        this.index = index;
        this.fact = fact;
    }

    TestPrng(IDataArrayFactory fact) {
        this.index = 0L;
        this.fact = fact;
    }

    @Override
    public long index() {
        return index;
    }

    @Override
    public DataArray current() {
        return fact.create(index);
    }

    @Override
    public void next() {
        index++;
    }

    @Override
    public ICryptPrng capture() {
        return new TestPrng(index,fact);
    }

}
