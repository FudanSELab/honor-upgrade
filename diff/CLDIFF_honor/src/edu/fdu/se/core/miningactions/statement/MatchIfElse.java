package edu.fdu.se.core.miningactions.statement;

import com.github.gumtreediff.actions.model.Action;
import com.github.gumtreediff.tree.Tree;

import edu.fdu.se.core.miningactions.bean.Match;
import edu.fdu.se.core.miningactions.bean.MatchStatement;
import edu.fdu.se.core.miningactions.bean.MiningActionData;
import edu.fdu.se.core.miningactions.util.*;
import edu.fdu.se.core.miningchangeentity.base.ChangeEntity;
import edu.fdu.se.core.miningchangeentity.base.ChangeEntityDesc;
import edu.fdu.se.core.miningchangeentity.statement.IfChangeEntity;
import edu.fdu.se.lang.common.ClassLookupTbl;
import edu.fdu.se.lang.common.MethodLookupTbl;
import org.eclipse.jdt.core.dom.ASTNode;

@ClassLookupTbl(key = ChangeEntityDesc.StageIIENTITY.ENTITY_IF_STMT)
public class MatchIfElse extends MatchStatement {

	public IfChangeEntity newEntity() {
		return new IfChangeEntity(mBean);
	}

    @MethodLookupTbl(key = ASTNode.IF_STATEMENT)
	public void matchIfTopDown(MiningActionData fp, Action a) {
		init();
		traverseTopDown(fp, a, TraverseWays.TopDown.IF, 1);
		newBean(a);
		IfChangeEntity code = newEntity();
		setTopDown(code, a, fp, 0);
	}

    //    @MethodLookupTbl(key = ASTNode.IF_STATEMENT)
	public void matchElseTopDown(MiningActionData fp, Action a) {

		init();
		traverseTopDown(fp, a, TraverseWays.TopDown.TYPE_I, 0);
		newBean(a);
		IfChangeEntity code = newEntity();
		setTopDown(code, a, fp, 0);
		code.stageIIBean.setSubEntity(ChangeEntityDesc.StageIISub.SUB_ELSE);
	}


    @MethodLookupTbl(key = ASTNode.IF_STATEMENT)
    public void matchIfPredicateBottomUpNew(MiningActionData fp, Action a, Tree queryFather, int treeType, Tree traverseFather) {

        init();
        traverseBottomUp(fp, a, traverseFather, TraverseWays.BottomUp.IF_PREDICATE, false);
        newBean(a, queryFather, treeType);
        IfChangeEntity code = newEntity();
		setBottomUpNew(code, a, fp, 0);
		code.stageIIBean.setSubEntity(ChangeEntityDesc.StageIISub.SUB_CONDITION);
    }

    @MethodLookupTbl(key = ASTNode.IF_STATEMENT)
    public void matchIfPredicateBottomUpCurr(MiningActionData fp, Action a, ChangeEntity changeEntity, Tree traverseFather) {
        initFromCurrEntity(changeEntity);
        traverseBottomUpCurr(fp, a, traverseFather, TraverseWays.BottomUp.IF_PREDICATE, false);
    }

}
