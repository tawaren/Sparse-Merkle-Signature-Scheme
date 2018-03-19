package suit.algorithms.hash.blake;


import suit.tools.arrays.ByteDataArray;
import suit.tools.arrays.DataArray;
import suit.tools.arrays.factory.IDataArrayFactory;

/**
 * Abilities shared between all blake versions based on 64bit blocks
 */
public final class BlakeLongCommon {
    //generates the result does cutting of ByteOverheads
    public static DataArray genResult(long[] h, int outLen, IDataArrayFactory backingFactory){
        if(outLen < 64){
            if((outLen & 7) == 0){
                //In this case it is long aligned
                long[] shrunken = new long[outLen >>> 3];
                for(int i = 0; i < shrunken.length & i < h.length; i++) shrunken[i] = h[i];
                return backingFactory.create(shrunken);
            }else{
                //In this case it is Byte aligned  (at least) | Case should be avoided can produce copy overhead
                //transform to Byte Array with outLen bytes
                byte[] shrunken = new byte[outLen];
                backingFactory.create(h).copyTo(shrunken);
                return new ByteDataArray(shrunken);         //Make Byte Based DataArray
            }
        }else{
            //In this case we do not have to shrink anything
            return backingFactory.create(h);
        }
    }

    //conversion from bytes to long (Little endian)
    public static long getLongLittle(final byte[] b, final int off) {
        return  ((b[off]     & 0xFFL)      ) |
                ((b[off + 1] & 0xFFL) <<  8) |
                ((b[off + 2] & 0xFFL) << 16) |
                ((b[off + 3] & 0xFFL) << 24) |
                ((b[off + 4] & 0xFFL) << 32) |
                ((b[off + 5] & 0xFFL) << 40) |
                ((b[off + 6] & 0xFFL) << 48) |
                ((b[off + 7] & 0xFFL) << 56);
    }

    //conversion from bytes to long (Big endian)
    public static long getLongBig(final byte[] b, final int off) {
        return  ((b[off + 7] & 0xFFL)      ) |
                ((b[off + 6] & 0xFFL) <<  8) |
                ((b[off + 5] & 0xFFL) << 16) |
                ((b[off + 4] & 0xFFL) << 24) |
                ((b[off + 3] & 0xFFL) << 32) |
                ((b[off + 2] & 0xFFL) << 40) |
                ((b[off + 1] & 0xFFL) << 48) |
                ((b[off]     & 0xFFL) << 56);
    }
}
