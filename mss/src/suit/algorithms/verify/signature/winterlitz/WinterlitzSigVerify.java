package suit.algorithms.verify.signature.winterlitz;


import suit.algorithms.interfaces.IDetOneTimeSigVerifier;
import suit.algorithms.interfaces.IHashFunction;
import suit.algorithms.interfaces.IMessageHasher;
import suit.tools.MathHelper;
import suit.tools.arrays.DataArray;

/**
 * Algorithm for verifying signatures
 */
public class WinterlitzSigVerify implements IDetOneTimeSigVerifier{

    private final byte w;       //parameter w (width of one block)
    private final int t;        //total amount of signature parts generated
    private final int bs;       //basic signature part amounts (without checksum parts)
    IHashFunction h;            //Used hash function

    public WinterlitzSigVerify(byte w, IHashFunction h) {
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
    public DataArray verifySignature(DataArray msg, DataArray[] sig) {
        msg = h.hash(msg);                                                  //hash the message
        int hs = (1 << w) -1;                                               //The maximal amount of hash iteration needed for w bits
        IMessageHasher YGen = h.createMessageHasher();                      //Prepare the Hasher to hash everything together
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
        YGen.update(h.iterativeHash(sig[c++],hs - b).asByteArray());        //make the remaining iterations missing top the commitment and hash them into the previous blocks


        for(; c < bs; i+=w){                                                //do the remaining basic parts
            b = msg.extractBits(i,w, DataArray.Endianess.BIG);              //extract w bytes
            C+= (1 << w) - b;                                               //add the extracted b to checksum
            YGen.update(h.iterativeHash(sig[c++],hs - b).asByteArray());    //make the remaining iterations missing top the commitment and hash them into the previous blocks
        }


        msg = h.getBackingFactory().create(C);                              //make a DataArray from the Checksum
        i = (msg.getByteSize()*8) - ((t-bs)*w);                             //calculate the start - padding happens implicitly


        for(; c < t; i+=w){                                                 //do the checksum parts
            b = msg.extractBits(i,w, DataArray.Endianess.BIG);              //extract w bytes
            YGen.update(h.iterativeHash(sig[c++],hs - b).asByteArray());    //make the remaining iterations missing top the commitment and hash them into the previous blocks
        }

        return YGen.finalStep();                                            //calc the commitment
    }

}
