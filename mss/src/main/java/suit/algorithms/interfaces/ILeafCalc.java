package suit.algorithms.interfaces;

import suit.tools.arrays.DataArray;


/**
 * Calcs the leaf commitment of the underlying one Time algorithm
 */
public interface ILeafCalc {
    //the commitment leaf for this algorithm for leaveSk
    public DataArray calcCommitmentLeave(DataArray leaveSk);
}
