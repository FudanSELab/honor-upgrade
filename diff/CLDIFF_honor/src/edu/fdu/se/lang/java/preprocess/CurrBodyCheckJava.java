package edu.fdu.se.lang.java.preprocess;

import edu.fdu.se.global.Global;
import edu.fdu.se.core.miningactions.bean.MyRange;
import edu.fdu.se.core.miningchangeentity.base.ChangeEntityDesc;
import edu.fdu.se.core.miningchangeentity.member.EnumChangeEntity;
import edu.fdu.se.core.preprocessingfile.PreprocessUtil;
import edu.fdu.se.core.preprocessingfile.data.BodyDeclarationPair;
import edu.fdu.se.core.preprocessingfile.data.PreCacheData;
import edu.fdu.se.core.preprocessingfile.data.PreCacheTmpData;
import org.eclipse.jdt.core.dom.*;

import java.util.List;

/**
 * Created by huangkaifeng on 2018/4/2.
 *
 */
public class CurrBodyCheckJava {

    /**
     * 获取与指定类型一样的 BodyDeclarationPair.bd
     * 可能是Method field class
     * @param bodyDeclarationPairs
     * @param clazz
     * @return
     */
    public BodyDeclarationPair getSameTypeBodyDeclarationPair(List<BodyDeclarationPair> bodyDeclarationPairs, Class clazz){
        for(BodyDeclarationPair bodyDeclarationPair:bodyDeclarationPairs){
            if(bodyDeclarationPair.getBodyDeclaration().getClass().equals(clazz)){
                return bodyDeclarationPair;
            }
        }
        return null;
    }

    /**
     * 检查 curr 中的 FieldDeclaration
     * 如果一样就更新标记 remove
     * 不一样会更新标记 retain
     * 如果在 prevBodyList 不存在，意味着是curr的新增，加入该类
     */
    public int checkFieldDeclarationInCurr(PreCacheData compareResult, PreCacheTmpData compareCache, FieldDeclaration fd, String prefix) {
        String uniqueKey = Global.astNodeUtil.getBodyDeclarationUniqueKey(fd);
        String key = prefix + uniqueKey;
        List<VariableDeclarationFragment> vdList = fd.fragments();
        for (VariableDeclarationFragment vd : vdList) {
            compareResult.getCurrFieldNames().add(vd.getName().toString());
            compareResult.getPrevCurrFieldNames().add(vd.getName().toString());
        }
        List<BodyDeclarationPair> prevBodyPairs = compareCache.getPrevNodeBodyNameMapByKey(key);
        if (prevBodyPairs != null){
            //todo 只会返回第一个，如果有多个相同的field，会有问题，例如一个类里面有两个相同的字段名，一个在外部一个在内部类
            BodyDeclarationPair prevBody = getSameTypeBodyDeclarationPair(prevBodyPairs,FieldDeclaration.class);
            if(prevBody != null){// same field name exists
                if (prevBody.getBodyDeclaration().toString().hashCode() == fd.toString().hashCode()
                        && prevBody.getCanonicalName().getPrefixName().hashCode() == prefix.hashCode()) {
                    compareCache.addToRemoveList(fd, ChangeEntityDesc.TreeType.CURR_TREE_NODE);
                    compareCache.addToPrevBodyVisitingMap(prevBody, PreCacheTmpData.BODY_SAME_REMOVE);
                    return 1;
                } else {
                    // variable相同，设置为不删除
                    if (PreCacheTmpData.BODY_SAME_REMOVE != compareCache.getVisitingMapValueByKey(prevBody)) {
                        compareCache.addToPrevBodyVisitingMap(prevBody, PreCacheTmpData.BODY_DIFFERENT_RETAIN);
                    }
                    return 2;
                }
            }else {
                Global.fileOutputLog.writeErrFile("[ERR]");
            }
        }
        //new field
        compareResult.addBodiesAdded(fd, prefix);
        compareCache.addToRemoveList(fd, ChangeEntityDesc.TreeType.CURR_TREE_NODE);
        return 3;
    }

    /**
     * 将 curr 中的 TypeDeclaration 和 pre 的 TypeDeclaration 进行对比
     * 如果一样就更新标记 remove
     * 不一样会更新标记 retain
     * 如果在 prevBodyList 不存在，意味着是curr的新增，加入该类
     * 同时会递归进行内部类的检查。
     * @param cod curr 中遍历的 TypeDeclaration
     * @param prefixClassName classname到cod的name前一个为止
     * @param uniqueKey
     * @return 1 2
     */
    public int checkTypeDeclarationInCurr(PreCacheData compareResult, PreCacheTmpData compareCache, TypeDeclaration cod, String prefixClassName, String uniqueKey) {
        String key = prefixClassName + uniqueKey;
        //取出Pre数据
        List<BodyDeclarationPair> prevBodyList = compareCache.getPrevNodeBodyNameMapByKey(key);
        if (prevBodyList != null) {
            //获取 TypeDeclaration 类型的 BodyDeclaration
            BodyDeclarationPair prevBody = getSameTypeBodyDeclarationPair(prevBodyList,TypeDeclaration.class);
            if(prevBody != null) {
                //检查 BodyDeclaration 的哈希是否一样，前缀名是否一样
                if (prevBody.getBodyDeclaration().toString().hashCode() == cod.toString().hashCode()
                        && prefixClassName.hashCode() == prevBody.getCanonicalName().getPrefixName().hashCode()) {
                    //如果一样，意味着没动
                    compareCache.addToRemoveList(cod, ChangeEntityDesc.TreeType.CURR_TREE_NODE);
                    compareCache.addToPrevBodyVisitingMap(prevBody, PreCacheTmpData.BODY_SAME_REMOVE);
                    new TypeNodesTraversalJava().traverseTypeDeclarationSetVisited(compareCache, prevBody.getBodyDeclaration(), key +".");
                    return 1;
                } else {
                    //如果不一样直接保留
                    compareCache.addToPrevBodyVisitingMap(prevBody, PreCacheTmpData.BODY_DIFFERENT_RETAIN);
                    return 2;
                }
            }else{
                //存在同名 prevBodyList ，但是不存在 TypeDeclaration ，就会报错
                Global.fileOutputLog.writeErrFile("[ERR]");
            }
        }
        // new class
        //如果不存在同名 prevBodyList，意味着curr中有新增class
        compareResult.addBodiesAdded(cod, prefixClassName);
        compareCache.addToRemoveList(cod, ChangeEntityDesc.TreeType.CURR_TREE_NODE);
        return 3;
    }

