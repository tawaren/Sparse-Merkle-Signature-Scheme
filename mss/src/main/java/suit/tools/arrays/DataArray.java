package suit.tools.arrays;


import java.util.Arrays;
import java.util.Formatter;

/**
 * Class for representing Data (mainly hash values)
 * Its Abstract to abstract over the concrete implementation
 */
public abstract class DataArray {

    //Enum to indicate Endianess
    public enum Endianess{BIG,LITTLE}

    //the endianess of the underlying implementation
    protected Endianess endianess;

    //Constructor
    protected DataArray(Endianess endian) {
        endianess = endian;
    }

    //the size of the Data in Bytes
    public abstract int getByteSize();
    //exclusive or between two values | x.length === y.length
    public abstract DataArray xor(DataArray other);
    //addition between two values  (Does not extend if their would be an overflow)
    //is (x + y) mod 2^x.length | x.length == y.length
    public abstract DataArray add(DataArray other);
    //copies the data into a Byte array
    public abstract void copyTo(byte[] bArray, int start, int length);
    //copies the data into a Short array
    public abstract void copyTo(short[] sArray, int start, int length, Endianess endianess);
    //copies the data into a Int array
    public abstract void copyTo(int[] iArray, int start, int length, Endianess endianess);
    //copies the data into a Long array
    public abstract void copyTo(long[] lArray, int start, int length, Endianess endianess);
    //extracts some bits from the data
    public abstract int extractBits(int bitIndex, byte bits, Endianess endianess);

    public Endianess getEndianess(){
      return endianess;
    }

    //converts the Data to a Byte array
    public byte[] asByteArray(){
        byte[] res = new byte[getByteSize()];
        copyTo(res);
        return res;
    }

    //converts the Data to a Short array
    public short[] asShortArray(Endianess newEndianness){
        int pad = ((getByteSize() & 1) == 0) ? 0 : 1;
        short[] res = new short[(getByteSize() >>> 1) + pad];
        copyTo(res,newEndianness);
        return res;
    }

    //converts the Data to a Int array
    public int[] asIntArray(Endianess newEndianness){
        int pad = ((getByteSize() & 3) == 0) ? 0 : 1;
        int[] res = new int[(getByteSize() >>> 2) + pad];
        copyTo(res,newEndianness);
        return res;
    }

    //converts the Data to a Long array
    public long[] asLongArray(Endianess newEndianness){
        int pad = ((getByteSize() & 7) == 0) ? 0 : 1;
        long[] res = new long[(getByteSize() >>> 3) + pad];
        copyTo(res,newEndianness);
        return res;
    }

    //converts the Data to a Short array with same endianess
    public short[] asShortArray(){
        return asShortArray(getEndianess());
    }

    //converts the Data to a Int array with same endianess
    public int[] asIntArray(){
        return asIntArray(getEndianess());
    }

    //converts the Data to a Long array with same endianess
    public long[] asLongArray(){
        return asLongArray(getEndianess());
    }

    //copies the data into a Short array with same endianess
    public  void copyTo(short[] sArray, int start, int length){
        copyTo(sArray, start, length, getEndianess());
    }

    //copies the data into a Int array with same endianess
    public  void copyTo(int[] iArray, int start, int length){
        copyTo(iArray,start,length,getEndianess());
    }

    //copies the data into a Long array with same endianess
    public  void copyTo(long[] lArray, int start, int length){
        copyTo(lArray,start,length,getEndianess());
    }

    //copies all the data into a Byte array  with same endianess
    public  void copyTo(byte[] bArray){
        copyTo(bArray,0,bArray.length);
    }

    //copies all the data into a Short array  with same endianess
    public  void copyTo(short[] sArray){
        copyTo(sArray,0,sArray.length,getEndianess());
    }

    //copies all the data into a Int array  with same endianess
    public  void copyTo(int[] iArray){
        copyTo(iArray,0,iArray.length,getEndianess());
    }

    //copies all the data into a Long array  with same endianess
    public  void copyTo(long[] lArray){
        copyTo(lArray,0,lArray.length,getEndianess());
    }

    //copies all the data into a Short array
    public  void copyTo(short[] sArray, Endianess endianess){
        copyTo(sArray,0,sArray.length,endianess);
    }

    //copies all the data into a Int array
    public  void copyTo(int[] iArray, Endianess endianess){
        copyTo(iArray,0,iArray.length,endianess);
    }

    //copies all the data into a Long array
    public  void copyTo(long[] lArray, Endianess endianess){
        copyTo(lArray,0,lArray.length,endianess);
    }

    //reads a long from position index (in Bytes) if swap is true, the composing words are swapped
    protected abstract long getData(int index, boolean swap);
    //reads a mask to extract one primitive over & (11111111 for Byte for example)
    protected abstract long getMask(int bits);

    //helper to extract data independent of its representation from this DataArray
    protected int extractHelper(int bitIndex, byte bits, byte alig, byte shift, boolean swap){
        //assert its a valid part of the Data
        assert(bits != 0 && ((bitIndex+bits) <= (getByteSize()*8)));
        //find offset to last aligned part
        int offset = (byte)(bitIndex & (alig-1));

        //check if this is a unaligned access
        if(offset+bits <= alig){
            //Aligned access
            long target = getData(bitIndex >>> shift, swap);            //get a long containing the Data
            long mask =  getMask(bits) >>> (offset);                    //shift the mask to the right spot
            return (int)((target & mask) >>> (alig-bits-offset));       //mask and shift back

        }else{

            //unaligned access

            //border start  (unaligned access at the start)
            int index = bitIndex >>> shift;
            long target = getData(index++, swap);                       //get a long containing the Data start part
            long mask = getMask(alig-offset) >>> offset;                //shift the mask to the right spot
            int res = (int)(((target & mask)));                         //mask (no shift back needed) (is already at right pos)

            //full  (aligned access)
            bits -= (alig-offset);
            while (bits >= alig){
                res = (res << alig) | (int)(getData(index++, swap));     //gets the data and or it into the accumulated res
                bits -= alig;
            }

            //border end (unaligned access at the end)
            target = getData(index, swap);                               //get a long containing the Data end part
            mask =  getMask(bits);                                       //get the mask (no shift needed mask already in right place
            return (res << bits)| (int)((target & mask) >>> (alig-bits));//mask and shift back
        }
    }

    @Override
    public String toString() {
        return bytesToHexString(asByteArray());
    }

    //this is slow, really slow  (should only used for pretty printing)
    private static String bytesToHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);

        Formatter formatter = new Formatter(sb);
        for (byte b : bytes) {
            formatter.format("%02x", b);
        }

        return sb.toString().toUpperCase();
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(asByteArray());
    }



}
