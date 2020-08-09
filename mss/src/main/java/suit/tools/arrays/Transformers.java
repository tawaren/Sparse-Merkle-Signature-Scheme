package suit.tools.arrays;

/**
 * A Lot of helpers for DataArray to transform between different versions
 */
public class Transformers {

    public static final int systemCopyLimit = 256;  //TODO: find good value (or is already ok)

    //invert endianess of a short
    static short swapBytes(short s){
        return (short)(
                (s << 8) |
                (s >>> 8)& 0x00FF
        );
    }

    //invert endianess of a int
    static int swapBytes(int i){
        return (
                (i << 24) |
                (i << 8)  & 0x00FF0000 |
                (i >>> 8) & 0x0000FF00 |
                (i >>> 24)& 0x000000FF
        );
    }

    //invert endianess of a long
    static long swapBytes(long l){
        return (
                (l << 56) |
                (l << 40) & 0x00FF000000000000L |
                (l << 24) & 0x0000FF0000000000L |
                (l << 8)  & 0x000000FF00000000L |
                (l >>> 8) & 0x00000000FF000000L |
                (l >>> 24)& 0x0000000000FF0000L |
                (l >>> 40)& 0x000000000000FF00L |
                (l >>> 56)& 0x00000000000000FFL
        );
    }

    //Array copy which handles endianess and different representations
    public static void copyTo(byte[] data, byte[] bArray, int start, int length) {
        if(data.length > systemCopyLimit){
            System.arraycopy(data,0,bArray,start,length);
        } else {
            for(int i = 0; i < length && i < data.length && i < bArray.length-start; i++) bArray[i+start] = data[i];
        }
    }

    //Array copy which handles endianess and different representations
    public static void copyTo(byte[] data,short[] sArray, int start, int length, DataArray.Endianess newEndianness) {
        if((newEndianness == DataArray.Endianess.BIG)){
            for(int i = start; i < start+length; i++){
                int idx = (i-start) << 1;
                sArray[i] = (short)(data[idx] << 8 | (data[idx+1] & 0xFF));
            }
        } else {
            for(int i = start; i < start+length; i++){
                int idx = (i-start) << 1;
                sArray[i] = (short)(data[idx + 1] << 8 | (data[idx] & 0xFF));
            }
        }
    }

    //Array copy which handles endianess and different representations
    public static void copyTo(byte[] data,int[] iArray, int start, int length, DataArray.Endianess newEndianness) {
        if((newEndianness == DataArray.Endianess.BIG)){
            for(int i = start; i < start+length; i++){
                int idx = (i-start) << 2;
                iArray[i] = ((data[idx]) << 24 | (data[idx+1] & 0xFF) << 16 | (data[idx+2] & 0xFF) << 8 | data[idx+3] & 0xFF);
            }
        } else {
            for(int i = start; i < start+length; i++){
                int idx = (i-start) << 2;
                iArray[i] = ((data[idx+3]) << 24 | (data[idx+2] & 0xFF) << 16 | (data[idx+1] & 0xFF) << 8 | data[idx] & 0xFF);
            }
        }
    }

    //Array copy which handles endianess and different representations
    public static void copyTo(byte[] data,long[] lArray, int start, int length, DataArray.Endianess newEndianness) {
        if((newEndianness == DataArray.Endianess.BIG)){
            for(int i = start; i < start+length; i++){
                int idx = (i-start) << 3;
                lArray[i] = ((data[idx] & 0xFFL) << 56L | (data[idx+1] & 0xFFL) << 48L | (data[idx+2] & 0xFFL) << 40L | (data[idx+3] & 0xFFL) << 32L | (data[idx+4] & 0xFFL) << 24L | (data[idx+5] & 0xFFL) << 16L | (data[idx+6] & 0xFFL) << 8L |(data[idx+7] & 0xFFL));
            }
        } else {
            for(int i = start; i < start+length; i++){
                int idx = (i-start) << 3;
                lArray[i] = ((data[idx+7] & 0xFFL) << 56L | (data[idx+6] & 0xFFL) << 48L | (data[idx+5] & 0xFFL) << 40L | (data[idx+4] & 0xFFL) << 32L | (data[idx+3] & 0xFFL) << 24L | (data[idx+2] & 0xFFL) << 16L | (data[idx+1] & 0xFFL) << 8L |(data[idx] & 0xFFL));
            }
        }
    }

