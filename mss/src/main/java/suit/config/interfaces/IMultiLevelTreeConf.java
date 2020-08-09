package suit.config.interfaces;


import suit.tools.arrays.factory.IDataArrayFactory;

public interface IMultiLevelTreeConf {
    public int getNumLevels();
    public int getTotalHeight();
    public ITreeSignatureLevelConf getSignatureTreeLevelConf(int levelIndex);
    public ITreeLevelConf getBaseTreeLevelConf();
    public IDataArrayFactory getDataFactory();

}
