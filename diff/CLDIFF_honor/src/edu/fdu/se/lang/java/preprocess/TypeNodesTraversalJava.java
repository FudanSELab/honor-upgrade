package edu.fdu.se.lang.java.preprocess;


import edu.fdu.se.core.miningchangeentity.base.ChangeEntityDesc;
import edu.fdu.se.core.preprocessingfile.data.BodyDeclarationPair;
import edu.fdu.se.core.preprocessingfile.data.PreCacheData;
import edu.fdu.se.core.preprocessingfile.data.PreCacheTmpData;
import edu.fdu.se.global.Global;
import edu.fdu.se.lang.common.preprocess.TypeNodesTraversal;
import org.eclipse.jdt.core.dom.*;

import java.util.List;

/**
 * Created by huangkaifeng on 2018/3/12.
 */
public class TypeNodesTraversalJava implements TypeNodesTraversal {

    private CurrBodyCheckJava currBodyCheck;

    public TypeNodesTraversalJava() {
        currBodyCheck = new CurrBodyCheckJava();
    }

    /**
     * TypeDeclaration
     * 遍历 curr 并与 Prev 的内容进行比较
     * @param compareCache 这里保存了pre的相关内容，用来和curr作比较
     * @param typeDeclaration curr       class 节点
     * @param prefixClassName class 节点为止的prefix ， root节点的class prefix 为classname
     */
    public void traverseCurrTypeDeclarationComparePrev(PreCacheData compareResult, PreCacheTmpData compareCache, TypeDeclaration typeDeclaration, String prefixClassName) {
        String uniqueKey = Global.astNodeUtil.getBodyDeclarationUniqueKey(typeDeclaration);
//        compareResult.addTypeDeclaration(prefixClassName + uniqueKey, typeDeclaration);
        int status = currBodyCheck.checkTypeDeclarationInCurr(compareResult, compareCache, typeDeclaration, prefixClassName, uniqueKey);
        if (status == 1 || status == 3) {
            return;
        }
        List<BodyDeclaration> nodeList = typeDeclaration.bodyDeclarations();
        for (int i = nodeList.size() - 1; i >= 0; i--) {
            BodyDeclaration node = nodeList.get(i);
            if (node instanceof Initializer || node instanceof MethodDeclaration) {
                currBodyCheck.checkMethodDeclarationOrInitializerInCurr(compareResult, compareCache, node, prefixClassName + uniqueKey + ".");
            } else if (node instanceof FieldDeclaration) {
                FieldDeclaration fd = (FieldDeclaration) node;
                currBodyCheck.checkFieldDeclarationInCurr(compareResult, compareCache, fd, prefixClassName + uniqueKey + ".");
            } else if (node instanceof AnnotationTypeDeclaration) {
                compareCache.addToRemoveList(node, ChangeEntityDesc.TreeType.CURR_TREE_NODE);
            } else if (node instanceof EnumDeclaration) {
                EnumDeclaration ed = (EnumDeclaration) node;
                currBodyCheck.checkEnumDeclarationInCurr(compareResult, compareCache, ed, prefixClassName + uniqueKey + ".");
            } else {
                Global.fileOutputLog.writeErrFile("[ERR]" + node.getClass().getSimpleName() + "@traverseCurrTypeDeclarationComparePrev");
            }
        }

    }

    /**
     * 对 cod 以下的节点进行遍历，如果已经存在了则标记remove
     * 设置该cod下的孩子节点为访问，因为father已经被remove了，所以不需要remove
     *
     * @param prevBody        该节点 prev Body
     * @param prefixClassName
     */
    public void traverseTypeDeclarationSetVisited(PreCacheTmpData compareCache, Object prevBody, String prefixClassName) {
        TypeDeclaration cod = (TypeDeclaration) prevBody;
        List<BodyDeclaration> tmpList = cod.bodyDeclarations();
        //对 TypeDeclaration 的 bodyDeclaration 变量声明进行遍历
        for (int m = tmpList.size() - 1; m >= 0; m--) {
            BodyDeclaration childrenNode = tmpList.get(m);
            //如果有内部类
            if (childrenNode instanceof TypeDeclaration) {
                String prefixClassName2 = prefixClassName.substring(0, prefixClassName.length() - 1) + "$";
                BodyDeclarationPair bdp = new BodyDeclarationPair(childrenNode, prefixClassName2, ChangeEntityDesc.TreeType.PREV_TREE_NODE);
                int value = compareCache.getVisitingMapValueByKey(bdp);
                //查到内部类是否也已经有了，如果有也标记remove
                if (value != PreCacheTmpData.BODY_NULL) {
                    compareCache.addToPrevBodyVisitingMap(bdp, PreCacheTmpData.BODY_FATHERNODE_REMOVE);
                }
                //对内部类进行递归检查
                traverseTypeDeclarationSetVisited(compareCache, childrenNode, bdp.getCanonicalName().getLongName() + ".");
            } else {
                //如果不是内部类那可以直接标记remove
                BodyDeclarationPair bdp = new BodyDeclarationPair(childrenNode, prefixClassName, ChangeEntityDesc.TreeType.PREV_TREE_NODE);
                int value = compareCache.getVisitingMapValueByKey(bdp);
                if (value != PreCacheTmpData.BODY_NULL) {
                    compareCache.addToPrevBodyVisitingMap(bdp, PreCacheTmpData.BODY_FATHERNODE_REMOVE);
                }
            }
        }
    }

