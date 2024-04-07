package edu.fdu.se.core.miningactions.generator;

import com.github.gumtreediff.actions.model.Action;
import edu.fdu.se.global.Global;
import edu.fdu.se.core.miningactions.declaration.MatchNonSubTrees;
import edu.fdu.se.core.miningactions.bean.MiningActionData;
import org.eclipse.jdt.core.dom.ASTNode;

import java.util.List;

/**
 * Created by huangkaifeng on 2018/2/2.
 */
public class ClusterBottomUp extends AbstractCluster {


    public ClusterBottomUp(Class mClazz, MiningActionData mminingActionData) {
        super(mClazz, mminingActionData);
    }

    public void doClusterBottomUp() {
        traverseActionsToGroupByCommonRootNode(Global.granularity);
    }

    public void traverseActionsToGroupByCommonRootNode(String rootNodeGranulatiry) {
        List<Integer> commonRootNodeList = GranularityTool.getCommonRootNodeList(rootNodeGranulatiry);
        for (Action a : this.actionList) {
            if (isActionVisited(a)) {
                continue;
            }
            //find in here
            MatchNonSubTrees.matchNonCommonRootNode(mad, a, commonRootNodeList);
        }
    }
}
