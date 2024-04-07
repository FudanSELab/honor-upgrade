package edu.fdu.se.core.miningactions.statement;

import com.github.gumtreediff.actions.model.Action;
import com.github.gumtreediff.tree.Tree;
import edu.fdu.se.core.miningactions.bean.Match;
import edu.fdu.se.core.miningactions.bean.MatchStatement;
import edu.fdu.se.core.miningactions.bean.MiningActionData;
import edu.fdu.se.core.miningactions.util.*;
import edu.fdu.se.core.miningchangeentity.base.ChangeEntity;
import edu.fdu.se.core.miningchangeentity.base.ChangeEntityDesc;
import edu.fdu.se.core.miningchangeentity.statement.LabeledStatementChangeEntity;
import edu.fdu.se.lang.common.ClassLookupTbl;
import edu.fdu.se.lang.common.MethodLookupTbl;
import org.eclipse.jdt.core.dom.ASTNode;

/**
 * Created by huangkaifeng on 2018/4/4.
 * LabeledStatement
 */
@ClassLookupTbl(key = ChangeEntityDesc.StageIIENTITY.ENTITY_LABELED_STATEMENT)
public class MatchLabeled extends MatchStatement {

    public LabeledStatementChangeEntity newEntity() {
        return new LabeledStatementChangeEntity(mBean);
    }


    @MethodLookupTbl(key = ASTNode.LABELED_STATEMENT)
    public void matchLabeledTopDown(MiningActionData fp, Action a) {
        init();
        traverseTopDown(fp, a, TraverseWays.TopDown.TYPE_I, 0);
        newBean(a);
        LabeledStatementChangeEntity code = newEntity();
        setTopDown(code, a, fp, 0);
    }


    @MethodLookupTbl(key = ASTNode.LABELED_STATEMENT)
    public void matchLabeledBottomUpNew(MiningActionData fp, Action a, Tree queryFather, int treeType, Tree traverseFather) {

        init();
        traverseBottomUp(fp, a, traverseFather, TraverseWays.BottomUp.FATHER_NODE_GET_SAME_NODE_ACTIONS, false);
        newBean(a, queryFather, treeType);
        LabeledStatementChangeEntity code = newEntity();
        setBottomUpNew(code, a, fp, 0);
    }


    @MethodLookupTbl(key = ASTNode.LABELED_STATEMENT)
    public void matchLabeledBottomUpCurr(MiningActionData fp, Action a, ChangeEntity changeEntity, Tree traverseFather) {
        initFromCurrEntity(changeEntity);
        traverseBottomUpCurr(fp, a, traverseFather, TraverseWays.BottomUp.FATHER_NODE_GET_SAME_NODE_ACTIONS, false);

    }


}
