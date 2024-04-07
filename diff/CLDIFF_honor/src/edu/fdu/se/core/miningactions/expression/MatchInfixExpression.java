package edu.fdu.se.core.miningactions.expression;


import com.github.gumtreediff.actions.model.Action;
import com.github.gumtreediff.tree.Tree;
import edu.fdu.se.core.miningactions.bean.MatchExpression;
import edu.fdu.se.core.miningactions.bean.MiningActionData;
import edu.fdu.se.core.miningactions.util.TraverseWays;
import edu.fdu.se.core.miningchangeentity.base.ChangeEntity;
import edu.fdu.se.core.miningchangeentity.base.ChangeEntityDesc;
import edu.fdu.se.core.miningchangeentity.expression.InfixExpressionChangeEntity;
import edu.fdu.se.lang.common.ClassLookupTbl;
import edu.fdu.se.lang.common.MethodLookupTbl;
import org.eclipse.jdt.core.dom.ASTNode;


@ClassLookupTbl(key = ChangeEntityDesc.StageIIENTITY.ENTITY_INFIXEXPRESSION_EXP)
public class MatchInfixExpression extends MatchExpression {

    public InfixExpressionChangeEntity newEntity() {
        return new InfixExpressionChangeEntity(mBean);
    }

    @MethodLookupTbl(key = ASTNode.INFIX_EXPRESSION)
    public void matchInfixExpressionTopDown(MiningActionData fp, Action a) {
        init();
        traverseTopDown(fp, a, TraverseWays.TopDown.TYPE_I, 0);
        newBean(a);
        InfixExpressionChangeEntity code = newEntity();
        setTopDown(code, a, fp, 0);
    }


    @MethodLookupTbl(key = ASTNode.INFIX_EXPRESSION)
    public void matchInfixExpressionBottomUpNew(MiningActionData fp, Action a, Tree queryFather, int treeType, Tree traverseFather) {
        init();
        traverseBottomUp(fp, a, traverseFather, TraverseWays.BottomUp.FATHER_NODE_GET_SAME_NODE_ACTIONS, false);
        newBean(a, queryFather, treeType);
        InfixExpressionChangeEntity code = newEntity();
        setBottomUpNew(code, a, fp, 0);
    }


    @MethodLookupTbl(key = ASTNode.INFIX_EXPRESSION)
    public void matchInfixExpressionBottomUpCurr(MiningActionData fp, Action a, ChangeEntity changeEntity, Tree traverseFather) {
        initFromCurrEntity(changeEntity);
        traverseBottomUpCurr(fp, a, traverseFather, TraverseWays.BottomUp.FATHER_NODE_GET_SAME_NODE_ACTIONS, false);
    }
}
