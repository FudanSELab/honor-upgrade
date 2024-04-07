package edu.fdu.se.core.miningactions.bean;

/**
 * Created by huangkaifeng on 2018/3/12.
 *
 */
public class MyRange {

    /**
     * type insert/delete
     * ChangeEntityDesc.StageITreeType.PREV_TREE_NODE
     */
    public int type;

    public int startLineNo;
    public int endLineNo;

    public MyRange(int start,int end,int type){
        this.startLineNo = start;
        this.endLineNo = end;
        this.type = type;
    }

    /**
     * reverse
     *
     * @param rangeString
     */
    public MyRange(String rangeString) {
        if (rangeString.startsWith("(") && rangeString.endsWith(")")) {
            String sub = rangeString.substring(1, rangeString.length() - 1);
            String[] data = sub.split(",");
            this.startLineNo = Integer.valueOf(data[0]);
            this.endLineNo = Integer.valueOf(data[1]);
        }
    }

    @Override
    public String toString(){
        return "("+this.startLineNo+","+this.endLineNo+")";
    }


    public int isRangeWithin(MyRange myRange){
        if(this.startLineNo<=myRange.startLineNo
                && this.endLineNo >= myRange.endLineNo){
            return -1;
        }else if(myRange.startLineNo <= this.startLineNo
                && this.endLineNo <=myRange.endLineNo){
            return 1;
        }else{
            return 0;
        }
    }
}
