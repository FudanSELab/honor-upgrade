package edu.fdu.se.core.links.generator;

import com.github.gumtreediff.tree.Tree;
import edu.fdu.se.core.links.linkbean.MyParameters;
import edu.fdu.se.core.miningchangeentity.base.CanonicalName;
import edu.fdu.se.core.miningchangeentity.base.MemberPlusChangeEntity;
import edu.fdu.se.global.Global;
import edu.fdu.se.core.miningactions.bean.MiningActionData;
import edu.fdu.se.core.miningchangeentity.base.ChangeEntity;
import edu.fdu.se.core.miningchangeentity.base.ChangeEntityDesc;
import edu.fdu.se.core.miningchangeentity.member.*;
import edu.fdu.se.core.preprocessingfile.data.BodyDeclarationPair;

import java.util.*;
import java.util.Map.Entry;

/**
 * Created by huangkaifeng on 3/24/18.
 * 树的节点的框架: Body， Prev的body为主，如果是增的，那么是Curr的body
 * 内部change entity: ChangeEntity
 * <p>
 * method，field等change的key是TypeDeclaration
 * method内部的change的key是Method
 */
public class ChangeEntityTree {


    private Map<String, BodyDeclarationPair> bodyDeclarationUniqueKey;
    private Map<BodyDeclarationPair, List<ChangeEntity>> layerChangeEntityMap;

    /**
     * Link 部分做cache用
     */
    private Map<BodyDeclarationPair, List<String>> methodDeclarationNames;

    /**
     * init
     */
    public ChangeEntityTree() {
        this.layerChangeEntityMap = new HashMap<>();
        this.bodyDeclarationUniqueKey = new HashMap<>();
        if (Global.isLink) {
            methodDeclarationNames = new HashMap<>();
        }
    }


    public void addKey(BodyDeclarationPair bodyDeclarationPair) {
        if (layerChangeEntityMap.containsKey(bodyDeclarationPair)) {
            return;
        }
        layerChangeEntityMap.put(bodyDeclarationPair, new ArrayList<>());
        bodyDeclarationUniqueKey.put(bodyDeclarationPair.getCanonicalName().getLongName(), bodyDeclarationPair);
        addMethodParamNameCache(bodyDeclarationPair, bodyDeclarationPair.getBodyDeclaration());

    }

    public boolean isValueMethodParam(BodyDeclarationPair bodyDeclarationPair,String value){
        if(this.methodDeclarationNames.containsKey(bodyDeclarationPair)){
            if(this.methodDeclarationNames.get(bodyDeclarationPair).contains(value)){
                return true;
            }
        }
        return false;
    }

    /**
     *
     */
    private void addMethodParamNameCache(BodyDeclarationPair bodyDeclarationPair, Object astNode) {
        if (Global.isLink) {
            if (Global.astNodeUtil.isMethodDeclaration(astNode)) {
                if (!methodDeclarationNames.containsKey(bodyDeclarationPair)) {
                    methodDeclarationNames.put(bodyDeclarationPair, new ArrayList<>());
                    List<MyParameters> params = Global.astNodeUtil.getMethodDeclarationParameters(bodyDeclarationPair.getBodyDeclaration());
                    if (params != null && params.size() != 0) {
                        for (MyParameters m : params) {
                            methodDeclarationNames.get(bodyDeclarationPair).add(m.name);
                        }
                    }
                }else if(!bodyDeclarationPair.getBodyDeclaration().equals(astNode)){
                    List<MyParameters> params = Global.astNodeUtil.getMethodDeclarationParameters(astNode);
                    if (params != null && params.size() != 0) {
                        List<String> nameList = methodDeclarationNames.get(bodyDeclarationPair);
                        for (MyParameters m : params) {
                            if(!nameList.contains(m.name)) {
                                nameList.add(m.name);
                            }
                        }
                    }
                }
            }
        }

    }

    private BodyDeclarationPair getBodyDeclarationPairWithKey(String key) {
        if (bodyDeclarationUniqueKey.containsKey(key)) {
            return bodyDeclarationUniqueKey.get(key);
        }
        return null;
    }

    /**
     * Add preprocess
     *
     * @param changeEntity
     */
    public void addPreDiffChangeEntity(ChangeEntity changeEntity) {
        if (changeEntity == null) {
            return;
        }
        String location = null;
        if(changeEntity instanceof ClassChangeEntity){
            location = changeEntity.stageIIBean.getCanonicalName().getLongName();
        }else{
            location = changeEntity.stageIIBean.getCanonicalName().getPrefixName();
            location = location.substring(0, location.length() - 1);
        }
        BodyDeclarationPair bodyPair = getBodyDeclarationPairWithKey(location);
        if (bodyPair != null) {
            insert(bodyPair, changeEntity);
        } else {
            Global.fileOutputLog.writeErrFile("[ERR]Put to LayerMap error: " + changeEntity.stageIIBean.getCanonicalName().getPrintName() + " " + changeEntity.getClass().getSimpleName());
        }

    }

