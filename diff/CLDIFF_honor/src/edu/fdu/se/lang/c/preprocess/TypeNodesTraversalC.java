package edu.fdu.se.lang.c.preprocess;

import edu.fdu.se.global.Constants;
import edu.fdu.se.global.Global;
import edu.fdu.se.core.miningchangeentity.base.ChangeEntityDesc;
import edu.fdu.se.core.preprocessingfile.data.BodyDeclarationPair;
import edu.fdu.se.core.preprocessingfile.data.PreCacheData;
import edu.fdu.se.core.preprocessingfile.data.PreCacheTmpData;
import edu.fdu.se.lang.c.CUtil;
import edu.fdu.se.lang.common.preprocess.TypeNodesTraversal;
import org.eclipse.cdt.core.dom.ast.*;
import org.eclipse.cdt.internal.core.dom.parser.cpp.*;

import java.util.Arrays;
import java.util.List;

/**
 * Created by huangkaifeng on 2018/3/12.
 */
public class TypeNodesTraversalC implements TypeNodesTraversal {

    private CurrBodyCheckC currBodyCheck;

    public TypeNodesTraversalC() {
        currBodyCheck = new CurrBodyCheckC();
    }

    private boolean isMacro(IASTNode node) {
        if (node.getFileLocation() == null)
            return false;
        if (!node.getFileLocation().getFileName().replace("\\", "/").equals(Global.repository
                + Constants.SaveFilePath.PREV + "/" + Global.fileShortName)
                && !node.getFileLocation().getFileName().replace("\\", "/")
                .equals(Global.repository + Constants.SaveFilePath.CURR + "/" + Global.fileShortName))
            return true;
        return false;
    }

    /**
     * 遍历curr中类内部的信息，与prev进行比较
     *
     * @param cod             class 节点，c children
     *
     */
    public void traverseCurrTypeDeclarationComparePrev(PreCacheData compareResult, PreCacheTmpData compareCache, IASTNode cod, String prefixClassName) {
        //虚拟节点不用检查状态码，因为一个文件只有一个虚拟类，而虚拟类必然是对应的
        for (IASTNode iastNode : cod.getChildren()) {
            if (iastNode instanceof CPPASTSimpleDeclaration) {
                IASTDeclSpecifier iastDeclSpecifier = ((IASTSimpleDeclaration) iastNode).getDeclSpecifier();
                if (iastDeclSpecifier instanceof CPPASTCompositeTypeSpecifier) {
                    String uniqueKey = Global.astNodeUtil.getBodyDeclarationUniqueKey(iastNode);
                    currBodyCheck.checkTypeDeclarationInCurr(compareResult, compareCache, iastNode, prefixClassName,uniqueKey);
                    checkTypeDeclaration(compareResult, compareCache, iastNode,prefixClassName+uniqueKey);
                    continue;
                }
                if (iastDeclSpecifier instanceof IASTEnumerationSpecifier) {
                    //String uniqueKey =  prefixClassName + Global.astNodeUtil.getBodyDeclarationUniqueKey(iastNode) + ".";
                    currBodyCheck.checkEnumDeclarationInCurr(compareResult, compareCache, iastNode, prefixClassName);
                    continue;
                }
                if(iastDeclSpecifier instanceof CPPASTElaboratedTypeSpecifier){
                    currBodyCheck.checkElaboratedTypeDeclarationInCurr(compareResult, compareCache, iastNode, prefixClassName);
                    continue;
                }
                //if (iastNode instanceof CPPASTSimpleDeclaration)
                //String uniqueKey =  Global.astNodeUtil.getBodyDeclarationUniqueKey(iastNode);
                //if(iastDeclSpecifier instanceof  CPPASTF)
                //TODO declarator与specifier同时为空，直接跳过
                if( ((IASTSimpleDeclaration) iastNode).getDeclarators().length == 0)continue;
                IASTDeclarator declarator = ((IASTSimpleDeclaration) iastNode).getDeclarators()[0];
                if(declarator instanceof CPPASTDeclarator){
                    currBodyCheck.checkFieldDeclarationInCurr(compareResult, compareCache, iastNode, prefixClassName);
                    continue;
                }
                if(declarator instanceof CPPASTFunctionDeclarator){
                    currBodyCheck.checkMethodDeclarationOrInitializerInCurr(compareResult, compareCache, iastNode, prefixClassName);
                    continue;
                }
            }
            if (iastNode instanceof CPPASTFunctionDefinition functionDefinition) {
                //String uniqueKey = Global.astNodeUtil.getBodyDeclarationUniqueKey(iastNode);
                currBodyCheck.checkMethodDeclarationOrInitializerInCurr(compareResult, compareCache, iastNode, prefixClassName);
            }
            if( iastNode instanceof CPPASTNamespaceDefinition){
                String uniqueKey = Global.astNodeUtil.getBodyDeclarationUniqueKey(iastNode);
                currBodyCheck.checkNamespaceInCurr(compareResult, compareCache, iastNode, prefixClassName, uniqueKey);
                checkNamespace(compareResult, compareCache, iastNode, prefixClassName + uniqueKey);
            }
        }

    }

