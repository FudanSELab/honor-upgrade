package edu.fdu.se.core.miningactions.util;

import com.github.gumtreediff.actions.model.Action;
import com.github.gumtreediff.tree.ITree;
import com.github.gumtreediff.tree.Tree;
import edu.fdu.se.global.Global;
import edu.fdu.se.core.generatingactions.ActionConstants;
import edu.fdu.se.core.miningactions.bean.ChangePacket;
import edu.fdu.se.core.miningactions.bean.MyList;
import edu.fdu.se.lang.common.MethodLookupTbl;
import org.eclipse.jdt.core.dom.ASTNode;

import java.util.ArrayList;
import java.util.List;

/**
* Created by huangkaifeng on 2018/1/26.
 * up down
 */
public class DefaultTopDownTraversal extends BasicTreeTraversal{


    @MethodLookupTbl(key = TraverseWays.TopDown.ONE_TYPE)
    private static void traverseOneType(Action a, List<Action> result1, ChangePacket changePacket){
        traverseOneType(a.getNode(),result1,changePacket);
    }

    @MethodLookupTbl(key = TraverseWays.TopDown.CLASSD)
    public static void traverseClass(Action a, List<Action> result1, ChangePacket changePacket){
        ITree node = a.getNode();
        result1.add(a);
        List<ITree> children = node.getChildren();
        int i=0;
        for(; i<children.size(); i++){
            Tree t = (Tree) children.get(i);

            if (Global.astNodeUtil.isFieldDeclaration(t.getNode()) || Global.astNodeUtil.isMethodDeclaration(t.getNode())) {
                break;
            }
        }
        changePacket.initChangeSet1();
        changePacket.initChangeSet2();
        changePacket.getChangeSet1().add(a.getClass().getSimpleName());
        traverseNodeSubTreeInRange(node,0,i-1,result1,changePacket.getChangeSet1());

        List<Action> group2 = new MyList<>();
        List<String> changeType2 = new MyList<>();
        traverseNodeSubTreeInRange(node,i,children.size()-1,group2,changeType2);
        if(changeType2.contains(ActionConstants.NULLACTION)){

        }else{
            result1.addAll(group2);
            changePacket.getChangeSet2().addAll(changeType2);
        }

    }


    @MethodLookupTbl(key = TraverseWays.TopDown.FIELD)
    public static void traverseField(Action a, List<Action> result1, ChangePacket changePacket){
        traverseOneType(a,result1,changePacket);
    }

    @MethodLookupTbl(key = TraverseWays.TopDown.INITIALIZER)
    public static void traverseInitializer(Action a, List<Action> result1, ChangePacket changePacket){
        traverseOneType(a,result1,changePacket);
    }

    @MethodLookupTbl(key = TraverseWays.TopDown.METHOD)
    public static void traverseMethod(Action a, List<Action> result1, ChangePacket changePacket){
        ITree node = a.getNode();
        result1.add(a);
        List<ITree> children = node.getChildren();
        int i=0;
        for(; i<children.size(); i++){
            Tree t = (Tree) children.get(i);
            if (Global.astNodeUtil.isBlock(t.getNode())) {
                break;
            }
        }
        changePacket.initChangeSet1();
        changePacket.initChangeSet2();
        changePacket.getChangeSet1().add(a.getClass().getSimpleName());
        traverseNodeSubTreeInRange(node,0,i-1,result1,changePacket.getChangeSet1());


        List<Action> group2 = new MyList<>();
        List<String> changeType2 = new MyList<>();
        traverseNodeSubTreeInRange(node,i,children.size()-1,group2,changeType2);
        if(changeType2.contains(ActionConstants.NULLACTION)){

        }else{
            result1.addAll(group2);
            changePacket.getChangeSet2().addAll(changeType2);
        }

    }


//    public static void traverseTypeIIStatements(Action a,List<Action> result1,ChangePacket changePacket){
//        ITree node = a.getNode();
//        result1.add(a);
//        List<ITree> children = node.getChildren();
//        int i=0;
//        for(;i<children.size();i++){
//            Tree t = (Tree) children.get(i);
//            if(t.getAstNode().getNodeType() == ASTNode.BLOCK  || t.getAstClass().getSimpleName().endsWith("Statement")){
//                break;
//            }
//        }
//        changePacket.initChangeSet1();
//        changePacket.initChangeSet2();
//        changePacket.getChangeSet1().add(a.getClass().getSimpleName());
//        traverseNodeSubTreeInRange(node,0,i-1,result1,changePacket.getChangeSet1());
//        traverseNodeSubTreeInRange(node,i,children.size()-1,result1,changePacket.getChangeSet2());
//
//    }

