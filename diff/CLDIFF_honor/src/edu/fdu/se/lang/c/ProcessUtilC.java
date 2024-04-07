package edu.fdu.se.lang.c;

import com.github.gumtreediff.actions.model.Action;
import com.github.gumtreediff.tree.Tree;
import edu.fdu.se.core.miningchangeentity.base.MemberPlusChangeEntity;
import edu.fdu.se.core.miningchangeentity.member.ClassChangeEntity;
import edu.fdu.se.core.miningchangeentity.member.FieldChangeEntity;
import edu.fdu.se.core.miningchangeentity.member.MethodChangeEntity;
import edu.fdu.se.core.miningchangeentity.statement.VariableChangeEntity;
import edu.fdu.se.global.Constants;
import edu.fdu.se.global.Global;
import edu.fdu.se.core.miningactions.bean.MiningActionData;
import edu.fdu.se.core.miningactions.statement.*;
import edu.fdu.se.core.miningchangeentity.base.ChangeEntity;
import edu.fdu.se.core.miningchangeentity.base.ChangeEntityDesc;
import edu.fdu.se.core.preprocessingfile.FilePairPreDiff;
import edu.fdu.se.core.preprocessingfile.data.PreCacheData;
import edu.fdu.se.core.preprocessingfile.data.PreCacheTmpData;
import edu.fdu.se.lang.c.generatingactions.CParserVisitor;
import edu.fdu.se.lang.c.preprocess.TypeNodesTraversalC;
import edu.fdu.se.lang.common.ProcessUtil;
import org.eclipse.cdt.core.dom.ast.*;
import org.eclipse.cdt.internal.core.dom.parser.cpp.*;
import org.eclipse.cdt.internal.core.dom.rewrite.commenthandler.ASTCommenter;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;


