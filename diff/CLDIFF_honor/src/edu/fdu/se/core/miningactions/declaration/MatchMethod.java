package edu.fdu.se.core.miningactions.declaration;

import com.github.gumtreediff.actions.model.Action;

import com.github.gumtreediff.tree.Tree;
import edu.fdu.se.core.miningactions.bean.Match;
import edu.fdu.se.core.miningactions.bean.MatchDeclaration;
import edu.fdu.se.core.miningactions.bean.MiningActionData;
import edu.fdu.se.core.miningactions.util.*;
import edu.fdu.se.core.miningchangeentity.base.ChangeEntity;
import edu.fdu.se.core.miningchangeentity.base.ChangeEntityDesc;
import edu.fdu.se.core.miningchangeentity.member.MethodChangeEntity;
import edu.fdu.se.lang.c.generatingactions.CParserVisitor;
import edu.fdu.se.lang.common.ClassLookupTbl;
import edu.fdu.se.lang.common.MethodLookupTbl;
import org.eclipse.jdt.core.dom.ASTNode;

@ClassLookupTbl(key = ChangeEntityDesc.StageIIENTITY.ENTITY_METHOD)
public class MatchMethod extends MatchDeclaration {

	public MethodChangeEntity newEntity() {
		return new MethodChangeEntity(mBean);
	}


    @MethodLookupTbl(key = ASTNode.METHOD_DECLARATION)
    public void matchMethodTopDown(MiningActionData fp, Action a) {
        init();
        traverseTopDown(fp, a, TraverseWays.TopDown.METHOD, 1);
        newBean(a);
        MethodChangeEntity code = newEntity();
        setTopDown(code, a, fp, 0);
    }
    @MethodLookupTbl(key = CParserVisitor.METHOD_DECLARATION)
    public void matchMethodTopDownC(MiningActionData fp, Action a) {
        init();
        traverseTopDown(fp, a, TraverseWays.TopDown.METHOD, 1);
        newBean(a);
        MethodChangeEntity code = newEntity();
        setTopDown(code, a, fp, 0);
    }


    @MethodLookupTbl(key = ASTNode.METHOD_DECLARATION)
    public void matchMethodSignatureBottomUpNew(MiningActionData fp, Action a, Tree queryFather, int treeType, Tree traverseFather) {
        init();
        traverseBottomUp(fp, a, traverseFather, TraverseWays.BottomUp.METHOD_SIGNATURE, false);
        newBean(a, queryFather, treeType);
        MethodChangeEntity code = newEntity();
        setBottomUpNew(code, a, fp, 0);
        code.stageIIBean.setSubEntity(ChangeEntityDesc.StageIISub.SUB_DECLARATION);
    }

    @MethodLookupTbl(key = CParserVisitor.METHOD_DECLARATION)
    public void matchMethodSignatureBottomUpNewC(MiningActionData fp, Action a, Tree queryFather, int treeType, Tree traverseFather) {
        init();
        traverseBottomUp(fp, a, traverseFather, TraverseWays.BottomUp.METHOD_SIGNATURE, false);
        newBean(a, queryFather, treeType);
        MethodChangeEntity code = newEntity();
        setBottomUpNew(code, a, fp, 0);
        code.stageIIBean.setSubEntity(ChangeEntityDesc.StageIISub.SUB_DECLARATION);
    }


    @MethodLookupTbl(key = ASTNode.METHOD_DECLARATION)
    public void matchMethodSignatureBottomUpCurr(MiningActionData fp, Action a, ChangeEntity changeEntity, Tree traverseFather) {
        initFromCurrEntity(changeEntity);
        traverseBottomUpCurr(fp, a, traverseFather, TraverseWays.BottomUp.METHOD_SIGNATURE, false);
    }
    @MethodLookupTbl(key = CParserVisitor.METHOD_DECLARATION)
    public void matchMethodSignatureBottomUpCurrC(MiningActionData fp, Action a, ChangeEntity changeEntity, Tree traverseFather) {
        initFromCurrEntity(changeEntity);
        traverseBottomUpCurr(fp, a, traverseFather, TraverseWays.BottomUp.METHOD_SIGNATURE, false);
    }





}
