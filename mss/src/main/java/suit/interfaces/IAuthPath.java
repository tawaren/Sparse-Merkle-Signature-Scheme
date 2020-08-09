package suit.interfaces;


import suit.algorithms.interfaces.IHashFunction;
import suit.tools.arrays.DataArray;

/**
 * Interface for an Authentication Path
 * (just alias fir Iterable of IAuthEntries)
 */
public interface IAuthPath extends Iterable<IAuthEntry>{

    default DataArray computeRoot(IHashFunction h, DataArray leaf){
        var cur = leaf;
        var iter = iterator();
        while (iter.hasNext()) {
            var sibling = iter.next();
            if(sibling.isLeftSilbling()) {
                cur = h.combineHashs(sibling.getHash(), cur);
            } else {
                cur = h.combineHashs(cur,sibling.getHash());
            }
        }
        return cur;
    }

}
