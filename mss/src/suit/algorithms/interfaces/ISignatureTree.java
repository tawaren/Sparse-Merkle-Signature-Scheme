package suit.algorithms.interfaces;

import suit.interfaces.ITreeSig;
import suit.tools.DataHolder;
import suit.tools.arrays.DataArray;

/**
 * Specific Merkle Tree interface for creating signatures (instead of generic Leaf calc function)
 */
public interface ISignatureTree extends ITree{
    //creates next sig for a DataHolder message
     public ITreeSig createSignature(DataHolder message);
    //creates next sig for a DataArray message
    public ITreeSig createSignature(DataArray message);
}
