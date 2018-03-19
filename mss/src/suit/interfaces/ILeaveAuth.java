package suit.interfaces;

import suit.tools.arrays.DataArray;


/**
 * All the information needed to create the cryptographical primitive (signature & auth or token & auth or ....)
 */
public interface ILeaveAuth {
    public IAuthPath getAuthPath();             //the path to verify a commitment
    public DataArray getLeaveSk();              //the Sk used to calc the commitment
}
