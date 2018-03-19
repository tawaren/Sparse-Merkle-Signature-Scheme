package suit.config.interfaces;


import suit.config.AlgorithmConfig;

import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

public interface ITreeConfig<T,V> {
    public T generateTree(AlgorithmConfig ac, ILeafCalcConf calc) throws NoSuchProviderException, NoSuchAlgorithmException;
    public V generateSignatureTree(AlgorithmConfig ac, int w) throws NoSuchProviderException, NoSuchAlgorithmException;
}
