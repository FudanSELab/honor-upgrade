package edu.fdu.se.core.links.linkbean;

import com.github.gumtreediff.actions.model.Action;
import com.github.gumtreediff.actions.model.Move;
import com.github.gumtreediff.actions.model.Update;
import com.github.gumtreediff.tree.ITree;
import com.github.gumtreediff.tree.Tree;
import com.github.javaparser.printer.lexicalpreservation.changes.Change;
import edu.fdu.se.core.links.generator.LinkConstants;
import edu.fdu.se.core.miningactions.bean.MiningActionData;
import edu.fdu.se.core.miningactions.bean.MyRange;
import edu.fdu.se.core.miningchangeentity.base.ChangeEntity;
import edu.fdu.se.core.miningchangeentity.base.ChangeEntityDesc;
import edu.fdu.se.core.preprocessingfile.data.BodyDeclarationPair;
import edu.fdu.se.global.FilePairData;
import edu.fdu.se.global.Global;
import edu.fdu.se.core.miningchangeentity.base.StatementPlusChangeEntity;
import edu.fdu.se.core.preprocessingfile.data.PreCacheData;
import org.eclipse.jdt.core.dom.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by huangkaifeng on 2018/4/7.
 */
public class InitStmtBean {

    private List<String> defSymbols;

    private MiningActionData mMad;


    public void parse(ChangeEntity en, MiningActionData mad, boolean filterDef) {
        this.mMad = mad;
        if (filterDef) {
            defSymbols = new ArrayList<>();
            for (Def def : en.linkBean.defList.getDefs()) {
                defSymbols.add(def.varName);
            }
        }

        StatementPlusChangeEntity ce = (StatementPlusChangeEntity) en;
        if (ce.clusteredActionBean.curAction instanceof Move) {
            parseMove(ce, mad);
        } else {
            parseNonMove(ce, mad);
        }
        defSymbols = null;
        this.mMad = null;
    }

    protected List<Tree> getSimpleNameNodesFromRootChildren(Tree root) {
        List<Tree> simpleNames = new ArrayList<>();
        for (ITree tmp : root.preOrder()) {
            Tree t = (Tree) tmp;
            if (Global.astNodeUtil.isLiteralOrName(t)) {
                simpleNames.add(t);
            }
        }
        return simpleNames;
    }

    protected void parseMove(ChangeEntity ce, MiningActionData mad) {
        PreCacheData preCacheData = mad.preCacheData;
        Action a = ce.clusteredActionBean.curAction;
        Tree root = (Tree) a.getNode();
        List<Tree> simpleNames = getSimpleNameNodesFromRootChildren(root);
        List<ParseTuple> parseList = new ArrayList<>();
        addToMoveParseTuple(parseList, simpleNames);

        for (ParseTuple pt : parseList) {
            Tree tree = (Tree) pt.node;
            String value = pt.value;
            int treeType = pt.treeType;
            Object exp = pt.exp;
            if (Global.astNodeUtil.isMethodDeclaration(exp)) {
                return;
            }
            dispatchUseCase(ce, mad, preCacheData, treeType, tree, exp, value);

            if (Global.astNodeUtil.isMethodInvocation(exp)) {
                if (isMethodInvocationName(exp, tree.getLabel())) {
                    parseMethodInvocationToMethodUse(ce, exp, (ASTNode) tree.getNode(), mad, value, ChangeEntityDesc.TreeType.BOTH);
                }
            } else {
                dispatchUseCase(ce, mad, preCacheData, ChangeEntityDesc.TreeType.BOTH, tree, exp, value);
            }
        }
    }

    protected void parseNonMove(ChangeEntity ce, MiningActionData mad) {
        List<ParseTuple> parseList = new ArrayList<>();
        PreCacheData preCacheData = mad.preCacheData;
        List<Action> actions = ce.clusteredActionBean.actions;
        addToParseTuple(parseList, actions);
        addAdditionalMoveUpdateLabelToParseTuple(parseList, ce, mad);

        for (ParseTuple pt : parseList) {
            Tree tree = (Tree) pt.node;
            String value = pt.value;
            int treeType = pt.treeType;
            Object exp = pt.exp;
            if (Global.astNodeUtil.isMethodDeclaration(exp)) {
                return;
            }
            dispatchUseCase(ce, mad, preCacheData, treeType, tree, exp, value);
        }
    }

