package edu.fdu.se.core.miningactions.statement;

import com.github.gumtreediff.actions.model.Action;

import com.github.gumtreediff.tree.Tree;
import edu.fdu.se.core.miningactions.bean.Match;
import edu.fdu.se.core.miningactions.bean.MatchStatement;
import edu.fdu.se.core.miningactions.bean.MiningActionData;
import edu.fdu.se.core.miningactions.util.*;
import edu.fdu.se.core.miningchangeentity.base.ChangeEntity;
import edu.fdu.se.core.miningchangeentity.base.ChangeEntityDesc;
import edu.fdu.se.core.miningchangeentity.statement.VariableChangeEntity;
import edu.fdu.se.lang.common.ClassLookupTbl;
import edu.fdu.se.lang.common.MethodLookupTbl;
import org.eclipse.jdt.core.dom.ASTNode;

@ClassLookupTbl(key = ChangeEntityDesc.StageIIENTITY.ENTITY_VARIABLE_STMT)
public class MatchVariableDeclaration extends MatchStatement {

    public VariableChangeEntity newEntity() {
        return new VariableChangeEntity(mBean);
    }


    @MethodLookupTbl(key = ASTNode.VARIABLE_DECLARATION_STATEMENT)
    public void matchVariableDeclarationTopDown(MiningActionData fp, Action a) {
        init();
        traverseTopDown(fp, a, TraverseWays.TopDown.TYPE_I, 0);
        newBean(a);
        VariableChangeEntity code = newEntity();
        setTopDown(code, a, fp, 0);
    }

    @MethodLookupTbl(key = ASTNode.VARIABLE_DECLARATION_STATEMENT)
    public void matchVariableDeclarationBottomUpNew(MiningActionData fp, Action a, Tree queryFather, int treeType, Tree traverseFather) {
        init();
        traverseBottomUp(fp, a, traverseFather, TraverseWays.BottomUp.FATHER_NODE_GET_SAME_NODE_ACTIONS, false);
        newBean(a, queryFather, treeType);
        VariableChangeEntity code = newEntity();
        setBottomUpNew(code, a, fp, 0);
    }

    @MethodLookupTbl(key = ASTNode.VARIABLE_DECLARATION_STATEMENT)
    public void matchVariableDeclarationBottomUpCurr(MiningActionData fp, Action a, ChangeEntity changeEntity, Tree traverseFather) {
        initFromCurrEntity(changeEntity);
        traverseBottomUpCurr(fp, a, traverseFather, TraverseWays.BottomUp.FATHER_NODE_GET_SAME_NODE_ACTIONS, false);
    }
}
