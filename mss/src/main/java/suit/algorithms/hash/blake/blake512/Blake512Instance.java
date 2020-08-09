package suit.algorithms.hash.blake.blake512;



import suit.algorithms.hash.blake.BlakeLongCommon;

import java.util.Arrays;

//transfer padding here later
final class Blake512Instance {

    private static long rot(final long w, final int c){
        return ( (w >>> c)  | (w << ( 64 - c )) );
    }

    private final static long[] u512 =
    {
            0x243f6a8885a308d3L, 0x13198a2e03707344L,
            0xa4093822299f31d0L, 0x082efa98ec4e6c89L,
            0x452821e638d01377L, 0xbe5466cf34e90c6cL,
            0xc0ac29b7c97c50ddL, 0x3f84d5b5b5470917L,
            0x9216d5d98979fb1bL, 0xd1310ba698dfb5acL,
            0x2ffd72dbd01adfb7L, 0xb8e1afed6a267e96L,
            0xba7c9045f12c7f99L, 0x24a19947b3916cf7L,
            0x0801f2e2858efc16L, 0x636920d871574e69L
    };

    private final static byte[][] sigma =
    {
            { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15 },
            {14, 10, 4, 8, 9, 15, 13, 6, 1, 12, 0, 2, 11, 7, 5, 3 },
            {11, 8, 12, 0, 5, 2, 15, 13, 10, 14, 3, 6, 7, 1, 9, 4 },
            { 7, 9, 3, 1, 13, 12, 11, 14, 2, 6, 5, 10, 4, 0, 15, 8 },
            { 9, 0, 5, 7, 2, 4, 10, 15, 14, 1, 11, 12, 6, 8, 3, 13 },
            { 2, 12, 6, 10, 0, 11, 8, 3, 4, 13, 7, 5, 15, 14, 1, 9 },
            {12, 5, 1, 15, 14, 13, 4, 10, 0, 7, 6, 3, 9, 2, 8, 11 },
            {13, 11, 7, 14, 12, 1, 3, 9, 5, 0, 15, 4, 8, 6, 2, 10 },
            { 6, 15, 14, 9, 11, 3, 0, 8, 12, 2, 13, 7, 1, 4, 10, 5 },
            {10, 2, 8, 4, 7, 6, 1, 5, 15, 11, 9, 14, 3, 12, 13 , 0 },
            { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15 },
            {14, 10, 4, 8, 9, 15, 13, 6, 1, 12, 0, 2, 11, 7, 5, 3 },
            {11, 8, 12, 0, 5, 2, 15, 13, 10, 14, 3, 6, 7, 1, 9, 4 },
            { 7, 9, 3, 1, 13, 12, 11, 14, 2, 6, 5, 10, 4, 0, 15, 8 },
            { 9, 0, 5, 7, 2, 4, 10, 15, 14, 1, 11, 12, 6, 8, 3, 13 },
            { 2, 12, 6, 10, 0, 11, 8, 3, 4, 13, 7, 5, 15, 14, 1, 9 }
    };

    long[] s = new long[4];
    long[] t = new long[2];
    long[] v = new long[16];
    final long[] m = new long[16];
    private long[] h;

    int nullt = 0;

    //should be 128 or once bevor finish < 8
    final void countUp(final int n){
        t[0] += n << 3;
        if ( t[0] == 0 && n != 0) t[1]++;
    }

    public Blake512Instance(long[] salt){
        initH(salt);
    }

    final void initH(long[] salt){
        h = new long[]{
                0x6a09e667f3bcc908L,
                0xbb67ae8584caa73bL,
                0x3c6ef372fe94f82bL,
                0xa54ff53a5f1d36f1L,
                0x510e527fade682d1L,
                0x9b05688c2b3e6c1fL,
                0x1f83d9abfb41bd6bL,
                0x5be0cd19137e2179L
        };

        if(salt != null){
            if(salt.length != 4) throw new IllegalArgumentException("Wrong Salt Size"); //Make custom error later
            for(int i = 0; i < s.length && i < salt.length;i++)s[i] = salt[i];
        }
    }

