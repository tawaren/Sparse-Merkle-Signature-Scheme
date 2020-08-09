package suit.algorithms.prng;

import suit.algorithms.hash.hmac.HMAC;
import suit.algorithms.interfaces.ICryptPrng;
import suit.algorithms.interfaces.IKeyedHashFunction;
import suit.tools.arrays.DataArray;
import suit.tools.arrays.RefCountDataArray;
import test.measure.MeasureToolbox;

import java.util.Objects;

public class RandomAccessHashPRNG implements ICryptPrng {
    final IKeyedHashFunction hash;
    final DataArray Sk;
    long index;

    @Override
    public void unMark() {
        RefCountDataArray.prepare(Sk);
    }

    @Override
    public int markAndCount() {
        int sum = 0;
        sum += RefCountDataArray.count(Sk);
        return sum;
    }

    public RandomAccessHashPRNG(IKeyedHashFunction hash, DataArray sk) {
        this(hash,sk,0);
    }

    private RandomAccessHashPRNG(IKeyedHashFunction hash, DataArray sk, long index) {
        this.hash = hash;
        Sk = sk;
        this.index = index;
    }

    @Override
    public long index() {
        return index;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public DataArray current() {
        if(MeasureToolbox.needsHashData){
            MeasureToolbox.emitHashCalculation();
            if(hash instanceof HMAC){
                MeasureToolbox.emitHashCalculation();
            }
        }
        return hash.mac(Sk, hash.getBackingFactory().create(index));
    }

    @Override
    public void next() {
        index++;
    }

    @Override
    public ICryptPrng capture() {
        return new RandomAccessHashPRNG(hash,Sk,index);
    }



    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RandomAccessHashPRNG that = (RandomAccessHashPRNG) o;

        if (!Objects.equals(Sk, that.Sk)) return false;
        if (!Objects.equals(hash, that.hash)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = hash != null ? hash.hashCode() : 0;
        result = 31 * result + (Sk != null ? Sk.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "RandomAccessHashPRNG{" +
                "hash=" + hash +
                ", Sk=" + Sk +
                '}';
    }
}