    //Array copy which handles endianess and different representations
    public static void copyTo(short[] data, byte[] bArray, int start, int length, boolean isLittle) {
        if(isLittle){
            int i = 0;
            for(; i < data.length & length > 1; i++){
                int idx = (i << 1)+start;
                long cur = data[i];
                bArray[idx]   = (byte)cur; cur >>>= 8;
                bArray[idx+1] = (byte)cur;
                length -= 2;
            }

            if(i < data.length & length != 0){
                int idx = (i << 1)+start;
                long cur = data[i];
                if(length >= 1)bArray[idx]   = (byte)cur; cur >>>= 8;
                if(length >= 2)bArray[idx+1] = (byte)cur;
            }
        } else {
            int i = 0;
            for(; i < data.length & length > 1; i++){
                int idx = (i << 1)+start;
                long cur = data[i];
                bArray[idx+1] = (byte)cur; cur >>>= 8;
                bArray[idx]   = (byte)cur;
                length -= 2;
            }
            if(i < data.length & length != 0){
                int idx = (i << 1)+start;
                long cur = data[i] >>> 8;
                if(length >= 1)bArray[idx]   = (byte)cur;
            }

        }
    }

    //Array copy which handles endianess and different representations
    public static void copyTo(short[] data, short[] sArray, int start, int length, DataArray.Endianess newEndianness, boolean isLittle) {
        if(!(newEndianness == DataArray.Endianess.LITTLE ^ isLittle)){
            if(data.length > systemCopyLimit){
                System.arraycopy(data,0,sArray,start,length);
            } else {
                for(int i = 0; i < length && i < data.length && i < sArray.length-start; i++) sArray[i+start] = data[i];
            }
        }
        else{
            //we need to change Byte Order
            for(int i = 0; i < length && i < data.length && i < sArray.length-start; i++){
                sArray[i+start] = swapBytes(data[i]);
            }
        }
    }

    //Array copy which handles endianess and different representations
    public static void copyTo(short[] data, int[] iArray, int start, int length, DataArray.Endianess newEndianness, boolean isLittle) {
        if(newEndianness == DataArray.Endianess.LITTLE){
            if(isLittle){
                for(int i = start; i < start+length; i++){
                    int idx = (i-start) << 1;
                    iArray[i] = ((data[idx+1]) << 16 | (data[idx] & 0xFFFF));
                }
            } else {
                for(int i = start; i < start+length; i++){
                    int idx = (i-start) << 1;
                    iArray[i] = (swapBytes(data[idx+1]) << 16 | (swapBytes(data[idx]) & 0xFFFF));
                }
            }

        } else {
            if(!isLittle){
                for(int i = start; i < start+length; i++){
                    int idx = (i-start) << 1;
                    iArray[i] = ((data[idx]) << 16 | (data[idx+1] & 0xFFFF));
                }
            } else {
                for(int i = start; i < start+length; i++){
                    int idx = (i-start) << 1;
                    iArray[i] = (swapBytes(data[idx]) << 16 | (swapBytes(data[idx+1]) & 0xFFFF));
                }
            }
        }
    }


