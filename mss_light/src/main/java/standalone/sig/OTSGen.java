package standalone.sig;


import standalone.mss.sparse.auth.LeaveAuth;
import standalone.mss.sparse.auth.TreeSig;

/**
 *  One Time Signature Generator algorithm
 *  (For verification their is another interface)
 */
public interface OTSGen {
    //calculates the public key (the commitment for leaveSk)
    byte[] calcCommitmentLeave(byte[] leaveSk);

    //Calculates a Signature for an Authentification Path (inkl SK)
    TreeSig calcSignature(LeaveAuth auth, byte[] msg);

    //Calculates a Signature from an SK | needed for standalone tests
    byte[][] calcSignature(byte[] Sk, byte[] msg);

}
