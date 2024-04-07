package edu.fdu.se.core.json;

import com.github.gumtreediff.actions.model.*;
import com.github.gumtreediff.tree.Tree;
import edu.fdu.se.core.miningactions.bean.MiningActionData;
import edu.fdu.se.core.miningchangeentity.generator.ClusteredActionBean;
import edu.fdu.se.core.miningchangeentity.base.ChangeEntity;
import edu.fdu.se.core.miningchangeentity.base.ChangeEntityDesc;
import edu.fdu.se.core.miningchangeentity.base.FrontData;
import edu.fdu.se.core.miningchangeentity.member.EnumChangeEntity;
import edu.fdu.se.global.Global;

import java.util.ArrayList;
import java.util.List;

public class StmtLevelBeanSetter extends BeanSetter {

    /**
     * statement
     * @param mad
     */
    public void setChangeEntitySubRange(MiningActionData mad) {
        //todo 需要审阅
        List<ChangeEntity> mList = mad.getChangeEntityList();
        for (ChangeEntity tmp : mList) {
            if (tmp.stageIIBean.getOpt().equals(ChangeEntityDesc.StageIIOpt.OPT_CHANGE)) {
                // 设置sub
                if (tmp instanceof EnumChangeEntity) {
                    //TODO
                } else {
                    setStageIIIBeanSubRangeDetail(tmp.frontData, tmp.clusteredActionBean.actions, mad);
                }
            } else if (tmp.stageIIBean.getOpt().equals(ChangeEntityDesc.StageIIOpt.OPT_CHANGE_MOVE)) {
                // 设置move

                setStageIIIBeanSubRangeDetailMove(tmp.frontData, tmp.clusteredActionBean, mad);
            }
        }
    }

    public static void setStageIIIBeanSubRangeDetailMove(FrontData frontData, ClusteredActionBean bean, MiningActionData mad) {
        Action a = bean.curAction;
        if (!(a instanceof Move)) {
            return;
        }
        Object src = mad.preCacheData.getPrevCu();
        Object dst = mad.preCacheData.getCurrCu();
        Move mv = (Move) a;
        Tree moveNode = (Tree) mv.getNode();
        Tree movedDstNode = (Tree) mad.getMappedCurrOfPrevNode(moveNode);
        frontData.setRange(moveNode.getRangeString() + "-" + movedDstNode.getRangeString());
        Integer[] m = {moveNode.getPos(), moveNode.getPos() + moveNode.getLength()};
        Integer[] n = {movedDstNode.getPos(), movedDstNode.getPos() + movedDstNode.getLength()};
        frontData.addMoveListSrc(m, src);
        frontData.addMoveListDst(n, dst);
    }

    public static void setStageIIIBeanSubRangeDetail(FrontData frontData, List<Action> actions, MiningActionData mad) {
        Object src = Global.astNodeUtil.getPrevCu(mad.preCacheData.getPrevCu());
        Object dst = Global.astNodeUtil.getCurrCu(mad.preCacheData.getCurrCu());
        List<Integer[]> rangeList = new ArrayList<>();
        MergeIntervals mi = new MergeIntervals();
        actions.forEach(a -> {
            if (a instanceof Insert) {
                Tree temp = (Tree) a.getNode();
                Integer[] tempArr = {temp.getPos(), temp.getPos() + temp.getLength()};
                rangeList.add(tempArr);
            }
        });
        List<Integer[]> insertResult = mi.merge(rangeList);
//        int[] insertRange = maxminLineNumber(insertResult, dst);
        if (insertResult != null && insertResult.size() != 0)
            frontData.addInsertList(insertResult, dst);
//        String dstRangeStr = "(" + insertRange[0] + "," + insertRange[1] + ")";
        rangeList.clear();
        actions.forEach(a -> {
            if (a instanceof Delete) {
                Tree temp = (Tree) a.getNode();
                Integer[] tempArr = {temp.getPos(), temp.getPos() + temp.getLength()};
                rangeList.add(tempArr);
            }

        });
        List<Integer[]> deleteResult = mi.merge(rangeList);
        if (deleteResult != null && deleteResult.size() != 0)
            frontData.addDeleteList(deleteResult, src);
//        int[] deleteRange = maxminLineNumber(deleteResult, src);
        rangeList.clear();
        actions.forEach(a -> {
            if (a instanceof Update) {
                Tree temp = (Tree) a.getNode();
                Integer[] tempArr = {temp.getPos(), temp.getPos() + temp.getLength()};
                rangeList.add(tempArr);
            }
        });
        List<Integer[]> updateResult = mi.merge(rangeList);
        if (updateResult != null && updateResult.size() != 0)
            frontData.addUpdateList(updateResult, src);
//        int[] updateRange = maxminLineNumber(updateResult, src);
//        int max, min;
//        if (deleteRange[0] < updateRange[0]) {
//            min = deleteRange[0];
//        } else {
//            min = updateRange[0];
//        }
//        if (deleteRange[1] > updateRange[1]) {
//        if (deleteRange[1] > updateRange[1]) {
//            max = deleteRange[1];
//        } else {
//            max = updateRange[1];
//        }
//        String srcRangeStr = "(" + min + "," + max + ")";
//        stageIIIBean.setRange(srcRangeStr + "-" + dstRangeStr);

    }

    public void setChangeEntityOpt(MiningActionData miningActionData) {
        setChangeEntityOptDetail(miningActionData);
    }

}