    //Array copy which handles endianess and different representations
    public static void copyTo(short[] data, long[] lArray, int start, int length, DataArray.Endianess newEndianness, boolean isLittle) {
        if(newEndianness == DataArray.Endianess.LITTLE){
            if(isLittle){
                for(int i = start; i < start+length; i++){
                    int idx = (i-start) << 2;
                    lArray[i] = ((data[idx+3] & 0xFFFFL) << 48 | (data[idx+2]  & 0xFFFFL) << 32 | (data[idx+1]  & 0xFFFFL) << 16 | (data[idx] & 0xFFFFL));
                }
            } else {
                for(int i = start; i < start+length; i++){
                    int idx = (i-start) << 2;
                    lArray[i] = ((swapBytes(data[idx+3]) & 0xFFFFL )<< 48 | (swapBytes(data[idx+2]) & 0xFFFFL ) << 32 | (swapBytes(data[idx+1]) & 0xFFFFL ) << 16 | (swapBytes(data[idx]) & 0xFFFFL));
                }
            }

        } else {
            if(!isLittle){
                for(int i = start; i < start+length; i++){
                    int idx = (i-start) << 2;
                    lArray[i] = ((data[idx] & 0xFFFFL) << 48 | (data[idx+1] & 0xFFFFL) << 32 | (data[idx+2] & 0xFFFFL) << 16 | (data[idx+3]  & 0xFFFFL));
                }
            } else {
                for(int i = start; i < start+length; i++){
                    int idx = (i-start) << 2;
                    lArray[i] = ((swapBytes(data[idx]) & 0xFFFFL) << 48 | (swapBytes(data[idx+1]) & 0xFFFFL) << 32 | (swapBytes(data[idx+2]) & 0xFFFFL) << 16 | (swapBytes(data[idx+3]) & 0xFFFFL));
                }
            }
        }
    }

    //Array copy which handles endianess and different representations
    public static void copyTo(int[] data, byte[] bArray, int start, int length, boolean isLittle) {
        if(isLittle){
            int i = 0;
            for(; i < data.length & length > 3; i++){
                int idx = (i << 2)+start;
                long cur = data[i];
                bArray[idx]   = (byte)cur; cur >>>= 8;
                bArray[idx+1] = (byte)cur; cur >>>= 8;
                bArray[idx+2] = (byte)cur; cur >>>= 8;
                bArray[idx+3] = (byte)cur; cur >>>= 8;
                length -= 4;
            }

            if(i < data.length & length != 0){
                int idx = (i << 2)+start;
                long cur = data[i];
                if(length >= 1)bArray[idx]   = (byte)cur; cur >>>= 8;
                if(length >= 2)bArray[idx+1] = (byte)cur; cur >>>= 8;
                if(length >= 3)bArray[idx+2] = (byte)cur; cur >>>= 8;
                if(length >= 4)bArray[idx+3] = (byte)cur;
            }
        } else {
            int i = 0;
            for(; i < data.length & length > 3; i++){
                int idx = (i << 2)+start;
                long cur = data[i];
                bArray[idx+3] = (byte)cur; cur >>>= 8;
                bArray[idx+2] = (byte)cur; cur >>>= 8;
                bArray[idx+1] = (byte)cur; cur >>>= 8;
                bArray[idx]   = (byte)cur;
                length -= 4;
            }
            if(i < data.length & length != 0){
                int idx = (i << 2)+start;
                long cur = data[i] >>> 8;
                if(length >= 3)bArray[idx+2] = (byte)cur; cur >>>= 8;
                if(length >= 2)bArray[idx+1] = (byte)cur; cur >>>= 8;
                if(length >= 1)bArray[idx]   = (byte)cur;
            }

        }
    }

    //Array copy which handles endianess and different representations
    public static void copyTo(int[] data, short[] sArray, int start, int length, DataArray.Endianess newEndianness, boolean isLittle) {
        if(newEndianness == DataArray.Endianess.LITTLE){
            if(isLittle){
                int i = 0;
                for(; i < data.length & length > 1; i++){
                    int idx = (i << 1)+start;
                    long cur = data[i];
                    sArray[idx]   = (short)cur; cur >>>= 16;
                    sArray[idx+1] = (short)cur;
                    length -= 2;
                }

                if(i < data.length & length != 0){
                    int idx = (i << 1)+start;
                    long cur = data[i];
                    if(length >= 1)sArray[idx]   = (short)cur;
                }
            } else {
                int i = 0;
                for(; i < data.length & length > 1; i++){
                    int idx = (i << 1)+start;
                    long cur = swapBytes(data[i]);
                    sArray[idx]   = (short)cur; cur >>>= 16;
                    sArray[idx+1] = (short)cur;
                    length -= 2;
                }

                if(i < data.length & length != 0){
                    int idx = (i << 1)+start;
                    long cur = swapBytes(data[i]);
                    if(length >= 1)sArray[idx]   = (short)cur;
                }
            }

        } else {
            if(!isLittle){
                int i = 0;
                for(; i < data.length & length > 1; i++){
                    int idx = (i << 1)+start;
                    long cur = data[i];
                    sArray[idx+1] = (short)cur; cur >>>= 16;
                    sArray[idx]   = (short)cur;
                    length -= 2;
                }

                if(i < data.length & length != 0){
                    int idx = (i << 1)+start;
                    long cur = data[i] >>> 16;
                    if(length >= 1)sArray[idx]   = (short)cur;
                }
            } else {
                int i = 0;
                for(; i < data.length & length > 1; i++){
                    int idx = (i << 1)+start;
                    long cur = swapBytes(data[i]);
                    sArray[idx+1] = (short)cur; cur >>>= 16;
                    sArray[idx]   = (short)cur;
                    length -= 2;
                }

                if(i < data.length & length != 0){
                    int idx = (i << 1)+start;
                    long cur = swapBytes(data[i]) >>> 16;
                    if(length >= 1)sArray[idx] = (short)cur;
                }
            }
        }
    }

