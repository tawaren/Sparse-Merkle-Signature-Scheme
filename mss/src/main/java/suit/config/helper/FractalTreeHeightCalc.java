package suit.config.helper;


import suit.tools.MathHelper;

public class FractalTreeHeightCalc {


    public static byte[] assignHeights(int levels, int totalHeight){
        return createApproxLevelArray(totalHeight/levels,totalHeight);
    }

    public static byte[] calcOptHight(int totalHeight){
        int optSubTHight = MathHelper.log2RoundDown(totalHeight);
        return createApproxLevelArray(optSubTHight,totalHeight);
    }

    private static byte[] createApproxLevelArray(int levelHeight, int totalHeight) {
        if(levelHeight == 0) levelHeight =1;
        int subTrees =  totalHeight / levelHeight;
        byte[] res = new byte[subTrees];
        //basic init
        for(int i = 0; i < subTrees;i++)res[i] =  (byte)(levelHeight);

        //distribute rest over levels low/heigh/low/.....
        int upperHighTrees = totalHeight % levelHeight;
        int i = 0;
        while (upperHighTrees > 1){
            res[i]++;
            res[subTrees-i-1]++;
            upperHighTrees =- 2;
            i++;
        }
        if(upperHighTrees > 0)res[i]++;
        return res;
    }
}