    //does the complex padding, full longs have to be filled to m previously, if their remaining unaligned bytes they are passed in remaining as byte[8], remLength is the index into remaining
    final void prepareFinalPart(int bufIndex, byte[] remaining, int remLength){
        if(remLength > 0)Arrays.fill(remaining, remLength, 8, (byte) 0);
        int byteIndex = bufIndex << 3;
        int longIndex = bufIndex >>> 3;
        long zo = 0x01L;
        byte oo = -0x02; //is this right for 0x81

        long lo = t[0] ;
        long hi = t[1];

        // support for hashing more than 2^32 bits
        if ( lo < byteIndex ) hi++;

        if ( bufIndex == 111 )   // one padding byte
        {
            remaining[7] = oo;
            m[++byteIndex] = BlakeLongCommon.getLongBig(remaining, 0);
        }
        else
        {
            if ( bufIndex < 111 )  //enough space to fill the block
            {

                if ( bufIndex == 0 ) nullt = 1;
                if(remLength != 0){
                  remaining[remLength] = -0x80;
                  m[longIndex++] = BlakeLongCommon.getLongBig(remaining,0);
                } else{
                  m[longIndex++] = (-0x80 & 0xFFL)  << 56;
                }
                Arrays.fill(m, longIndex, 13, 0L);

            }
            else   // need 2 compressions
            {
                //one fits always, because else compress would be called before
                if(remLength != 0){
                    remaining[remLength] = -0x80;
                    m[longIndex++] = BlakeLongCommon.getLongBig(remaining,0);
                } else{
                    m[longIndex++] = (-0x80 & 0xFFL) << 56;
                }

                Arrays.fill(m,longIndex,16,0L);
                compress();
                Arrays.fill(m,0,longIndex,0L);
                nullt = 1;
            }
            m[13] = zo;

        }

        m[14] = hi;
        m[15] = lo;
    }

    final long[] getHash(){
        return h;
    }

    final void compress(){

        System.arraycopy(h, 0, v, 0, 8);

        v[ 8] = s[0] ^ u512[0];
        v[ 9] = s[1] ^ u512[1];
        v[10] = s[2] ^ u512[2];
        v[11] = s[3] ^ u512[3];

        System.arraycopy(u512, 4, v, 12, 4);

        if (nullt == 0)
        {
            v[12] ^= t[0];
            v[13] ^= t[0];
            v[14] ^= t[1];
            v[15] ^= t[1];
        }


        //workhorse
        //do use sigma.length instead of 8 to negate bounding checks (undo for first performance measurement)
        for(int round = 0; round < sigma.length; round++ )
        {

            // column singleStep
            g( 0, 4, 8, 12, 0, round);
            g( 1, 5, 9, 13, 2, round);
            g( 2, 6, 10, 14, 4, round);
            g( 3, 7, 11, 15, 6, round);

            // diagonal singleStep
            g( 0, 5, 10, 15, 8, round);
            g( 1, 6, 11, 12, 10, round);
            g( 2, 7, 8, 13, 12, round);
            g( 3, 4, 9, 14, 14, round);

        }

        for(int i = 0; i < v.length; i++ )  h[(i & 7)] ^= v[i];
        for(int i = 0; i < h.length ; i++ )  h[i] ^= s[(i & 3)];

    }

    private void g(int a,int b,int c,int d,int e, int round) {
        byte sig_round_e = sigma[round][e];
        byte sig_round_e1 = sigma[round][e+1];

        v[a] += (m[sig_round_e] ^ u512[sig_round_e1]) + v[b];
        v[d] = rot( v[d] ^ v[a],32);
        v[c] += v[d];
        v[b] = rot( v[b] ^ v[c],25);
        v[a] += (m[sig_round_e1] ^ u512[sig_round_e])+v[b];
        v[d] = rot( v[d] ^ v[a],16);
        v[c] += v[d];
        v[b] = rot( v[b] ^ v[c],11);
    }

}