    //Array copy which handles endianess and different representations
    public static void copyTo(int[] data, int[] iArray, int start, int length, DataArray.Endianess newEndianness, boolean isLittle) {
        if(!(newEndianness == DataArray.Endianess.LITTLE ^ isLittle)){
            if(data.length > systemCopyLimit){
                System.arraycopy(data,0,iArray,start,length);
            } else {
                for(int i = 0; i < length && i < data.length && i < iArray.length-start; i++) iArray[i+start] = data[i];
            }
        }
        else{
            //we need to change Byte Order
            for(int i = 0; i < length && i < data.length && i < iArray.length-start; i++){
                iArray[i+start] = swapBytes(data[i]);
            }
        }
    }

    //Array copy which handles endianess and different representations
    public static void copyTo(int[] data, long[] lArray, int start, int length, DataArray.Endianess newEndianness, boolean isLittle) {
        if(newEndianness == DataArray.Endianess.LITTLE){
            if(isLittle){
                for(int i = start; i < start+length; i++){
                    int idx = (i-start) << 1;
                    lArray[i] = ((data[idx+1] & 0xFFFFFFFFL) << 32 | (data[idx] & 0xFFFFFFFFL));
                }
            } else {
                for(int i = start; i < start+length; i++){
                    int idx = (i-start) << 1;
                    lArray[i] = ((swapBytes(data[idx+1]) & 0xFFFFFFFFL) << 32 | (swapBytes(data[idx]) & 0xFFFFFFFFL));
                }
            }

        } else {
            if(!isLittle){
                for(int i = start; i < start+length; i++){
                    int idx = (i-start) << 1;
                    lArray[i] = ((data[idx] & 0xFFFFFFFFL) << 32 | (data[idx+1] & 0xFFFFFFFFL));
                }
            } else {
                for(int i = start; i < start+length; i++){
                    int idx = (i-start) << 1;
                    lArray[i] = ((swapBytes(data[idx])& 0xFFFFFFFFL) << 32 | (swapBytes(data[idx+1]) & 0xFFFFFFFFL));
                }
            }
        }
    }

