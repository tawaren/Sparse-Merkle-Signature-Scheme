package suit.algorithms.generate.signature.winterlitz;

import suit.algorithms.generate.tree.TreeSig;
import suit.algorithms.interfaces.*;
import suit.algorithms.prng.RandomAccessHashPRNG;
import suit.interfaces.ILeaveAuth;
import suit.interfaces.ITreeSig;
import suit.tools.MathHelper;
import suit.tools.arrays.DataArray;
import suit.tools.arrays.factory.IDataArrayFactory;

import java.util.Objects;

/**
 * Algorithm for generating winterlitz signatures
 */
public class WinterlitzSigGen implements IDetOneTimeSigGenerator {

    final byte w;           //paramter w (bits used per iterative hash)
    final int t;            //total amount of signature parts generated
    final int bs;           //basic signature part amounts (without checksum parts)
    final IFullHashFunction h;  //Used hash function

    /**
     * Generates an instance of the algorithm, can be used multiple times
     * @param w is the width used to form one signature part
     * @param h is the hash function used to calculate the signature parts
     */
    public WinterlitzSigGen(byte w, IFullHashFunction h) {
        this.w = w;
        this.h = h;
        int s = h.getOutByteLen()*8;                    //s is bitlen
        int mbs = s/w;                                  //mbs is the number of basic parts
        if(s%w != 0) mbs++;                             //if it don't work out evenly add 1 to hold the remainder
        int lg = MathHelper.log2RoundDown(mbs)+1+w;     //bit size of the checksum part
        int mt = mbs + lg/w;                            //mt  is the number of checksum parts
        if(lg%w != 0) mt++;                             //if it don't work out evenly add 1 to hold the remainder
        bs = mbs;
        t = mt;
    }

    @Override
    public IDataArrayFactory getBackingFactory() {
        //We use the same as the hash to prevent conversions when hashing
        return h.getBackingFactory();
    }

    //Calcs Pk
    private DataArray calcCommitmentLeaveLocal(ICryptPrng csprng){
      int hs = (1 << w) -1;                             //The maximal amount of hash iteration needed for w bits
      IMessageHasher YGen = h.createMessageHasher();    //Prepare the Hasher to hash everything together
      for(int i = 0; i < t; i++){                       //generate Pk part of each sk part and hash them together
          DataArray x = csprng.current();               //get cuurent sk part
          csprng.next();                                //advance sk part
          DataArray y = h.iterativeHash(x,hs);          //do the iterative hashing (calc pk part)
          YGen.update(y.asByteArray());                 //hash into existing pk parts
      }
      return YGen.finalStep();                          //produce the final pk
    }

    @Override
    public DataArray calcCommitmentLeave(DataArray Sk) {
        //Create a PRNG from a Sk and use it TODO: Change to not fixed PRNG
        return calcCommitmentLeaveLocal(new RandomAccessHashPRNG(h,Sk));
    }

    private DataArray[] calcSignatureLocal(ICryptPrng csprng, DataArray msg){
        msg = h.hash(msg);                                                  //hash the message
        DataArray[] res = new DataArray[t];                                 //prepare array for parts
        long C = 0;                                                         //Checksum| this could already be insufficient on w > 59 (but then wie have another problpem calc 2^59 hashes will not stop :)
        int c = 0;                                                          //index for part array
        int pad = ((msg.getByteSize()*8)%w);                                //number of pad bits needed
        int b;                                                              //value of the current w bits
        int i;                                                              //active position in the bitstream
        if(pad != 0){
            b = msg.extractBits(0,(byte)pad, DataArray.Endianess.BIG);      //extract the uneven part
            i = pad;                                                        //we have already consumed pad bytes
        } else {
            b = msg.extractBits(0,w, DataArray.Endianess.BIG);              //extract the even part
            i = w;                                                          //we have already consumed w bytes
        }
        C+= (1 << w) - b;                                                   //add the extracted b to checksum
        DataArray x = csprng.current();                                     //get the sk key part
        csprng.next();                                                      //advance the sk
        res[c++] = h.iterativeHash(x,b);                                    //calculate the first signature part

        for(; c < bs; i+=w){                                                //do the remaining basic parts
            b = msg.extractBits(i,w, DataArray.Endianess.BIG);              //extract w bytes
            C+= (1 << w) - b;                                               //add the extracted b to checksum
            x = csprng.current();                                           //get the sk key part
            csprng.next();                                                  //advance the sk
            res[c++] = h.iterativeHash(x,b);                                //calculate the signature part
        }

        msg = h.getBackingFactory().create(C);                              //make a DataArray from the Checksum
        i = (msg.getByteSize()*8) - ((t-bs)*w);                             //calculate the start - padding happens implicitly

        for(; c < t; i+=w){                                                 //do the checksum parts
            b = msg.extractBits(i,w, DataArray.Endianess.BIG);              //extract w bytes
            x = csprng.current();                                           //get the sk key part
            csprng.next();                                                  //advance the sk
            res[c++] = h.iterativeHash(x,b);                                //calculate the signature part

        }

        return res;                                                         //return the result
    }

    @Override
    public ITreeSig calcSignature(ILeaveAuth auth, DataArray msg) {
        //Create a PRNG from a Sk and use it TODO: Change to not fixed PRNG
        DataArray[] sig = calcSignatureLocal(new RandomAccessHashPRNG(h,auth.getLeaveSk()), msg);
        //Return the Signature
        return new TreeSig(sig, auth.getAuthPath());
    }

    @Override
    public DataArray[] calcSignature(DataArray Sk, DataArray msg) {
        //Create a PRNG from a Sk and use it TODO: Change to not fixed PRNG
        return calcSignatureLocal(new RandomAccessHashPRNG(h,Sk), msg);
    }

    @Override
    public boolean equals(Object o) {
        //checks for same w and same h
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        WinterlitzSigGen that = (WinterlitzSigGen) o;

        if (w != that.w) return false;
        if (!Objects.equals(h, that.h)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (int) w;
        result = 31 * result + (h != null ? h.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "WinterlitzSigGen{" +
                "w=" + w +
                ", hash =" + h +
                '}';
    }
}
