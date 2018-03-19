package suit.algorithms.hash.blake;


import suit.tools.arrays.ByteDataArray;
import suit.tools.arrays.DataArray;
import suit.tools.arrays.factory.IDataArrayFactory;

/**
 * Abilities shared between all blake versions based on 32bit blocks
 */
public final class BlakeIntCommon {
    //generates the result does cutting of ByteOverheads
    public static DataArray genResult(int[] h, int outLen, IDataArrayFactory backingFactory){
        if(outLen < 32){
            if((outLen & 3) == 0){
                //In this case it is int aligned
                int[] shrunken = new int[outLen >>> 2];
                for(int i = 0; i < shrunken.length & i < h.length; i++) shrunken[i] = h[i];
                return backingFactory.create(shrunken);
            }else{
                //In this case it is Byte aligned  (at least) | Case should be avoided can produce copy overhead
                //transform to Byte Array with outLen bytes
                byte[] shrunken = new byte[outLen];
                backingFactory.create(h).copyTo(shrunken);
                return new ByteDataArray(shrunken);
            }
        }else{
            //In this case we do not have to shrink anything
            return backingFactory.create(h);
        }
    }

    //conversion from bytes to int
    public static int getInt(final byte[] b, final int off) {
        return  ((b[off]     & 0xFF)      ) |
                ((b[off + 1] & 0xFF) <<  8) |
                ((b[off + 2] & 0xFF) << 16) |
                ((b[off + 3] & 0xFF) << 24) ;
    }
}
