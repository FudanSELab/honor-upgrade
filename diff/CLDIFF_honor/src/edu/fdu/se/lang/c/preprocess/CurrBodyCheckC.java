package edu.fdu.se.lang.c.preprocess;

import edu.fdu.se.core.miningchangeentity.base.ChangeEntityDesc;
import edu.fdu.se.core.preprocessingfile.data.BodyDeclarationPair;
import edu.fdu.se.core.preprocessingfile.data.PreCacheData;
import edu.fdu.se.core.preprocessingfile.data.PreCacheTmpData;
import edu.fdu.se.global.Global;
import org.eclipse.cdt.core.dom.ast.*;
import org.eclipse.cdt.internal.core.dom.parser.cpp.*;

import java.util.List;

/**
 * Created by huangkaifeng on 2018/4/2.
 *
 */
public class CurrBodyCheckC {


    enum Type{
        TypeDeclaration,FieldDeclaration,FunctionDeclaration,EnumDeclaration
    }

    public BodyDeclarationPair getSameTypeBodyDeclarationPair(List<BodyDeclarationPair> bodyDeclarationPairs, Class clazz){
        for(BodyDeclarationPair bodyDeclarationPair:bodyDeclarationPairs){
            if(!(bodyDeclarationPair.getBodyDeclaration() instanceof IASTSimpleDeclaration))continue;
            if(((IASTSimpleDeclaration)(bodyDeclarationPair.getBodyDeclaration())).getDeclSpecifier().getClass().equals(clazz)){
                return bodyDeclarationPair;
            }
        }
        return null;
    }

    public int checkElaboratedTypeDeclarationInCurr(PreCacheData compareResult, PreCacheTmpData compareCache, IASTNode iastNode, String prefix){
        String name = Global.astNodeUtil.getBodyDeclarationUniqueKey(iastNode);
        String prefixName = prefix + name;
        List<BodyDeclarationPair> prevBodyPairs = compareCache.getPrevNodeBodyNameMapByKey(prefixName);
        IASTDeclarator[] vdList = ((IASTSimpleDeclaration) iastNode).getDeclarators();
        if (prevBodyPairs != null) {
            for(BodyDeclarationPair prevBody : prevBodyPairs){//由于可能出现同名的类定义，所以需要校验类型
                if(prevBody != null && prevBody.getBodyDeclaration() instanceof CPPASTSimpleDeclaration declaration){
                    IASTDeclSpecifier iastDeclSpecifier = declaration.getDeclSpecifier();
                    if(iastDeclSpecifier instanceof CPPASTSimpleDeclaration){
                        if (((IASTNode)prevBody.getBodyDeclaration()).getRawSignature().hashCode() == iastNode.getRawSignature().hashCode()
                                && prevBody.getCanonicalName().getPrefixName().hashCode() == prefix.hashCode()) {
                            compareCache.addToRemoveList(iastNode, ChangeEntityDesc.TreeType.CURR_TREE_NODE);
                            compareCache.addToPrevBodyVisitingMap(prevBody, PreCacheTmpData.BODY_SAME_REMOVE);
                            return 1;
                        }
                        compareCache.addToPrevBodyVisitingMap(prevBody, PreCacheTmpData.BODY_DIFFERENT_RETAIN);
                        return 2;
                    }
                }
            }
        }
        //new field
        compareResult.addBodiesAdded(iastNode, prefix);
        compareCache.addToRemoveList(iastNode, ChangeEntityDesc.TreeType.CURR_TREE_NODE);
        return 3;
    }


