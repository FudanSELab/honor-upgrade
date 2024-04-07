package edu.fdu.se.core.miningactions.util;

import com.github.gumtreediff.actions.model.Action;
import com.github.gumtreediff.actions.model.Insert;
import com.github.gumtreediff.actions.model.Move;
import com.github.gumtreediff.tree.ITree;
import com.github.gumtreediff.tree.Tree;
import edu.fdu.se.global.Global;
import edu.fdu.se.core.generatingactions.ActionConstants;

import edu.fdu.se.core.miningactions.bean.ChangePacket;
import edu.fdu.se.core.miningactions.bean.MiningActionData;


import java.util.List;

/**
 * Created by huangkaifeng on 2018/1/26.
 *
 */
public class BasicTreeTraversal {

    /**
     * 遍历子树，子树中含有任何的action都添加进resultActions，resultType记录type
     *
     * @param tree 遍历的root节点
     * @param resultActions [insert,null] 或者[delete,move,update,null]
     * @param resultTypes 同上
     */
    protected static void traverseNodeSubTree(ITree tree, List<Action> resultActions, List<String> resultTypes){
        Iterable<ITree> no = tree.preOrder();
        for(ITree node:tree.preOrder()){
            Tree tmp = (Tree)node;
            if(tmp.getDoAction()==null){
                resultTypes.add(ActionConstants.NULLACTION);
            }else{
                tmp.getDoAction().forEach(a -> {
                    if(!(a instanceof Move)) {
                        resultActions.add(a);
                        resultTypes.add(a.getClass().getSimpleName());
                    }
                });
            }
        }
    }

    /**
     * 类似于traverseNode，只是考虑到遍历block节点，block节点的属性会影响类型的判断，root节点为block属性时，舍弃block信息
     * @param tree root节点
     * @param resultActions result
     * @param resultTypes resulttype
     */
    protected static void traverseNodeChildrenSubTree(ITree tree, List<Action> resultActions, List<String> resultTypes){
        boolean flag = true;
        for(ITree node:tree.preOrder()){
            if(flag){
                flag = false;
                continue;
            }

            Tree tmp = (Tree)node;
            if(tmp.getDoAction()==null){
                resultTypes.add(ActionConstants.NULLACTION);
            }else{
                tmp.getDoAction().forEach(a -> {
                    if(!(a instanceof Move)) {
                        resultActions.add(a);
                        resultTypes.add(a.getClass().getSimpleName());
                    }
                });
            }
        }
    }

    /**
     * class signature，method signature等需要将形如 XXX(A){B} A和B的变化分开，所以需要首先识别分割点，然后按照range遍历
     * @param tree root
     * @param a range a
     * @param b range b
     * @param resultActions result
     * @param resultTypes resulttype
     */
    protected static void traverseNodeSubTreeInRange(ITree tree, int a, int b, List<Action> resultActions, List<String> resultTypes){
        List<ITree> children = tree.getChildren();
        for(int i = a;i<=b;i++){
            ITree c = children.get(i);
            traverseNodeSubTree(c,resultActions,resultTypes);
        }
    }

    /**
     * 如果是形如field等类型，将其作为一个整体考虑，所以不需要range
     * @param node root
     * @param result1
     * @param changePacket
     */
    protected static void traverseOneType(ITree node,List<Action> result1,ChangePacket changePacket){
        if(changePacket.getChangeSet1()==null){
            changePacket.initChangeSet1();
        }
        traverseNodeSubTree(node,result1,changePacket.getChangeSet1());
    }



    /**
     * 找不同粒度下的公共父节点
     *
     * @return
     */
    public static Tree findCommonRootNode(ITree node, List<Integer> nodeTypes) {
        return Global.astNodeUtil.findCommonRootNode(node, nodeTypes);
    }

    public static ITree[] getMappedFafatherNode(MiningActionData fp, Action a, ITree fafather){
        ITree srcFafather;
        ITree dstFafather;
        if (a instanceof Insert) {
            dstFafather = fafather;
            srcFafather = fp.getMappedPrevOfCurrNode(dstFafather);
        } else {
            srcFafather = fafather;
            dstFafather = fp.getMappedCurrOfPrevNode(srcFafather);
        }
        ITree [] result = {srcFafather,dstFafather};
        return result;
    }

    public static boolean traverseWhenActionIsMove(Action a,List<Action> result,ChangePacket changePacket,boolean flag){
        if(a instanceof Move){
            result.add(a);
            changePacket.initChangeSet1();
            changePacket.getChangeSet1().add(ActionConstants.MOVE);
            if(flag) {
                changePacket.initChangeSet2();
                changePacket.getChangeSet2().add(ActionConstants.NULLACTION);
            }
            return true;
        }
        return false;
    }









}
