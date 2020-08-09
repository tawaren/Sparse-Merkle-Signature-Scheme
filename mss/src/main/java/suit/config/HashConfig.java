package suit.config;

import suit.algorithms.hash.blake.blake2b.Blake2b;
import suit.algorithms.hash.blake.blake2s.Blake2s;
import suit.algorithms.interfaces.IFullHashFunction;
import suit.tools.DataHolder;
import suit.tools.MessageDigestHash;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

public class HashConfig {

    String algorithm;
    String provider;
    Byte outLen;

    public static HashConfig Blake2b(){return lookup("Blake2b");}
    public static HashConfig Blake2b(byte outLen){return lookup("Blake2b",outLen);}
    public static HashConfig Blake2s(){return lookup("Blake2s");}
    public static HashConfig Blake2s(byte outLen){return lookup("Blake2s",outLen);}
    public static HashConfig Blake512(){return lookup("Blake512");}
    public static HashConfig Blake512(byte outLen){return lookup("Blake512",outLen);}

    public static HashConfig lookup(String algorithm){
        return new HashConfig(algorithm,null,null);
    }

    public static HashConfig lookup(String algorithm, byte outLen){
        return new HashConfig(algorithm,null,outLen);
    }

    public static HashConfig lookup(String algorithm, String provider){
        return new HashConfig(algorithm,provider,null);
    }

    public static HashConfig lookup(String algorithm, String provider, byte outLen){
        return new HashConfig(algorithm,provider,outLen);
    }

    public HashConfig(String algorithm, String provider, Byte outLen) {
        this.algorithm = algorithm;
        this.provider = provider;
        this.outLen = outLen;
    }

    static HashConfig defaultConfig(){return Blake2b();}

    IFullHashFunction genHashFunction(DataHolder salt) throws NoSuchProviderException, NoSuchAlgorithmException {
        switch (algorithm){
            case "Blake2b":
                if(outLen ==  null){
                    if(salt == null){
                        return new Blake2b();
                    }else {
                        return new Blake2b(salt);
                    }
                } else {
                    if(salt == null){
                        return new Blake2b(outLen);
                    }else {
                        return new Blake2b(outLen, salt);
                    }
                }
            case "Blake2s":
                if(outLen ==  null){
                    if(salt == null){
                        return new Blake2s();
                    }else {
                        return new Blake2s(salt);
                    }
                } else {
                    if(salt == null){
                        return new Blake2s(outLen);
                    }else {
                        return new Blake2s(outLen, salt);
                    }
                }
            default:
                MessageDigest digest;
                if(provider == null){
                    digest = MessageDigest.getInstance(algorithm);
                } else {
                    digest =  MessageDigest.getInstance(algorithm,provider);
                }
                if(outLen ==  null){
                    if(salt == null){
                        return new MessageDigestHash(digest);
                    }else {
                        return new MessageDigestHash(digest,salt);
                    }
                } else {
                    if(salt == null){
                        return new MessageDigestHash(digest,outLen);
                    }else {
                        return new MessageDigestHash(digest,salt,outLen);
                    }
                }

        }
    }
}

