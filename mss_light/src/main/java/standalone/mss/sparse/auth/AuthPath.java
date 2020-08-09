package standalone.mss.sparse.auth;

import standalone.hash.HashFunction;

import java.util.Iterator;

/**
 * Authentication Path implementation
 */
public final class AuthPath {

    //The Authentification Path
    AuthEntry[] auths;

    public AuthPath(AuthEntry[] auths) {
        //copy the stuff
        this.auths = new AuthEntry[auths.length];
        for(int i = 0; i < auths.length && i < this.auths.length;i++)  this.auths[i] = auths[i];
    }

    //Iterator to iterate over entries
    public Iterator<AuthEntry> iterator() {
        return new LogSpaceTimeAuthPathIterator();
    }

    //Iterator impl
    private class LogSpaceTimeAuthPathIterator implements Iterator<AuthEntry>{
        int index = 0;                          //next index

        @Override
        public boolean hasNext() {
            return (index < auths.length);     //is end of array?
        }

        @Override
        public AuthEntry next() {
            return auths[index++];             //fetch and increase index
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    public byte[] computeRoot(HashFunction h, byte[] leaf){
        var cur = leaf;
        var iter = iterator();
        while (iter.hasNext()) {
            var sibling = iter.next();
            if(sibling.isLeftSilbling()) {
                cur = h.combineHashes(sibling.getHash(), cur);
            } else {
                cur = h.combineHashes(cur,sibling.getHash());
            }
        }
        return cur;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AuthPath)) return false;

        AuthPath that = (AuthPath) o;
        Iterator<AuthEntry> it1 = iterator();
        Iterator<AuthEntry> it2 = that.iterator();
        while (it1.hasNext() && it2.hasNext()){
            if(!it1.next().equals(it2.next())) return false;
        }

        return it1.hasNext() == it2.hasNext();

    }
}