    //Array copy which handles endianess and different representations
    public static void copyTo(long[] data, byte[] bArray, int start, int length, boolean isLittle) {
        if(isLittle){
            int i = 0;
            for(; i < data.length & length > 7; i++){
                int idx = (i << 3)+start;
                long cur = data[i];
                bArray[idx]   = (byte)cur; cur >>= 8;
                bArray[idx+1] = (byte)cur; cur >>= 8;
                bArray[idx+2] = (byte)cur; cur >>= 8;
                bArray[idx+3] = (byte)cur; cur >>= 8;
                bArray[idx+4] = (byte)cur; cur >>= 8;
                bArray[idx+5] = (byte)cur; cur >>= 8;
                bArray[idx+6] = (byte)cur; cur >>= 8;
                bArray[idx+7] = (byte)cur;
                length -= 8;
            }

            if(i < data.length & length != 0){
                int idx = (i << 3)+start;
                long cur = data[i];
                if(length >= 1)bArray[idx]   = (byte)cur; cur >>= 8;
                if(length >= 2)bArray[idx+1] = (byte)cur; cur >>= 8;
                if(length >= 3)bArray[idx+2] = (byte)cur; cur >>= 8;
                if(length >= 4)bArray[idx+3] = (byte)cur; cur >>= 8;
                if(length >= 5)bArray[idx+4] = (byte)cur; cur >>= 8;
                if(length >= 6)bArray[idx+5] = (byte)cur; cur >>= 8;
                if(length >= 7)bArray[idx+6] = (byte)cur;
            }
        } else {
            int i = 0;
            for(; i < data.length & length > 7; i++){
                int idx = (i << 3)+start;
                long cur = data[i];
                bArray[idx+7] = (byte)cur; cur >>= 8;
                bArray[idx+6] = (byte)cur; cur >>= 8;
                bArray[idx+5] = (byte)cur; cur >>= 8;
                bArray[idx+4] = (byte)cur; cur >>= 8;
                bArray[idx+3] = (byte)cur; cur >>= 8;
                bArray[idx+2] = (byte)cur; cur >>= 8;
                bArray[idx+1] = (byte)cur; cur >>= 8;
                bArray[idx]   = (byte)cur;
                length -= 8;
            }
            if(i < data.length & length != 0){
                int idx = (i << 3)+start;
                long cur = data[i] >> 8;

                if(length >= 7)bArray[idx+6] = (byte)cur; cur >>= 8;
                if(length >= 6)bArray[idx+5] = (byte)cur; cur >>= 8;
                if(length >= 5)bArray[idx+4] = (byte)cur; cur >>= 8;
                if(length >= 4)bArray[idx+3] = (byte)cur; cur >>= 8;
                if(length >= 3)bArray[idx+2] = (byte)cur; cur >>= 8;
                if(length >= 2)bArray[idx+1] = (byte)cur; cur >>= 8;
                if(length >= 1)bArray[idx]   = (byte)cur;
            }

        }
    }

    //Array copy which handles endianess and different representations
    public static void copyTo(long[] data, short[] sArray, int start, int length, DataArray.Endianess newEndianness, boolean isLittle) {
        if(newEndianness == DataArray.Endianess.LITTLE){
            if(isLittle){
                int i = 0;
                for(; i < data.length & length > 3; i++){
                    int idx = (i << 2)+start;
                    long cur = data[i];
                    sArray[idx]   = (short)cur; cur >>>= 16;
                    sArray[idx+1] = (short)cur; cur >>>= 16;
                    sArray[idx+2] = (short)cur; cur >>>= 16;
                    sArray[idx+3] = (short)cur;
                    length -= 4;
                }

                if(i < data.length & length != 0){
                    int idx = (i << 2)+start;
                    long cur = data[i];
                    if(length >= 1)sArray[idx]   = (short)cur; cur >>>= 16;
                    if(length >= 2)sArray[idx+1] = (short)cur; cur >>>= 16;
                    if(length >= 3)sArray[idx+2] = (short)cur;
                }
            } else {
                int i = 0;
                for(; i < data.length & length > 3; i++){
                    int idx = (i << 2)+start;
                    long cur = swapBytes(data[i]);
                    sArray[idx]   = (short)cur; cur >>>= 16;
                    sArray[idx+1] = (short)cur; cur >>>= 16;
                    sArray[idx+2] = (short)cur; cur >>>= 16;
                    sArray[idx+3] = (short)cur;
                    length -= 4;
                }

                if(i < data.length & length != 0){
                    int idx = (i << 2)+start;
                    long cur = swapBytes(data[i]);
                    if(length >= 1)sArray[idx]   = (short)cur; cur >>>= 16;
                    if(length >= 2)sArray[idx+1] = (short)cur; cur >>>= 16;
                    if(length >= 3)sArray[idx+2] = (short)cur;
                }
            }

        } else {
            if(!isLittle){
                int i = 0;
                for(; i < data.length & length > 3; i++){
                    int idx = (i << 2)+start;
                    long cur = data[i];
                    sArray[idx+3] = (short)cur; cur >>>= 16;
                    sArray[idx+2] = (short)cur; cur >>>= 16;
                    sArray[idx+1] = (short)cur; cur >>>= 16;
                    sArray[idx]   = (short)cur;
                    length -= 4;
                }

                if(i < data.length & length != 0){
                    int idx = (i << 2)+start;
                    long cur = data[i] >> 16;

                    if(length >= 3)sArray[idx+2] = (short)cur; cur >>>= 16;
                    if(length >= 2)sArray[idx+1] = (short)cur; cur >>>= 16;
                    if(length >= 1)sArray[idx]   = (short)cur;
                }
            } else {
                int i = 0;
                for(; i < data.length & length > 3; i++){
                    int idx = (i << 2)+start;
                    long cur = swapBytes(data[i]);
                    sArray[idx+3] = (short)cur; cur >>>= 16;
                    sArray[idx+2] = (short)cur; cur >>>= 16;
                    sArray[idx+1] = (short)cur; cur >>>= 16;
                    sArray[idx]   = (short)cur;
                    length -= 4;
                }

                if(i < data.length & length != 0){
                    int idx = (i << 2)+start;
                    long cur = swapBytes(data[i]) >>> 16;

                    if(length >= 3)sArray[idx+2] = (short)cur; cur >>>= 16;
                    if(length >= 2)sArray[idx+1] = (short)cur; cur >>>= 16;
                    if(length >= 1)sArray[idx]   = (short)cur;
                }
            }
        }
    }

