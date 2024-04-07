package edu.fdu.se.core.miningactions.statement;

import com.github.gumtreediff.actions.model.Action;
import com.github.gumtreediff.tree.Tree;
import edu.fdu.se.core.miningactions.bean.Match;
import edu.fdu.se.core.miningactions.bean.MatchStatement;
import edu.fdu.se.core.miningactions.bean.MiningActionData;
import edu.fdu.se.core.miningactions.util.*;
import edu.fdu.se.core.miningchangeentity.base.ChangeEntity;
import edu.fdu.se.core.miningchangeentity.base.ChangeEntityDesc;
import edu.fdu.se.core.miningchangeentity.statement.ForChangeEntity;
import edu.fdu.se.lang.common.ClassLookupTbl;
import edu.fdu.se.lang.common.MethodLookupTbl;
import org.eclipse.jdt.core.dom.ASTNode;

@ClassLookupTbl(key = ChangeEntityDesc.StageIIENTITY.ENTITY_FOR_STMT + ChangeEntityDesc.SPLITTER + ChangeEntityDesc.StageIIENTITY.ENTITY_ENHANCED_FOR_STMT)
public class MatchFor extends MatchStatement {

    public ForChangeEntity newEntity() {
        return new ForChangeEntity(mBean);
    }


    @MethodLookupTbl(key = ASTNode.FOR_STATEMENT)
    public void matchForTopDown(MiningActionData fp, Action a) {
        init();
        traverseTopDown(fp, a, TraverseWays.TopDown.IF, 1);
        newBean(a);
        ForChangeEntity code = newEntity();
        setTopDown(code, a, fp, 0);

    }


    @MethodLookupTbl(key = ASTNode.ENHANCED_FOR_STATEMENT)
    public void matchEnhancedForTopDown(MiningActionData fp, Action a) {
        init();
        traverseTopDown(fp, a, TraverseWays.TopDown.IF, 1);
        newBean(a);
        ForChangeEntity code = newEntity();
        setTopDown(code, a, fp, 1);
    }


    @MethodLookupTbl(key = ASTNode.FOR_STATEMENT)
    public void matchForConditionBottomUpNew(MiningActionData fp, Action a, Tree queryFather, int treeType, Tree traverseFather) {
        init();
        traverseBottomUp(fp, a, traverseFather, TraverseWays.BottomUp.IF_PREDICATE, false);
        newBean(a, queryFather, treeType);
        ForChangeEntity code = newEntity();
        setBottomUpNew(code, a, fp, 0);

    }


    @MethodLookupTbl(key = ASTNode.ENHANCED_FOR_STATEMENT)
    public void matchEnhancedForConditionBottomUpNew(MiningActionData fp, Action a, Tree queryFather, int treeType, Tree traverseFather) {

        init();
        traverseBottomUp(fp, a, traverseFather, TraverseWays.BottomUp.IF_PREDICATE, false);
        newBean(a, queryFather, treeType);
        ForChangeEntity code = newEntity();
        setBottomUpNew(code, a, fp, 1);
    }


    @MethodLookupTbl(key = ASTNode.FOR_STATEMENT)
    public void matchForConditionBottomUpCurr(MiningActionData fp, Action a, ChangeEntity changeEntity, Tree traverseFather) {
        initFromCurrEntity(changeEntity);
        traverseBottomUpCurr(fp, a, traverseFather, TraverseWays.BottomUp.IF_PREDICATE, false);

    }

    @MethodLookupTbl(key = ASTNode.ENHANCED_FOR_STATEMENT)
    public void matchEnhancedForConditionBottomUpCurr(MiningActionData fp, Action a, ChangeEntity changeEntity, Tree traverseFather) {
        initFromCurrEntity(changeEntity);
        traverseBottomUpCurr(fp, a, traverseFather, TraverseWays.BottomUp.IF_PREDICATE, false);
    }
}
