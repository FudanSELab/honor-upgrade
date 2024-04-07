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
import edu.fdu.se.core.miningchangeentity.expression.NameChangeEntity;
import edu.fdu.se.lang.common.ClassLookupTbl;
import edu.fdu.se.lang.common.MethodLookupTbl;
import org.eclipse.jdt.core.dom.ASTNode;

@ClassLookupTbl(key = ChangeEntityDesc.StageIIENTITY.ENTITY_QUALIFIEDNAME_EXP + ChangeEntityDesc.SPLITTER + ChangeEntityDesc.StageIIENTITY.ENTITY_SIMPLENAME_EXP)
public class MatchName extends MatchExpression {

    public NameChangeEntity newEntity() {
        return new NameChangeEntity(mBean);
    }

    @MethodLookupTbl(key = ASTNode.SIMPLE_NAME)
    public void matchSimpleNameTopDown(MiningActionData fp, Action a) {
        init();
        traverseTopDown(fp, a, TraverseWays.TopDown.TYPE_I, 1);
        newBean(a);
        NameChangeEntity code = newEntity();
        setTopDown(code, a, fp, 1);
    }

    @MethodLookupTbl(key = ASTNode.SIMPLE_NAME)
    public void matchSimpleNameBottomUpNew(MiningActionData fp, Action a, Tree queryFather, int treeType, Tree traverseFather) {
        init();
        traverseBottomUp(fp, a, traverseFather, TraverseWays.BottomUp.FATHER_NODE_GET_SAME_NODE_ACTIONS, false);
        newBean(a, queryFather, treeType);
        NameChangeEntity code = newEntity();
        setBottomUpNew(code, a, fp, 1);
    }

    @MethodLookupTbl(key = ASTNode.SIMPLE_NAME)
    public void matchSimpleNameBottomUpCurr(MiningActionData fp, Action a, ChangeEntity changeEntity, Tree traverseFather) {
        initFromCurrEntity(changeEntity);
        traverseBottomUpCurr(fp, a, traverseFather, TraverseWays.BottomUp.FATHER_NODE_GET_SAME_NODE_ACTIONS, false);
    }


    @MethodLookupTbl(key = ASTNode.QUALIFIED_NAME)
    public void matchQualifiedNameTopDown(MiningActionData fp, Action a) {
        init();
        traverseTopDown(fp, a, TraverseWays.TopDown.TYPE_I, 1);
        newBean(a);
        NameChangeEntity code = newEntity();
        setTopDown(code, a, fp, 0);
    }

    @MethodLookupTbl(key = ASTNode.QUALIFIED_NAME)
    public void matchQualifiedNameBottomUpNew(MiningActionData fp, Action a, Tree queryFather, int treeType, Tree traverseFather) {
        init();
        traverseBottomUp(fp, a, traverseFather, TraverseWays.BottomUp.FATHER_NODE_GET_SAME_NODE_ACTIONS, false);
        newBean(a, queryFather, treeType);
        NameChangeEntity code = newEntity();
        setBottomUpNew(code, a, fp, 0);
    }

    @MethodLookupTbl(key = ASTNode.QUALIFIED_NAME)
    public void matchQualifiedNameBottomUpCurr(MiningActionData fp, Action a, ChangeEntity changeEntity, Tree traverseFather) {
        initFromCurrEntity(changeEntity);
        traverseBottomUpCurr(fp, a, traverseFather, TraverseWays.BottomUp.FATHER_NODE_GET_SAME_NODE_ACTIONS, false);
    }


}