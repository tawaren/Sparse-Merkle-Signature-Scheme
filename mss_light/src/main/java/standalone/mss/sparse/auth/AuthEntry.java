package standalone.mss.sparse.auth;

import java.util.Arrays;

/**
 * Entry of an Authentification path: node value and left or right node info
 */
public final class AuthEntry {
    //value
    private final byte[] hash;
    //left node?
    private final boolean leftSilbling;

    public AuthEntry(byte[] hash, boolean leftSilbling) {
        this.hash = hash;
        this.leftSilbling = leftSilbling;
    }

    public byte[] getHash() {
        return hash;
    }

    public boolean isLeftSilbling() {
        return leftSilbling;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AuthEntry authEntry = (AuthEntry) o;

        return leftSilbling == authEntry.leftSilbling && Arrays.equals(hash, authEntry.hash);

    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(hash);
        result = 31 * result + (leftSilbling ? 1 : 0);
        return result;
    }
}