    /**
     * non move
     *
     * @param parseList
     * @param actions
     */
    private void addToParseTuple(List<ParseTuple> parseList, List<Action> actions) {
        // cluster parse list
        for (Action a : actions) {
            Tree tree = (Tree) a.getNode();
            if (!Global.astNodeUtil.isLiteralOrName(tree)) {
                continue;
            }
            Tree parent = (Tree) tree.getParent();
            if (parent.getAstClass() == SimpleType.class) {
                // def type
                Tree pParent = (Tree)parent.getParent();
                if(pParent.getAstClass() != ClassInstanceCreation.class){
                    continue;
                }
            }
            String value;
            Object exp;
            int treeType;
            if (a instanceof Update) {
                value = ((Update) a).getValue();
                Tree dstNode = (Tree) Global.mad.getMappedCurrOfPrevNode(tree);
                if (dstNode == null) {
                    continue;
                }
                tree = dstNode;
                exp = Global.astNodeUtil.searchBottomUpFindNoneSimpleNameOrLiteralExpression(tree.getNode());
                treeType = ChangeEntityDesc.TreeType.CURR_TREE_NODE;
                if (exp instanceof VariableDeclarationFragment) {
                    continue;
                }
            } else {
                value = tree.getLabel();
                exp = Global.astNodeUtil.searchBottomUpFindNoneSimpleNameOrLiteralExpression(tree.getNode());
                treeType = ChangeEntityDesc.optStringToTreeType(a.getClass());
            }
            if (exp != null) {
                ParseTuple pt = new ParseTuple(tree, treeType, value, exp);
                parseList.add(pt);
            }
        }

    }

    /**
     * Move
     *
     * @param parseList
     * @param trees
     */
    private void addToMoveParseTuple(List<ParseTuple> parseList, List<Tree> trees) {
        // cluster parse list
        for (Tree a : trees) {
            Tree t = a;
            if (!Global.astNodeUtil.isLiteralOrName(t)) {
                continue;
            }
            Object exp = Global.astNodeUtil.searchBottomUpFindNoneSimpleNameOrLiteralExpression(t.getNode());
            if (exp != null) {
                ParseTuple pt = new ParseTuple(t, t.getType(), t.getLabel(), exp);
                parseList.add(pt);
            }
        }

    }

    private void addAdditionalMoveUpdateLabelToParseTuple(List<ParseTuple> parseList, ChangeEntity ce, MiningActionData mad) {
        // additional parseList
        if (ChangeEntityDesc.StageIIOpt.OPT_INSERT.equals(ce.getStageIIBean().getOpt()) || ChangeEntityDesc.StageIIOpt.OPT_CHANGE.equals(ce.stageIIBean.getOpt())) {
            Tree fafather = ce.getClusteredActionBean().fafather;
            Iterable<ITree> children = fafather.preOrder();
            List<ITree> moveOrUpdateNode = new ArrayList<>();
            moveOrUpdateNode.addAll(mad.getMoveDstNodes());
            moveOrUpdateNode.addAll(mad.getUpdateDstNodes());
            for (ITree t : children) {
                if (moveOrUpdateNode.contains(t)) {
                    Tree tt = (Tree) t;
                    Object exp = Global.astNodeUtil.searchBottomUpFindNoneSimpleNameOrLiteralExpression(tt.getNode());
                    ParseTuple pt = new ParseTuple(t, ChangeEntityDesc.TreeType.CURR_TREE_NODE, tt.getLabel(), exp);
                    parseList.add(pt);
                }
            }

        }

    }

    /**
     * @param ce
     * @param mad
     * @param preCacheData
     * @param treeType
     * @param tree
     * @param exp
     * @param value
     */
    private void dispatchUseCase(ChangeEntity ce, MiningActionData mad, PreCacheData preCacheData, int treeType, Tree tree, Object exp, String value) {
        if (exp != null && Global.astNodeUtil.isMethodInvocation(exp) && isMethodInvocationName(exp, value)) {
            //method invocation
            parseMethodInvocationToMethodUse(ce, exp, (ASTNode) tree.getNode(), mad, value, treeType);
            return;
        }
        if (exp != null && Global.astNodeUtil.isClassInstanceCreation(exp) && isClassCreationName(exp, value)) {
            //class creation
            addUse(ce, value, LinkConstants.USE_CLASS_CREATION, treeType, null, null);
            return;
        }
        if(addFieldUse(ce,preCacheData,value,exp,treeType,mad)){
            return;
        }
        // local var
        addUse(ce, value, LinkConstants.USE_LOCAL_VAR, treeType, tree, mad);
    }

