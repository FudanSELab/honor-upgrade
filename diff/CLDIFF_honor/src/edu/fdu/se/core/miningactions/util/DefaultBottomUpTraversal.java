package edu.fdu.se.core.miningactions.util;

import com.github.gumtreediff.actions.model.Action;
import com.github.gumtreediff.tree.ITree;
import com.github.gumtreediff.tree.Tree;
import edu.fdu.se.global.Global;
import edu.fdu.se.core.generatingactions.ActionConstants;
import edu.fdu.se.core.miningactions.bean.ChangePacket;
import edu.fdu.se.lang.common.MethodLookupTbl;

import java.util.List;

/**
 * Created by huangkaifeng on 2018/1/29.
 *
 */
public class DefaultBottomUpTraversal extends BasicTreeTraversal{


    @MethodLookupTbl(key = TraverseWays.BottomUp.CLASS_SIGNATURE)
    public static void traverseClassSignature(Tree node, List<Action> result1,ChangePacket changePacket){
        assert node.getDoAction() ==null;
        if(changePacket.getChangeSet1()==null){
            changePacket.initChangeSet1();
        }
        changePacket.getChangeSet1().add(ActionConstants.NULLACTION);
        List<ITree> children = node.getChildren();
        for(ITree child :children){
            Tree tmp = (Tree) child;
            //to Do
//            if(tmp.getAstNode().getNodeType()== ASTNode.MODIFIER ||
//                    tmp.getAstNode().getNodeType() == ASTNode.SIMPLE_NAME ||
//                    tmp.getAstNode().getNodeType() == ASTNode.PARAMETERIZED_TYPE||
//                    tmp.getAstNode().getNodeType() == ASTNode.SIMPLE_TYPE){
            if(false){
                traverseNodeSubTree(tmp,result1,changePacket.getChangeSet1());
            }
        }
    }

    @MethodLookupTbl(key = TraverseWays.BottomUp.METHOD_SIGNATURE)
    public static void traverseMethodSignature(Tree node, List<Action> result1, ChangePacket changePacket){
        assert(node.getDoAction() == null);
        if(changePacket.getChangeSet1()==null){
            changePacket.initChangeSet1();
        }
        changePacket.getChangeSet1().add(ActionConstants.NULLACTION);
        List<ITree> children = node.getChildren();
        for(ITree child:children){
            Tree tmp = (Tree) child;
            if (Global.astNodeUtil.isBlock(tmp.getNode())) {
                break;
            }
            traverseNodeSubTree(tmp,result1,changePacket.getChangeSet1());
        }
    }

    /**
     * 最简单的从father node往下找edit 节点，然后标记
     * @param fafather root节点
     * @param editAction editAction
     * @param changePacket changePacket
     */
    @MethodLookupTbl(key = TraverseWays.BottomUp.FATHER_NODE_GET_SAME_NODE_ACTIONS)
    public static void traverseFatherNodeGetSameNodeActions(Tree fafather, List<Action> editAction, ChangePacket changePacket){
        if(changePacket.getChangeSet1()==null){
            changePacket.initChangeSet1();
        }
        traverseNodeSubTree(fafather,editAction,changePacket.getChangeSet1());
    }

    @MethodLookupTbl(key = TraverseWays.BottomUp.IF_PREDICATE)
    public static void traverseIfPredicate(Tree node, List<Action> result1, ChangePacket changePacket){
//        assert(node.getDoAction() == null);
        if(changePacket.getChangeSet1()==null){
            changePacket.initChangeSet1();
        }
        changePacket.getChangeSet1().add(ActionConstants.NULLACTION);
        List<ITree> children = node.getChildren();
        int i;
        for(i =0; i<children.size(); i++){
            Tree tmp = (Tree) children.get(i);
            if (Global.astNodeUtil.isBlock(tmp.getNode())) {
                break;
            }
        }
        int bound;
        if(i!=children.size()-1){
            bound=i;

        }else{
            bound=children.size()-1;
        }
        for(int j = 0; j<bound; j++){
            Tree tmp = (Tree) children.get(j);
            traverseNodeSubTree(tmp,result1,changePacket.getChangeSet1());
        }
    }

    @MethodLookupTbl(key = TraverseWays.BottomUp.DO_WHILE)
    public static void traverseDoWhileCondition(Tree node, List<Action> result, ChangePacket changePacket){
//        assert(node.getDoAction() == null);
//        assert(node.getChildren().size()==2);
        changePacket.initChangeSet1();
        Tree secondChild = (Tree) node.getChild(1);
        traverseNodeSubTree(secondChild,result,changePacket.getChangeSet1());
    }

    @MethodLookupTbl(key = TraverseWays.BottomUp.SWITCH_CONDITION)
    public static void traverseSwitchCondition(Tree node, List<Action> result, ChangePacket changePacket){
        assert(node.getDoAction() == null);
        assert(node.getChildren().size()==2);
        changePacket.initChangeSet1();
        List<ITree> children = node.getChildren();
        for(ITree tmp:children){
            Tree tmp2 = (Tree) tmp;
            if (Global.astNodeUtil.isSwitchCase(tmp2.getNode())) {
                break;
            }
            traverseOneType(node,result,changePacket);
        }

    }


}
