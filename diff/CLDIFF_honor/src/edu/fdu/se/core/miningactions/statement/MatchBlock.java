package edu.fdu.se.core.miningactions.statement;

import com.github.gumtreediff.actions.model.Action;
import com.github.gumtreediff.actions.model.Move;
import com.github.gumtreediff.tree.Tree;
import edu.fdu.se.core.miningactions.bean.Match;
import edu.fdu.se.global.Global;
import edu.fdu.se.core.miningactions.bean.ChangePacket;
import edu.fdu.se.core.miningactions.bean.MiningActionData;
import edu.fdu.se.core.miningactions.util.AstRelations;
import edu.fdu.se.core.miningchangeentity.generator.ClusteredActionBean;
import edu.fdu.se.core.miningchangeentity.base.ChangeEntity;
import edu.fdu.se.core.miningchangeentity.base.ChangeEntityDesc;
import edu.fdu.se.core.miningchangeentity.base.StatementPlusChangeEntity;

import java.util.ArrayList;
import java.util.List;

public class MatchBlock extends Match {

    public static void matchBlock(MiningActionData fp, Action a) {
        Tree fatherNode = (Tree) a.getNode().getParent();
        int type = Global.astNodeUtil.getNodeTypeId(fatherNode.getNode());
        if (a instanceof Move && !Global.astNodeUtil.isIf(fatherNode.getNode())) {
            handleMoveOnBlock(fp, a);
            fp.setActionTraversedMap(a);
            return;
        }
        Global.processUtil.matchBlock(fp, a, type, fatherNode);
    }

    public static void handleMoveOnBlock(MiningActionData fp, Action a) {
        Global.fileOutputLog.writeErrFile("Move on block@handleMoveOnBlock");
        Tree movedBlock = (Tree) a.getNode();
        ChangePacket changePacket = new ChangePacket();
        changePacket.initChangeSet1();
        List<Action> subActions = new ArrayList<>();
        subActions.add(a);
        ClusteredActionBean clusteredActionBean = new ClusteredActionBean(ChangeEntityDesc.StageITraverseType.TRAVERSE_TOP_DOWN, a, subActions, changePacket, movedBlock, ChangeEntityDesc.TreeType.PREV_TREE_NODE);
        ChangeEntity changeEntity = new StatementPlusChangeEntity(clusteredActionBean);
        changeEntity.stageIIBean.setEntityCreationStage(ChangeEntityDesc.StageIIGenStage.ENTITY_GENERATION_STAGE_GT_TD);
        changeEntity.stageIIBean.setGranularity(ChangeEntityDesc.StageIIGranularity.GRANULARITY_STATEMENT);
        changeEntity.stageIIBean.setChangeEntity(movedBlock.getNode().getClass().getSimpleName());
        changeEntity.stageIIBean.setOpt(ChangeEntityDesc.StageIIOpt.OPT_MOVE);
        changeEntity.stageIIBean.setLineRange(Global.astNodeUtil.getRange(movedBlock,movedBlock.getTreePrevOrCurr()).toString());
        fp.addOneChangeEntity(changeEntity);
    }

}