    private void checkTypeDeclaration(PreCacheData compareResult, PreCacheTmpData compareCache, IASTNode cod, String prefixClassName) {
        prefixClassName += prefixClassName.equals("^") ? "" : ".";
        for (IASTNode iastNode : ((CPPASTSimpleDeclaration) cod).getDeclSpecifier().getChildren()) {
            if (iastNode instanceof CPPASTSimpleDeclaration) {
                IASTDeclSpecifier iastDeclSpecifier = ((IASTSimpleDeclaration) iastNode).getDeclSpecifier();
                if (iastDeclSpecifier instanceof CPPASTCompositeTypeSpecifier) {
                    String uniqueKey = Global.astNodeUtil.getBodyDeclarationUniqueKey(iastNode);
                    currBodyCheck.checkTypeDeclarationInCurr(compareResult, compareCache, iastNode, prefixClassName,uniqueKey);
                    checkTypeDeclaration(compareResult, compareCache, iastNode, prefixClassName + uniqueKey);
                    continue;
                }
                if (iastDeclSpecifier instanceof IASTEnumerationSpecifier) {
                    currBodyCheck.checkEnumDeclarationInCurr(compareResult, compareCache, iastNode, prefixClassName);
                    continue;
                }
                if (iastDeclSpecifier instanceof CPPASTElaboratedTypeSpecifier){
                    currBodyCheck.checkElaboratedTypeDeclarationInCurr(compareResult, compareCache, iastNode, prefixClassName);
                    continue;
                }
                //TODO declarator与specifier同时为空，直接跳过
                if( ((IASTSimpleDeclaration) iastNode).getDeclarators().length == 0)continue;
                IASTDeclarator declarator = ((IASTSimpleDeclaration) iastNode).getDeclarators()[0];
                if(declarator instanceof CPPASTFunctionDeclarator) {
                    currBodyCheck.checkMethodDeclarationOrInitializerInCurr(compareResult, compareCache, iastNode, prefixClassName);
                    continue;
                }
                if(declarator instanceof CPPASTDeclarator) {
                    currBodyCheck.checkFieldDeclarationInCurr(compareResult, compareCache, iastNode, prefixClassName);
                    continue;
                }
            }
            if(iastNode instanceof CPPASTFunctionDefinition) {
                currBodyCheck.checkMethodDeclarationOrInitializerInCurr(compareResult, compareCache, iastNode, prefixClassName);
                continue;
            }
        }
    }

