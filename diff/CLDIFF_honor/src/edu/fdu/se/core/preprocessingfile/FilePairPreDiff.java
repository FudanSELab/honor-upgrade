package edu.fdu.se.core.preprocessingfile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import edu.fdu.se.global.Constants;
import edu.fdu.se.global.Global;
import edu.fdu.se.core.miningchangeentity.base.ChangeEntityDesc;

import edu.fdu.se.core.preprocessingfile.data.BodyDeclarationPair;
import edu.fdu.se.core.preprocessingfile.data.PreCacheData;
import edu.fdu.se.core.preprocessingfile.data.PreCacheTmpData;
import edu.fdu.se.lang.common.preprocess.TypeNodesTraversal;
import edu.fdu.se.util.StringUtil;

/**
 * 两个文件 预处理
 * 删除一摸一样的方法
 * 删除一摸一样的field
 * 删除一摸一样的内部类
 * 删除add method
 * 删除remove method
 * 删除内部类中的add / remove method
 * 保留 remove field 和add field 因为需要识别是否是refactor
 *
 * prefx 为 method field等所属的class，如果是内部类A, 那么prfix写到X.X.X.A.为止
 */
public class FilePairPreDiff {


    public FilePairPreDiff(Object prev, Object curr) {
        preCacheData = new PreCacheData();
        preCacheTmpData = new PreCacheTmpData();
        initFile(prev, curr);
    }

    private PreCacheData preCacheData;
    private PreCacheTmpData preCacheTmpData;



    private void initFile(Object prev, Object curr) {
        if (prev instanceof String) {
            initFilePath((String) prev, (String) curr);
        } else if (prev instanceof byte[]) {
            initFileContent((byte[]) prev, (byte[]) curr);
        }
    }

    private void initFilePath(String prevPath, String currPath) {
        preCacheData.setPrevCu(Global.astNodeUtil.parseCu(prevPath));
        preCacheData.setCurrCu(Global.astNodeUtil.parseCu(currPath));
        preCacheData.loadTwoCompilationUnitsObj(preCacheData.getPrevCu(), preCacheData.getCurrCu(), prevPath, currPath);
    }

