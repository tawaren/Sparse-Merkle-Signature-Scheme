package standalone.prng;

import standalone.hash.KeyedHashFunction;
import standalone.utils.DataUtils;

import java.util.Arrays;
import java.util.Objects;

public class RandomAccessHashPRNG implements Prng {
    final KeyedHashFunction hash;
    final byte[] Sk;
    long index;

    public RandomAccessHashPRNG(KeyedHashFunction hash, byte[] sk) {
        this(hash,sk,0);
    }

    private RandomAccessHashPRNG(KeyedHashFunction hash, byte[] sk, long index) {
        this.hash = hash;
        Sk = sk;
        this.index = index;
    }

    @Override
    public long index() {
        return index;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public byte[] current() {
        return hash.mac(Sk, DataUtils.create(index));
    }

    @Override
    public void next() {
        index++;
    }

    @Override
    public Prng capture() {
        return new RandomAccessHashPRNG(hash,Sk,index);
    }



    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RandomAccessHashPRNG that = (RandomAccessHashPRNG) o;

        if (Sk != null ? !Arrays.equals(Sk, that.Sk) : that.Sk != null) return false;
        if (!Objects.equals(hash, that.hash)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = hash != null ? hash.hashCode() : 0;
        result = 31 * result + (Sk != null ? Arrays.hashCode(Sk) : 0);
        return result;
    }

}
