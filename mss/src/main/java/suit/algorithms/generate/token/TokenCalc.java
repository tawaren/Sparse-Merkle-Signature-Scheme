package suit.algorithms.generate.token;


import suit.algorithms.interfaces.IHashFunction;
import suit.algorithms.interfaces.ILeafCalc;
import suit.tools.arrays.DataArray;

/**
 * Algorithm for a simple authorisation token
 */
public class TokenCalc implements ILeafCalc {

    IHashFunction h;    //hash function to use

    //Basic constructor
    public TokenCalc(IHashFunction h) {
        this.h = h;
    }

    @Override
    public DataArray calcCommitmentLeave(DataArray leaveSk) {
        //just Hash
        return h.hash(leaveSk);
    }

    public DataArray createToken(DataArray token){
        //the signature is the sk itself
        return token;
    }

    public DataArray verifyToken(DataArray token){
        //We can nothing verify here, but result can be authentificated
        //same as pk calc
        return h.hash(token);
    }
}
