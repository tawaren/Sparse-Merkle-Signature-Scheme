package standalone.hash;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.security.MessageDigest;

/**
 *  Wrapper to use Java Cryptographics with this Framework
 */
public class MessageDigestWrapper implements HashFunction, MessageHasher {

    MessageDigest digest;                       //the Java crypto Digest
    byte[] salt;                                //the salt
    Byte outLen;                                //the hash len

    public MessageDigestWrapper(MessageDigest digest) {
        this(digest, null, null);
    }

    public MessageDigestWrapper(MessageDigest digest , byte[] salt) {
        this(digest, salt, null);
    }

    public MessageDigestWrapper(MessageDigest digest, byte outLen) {
        this(digest, null, outLen);
    }

    public MessageDigestWrapper(MessageDigest digest ,byte[] salt, Byte outLen) {
        this.digest = digest;
        this.salt = salt;
        this.outLen = outLen;
        assert(outLen == null || outLen <= digest.getDigestLength());
        reset();
    }

    @Override
    public int getOutByteLen() {
        if(outLen != null) return outLen;
        return digest.getDigestLength();        //fallback to primitive len
    }

    //cuts a result down
    private byte[] reduce(byte[] in){
       if(outLen != null){
           byte[] reduced = new byte[(int)outLen];
           for(int i= 0; i < in.length && i < reduced.length; i++) reduced[i] = in[i];
           return reduced;
       }
       return in;           //No reduction necessary
    }

    @Override
    //uses this MessageHasher for standalone hashin
    public byte[] hash(byte[] data) {
        reset();                             //reset
        digest.update(data);                 //fill
        return reduce(digest.digest());      //hash & reduce & produce
    }

    @Override
    //hashes iteratively
    public byte[] iterativeHash(byte[] msg, int iterations) {
        var res = msg;
        for (int i = 0; i < iterations;i++){
            res = hash(res);                            //Hash ones
        }
        return res;                                     //create result
    }

    @Override
    //concat and hash
    public byte[] combineHashes(byte[] a, byte[] b) {
        reset();                                  //reset
        digest.update(a);                         //fill1
        digest.update(b);                         //fill2
        return reduce(digest.digest());           //hash & reduce & produce
    }


    @Override
    public void update(byte[] data) {
        digest.update(data);
    }

    @Override
    //reads a hole file
    public void update(RandomAccessFile data) throws IOException {
        data.seek(0);                           //reset file
        byte[] bufB = new byte[16 * 1024];          //prepare buffer TODO: Read from config (allow NIO)
        int num = data.read(bufB);                  //read first
        while(num > 0){                             //read was success?
            digest.update(bufB, 0, num);      //fill
            num = data.read(bufB);                  //read next
        }
    }

    @Override
    public byte[] finalStep() {
        byte[] res = reduce(digest.digest());      //produce result
        reset();                                   //reset
        return res;                                //create
    }

    private void reset(){
        digest.reset();
        if(salt != null){
            digest.update(salt);       //fill salt after reset when salt was avaiable
        }
    }

}
