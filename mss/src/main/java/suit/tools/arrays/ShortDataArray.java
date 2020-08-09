package suit.tools.arrays;

import java.util.Arrays;

/**
 * Short implementation of DataArray
 */
public final class ShortDataArray extends DataArray{

    //the actual Data
    public final short[] data;

    //The constructor (should not be used directly, use Factory instead)
    public ShortDataArray(short[] data, Endianess endian) {
        super(endian);
        this.data = data;
    }

    @Override
    public int getByteSize() {
        return data.length << 1;
    }

    @Override
    public DataArray xor(DataArray other) {
        assert(other.getByteSize() == getByteSize());
        short[] oArr = other.asShortArray(getEndianess());
        for(int i = 0; i < oArr.length && i < data.length; i++)oArr[i] = (short)(oArr[i] ^ data[i]);
        return new ShortDataArray(oArr,getEndianess());
    }

    @Override
    public DataArray add(DataArray other) {
        assert(other.getByteSize() == getByteSize());
        short[] oArr = other.asShortArray(getEndianess());
        boolean carry = false;
        for(int i = 0; i < oArr.length && i < data.length; i++){
            int res = ((((int)oArr[i]) & 65535) + (((int)data[i]) & 65535));
            if(carry)res++;
            oArr[i] = (short)(res & 65535);
            carry = res > Short.MAX_VALUE;
        }
        return new ShortDataArray(oArr,getEndianess());
    }

    @Override
    public void copyTo(byte[] bArray, int start, int length) { Transformers.copyTo(data,bArray,start,length,endianess == Endianess.LITTLE);}

    @Override
    public void copyTo(short[] sArray, int start, int length, Endianess newEndianness) { Transformers.copyTo(data,sArray,start,length,newEndianness,endianess == Endianess.LITTLE);}

    @Override
    public void copyTo(int[] iArray, int start, int length, Endianess newEndianness) { Transformers.copyTo(data,iArray,start,length,newEndianness,endianess == Endianess.LITTLE);}

    @Override
    public void copyTo(long[] lArray, int start, int length, Endianess newEndianness) { Transformers.copyTo(data,lArray,start,length,newEndianness,endianess == Endianess.LITTLE);}

    @Override
    protected long getData(int index, boolean swap) {
        if(swap){
            return (Transformers.swapBytes(data[index]) & 0xFFFFL);
        } else {
            return (data[index] & 0xFFFFL);

        }
    }

    @Override
    protected long getMask(int bits) {
        return ((Short.MIN_VALUE >> bits-1) & 0xFFFFL);
    }

    @Override
    public int extractBits(int bitIndex, byte bits, Endianess endianess) {
        return extractHelper(bitIndex, bits, (byte)16, (byte)4, endianess != getEndianess());
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if ((o instanceof DataArray)) {
            DataArray that = (DataArray) o;
            return Arrays.equals(data, that.asShortArray(getEndianess()));
        }
        return false;
    }
}
