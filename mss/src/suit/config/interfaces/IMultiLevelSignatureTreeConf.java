package suit.config.interfaces;


import suit.algorithms.interfaces.IDetOneTimeSigGenerator;

public interface IMultiLevelSignatureTreeConf extends IMultiLevelTreeConf{
    public IDetOneTimeSigGenerator getSignatureGen();

}
