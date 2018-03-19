package suit.algorithms.generate.tree.single;

import suit.interfaces.IAuthPath;
import suit.interfaces.ILeaveAuth;
import suit.tools.arrays.DataArray;

/**
 * Token and Authentification
 */
public final class SimpleLeaveAuth implements ILeaveAuth {
    //The AuthPath
    private final IAuthPath authPath;
    //The Token
    private final DataArray leaveSk;

    public SimpleLeaveAuth(IAuthPath authPath, DataArray leaveSk) {
        this.authPath = authPath;
        this.leaveSk = leaveSk;
    }

    @Override
    public IAuthPath getAuthPath() {
        return authPath;
    }

    @Override
    public DataArray getLeaveSk() {
        return leaveSk;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SimpleLeaveAuth that = (SimpleLeaveAuth) o;

        return authPath.equals(that.authPath) && leaveSk.equals(that.leaveSk);

    }

    @Override
    public int hashCode() {
        int result = authPath.hashCode();
        result = 31 * result + leaveSk.hashCode();
        return result;
    }
}