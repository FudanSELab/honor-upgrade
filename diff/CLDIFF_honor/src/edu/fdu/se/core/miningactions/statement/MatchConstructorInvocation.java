package edu.fdu.se.core.miningactions.statement;

import com.github.gumtreediff.actions.model.Action;
import com.github.gumtreediff.tree.Tree;
import edu.fdu.se.core.miningactions.bean.Match;
import edu.fdu.se.core.miningactions.bean.MatchStatement;
import edu.fdu.se.core.miningactions.bean.MiningActionData;
import edu.fdu.se.core.miningactions.util.*;
import edu.fdu.se.core.miningchangeentity.base.ChangeEntity;
import edu.fdu.se.core.miningchangeentity.base.ChangeEntityDesc;
import edu.fdu.se.core.miningchangeentity.statement.ConstructorInvocationChangeEntity;
import edu.fdu.se.lang.common.ClassLookupTbl;
import edu.fdu.se.lang.common.MethodLookupTbl;
import org.eclipse.jdt.core.dom.ASTNode;

/**
 * Created by huangkaifeng on 2018/4/4.
 *
 */
@ClassLookupTbl(key = ChangeEntityDesc.StageIIENTITY.ENTITY_CONSTRUCTOR_INVOCATION + ChangeEntityDesc.SPLITTER + ChangeEntityDesc.StageIIENTITY.ENTITY_SUPER_CONSTRUCTOR_INVOCATION)
public class MatchConstructorInvocation extends MatchStatement {

    public ConstructorInvocationChangeEntity newEntity() {
        return new ConstructorInvocationChangeEntity(mBean);
    }

    @MethodLookupTbl(key = ASTNode.SUPER_CONSTRUCTOR_INVOCATION)
    public void matchSuperConstructorInvocationTopDown(MiningActionData fp, Action a) {

        init();
        traverseTopDown(fp, a, TraverseWays.TopDown.TYPE_I, 0);
        newBean(a);
        ConstructorInvocationChangeEntity code = newEntity();
        setTopDown(code, a, fp, 1);
    }


    @MethodLookupTbl(key = ASTNode.SUPER_CONSTRUCTOR_INVOCATION)
    public void matchSuperConstructorInvocationBottomUpNew(MiningActionData fp, Action a, Tree queryFather, int treeType, Tree traverseFather) {
        init();
        traverseBottomUp(fp, a, traverseFather, TraverseWays.BottomUp.FATHER_NODE_GET_SAME_NODE_ACTIONS, false);
        // sameEdits -> subActions
        newBean(a);
        ConstructorInvocationChangeEntity code = newEntity();
        setBottomUpNew(code, a, fp, 1);
    }


    @MethodLookupTbl(key = ASTNode.SUPER_CONSTRUCTOR_INVOCATION)
    public void matchSuperConstructorInvocationBottomUpCurr(MiningActionData fp, Action a, ChangeEntity changeEntity, Tree traverseFather) {
        initFromCurrEntity(changeEntity);
        traverseBottomUpCurr(fp, a, traverseFather, TraverseWays.BottomUp.FATHER_NODE_GET_SAME_NODE_ACTIONS, false);
    }


    @MethodLookupTbl(key = ASTNode.CONSTRUCTOR_INVOCATION)
    public void matchConstructorInvocationTopDown(MiningActionData fp, Action a) {

        init();
        traverseTopDown(fp, a, TraverseWays.TopDown.TYPE_I, 0);
        newBean(a);
        ConstructorInvocationChangeEntity code = newEntity();
        setTopDown(code, a, fp, 0);
    }


    @MethodLookupTbl(key = ASTNode.CONSTRUCTOR_INVOCATION)
    public void matchConstructorInvocationBottomUpNew(MiningActionData fp, Action a, Tree queryFather, int treeType, Tree traverseFather) {

        init();
        traverseBottomUp(fp, a, traverseFather, TraverseWays.BottomUp.FATHER_NODE_GET_SAME_NODE_ACTIONS, false);
        newBean(a, queryFather, treeType);
        ConstructorInvocationChangeEntity code = newEntity();
        setBottomUpNew(code, a, fp, 0);

    }


    @MethodLookupTbl(key = ASTNode.CONSTRUCTOR_INVOCATION)
    public void matchConstructorInvocationBottomUpCurr(MiningActionData fp, Action a, ChangeEntity changeEntity, Tree traverseFather) {
        initFromCurrEntity(changeEntity);
        traverseBottomUpCurr(fp, a, traverseFather, TraverseWays.BottomUp.FATHER_NODE_GET_SAME_NODE_ACTIONS, false);

    }
}
