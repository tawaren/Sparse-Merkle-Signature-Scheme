package suit.algorithms.interfaces;

import suit.tools.arrays.DataArray;
import suit.tools.arrays.factory.IDataArrayFactory;

//representation Normalized HashIterface
public interface IKeyedHashFunction {

    //The length in Bytes of the Output
    public int getOutByteLen();

    //The Factory used to create the DataArrays
    public IDataArrayFactory getBackingFactory();

    //creates a hash with a Key as additional input
    public DataArray mac(DataArray Key, DataArray data);

}