    /**
     * Add CLDIFF
     *
     * @param changeEntity
     * @param mad
     */
    public void addCLDiffChangeEntity(ChangeEntity changeEntity, MiningActionData mad) {
        Tree fafather = changeEntity.clusteredActionBean.fafather;
        fafather = (Tree) fafather.getParent();
        if (Global.astNodeUtil.isCompilationUnit(fafather.getNode())) {
            fafather = changeEntity.clusteredActionBean.fafather;
        }
        Tree tree;
        Tree currTree = null;
        if (changeEntity instanceof MemberPlusChangeEntity) {
            tree = (Tree) Global.astNodeUtil.searchBottomUpFindTypeDeclaration(fafather, true);
        } else {
            tree = (Tree) Global.astNodeUtil.searchBottomUpFindBodyDeclaration(fafather,false);
        }
        if (ChangeEntityDesc.TreeType.CURR_TREE_NODE == tree.getTreePrevOrCurr()) {
            currTree = tree;
            tree = (Tree) mad.getMappedPrevOfCurrNode(tree);
        }
        if(tree == null){
            return;//hot fix
        }
        for (BodyDeclarationPair key : this.layerChangeEntityMap.keySet()) {
            Object o = key.getBodyDeclaration();
            if (o == tree.getNode()) {// find it
                this.layerChangeEntityMap.get(key).add(changeEntity);
                if (currTree != null) {
                    addMethodParamNameCache(key, currTree.getNode());
                }
                return;
            }
        }
        //not find any key, so tree become a key
        CanonicalName canonicalName = Global.astNodeUtil.getCanonicalNameFromTree(tree);
        String prefix = canonicalName.getPrefixName();
        BodyDeclarationPair bodyDclPair = new BodyDeclarationPair(tree.getNode(),prefix,ChangeEntityDesc.TreeType.PREV_TREE_NODE);
        this.addKey(bodyDclPair);
        this.layerChangeEntityMap.get(bodyDclPair).add(changeEntity);
        if (currTree != null) {
            addMethodParamNameCache(bodyDclPair, currTree.getNode());
        }

//        Global.logger.warning("[WARN] not put into layer map");
        Global.fileOutputLog.writeErrFile("[ERR]Put to LayerMap error: " + changeEntity.stageIIBean.getCanonicalName().getPrintName() + " " + changeEntity.getClass().getSimpleName());
    }


    private void insert(BodyDeclarationPair bodyPair, ChangeEntity changeEntity) {
        if (Global.astNodeUtil.isTypeDeclaration(bodyPair.getBodyDeclaration())) {
            this.layerChangeEntityMap.get(bodyPair).add(changeEntity);
        } else {
            this.layerChangeEntityMap.get(bodyPair).add(changeEntity);
        }
    }


    /**
     * 对 ChangeEntityTree 进行排序，先排序元素内部的映射列表，再排序元素
     */
    public List<Entry<BodyDeclarationPair, List<ChangeEntity>>> getSortedChangeEntityTree() {
        // sort change entity list
        for (Entry<BodyDeclarationPair, List<ChangeEntity>> entry : this.layerChangeEntityMap.entrySet()) {
            List<ChangeEntity> mList = entry.getValue();
            mList.sort(Comparator.comparingInt(a -> a.lineRange.startLineNo));
        }
        // sort keys
        List<Entry<BodyDeclarationPair, List<ChangeEntity>>> mList = new ArrayList<>(layerChangeEntityMap.entrySet());
        mList.sort(Comparator.comparingInt(a -> Global.astNodeUtil.getStartPosition(a.getKey().getBodyDeclaration())));

        return mList;

    }

    /**
     * getter
     *
     * @return
     */
    public int getChangeEntitySize() {
        int size = 0;
        for (Entry<BodyDeclarationPair, List<ChangeEntity>> entry : this.layerChangeEntityMap.entrySet()) {
            size += entry.getValue().size();
        }
        return size;
    }

    /**
     * getter
     *
     * @return
     */
    public Map<BodyDeclarationPair, List<ChangeEntity>> getLayerChangeEntityMap() {
        return layerChangeEntityMap;
    }


    public BodyDeclarationPair getChangeEntityBelongedBodyDeclarationPair(ChangeEntity ce) {
        for (Map.Entry<BodyDeclarationPair, List<ChangeEntity>> entry : this.layerChangeEntityMap.entrySet()) {
            BodyDeclarationPair key = entry.getKey();
            List<ChangeEntity> value = entry.getValue();
            for(ChangeEntity can : value){
                if(can.getChangeEntityId()== ce.getChangeEntityId()){
                    return key;
                }
            }

        }
        return null;
    }

    public String toString(){
        for (Entry<BodyDeclarationPair, List<ChangeEntity>> entry : this.layerChangeEntityMap.entrySet()) {
            System.out.println("entityKey" + entry.getKey().getCanonicalName().getLongName());
            for(ChangeEntity entity: entry.getValue()){
                System.out.println(entity.getChangeEntityId());
            }
        }//方便看结果
        return "";
    }


}
