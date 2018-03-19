package suit.algorithms.generate.tree.hash;

import suit.algorithms.generate.tree.interfaces.IFullCalcController;
import suit.algorithms.interfaces.ICryptPrng;
import suit.tools.arrays.DataArray;
import suit.tools.arrays.RefCountDataArray;
import test.measure.SpaceMeasureable;

//TreeHash which can only do full step, based on a static height

/**
 * improved TreeHash algorithm
 * does one leaf calc and as many hash calcs as possible
 */
public class TreeHash implements SpaceMeasureable {

    //Prng used for Leaf calc
    public ICryptPrng cprng;

    @Override
    public void unMark() {
        for (DataArray arr: stack) RefCountDataArray.prepare(arr);
    }

    @Override
    public int markAndCount() {
        int sum = 0;
        for (DataArray arr: stack) sum += RefCountDataArray.count(arr);
        return sum;
    }

    //Stack used as Array
    private final DataArray[] stack;

    //controller for processing results
    private IFullCalcController controller;

    //tracker for leaveIndex
    private long index=0;

    //Creates an instance of the algorithm to calc a tree of height h, beginning with the leaf which has sk cprng.current
    public TreeHash(IFullCalcController controller, ICryptPrng cprng, int height) {
        this.stack = new DataArray[height];
        this.controller = controller;
        this.cprng = cprng.capture();
    }

    //calcs one leaf and as many inner nodes as possible
    public boolean step(){
        assert(controller!=null);
        int level = -1;                                         //the level of the active node
        long levelIndex = index++;                              //the index of the active node on the level
        DataArray node = controller.calcLeave(cprng);           //calc the leaf
        cprng.next();                                           //forward the csprng
        controller.provide(node,++level,levelIndex);            //process the leaf (done by controller)
        while(level < stack.length && stack[level] != null){    //forall inner nodes calcable (without a new leaf)
             node = controller.calcInnerNode(stack[level], node); //calc inner node
             stack[level] = null;                               //pop old node
             levelIndex >>>= 1;                                 //calc new level index
             controller.provide(node,++level,levelIndex);       //process the node (done by controller)
        }

        if(level == stack.length){                              //Was it the last node of the tree
            controller = null;                                  //clean up and mark finished
            stack[0] = node;                                    //record root node (0 because stack is empty and it woulkd be a waste to make array one larger just for this)
            return false;                                       //return false (no more nodes to calc)
        }

        stack[level] = node;                                    //if we are not done push the node

        return true;                                            //return true (more stuff to calc)
    }

    //initialize all
    public void init() {
        while (step());
    }

    public DataArray getRootHash() {
        assert(controller==null);                                //assert finished
        return stack[0];                                         //return rootHash (Stored at 0 to spare one elem)
    }


}
