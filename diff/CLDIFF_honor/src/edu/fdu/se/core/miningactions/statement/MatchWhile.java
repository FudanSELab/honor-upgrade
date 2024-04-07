package edu.fdu.se.core.miningactions.statement;

import com.github.gumtreediff.actions.model.Action;
import com.github.gumtreediff.tree.Tree;
import edu.fdu.se.core.miningactions.bean.Match;
import edu.fdu.se.core.miningactions.bean.MatchStatement;
import edu.fdu.se.core.miningactions.bean.MiningActionData;
import edu.fdu.se.core.miningactions.util.*;
import edu.fdu.se.core.miningchangeentity.base.ChangeEntity;
import edu.fdu.se.core.miningchangeentity.base.ChangeEntityDesc;
import edu.fdu.se.core.miningchangeentity.statement.WhileChangeEntity;
import edu.fdu.se.lang.common.ClassLookupTbl;
import edu.fdu.se.lang.common.MethodLookupTbl;
import org.eclipse.jdt.core.dom.ASTNode;

@ClassLookupTbl(key = ChangeEntityDesc.StageIIENTITY.ENTITY_WHILE_STMT + ChangeEntityDesc.SPLITTER + ChangeEntityDesc.StageIIENTITY.ENTITY_DO_STMT)
public class MatchWhile extends MatchStatement {

    public WhileChangeEntity newEntity() {
        return new WhileChangeEntity(mBean);
    }

    @MethodLookupTbl(key = ASTNode.WHILE_STATEMENT)
    public void matchWhileTopDown(MiningActionData fp, Action a) {
        init();
        traverseTopDown(fp, a, TraverseWays.TopDown.IF, 1);
        newBean(a);
        WhileChangeEntity code = newEntity();
        setTopDown(code, a, fp, 0);
        code.stageIIBean.setSubEntity(ChangeEntityDesc.StageIISub.SUB_CONDITION_AND_BODY);
    }

    @MethodLookupTbl(key = ASTNode.DO_STATEMENT)
    public void matchDoTopDown(MiningActionData fp, Action a) {
        init();
        traverseTopDown(fp, a, TraverseWays.TopDown.IF, 1);
        newBean(a);
        WhileChangeEntity code = newEntity();
        setBottomUpNew(code, a, fp, 1);
    }

    @MethodLookupTbl(key = ASTNode.WHILE_STATEMENT)
    public void matchWhileConditionBottomUpNew(MiningActionData fp, Action a, Tree queryFather, int treeType, Tree traverseFather) {
        init();
        traverseBottomUp(fp, a, traverseFather, TraverseWays.BottomUp.IF_PREDICATE, false);
        newBean(a, queryFather, treeType);
        WhileChangeEntity code = newEntity();
        setBottomUpNew(code, a, fp, 0);
        code.stageIIBean.setSubEntity(ChangeEntityDesc.StageIISub.SUB_CONDITION);
    }

    @MethodLookupTbl(key = ASTNode.DO_STATEMENT)
    public void matchDoConditionBottomUpNew(MiningActionData fp, Action a, Tree queryFather, int treeType, Tree traverseFather) {

        init();
        traverseBottomUp(fp, a, traverseFather, TraverseWays.BottomUp.DO_WHILE, false);
        newBean(a, queryFather, treeType);
        WhileChangeEntity code = newEntity();
        setBottomUpNew(code, a, fp, 1);
        code.stageIIBean.setSubEntity(ChangeEntityDesc.StageIISub.SUB_CONDITION);
    }

    @MethodLookupTbl(key = ASTNode.DO_STATEMENT)
    public void matchDoConditionBottomUpCurr(MiningActionData fp, Action a, ChangeEntity changeEntity, Tree traverseFather) {
        initFromCurrEntity(changeEntity);
        traverseBottomUpCurr(fp, a, traverseFather, TraverseWays.BottomUp.DO_WHILE, false);
    }

    @MethodLookupTbl(key = ASTNode.WHILE_STATEMENT)
    public void matchWhileConditionBottomUpCurr(MiningActionData fp, Action a, ChangeEntity changeEntity, Tree traverseFather) {
        initFromCurrEntity(changeEntity);
        traverseBottomUpCurr(fp, a, traverseFather, TraverseWays.BottomUp.IF_PREDICATE, false);

    }

}
