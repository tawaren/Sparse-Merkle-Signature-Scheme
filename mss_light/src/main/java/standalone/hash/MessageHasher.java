package standalone.hash;

import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Interface for Hashing large inputs
 */
public interface MessageHasher {
    //adds bytes to the hash
    void update(byte[] data);
    //adds a hole file to the hash
    void update(RandomAccessFile data) throws IOException;
    //calc the result from the input
    byte[] finalStep ();
}
