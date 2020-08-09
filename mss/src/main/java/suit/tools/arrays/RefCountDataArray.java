package suit.tools.arrays;


/**
 * RefCount Wrapper for DataArray
 */
public class RefCountDataArray extends DataArray {

    DataArray inner;        //inner
    boolean reached;        //reachable flag

    public RefCountDataArray(DataArray inner) {
        super(inner.getEndianess());
        this.inner = inner;
        reached = false;
    }

    public void prepare(){
        reached = false;
    }

    //counts reachability (only once even if reachable over multiple paths)
    public int count(){
        int res = reached?0:1;
        reached = true;
        return res;
    }

    //static helper to handle null refs
    public static void prepare(DataArray arr){
        if(arr == null) return;
        if(arr instanceof RefCountDataArray) ((RefCountDataArray)arr).prepare();
    }

    //static helper to handle null refs
    public static int count(DataArray arr){
        if(arr == null) return 0;
        if(arr instanceof RefCountDataArray) return ((RefCountDataArray)arr).count();
        return 1;           //fall back for non RefCounted Arrs (Should not happen)
    }

    @Override
    public int getByteSize() {
        return inner.getByteSize();
    }

    @Override
    public DataArray xor(DataArray other) {
        return new RefCountDataArray(inner.xor(other));
    }

    @Override
    public DataArray add(DataArray other) {
        return new RefCountDataArray(inner.add(other));
    }

    @Override
    public void copyTo(byte[] bArray, int start, int length) {
       inner.copyTo(bArray, start,length);
    }

    @Override
    public void copyTo(short[] sArray, int start, int length, Endianess endianess) {
        inner.copyTo(sArray,start,length,endianess);
    }

    @Override
    public void copyTo(int[] iArray, int start, int length, Endianess endianess) {
        inner.copyTo(iArray,start,length,endianess);
    }

    @Override
    public void copyTo(long[] lArray, int start, int length, Endianess endianess) {
        inner.copyTo(lArray,start,length,endianess);
    }

    @Override
    public int extractBits(int bitIndex, byte bits, Endianess endianess) {
        return inner.extractBits(bitIndex,bits,endianess);
    }

    @Override
    protected long getData(int index, boolean swap) {
        return inner.getData(index,swap);
    }

    @Override
    protected long getMask(int bits) {
        return inner.getMask(bits);
    }

    @Override
    public boolean equals(Object o) {
        if(o instanceof RefCountDataArray)return inner.equals(((RefCountDataArray)o).inner);
        else return inner.equals(o);
    }

    @Override
    public int hashCode() {
        return inner.hashCode();
    }
}
