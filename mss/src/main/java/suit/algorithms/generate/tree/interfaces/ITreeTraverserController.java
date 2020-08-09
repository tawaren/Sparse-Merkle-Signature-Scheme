package suit.algorithms.generate.tree.interfaces;

import suit.tools.arrays.DataArray;

/**
 * Interface for Controller (consumer)
 * provide function
 */
public interface ITreeTraverserController{
    //boolean says if the traversing should be continued, this allows to break early (for example for Fractal Traverser)
    public void provide(DataArray value, int level, long levelIndex);
}
