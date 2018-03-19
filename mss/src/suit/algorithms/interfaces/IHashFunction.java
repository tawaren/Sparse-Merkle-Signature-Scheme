package suit.algorithms.interfaces;

import suit.tools.arrays.DataArray;
import suit.tools.arrays.factory.IDataArrayFactory;

/**
 * Hash function Interface
 */
public interface IHashFunction {

    //creates a MessageHasher for non DataArray inputs
    public IMessageHasher createMessageHasher();

    //The length in Bytes of the Output
    public int getOutByteLen();

    //The Factory used to create the DataArrays
    public IDataArrayFactory getBackingFactory();

    //Hashes some input
    public DataArray hash(DataArray data);

    //applies hash function iteration times on msg
    public DataArray iterativeHash(DataArray msg, int iterations);

    //hash(a||b)
    //specially handled, because internal state is double outlen when max security is chosen (in BLAKE, probably in some others to)
    public DataArray combineHashs(DataArray a, DataArray b);

}
