package suit.algorithms.hash.blake;

import suit.tools.arrays.DataArray;

/**
 * Configuration to define how a Message hasher works
 */
public class BlakeMessageHasherConfig {
    //pure Performance parameters

    public enum Mode{Standard,MemMapped}
    private Mode mode;          //Nio or not Nio
    private int memBlockSize;   //only needed for MemMapped
    private int bufferSize;

    //Security parameters
    private byte outLen;        //may also have impact on preformance
    private DataArray salt;

    public BlakeMessageHasherConfig(byte outLen, Mode mode, int bufferSize, int memBlockSize, DataArray salt) {
        this.bufferSize = bufferSize;
        this.memBlockSize = memBlockSize;
        this.outLen = outLen;
        this.mode = mode;
        this.salt = salt;
    }

    public BlakeMessageHasherConfig(byte outLen, int bufferSize) {
        this(outLen, Mode.MemMapped,bufferSize,0, null);
    }

    //Some standard configs
    public static BlakeMessageHasherConfig standardConfig64 = new BlakeMessageHasherConfig((byte)64, Mode.Standard, 16 * 1024, 0, null);
    public static BlakeMessageHasherConfig standardConfig32 = new BlakeMessageHasherConfig((byte)32, Mode.Standard, 16 * 1024, 0, null);


    //getters to read the config
    public DataArray getSalt() {
        return salt;
    }

    public byte getOutLen() {
        return outLen;
    }

    public int getBufferSize() {
        return bufferSize;
    }

    public int getMemBlockSize() {
        return memBlockSize;
    }

    public Mode getMode() {
        return mode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BlakeMessageHasherConfig that = (BlakeMessageHasherConfig) o;

        if (bufferSize != that.bufferSize) return false;
        if (memBlockSize != that.memBlockSize) return false;
        if (outLen != that.outLen) return false;
        if (mode != that.mode) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = bufferSize;
        result = 31 * result + (int) outLen;
        result = 31 * result + memBlockSize;
        result = 31 * result + (mode != null ? mode.hashCode() : 0);
        return result;
    }
}