import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public class ProcessUtilC implements ProcessUtil {


    @Override
    public void initDefs(ChangeEntity ce, MiningActionData mad) {

    }

    @Override
    public void removeAllComments(PreCacheTmpData tempData, Object o, List<Integer> lineList, int treeType,PreCacheData preCacheData) {
        IASTTranslationUnit cu = (IASTTranslationUnit) o;
        //将 using 指令 加入到 RemoveList
        for (IASTNode item : cu.getChildren()) {
            if (item instanceof CPPASTUsingDirective) {
                tempData.addToRemoveList(item, treeType);
            }
        }

        //加入comment
        List<IASTComment> commentList = Arrays.asList(cu.getComments());
        for (int i = commentList.size() - 1; i >= 0; i--) {
            commentList.get(i).setComment(null);
            tempData.addToRemoveList(commentList.get(i), treeType);
        }
        //加入include
        List<IASTNode> includes = Arrays.asList(cu.getIncludeDirectives());
        for (int i = includes.size() - 1; i >= 0; i--) {
            tempData.addToRemoveList(includes.get(i), treeType);
        }
        removeRemovalList(tempData, cu, lineList, treeType, preCacheData);
    }


    @Override
    public String bodyDeclarationToString(Object o) {
        IASTNode node = (IASTNode) o;
        return node.getRawSignature().toString();
    }

    @Override
    public Map<Integer, String> filterByGranularity(String granularity, Map<Integer, String> mMap) {
        Set<Integer> keys = mMap.keySet();
        int[] elements = Global.iLookupTbl.astNodeMap.get(granularity);
        Map<Integer, String> res = new HashMap<>();
        for (int a : elements) {
            if (keys.contains(a)) {
                res.put(a, mMap.get(a));
            }
        }
        return res;
    }

    @Override
    public void matchEntityBottomUpNew(MiningActionData fp, Action a, Tree queryFather, int treeType, Tree traverseFather, List<Integer> commonRootNodeTypes) {
        int nodeType = Global.astNodeUtil.getNodeTypeId(traverseFather.getNode());
        Map<Integer, String> mMap = Global.iLookupTbl.callTable.get("match_bottom_up_new_entity");

        if (mMap.containsKey(nodeType)) {
            String call = mMap.get(nodeType);
            int index = call.lastIndexOf(".");
            String className = call.substring(0, index);
            String methodName = call.substring(index + 1);
            try {
                Class<?> printClass = Class.forName(className);
                Method printMethod = printClass.getMethod(methodName, MiningActionData.class, Action.class, Tree.class, int.class, Tree.class);
                printMethod.invoke(printClass.newInstance(), fp, a, queryFather, treeType, traverseFather);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }


    @Override
    public void matchEntityBottomUpCurr(MiningActionData fp, Action a, ChangeEntity changeEntity, int nodeType, Tree traverseFather) {
        Map<Integer, String> mMap = Global.iLookupTbl.callTable.get("match_bottom_up_curr_entity");
        if (mMap.containsKey(nodeType)) {
            String call = mMap.get(nodeType);
            int index = call.lastIndexOf(".");
            String className = call.substring(0, index);
            String methodName = call.substring(index + 1);
            try {
                Class<?> printClass = Class.forName(className);
                Method printMethod = printClass.getMethod(methodName, MiningActionData.class, Action.class, ChangeEntity.class, Tree.class);
                printMethod.invoke(printClass.newInstance(), fp, a, changeEntity, traverseFather);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    public int matchEntityTopDown(MiningActionData fp, Action a, int nodeType, String granularity) {
        int res = 0;
        Map<Integer, String> mMap = Global.iLookupTbl.callTable.get(Constants.MATCH_TOP_DOWN);
        if (mMap.containsKey(nodeType)) {
            String call = mMap.get(nodeType);
            //String[] data = call.split("\\.");
//            String className = data[0];
//            String methodName = data[1];
            String className = call.substring(0, call.lastIndexOf('.'));
            String methodName = call.substring(call.lastIndexOf('.') + 1);
            try {
                Class<?> printClass = Class.forName(className);
                Method printMethod = printClass.getMethod(methodName, MiningActionData.class, Action.class);
                printMethod.invoke(printClass.newInstance(), fp, a);
            } catch (InvocationTargetException e) {
                Throwable cause = e.getCause();
                cause.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return res;
    }

    @Override
    public void matchBlock(MiningActionData fp, Action a, int type, Tree fatherNode) {
        switch (type) {
            case CParserVisitor.AST_SWITCH_STATEMENT:
//                MatchSwitch.matchSwitchCaseNewEntity(mad,a);
                fp.setActionTraversedMap(a);
                break;
            case CParserVisitor.AST_IF_STATEMENT:
                //Pattern 1.2 Match else
                if (fatherNode.getChildPosition(a.getNode()) == 2) {
                    new MatchIfElse().matchElseTopDown(fp, a);
                }
                fp.setActionTraversedMap(a);
                break;
            case CParserVisitor.CPPAST_TRY_BLOCK_STATEMENT:
                ////Finally块
                if (fatherNode.getChildPosition(a.getNode()) == fatherNode.getChildren().size() - 1) {
                    new MatchTry().matchFinallyTopDown(fp, a);
                }
                fp.setActionTraversedMap(a);
                break;
            default:
                fp.setActionTraversedMap(a);
                break;
        }
    }


    private void removeRemovalList(PreCacheTmpData tempData, IASTTranslationUnit iu, List<Integer> lineList,
                                   int treeType, PreCacheData preCacheData) {
        List<Object> nodesToBeRemoved;
        if (ChangeEntityDesc.TreeType.PREV_TREE_NODE == treeType) {
            nodesToBeRemoved = tempData.prevRemovalNodes;
        } else if (ChangeEntityDesc.TreeType.CURR_TREE_NODE == treeType) {
            nodesToBeRemoved = tempData.currRemovalNodes;
        } else {
            return;
        }
        for (Object o : nodesToBeRemoved) {
            IASTNode item = (IASTNode) o;
            tempData.setLinesFlag(lineList, item.getFileLocation().getStartingLineNumber(),
                    item.getFileLocation().getEndingLineNumber());
            preCacheData.addDeletionKey(item, item.getParent());

            //todo 想办法删除item


        }
        nodesToBeRemoved.clear();
    }


    private void removeRemovalList(PreCacheTmpData tempData, IASTTranslationUnit cu, List<Integer> lineList, int treeType) {
        List<Object> nodesToBeRemoved = null;
        if (ChangeEntityDesc.TreeType.PREV_TREE_NODE == treeType) {
            nodesToBeRemoved = tempData.prevRemovalNodes;
        } else if (ChangeEntityDesc.TreeType.CURR_TREE_NODE
                == treeType) {
            nodesToBeRemoved = tempData.currRemovalNodes;

        }
        for (Object o : nodesToBeRemoved) {
            IASTNode item = (IASTNode) o;
            IASTFileLocation f = null;
            if (item.getFileLocation().getEndingLineNumber() > 355) {
                f = item.getFileLocation();
            }
            if (item.getFileLocation().getFileName().replace("\\", "/").equals(Global.repository + Constants.SaveFilePath.CURR + "/" + Global.fileShortName)) {
                tempData.setLinesFlag(lineList, item.getFileLocation().getStartingLineNumber(),
                        item.getFileLocation().getEndingLineNumber());
            }
        }
    }


    /**
     * 由于C中不能以 Class 作为基本遍历单位，所以设置一个虚拟的 Class 来代表一个文件的内容
     * 这个虚拟的根节点在这里就是 cu
     */
    @Override
    public int compareTwoFile(FilePairPreDiff preDiff, PreCacheTmpData tempData, PreCacheData preCacheData) {
        if(preCacheData.getPrevCu() == null || preCacheData.getCurrCu() == null){
            return -1;
        }
        CPPASTTranslationUnit cuPrev = (CPPASTTranslationUnit) preCacheData.getPrevCu();
        CPPASTTranslationUnit cuCurr = (CPPASTTranslationUnit) preCacheData.getCurrCu();
        compare(cuPrev, cuCurr, cuPrev, cuCurr, preCacheData, tempData, preDiff);
        return 0;
    }

    /**
     * @param cuPrev Pre文件的 cu class
     * @param cuCurr Curr文件的 cu class
     * @param tdPrev Pre文件的虚拟节点
     * @param tdCurr Curr文件的虚拟节点
     */
    private void compare(CPPASTTranslationUnit cuPrev, CPPASTTranslationUnit cuCurr, IASTNode tdPrev,
                         IASTNode tdCurr, PreCacheData preCacheData, PreCacheTmpData tempData, FilePairPreDiff preDiff) {
        TypeNodesTraversalC astTraversal = new TypeNodesTraversalC();
        astTraversal.traversePrevFakeTypeDeclarationInit(preCacheData, tempData, tdPrev, "^",true);
        astTraversal.traverseCurrTypeDeclarationComparePrev(preCacheData, tempData, tdCurr,"^");
        preDiff.iterateVisitingMap();
        preDiff.undeleteSignatureChange();
        sameFieldNodeToCache(tempData,preCacheData);
        //tode 暂时不可用
        //removeRemovalList(tempData, cuPrev, preCacheData.getPrevLineNums(), ChangeEntityDesc.TreeType.PREV_TREE_NODE,preCacheData);
        //removeRemovalList(tempData, cuCurr, preCacheData.getCurrLineNums(), ChangeEntityDesc.TreeType.CURR_TREE_NODE,preCacheData);
        preDiff.iterateVisitingMap2LoadContainerMap();
    }


    private void sameFieldNodeToCache(PreCacheTmpData preCacheTmpData,PreCacheData preCacheData){
        List<Object> currList = new ArrayList<>();
        List<Object> prevList = new ArrayList<>();
        preCacheData.setmCurrFields(currList);
        preCacheData.setmPrevFields(prevList);
        for(Object o :preCacheTmpData.currRemovalNodes){
            if(o instanceof CPPASTSimpleDeclaration && ((CPPASTSimpleDeclaration)o).getDeclSpecifier() instanceof CPPASTSimpleDeclSpecifier){
                currList.add(o);
            }
        }
        for(Object o :preCacheTmpData.prevRemovalNodes){
            if(o instanceof CPPASTSimpleDeclaration && ((CPPASTSimpleDeclaration)o).getDeclSpecifier() instanceof CPPASTSimpleDeclSpecifier){
                prevList.add(o);
            }
        }
    }

    public void handleMethodNameModification(ChangeEntity changeEntity,ChangeEntity tempCE){
        if(((MethodChangeEntity) tempCE).bodyDeclarationPair != null && ((MethodChangeEntity) changeEntity).bodyDeclarationPair!= null){
            CPPASTFunctionDefinition bdTmp = (CPPASTFunctionDefinition)((MethodChangeEntity) tempCE).bodyDeclarationPair.getBodyDeclaration();
            CPPASTFunctionDefinition bdCe = (CPPASTFunctionDefinition)((MethodChangeEntity) changeEntity).bodyDeclarationPair.getBodyDeclaration();
            if(compareMethodBody(bdCe,bdTmp)){
                if(changeEntity.getFrontData().getFile().equals("dst")){
                    changeEntity.getFrontData().setMethodFrom(bdTmp);
                    tempCE.getFrontData().setMethodTo(bdCe);
                }
                else {
                    changeEntity.getFrontData().setMethodTo(bdTmp);
                    tempCE.getFrontData().setMethodFrom(bdCe);
                }
            }
        }
    };

    public static boolean compareMethodBody(CPPASTFunctionDefinition md1,CPPASTFunctionDefinition md2){
        if(md1.getBody() != null && md2.getBody() != null)
            return md1.getBody().toString().hashCode()==md2.getBody().toString().hashCode();
        else
            return false;
    }

    public String methodDeclarationToString(Object method){
        CPPASTFunctionDefinition methodDeclaration = (CPPASTFunctionDefinition) method;
        StringBuilder sb = new StringBuilder();
        String methodName = methodDeclaration.getDeclarator().getName().toString();
        sb.append(methodName);
        List<?> pList = Arrays.stream(((CPPASTFunctionDeclarator)(methodDeclaration.getDeclarator())).getParameters()).toList();
        Iterator<?> iter2 = pList.iterator();
        sb.append("(");

        while (iter2.hasNext()){
            // short param
            CPPASTParameterDeclaration var = (CPPASTParameterDeclaration) iter2.next();
            String varString = var.getDeclarator().getName().toString();
            int index = varString.lastIndexOf(".");
            sb.append(varString.substring(index+1));
            sb.append(",");
        }
        if(pList.size()!=0){
            sb.deleteCharAt(sb.length()-1);
        }
        sb.append(")");
        return sb.toString();
    }


    @Override
    public void addMethodDef(ChangeEntity ce, Object methodDeclaration) {

    }

    @Override
    public void addFieldDef(ChangeEntity ce, Object fd) {

    }

    @Override
    public void addVarDeclarationDef(ChangeEntity ce, Object vds) {

    }

    @Override
    public void initInheritance(ChangeEntity ce, Object td) {

    }


}
