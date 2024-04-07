package edu.fdu.se.core.miningactions.declaration;

import com.github.gumtreediff.actions.model.*;
import com.github.gumtreediff.tree.ITree;
import com.github.gumtreediff.tree.Tree;

import edu.fdu.se.global.Global;
import edu.fdu.se.core.miningactions.bean.MiningActionData;
import edu.fdu.se.core.miningactions.util.BasicTreeTraversal;
import edu.fdu.se.core.miningchangeentity.base.ChangeEntity;
import edu.fdu.se.core.miningchangeentity.base.ChangeEntityDesc;

import java.util.List;

public class MatchNonSubTrees {
    /**
     * @return queryTreeNode srcTreeNode dstTreeNode
     */
    private static Object[] getThreeTreeNode(List<Integer> commonRootNodeTypes, Action a, MiningActionData fp) {
        ITree fafather1 = BasicTreeTraversal.findCommonRootNode(a.getNode(), commonRootNodeTypes);
        ITree[] fathers = BasicTreeTraversal.getMappedFafatherNode(fp, a, fafather1);
        Tree srcFather = (Tree) fathers[0];
        Tree dstFather = (Tree) fathers[1];
        Tree queryFather = null;
        int treeType = -1;
        if (srcFather == null && dstFather != null) {
            queryFather = dstFather;
            treeType = ChangeEntityDesc.TreeType.CURR_TREE_NODE;
        } else if (srcFather != null) {
            queryFather = srcFather;
            treeType = ChangeEntityDesc.TreeType.PREV_TREE_NODE;
        }
        Object[] result = {queryFather, srcFather, dstFather, new Integer(treeType)};
        return result;
    }

    public static void matchNonCommonRootNode(MiningActionData fp, Action a, List<Integer> commonRootNodeTypes) {
//        ITree fafather1 = BasicTreeTraversal.findFafatherNode(a.getNode());
        Object[] result = getThreeTreeNode(commonRootNodeTypes, a, fp);
        Tree queryFather = (Tree) result[0];
        Tree srcFather = (Tree) result[1];
        Tree dstFather = (Tree) result[2];
        int treeType = ((Integer) result[3]).intValue();
        ChangeEntity changeEntity;
        changeEntity = fp.getEntityByNode(queryFather);
        if (changeEntity != null && changeEntity.clusteredActionBean.curAction instanceof Move) {
            changeEntity = null;
        }
        if (changeEntity == null || (a instanceof Move)) {
            if (a instanceof Insert) {
                matchNodeNewEntity(fp, a, queryFather, treeType, dstFather, commonRootNodeTypes);
            } else {
                matchNodeNewEntity(fp, a, queryFather, treeType, srcFather, commonRootNodeTypes);
            }
        } else {
            if (a instanceof Insert) {
                matchXXXChangeCurEntity(fp, a, changeEntity, dstFather);
            } else {
                matchXXXChangeCurEntity(fp, a, changeEntity, srcFather);
            }
        }

    }


    public static void matchNodeNewEntity(MiningActionData fp, Action a, Tree queryFather, int treeType, Tree traverseFather, List<Integer> commonRootNodeTypes) {
        Global.processUtil.matchEntityBottomUpNew(fp, a, queryFather, treeType, traverseFather, commonRootNodeTypes);
    }

    public static void matchXXXChangeCurEntity(MiningActionData fp, Action a, ChangeEntity changeEntity, Tree traverseFather) {
        Tree queryFather = changeEntity.clusteredActionBean.fafather;
        int nodeType = Global.astNodeUtil.getNodeTypeId(queryFather.getNode());
        Global.processUtil.matchEntityBottomUpCurr(fp, a, changeEntity, nodeType, traverseFather);

    }

}
