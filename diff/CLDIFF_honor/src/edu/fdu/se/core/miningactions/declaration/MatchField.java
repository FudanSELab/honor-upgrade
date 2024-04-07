package edu.fdu.se.core.miningactions.declaration;

import com.github.gumtreediff.actions.model.Action;
import com.github.gumtreediff.tree.Tree;
import edu.fdu.se.core.miningactions.bean.Match;
import edu.fdu.se.core.miningactions.bean.MatchDeclaration;
import edu.fdu.se.core.miningactions.util.*;
import edu.fdu.se.core.miningactions.bean.MiningActionData;
import edu.fdu.se.core.miningchangeentity.base.ChangeEntity;
import edu.fdu.se.core.miningchangeentity.base.ChangeEntityDesc;
import edu.fdu.se.core.miningchangeentity.member.FieldChangeEntity;
import edu.fdu.se.lang.c.generatingactions.CParserVisitor;
import edu.fdu.se.lang.common.ClassLookupTbl;
import edu.fdu.se.lang.common.MethodLookupTbl;
import org.eclipse.jdt.core.dom.ASTNode;

@ClassLookupTbl(key = ChangeEntityDesc.StageIIENTITY.ENTITY_FIELD)
public class MatchField extends MatchDeclaration {

    public FieldChangeEntity newEntity() {
        return new FieldChangeEntity(mBean);
    }

    @MethodLookupTbl(key = ASTNode.FIELD_DECLARATION)
    public void matchFieldTopDown(MiningActionData fp, Action a) {
        init();
        traverseTopDown(fp, a, TraverseWays.TopDown.FIELD, 0);
        newBean(a);
        FieldChangeEntity code = newEntity();
        setTopDown(code, a, fp, 0);
    }

    @MethodLookupTbl(key = CParserVisitor.FIELD_DECLARATION)
    public void matchFieldTopDownC(MiningActionData fp, Action a) {
        init();
        traverseTopDown(fp, a, TraverseWays.TopDown.FIELD, 0);
        newBean(a);
        FieldChangeEntity code = newEntity();
        setTopDown(code, a, fp, 0);
    }


    @MethodLookupTbl(key = ASTNode.FIELD_DECLARATION)
    public void matchFieldBottomUpNew(MiningActionData fp, Action a, Tree queryFather, int treeType, Tree traverseFather) {
        init();
        traverseBottomUp(fp, a, traverseFather, TraverseWays.BottomUp.FATHER_NODE_GET_SAME_NODE_ACTIONS, false);
        newBean(a, queryFather, treeType);
        FieldChangeEntity code = newEntity();
        setBottomUpNew(code, a, fp, 0);
    }

    @MethodLookupTbl(key = CParserVisitor.FIELD_DECLARATION)
    public void matchFieldBottomUpNewC(MiningActionData fp, Action a, Tree queryFather, int treeType, Tree traverseFather) {
        init();
        traverseBottomUp(fp, a, traverseFather, TraverseWays.BottomUp.FATHER_NODE_GET_SAME_NODE_ACTIONS, false);
        newBean(a, queryFather, treeType);
        FieldChangeEntity code = newEntity();
        setBottomUpNew(code, a, fp, 0);
    }


    @MethodLookupTbl(key = ASTNode.FIELD_DECLARATION)
    public void matchFieldBottomUpCurr(MiningActionData fp, Action a, ChangeEntity changeEntity, Tree traverseFather) {
        initFromCurrEntity(changeEntity);
        traverseBottomUpCurr(fp, a, traverseFather, TraverseWays.BottomUp.FATHER_NODE_GET_SAME_NODE_ACTIONS, false);
    }

    @MethodLookupTbl(key = CParserVisitor.FIELD_DECLARATION)
    public void matchFieldBottomUpCurrC(MiningActionData fp, Action a, ChangeEntity changeEntity, Tree traverseFather) {
        initFromCurrEntity(changeEntity);
        traverseBottomUpCurr(fp, a, traverseFather, TraverseWays.BottomUp.FATHER_NODE_GET_SAME_NODE_ACTIONS, false);
    }
}
