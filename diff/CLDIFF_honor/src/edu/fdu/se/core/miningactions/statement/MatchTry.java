package edu.fdu.se.core.miningactions.statement;

import com.github.gumtreediff.actions.model.Action;

import com.github.gumtreediff.tree.Tree;
import edu.fdu.se.core.miningactions.bean.Match;
import edu.fdu.se.core.miningactions.bean.MatchStatement;
import edu.fdu.se.core.miningactions.bean.MiningActionData;
import edu.fdu.se.core.miningactions.util.*;
import edu.fdu.se.core.miningchangeentity.base.ChangeEntity;
import edu.fdu.se.core.miningchangeentity.base.ChangeEntityDesc;
import edu.fdu.se.core.miningchangeentity.statement.TryCatchChangeEntity;
import edu.fdu.se.lang.common.ClassLookupTbl;
import edu.fdu.se.lang.common.MethodLookupTbl;
import org.eclipse.jdt.core.dom.ASTNode;

@ClassLookupTbl(key = ChangeEntityDesc.StageIIENTITY.ENTITY_TRY_STMT + ChangeEntityDesc.SPLITTER + ChangeEntityDesc.StageIIENTITY.ENTITY_THROW_STMT)
public class MatchTry extends MatchStatement {

	public TryCatchChangeEntity newEntity() {
		return new TryCatchChangeEntity(mBean);
	}

	@MethodLookupTbl(key = ASTNode.TRY_STATEMENT)
	public void matchTryTopDown(MiningActionData fp, Action a) {
//		code.stageIIBean.setSubEntity(ChangeEntityDesc.StageIISub.SUB_BODY_AND_CATCH_CLAUSE);
// todo 和finally 做区别
		init();
		traverseTopDown(fp, a, TraverseWays.TopDown.TRY, 1);
		newBean(a);
		TryCatchChangeEntity code = newEntity();
		setTopDown(code, a, fp, 0);
		code.stageIIBean.setSubEntity(ChangeEntityDesc.StageIISub.SUB_BODY_AND_CATCH_CLAUSE);

	}

	@MethodLookupTbl(key = ASTNode.TRY_STATEMENT)
	public void matchTryBottomUpNew(MiningActionData fp, Action a, Tree queryFather, int treeType, Tree traverseFather) {
		init();
		traverseBottomUp(fp, a, traverseFather, TraverseWays.BottomUp.IF_PREDICATE, true);
		newBean(a, queryFather, treeType);
		TryCatchChangeEntity code = newEntity();
		setBottomUpNew(code, a, fp, 0);
		code.stageIIBean.setSubEntity(ChangeEntityDesc.StageIISub.SUB_TRY_WITH);

	}



	@MethodLookupTbl(key = ASTNode.TRY_STATEMENT)
	public void matchTryBottomUpCurr(MiningActionData fp, Action a, ChangeEntity changeEntity, Tree traverseFather) {
		initFromCurrEntity(changeEntity);
		traverseBottomUpCurr(fp, a, traverseFather, TraverseWays.BottomUp.IF_PREDICATE, false);
	}




	@MethodLookupTbl(key = ASTNode.CATCH_CLAUSE)
	public void matchCatchClauseTopDown(MiningActionData fp, Action a) {
		init();
		traverseTopDown(fp, a, TraverseWays.TopDown.IF, 1);
		newBean(a);
		TryCatchChangeEntity code = newEntity();
		setTopDown(code, a, fp, 0);
		code.stageIIBean.setSubEntity(ChangeEntityDesc.StageIISub.SUB_CATCH_CLAUSE);
		code.stageIIBean.setOpt(ChangeEntityDesc.StageIIOpt.OPT_CHANGE);
	}


	public void matchFinallyTopDown(MiningActionData fp, Action a) {
		init();
		traverseTopDown(fp, a, TraverseWays.TopDown.TYPE_I, 0);
		newBean(a);
		TryCatchChangeEntity code = newEntity();
		setTopDown(code, a, fp, 0);
		code.stageIIBean.setSubEntity(ChangeEntityDesc.StageIISub.SUB_FINALLY);
		code.stageIIBean.setOpt(ChangeEntityDesc.StageIIOpt.OPT_CHANGE);
	}

	@MethodLookupTbl(key = ASTNode.CATCH_CLAUSE)
	public void matchCatchBottonUpNew(MiningActionData fp, Action a, Tree queryFather, int treeType, Tree traverseFather) {
		init();
		traverseBottomUp(fp, a, traverseFather, TraverseWays.BottomUp.IF_PREDICATE, false);
		newBean(a, queryFather, treeType);
		TryCatchChangeEntity code = newEntity();
		setBottomUpNew(code, a, fp, 0);
		code.stageIIBean.setSubEntity(ChangeEntityDesc.StageIISub.SUB_CATCH_CLAUSE);
		code.stageIIBean.setOpt(ChangeEntityDesc.StageIIOpt.OPT_CHANGE);

	}


	@MethodLookupTbl(key = ASTNode.CATCH_CLAUSE)
	public void matchCatchBottomUpCurr(MiningActionData fp, Action a, ChangeEntity changeEntity, Tree traverseFather) {
		initFromCurrEntity(changeEntity);
		traverseBottomUpCurr(fp, a, traverseFather, TraverseWays.BottomUp.IF_PREDICATE, false);
	}


	@MethodLookupTbl(key = ASTNode.THROW_STATEMENT)
	public void matchThrowTopDown(MiningActionData fp, Action a) {
		init();
		traverseTopDown(fp, a, TraverseWays.TopDown.TYPE_I, 0);
		newBean(a);
		TryCatchChangeEntity code = newEntity();
		setTopDown(code, a, fp, 1);
		String optName = changeEntityDescString(a);
		code.stageIIBean.setOpt(optName);
	}



	@MethodLookupTbl(key = ASTNode.THROW_STATEMENT)
	public void matchThrowBottomUpNew(MiningActionData fp, Action a, Tree queryFather, int treeType, Tree traverseFather) {
		init();
		traverseBottomUp(fp, a, traverseFather, TraverseWays.BottomUp.FATHER_NODE_GET_SAME_NODE_ACTIONS, false);
		newBean(a, queryFather, treeType);
		TryCatchChangeEntity code = newEntity();
		setBottomUpNew(code, a, fp, 1);
		code.stageIIBean.setOpt(ChangeEntityDesc.StageIIOpt.OPT_CHANGE);
	}


	@MethodLookupTbl(key = ASTNode.THROW_STATEMENT)
	public void matchThrowBottomUpCurr(MiningActionData fp, Action a, ChangeEntity changeEntity, Tree traverseFather) {
		initFromCurrEntity(changeEntity);
		traverseBottomUpCurr(fp, a, traverseFather, TraverseWays.BottomUp.FATHER_NODE_GET_SAME_NODE_ACTIONS, false);
	}


}
