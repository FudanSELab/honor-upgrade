package edu.fdu.se.core.preprocessingfile.data;


import java.util.*;

import edu.fdu.se.core.miningchangeentity.base.ChangeEntityDesc;

/**
 * Created by huangkaifeng on 2018/1/18.
 *
 */
public class PreCacheTmpData {

    /**
     * 已经设置为same remove有可能被 overload遍历到，设置为retain，需要加check
     */
    final static public int BODY_SAME_REMOVE = 10;
    /**
     * 已经为retain ，如果遍历到发现是same remove 则可以re- set 为remove
     */
    final static public int BODY_DIFFERENT_RETAIN = 11;
    final static public int BODY_INITIALIZED_VALUE = 12;
    /**
     * 已经设置为remove的，表示curr中cod已经被删除，所以不会再revisit到
     */
    final static public int BODY_FATHERNODE_REMOVE = 13;

    final static public int BODY_NULL = Integer.MAX_VALUE;


    public PreCacheTmpData() {
        prevNodeBodyNameMap = new HashMap<>();
        prevBodyVisitingMap = new HashMap<>();
//        prevNodeHashCodeMap = new HashMap<>();
        prevRemovalNodes = new ArrayList<>();
        currRemovalNodes = new ArrayList<>();
    }

    /**
     * String key = classname.methodnname(param,param2)
     * BodyDeclarationPair: new BodyDeclarationPair(typeDeclaration, prefixClassName, ChangeEntityDesc.TreeType.PREV_TREE_NODE)
     */
    private Map<String, List<BodyDeclarationPair>> prevNodeBodyNameMap;
    /**
     *
     * Integer ： 0 初始化之后的值  1 遍历到了之后 需要保留的different  2 遍历到了之后 需要删除的same   3 prev中有，curr没有，change：deleted
     */
    private Map<BodyDeclarationPair,Integer> prevBodyVisitingMap;

//    public Map<BodyDeclarationPair,Integer> prevNodeHashCodeMap;

    /**
     * list of comments to be removed
     */
    public List<Object> prevRemovalNodes;
    public List<Object> currRemovalNodes;


    /**
     * key = classname.methodnname(param,param2)
     * key 是 object 的声明名字、变量声明列表、方法参数列表、静态方法块等内容
     * 将 BodyDeclaration 的 key 加入 prevNodeBodyNameMap，也就是除了参数以外的名字加入
     */
    public void addToPrevNodeBodyNameMap(String key, BodyDeclarationPair bd) {
        if (key.endsWith(")")){
            int index = key.lastIndexOf("(");
            key = key.substring(0,index);
        }

        if (this.prevNodeBodyNameMap.containsKey(key)) {
            this.prevNodeBodyNameMap.get(key).add(bd);
        } else {
            this.prevNodeBodyNameMap.put(key, new ArrayList<>());
            this.prevNodeBodyNameMap.get(key).add(bd);
        }
    }

    /**
     * 根据 key 值获取 prevNodeBodyNameMap 中的数据，也就是 List< BodyDeclarationPair >
     * class: key = ClassName
     * method: key = ClassName.methodName
     * field: key = ClassName.fieldName;
     */
    public List<BodyDeclarationPair> getPrevNodeBodyNameMapByKey(String key){
        if (key.endsWith(")")){
            int index = key.lastIndexOf("(");
            key = key.substring(0,index);
        }
        if (this.prevNodeBodyNameMap.containsKey(key)){
            return this.prevNodeBodyNameMap.get(key);
        }
        return null;
    }





    public int getVisitingMapValueByKey(BodyDeclarationPair bodyDeclarationPair){
        if(this.prevBodyVisitingMap.containsKey(bodyDeclarationPair)){
            return this.prevBodyVisitingMap.get(bodyDeclarationPair);
        }
        return BODY_NULL;
    }

    /**
     * 替换列表中指定索引位置的元素
     */
    public void setLinesFlag(List<Integer> lineFlags,int start,int end){
        for(int i =start ;i<=end;i++){
            if(lineFlags.get(i-1)>0){
                //替换列表中指定索引位置的元素
                //索引是从1开始的，而不是从0开始。因此，在访问列表元素时，使用 i-1 进行索引调整
                lineFlags.set(i-1, -lineFlags.get(i-1));
            }
        }
    }


    public void addToRemoveList(Object bd, int treeType) {
        if (ChangeEntityDesc.TreeType.PREV_TREE_NODE == treeType) {
            this.prevRemovalNodes.add(bd);
        } else if (ChangeEntityDesc.TreeType.CURR_TREE_NODE == treeType) {
            this.currRemovalNodes.add(bd);
        }
    }


    public void initPrevBodyVisitingMap(BodyDeclarationPair bodyDeclarationPair){
        this.prevBodyVisitingMap.put(bodyDeclarationPair,BODY_INITIALIZED_VALUE);
    }

    /**
     *
     * @param v
     */
    public void addToPrevBodyVisitingMap(BodyDeclarationPair bdp, int v){
        this.prevBodyVisitingMap.put(bdp,v);
    }


    public Map<BodyDeclarationPair, Integer> getPrevBodyVisitingMap() {
        return prevBodyVisitingMap;
    }
}
