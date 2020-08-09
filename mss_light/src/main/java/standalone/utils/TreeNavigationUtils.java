package standalone.utils;

public class TreeNavigationUtils {
    public static long leavesInTreeWithHeight(int H) {
        assert (H < 64);
        return 1L << H;
    }

    public static long nodesInTreeWithHeight(int H) {
        return leavesInTreeWithHeight(H)-1;
    }

    public static boolean isLeftNode(long index) {
        return (index & 1) == 0;
    }

    public static boolean isRightNode(long index) {
        return (index & 1) == 1;
    }

    public static long shiftIndexIntoTree(long index, long level, byte treeHeight){
        assert (level <= treeHeight);
        //levels between level and treeHeight
        long numberOfHigherLevels = treeHeight - level;
        //the nodes on that level in each tree
        long numberTreeNodes = 1 << numberOfHigherLevels;
        //to get the local index we cals the index modulo the nodes on the level (because the modulo is 2^x, we can use & 2^x-1 instead)
        return (index & (numberTreeNodes-1));
    }

    public static long ancestorLevelIndex(long index, long levelDiff) {
        //dividing by 2 gives the index of the parent on its level so we do this levelDiff times
        return index >>> levelDiff;
    }

    public static long parentLevelIndex(long index) {
        return ancestorLevelIndex(index, 1);
    }

}