    /**
     * 检查 curr 中的 FieldDeclaration
     * 如果一样就更新标记 remove
     * 不一样会更新标记 retain
     * 如果在 prevBodyList 不存在，意味着是curr的新增，加入该类
     */
    public int checkFieldDeclarationInCurr(PreCacheData compareResult, PreCacheTmpData compareCache, IASTNode iastNode, String prefix) {
        String name = Global.astNodeUtil.getBodyDeclarationUniqueKey(iastNode);
        String prefixName = prefix + name;
        List<BodyDeclarationPair> prevBodyPairs = compareCache.getPrevNodeBodyNameMapByKey(prefixName);
        IASTDeclarator[] vdList = ((IASTSimpleDeclaration) iastNode).getDeclarators();
        if (prevBodyPairs != null) {
            //BodyDeclarationPair prevBody = getSameTypeBodyDeclarationPair(prevBodyPairs, CPPASTDeclarator.class);
            BodyDeclarationPair prevBody = prevBodyPairs.get(0);

            if(prevBody != null){
                if (((IASTNode)prevBody.getBodyDeclaration()).getRawSignature().hashCode() == iastNode.getRawSignature().hashCode()
                        && prevBody.getCanonicalName().getPrefixName().hashCode() == prefix.hashCode()) {
                    compareCache.addToRemoveList(iastNode, ChangeEntityDesc.TreeType.CURR_TREE_NODE);
                    compareCache.addToPrevBodyVisitingMap(prevBody, PreCacheTmpData.BODY_SAME_REMOVE);
                    return 1;
                }
                compareCache.addToPrevBodyVisitingMap(prevBody, PreCacheTmpData.BODY_DIFFERENT_RETAIN);
                return 2;
            }
        }
        //new field
        compareResult.addBodiesAdded(iastNode, prefix);
        compareCache.addToRemoveList(iastNode, ChangeEntityDesc.TreeType.CURR_TREE_NODE);
        return 3;
    }

    /**
     * 检查代码内部是否存在类并进行标记
     * @return 1 2
     */
    public int  checkTypeDeclarationInCurr(PreCacheData compareResult, PreCacheTmpData compareCache, IASTNode cod,String prefixClassName, String uniqueKey) {
        String key = prefixClassName + uniqueKey;
        List<BodyDeclarationPair> prevBodyList = compareCache.getPrevNodeBodyNameMapByKey(key);
        if (prevBodyList != null) {
           for(BodyDeclarationPair prevBody : prevBodyList){
               if(prevBody != null && prevBody.getBodyDeclaration() instanceof CPPASTSimpleDeclaration declaration) {//可能和elaboratedtype出现冲突，所以需要类型校验
                   if(declaration.getDeclSpecifier() instanceof CPPASTCompositeTypeSpecifier){
                       if (((IASTNode)prevBody.getBodyDeclaration()).getRawSignature().hashCode() == cod.getRawSignature().hashCode()
                               && key.hashCode() == prevBody.getCanonicalName().getLongName().hashCode()) {
                           compareCache.addToRemoveList(cod, ChangeEntityDesc.TreeType.CURR_TREE_NODE);
                           compareCache.addToPrevBodyVisitingMap(prevBody, PreCacheTmpData.BODY_SAME_REMOVE);
                           //new TypeNodesTraversalC().traverseTypeDeclarationSetVisited(compareCache, prevBody.getBodyDeclaration(), key+".");
                           return 1;
                       } else {
                           compareCache.addToPrevBodyVisitingMap(prevBody, PreCacheTmpData.BODY_DIFFERENT_RETAIN);
                           return 2;
                       }
                   }
            }

            }
        }
        // new class
        compareResult.addBodiesAdded(cod, key);
        compareCache.addToRemoveList(cod, ChangeEntityDesc.TreeType.CURR_TREE_NODE);
        return 3;
    }

    /**
     * 检查枚举声明并进行标记
     * @return 1 2
     */
    public int checkEnumDeclarationInCurr(PreCacheData compareResult, PreCacheTmpData compareCache, IASTNode ed, String prefixClassName) {
        String name = Global.astNodeUtil.getBodyDeclarationUniqueKey(ed);
        String prefix = prefixClassName + name;
        List<BodyDeclarationPair> prevBodyList = compareCache.getPrevNodeBodyNameMapByKey(prefix);
        if(prevBodyList != null){

            BodyDeclarationPair prevBody = getSameTypeBodyDeclarationPair(prevBodyList, CPPASTEnumerationSpecifier.class);
            if(prevBody != null) {
                if (((IASTNode)prevBody.getBodyDeclaration()).getRawSignature().hashCode() == ed.getRawSignature().hashCode()
                        && prefixClassName.hashCode() == prevBody.getCanonicalName().getPrefixName().hashCode()) {
                    compareCache.addToRemoveList(ed, ChangeEntityDesc.TreeType.CURR_TREE_NODE);
                    compareCache.addToPrevBodyVisitingMap(prevBody, PreCacheTmpData.BODY_SAME_REMOVE);
                    return 1;
                } else {
                    compareCache.addToPrevBodyVisitingMap(prevBody, PreCacheTmpData.BODY_DIFFERENT_RETAIN);
                    return 2;
                }
            }else{
                Global.fileOutputLog.writeErrFile("[ERR]EEE--------");
            }
        }
        // new class
        compareResult.addBodiesAdded(ed,prefixClassName);
        compareCache.addToRemoveList(ed, ChangeEntityDesc.TreeType.CURR_TREE_NODE);
        return 3;
    }

