package suit.config;

import suit.algorithms.generate.signature.winterlitz.WinterlitzSigGen;
import suit.algorithms.interfaces.ICryptPrng;
import suit.algorithms.interfaces.IFullHashFunction;
import suit.algorithms.prng.RandomAccessHashPRNG;
import suit.config.implementation.TreeAlgorithmConf;
import suit.config.implementation.TreeSignatureAlgorithmConf;
import suit.config.interfaces.ILeafCalcConf;
import suit.config.interfaces.ITreeAlgorithmConf;
import suit.config.interfaces.ITreeSignatureAlgorithmConf;
import suit.tools.DataHolder;

import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;

public class AlgorithmConfig {
    private HashConfig hc;
    private DataHolder salt;
    private DataHolder sK;

    private int h;

    public static AlgorithmConfig create(HashConfig hc, DataHolder salt, int h){return new AlgorithmConfig(hc, salt,h,null);}
    public static AlgorithmConfig create(HashConfig hc, int h){return new AlgorithmConfig(hc,null,h,null);}
    public static AlgorithmConfig create(DataHolder salt,int h){return new AlgorithmConfig(HashConfig.defaultConfig(),salt,h,null);}
    public static AlgorithmConfig create(int h){return new AlgorithmConfig(HashConfig.defaultConfig(),null,h,null);}

    public static AlgorithmConfig create(HashConfig hc, DataHolder salt, int h,  DataHolder sK){return new AlgorithmConfig(hc, salt,h,sK);}
    public static AlgorithmConfig create(HashConfig hc, int h,  DataHolder sK){return new AlgorithmConfig(hc,null,h,sK);}
    public static AlgorithmConfig create(DataHolder salt,int h,  DataHolder sK){return new AlgorithmConfig(HashConfig.defaultConfig(),salt,h,sK);}
    public static AlgorithmConfig create(int h,  DataHolder sK){return new AlgorithmConfig(HashConfig.defaultConfig(),null,h,sK);}


    public AlgorithmConfig(HashConfig hc,DataHolder salt, int h, DataHolder sK) {
        this.hc = hc;
        this.salt = salt;
        this.h = h;
        this.sK = sK;
    }


    public ICryptPrng getPrng() throws NoSuchProviderException, NoSuchAlgorithmException {
        IFullHashFunction hash = hc.genHashFunction(salt);
        if(sK == null)sK = generateRandomSk();
        return new RandomAccessHashPRNG(hash, hash.getBackingFactory().create(sK));
    }



    private DataHolder generateRandomSk(){
        SecureRandom random = new SecureRandom();
        byte[] bsk = new byte[hc.outLen];
        random.nextBytes(bsk);
        return new DataHolder(bsk);
    }

    int getH() {
        return h;
    }

    private IFullHashFunction hCache;

    IFullHashFunction getHashFunction() throws NoSuchProviderException, NoSuchAlgorithmException {
        if(hCache == null)hCache = hc.genHashFunction(salt);
        return hCache;
    }

    ITreeAlgorithmConf genConf(ILeafCalcConf calc) throws NoSuchProviderException, NoSuchAlgorithmException {
        IFullHashFunction hash =  getHashFunction();
        return new TreeAlgorithmConf(hash, calc.genCalc(hash));
    }

    ITreeSignatureAlgorithmConf genSignatureConf(int w) throws NoSuchProviderException, NoSuchAlgorithmException {
        IFullHashFunction hash =  getHashFunction();
        return new TreeSignatureAlgorithmConf(hash, new WinterlitzSigGen((byte)w,hash));
    }
}