    public int checkEnumDeclarationInCurr(PreCacheData compareResult, PreCacheTmpData compareCache, EnumDeclaration ed, String prefixClassName) {
        String uniqueKey = Global.astNodeUtil.getBodyDeclarationUniqueKey(ed);
        String key = prefixClassName + uniqueKey;
        List<BodyDeclarationPair> prevBodyList = compareCache.getPrevNodeBodyNameMapByKey(key);
        if(prevBodyList != null){
            BodyDeclarationPair prevBody = getSameTypeBodyDeclarationPair(prevBodyList,EnumDeclaration.class);
            if(prevBody != null) {
                if (prevBody.getBodyDeclaration().toString().hashCode() == ed.toString().hashCode()
                        && prefixClassName.hashCode() == prevBody.getCanonicalName().getPrefixName().hashCode()) {
                    compareCache.addToRemoveList(ed, ChangeEntityDesc.TreeType.CURR_TREE_NODE);
                    compareCache.addToPrevBodyVisitingMap(prevBody, PreCacheTmpData.BODY_SAME_REMOVE);
                    return 1;
                } else {
                    MyRange myRange1 = Global.astNodeUtil.getRange(prevBody,ChangeEntityDesc.TreeType.PREV_TREE_NODE);
                    MyRange myRange2 = Global.astNodeUtil.getRange(ed,ChangeEntityDesc.TreeType.CURR_TREE_NODE);
                    EnumChangeEntity code = new EnumChangeEntity(prevBody, ChangeEntityDesc.StageIIOpt.OPT_CHANGE, myRange1,myRange2);
                    EnumDeclaration fd = (EnumDeclaration) prevBody.getBodyDeclaration();
                    PreprocessUtil.generateEnumChangeEntity(code, fd, ed);
                    if (compareResult.getPreChangeEntity() == null) {
                        compareResult.initPreprocessChangeEntity();
                    }
                    compareResult.getPreChangeEntity().add(code);
                    compareCache.addToRemoveList(ed, ChangeEntityDesc.TreeType.CURR_TREE_NODE);
                    compareCache.addToPrevBodyVisitingMap(prevBody, PreCacheTmpData.BODY_SAME_REMOVE);
                    return 2;
                }
            }else{
                Global.fileOutputLog.writeErrFile("[ERR]");
            }
        }
        compareResult.addBodiesAdded(ed,prefixClassName);
        compareCache.addToRemoveList(ed, ChangeEntityDesc.TreeType.CURR_TREE_NODE);
        return 3;
    }

    /**
     * curr的节点去prev的map里check
     */
    public int checkMethodDeclarationOrInitializerInCurr(PreCacheData compareResult, PreCacheTmpData compareCache, BodyDeclaration bd, String prefixClassName) {
        String uniqueKey = Global.astNodeUtil.getBodyDeclarationUniqueKey(bd);
        String methodName = Global.astNodeUtil.getMethodName(bd);
        String key = prefixClassName + uniqueKey;
        List<BodyDeclarationPair> prevBodyList = compareCache.getPrevNodeBodyNameMapByKey(key);
        if (prevBodyList != null) {
            boolean findSame = false;
            //todo 这里只考虑了相似度问题，好像不能检测move。考虑是否删除findSame
            for (BodyDeclarationPair prevBody : prevBodyList) {
                if (prevBody.hashCode() == (bd.toString().hashCode() + String.valueOf(prefixClassName.hashCode())).hashCode()) {
                    compareCache.addToPrevBodyVisitingMap(prevBody, PreCacheTmpData.BODY_SAME_REMOVE);
                    compareCache.addToRemoveList(bd, ChangeEntityDesc.TreeType.CURR_TREE_NODE);
                    findSame = true;
                    break;
                }
            }
            if (findSame) {
                return 1;
            } else {
                prevBodyList.forEach(a ->{
                    if (PreCacheTmpData.BODY_SAME_REMOVE != compareCache.getVisitingMapValueByKey(a)) {
                        compareCache.addToPrevBodyVisitingMap(a, PreCacheTmpData.BODY_DIFFERENT_RETAIN);
                    }
                });
                return 2;
            }

        } else {
            //new method
            compareResult.addBodiesAdded(bd, prefixClassName);
            compareCache.addToRemoveList(bd, ChangeEntityDesc.TreeType.CURR_TREE_NODE);
            return 3;
        }
    }

}
