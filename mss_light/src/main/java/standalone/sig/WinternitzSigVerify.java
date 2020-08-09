package standalone.sig;


import standalone.hash.HashFunction;
import standalone.hash.MessageHasher;
import standalone.utils.DataUtils;
import standalone.utils.MathHelper;

/**
 * Algorithm for verifying signatures
 */
public class WinternitzSigVerify {

    private final byte w;       //parameter w (width of one block)
    private final int t;        //total amount of signature parts generated
    private final int bs;       //basic signature part amounts (without checksum parts)
    HashFunction h;             //Used hash function
    MessageHasher mac;

    public WinternitzSigVerify(byte w, HashFunction h, MessageHasher mac) {
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

    public byte[] verifySignature(byte[] msg, byte[][] sig) {
        msg = h.hash(msg);                                                  //hash the message
        int hs = (1 << w) -1;                                               //The maximal amount of hash iteration needed for w bits
        long C = 0;                                                         //Checksum| this could already be insufficient on w > 59 (but then wie have another problpem calc 2^59 hashes will not stop :)
        int c = 0;                                                          //index for part array
        int pad = ((msg.length*8)%w);                                       //number of pad bits needed
        int b;                                                              //value of the current w bits
        int i;                                                              //active position in the bitstream
        if(pad != 0){
            b = DataUtils.extractBits(msg, 0,(byte)pad);            //extract the uneven part
            i = pad;                                                        //we have already consumed pad bytes
        } else {
            b = DataUtils.extractBits(msg,0,w);                     //extract the even part
            i = w;                                                          //we have already consumed w bytes
        }

        C+= (1 << w) - b;                                            //add the extracted b to checksum
        mac.update(h.iterativeHash(sig[c++],hs - b));        //make the remaining iterations missing top the commitment and hash them into the previous blocks


        for(; c < bs; i+=w){                                         //do the remaining basic parts
            b = DataUtils.extractBits(msg,i,w);                      //extract w bytes
            C+= (1 << w) - b;                                        //add the extracted b to checksum
            mac.update(h.iterativeHash(sig[c++],hs - b));    //make the remaining iterations missing top the commitment and hash them into the previous blocks
        }

        msg = DataUtils.create(C);                                   //make a DataArray from the Checksum
        i = (msg.length*8) - ((t-bs)*w);                             //calculate the start - padding happens implicitly


        for(; c < t; i+=w){                                          //do the checksum parts
            b =  DataUtils.extractBits(msg,i,w);                     //extract w bytes
            mac.update(h.iterativeHash(sig[c++],hs - b));    //make the remaining iterations missing top the commitment and hash them into the previous blocks
        }

        return mac.finalStep();                                      //calc the commitment
    }

}
