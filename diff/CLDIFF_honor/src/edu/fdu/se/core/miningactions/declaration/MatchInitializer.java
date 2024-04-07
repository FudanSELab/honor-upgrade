package edu.fdu.se.core.miningactions.declaration;

import com.github.gumtreediff.actions.model.Action;
import com.github.gumtreediff.tree.Tree;
import edu.fdu.se.core.miningactions.bean.Match;
import edu.fdu.se.core.miningactions.bean.MatchDeclaration;
import edu.fdu.se.core.miningactions.bean.MiningActionData;
import edu.fdu.se.core.miningactions.util.*;
import edu.fdu.se.core.miningchangeentity.base.ChangeEntity;
import edu.fdu.se.core.miningchangeentity.base.ChangeEntityDesc;
import edu.fdu.se.core.miningchangeentity.member.InitializerChangeEntity;
import edu.fdu.se.lang.c.generatingactions.CParserVisitor;
import edu.fdu.se.lang.common.ClassLookupTbl;
import edu.fdu.se.lang.common.MethodLookupTbl;
import org.eclipse.jdt.core.dom.ASTNode;

@ClassLookupTbl(key = ChangeEntityDesc.StageIIENTITY.ENTITY_INITIALIZER)
public class MatchInitializer extends MatchDeclaration {

    public InitializerChangeEntity newEntity() {
        return new InitializerChangeEntity(mBean);
    }


    @MethodLookupTbl(key = ASTNode.INITIALIZER)
    public void matchInitializerTopDown(MiningActionData fp, Action a) {
        init();
        traverseTopDown(fp, a, TraverseWays.TopDown.INITIALIZER, -1);
        newBean(a);
        InitializerChangeEntity code = newEntity();
        setTopDown(code, a, fp, 0);

    }
    

    @MethodLookupTbl(key = ASTNode.INITIALIZER)
    public void matchInitializerBottomUpNew(MiningActionData fp, Action a, Tree queryFather, int treeType, Tree traverseFather) {
        init();
        traverseBottomUp(fp, a, traverseFather, TraverseWays.BottomUp.FATHER_NODE_GET_SAME_NODE_ACTIONS, false);
        newBean(a, queryFather, treeType);
        InitializerChangeEntity code = newEntity();
        setBottomUpNew(code, a, fp, 0);
    }
    


    @MethodLookupTbl(key = ASTNode.INITIALIZER)
    public void matchInitializerBottomUpCurr(MiningActionData fp, Action a, ChangeEntity changeEntity, Tree traverseFather) {
        initFromCurrEntity(changeEntity);
        traverseBottomUpCurr(fp, a, traverseFather, TraverseWays.BottomUp.FATHER_NODE_GET_SAME_NODE_ACTIONS, false);
    }

}

