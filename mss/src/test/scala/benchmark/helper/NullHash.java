package benchmark.helper;


import suit.algorithms.interfaces.IFullHashFunction;
import suit.algorithms.interfaces.IHashFunction;
import suit.algorithms.interfaces.IKeyedHashFunction;
import suit.algorithms.interfaces.IMessageHasher;
import suit.tools.arrays.DataArray;
import suit.tools.arrays.factory.IDataArrayFactory;

public class NullHash implements IFullHashFunction {

    private static IMessageHasher hasher = new NullMessageHasher();
    private static IDataArrayFactory factory = new NullDataFactory();


    @Override
    public IMessageHasher createMessageHasher() {
        return hasher;
    }

    @Override
    public int getOutByteLen() {
        return 0;
    }

    @Override
    public IDataArrayFactory getBackingFactory() {
        return factory;
    }

    @Override
    public DataArray hash(DataArray data) {
        return data;
    }

    @Override
    public DataArray mac(DataArray Key, DataArray data) {
        return data;
    }

    @Override
    public DataArray iterativeHash(DataArray msg, int iterations) {
        return msg;
    }

    @Override
    public DataArray combineHashs(DataArray a, DataArray b) {
        return a;
    }
}
