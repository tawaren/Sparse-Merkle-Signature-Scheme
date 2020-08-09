package suit.tools.arrays.factory;

import suit.tools.DataHolder;
import suit.tools.arrays.DataArray;

/**
 * Factory to create DataArrays
 * This allows to ensure a Monomorphic DataArray callsite and thus eliminating the overhead
 * Monoprhism only ensured if all implementations use same hash
 */
public interface IDataArrayFactory {
    //create a 0 with bits equal to primitive size
    public DataArray create();
    //create a value with bits equal to primitive size
    public DataArray create(byte value);           //makes a cast up if necessary
    //create a value with bits equal to n*primitive size (n is smallest n possible)
    public DataArray create(short value);          //makes a cast up if necessary
    //create a value with bits equal to n*primitive size (n is smallest n possible)
    public DataArray create(int value);            //makes a cast up if necessary
    //create a value with bits equal to n*primitive size (n is smallest n possible)
    public DataArray create(long value);           //makes a cast up if necessary
    //create a value (value must be n*primitive size in size)
    public DataArray create(byte[] value);         //must be alligned
    //create a value (value must be n*primitive size in size)
    public DataArray create(short[] value);        //must be alligned
    //create a value (value must be n*primitive size in size)
    public DataArray create(int[] value);          //must be alligned
    //create a value (value must be n*primitive size in size)
    public DataArray create(long[] value);         //must be alligned
    //create a value (value must be n*primitive size in size)
    public DataArray create(DataHolder bs);        //must be alligned
    //concatenates multiple DataArrays
    public DataArray concat(DataArray... arrs);
    //extends an array by filling it with 0s
    public DataArray extend(DataArray in,int len);

}
