package edu.fdu.se.core.miningactions.expression;

import com.github.gumtreediff.actions.model.Action;
import com.github.gumtreediff.tree.Tree;
import edu.fdu.se.core.miningactions.bean.MatchExpression;
import edu.fdu.se.core.miningactions.bean.MiningActionData;
import edu.fdu.se.core.miningactions.util.TraverseWays;
import edu.fdu.se.core.miningchangeentity.base.ChangeEntity;
import edu.fdu.se.core.miningchangeentity.base.ChangeEntityDesc;
import edu.fdu.se.core.miningchangeentity.expression.FieldAccessChangeEntity;
import edu.fdu.se.lang.common.ClassLookupTbl;
import edu.fdu.se.lang.common.MethodLookupTbl;
import org.eclipse.jdt.core.dom.ASTNode;


@ClassLookupTbl(key = ChangeEntityDesc.StageIIENTITY.ENTITY_FIELDACCESS_EXP + ChangeEntityDesc.StageIIENTITY.ENTITY_SUPERFIELDACCESS_EXP)
public class MatchFieldAccess extends MatchExpression {

    public FieldAccessChangeEntity newEntity() {
        return new FieldAccessChangeEntity(mBean);
    }

    @MethodLookupTbl(key = ASTNode.FIELD_ACCESS)
    public void matchFieldAccessTopDown(MiningActionData fp, Action a) {
        init();
        traverseTopDown(fp, a, TraverseWays.TopDown.TYPE_I, 0);
        newBean(a);
        FieldAccessChangeEntity code = newEntity();
        setTopDown(code, a, fp, 0);
    }


    @MethodLookupTbl(key = ASTNode.FIELD_ACCESS)
    public void matchFieldAccessBottomUpNew(MiningActionData fp, Action a, Tree queryFather, int treeType, Tree traverseFather) {
        init();
        traverseBottomUp(fp, a, traverseFather, TraverseWays.BottomUp.FATHER_NODE_GET_SAME_NODE_ACTIONS, false);
        newBean(a, queryFather, treeType);
        FieldAccessChangeEntity code = newEntity();
        setBottomUpNew(code, a, fp, 0);
    }


    @MethodLookupTbl(key = ASTNode.FIELD_ACCESS)
    public void matchFieldAccessBottomUpCurr(MiningActionData fp, Action a, ChangeEntity changeEntity, Tree traverseFather) {
        initFromCurrEntity(changeEntity);
        traverseBottomUpCurr(fp, a, traverseFather, TraverseWays.BottomUp.FATHER_NODE_GET_SAME_NODE_ACTIONS, false);
    }


    @MethodLookupTbl(key = ASTNode.SUPER_FIELD_ACCESS)
    public void matchSuperFieldAccessTopDown(MiningActionData fp, Action a) {
        init();
        traverseTopDown(fp, a, TraverseWays.TopDown.TYPE_I, 0);
        newBean(a);
        FieldAccessChangeEntity code = newEntity();
        setTopDown(code, a, fp, 1);
    }


    @MethodLookupTbl(key = ASTNode.SUPER_FIELD_ACCESS)
    public void matchSuperFieldAccessBottomUpNew(MiningActionData fp, Action a, Tree queryFather, int treeType, Tree traverseFather) {
        init();
        traverseBottomUp(fp, a, traverseFather, TraverseWays.BottomUp.FATHER_NODE_GET_SAME_NODE_ACTIONS, false);
        newBean(a, queryFather, treeType);
        FieldAccessChangeEntity code = newEntity();
        setBottomUpNew(code, a, fp, 1);
    }


    @MethodLookupTbl(key = ASTNode.SUPER_FIELD_ACCESS)
    public void matchSueprFieldAccessBottomUpCurr(MiningActionData fp, Action a, ChangeEntity changeEntity, Tree traverseFather) {
        initFromCurrEntity(changeEntity);
        traverseBottomUpCurr(fp, a, traverseFather, TraverseWays.BottomUp.FATHER_NODE_GET_SAME_NODE_ACTIONS, false);
    }
}