    /**
     * namespace
     */
    private void checkNamespace(PreCacheData compareResult, PreCacheTmpData compareCache, IASTNode cod, String prefixClassName){
        prefixClassName += ".";
        for(IASTNode iastNode : cod.getChildren()){
            if(iastNode instanceof CPPASTSimpleDeclaration){
                IASTDeclSpecifier iastDeclSpecifier = ((IASTSimpleDeclaration) iastNode).getDeclSpecifier();
                if (iastDeclSpecifier instanceof CPPASTCompositeTypeSpecifier) {
                    String uniqueKey = Global.astNodeUtil.getBodyDeclarationUniqueKey(iastNode);
                    currBodyCheck.checkTypeDeclarationInCurr(compareResult, compareCache, iastNode, prefixClassName,uniqueKey);
                    checkTypeDeclaration(compareResult, compareCache, iastNode,prefixClassName + uniqueKey);
                    continue;
                }//struct、class、union
                if (iastDeclSpecifier instanceof IASTEnumerationSpecifier) {
                    currBodyCheck.checkEnumDeclarationInCurr(compareResult, compareCache, iastNode, prefixClassName);
                    continue;
                }//枚举
                //TODO 预防一些莫名出现的空声明节点，之后看看有没有别的方法
                if(((IASTSimpleDeclaration) iastNode).getDeclarators().length == 0)continue;
                IASTDeclarator declarator = ((IASTSimpleDeclaration) iastNode).getDeclarators()[0];
                if(declarator instanceof CPPASTFunctionDeclarator) {
                    currBodyCheck.checkMethodDeclarationOrInitializerInCurr(compareResult, compareCache, iastNode, prefixClassName);
                    continue;
                }//函数声明
                if(declarator instanceof CPPASTDeclarator) {
                    currBodyCheck.checkFieldDeclarationInCurr(compareResult, compareCache, iastNode, prefixClassName);
                    continue;
                }//变量声明
            }//函数实现
            if(iastNode instanceof CPPASTFunctionDefinition){
                currBodyCheck.checkMethodDeclarationOrInitializerInCurr(compareResult, compareCache, iastNode, prefixClassName);
                continue;
            }
            //namespace可以嵌套
            if(iastNode instanceof CPPASTNamespaceDefinition){
                String uniqueKey = Global.astNodeUtil.getBodyDeclarationUniqueKey(iastNode);
                currBodyCheck.checkNamespaceInCurr(compareResult, compareCache, iastNode, prefixClassName, uniqueKey);
                checkNamespace(compareResult, compareCache, iastNode, prefixClassName + uniqueKey);

            }
        }
    }

    /**
     * 设置该cod下的孩子节点为访问，因为father已经被remove了，所以不需要remove
     *
     * @param prevBody        该节点 prev Body
     * @param prefixClassName 该节点为止的preix ClassName
     */
    @Override
    public void traverseTypeDeclarationSetVisited(PreCacheTmpData compareCache, Object prevBody, String prefixClassName) {
        IASTNode cod = (IASTNode) prevBody;
        List<IASTNode> tmpList = null;
        if (cod instanceof IASTTranslationUnit) {             //最外部
            tmpList = Arrays.asList(((IASTTranslationUnit) cod).getChildren());
        } else if (cod instanceof IASTSimpleDeclaration && ((IASTSimpleDeclaration) cod).getDeclSpecifier() instanceof IASTCompositeTypeSpecifier) {      //class,struct
            tmpList = Arrays.asList(((IASTCompositeTypeSpecifier) ((IASTSimpleDeclaration) cod).getDeclSpecifier()).getMembers());
        }

        assert (tmpList != null);
        for (int m = tmpList.size() - 1; m >= 0; m--) {
            IASTNode n = tmpList.get(m);
            if (CUtil.isTypeDeclaration(n)) {
                IASTSimpleDeclaration next = (IASTSimpleDeclaration) n;
                String name = CUtil.getTypeName(n);
                int type = ((IASTCompositeTypeSpecifier) (next.getDeclSpecifier())).getKey();
                String curName = prefixClassName + (type == 3 ? "class:" : "struct:") + name + ".";
                BodyDeclarationPair bdp = new BodyDeclarationPair(n, curName, ChangeEntityDesc.TreeType.PREV_TREE_NODE);
                int value = compareCache.getVisitingMapValueByKey(bdp);
                if (value != PreCacheTmpData.BODY_NULL) {
                    compareCache.addToPrevBodyVisitingMap(bdp, PreCacheTmpData.BODY_FATHERNODE_REMOVE);
                }
                traverseTypeDeclarationSetVisited(compareCache, next, curName);
            } else {
                BodyDeclarationPair bdp = new BodyDeclarationPair(n, prefixClassName, ChangeEntityDesc.TreeType.PREV_TREE_NODE);
                int value = compareCache.getVisitingMapValueByKey(bdp);
                if (value != PreCacheTmpData.BODY_NULL) {
                    compareCache.addToPrevBodyVisitingMap(bdp, PreCacheTmpData.BODY_FATHERNODE_REMOVE);
                }
            }
        }
    }

