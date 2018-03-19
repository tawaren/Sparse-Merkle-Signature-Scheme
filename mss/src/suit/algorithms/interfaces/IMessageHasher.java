package suit.algorithms.interfaces;

import suit.tools.arrays.DataArray;

import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Interface for Hashing large inputs
 */
//Todo: allow MAC Mode
public interface IMessageHasher {
    //adds bytes to the hash
    void update(byte[] data);
    //adds a hole file to the hash
    void update(RandomAccessFile data) throws IOException;
    //calc the result from the input
    DataArray finalStep ();
}
