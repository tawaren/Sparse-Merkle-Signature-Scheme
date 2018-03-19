package suit.algorithms.generate.tree.single;

import suit.interfaces.IAuthEntry;
import suit.interfaces.IAuthPath;

import java.util.Arrays;
import java.util.Iterator;

/**
 * Simple Authentification Path implementation
 */
public final class SimpleAuthPath implements IAuthPath {

    //The Authentification Path
    IAuthEntry[] auths;

    public SimpleAuthPath(IAuthEntry[] auths) {
        //copy the stuff
        this.auths = new IAuthEntry[auths.length];
        for(int i = 0; i < auths.length && i < this.auths.length;i++)  this.auths[i] = auths[i];
    }

    @Override
    //Iterator to iterate over entries
    public Iterator<IAuthEntry> iterator() {
        return new LogSpaceTimeAuthPathIterator();
    }

    //Iterator impl
    private class LogSpaceTimeAuthPathIterator implements Iterator<IAuthEntry>{
        int index = 0;                          //next index

        @Override
        public boolean hasNext() {
            return (index < auths.length);     //is end of array?
        }

        @Override
        public IAuthEntry next() {
            return auths[index++];             //fetch and increase index
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof IAuthPath)) return false;

        IAuthPath that = (IAuthPath) o;
        Iterator<IAuthEntry> it1 = iterator();
        Iterator<IAuthEntry> it2 = that.iterator();
        while (it1.hasNext() && it2.hasNext()){
            if(!it1.next().equals(it2.next())) return false;
        }

        return it1.hasNext() == it2.hasNext();

    }

    @Override
    public String toString() {
        return "LogSpaceTimeAuthPath{" +
                "auths=" + Arrays.toString(auths) +
                '}';
    }
}