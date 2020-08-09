package suit.tools;


public class MathHelper {
    //just a discrete 2er logarithm which uses real log and rounds down (not a real discrete log (this would be a hard problem))
    public static byte log2RoundDown(long num){
        //this is as simple as find the most significant bit that is set, the bitindex is the logarithm
        //this is the case because of a number is represented as b0 * base^0 + b1*2^1 + ... + bn*2^n, so we just have to find biggest n where bn != 0 , if base is 2, this is log2 rounds down
        assert(num > 0);
        byte log = -1;
        do {
            log++;
            num = num >>> 1;
        } while (num != 0);
        return log;
    }
}
