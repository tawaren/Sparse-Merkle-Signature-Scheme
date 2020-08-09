package standalone.mss.sparse;



//TreeHash which can only do full step, based on a static height
import standalone.prng.Prng;

/**
 * improved TreeHash algorithm
 * does one leaf calc and as many hash calcs as possible
 */
public class TreeHash {

    //Prng used for Leaf calc
    public Prng cprng;


    //Stack used as Array
    private final byte[][] stack;

    //controller for processing results
    private SparseFractalTree tree;

    //tracker for leaveIndex
    private long index=0;

    //Creates an instance of the algorithm to calc a tree of height h, beginning with the leaf which has sk cprng.current
    public TreeHash(SparseFractalTree tree, Prng cprng, int height) {
        this.stack = new byte[height][];
        this.tree = tree;
        this.cprng = cprng.capture();
    }

    //calcs one leaf and as many inner nodes as possible
    public boolean step(){
        assert(tree!=null);
        int level = -1;                                         //the level of the active node
        long levelIndex = index++;                              //the index of the active node on the level
        byte[] node = tree.calcLeaf(cprng);                    //calc the leaf
        cprng.next();                                           //forward the csprng
        tree.provide(node,++level,levelIndex);                  //process the leaf (done by controller)
        while(level < stack.length && stack[level] != null){    //forall inner nodes calcable (without a new leaf)
             node = tree.calcInnerNode(stack[level], node);     //calc inner node
             stack[level] = null;                               //pop old node
             levelIndex >>>= 1;                                 //calc new level index
             tree.provide(node,++level,levelIndex);             //process the node (done by controller)
        }

        if(level == stack.length){                              //Was it the last node of the tree
            tree = null;                                        //clean up and mark finished
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

    public byte[] getRootHash() {
        assert(tree==null);                                     //assert finished
        return stack[0];                                         //return rootHash (Stored at 0 to spare one elem)
    }


}