    /**
     * 多态方法，获取解析的cu
     * @param prevContent prev的byte文件
     * @param currContent Curr的byte文件
     */
    private void initFileContent(byte[] prevContent, byte[] currContent) {
        try {
            preCacheData.setPrevCu(Global.astNodeUtil.parseCu(prevContent, Constants.SaveFilePath.PREV + "/"));
            preCacheData.setCurrCu(Global.astNodeUtil.parseCu(currContent, Constants.SaveFilePath.CURR + "/"));
            preCacheData.loadTwoCompilationUnitsObj(preCacheData.getPrevCu(), preCacheData.getCurrCu(), prevContent, currContent);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public int  compareTwoFile() {
        Object cuSrc = preCacheData.getPrevCu();
        Object cuDst = preCacheData.getCurrCu();
        Global.processUtil.removeAllComments(preCacheTmpData, cuSrc, preCacheData.getPrevLineNums(), ChangeEntityDesc.TreeType.PREV_TREE_NODE,preCacheData);
        Global.processUtil.removeAllComments(preCacheTmpData, cuDst, preCacheData.getCurrLineNums(), ChangeEntityDesc.TreeType.CURR_TREE_NODE,preCacheData);
        System.out.println("compare:");
        return Global.processUtil.compareTwoFile(this, preCacheTmpData, preCacheData);
    }

    public void iterateVisitingMap() {
        //遍历之前访问过的pre pair元素，这些元素都带有标记
        //key：BodyDeclarationPair value：标记
        for (Entry<BodyDeclarationPair, Integer> item : preCacheTmpData.getPrevBodyVisitingMap().entrySet()) {
            BodyDeclarationPair bdp = item.getKey();
            int value = item.getValue();
            Object bd = bdp.getBodyDeclaration();
            TypeNodesTraversal traversal= null;
            try {
                Class clazz = Class.forName(String.format(Constants.TYPE_NODES_TRAVERSAL, Global.lang.toLowerCase(), Global.lang));
                traversal = (TypeNodesTraversal) clazz.newInstance();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (Global.astNodeUtil.isTypeDeclaration(bd)) {
                switch (value) {
//                    case PreprocessedTempData.BODY_DIFFERENT_RETAIN:
//                    case PreprocessedTempData.BODY_FATHERNODE_REMOVE:
//                        break;
                    case PreCacheTmpData.BODY_INITIALIZED_VALUE -> {
                        preCacheData.addBodiesDeleted(bdp);
                        preCacheTmpData.addToRemoveList(bd, ChangeEntityDesc.TreeType.PREV_TREE_NODE);
                        traversal.traverseTypeDeclarationSetVisited(preCacheTmpData, bd, bdp.getCanonicalName().getLongName() + ".");
                    }
                    case PreCacheTmpData.BODY_SAME_REMOVE -> {
                        preCacheTmpData.addToRemoveList(bd, ChangeEntityDesc.TreeType.PREV_TREE_NODE);
                        traversal.traverseTypeDeclarationSetVisited(preCacheTmpData, bd, bdp.getCanonicalName().getLongName() + ".");
                    }
                }
            }
        }
        for (Entry<BodyDeclarationPair, Integer> item : preCacheTmpData.getPrevBodyVisitingMap().entrySet()) {
            BodyDeclarationPair bdp = item.getKey();
            int value = item.getValue();
            Object bd = bdp.getBodyDeclaration();

            if (!Global.astNodeUtil.isTypeDeclaration(bd)) {
                switch (value) {
                    case PreCacheTmpData.BODY_DIFFERENT_RETAIN:
                    case PreCacheTmpData.BODY_FATHERNODE_REMOVE:
                        break;
                    case PreCacheTmpData.BODY_INITIALIZED_VALUE:
                        preCacheData.addBodiesDeleted(bdp);
                        preCacheTmpData.addToRemoveList(bd, ChangeEntityDesc.TreeType.PREV_TREE_NODE);
                        break;
                    case PreCacheTmpData.BODY_SAME_REMOVE:
                        preCacheTmpData.addToRemoveList(bd, ChangeEntityDesc.TreeType.PREV_TREE_NODE);
                        break;
                }
            }
        }
    }

    /**
     * 遍历之前访问过的curr pair元素，这些元素都带有标记，RETAIN标记的会加入到EntityTree中，
     * 并对EntityTree进行排序
     */
    public void iterateVisitingMap2LoadContainerMap() {
        for (Entry<BodyDeclarationPair, Integer> item : preCacheTmpData.getPrevBodyVisitingMap().entrySet()) {
            BodyDeclarationPair bdp = item.getKey();
            int value = item.getValue();
            switch (value) {
                case PreCacheTmpData.BODY_DIFFERENT_RETAIN:
                    this.preCacheData.getEntityTree().addKey(bdp);
                    break;
                case PreCacheTmpData.BODY_FATHERNODE_REMOVE:
                case PreCacheTmpData.BODY_INITIALIZED_VALUE:
                case PreCacheTmpData.BODY_SAME_REMOVE:
                    break;
            }
        }
        this.preCacheData.getEntityTree().getSortedChangeEntityTree();
    }

    public PreCacheData getPreCacheData() {
        return preCacheData;
    }

    public void undeleteSignatureChange() {
        List<BodyDeclarationPair> addTmp = new ArrayList<>();
        for (BodyDeclarationPair bdpAdd : preCacheData.getmBodiesAdded()) {
            if (Global.astNodeUtil.isMethodDeclaration(bdpAdd.getBodyDeclaration())) {
                Object md = bdpAdd.getBodyDeclaration();
                //带上参数，否则名字很短，稍微变化一点就不一样了
                String methodName = Global.processUtil.methodDeclarationToString(md);
                List<BodyDeclarationPair> bdpDeleteList = new ArrayList<>();
                for (BodyDeclarationPair bdpDelete : preCacheData.getmBodiesDeleted()) {
                    if (Global.astNodeUtil.isMethodDeclaration(bdpDelete.getBodyDeclaration())) {
                        Object md2 = bdpDelete.getBodyDeclaration();
                        String methodName2 = Global.processUtil.methodDeclarationToString(md2);
                        //如果相似度大于0.6，那么就认为是同一个方法
                        if (potentialMethodNameChange(methodName, methodName2)) {
                            bdpDeleteList.add(bdpDelete);
                        }
                    }
                }
                if (bdpDeleteList.size() > 0) {
                    //todo remove的时候可能会有hashcode相同但是一个是在内部类的情况，但是这种情况很少见，所以暂时先不考虑
                    preCacheTmpData.currRemovalNodes.remove(bdpAdd.getBodyDeclaration());
                    addTmp.add(bdpAdd);
                    for (BodyDeclarationPair bdpTmp : bdpDeleteList) {
                        this.preCacheTmpData.prevRemovalNodes.remove(bdpTmp.getBodyDeclaration());
                        this.preCacheData.getmBodiesDeleted().remove(bdpTmp);
                        this.preCacheData.getEntityTree().addKey(bdpTmp);
                    }
                }
            }

        }
        for (BodyDeclarationPair tmp : addTmp) {
            this.preCacheData.getmBodiesAdded().remove(tmp);
        }
    }

    /**
     * 判断两个字符串的相似度是否大于0.6
     */
    public boolean potentialMethodNameChange(String name1, String name2) {
        return StringUtil.SimilarDegree(name1,name2)>0.6;
    }



}
