package suit.tools;

/**
 * Helper to encapsel Data if DataArray implementation (factory) is not known
 * This is done to not destroy monomorphic call site
 */
public class DataHolder {
    private byte[] data;

    public DataHolder(byte[] data) {
        this.data = data;
    }

    public DataHolder(byte data) {
        this.data = new byte[]{data};
    }

    public byte[] getData() {
        return data;
    }
}
