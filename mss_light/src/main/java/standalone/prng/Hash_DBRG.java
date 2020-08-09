package standalone.prng;

import standalone.utils.DataUtils;
import standalone.hash.HashFunction;

/**
 * Secure Pseudo random number generator based on HASH_DBRG from Nist
 */
public final class Hash_DBRG implements Prng{
   private final HashFunction hash;       //the underlying hash function
   private byte[] V;                      //The Private Key State
   private final byte[] C;                //Internal state V
   private long index;                    //index to track current leaf number

   //Constructs a Blank new One
   public Hash_DBRG(HashFunction hash, byte[] Sk){
      this.hash = hash;
       V = hash_df(Sk);
       C = hash_df(DataUtils.concat(DataUtils.create(0), V));
       index = 0;
   }

    //Constructs a Copy of existing one
    private Hash_DBRG(Hash_DBRG target){
        this.hash = target.hash;
        this.index = target.index;
        this.C = target.C;
        this.V = target.V;
    }

    private byte[] hash_df(byte[] inp){
        int len = hash.getOutByteLen()>>>3;
        byte[] pre = new byte[]{
                1,
                (byte)((len >>> 24) & 255),
                (byte)((len >>> 16) & 255),
                (byte)((len >>> 8) & 255),
                (byte)((len) & 255)
        };
        return hash.combineHashes(pre,inp);
    }

    @Override
    //Calcs next internal state
    public void next() {
        byte[] H = hash.combineHashes(DataUtils.create(3),V);
        //RC needs to be size extended to V.length
        byte[] RC = DataUtils.extend(DataUtils.create(index+1),V.length);
        V = DataUtils.add(DataUtils.add(DataUtils.add(V, H), C),RC);
        index++;                //increment index
    }


    @Override
    //extracts current (clacs if not ready)
    public byte[] current() {
        return hash.hash(V);
    }

    @Override
    //get the current index (mainly for assertions)
    public long index() {
        return index;
    }

    @Override
    //copy internal state of this
    public Prng capture() {
        return new Hash_DBRG(this);
    }
}
