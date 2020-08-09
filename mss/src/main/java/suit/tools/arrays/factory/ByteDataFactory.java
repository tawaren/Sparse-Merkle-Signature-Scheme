package suit.tools.arrays.factory;


import suit.tools.DataHolder;
import suit.tools.arrays.ByteDataArray;
import suit.tools.arrays.DataArray;
import suit.tools.arrays.LongDataArray;
import suit.tools.arrays.Transformers;

/**
 * IDataArrayFactory with Byte as Primitive
 * Converting between different implementation is depending on endianess
 */
public class ByteDataFactory implements IDataArrayFactory{

    //the endianess used in conversions
    DataArray.Endianess endian;

    public ByteDataFactory(DataArray.Endianess endian) {
        this.endian = endian;
    }

    @Override
    public DataArray create() {
        return new ByteDataArray(new byte[0]);
    }

    @Override
    public DataArray create(byte value) {
        return new ByteDataArray(new byte[]{value});
    }

    @Override
    public DataArray create(short value) {
        byte[] res = new byte[2];
        Transformers.copyTo(new short[]{value},res,0,2, endian == DataArray.Endianess.LITTLE);
        return create(res);
    }

    @Override
    public DataArray create(int value) {
        byte[] res = new byte[4];
        Transformers.copyTo(new int[]{value},res,0,4, endian == DataArray.Endianess.LITTLE);
        return create(res);
    }

    @Override
    public DataArray create(long value) {
        byte[] res = new byte[8];
        Transformers.copyTo(new long[]{value},res,0,8, endian == DataArray.Endianess.LITTLE);
        return create(res);
    }

    @Override
    public DataArray create(byte[] value) {
        return new ByteDataArray(value);
    }

    @Override
    public DataArray create(short[] value) {
        byte[] res = new byte[value.length << 1];
        Transformers.copyTo(value,res,0,res.length, endian == DataArray.Endianess.LITTLE);
        return create(res);
    }

    @Override
    public DataArray create(int[] value) {
        byte[] res = new byte[value.length << 2];
        Transformers.copyTo(value,res,0,res.length, endian == DataArray.Endianess.LITTLE);
        return create(res);
    }

    @Override
    public DataArray create(long[] value) {
        byte[] res = new byte[value.length << 3];
        Transformers.copyTo(value,res,0,res.length, endian == DataArray.Endianess.LITTLE);
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
        byte[] res = new byte[size];
        int i = 0;
        for(DataArray arr:arrs) {
            byte[] arrB;
            if(arr instanceof ByteDataArray)arrB = ((ByteDataArray)arr).data;
            else arrB = arr.asByteArray();
            Transformers.copyTo(arrB,res,i,res.length);
            i+= arrB.length;
        }
        return create(res);
    }

    @Override
    public DataArray extend(DataArray in, int len) {
        assert(in.getByteSize() <= len);
        byte[] res = new byte[len];
        in.copyTo(res);
        return create(res);
    }
}
