package suit.algorithms.interfaces;

import suit.interfaces.ILeaveAuth;

/**
 * Basic Merkle Tree interface for generating AuthPaths
 */
public interface IMerkleTree extends ITree{
    //Claculates the next Auth Path (inkluding SK -- Not suited for publishing (first a Token algorithm or sig algorithm have to be used on SK)
     public ILeaveAuth createNextAuth();
}
