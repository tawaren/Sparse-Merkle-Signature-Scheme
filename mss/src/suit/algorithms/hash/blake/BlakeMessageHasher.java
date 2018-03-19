package suit.algorithms.hash.blake;

import suit.algorithms.interfaces.IMessageHasher;
import suit.tools.arrays.DataArray;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

/**
 * Basic functionality to hash large files or byte stream
 * Or even multiple files
 */
public abstract class BlakeMessageHasher implements IMessageHasher {
    protected int bufIndex = 0;                             //index into Buffer of corresponding Blake implementation (in bytes)
    protected final byte outLen;                            //length of output in Byte
    protected final byte[] notEven = new byte[getAlign()];  //if not a full int or long (depending on actual Blake) can be filled the remainder is stored in this
    private final BlakeMessageHasherConfig conf;            //configuration to use

    //abstract methods
    //outs bytes into the buffer of the Hashing algorithm
    protected abstract void putBytes(final int i, final byte[] data, final int dataIndex);

    //defines the alignement, put bytes will always be called with data.length == n*getAlign()  & n > 0   | getAlign() must be 2^x & x > 0
    protected abstract int getAlign();

    //record the number of bytes submitted for this compress (should always be buffer size except in the last compress)
    protected abstract void countUp(final int n);

    //Mark a compress as the last compress
    public abstract DataArray finalStep();

    //Do a compress (empties the buffer)
    protected abstract void compress();



    //Construct from config
    protected BlakeMessageHasher(BlakeMessageHasherConfig conf) {
        this.conf = conf;
        this.outLen = conf.getOutLen();
    }

    //Construct from outlen
    protected BlakeMessageHasher(byte outLen) {
        this.conf = null;
        this.outLen = outLen;
    }

    //Feeds all bytes from data to the Blake Hash algorithm
    public final void update( final RandomAccessFile data) throws IOException {
        //rewind File
        data.seek(0);
        //Check File read mode
        switch(conf.getMode()){
            case Standard:
                updateFile(data);
                break;
            case MemMapped:
                updateNioMapped(data);
                break;
        }
    }

    //Uses Nio to read file
    private void updateNioMapped( final RandomAccessFile data) throws IOException {
        //Open the channel
        final FileChannel channel = data.getChannel();

        //create the buffer
        final int bufS = conf.getMemBlockSize();
        final byte[] buffer = new byte[conf.getBufferSize()];

        //how much data is their
        final long s = data.length();

        //initialize number of Bytes to read for next File Block
        long pos = 0;
        int size = (int)Math.min(bufS,s - pos);

        //as long as their is more to read
        while(size > 0){
            //Get the Buffer for the next File Block
            final MappedByteBuffer bufB = channel.map(FileChannel.MapMode.READ_ONLY, pos, size);

            //position into block
            int localPos = 0;

            //initialize number of Bytes to read for next Buffer Block
            int localSize = Math.min(buffer.length,size - localPos);

            //As long as their are more Buffer Blocks to read in this File Block
            while(localSize > 0){
                //fill the local buffer
                bufB.get(buffer,0,localSize);

                //update the Blake Buffer with this buffer
                update(buffer,localSize,0);

                //adapt position and size of next Buffer Block
                localPos += localSize;
                localSize = Math.min(buffer.length,size - localPos);
            }

            //adapt position and size of next File Block
            pos += size;
            size = (int)Math.min(bufS,s - pos);
        }
    }

    //Uses classical IO
    private void updateFile( final RandomAccessFile data) throws IOException {
        //Create Buffer
        final byte[] bufB = new byte[conf.getBufferSize()];
        //read from buffer num bytes (max conf.getBufferSize())
        int num = data.read(bufB);

        //as long as more is read  do
        while(num > 0){
            //update the Blake Buffer with this buffer
            update(bufB, num, 0);
            //read more
            num = data.read(bufB);
        }
    }

    //just a bridge function to get shorter signature
    public final void update( final byte[] data){
        update(data,data.length,0);
    }

    //The main update methode which feeds bytes to the Blake Buffer and compress it
    private void update( final byte[] data, int length, int dataIndex)
    {
        final int align = getAlign();       //get Byte Alignement
        final int bLength = align << 4;     //get Buffer length (all blakes have buffers with 16 entries)
        final int mask = align - 1;         //mask for faster modulo align

        //while somethign is left
        while( length > 0 )
        {
            //if buffer is full
            if(bufIndex == bLength){
                countUp(bLength);           //mark full buffer compress
                compress();                 //do compress
                bufIndex = 0;               //reset buffer index
            }

            //if their are at least one buffer entry and the current pointer is aligned
            if(length >= align && (bufIndex & mask) == 0){
                //push into Blake Buffer until buffer full or no more complete entry left
                while(bufIndex != bLength & length >= align){
                    putBytes(bufIndex, data, dataIndex);        //put one entry
                    //advance pointers
                    dataIndex += align;
                    bufIndex += align;
                    //reduce unprocessed length
                    length -= align;
                }
            } else if((bufIndex & mask) == 0){  //if their is less then one entry remaining but the pointer is aligned
                //collect partial entry into notEven
                for(int i = 0; i < length && i < notEven.length && i < data.length-dataIndex; i++) notEven[i] = data[i+dataIndex];
                //advance pointers
                dataIndex += length;
                bufIndex += length;         //Not aligned any more
                //everything consumed
                length = 0;
            } else {                            //pointer not aligned (= we have stuff in notEven)
                int open = bufIndex & mask;    //find remainder to the aligned offset

                //how much can we consume into notEven if l == align-open (we can fill notEven completly)
                int l = Math.min(align-open,length);

                //fill notEven with as much as possible
                for(int i = 0; i < l && i < notEven.length-open && i < data.length-dataIndex; i++) notEven[i+open] = data[i+dataIndex];

                //if we could fill notEven completly, push it into blake Buffer
                if(open+l == align) putBytes(bufIndex, notEven, 0);

                //advance pointers
                dataIndex += l;
                bufIndex += l;                  //may be aligned or not depending on  l == align-open
                //reduce unprocessed lengt
                length -= l;
            }
        }

    }


}
