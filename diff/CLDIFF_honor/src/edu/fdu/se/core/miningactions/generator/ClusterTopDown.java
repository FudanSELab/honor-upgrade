package edu.fdu.se.core.miningactions.generator;

import com.github.gumtreediff.actions.model.Action;
import com.github.gumtreediff.actions.model.Insert;
import com.github.gumtreediff.actions.model.Move;
import com.github.gumtreediff.tree.ITree;
import com.github.gumtreediff.tree.Tree;
import edu.fdu.se.global.Constants;
import edu.fdu.se.global.Global;
import edu.fdu.se.core.miningactions.bean.MiningActionData;
import edu.fdu.se.core.miningchangeentity.base.ChangeEntityDesc;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by huangkaifeng on 2018/2/2.
 * Statement/Declaration/控制流的子结构
 */
public class ClusterTopDown extends AbstractCluster {

    /**
     * 主要是装入指定类型的actionList
     */
    public ClusterTopDown(Class mClazz, MiningActionData miningActionData) {
        super(mClazz, miningActionData);
    }


    public void doClusterTopDown() {
        if (Constants.GRANULARITY.TYPE.equals(Global.granularity)) {
            traverseActionsToFindSubTreeByRootNode(Constants.GRANULARITY.TYPE);
        } else if (Constants.GRANULARITY.DECLARATION.equals(Global.granularity)) {
            traverseActionsToFindSubTreeByRootNode(Constants.GRANULARITY.TYPE);
            traverseActionsToFindSubTreeByRootNode(Constants.GRANULARITY.DECLARATION);

        } else if (Constants.GRANULARITY.STATEMENT.equals(Global.granularity)) {
            traverseActionsToFindSubTreeByRootNode(Constants.GRANULARITY.TYPE);
            traverseActionsToFindSubTreeByRootNode(Constants.GRANULARITY.DECLARATION);
            traverseActionsToFindSubTreeByRootNode(Constants.GRANULARITY.STATEMENT);

        } else if (Constants.GRANULARITY.EXPRESSION.equals(Global.granularity)) {
            traverseActionsToFindSubTreeByRootNode(Constants.GRANULARITY.TYPE);
            traverseActionsToFindSubTreeByRootNode(Constants.GRANULARITY.DECLARATION);
            traverseActionsToFindSubTreeByRootNode(Constants.GRANULARITY.STATEMENT);
            traverseActionsToFindSubTreeByRootNode(Constants.GRANULARITY.EXPRESSION);
        }
    }

    public void traverseActionsToFindSubTreeByRootNode(String rootNodeGranularity) {
        for (Action action : this.actionList) {
            if (isActionVisited(action)) {
                continue;
            }
            Tree insNode = (Tree) action.getNode();
            int nodeID = Global.astNodeUtil.getNodeTypeId(insNode.getNode());
            if (Global.processUtil.matchEntityTopDown(mad, action, nodeID, rootNodeGranularity) == 1) {

            }
        }
    }

    public void passGumtreePalsePositiveMoves() {
        for (Action action : this.actionList) {
            if (isActionVisited(action)) {
                continue;
            }
            Tree moveSrcNode = (Tree) action.getNode();
            Tree moveDstNode = (Tree) mad.getMappedCurrOfPrevNode(moveSrcNode);
            //todo 需要看懂规则
            rule1(moveSrcNode, moveDstNode, action);
            rule2(moveSrcNode, moveDstNode, action);
            rule3(moveSrcNode, moveDstNode, action);
        }
    }

    private void rule3(Tree src, Tree dst, Action a) {
        if (!src.getLabel().equals(dst.getLabel())) {
            mad.setActionTraversedMap(a);
            // label not same, treat as gumtree's false positive
        }
    }

    private void rule2(Tree moveSrcNode, Tree moveDstNode, Action a) {
        List<ITree> children = moveSrcNode.getChildren();
        Set<Class> aSet = new HashSet<>();
        for (ITree t : children) {
            Tree tt = (Tree) t;
            List<Action> does = tt.getDoAction();
            if (does != null) {
                does.forEach(aaa -> aSet.add(aaa.getClass()));
            }
        }
        if (aSet.size() == 1 && aSet.contains(Move.class)) {
            List<ITree> dstChildren = moveDstNode.getChildren();
            Set<Class> bSet = new HashSet<>();
            for (ITree t : dstChildren) {
                Tree tt = (Tree) t;
                List<Action> dstAc = tt.getDoAction();
                if (dstAc != null) {
                    dstAc.forEach(ccc -> bSet.add(ccc.getClass()));
                }
            }
            if (bSet.size() == 1 && bSet.contains(Insert.class)) {
                // todo srcNode delete? add or ignore?
                //
                mad.setActionTraversedMap(a);
                Insert insert = new Insert(moveDstNode, moveDstNode.getParent(), moveDstNode.getPos());
                moveDstNode.setDoAction(insert);
                this.mad.mActionsMap.addAction(insert);
                this.mad.mActionsMap.addActionMap(insert);
            }
        }

    }

    private void rule1(Tree moveSrcNode, Tree moveDstNode, Action action) {
        List<Integer> posA = new ArrayList<>();
        List<Integer> posB = new ArrayList<>();
        //获得所有是block的父节点
        //posA是父节点的位置
        List<Tree> treeListA = nonBlockParents(moveSrcNode, posA);
        List<Tree> treeListB = nonBlockParents(moveDstNode, posB);
        if (treeListA.size() != treeListB.size()) {
            return;
        }
        Tree ta = null;
        Tree tb = null;
        int flag = 0;
        for (int i = 0; i < treeListA.size(); i++) {
            ta = treeListA.get(i);
            tb = treeListB.get(i);
            if (Global.astNodeUtil.getNodeTypeId(ta.getNode()) != Global.astNodeUtil.getNodeTypeId(tb.getNode())) {
                flag = 1;
                break;
            }
            if (posA.get(i).intValue() != posB.get(i).intValue()) {
                flag = 1;
                break;
            }
        }
        if (flag == 1) {
            return;
        }
        if (ta == null || tb == null) {
            return;
        }
        if (ta.getTreePrevOrCurr() == ChangeEntityDesc.TreeType.PREV_TREE_NODE) {
            Tree tmp = (Tree) mad.getMappedCurrOfPrevNode(ta);
            if (tmp != null) {
                if (tmp.equals(tb)) {
                    mad.setActionTraversedMap(action);
                }
            }
        }
    }

    private List<Tree> nonBlockParents(Tree t, List<Integer> posList) {
        List<Tree> list = new ArrayList<>();
        Tree parent = t;
        do {
            t = parent;
            parent = (Tree) parent.getParent();
            int pos = parent.getChildPosition(t);
            posList.add(pos);
            list.add(parent);
        } while (Global.astNodeUtil.isBlock(parent.getNode()));
        return list;
    }


//    public int matchEntityTopDown(Action a,int type) {
//        return Global.util.matchEntityTopDown(mad,a,type);
//    }
}
