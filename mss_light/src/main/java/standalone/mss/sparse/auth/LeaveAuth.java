package standalone.mss.sparse.auth;

import java.util.Arrays;

/**
 * Token and Authentication
 */
public final class LeaveAuth {
    //The AuthPath
    private final AuthPath authPath;
    //The Token
    private final byte[] leaveSk;

    public LeaveAuth(AuthPath authPath, byte[]  leaveSk) {
        this.authPath = authPath;
        this.leaveSk = leaveSk;
    }

    public AuthPath getAuthPath() {
        return authPath;
    }

    public byte[]  getLeaveSk() {
        return leaveSk;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LeaveAuth that = (LeaveAuth) o;

        return authPath.equals(that.authPath) && Arrays.equals(leaveSk, that.leaveSk);

    }

    @Override
    public int hashCode() {
        int result = authPath.hashCode();
        result = 31 * result + Arrays.hashCode(leaveSk);
        return result;
    }
}