    //Array copy which handles endianess and different representations
    public static void copyTo(long[] data, int[] iArray, int start, int length, DataArray.Endianess newEndianness, boolean isLittle) {
        if(newEndianness == DataArray.Endianess.LITTLE){
            if(isLittle){
                int i = 0;
                for(; i < data.length & length > 1; i++){
                    int idx = (i << 1)+start;
                    long cur = data[i];
                    iArray[idx]   = (int)cur; cur >>>= 32;
                    iArray[idx+1] = (int)cur;
                    length -= 2;
                }

                if(i < data.length & length != 0){
                    int idx = (i << 1)+start;
                    long cur = data[i];
                    if(length >= 1)iArray[idx] = (int)cur;

                }
            } else {
                int i = 0;
                for(; i < data.length & length > 1; i++){
                    int idx = (i << 1)+start;
                    long cur = swapBytes(data[i]);
                    iArray[idx]   = (int)cur; cur >>>= 32;
                    iArray[idx+1] = (int)cur;
                    length -= 2;
                }

                if(i < data.length & length != 0){
                    int idx = (i << 1)+start;
                    long cur = swapBytes(data[i]);
                    if(length >= 1)iArray[idx]   = (int)cur;
                }
            }

        } else {
            if(!isLittle){
                int i = 0;
                for(; i < data.length & length > 1; i++){
                    int idx = (i << 1)+start;
                    long cur = data[i];
                    iArray[idx+1] = (int)cur; cur >>>= 32;
                    iArray[idx]   = (int)cur;
                    length -= 2;
                }

                if(i < data.length & length != 0){
                    int idx = (i << 1)+start;
                    long cur = data[i] >>> 32;
                    if(length >= 1)iArray[idx]   = (int)cur;
                }
            } else {
                int i = 0;
                for(; i < data.length & length > 1; i++){
                    int idx = (i << 1)+start;
                    long cur = swapBytes(data[i]);
                    iArray[idx+1] = (int)cur; cur >>>= 32;
                    iArray[idx]   = (int)cur;
                    length -= 2;
                }

                if(i < data.length & length != 0){
                    int idx = (i << 1)+start;
                    long cur = swapBytes(data[i]) >>> 32;
                    if(length >= 1)iArray[idx] = (int)cur;
                }
            }
        }
    }

    //Array copy which handles endianess and different representations
    public static void copyTo(long[] data, long[] lArray, int start, int length, DataArray.Endianess newEndianness, boolean isLittle) {
        if(!(newEndianness == DataArray.Endianess.LITTLE ^ isLittle)){
            if(data.length > systemCopyLimit){
                System.arraycopy(data,0,lArray,start,length);
            } else {
                for(int i = 0; i < length && i < data.length && i < lArray.length-start; i++) lArray[i+start] = data[i];
            }
        }
        else{
            //we need to change Byte Order
            for(int i = 0; i < length && i < data.length && i < lArray.length-start; i++){
                lArray[i+start] = swapBytes(data[i]);
            }
        }
    }

}
