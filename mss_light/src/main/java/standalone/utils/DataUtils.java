package standalone.utils;

import java.util.Random;

public class DataUtils {

    public static byte[] create(int value) {
        byte[] res = new byte[4];
        //is LittleEndian
        long cur = value;
        res[0] = (byte)cur; cur >>>= 8;
        res[1] = (byte)cur; cur >>>= 8;
        res[2] = (byte)cur; cur >>>= 8;
        res[3] = (byte)cur;
        return res;
    }

    public static byte[] create(long value) {
        byte[] res = new byte[8];
        //is LittleEndian
        long cur = value;
        res[0] = (byte)cur; cur >>>= 8;
        res[1] = (byte)cur; cur >>>= 8;
        res[2] = (byte)cur; cur >>>= 8;
        res[3] = (byte)cur; cur >>>= 8;
        res[4] = (byte)cur; cur >>>= 8;
        res[5] = (byte)cur; cur >>>= 8;
        res[6] = (byte)cur; cur >>>= 8;
        res[7] = (byte)cur;
        return res;
    }

    public static byte[] concat(byte[]... arrs) {
        int size = 0;
        for(byte[] arr:arrs) size += arr.length;
        byte[] res = new byte[size];
        int i = 0;
        for(byte[] arr:arrs) {
            System.arraycopy(arr,0,res,i,arr.length);
            i+= arr.length;
        }
        return res;
    }

    public static byte[] extend(byte[] in, int len) {
        assert(in.length <= len);
        byte[] res = new byte[len];
        System.arraycopy(in,0,res,0,in.length);
        return res;
    }

    public static byte[] xor(byte[] op1, byte[] op2) {
        assert(op1.length == op2.length);
        byte[] oArr = op2.clone();
        for(int i = 0; i < oArr.length && i < op1.length; i++)oArr[i] = (byte)(oArr[i] ^ op1[i]);
        return oArr;
    }

    public static byte[] add(byte[] op1, byte[] op2) {
        assert(op1.length == op2.length);
        byte[] oArr = op2.clone();
        boolean carry = false;
        for(int i = 0; i < oArr.length && i < op1.length; i++){
            int res = ((((int)oArr[i]) & 255) + (((int)op1[i]) & 255));
            if(carry)res++;
            oArr[i] = (byte)(res & 255);
            carry = res > Byte.MAX_VALUE;
        }
        return oArr;
    }

    //helper to extract data independent of its representation from this DataArray
    public static int extractBits(byte[] data, int bitIndex, byte bits){
        //assert its a valid part of the Data
        assert(bits != 0 && ((bitIndex+bits) <= (data.length*8)));
        //find offset to last aligned part
        int offset = (byte)(bitIndex & 7);
        //check if this is a aligned access
        if(offset+bits <= 8){
            //Aligned access
            long target = (data[bitIndex >>> 3] & 0xFFL);                   //get a long containing the Data
            long mask = ((Byte.MIN_VALUE >> bits-1) & 0xFFL);               //get a mask for bits bits
            long posMask = mask >>> offset;                                 //shift the mask to the right spot
            return (int)((target & posMask) >>> (8-bits-offset));           //mask and shift rest

        }else{
            //unaligned access
            //border start  (unaligned access at the start)
            int index = bitIndex >>> 3;
            long target =(data[index++] & 0xFFL);                               //get a long containing the Data start part
            int startBits = 8-offset;
            long mask = ((Byte.MIN_VALUE >> startBits-1) & 0xFFL);                //get a mask for the bits in this slot
            long posMask = mask >>> offset;                                     //shift the mask to the right spot
            int res = (int)(((target & posMask)));                              //mask (no shift back needed) (is already at right pos)

            //full  (aligned access)
            bits -= startBits;
            while (bits >= 8){
                res = (res << 8) | (int)((data[index++] & 0xFFL));     //gets the data and or it into the accumulated res
                bits -= 8;
            }

            //border end (unaligned access at the end)
            target = (data[index] & 0xFFL);                               //get a long containing the Data end part
            mask =  ((Byte.MIN_VALUE >> bits-1) & 0xFFL);                 //get the mask (no shift needed mask already in right place
            return (res << bits)| (int)((target & mask) >>> (8-bits));    //mask and shift back
        }
    }

    public static byte[] random(int len) {
        var rng = new byte[len];
        (new Random()).nextBytes(rng);
        return rng;
    }
}
