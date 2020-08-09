package suit.algorithms.generate.tree;

import suit.interfaces.IAuthEntry;
import suit.tools.arrays.DataArray;

/**
 * Entry of an Authentification path: node value and left or right node info
 */
public final class AuthEntry implements IAuthEntry {
    //value
    private DataArray hash;
    //left node?
    private boolean leftSilbling;

    public AuthEntry(DataArray hash, boolean leftSilbling) {
        this.hash = hash;
        this.leftSilbling = leftSilbling;
    }

    @Override
    public DataArray getHash() {
        return hash;
    }

    @Override
    public boolean isLeftSilbling() {
        return leftSilbling;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AuthEntry authEntry = (AuthEntry) o;

        return leftSilbling == authEntry.leftSilbling && hash.equals(authEntry.hash);

    }

    @Override
    public int hashCode() {
        int result = hash.hashCode();
        result = 31 * result + (leftSilbling ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "AuthEntry{" +
                "hash=" + hash +
                ", leftSilbling=" + leftSilbling +
                '}';
    }
}
