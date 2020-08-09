package suit.algorithms.interfaces;

import suit.tools.arrays.DataArray;


/**
 *  One Time Signature Generator algorithm
 *  (For generation their is another interface)
 */
public interface IDetOneTimeSigVerifier {
    //verifies a signature by restoring the commitment (or null if signature can be determined invalid independent of commitment)
    //commitment is then verified over merkle tree
    public DataArray verifySignature(DataArray msg, DataArray[] sig);
}
