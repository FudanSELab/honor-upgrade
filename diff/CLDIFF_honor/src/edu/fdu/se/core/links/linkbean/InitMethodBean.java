package edu.fdu.se.core.links.linkbean;

import com.github.gumtreediff.actions.model.Action;
import com.github.gumtreediff.actions.model.Move;
import com.github.gumtreediff.tree.Tree;
import edu.fdu.se.core.miningactions.generator.GranularityTool;
import edu.fdu.se.core.miningchangeentity.base.ChangeEntity;
import edu.fdu.se.global.Global;
import edu.fdu.se.core.miningactions.bean.MiningActionData;
import edu.fdu.se.core.miningactions.util.BasicTreeTraversal;
import edu.fdu.se.core.miningchangeentity.base.ChangeEntityDesc;
import edu.fdu.se.core.miningchangeentity.member.MethodChangeEntity;


import java.util.ArrayList;
import java.util.List;

/**
 * Created by huangkaifeng on 2018/4/7.
 *
 */
public class InitMethodBean {


    public static void parse(ChangeEntity en, MiningActionData fp) {
        MethodChangeEntity ce = (MethodChangeEntity) en;
        if(ce.stageIIBean.getEntityCreationStage().equals(ChangeEntityDesc.StageIIGenStage.ENTITY_GENERATION_STAGE_PRE_DIFF)){
            Object md =  ce.bodyDeclarationPair.getBodyDeclaration();
            setValue(md, ce);
        }else{
            Tree tree = (Tree)ce.clusteredActionBean.curAction.getNode();
            if(ce.clusteredActionBean.curAction instanceof Move){
                if (Global.astNodeUtil.isMethodDeclaration(tree.getNode())) {
                    Object md = tree.getNode();
                    setValue(md, ce);
                }
            }else {
                parseNonMove(ce,fp);
            }
        }

    }

    public static void setValue(Object md, ChangeEntity ce) {
        String methodName = Global.astNodeUtil.getMethodName(md);
        List<MyParameters> myParameters = Global.astNodeUtil.getMethodDeclarationParameters(md);
        String returnType = "null";
        if (Global.astNodeUtil.getMethodType(md) != null) {
            returnType = Global.astNodeUtil.getMethodType(md).toString();
        }
        int treeType = ChangeEntityDesc.optStringToTreeType(ce.getStageIIBean().getOpt());
        ce.linkBean.defList.addDef(ce.stageIIBean.getCanonicalName().getLongName(), returnType, methodName, myParameters, ce.lineRange,treeType);
    }


    public static void parseNonMove(MethodChangeEntity ce, MiningActionData fp) {
        Tree tree = (Tree)ce.clusteredActionBean.curAction.getNode();
        if (!Global.astNodeUtil.isMethodDeclaration(tree.getNode())) {
            tree = BasicTreeTraversal.findCommonRootNode(ce.clusteredActionBean.curAction.getNode(), GranularityTool.getCommonRootNodeList(Global.granularity));
        }
        List<String> literals = new ArrayList<>();
        for (Action a : ce.clusteredActionBean.actions) {
            Tree t = (Tree) a.getNode();
            if (Global.astNodeUtil.isLiteralOrName(t)) {
                literals.add(t.getLabel());
            }
        }
        String methodName = null;
        String returnType = null;
        List<MyParameters> myParamList = new ArrayList<>();
        if (tree.getTreePrevOrCurr() == ChangeEntityDesc.TreeType.PREV_TREE_NODE) {
            Tree dstTree = (Tree) fp.getMappedCurrOfPrevNode(tree);
            if(dstTree!=null){
                Object mdDst =  dstTree.getNode();
                if (literals.contains(Global.astNodeUtil.getMethodName(mdDst))) {
                    methodName = Global.astNodeUtil.getMethodName(mdDst);
                }
            }
        }
        Object md =  tree.getNode();
        List<Object> params = Global.astNodeUtil.getSingleVariableDeclarations(md);
        if (params.size() != 0) {
            params = params.subList(1, params.size());
            for (Object svd : params) {
                if (literals.contains(Global.astNodeUtil.getSingleVariableDeclarationName(svd))) {
                    MyParameters myParameters = new MyParameters(Global.astNodeUtil.getSingleVariableDeclarationName(svd), Global.astNodeUtil.getSingleVariableDeclarationTypeName(svd));
                    myParamList.add(myParameters);
                }
            }
        }
        if (Global.astNodeUtil.getMethodType(md) != null && literals.contains(Global.astNodeUtil.getMethodType(md).toString())) {
            returnType = Global.astNodeUtil.getMethodType(md).toString();
        }
        int treeType = ChangeEntityDesc.optStringToTreeType(ce.stageIIBean.getOpt());
        ce.linkBean.defList.addDef(ce.stageIIBean.getCanonicalName().getLongName(), returnType, methodName, myParamList, ce.lineRange,treeType);


    }



}
