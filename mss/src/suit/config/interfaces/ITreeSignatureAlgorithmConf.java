package suit.config.interfaces;

import suit.algorithms.interfaces.IDetOneTimeSigGenerator;

public interface ITreeSignatureAlgorithmConf extends ITreeAlgorithmConf{
    public IDetOneTimeSigGenerator getOneTimeSignatureGenerator();
}
