package suit.algorithms.generate.tree;


import suit.algorithms.generate.tree.interfaces.IFullCalcController;
import suit.algorithms.generate.tree.interfaces.INodeCalcController;
import suit.algorithms.interfaces.ICryptPrng;
import suit.tools.arrays.DataArray;

public class EmptyTraverser implements IFullCalcController {

    INodeCalcController inner;

    public EmptyTraverser(INodeCalcController inner) {
        this.inner = inner;
    }

    @Override
    public DataArray calcLeave(ICryptPrng localPrng) {
        return inner.calcLeave(localPrng);
    }



    @Override
    public DataArray calcInnerNode(DataArray left, DataArray right) {
        return inner.calcInnerNode(left, right);
    }

    @Override
    public void provide(DataArray value, int level, long levelIndex) { }
}
