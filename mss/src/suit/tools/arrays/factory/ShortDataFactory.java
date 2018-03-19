package suit.tools.arrays.factory;


import suit.tools.DataHolder;
import suit.tools.arrays.DataArray;
import suit.tools.arrays.IntDataArray;
import suit.tools.arrays.ShortDataArray;
import suit.tools.arrays.Transformers;

/**
 * IDataArrayFactory with Short as Primitive
 * Converting between different implementation is depending on endianess
 */
public class ShortDataFactory implements IDataArrayFactory{

    //the endianess used in conversions
    DataArray.Endianess endian;

    public ShortDataFactory(DataArray.Endianess endian) {
        this.endian = endian;
    }

    @Override
    public DataArray create() {
        return new ShortDataArray(new short[0], endian);
    }

    @Override
    public DataArray create(byte value) {
        return new ShortDataArray(new short[]{value}, endian);
    }

    @Override
    public DataArray create(short value) {
        return new ShortDataArray(new short[]{value}, endian);
    }

    @Override
    public DataArray create(int value) {
        short[] res = new short[2];
        Transformers.copyTo(new int[]{value},res,0,2,endian, endian == DataArray.Endianess.LITTLE);
        return create(res);
    }

    @Override
    public DataArray create(long value) {
        short[] res = new short[4];
        Transformers.copyTo(new long[]{value},res,0,4,endian, endian == DataArray.Endianess.LITTLE);
        return create(res);
    }

    @Override
    public DataArray create(byte[] value) {
        assert((value.length & 1) == 0);
        short[] res = new short[value.length >>> 1];
        Transformers.copyTo(value,res,0,res.length,endian);
        return create(res);
    }

    @Override
    public DataArray create(short[] value) {
        return new ShortDataArray(value, endian);
    }

    @Override
    public DataArray create(int[] value) {
        short[] res = new short[value.length << 1];
        Transformers.copyTo(value,res,0,res.length,endian, endian == DataArray.Endianess.LITTLE);
        return create(res);
    }

    @Override
    public DataArray create(long[] value) {
        short[] res = new short[value.length << 2];
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
        assert((size & 1) == 0);
        short[] res = new short[size >>> 1];
        int i = 0;
        for(DataArray arr:arrs) {
            short[] arrS;
            if(arr instanceof ShortDataArray)arrS = ((ShortDataArray)arr).data;
            else arrS = arr.asShortArray();
            Transformers.copyTo(arrS,res,i,res.length,endian,arr.getEndianess() == DataArray.Endianess.LITTLE);
            i+= arrS.length;
        }
        return create(res);
    }

    @Override
    public DataArray extend(DataArray in, int len) {
        assert(in.getByteSize() <= len && (len & 1) == 0);
        short[] res = new short[len>>>1];
        in.copyTo(res);
        return create(res);
    }
}
