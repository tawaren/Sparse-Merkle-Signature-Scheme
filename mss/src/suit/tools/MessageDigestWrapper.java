package suit.tools;


import suit.algorithms.interfaces.IHashFunction;
import suit.algorithms.interfaces.IMessageHasher;
import suit.tools.arrays.DataArray;
import suit.tools.arrays.factory.ByteDataFactory;
import suit.tools.arrays.factory.IDataArrayFactory;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.security.MessageDigest;

/**
 *  Wrapper to use Java Cryptographics with this Framework
 */
public class MessageDigestWrapper  implements IHashFunction, IMessageHasher {

    MessageDigest digest;                       //the Java crypto Digest
    byte[] salt;                                //the salt
    Byte outLen;                                //the hash len
    final IDataArrayFactory backingFactory;     //the backing factory


    public MessageDigestWrapper(MessageDigest digest) {
        this(digest, null, null);
    }

    public MessageDigestWrapper(MessageDigest digest ,DataHolder salt) {
        this(digest, salt, null);
    }

    public MessageDigestWrapper(MessageDigest digest, byte outLen) {
        this(digest, null, outLen);
    }

    public MessageDigestWrapper(MessageDigest digest ,DataHolder salt, Byte outLen) {
        this.digest = digest;
        this.salt = salt == null ? null : salt.getData();
        this.outLen = outLen;
        backingFactory = new ByteDataFactory(DataArray.Endianess.LITTLE);
        assert(outLen == null || outLen <= digest.getDigestLength());
        reset();
    }

    @Override
    public int getOutByteLen() {
        if(outLen != null) return outLen;
        return digest.getDigestLength();        //fallback to primitive len
    }

    @Override
    public IDataArrayFactory getBackingFactory() {
        return backingFactory;
    }

    //cuts a result down
    private byte[] reduce(byte[] in){
       if(outLen != null){
           byte[] reduced = new byte[outLen];
           for(int i= 0; i < in.length && i < reduced.length; i++) reduced[i] = in[i];
           return reduced;
       }
       return in;           //No reduction necessary
    }

    @Override
    public IMessageHasher createMessageHasher() {
        return this;
    }

    @Override
    //uses this MessageHasher for standalone hashin
    public DataArray hash(DataArray data) {
        reset();                                                    //reset
        digest.update(data.asByteArray());                          //fill
        return backingFactory.create(reduce(digest.digest()));      //hash & reduce & produce
    }

    @Override
    //hashes iteratively
    public DataArray iterativeHash(DataArray msg, int additionalIterations) {
        reset();
        digest.update(msg.asByteArray());
        byte[] res = reduce(digest.digest());              //get first Hash
        for (int i = 0; i < additionalIterations;i++){
            reset();
            digest.update(res);                            //insert previous hash
            res = reduce(digest.digest());                 //get next hash
        }
        return  backingFactory.create(res);                //create result
    }

    @Override
    //concat and hash
    public DataArray combineHashs(DataArray a, DataArray b) {
        reset();                                                //reset
        digest.update(a.asByteArray());                         //fill1
        digest.update(b.asByteArray());                         //fill2
        return backingFactory.create(reduce(digest.digest()));  //hash & reduce & produce
    }


    @Override
    public void update(byte[] data) {
        digest.update(data);
    }

    @Override
    //reads a hole file
    public void update(RandomAccessFile data) throws IOException {
        data.seek(0);                               //reset file
        byte[] bufB = new byte[16 * 1024];          //prepare buffer TODO: Read from config (allow NIO)
        int num = data.read(bufB);                  //read first


        while(num > 0){                             //read was success?

            digest.update(bufB, 0, num);            //fill
            num = data.read(bufB);                  //read next
        }
    }

    @Override
    public DataArray finalStep() {
        byte[] res = reduce(digest.digest());      //produce result
        reset();                                   //reset
        return backingFactory.create(res);         //create
    }

    private void reset(){
        digest.reset();
        if(salt != null)digest.update(salt);       //fill salt after reset when salt was avaiable
    }

    @Override
    public String toString() {
        return "MessageDigestWrapper{" +
                "digest=" + digest +
                '}';
    }
}
