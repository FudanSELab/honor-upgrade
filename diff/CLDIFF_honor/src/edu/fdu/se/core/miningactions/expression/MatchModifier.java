package edu.fdu.se.core.miningactions.expression;

import com.github.gumtreediff.actions.model.Action;
import com.github.gumtreediff.tree.Tree;
import edu.fdu.se.core.miningactions.bean.MatchExpression;
import edu.fdu.se.core.miningactions.bean.MiningActionData;
import edu.fdu.se.core.miningactions.util.TraverseWays;
import edu.fdu.se.core.miningchangeentity.base.ChangeEntity;
import edu.fdu.se.core.miningchangeentity.base.ChangeEntityDesc;
import edu.fdu.se.core.miningchangeentity.expression.AnnotationChangeEntity;
import edu.fdu.se.core.miningchangeentity.expression.ModifierChangeEntity;
import edu.fdu.se.lang.common.ClassLookupTbl;
import edu.fdu.se.lang.common.MethodLookupTbl;
import org.eclipse.jdt.core.dom.ASTNode;

@ClassLookupTbl(key = ChangeEntityDesc.StageIIENTITY.ENTITY_MODIFIER_EXP)
public class MatchModifier extends MatchExpression {

    public ModifierChangeEntity newEntity() {
        return new ModifierChangeEntity(mBean);
    }

    @MethodLookupTbl(key = ASTNode.MODIFIER)
    public void matchMarkerAnnotationTopDown(MiningActionData fp, Action a) {
        init();
        traverseTopDown(fp, a, TraverseWays.TopDown.TYPE_I, 0);
        newBean(a);
        ModifierChangeEntity code = newEntity();
        setTopDown(code, a, fp, 0);
    }

    @MethodLookupTbl(key = ASTNode.MODIFIER)
    public void matchMarkerAnnotationBottomUpNew(MiningActionData fp, Action a, Tree queryFather, int treeType, Tree traverseFather) {
        init();
        traverseBottomUp(fp, a, traverseFather, TraverseWays.BottomUp.FATHER_NODE_GET_SAME_NODE_ACTIONS, false);
        newBean(a, queryFather, treeType);
        ModifierChangeEntity code = newEntity();
        setBottomUpNew(code, a, fp, 0);

    }

    @MethodLookupTbl(key = ASTNode.MODIFIER)
    public void matchMarkerAnnotationBottomUpCurr(MiningActionData fp, Action a, ChangeEntity changeEntity, Tree traverseFather) {
        initFromCurrEntity(changeEntity);
        traverseBottomUpCurr(fp, a, traverseFather, TraverseWays.BottomUp.FATHER_NODE_GET_SAME_NODE_ACTIONS, false);
    }



}