    private boolean addFieldUse(ChangeEntity ce,PreCacheData preCacheData,String value,Object exp,int treeType,MiningActionData mad){
        if (exp != null && Global.astNodeUtil.isASTNodeSameAsClass(exp, QualifiedName.class)) {
            QualifiedName qn = (QualifiedName) exp;
            String subValue = qn.getName().toString();
            String qualifier = qn.getQualifier().toString();
            if (isQualifierClassName(qualifier)) {
                Use use = addUse(ce, subValue, LinkConstants.USE_FIELD, treeType, null, null);
                if(use == null){
                    return false;
                }
                use.qualifier = qualifier;
                return true;
            }
        }
        if (preCacheData.getPrevCurrFieldNames().contains(value)) {
            //use field
            int resV = checkFieldAccess(exp);
            if (resV > 0) {
                if (resV == 1) {
                    addUse(ce, value, LinkConstants.USE_FIELD_LOCAL, treeType, null, null);
                } else {
                    addUse(ce, value, LinkConstants.USE_FIELD, treeType, null, null);
                }
                return true;
            } else if (!isValueMethodParam(mad, value, ce)) {
                //如果不是方法的参数
                addUse(ce, value, LinkConstants.USE_FIELD, treeType, null, null);
                return true;
            }
        } else {
            if(addStaticFieldUse(preCacheData,value,ce,treeType)){
                return true;
            }
        }
        return false;

    }

    private boolean isQualifierClassName(String s){
        List<FilePairData> list = Global.filePairDatas;
        for(FilePairData fpd:list){
            if(fpd.getFileShortName().equals(s+".java")){
                return true;
            }
        }
        return false;
    }


    private boolean addStaticFieldUse(PreCacheData preCacheData,String value,ChangeEntity ce,int treeType){
        for (String staticImport : preCacheData.getStaticImports()) {
            int index = staticImport.lastIndexOf(".");
            String name = staticImport.substring(index + 1);
            if (value.equals(name)) {
                ce.linkBean.useList.addStaticFieldUse(ce.stageIIBean.getCanonicalName().getLongName(), ce.getLineRange(), value, treeType, staticImport);
                return true;
            }
        }
        return false;

    }


    private Use addUse(ChangeEntity ce, String value, int useType, int treeType, Tree tree, MiningActionData mad) {
        if (this.defSymbols != null && this.defSymbols.contains(value)) {
            return null;
        }
        Use use =  ce.linkBean.useList.addUse(ce.stageIIBean.getCanonicalName().getLongName(), ce.getLineRange(), value, useType, treeType, tree, mad);
        return use;
    }

    private boolean isValueMethodParam(MiningActionData mad, String value, ChangeEntity ce) {
        BodyDeclarationPair bodyDeclarationPair = mad.preCacheData.getEntityTree().getChangeEntityBelongedBodyDeclarationPair(ce);
        if (bodyDeclarationPair != null) {
            return mad.preCacheData.getEntityTree().isValueMethodParam(bodyDeclarationPair, value);
        }
        return false;
    }

    private int checkFieldAccess(Object exp) {
        if (exp instanceof FieldAccess) {
            FieldAccess fieldAccess = (FieldAccess) exp;
            Expression expression = fieldAccess.getExpression();
            if (expression instanceof ThisExpression) {
                return 1;
            }
            return 2;
        } else if (exp instanceof SuperFieldAccess) {
            return 3;
        }
        return 0;
    }


    private boolean isClassCreationName(Object classInstanceCreation, String clazzName) {
        String clazz = Global.astNodeUtil.getClassCreationName(classInstanceCreation);
        if (clazzName.equals(clazz)) {
            return true;
        }
        return false;
    }


    private boolean isMethodInvocationName(Object methodInvocation, String methodName) {
        String methodName1 = Global.astNodeUtil.getMethodInvocationName(methodInvocation);
        if (methodName.equals(methodName1)) {
            return true;
        }
        return false;

    }

    /**
     * @param ce
     * @param exp
     * @param treeNode if Update treeNode is in dst
     * @param mad
     * @param value
     * @param treeType
     */
    private void parseMethodInvocationToMethodUse(ChangeEntity ce, Object exp, ASTNode treeNode, MiningActionData mad, String value, int treeType) {
        List<String> data = Global.astNodeUtil.getMethodInvocationVarAndName(exp);
        MethodUse use = ce.linkBean.useList.addMethodInvocationUse(ce.stageIIBean.getCanonicalName().getLongName(), ce.getLineRange(), value, treeType, data.get(0));
        if (use == null) {
            return;
        }
        use.addToParamList(data);
        if (!"unknown".equals(data.get(0)) && data.get(0) != null && use != null) {
            String[] data2 = Global.astNodeUtil.resolveTypeOfVariable(data.get(0), treeNode, mad);
            use.setTypeNameAndUseType(data2);
        }


    }

}