    @MethodLookupTbl(key = TraverseWays.TopDown.IF)
    public static void traverseIf(Action a, List<Action> result1, ChangePacket changePacket){
        ITree node = a.getNode();
        result1.add(a);
        int type = 0;
        List<ITree> children = node.getChildren();
        int i=0;
        for(; i<children.size(); i++){
            Tree t = (Tree) children.get(i);
            if (Global.astNodeUtil.isBlock(t.getNode())) {
                type = 1;
                break;
            }else if(t.getAstClass().getSimpleName().endsWith("Statement")){
                type = 2;
                break;
            }
        }
        changePacket.initChangeSet1();
        changePacket.initChangeSet2();
        changePacket.getChangeSet1().add(a.getClass().getSimpleName());
        traverseNodeSubTreeInRange(node,0,i-1,result1,changePacket.getChangeSet1());
        List<Action> bodyResult = new ArrayList<>();
        List<String> changesSet = new MyList<>();
        if(type==1){
            Tree block  = (Tree) node.getChild(i);
            if(block.getDoAction()!=null){
                for(Action aa:block.getDoAction()){
                    if(aa.getClass().equals(a.getClass())){
                        result1.add(aa);
                    }
                }
            }
            traverseNodeChildrenSubTree(block,bodyResult,changesSet);
            traverseNodeSubTreeInRange(node,i+1,children.size()-1,bodyResult,changesSet);
        }else{
            traverseNodeSubTreeInRange(node,i,children.size()-1,bodyResult,changesSet);
        }
        if (changesSet.contains(ActionConstants.NULLACTION) || changePacket.getChangeSet1().contains(ActionConstants.NULLACTION)) {

        }else{
            result1.addAll(bodyResult);
            changePacket.getChangeSet2().addAll(changesSet);
        }

    }


    @MethodLookupTbl(key = TraverseWays.TopDown.TYPE_I)
    public static void traverseTypeIStatements(Action a, List<Action> result1, ChangePacket changePacket){
        traverseOneType(a,result1,changePacket);
    }

    @MethodLookupTbl(key = TraverseWays.TopDown.TRY)
    public static void traverseTryStatements(Action a,List<Action> result,ChangePacket changePacket){
        ITree node = a.getNode();
        result.add(a);
        List<ITree> children = node.getChildren();
        changePacket.initChangeSet1();
        changePacket.initChangeSet2();
        changePacket.getChangeSet1().add(a.getClass().getSimpleName());
        int i=0;
        for(;i<children.size();i++){
            Tree t = (Tree) children.get(i);
            if (Global.astNodeUtil.getNodeTypeId(t.getNode()) == ASTNode.BLOCK) {
                traverseNodeChildrenSubTree(t,result,changePacket.getChangeSet2());
            }else{
                traverseNodeSubTree(t,result,changePacket.getChangeSet1());
            }
        }


    }

    @MethodLookupTbl(key = TraverseWays.TopDown.SWITCH)
    public static void traverseSwitchStatements(Action a,List<Action> result1,ChangePacket changePacket){
        ITree node = a.getNode();
        result1.add(a);
        List<ITree> children = node.getChildren();
        int i=0;
        for(;i<children.size();i++){
            Tree t = (Tree) children.get(i);
            if (Global.astNodeUtil.getNodeTypeId(t.getNode()) == ASTNode.SWITCH_CASE) {
                break;
            }
        }
        changePacket.initChangeSet1();
        changePacket.initChangeSet2();
        changePacket.getChangeSet1().add(a.getClass().getSimpleName());
        traverseNodeSubTreeInRange(node,0,i-1,result1,changePacket.getChangeSet1());
        traverseNodeSubTreeInRange(node,i,children.size()-1,result1,changePacket.getChangeSet2());

    }

    @MethodLookupTbl(key = TraverseWays.TopDown.SWITCH_CASE)
    public static void traverseSwitchCase(Action a, List<Action> result1, ChangePacket changePacket){
        traverseOneType(a,result1,changePacket);
    }



}