    /**
     * curr的节点去prev的map里check
     */
    public int checkMethodDeclarationOrInitializerInCurr(PreCacheData compareResult, PreCacheTmpData compareCache, IASTNode iastNode, String prefixClassName) {
        String name = Global.astNodeUtil.getBodyDeclarationUniqueKey(iastNode);
        String methodNameKey = prefixClassName + name;

        List<BodyDeclarationPair> prevBodyList = compareCache.getPrevNodeBodyNameMapByKey(methodNameKey);
        if (prevBodyList != null) {
            //存在重载函数使得key相同的情况，遍历所有同名函数
            for(BodyDeclarationPair prevBody: prevBodyList){
                if(prevBody != null){
                    if (((IASTNode)prevBody.getBodyDeclaration()).getRawSignature().hashCode() == iastNode.getRawSignature().hashCode()
                            && prefixClassName.hashCode() == prevBody.getCanonicalName().getPrefixName().hashCode()) {
                        compareCache.addToRemoveList(iastNode, ChangeEntityDesc.TreeType.CURR_TREE_NODE);
                        compareCache.addToPrevBodyVisitingMap(prevBody, PreCacheTmpData.BODY_SAME_REMOVE);
                        return 1;
                    }
                    //防止覆盖之前已经成功匹配的重载函数
                    if(compareCache.getPrevBodyVisitingMap().get(prevBody) != PreCacheTmpData.BODY_SAME_REMOVE){
                        compareCache.addToPrevBodyVisitingMap(prevBody, PreCacheTmpData.BODY_DIFFERENT_RETAIN);
                    }
                }
            }//将所有无法完全匹配的同名函数遍历完之后返回
            return 2;
        }
        // new method
        compareResult.addBodiesAdded(iastNode, methodNameKey);
        compareCache.addToRemoveList(iastNode, ChangeEntityDesc.TreeType.CURR_TREE_NODE);
        return 3;
    }

    public int  checkNamespaceInCurr(PreCacheData compareResult, PreCacheTmpData compareCache, IASTNode cod,String prefixClassName, String uniqueKey) {
        String key = prefixClassName + uniqueKey;
        List<BodyDeclarationPair> prevBodyList = compareCache.getPrevNodeBodyNameMapByKey(key);
        if (prevBodyList != null) {
            for(BodyDeclarationPair prevBody : prevBodyList){
                if(prevBody != null && prevBody.getBodyDeclaration() instanceof CPPASTNamespaceDefinition declaration) {//可能和elaboratedtype出现冲突，所以需要类型校验
                        if (((IASTNode)prevBody.getBodyDeclaration()).getRawSignature().hashCode() == cod.getRawSignature().hashCode()
                                && key.hashCode() == prevBody.getCanonicalName().getLongName().hashCode()) {
                            compareCache.addToRemoveList(cod, ChangeEntityDesc.TreeType.CURR_TREE_NODE);
                            compareCache.addToPrevBodyVisitingMap(prevBody, PreCacheTmpData.BODY_SAME_REMOVE);
                            //new TypeNodesTraversalC().traverseTypeDeclarationSetVisited(compareCache, prevBody.getBodyDeclaration(), key+".");
                            return 1;
                        } else {
                            compareCache.addToPrevBodyVisitingMap(prevBody, PreCacheTmpData.BODY_DIFFERENT_RETAIN);
                            return 2;
                        }
                    }
                }

            }

        // new class
        compareResult.addBodiesAdded(cod, key);
        compareCache.addToRemoveList(cod, ChangeEntityDesc.TreeType.CURR_TREE_NODE);
        return 3;
    }

}
