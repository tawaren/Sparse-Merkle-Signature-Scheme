package suit.tools.arrays.factory;


import suit.tools.DataHolder;
import suit.tools.arrays.ByteDataArray;
import suit.tools.arrays.DataArray;
import suit.tools.arrays.IntDataArray;
import suit.tools.arrays.Transformers;

/**
 * IDataArrayFactory with Int as Primitive
 * Converting between different implementation is depending on endianess
 */
public class IntDataFactory implements IDataArrayFactory{

    //the endianess used in conversions
    DataArray.Endianess endian;

    public IntDataFactory(DataArray.Endianess endian) {
        this.endian = endian;
    }

    @Override
    public DataArray create() {
        return new IntDataArray(new int[0], endian);
    }

    @Override
    public DataArray create(byte value) {
        return new IntDataArray(new int[]{value}, endian);
    }

    @Override
    public DataArray create(short value) {
        return new IntDataArray(new int[]{value}, endian);
    }

    @Override
    public DataArray create(int value) {
        return new IntDataArray(new int[]{value}, endian);
    }

    @Override
    public DataArray create(long value) {
        int[] res = new int[2];
        Transformers.copyTo(new long[]{value},res,0,2,endian, endian == DataArray.Endianess.LITTLE);
        return create(res);
    }

    @Override
    public DataArray create(byte[] value) {
        assert((value.length & 3) == 0);
        int[] res = new int[value.length >>> 2];
        Transformers.copyTo(value,res,0,res.length,endian);
        return create(res);
    }

    @Override
    public DataArray create(short[] value) {
        assert((value.length & 1) == 0);
        int[] res = new int[value.length >>> 1];
        Transformers.copyTo(value,res,0,res.length,endian, endian == DataArray.Endianess.LITTLE);
        return create(res);
    }

    @Override
    public DataArray create(int[] value) {
        return new IntDataArray(value, endian);
    }

    @Override
    public DataArray create(long[] value) {
        int[] res = new int[value.length << 1];
        Transformers.copyTo(value,res,0,res.length,endian, endian == DataArray.Endianess.LITTLE);
        return create(res);
    }

    @Override
    public DataArray create(DataHolder bs) {
        return create(bs.getData());
    }

    @Override
    public DataArray concat(DataArray... arrs) {
        int size = 0;
        for(DataArray arr:arrs) size += arr.getByteSize();
        assert((size & 3) == 0);
        int[] res = new int[size >>> 2];
        int i = 0;
        for(DataArray arr:arrs) {
            int[] arrI;
            if(arr instanceof IntDataArray)arrI = ((IntDataArray)arr).data;
            else arrI = arr.asIntArray();
            Transformers.copyTo(arrI,res,i,res.length,endian,arr.getEndianess() == DataArray.Endianess.LITTLE);
            i+= arrI.length;
        }
        return create(res);
    }

    @Override
    public DataArray extend(DataArray in, int len) {
        assert(in.getByteSize() <= len && (len & 3) == 0);
        int[] res = new int[len>>>2];
        in.copyTo(res);
        return create(res);
    }
}
