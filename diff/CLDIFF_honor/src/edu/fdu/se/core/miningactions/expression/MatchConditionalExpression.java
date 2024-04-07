package edu.fdu.se.core.miningactions.expression;

import com.github.gumtreediff.actions.model.Action;
import com.github.gumtreediff.tree.Tree;
import edu.fdu.se.core.miningactions.bean.MatchExpression;
import edu.fdu.se.core.miningactions.bean.MiningActionData;
import edu.fdu.se.core.miningactions.util.TraverseWays;
import edu.fdu.se.core.miningchangeentity.base.ChangeEntity;
import edu.fdu.se.core.miningchangeentity.base.ChangeEntityDesc;
import edu.fdu.se.core.miningchangeentity.expression.ConditionalExpressionChangeEntity;
import edu.fdu.se.lang.common.ClassLookupTbl;
import edu.fdu.se.lang.common.MethodLookupTbl;
import org.eclipse.jdt.core.dom.ASTNode;


@ClassLookupTbl(key = ChangeEntityDesc.StageIIENTITY.ENTITY_CONDITIONALEXPRESSION_EXP)
public class MatchConditionalExpression extends MatchExpression {

    public ConditionalExpressionChangeEntity newEntity() {
        return new ConditionalExpressionChangeEntity(mBean);
    }

    @MethodLookupTbl(key = ASTNode.CONDITIONAL_EXPRESSION)
    public void matchConditionalExpressionTopDown(MiningActionData fp, Action a) {
        init();
        traverseTopDown(fp, a, TraverseWays.TopDown.TYPE_I, 0);
        newBean(a);
        ConditionalExpressionChangeEntity code = newEntity();
        setTopDown(code, a, fp, 0);
    }


    @MethodLookupTbl(key = ASTNode.CONDITIONAL_EXPRESSION)
    public void matchConditionalExpressionBottomUpNew(MiningActionData fp, Action a, Tree queryFather, int treeType, Tree traverseFather) {
        init();
        traverseBottomUp(fp, a, traverseFather, TraverseWays.BottomUp.FATHER_NODE_GET_SAME_NODE_ACTIONS, false);
        newBean(a, queryFather, treeType);
        ConditionalExpressionChangeEntity code = newEntity();
        setBottomUpNew(code, a, fp, -1);
    }


    @MethodLookupTbl(key = ASTNode.CONDITIONAL_EXPRESSION)
    public void matchConditionalExpressionBottomUpCurr(MiningActionData fp, Action a, ChangeEntity changeEntity, Tree traverseFather) {
        initFromCurrEntity(changeEntity);
        traverseBottomUpCurr(fp, a, traverseFather, TraverseWays.BottomUp.FATHER_NODE_GET_SAME_NODE_ACTIONS, false);
    }
}