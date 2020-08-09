package suit.config.interfaces;

import suit.algorithms.interfaces.IMerkleTree;
import suit.algorithms.interfaces.ITreeRootCalc;
import suit.tools.arrays.DataArray;


//Probably make also a TreeConf with fixed TreeSk, one OneTimeSignatureGenerator & one HashFun & height


public interface ITreeLevelConf {   //one instance per level, probably a sub conf shared over all levels possible?
     //the tree index is used to generate a new TreeSk  from the level SK
    public IMerkleTree createTreeLevelImplementation(DataArray treeIndex);
    public ITreeRootCalc createTreeRootCalcImplementation(DataArray treeIndex);
    public byte getHeight();

}
