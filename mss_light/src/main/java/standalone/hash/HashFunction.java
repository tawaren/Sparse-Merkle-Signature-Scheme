package standalone.hash;

/**
 * Hash function Interface
 */
public interface HashFunction {
    //The length in Bytes of the Output
    int getOutByteLen();

    //Hashes some input
    byte[] hash(byte[] data);

    //applies hash function iteration times on msg
    byte[] iterativeHash(byte[] msg, int iterations);

    //hash(a||b)
    //specially handled, because internal state is double outlen when max security is chosen (in BLAKE, probably in some others to)
    byte[] combineHashes(byte[] a, byte[] b);


}
