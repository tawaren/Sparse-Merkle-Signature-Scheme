package suit.algorithms.generate.tree.interfaces;

import suit.algorithms.interfaces.ICryptPrng;
import suit.tools.arrays.DataArray;

/**
 * Interface for Controller (provider)
 * Leaf and Node computation founction
 */
public interface INodeCalcController{
    //boolean says if the traversing should be continued, this allows to break early (for example for Fractal Traverser)
    public DataArray calcLeave(ICryptPrng localPrng);
    public DataArray calcInnerNode(DataArray left, DataArray right);
}
