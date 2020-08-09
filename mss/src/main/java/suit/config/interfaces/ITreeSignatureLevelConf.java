package suit.config.interfaces;


//Probably make also a TreeConf with fixed TreeSk, one OneTimeSignatureGenerator & one HashFun & height


public interface ITreeSignatureLevelConf {   //one instance per level, probably a sub conf shared over all levels possible?
     //the tree index is used to generate a new TreeSk  from the level SK
    //public IStepwiseTreeTraverser createTreeLevelImplementation(DataArray treeIndex);
   // public IStepwiseTreeRootCalc createTreeRootCalcImplementation(DataArray treeIndex);
    public byte getHeight();

}
