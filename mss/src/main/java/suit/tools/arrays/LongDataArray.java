package suit.tools.arrays;

import java.util.Arrays;

/**
 * Long implementation of DataArray
 */
public class LongDataArray extends DataArray{

    //the actual Data
    public final long[] data;

    //The constructor (should not be used directly, use Factory instead)
    public LongDataArray(long[] data, Endianess endian) {
        super(endian);
        this.data = data;
    }

    @Override
    public int getByteSize() {
        return data.length << 3;
    }

    @Override
    public DataArray xor(DataArray other) {
        assert(other.getByteSize() == getByteSize());
        long[] oArr = other.asLongArray(getEndianess());
        for(int i = 0; i < oArr.length && i < data.length; i++)oArr[i] = oArr[i] ^ data[i];
        return new LongDataArray(oArr,getEndianess());
    }

    @Override
    public DataArray add(DataArray other) {
        assert(other.getByteSize() == getByteSize());
        long[] oArr = other.asLongArray(getEndianess());
        boolean carry = false;
        for(int i = 0; i < oArr.length && i < data.length; i++){
            long res = (oArr[i] + data[i]);
            if(res == -1L && carry){
                res++;
                carry = true;
            } else {
                if(carry)res++;
                if(oArr[i] < 0 && data[i] < 0)carry = true;
            }
            oArr[i] = res;
        }
        return new LongDataArray(oArr,getEndianess());
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
            return Transformers.swapBytes(data[index]);
        } else {
            return data[index];

        }
    }

    @Override
    protected long getMask(int bits) {
         return ((Long.MIN_VALUE >> bits-1));
    }

    @Override
    public int extractBits(int bitIndex, byte bits, Endianess endianess) {
        return extractHelper(bitIndex, bits, (byte)64, (byte)6, endianess != getEndianess());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if ((o instanceof DataArray)) {
            DataArray that = (DataArray) o;
            return Arrays.equals(data, that.asLongArray(getEndianess()));
        }
        return false;
    }
}