    /**
     * 对传入的 TypeDeclaration 进行初始处理，按含有的各个部分加入到 PrevBodyNameMap 中去，格式： key BodyDeclarationPair
     * TypeDeclaration 内部的 TypeDeclaration 会递归执行该方法
     * @param compareResult
     * @param compareCache
     * @param typeDeclaration Prev
     * @param prefixClassName 代表这个TypeDeclartion的前缀
     */
    public void traversePrevTypeDeclarationInit(PreCacheData compareResult, PreCacheTmpData compareCache,
                                                TypeDeclaration typeDeclaration, String prefixClassName) {
        List<BodyDeclaration> nodeList = typeDeclaration.bodyDeclarations();
        BodyDeclarationPair typeBodyDeclarationPair = new BodyDeclarationPair(typeDeclaration, prefixClassName, ChangeEntityDesc.TreeType.PREV_TREE_NODE);
//        compareResult.addTypeDeclaration(typeBodyDeclarationPair.getCanonicalName().getLongName(), typeDeclaration);
        //将该 TypeDeclaration 加入 PrevBodyNameMap
        compareCache.addToPrevNodeBodyNameMap(typeBodyDeclarationPair.getCanonicalName().getLongName(), typeBodyDeclarationPair);
        //将 typeBodyDeclarationPair 加入到 temp 的 prevBodyVisitingMap 中去， value 为默认值
        compareCache.initPrevBodyVisitingMap(typeBodyDeclarationPair);

        for (int i = nodeList.size() - 1; i >= 0; i--) {
            BodyDeclaration bodyDeclaration = nodeList.get(i);
            //如果内部还存在 typeDeclaration（内部类） 则递归调用继续处理该 typeDeclaration
            if (bodyDeclaration instanceof TypeDeclaration) {
                traversePrevTypeDeclarationInit(compareResult, compareCache, (TypeDeclaration) bodyDeclaration, typeBodyDeclarationPair.getCanonicalName().getLongName() + "$");
                continue;
            }
            BodyDeclarationPair bodyPair = new BodyDeclarationPair(bodyDeclaration, typeBodyDeclarationPair.getCanonicalName().getLongName() + ".", ChangeEntityDesc.TreeType.PREV_TREE_NODE);
            compareCache.initPrevBodyVisitingMap(bodyPair);
            if (bodyDeclaration instanceof EnumDeclaration) {
                compareCache.addToPrevNodeBodyNameMap(bodyPair.getCanonicalName().getLongName(), bodyPair);
                continue;
            }
            if (bodyDeclaration instanceof MethodDeclaration) {
                compareCache.addToPrevNodeBodyNameMap(bodyPair.getCanonicalName().getLongName(), bodyPair);
                continue;
            }
            if (bodyDeclaration instanceof Initializer) {
                compareCache.addToPrevNodeBodyNameMap(bodyPair.getCanonicalName().getLongName(), bodyPair);
                continue;
            }
            if (bodyDeclaration instanceof FieldDeclaration) {
                FieldDeclaration fd = (FieldDeclaration) bodyDeclaration;
                compareCache.addToPrevNodeBodyNameMap(bodyPair.getCanonicalName().getLongName(), bodyPair);
                //获取字段声明中的变量声明片段列表存入mList
                List<VariableDeclarationFragment> mmList = fd.fragments();
                for (VariableDeclarationFragment vd : mmList) {
                    compareResult.getPrevFieldNames().add(vd.getName().toString());
                    compareResult.getPrevCurrFieldNames().add(vd.getName().toString());
                }
                continue;
            }
            //处理注解
            if (bodyDeclaration instanceof AnnotationTypeDeclaration) {
                compareCache.addToRemoveList(bodyDeclaration, ChangeEntityDesc.TreeType.PREV_TREE_NODE);
            }

        }
    }


}
