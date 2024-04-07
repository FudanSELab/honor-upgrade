package edu.fdu.se.core.miningactions.statement;

import com.github.gumtreediff.actions.model.Action;

import com.github.gumtreediff.tree.Tree;
import edu.fdu.se.core.miningactions.bean.Match;
import edu.fdu.se.core.miningactions.bean.MatchStatement;
import edu.fdu.se.core.miningactions.bean.MiningActionData;
import edu.fdu.se.core.miningactions.util.*;
import edu.fdu.se.core.miningchangeentity.base.ChangeEntity;
import edu.fdu.se.core.miningchangeentity.base.ChangeEntityDesc;
import edu.fdu.se.core.miningchangeentity.statement.SwitchChangeEntity;
import edu.fdu.se.lang.common.ClassLookupTbl;
import edu.fdu.se.lang.common.MethodLookupTbl;
import org.eclipse.jdt.core.dom.ASTNode;

@ClassLookupTbl(key = ChangeEntityDesc.StageIIENTITY.ENTITY_SWITCH_STMT)
public class MatchSwitch extends MatchStatement {

	public SwitchChangeEntity newEntity() {
		return new SwitchChangeEntity(mBean);
	}


    @MethodLookupTbl(key = ASTNode.SWITCH_STATEMENT)
    public void matchSwitchTopDown(MiningActionData fp, Action a) {
        init();
        traverseTopDown(fp, a, TraverseWays.TopDown.SWITCH, 1);
        newBean(a);
        SwitchChangeEntity code = newEntity();
        setTopDown(code, a, fp, 0);
    }

    @MethodLookupTbl(key = ASTNode.SWITCH_CASE)
    public void matchSwitchCaseTopDown(MiningActionData fp, Action a) {

        init();
        traverseTopDown(fp, a, TraverseWays.TopDown.SWITCH_CASE, 0);
        newBean(a);
        SwitchChangeEntity code = newEntity();
        setTopDown(code, a, fp, 0);
        if (a.getNode().getChildren() == null || a.getNode().getChildren().size() == 0) {
            code.stageIIBean.setSubEntity(ChangeEntityDesc.StageIISub.SUB_SWITCH_CASE_DEFAULT);
        } else {
            code.stageIIBean.setSubEntity(ChangeEntityDesc.StageIISub.SUB_SWITCH_CASE);
        }

    }

    @MethodLookupTbl(key = ASTNode.SWITCH_STATEMENT)
    public void matchSwitchBottomUpNew(MiningActionData fp, Action a, Tree queryFather, int treeType, Tree traverseFather) {
        init();
        traverseBottomUp(fp, a, traverseFather, TraverseWays.BottomUp.SWITCH_CONDITION, false);
        newBean(a, queryFather, treeType);
        SwitchChangeEntity code = newEntity();
        setBottomUpNew(code, a, fp, 0);
        code.stageIIBean.setSubEntity(ChangeEntityDesc.StageIISub.SUB_CONDITION);
    }


    @MethodLookupTbl(key = ASTNode.SWITCH_STATEMENT)
    public void matchSwitchBottomUpCurr(MiningActionData fp, Action a, ChangeEntity changeEntity, Tree traverseFather) {
        initFromCurrEntity(changeEntity);
        traverseBottomUpCurr(fp, a, traverseFather, TraverseWays.BottomUp.SWITCH_CONDITION, false);
    }


    @MethodLookupTbl(key = ASTNode.SWITCH_CASE)
    public void matchSwitchCaseBottomUpNew(MiningActionData fp, Action a, Tree queryFather, int treeType, Tree traverseFather) {
        init();
        traverseBottomUp(fp, a, traverseFather, TraverseWays.BottomUp.FATHER_NODE_GET_SAME_NODE_ACTIONS, false);
        newBean(a, queryFather, treeType);
        SwitchChangeEntity code = newEntity();
        setBottomUpNew(code, a, fp, 0);
        code.stageIIBean.setSubEntity(ChangeEntityDesc.StageIISub.SUB_SWITCH_CASE);

    }

    @MethodLookupTbl(key = ASTNode.SWITCH_CASE)
    public void matchSwitchCaseBottomUpCurr(MiningActionData fp, Action a, ChangeEntity changeEntity, Tree traverseFather) {
        initFromCurrEntity(changeEntity);
        traverseBottomUpCurr(fp, a, traverseFather, TraverseWays.BottomUp.FATHER_NODE_GET_SAME_NODE_ACTIONS, false);
    }
}
