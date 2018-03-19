package suit.interfaces;


import suit.tools.arrays.DataArray;

/**
 * A signature and Authentication
 */
public interface ITreeSig {
    DataArray[] getSig();       //the signature | concrete impl decide how this looks
    IAuthPath getAuthPath();    //the Authentification path
}
