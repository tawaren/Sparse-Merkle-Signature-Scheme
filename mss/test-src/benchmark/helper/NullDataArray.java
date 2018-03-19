package benchmark.helper;


import suit.tools.arrays.DataArray;

public class NullDataArray extends DataArray {

    public NullDataArray() {
        super(Endianess.BIG);
    }

    @Override
    public int getByteSize() {
        return 0;
    }

    @Override
    public DataArray xor(DataArray other) {
        return this;
    }

    @Override
    public DataArray add(DataArray other) {
        return this;
    }

    @Override
    public void copyTo(byte[] bArray, int start, int length) {}

    @Override
    public void copyTo(short[] sArray, int start, int length, Endianess endianess) {}

    @Override
    public void copyTo(int[] iArray, int start, int length, Endianess endianess) {}

    @Override
    public void copyTo(long[] lArray, int start, int length, Endianess endianess) {}

    @Override
    public int extractBits(int bitIndex, byte bits, Endianess endianess) {
        return 0;
    }

    @Override
    protected long getData(int index, boolean swap) {
        return 0;
    }

    @Override
    protected long getMask(int bits) {
        return 0;
    }
}
