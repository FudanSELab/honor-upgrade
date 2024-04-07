package edu.fdu.se.core.miningactions.declaration;

import com.github.gumtreediff.actions.model.Action;
import com.github.gumtreediff.tree.Tree;
import edu.fdu.se.core.miningactions.bean.Match;
import edu.fdu.se.core.miningactions.bean.MatchDeclaration;
import edu.fdu.se.core.miningactions.bean.MiningActionData;
import edu.fdu.se.core.miningactions.util.*;
import edu.fdu.se.core.miningchangeentity.base.ChangeEntity;
import edu.fdu.se.core.miningchangeentity.base.ChangeEntityDesc;
import edu.fdu.se.core.miningchangeentity.member.EnumChangeEntity;
import edu.fdu.se.lang.c.generatingactions.CParserVisitor;
import edu.fdu.se.lang.common.ClassLookupTbl;
import edu.fdu.se.lang.common.MethodLookupTbl;
import org.eclipse.jdt.core.dom.ASTNode;

/**
 * Created by huangkaifeng on 2018/3/21.
 * enum 增和删在预处理   enum内部的增删改，看作是enum的改，
 * constant的增删改 -> EnumConstantDeclaration- > EnumDeclaration
 * method field 的增删改 - > 其自身的增删改 （不另加考虑）
 */
@ClassLookupTbl(key = ChangeEntityDesc.StageIIENTITY.ENTITY_ENUM)
public class MatchEnum extends MatchDeclaration {

    public EnumChangeEntity newEntity() {
        return new EnumChangeEntity(mBean);
    }


    @MethodLookupTbl(key = ASTNode.ENUM_DECLARATION)
    public void matchEnumTopDown(MiningActionData fp, Action a) {
        init();
        traverseTopDown(fp, a, TraverseWays.TopDown.TYPE_I, 1);
        newBean(a);
        EnumChangeEntity code = newEntity();
        setTopDown(code, a, fp, 0);

    }

    @MethodLookupTbl(key = CParserVisitor.ENUM_DECLARATION)
    public void matchEnumTopDownC(MiningActionData fp, Action a) {
        init();
        traverseTopDown(fp, a, TraverseWays.TopDown.TYPE_I, 1);
        newBean(a);
        EnumChangeEntity code = newEntity();
        setTopDown(code, a, fp, 0);

    }


    @MethodLookupTbl(key = ASTNode.ENUM_DECLARATION)
    public void matchEnumBottomUpNew(MiningActionData fp, Action a, Tree queryFather, int treeType, Tree traverseFather) {
        init();
        traverseBottomUp(fp, a, traverseFather, TraverseWays.BottomUp.FATHER_NODE_GET_SAME_NODE_ACTIONS, false);
        newBean(a, queryFather, treeType);
        EnumChangeEntity code = newEntity();
        setBottomUpNew(code, a, fp, 0);
    }

    @MethodLookupTbl(key = CParserVisitor.ENUM_DECLARATION)
    public void matchEnumBottomUpNewC(MiningActionData fp, Action a, Tree queryFather, int treeType, Tree traverseFather) {
        init();
        traverseBottomUp(fp, a, traverseFather, TraverseWays.BottomUp.FATHER_NODE_GET_SAME_NODE_ACTIONS, false);
        newBean(a, queryFather, treeType);
        EnumChangeEntity code = newEntity();
        setBottomUpNew(code, a, fp, 0);
    }


    @MethodLookupTbl(key = ASTNode.ENUM_DECLARATION)
    public void matchEnumBottomUpCurr(MiningActionData fp, Action a, ChangeEntity changeEntity, Tree traverseFather) {
        initFromCurrEntity(changeEntity);
        traverseBottomUpCurr(fp, a, traverseFather, TraverseWays.BottomUp.FATHER_NODE_GET_SAME_NODE_ACTIONS, false);
    }

    @MethodLookupTbl(key = CParserVisitor.ENUM_DECLARATION)
    public void matchEnumBottomUpCurrC(MiningActionData fp, Action a, ChangeEntity changeEntity, Tree traverseFather) {
        initFromCurrEntity(changeEntity);
        traverseBottomUpCurr(fp, a, traverseFather, TraverseWays.BottomUp.FATHER_NODE_GET_SAME_NODE_ACTIONS, false);
    }
}
