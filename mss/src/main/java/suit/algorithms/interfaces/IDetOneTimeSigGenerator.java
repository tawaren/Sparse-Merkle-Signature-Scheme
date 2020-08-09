package suit.algorithms.interfaces;

import suit.interfaces.ILeaveAuth;
import suit.interfaces.ITreeSig;
import suit.tools.arrays.DataArray;
import suit.tools.arrays.factory.IDataArrayFactory;


/**
 *  One Time Signature Generator algorithm
 *  (For verification their is another interface)
 */
public interface IDetOneTimeSigGenerator extends ILeafCalc {
    //calculates the public key (the commitment for leaveSk)
    public DataArray calcCommitmentLeave(DataArray leaveSk);

    //Calculates a Signature for an Authentification Path (inkl SK)
    public ITreeSig calcSignature(ILeaveAuth auth, DataArray msg);

    //Calculates a Signature from an SK | needed for standalone tests
    public DataArray[] calcSignature(DataArray Sk, DataArray msg);

    //get the factory used for the creation of the backing DataArrays
    public IDataArrayFactory getBackingFactory();


}
