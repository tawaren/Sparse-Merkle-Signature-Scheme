package suit.interfaces;

import suit.tools.arrays.DataArray;

/**
 * Interface for Authentificaton Entries
 */
public interface IAuthEntry{
   public DataArray getHash();          //the hash value of the entry
   public boolean isLeftSilbling();     //is this the left or right child
}
