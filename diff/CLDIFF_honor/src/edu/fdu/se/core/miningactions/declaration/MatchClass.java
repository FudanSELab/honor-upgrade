package edu.fdu.se.core.miningactions.declaration;

import com.github.gumtreediff.actions.model.Action;
import com.github.gumtreediff.tree.Tree;
import edu.fdu.se.core.miningactions.bean.Match;
import edu.fdu.se.core.miningactions.bean.MatchDeclaration;
import edu.fdu.se.core.miningactions.util.*;
import edu.fdu.se.core.miningactions.bean.MiningActionData;
import edu.fdu.se.core.miningchangeentity.base.ChangeEntity;
import edu.fdu.se.core.miningchangeentity.base.ChangeEntityDesc;
import edu.fdu.se.core.miningchangeentity.member.ClassChangeEntity;
import edu.fdu.se.lang.c.generatingactions.CParserVisitor;
import edu.fdu.se.lang.common.ClassLookupTbl;
import edu.fdu.se.lang.common.MethodLookupTbl;
import org.eclipse.jdt.core.dom.ASTNode;

/**
 * invoke via reflection
 */
@ClassLookupTbl(key = ChangeEntityDesc.StageIIENTITY.ENTITY_CLASS)
public class MatchClass extends MatchDeclaration {

    public ClassChangeEntity newEntity() {
        return new ClassChangeEntity(mBean);
    }


    @MethodLookupTbl(key = ASTNode.TYPE_DECLARATION)
    public void matchClassTopDown(MiningActionData mad, Action action) {
        init();
        traverseTopDown(mad, action, TraverseWays.TopDown.CLASSD, 1);
        newBean(action);
        ClassChangeEntity code = newEntity();
        setTopDown(code, action, mad, 0);
        code.stageIIBean.setSubEntity(ChangeEntityDesc.StageIISub.SUB_CONDITION_AND_BODY);
        code.stageIIBean.setGranularity(ChangeEntityDesc.StageIIGranularity.GRANULARITY_CLASS);
    }

    @MethodLookupTbl(key = CParserVisitor.TYPE_DECLARATION)
    public void matchClassTopDownC(MiningActionData mad, Action action) {
        init();
        traverseTopDown(mad, action, TraverseWays.TopDown.CLASSD, 1);
        newBean(action);
        ClassChangeEntity code = newEntity();
        setTopDown(code, action, mad, 0);
        code.stageIIBean.setSubEntity(ChangeEntityDesc.StageIISub.SUB_CONDITION_AND_BODY);
        code.stageIIBean.setGranularity(ChangeEntityDesc.StageIIGranularity.GRANULARITY_CLASS);
    }

    @MethodLookupTbl(key = ASTNode.TYPE_DECLARATION)
    public void matchClassSignatureBottomUpNew(MiningActionData fp, Action a, Tree queryFather, int treeType, Tree traverseFather) {
        init();
        traverseBottomUp(fp, a, traverseFather, TraverseWays.BottomUp.CLASS_SIGNATURE, false);
        newBean(a, queryFather, treeType);
        ClassChangeEntity code = newEntity();
        //setBottomUpNew(code, a, fp, 0);
        setBottomUpNew1(code, a, fp, 0);
        //code.stageIIBean.setSubEntity(ChangeEntityDesc.StageIISub.SUB_DECLARATION);
        code.stageIIBean.setGranularity(ChangeEntityDesc.StageIIGranularity.GRANULARITY_CLASS);
    }

    @MethodLookupTbl(key = CParserVisitor.TYPE_DECLARATION)
    public void matchClassSignatureBottomUpNewC(MiningActionData fp, Action a, Tree queryFather, int treeType, Tree traverseFather) {
        init();
        traverseBottomUp(fp, a, traverseFather, TraverseWays.BottomUp.CLASS_SIGNATURE, false);
        newBean(a, queryFather, treeType);
        ClassChangeEntity code = newEntity();
        //setBottomUpNew(code, a, fp, 0);
        setBottomUpNew1(code, a, fp, 0);
        //code.stageIIBean.setSubEntity(ChangeEntityDesc.StageIISub.SUB_DECLARATION);
        code.stageIIBean.setGranularity(ChangeEntityDesc.StageIIGranularity.GRANULARITY_CLASS);
    }


    @MethodLookupTbl(key = ASTNode.TYPE_DECLARATION)
    public void matchClassSignatureBottomUpCurr(MiningActionData fp, Action a, ChangeEntity changeEntity, Tree traverseFather) {
        initFromCurrEntity(changeEntity);
        traverseBottomUpCurr(fp, a, traverseFather, TraverseWays.BottomUp.CLASS_SIGNATURE, false);
        if (a.toString().contains("SimpleName")){
            return;
        }
        else{
            String newStr = changeEntity.stageIIBean.getSubEntity() + ";" + a.toString().replace("SimpleType","class_or_interface");
            changeEntity.stageIIBean.setSubEntity(newStr);
        }
    }

    @MethodLookupTbl(key = CParserVisitor.TYPE_DECLARATION)
    public void matchClassSignatureBottomUpCurrC(MiningActionData fp, Action a, ChangeEntity changeEntity, Tree traverseFather) {
        initFromCurrEntity(changeEntity);
        traverseBottomUpCurr(fp, a, traverseFather, TraverseWays.BottomUp.CLASS_SIGNATURE, false);
        if (a.toString().contains("SimpleName")){
            return;
        }
        else{
            String newStr = changeEntity.stageIIBean.getSubEntity() + ";" + a.toString().replace("SimpleType","class_or_interface");
            changeEntity.stageIIBean.setSubEntity(newStr);
        }
    }

}
