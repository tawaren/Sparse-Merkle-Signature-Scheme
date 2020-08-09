package suit.tools.arrays;

import java.util.Arrays;

/**
 * Int implementation of DataArray
 */
public class IntDataArray extends DataArray{

    //the actual Data
    public final int[] data;

    //The constructor (should not be used directly, use Factory instead)
    public IntDataArray(int[] data, Endianess endian) {
        super(endian);
        this.data = data;
    }

    @Override
    public int getByteSize() {
        return data.length << 2;
    }

    @Override
    public DataArray xor(DataArray other) {
        assert(other.getByteSize() == getByteSize());
        int[] oArr = other.asIntArray(getEndianess());
        for(int i = 0; i < oArr.length && i < data.length; i++)oArr[i] = oArr[i] ^ data[i];
        return new IntDataArray(oArr,getEndianess());
    }

    @Override
    public DataArray add(DataArray other) {
        assert(other.getByteSize() == getByteSize());
        int[] oArr = other.asIntArray(getEndianess());
        boolean carry = false;
        for(int i = 0; i < oArr.length && i < data.length; i++){
            long res = ((((long)oArr[i]) & 4294967295L) + (((long)data[i]) & 4294967295L));
            if(carry)res++;
            oArr[i] = (int)(res & 4294967295L);
            carry = res > Integer.MAX_VALUE;
        }
        return new IntDataArray(oArr,getEndianess());
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
            return (Transformers.swapBytes(data[index]) & 0xFFFFFFFFL);
        } else {
            return (data[index] & 0xFFFFFFFFL);

        }
    }

    @Override
    protected long getMask(int bits) {
        return ((Integer.MIN_VALUE >> bits-1) & 0xFFFFFFFFL);
    }

    @Override
    public int extractBits(int bitIndex, byte bits, Endianess endianess) {
        return extractHelper(bitIndex, bits, (byte)32, (byte)5, endianess != getEndianess());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if ((o instanceof DataArray)) {
            DataArray that = (DataArray) o;
            return Arrays.equals(data, that.asIntArray(getEndianess()));
        }
        return false;
    }
}
