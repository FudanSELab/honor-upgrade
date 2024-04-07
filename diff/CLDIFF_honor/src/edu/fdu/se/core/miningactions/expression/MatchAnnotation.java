package edu.fdu.se.core.miningactions.expression;

import com.github.gumtreediff.actions.model.Action;
import com.github.gumtreediff.tree.Tree;
import edu.fdu.se.core.miningactions.bean.Match;
import edu.fdu.se.core.miningactions.bean.MatchExpression;
import edu.fdu.se.core.miningactions.bean.MiningActionData;
import edu.fdu.se.core.miningactions.util.TraverseWays;
import edu.fdu.se.core.miningchangeentity.base.ChangeEntity;
import edu.fdu.se.core.miningchangeentity.base.ChangeEntityDesc;
import edu.fdu.se.core.miningchangeentity.expression.AnnotationChangeEntity;
import edu.fdu.se.lang.common.ClassLookupTbl;
import edu.fdu.se.lang.common.MethodLookupTbl;
import org.eclipse.jdt.core.dom.ASTNode;

@ClassLookupTbl(key = ChangeEntityDesc.StageIIENTITY.ENTITY_MARKERANNOTATION_EXP + ChangeEntityDesc.SPLITTER + ChangeEntityDesc.StageIIENTITY.ENTITY_SINGLEMEMBERANNOTATION_EXP + ChangeEntityDesc.SPLITTER + ChangeEntityDesc.StageIIENTITY.ENTITY_NORMALANNOTATION_EXP)
public class MatchAnnotation extends MatchExpression {

    public AnnotationChangeEntity newEntity() {
        return new AnnotationChangeEntity(mBean);
    }

    @MethodLookupTbl(key = ASTNode.MARKER_ANNOTATION)
    public void matchMarkerAnnotationTopDown(MiningActionData fp, Action a) {
        init();
        traverseTopDown(fp, a, TraverseWays.TopDown.TYPE_I, 0);
        newBean(a);
        AnnotationChangeEntity code = newEntity();
        setTopDown(code, a, fp, 0);
    }

    @MethodLookupTbl(key = ASTNode.MARKER_ANNOTATION)
    public void matchMarkerAnnotationBottomUpNew(MiningActionData fp, Action a, Tree queryFather, int treeType, Tree traverseFather) {
        init();
        traverseBottomUp(fp, a, traverseFather, TraverseWays.BottomUp.FATHER_NODE_GET_SAME_NODE_ACTIONS, false);
        newBean(a, queryFather, treeType);
        AnnotationChangeEntity code = newEntity();
        setBottomUpNew(code, a, fp, 0);

    }

    @MethodLookupTbl(key = ASTNode.MARKER_ANNOTATION)
    public void matchMarkerAnnotationBottomUpCurr(MiningActionData fp, Action a, ChangeEntity changeEntity, Tree traverseFather) {
        initFromCurrEntity(changeEntity);
        traverseBottomUpCurr(fp, a, traverseFather, TraverseWays.BottomUp.FATHER_NODE_GET_SAME_NODE_ACTIONS, false);
    }

    @MethodLookupTbl(key = ASTNode.SINGLE_MEMBER_ANNOTATION)
    public void matchSingleMemberAnnotationTopDown(MiningActionData fp, Action a) {
        init();
        traverseTopDown(fp, a, TraverseWays.TopDown.TYPE_I, 0);
        newBean(a);
        AnnotationChangeEntity code = newEntity();
        setTopDown(code, a, fp, 1);
    }

    @MethodLookupTbl(key = ASTNode.SINGLE_MEMBER_ANNOTATION)
    public void matchSingleMemberAnnotationBottomUpNew(MiningActionData fp, Action a, Tree queryFather, int treeType, Tree traverseFather) {
        init();
        traverseBottomUp(fp, a, traverseFather, TraverseWays.BottomUp.FATHER_NODE_GET_SAME_NODE_ACTIONS, false);
        newBean(a, queryFather, treeType);
        AnnotationChangeEntity code = newEntity();
        setBottomUpNew(code, a, fp, 1);
    }

    @MethodLookupTbl(key = ASTNode.SINGLE_MEMBER_ANNOTATION)
    public void matchSingleMemberAnnotationBottomUpCurr(MiningActionData fp, Action a, ChangeEntity changeEntity, Tree traverseFather) {
        initFromCurrEntity(changeEntity);
        traverseBottomUpCurr(fp, a, traverseFather, TraverseWays.BottomUp.FATHER_NODE_GET_SAME_NODE_ACTIONS, false);
    }


    @MethodLookupTbl(key = ASTNode.NORMAL_ANNOTATION)
    public void matchNormalAnnotationTopDown(MiningActionData fp, Action a) {
        init();
        traverseTopDown(fp, a, TraverseWays.TopDown.TYPE_I, 0);
        newBean(a);
        AnnotationChangeEntity code = newEntity();
        setTopDown(code, a, fp, 2);
    }

    @MethodLookupTbl(key = ASTNode.NORMAL_ANNOTATION)
    public void matchNormalAnnotationBottomUpNew(MiningActionData fp, Action a, Tree queryFather, int treeType, Tree traverseFather) {
        init();
        traverseBottomUp(fp, a, traverseFather, TraverseWays.BottomUp.FATHER_NODE_GET_SAME_NODE_ACTIONS, false);
        newBean(a, queryFather, treeType);
        AnnotationChangeEntity code = newEntity();
        setBottomUpNew(code, a, fp, 2);
    }

    @MethodLookupTbl(key = ASTNode.NORMAL_ANNOTATION)
    public void matchNormalAnnotationBottomUpCurr(MiningActionData fp, Action a, ChangeEntity changeEntity, Tree traverseFather) {
        initFromCurrEntity(changeEntity);
        traverseBottomUpCurr(fp, a, traverseFather, TraverseWays.BottomUp.FATHER_NODE_GET_SAME_NODE_ACTIONS, false);
    }






}
