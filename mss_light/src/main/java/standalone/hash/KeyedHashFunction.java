package standalone.hash;

//representation Normalized HashIterface
public interface KeyedHashFunction {

    //The length in Bytes of the Output
    int getOutByteLen();

    //creates a hash with a Key as additional input
    public byte[] mac(byte[] Key, byte[] data);

}
