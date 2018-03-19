package suit.algorithms.prng;

import suit.algorithms.interfaces.ICryptPrng;
import suit.algorithms.interfaces.IHashFunction;
import suit.algorithms.interfaces.IKeyedHashFunction;
import suit.tools.arrays.ByteDataArray;
import suit.tools.arrays.DataArray;
import suit.tools.arrays.RefCountDataArray;
import test.measure.MeasureToolbox;

/**
 * Secure Pseudo random number generator based on HMAC_DBRG from Nist
 * Does not have to be used with HMAC if underlying hash has secure mac function, but HMAC can improve security (but probably reduce speed)
 */
public final class Hash_DBRG implements ICryptPrng{
   private final IHashFunction hash;       //the underlying hash function
   private DataArray V;                    //The Private Key State
   private final DataArray C;              //Iternal state V
   private long index;                     //index to track current leaf number

   @Override
   public void unMark() {
        RefCountDataArray.prepare(C);
        RefCountDataArray.prepare(V);
   }

   @Override
   public int markAndCount() {
       int sum = 0;
       sum += RefCountDataArray.count(C);
       sum += RefCountDataArray.count(V);
       return sum;
   }




   //Constructs a Blank new One
   public Hash_DBRG(IHashFunction hash, DataArray Sk){
      this.hash = hash;
       V = hash_df(Sk);
       C = hash_df(hash.getBackingFactory().concat(hash.getBackingFactory().create(0), V));
       index = 0;
   }

    //Constructs a Copy of existing one
    private Hash_DBRG(Hash_DBRG target){
        this.hash = target.hash;
        this.index = target.index;
        this.C = target.C;
        this.V = target.V;
    }

    private DataArray hash_df(DataArray inp){
        int len = hash.getOutByteLen()>>>3;
        DataArray pre =  hash.getBackingFactory().create(new byte[]{
                1,
                (byte)((len >>> 24) & 255),
                (byte)((len >>> 16) & 255),
                (byte)((len >>> 8) & 255),
                (byte)((len >>> 0) & 255)}
        );
        if(MeasureToolbox.needsHashData)MeasureToolbox.emitHashCalculation();
        return hash.combineHashs(pre,inp);
    }

    @Override
    //Calcs next internal state
    public void next() {
        if(MeasureToolbox.needsHashData)MeasureToolbox.emitHashCalculation();
        DataArray H = hash.combineHashs ( hash.getBackingFactory().create(3),V);
        //RC needs to be size extended to V.length
        DataArray RC = hash.getBackingFactory().extend(hash.getBackingFactory().create(index+1),V.getByteSize());
        V = V.add(H).add(C).add(RC);
        index++;                //increment index
    }


    @Override
    //extracts current (clacs if not ready)
    public DataArray current() {
        if(MeasureToolbox.needsHashData)MeasureToolbox.emitHashCalculation();
        return hash.hash(V);
    }

    @Override
    //get the current index (mainly for assertions)
    public long index() {
        return index;
    }

    @Override
    //copy internal state of this
    public ICryptPrng capture() {
        return new Hash_DBRG(this);
    }
}
