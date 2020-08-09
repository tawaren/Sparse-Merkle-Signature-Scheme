package standalone.sig;

import standalone.hash.FullHashFunction;
import standalone.hash.MessageHasher;
import standalone.mss.sparse.auth.LeaveAuth;
import standalone.mss.sparse.auth.TreeSig;
import standalone.prng.Prng;
import standalone.prng.RandomAccessHashPRNG;
import standalone.utils.DataUtils;
import standalone.utils.MathHelper;

import java.util.Objects;

/**
 * Algorithm for generating winternitz signatures
 */
public class WinternitzSigGen implements OTSGen{

    final byte w;               //paramter w (bits used per iterative hash)
    final int t;                //total amount of signature parts generated
    final int bs;               //basic signature part amounts (without checksum parts)
    final FullHashFunction h;   //Used hash function
    final MessageHasher mac;    //used as message digest

    /**
     * Generates an instance of the algorithm, can be used multiple times
     * @param w is the width used to form one signature part
     * @param h is the hash function used to calculate the signature parts
     */
    public WinternitzSigGen(byte w, FullHashFunction h, MessageHasher mac) {
        this.w = w;
        this.h = h;
        this.mac = mac;
        int s = h.getOutByteLen()*8;                    //s is bitlen
        int mbs = s/w;                                  //mbs is the number of basic parts
        if(s%w != 0) mbs++;                             //if it don't work out evenly add 1 to hold the remainder
        int lg = MathHelper.log2RoundDown(mbs)+1+w;     //bit size of the checksum part
        int mt = mbs + lg/w;                            //mt  is the number of checksum parts
        if(lg%w != 0) mt++;                             //if it don't work out evenly add 1 to hold the remainder
        bs = mbs;
        t = mt;
    }

    //Calcs Pk
    private byte[] calcCommitmentLeaveLocal(Prng csprng){
      int hs = (1 << w) -1;                             //The maximal amount of hash iteration needed for w bits
      for(int i = 0; i < t; i++){                       //generate Pk part of each sk part and hash them together
          byte[] x = csprng.current();                  //get cuurent sk part
          csprng.next();                                //advance sk part
          byte[] y = h.iterativeHash(x,hs);             //do the iterative hashing (calc pk part)
          mac.update(y);                                //hash into existing pk parts
      }
      return mac.finalStep();                           //produce the final pk
    }

    public byte[] calcCommitmentLeave(byte[] Sk) {
        //Create a PRNG from a Sk and use it
        return calcCommitmentLeaveLocal(new RandomAccessHashPRNG(h,Sk));
    }

    private byte[][] calcSignatureLocal(Prng csprng, byte[] msg){
        msg = h.hash(msg);                                                  //hash the message
        byte[][] res = new byte[t][];                                       //prepare array for parts
        long C = 0;                                                         //Checksum| this could already be insufficient on w > 59 (but then wie have another problpem calc 2^59 hashes will not stop :)
        int c = 0;                                                          //index for part array
        int pad = ((msg.length*8)%w);                                       //number of pad bits needed
        int b;                                                              //value of the current w bits
        int i;                                                              //active position in the bitstream
        if(pad != 0){
            b = DataUtils.extractBits(msg,0,(byte)pad);             //extract the uneven part
            i = pad;                                                        //we have already consumed pad bytes
        } else {
            b = DataUtils.extractBits(msg,0,w);                     //extract the even part
            i = w;                                                          //we have already consumed w bytes
        }
        C+= (1 << w) - b;                                                   //add the extracted b to checksum
        byte[] x = csprng.current();                                        //get the sk key part
        csprng.next();                                                      //advance the sk
        res[c++] = h.iterativeHash(x,b);                                    //calculate the first signature part

        for(; c < bs; i+=w){                                                //do the remaining basic parts
            b = DataUtils.extractBits(msg,i,w);                             //extract w bytes
            C+= (1 << w) - b;                                               //add the extracted b to checksum
            x = csprng.current();                                           //get the sk key part
            csprng.next();                                                  //advance the sk
            res[c++] = h.iterativeHash(x,b);                                //calculate the signature part
        }

        msg = DataUtils.create(C);                                          //make a DataArray from the Checksum
        i = (msg.length*8) - ((t-bs)*w);                                    //calculate the start - padding happens implicitly

        for(; c < t; i+=w){                                                 //do the checksum parts
            b = DataUtils.extractBits(msg, i,w);                            //extract w bytes
            x = csprng.current();                                           //get the sk key part
            csprng.next();                                                  //advance the sk
            res[c++] = h.iterativeHash(x,b);                                //calculate the signature part

        }

        return res;                                                         //return the result
    }

    public TreeSig calcSignature(LeaveAuth auth, byte[] msg) {
        //Create a PRNG from a Sk and use it TODO: Change to not fixed PRNG
        byte[][] sig = calcSignatureLocal(new RandomAccessHashPRNG(h,auth.getLeaveSk()), msg);
        //Return the Signature
        return new TreeSig(sig, auth.getAuthPath());
    }

    public byte[][] calcSignature(byte[] Sk, byte[] msg) {
        //Create a PRNG from a Sk and use it TODO: Change to not fixed PRNG
        return calcSignatureLocal(new RandomAccessHashPRNG(h,Sk), msg);
    }

    @Override
    public boolean equals(Object o) {
        //checks for same w and same h
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        WinternitzSigGen that = (WinternitzSigGen) o;

        if (w != that.w) return false;
        return Objects.equals(h, that.h);
    }

    @Override
    public int hashCode() {
        int result = (int) w;
        result = 31 * result + (h != null ? h.hashCode() : 0);
        return result;
    }

}
