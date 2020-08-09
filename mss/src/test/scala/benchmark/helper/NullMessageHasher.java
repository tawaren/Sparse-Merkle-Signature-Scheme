package benchmark.helper;


import suit.algorithms.interfaces.IMessageHasher;
import suit.tools.arrays.DataArray;

import java.io.IOException;
import java.io.RandomAccessFile;

public class NullMessageHasher implements IMessageHasher {



    @Override
    public void update(byte[] data) { }

    @Override
    public void update(RandomAccessFile data) throws IOException { }

    @Override
    public DataArray finalStep() {
        return NullDataFactory.res;
    }
}
