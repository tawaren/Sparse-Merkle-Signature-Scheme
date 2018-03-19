package suit.algorithms.hash.blake.blake2s;

/**
 * Implementation of the Blake2s algorithm (the Private Part)
 */
final class Blake2sInstance {

    //definition of a int rotation
    private static int rot(final int w, final int c){
        return ( (w >>> c)  | (w << ( 32 - c )) );
    }

    //initial Vector
    private final static int[] u256 =
    {
            0x6A09E667, 0xBB67AE85,
            0x3C6EF372, 0xA54FF53A,
            0x510E527F, 0x9B05688C,
            0x1F83D9AB, 0x5BE0CD19
    };

    //Permutation table
    private final static byte[][] sigma =
    {
            {  0,  1,  2,  3,  4,  5,  6,  7,  8,  9, 10, 11, 12, 13, 14, 15 } ,
            { 14, 10,  4,  8,  9, 15, 13,  6,  1, 12,  0,  2, 11,  7,  5,  3 } ,
            { 11,  8, 12,  0,  5,  2, 15, 13, 10, 14,  3,  6,  7,  1,  9,  4 } ,
            {  7,  9,  3,  1, 13, 12, 11, 14,  2,  6,  5, 10,  4,  0, 15,  8 } ,
            {  9,  0,  5,  7,  2,  4, 10, 15, 14,  1, 11, 12,  6,  8,  3, 13 } ,
            {  2, 12,  6, 10,  0, 11,  8,  3,  4, 13,  7,  5, 15, 14,  1,  9 } ,
            { 12,  5,  1, 15, 14, 13,  4, 10,  0,  7,  6,  3,  9,  2,  8, 11 } ,
            { 13, 11,  7, 14, 12,  1,  3,  9,  5,  0, 15,  4,  8,  6,  2, 10 } ,
            {  6, 15, 14,  9, 11,  3,  0,  8, 12,  2, 13,  7,  1,  4, 10,  5 } ,
            { 10,  2,  8,  4,  7,  6,  1,  5, 15, 11,  9, 14,  3, 12, 13 , 0 } ,
    };

    //internal state for multi compresses (changes between compresses, so the two compresses differ)
    private final int[] tf = new int[4];

    //internal working array
    private final int[] v = new int[16];

    //message input array
    final int[] m = new int[16];

    //intermediate state
    private final int[] h = new int[u256.length];

    //Constructor
    public Blake2sInstance(byte outLen, int[] salt){
        initH(outLen,salt);
    }

    //initialize instance

    final void initH(byte outLen, int[] salt){
        assert(outLen <= 32);
        for(int i = 0; i < h.length && i < u256.length;i++) h[i] = u256[i]; //put initial vector in state
        long param0 =  outLen;      //outlen param
        param0 |=  1 << 16;         //fanout param (fixed for this impl -- blake2b would have a hashtrie version how differs here)
        param0 |=  1 << 24;         //depth param (fixed for this impl -- blake2b would have a hashtrie version how differs here)
        h[0] ^= param0;             //integrate config in state
        if(salt != null){
            if(salt.length != 2) throw new IllegalArgumentException("Wrong Salt Size"); //Make custom error later
            h[3] ^= salt[0];  //integrate salt
            h[4] ^= salt[1];  //integrate salt
        }
    }

    //should be 64 if not last
    final void countUp(final int n){
        tf[0] += n;
        if ( tf[0] == 0 && n != 0) tf[1]++;
    }

    //set flag, that this is last compress
    final void flagFinal(){
        tf[2] = ~0;
    }

    //extract the result
    final int[] getHash(){
        return h;
    }

    //The Workhorse
    final void compress(){
        //copy intermediate to internal state
        for(int i = 0; i < 8 && i < v.length && i < h.length ;i++) v[i] = h[i];
        //fill remaining part of internal with initial vector (the first 4 only)
        for(int i = 8; i < 12 && i < u256.length+8 && i < v.length ;i++) v[i] = u256[i-8];

        //fill remaining part of internal with initial vector (the next 4)
        //integrate multi rounds state
        v[12] = tf[0] ^ u256[4];
        v[13] = tf[1] ^ u256[5];
        v[14] = tf[2] ^ u256[6];
        v[15] = tf[3] ^ u256[7];

        //workhorse
        //do use sigma.length instead of 8 to negate bounding checks (undo for first performance measurement)
        for(int round = 0; round < sigma.length; round++ )
        {

                // column singleStep
            g(round, 0, 0, 4, 8, 12);
            g(round, 1, 1, 5, 9, 13);
            g(round, 2, 2, 6, 10, 14);
            g(round, 3, 3, 7, 11, 15);

                 //diagonal singleStep
            g(round, 4, 0, 5, 10, 15);
            g(round, 5, 1, 6, 11, 12);
            g(round, 6, 2, 7, 8, 13);
            g(round, 7, 3, 4, 9, 14);

        }

        //do use h.length instead of 8 to negate bounding checks
        for(int i = 0; i < h.length; i++ )  h[i] ^=  v[i] ^ v[i + 8];

    }

    //The g function (the importest part of the hole algorithm)
    private void g(final int r, final int i, final int a, final int b, final int c, final int d) {
        int p = i << 1;
        v[a] += v[b] + m[sigma[r][p]];
        v[d] = rot(v[d] ^ v[a], 16);
        v[c] += v[d];
        v[b] = rot(v[b] ^ v[c], 12);
        v[a] += v[b] + m[sigma[r][p+1]];
        v[d] = rot(v[d] ^ v[a], 8);
        v[c] += v[d];
        v[b] = rot(v[b] ^ v[c], 7);
    }
}
