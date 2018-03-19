package suit.algorithms.prng;

import suit.algorithms.hash.hmac.HMAC;
import suit.algorithms.interfaces.ICryptPrng;
import suit.algorithms.interfaces.IKeyedHashFunction;
import suit.tools.arrays.ByteDataArray;
import suit.tools.arrays.DataArray;
import suit.tools.arrays.RefCountDataArray;
import test.measure.MeasureToolbox;

/**
 * Secure Pseudo random number generator based on HMAC_DBRG from Nist
 * Does not have to be used with HMAC if underlying hash has secure mac function, but HMAC can improve security (but probably reduce speed)
 */
public final class Keyed_DBRG implements ICryptPrng{
   private final IKeyedHashFunction hash;       //the underlying hash function
   private DataArray Key;                       //The Private Key State
   private DataArray V;                         //Iternal state V
   private long index;                          //index to track current leaf number

   @Override
   public void unMark() {
        RefCountDataArray.prepare(Key);
        RefCountDataArray.prepare(V);
   }

   @Override
   public int markAndCount() {
       int sum = 0;
       sum += RefCountDataArray.count(Key);
       sum += RefCountDataArray.count(V);
       return sum;
   }

    //Initial Vector
   private static long[] initialV = new long[]{
        0x55555555, 0x55555555, 0x55555555, 0x55555555,
        0x55555555, 0x55555555, 0x55555555, 0x55555555
    };


   //Constructs a Blank new One
   public Keyed_DBRG(IKeyedHashFunction hash, DataArray Sk){
      this.hash = hash;
      this.Key = hash.getBackingFactory().create(new long[8]);      //Initialize 0 key (loaded later with loadData
      this.V = hash.getBackingFactory().create(initialV);           //Load initial Vector into V
      loadData(Sk);
      index = 0;
   }

    //Constructs a Copy of existing one
    private Keyed_DBRG(Keyed_DBRG target){
        this.hash = target.hash;
        this.index = target.index;
        this.Key = target.Key;
        this.V = target.V;
    }

    //hashes Data into v,Key with two rounds
    //Could be used to refeed (but not used for current merkle tree usage)
    private void loadData(DataArray data){
        //4 compress calls  (unless data + 1Byte is bigger 128 Bytes, than it uses 6 compresses)
        if(MeasureToolbox.needsHashData){
            MeasureToolbox.emitHashCalculation();
            MeasureToolbox.emitHashCalculation();
            MeasureToolbox.emitHashCalculation();
            MeasureToolbox.emitHashCalculation();
            if(hash instanceof HMAC){
                MeasureToolbox.emitHashCalculation();
                MeasureToolbox.emitHashCalculation();
                MeasureToolbox.emitHashCalculation();
                MeasureToolbox.emitHashCalculation();
            }
        }
        Key = hash.mac(Key, hash.getBackingFactory().concat(V, hash.getBackingFactory().create(0), data));
        V = hash.mac(Key, V);
        Key = hash.mac(Key, hash.getBackingFactory().concat(V,  hash.getBackingFactory().create(1), data));
        V = hash.mac(Key, V);
    }


    @Override
    //Calcs next internal state
    public void next() {
        if(MeasureToolbox.needsHashData){
            MeasureToolbox.emitHashCalculation();
            MeasureToolbox.emitHashCalculation();
            if(hash instanceof HMAC){
                MeasureToolbox.emitHashCalculation();
                MeasureToolbox.emitHashCalculation();
            }
        }
        Key = hash.mac(Key, hash.getBackingFactory().concat(V,  hash.getBackingFactory().create(0)));
        V = hash.mac(Key, V);
        index++;                //increment index
    }


    @Override
    //extracts current (clacs if not ready)
    public DataArray current() {
        if(MeasureToolbox.needsHashData){
            MeasureToolbox.emitHashCalculation();
            if(hash instanceof HMAC)MeasureToolbox.emitHashCalculation();
        }
        return hash.mac(Key, V);
    }

    @Override
    //get the current index (mainly for assertions)
    public long index() {
        return index;
    }

    @Override
    //copy internal state of this
    public ICryptPrng capture() {
        return new Keyed_DBRG(this);
    }
}
