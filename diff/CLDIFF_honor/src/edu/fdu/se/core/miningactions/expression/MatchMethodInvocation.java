package edu.fdu.se.core.miningactions.expression;

import com.github.gumtreediff.actions.model.Action;
import com.github.gumtreediff.tree.Tree;
import edu.fdu.se.core.miningactions.bean.Match;
import edu.fdu.se.core.miningactions.bean.MatchExpression;
import edu.fdu.se.core.miningactions.bean.MiningActionData;
import edu.fdu.se.core.miningactions.util.AstRelations;
import edu.fdu.se.core.miningactions.util.TraverseWays;
import edu.fdu.se.core.miningchangeentity.base.ChangeEntity;
import edu.fdu.se.core.miningchangeentity.base.ChangeEntityDesc;
import edu.fdu.se.core.miningchangeentity.expression.MethodInvocationChangeEntity;
import edu.fdu.se.lang.common.ClassLookupTbl;
import edu.fdu.se.lang.common.MethodLookupTbl;
import org.eclipse.jdt.core.dom.ASTNode;


@ClassLookupTbl(key = ChangeEntityDesc.StageIIENTITY.ENTITY_METHODINVOCATION_EXP)
public class MatchMethodInvocation extends MatchExpression {

    public MethodInvocationChangeEntity newEntity() {
        return new MethodInvocationChangeEntity(mBean);
    }

    @MethodLookupTbl(key = ASTNode.METHOD_INVOCATION)
    public void matchMethodInvocationTopDown(MiningActionData fp, Action a) {
        init();
        traverseTopDown(fp, a, TraverseWays.TopDown.TYPE_I, 0);
        newBean(a);
        MethodInvocationChangeEntity code = newEntity();
        setTopDown(code, a, fp, 0);
    }


    @MethodLookupTbl(key = ASTNode.METHOD_INVOCATION)
    public void matchMethodInvocationBottomUpNew(MiningActionData fp, Action a, Tree queryFather, int treeType, Tree traverseFather) {
        init();
        traverseBottomUp(fp, a, traverseFather, TraverseWays.BottomUp.FATHER_NODE_GET_SAME_NODE_ACTIONS, false);
        newBean(a, queryFather, treeType);
        MethodInvocationChangeEntity code = newEntity();
        setBottomUpNew(code, a, fp, 0);
    }


    @MethodLookupTbl(key = ASTNode.METHOD_INVOCATION)
    public void matchMethodInvocationBottomUpCurr(MiningActionData fp, Action a, ChangeEntity changeEntity, Tree traverseFather) {
        initFromCurrEntity(changeEntity);
        traverseBottomUpCurr(fp, a, traverseFather, TraverseWays.BottomUp.FATHER_NODE_GET_SAME_NODE_ACTIONS, false);
    }
}