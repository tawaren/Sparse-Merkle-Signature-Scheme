package benchmark.helper;


import suit.tools.DataHolder;
import suit.tools.arrays.DataArray;
import suit.tools.arrays.factory.IDataArrayFactory;

public class NullDataFactory implements IDataArrayFactory {

    static DataArray res = new NullDataArray();

    @Override
    public DataArray create() {
        return res;
    }

    @Override
    public DataArray create(byte value) {
        return res;
    }

    @Override
    public DataArray create(short value) {
        return res;
    }

    @Override
    public DataArray create(int value) {
        return res;
    }

    @Override
    public DataArray create(long value) {
        return res;
    }

    @Override
    public DataArray create(byte[] value) {
        return res;
    }

    @Override
    public DataArray create(short[] value) {
        return res;
    }

    @Override
    public DataArray create(int[] value) {
        return res;
    }

    @Override
    public DataArray create(long[] value) {
        return res;
    }

    @Override
    public DataArray create(DataHolder bs) {
        return res;
    }

    @Override
    public DataArray concat(DataArray... arrs) {
        return res;
    }

    @Override
    public DataArray extend(DataArray in, int len) {
        return res;
    }
}