    public void traversePrevFakeTypeDeclarationInit(PreCacheData compareResult, PreCacheTmpData compareCache,
                                                    IASTNode iastNode,String prefixClassName, boolean isFake) {
        IASTNode[] nodeArray;
        //虚拟节点不需要装入
        if (!isFake) {
            if(iastNode instanceof CPPASTSimpleDeclaration){
                nodeArray = ((CPPASTSimpleDeclaration)iastNode).getDeclSpecifier().getChildren();
                //避免namespace中类、struct的name重复两次
                int lastIndex = prefixClassName.lastIndexOf(".");
                String prefix = lastIndex == -1 ? prefixClassName : prefixClassName.substring(0 ,prefixClassName.lastIndexOf(".") + 1);
                BodyDeclarationPair bodyDeclarationPair = new BodyDeclarationPair(iastNode, prefix, ChangeEntityDesc.TreeType.PREV_TREE_NODE);
                compareCache.addToPrevNodeBodyNameMap(bodyDeclarationPair.getCanonicalName().getLongName(), bodyDeclarationPair);
                compareCache.initPrevBodyVisitingMap(bodyDeclarationPair);
            }
            else{
                //namespace不算simple declaration,nodeArray第一个会是astName就是那么namespace的名字，不用管它
                nodeArray = iastNode.getChildren();
            }
        } else {
            nodeArray = iastNode.getChildren();
        }

        for (IASTNode node : nodeArray) {
            String accessModifier = "";
            //todo 是否要考虑修饰符？
//            if(!isFake) {
//                accessModifier = checkAccessModifier(nodeArray,i);
//            }
            String spacer = prefixClassName.equals("^") ? "" : ".";
            if (node instanceof CPPASTSimpleDeclaration) {
                IASTDeclSpecifier iastDeclSpecifier = ((IASTSimpleDeclaration) node).getDeclSpecifier();
                if (iastDeclSpecifier instanceof CPPASTCompositeTypeSpecifier) {
                    String key = prefixClassName + spacer + ((CPPASTCompositeTypeSpecifier) ((IASTSimpleDeclaration) node).getDeclSpecifier()).getName().toString();
                    traversePrevFakeTypeDeclarationInit(compareResult, compareCache, node, key, false);
                    continue;
                }
                if (iastDeclSpecifier instanceof CPPASTElaboratedTypeSpecifier){
                    BodyDeclarationPair bodyDeclarationPair = new BodyDeclarationPair(node, prefixClassName + spacer, ChangeEntityDesc.TreeType.PREV_TREE_NODE);
                    compareCache.initPrevBodyVisitingMap(bodyDeclarationPair);
                    compareCache.addToPrevNodeBodyNameMap(bodyDeclarationPair.getCanonicalName().getLongName(), bodyDeclarationPair);
                }
                if(iastDeclSpecifier instanceof CPPASTEnumerationSpecifier){
                    BodyDeclarationPair bodyDeclarationPair = new BodyDeclarationPair(node, prefixClassName + spacer, ChangeEntityDesc.TreeType.PREV_TREE_NODE);
                    compareCache.initPrevBodyVisitingMap(bodyDeclarationPair);
                    compareCache.addToPrevNodeBodyNameMap(bodyDeclarationPair.getCanonicalName().getLongName(), bodyDeclarationPair);
                }
                //TODO 预防一些莫名出现的空声明节点，之后看看有没有别的方法
                IASTDeclarator declarator = null;
                if(((IASTSimpleDeclaration) node).getDeclarators().length != 0){
                    declarator = ((IASTSimpleDeclaration) node).getDeclarators()[0];
                }
                if (declarator instanceof CPPASTDeclarator) {
                    BodyDeclarationPair bodyDeclarationPair = new BodyDeclarationPair(node, prefixClassName + spacer, ChangeEntityDesc.TreeType.PREV_TREE_NODE);
                    compareCache.initPrevBodyVisitingMap(bodyDeclarationPair);
                    compareCache.addToPrevNodeBodyNameMap(bodyDeclarationPair.getCanonicalName().getLongName(), bodyDeclarationPair);
                    continue;
                }
            }
            if (node instanceof CPPASTFunctionDefinition functionDefinition) {
                //String key = functionDefinition.getDeclarator().getName().toString();
                BodyDeclarationPair bodyDeclarationPair = new BodyDeclarationPair(node, prefixClassName + spacer, ChangeEntityDesc.TreeType.PREV_TREE_NODE);
                compareCache.initPrevBodyVisitingMap(bodyDeclarationPair);
                compareCache.addToPrevNodeBodyNameMap(bodyDeclarationPair.getCanonicalName().getLongName(), bodyDeclarationPair);
                continue;
            }
            if (node instanceof CPPASTFieldDeclarator) {
                //String key =  ((CPPASTFieldDeclarator) node).getName().toString();
                BodyDeclarationPair bodyDeclarationPair = new BodyDeclarationPair(node, prefixClassName + spacer, ChangeEntityDesc.TreeType.PREV_TREE_NODE);
                compareCache.initPrevBodyVisitingMap(bodyDeclarationPair);
                compareCache.addToPrevNodeBodyNameMap(bodyDeclarationPair.getCanonicalName().getLongName(), bodyDeclarationPair);
            }
            if(node instanceof CPPASTNamespaceDefinition namespaceDefinition){
                //部分namespce是没有名字的，防止后面判断不加spacer(.)，给一个“ ”做名字
                String name = namespaceDefinition.getName().toString().length() == 0 ? " " : namespaceDefinition.getName().toString();
                String key = prefixClassName + spacer + name;
                BodyDeclarationPair bodyDeclarationPair = new BodyDeclarationPair(node, prefixClassName + spacer, ChangeEntityDesc.TreeType.PREV_TREE_NODE);
                traversePrevFakeTypeDeclarationInit(compareResult, compareCache, node, key, false);
                compareCache.initPrevBodyVisitingMap(bodyDeclarationPair);
                compareCache.addToPrevNodeBodyNameMap(bodyDeclarationPair.getCanonicalName().getLongName(), bodyDeclarationPair);
            }
            //todo 还有其他类型需要处理
        }
    }

    /**
     * 检查一个array中的一个指定节点的访问修饰符是什么
     * 因为在C++中，class内部定义子节点的访问修饰符和它修饰的成员是并列的，
     * 所以需要检查Array内距离当前节点最近的且比当前节点index小的访问修饰符是什么类型，
     * 如果找不到，默认为私有类型3。
     * 1: public
     * 2: protected
     * 3: private
     */
    public String checkAccessModifier(IASTNode[] array, int index) {
        int result = 3;
        String accessModifier = "";
        for (int i = index - 1; i >= 0; i--) {
            if (array[i] instanceof CPPASTVisibilityLabel) {
                if (((CPPASTVisibilityLabel) array[i]).getVisibility() == 1) {
                    result = 1;
                } else if (((CPPASTVisibilityLabel) array[i]).getVisibility() == 2) {
                    result = 2;
                } else if (((CPPASTVisibilityLabel) array[i]).getVisibility() == 3) {
                    result = 3;
                }
            }
        }
        switch (result){
            case 1:
                accessModifier += "$public.";
                break;
            case 2:
                accessModifier += "$protected.";
                break;
            case 3:
                accessModifier += "$private.";
                break;
        }
        return accessModifier;
    }
}
