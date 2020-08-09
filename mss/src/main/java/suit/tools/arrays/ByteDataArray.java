package suit.tools.arrays;

import java.util.Arrays;

/**
 * Byte implementation of DataArray
 */
public class ByteDataArray extends DataArray{

    //the actual Data
    public final byte[] data;

    //The constructor (should not be used directly, use Factory instead)
    public ByteDataArray(byte[] data) {
        super(Endianess.BIG); //this is irrelevant for bytes
        this.data = data;
    }

    @Override
    public int getByteSize() {
        return data.length;
    }

    @Override
    public DataArray xor(DataArray other) {
        assert(other.getByteSize() == getByteSize());
        byte[] oArr = other.asByteArray();
        for(int i = 0; i < oArr.length && i < data.length; i++)oArr[i] = (byte)(oArr[i] ^ data[i]);
        return new ByteDataArray(oArr);
    }

    @Override
    public DataArray add(DataArray other) {
        assert(other.getByteSize() == getByteSize());
        byte[] oArr = other.asByteArray();
        boolean carry = false;
        for(int i = 0; i < oArr.length && i < data.length; i++){
            int res = ((((int)oArr[i]) & 255) + (((int)data[i]) & 255));
            if(carry)res++;
            oArr[i] = (byte)(res & 255);
            carry = res > Byte.MAX_VALUE;
        }
        return new ByteDataArray(oArr);
    }

    @Override
    public void copyTo(byte[] bArray, int start, int length) { Transformers.copyTo(data,bArray,start,length);}

    @Override
    public void copyTo(short[] sArray, int start, int length, Endianess newEndianness) {  Transformers.copyTo(data,sArray,start,length,newEndianness);}

    @Override
    public void copyTo(int[] iArray, int start, int length, Endianess newEndianness) {  Transformers.copyTo(data,iArray,start,length,newEndianness);}

    @Override
    public void copyTo(long[] lArray, int start, int length, Endianess newEndianness) {  Transformers.copyTo(data,lArray,start,length,newEndianness);}

    @Override
    protected long getData(int index, boolean swap) {
        return (data[index] & 0xFFL);
    }

    @Override
    protected long getMask(int bits) {
        return ((Byte.MIN_VALUE >> bits-1) & 0xFFL);
    }



    @Override
    public int extractBits(int bitIndex, byte bits, Endianess endianess) {
        return extractHelper(bitIndex, bits, (byte)8, (byte)3, true);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if ((o instanceof DataArray)) {
            DataArray that = (DataArray) o;
            return Arrays.equals(data, that.asByteArray());
        }
        return false;
    }

}
