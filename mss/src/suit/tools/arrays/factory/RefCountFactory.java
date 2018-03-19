package suit.tools.arrays.factory;

import suit.tools.DataHolder;
import suit.tools.arrays.DataArray;
import suit.tools.arrays.RefCountDataArray;

/**
 * IDataArrayFactory witch alows to count instances in memory (for measurement only)
 */
public class RefCountFactory implements IDataArrayFactory{

    //the real factory
    IDataArrayFactory inner;

    public RefCountFactory(IDataArrayFactory inner) {
        this.inner = inner;
    }

    private DataArray build(DataArray arr){
        return new RefCountDataArray(arr);
    }

    @Override
    public DataArray create() {
        return build(inner.create());
    }

    @Override
    public DataArray create(byte value) {
        return build(inner.create(value));
    }

    @Override
    public DataArray create(short value) {
        return build(inner.create(value));
    }

    @Override
    public DataArray create(int value) {
        return build(inner.create(value));
    }

    @Override
    public DataArray create(long value) {
        return build(inner.create(value));
    }

    @Override
    public DataArray create(byte[] value) {
        return build(inner.create(value));
    }

    @Override
    public DataArray create(short[] value) {
        return build(inner.create(value));
    }

    @Override
    public DataArray create(int[] value) {
        return build(inner.create(value));
    }

    @Override
    public DataArray create(long[] value) {
        return build(inner.create(value));
    }

    @Override
    public DataArray create(DataHolder bs) {
        return build(inner.create(bs));
    }

    @Override
    public DataArray concat(DataArray... arrs) {
        return build(inner.concat(arrs));
    }

    @Override
    public DataArray extend(DataArray in, int len) {
        return build(inner.extend(in, len));
    }
}
