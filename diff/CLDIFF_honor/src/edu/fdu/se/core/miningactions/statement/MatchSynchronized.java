package edu.fdu.se.core.miningactions.statement;

import com.github.gumtreediff.actions.model.Action;

import com.github.gumtreediff.tree.Tree;
import edu.fdu.se.core.miningactions.bean.Match;
import edu.fdu.se.core.miningactions.bean.MatchStatement;
import edu.fdu.se.core.miningactions.bean.MiningActionData;
import edu.fdu.se.core.miningactions.util.*;
import edu.fdu.se.core.miningchangeentity.base.ChangeEntity;
import edu.fdu.se.core.miningchangeentity.base.ChangeEntityDesc;
import edu.fdu.se.core.miningchangeentity.statement.SynchronizedChangeEntity;
import edu.fdu.se.lang.common.ClassLookupTbl;
import edu.fdu.se.lang.common.MethodLookupTbl;
import org.eclipse.jdt.core.dom.ASTNode;

@ClassLookupTbl(key = ChangeEntityDesc.StageIIENTITY.ENTITY_SYNCHRONIZED_STMT)
public class MatchSynchronized extends MatchStatement {


	public SynchronizedChangeEntity newEntity() {
		return new SynchronizedChangeEntity(mBean);
	}


    @MethodLookupTbl(key = ASTNode.SYNCHRONIZED_STATEMENT)
    public void matchSynchronizedTopDown(MiningActionData fp, Action a) {
        init();
        traverseTopDown(fp, a, TraverseWays.TopDown.IF, 1);
        newBean(a);
        SynchronizedChangeEntity code = newEntity();
        setTopDown(code, a, fp, 0);
    }

    @MethodLookupTbl(key = ASTNode.SYNCHRONIZED_STATEMENT)
    public void matchSynchronizedBottomUpNew(MiningActionData fp, Action a, Tree queryFather, int treeType, Tree traverseFather) {
        init();
        traverseBottomUp(fp, a, traverseFather, TraverseWays.BottomUp.IF_PREDICATE, false);
        newBean(a, queryFather, treeType);
        SynchronizedChangeEntity code = newEntity();
        setBottomUpNew(code, a, fp, 0);
        String optName = getBottomUpOptName(a);
    }

    @MethodLookupTbl(key = ASTNode.SYNCHRONIZED_STATEMENT)
    public void matchSynchronizedBottomUpCurr(MiningActionData fp, Action a, ChangeEntity changeEntity, Tree traverseFather) {
        initFromCurrEntity(changeEntity);
        traverseBottomUpCurr(fp, a, traverseFather, TraverseWays.BottomUp.IF_PREDICATE, false);

    }


}
