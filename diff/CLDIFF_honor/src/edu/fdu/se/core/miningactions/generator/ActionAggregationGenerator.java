package edu.fdu.se.core.miningactions.generator;

import com.github.gumtreediff.actions.model.*;
import edu.fdu.se.core.miningactions.bean.MiningActionData;
import edu.fdu.se.global.Constants;
import edu.fdu.se.global.Global;

/**
 * Created by huangkaifeng on 2018/1/13.
 *  GumTree 算法中用于聚类操作动作的类，主要作用是将相邻的操作动作聚类在一起，并生成聚类操作动作
 */
public class ActionAggregationGenerator {

    public void doCluster(MiningActionData mad) {
        if (Global.granularity.equals(Constants.GRANULARITY.TYPE)) {
            if (mad.getChangeEntityList().size() != 0) {
                // no need to cluster
                return;
            }
        }
        new ClusterTopDown(Move.class, mad).passGumtreePalsePositiveMoves();
        new ClusterTopDown(Move.class, mad).doClusterTopDown();
        new ClusterBottomUp(Move.class, mad).doClusterBottomUp();
        new ClusterTopDown(Insert.class, mad).doClusterTopDown();
        new ClusterTopDown(Delete.class, mad).doClusterTopDown();
        new ClusterBottomUp(Insert.class, mad).doClusterBottomUp();
        //find here
        new ClusterBottomUp(Delete.class, mad).doClusterBottomUp();
        if(Global.granularity.equals(Constants.GRANULARITY.STATEMENT)||Global.granularity.equals(Constants.GRANULARITY.DECLARATION)) {
            new ClusterBottomUp(Update.class, mad).doClusterBottomUp();
        }else if(Global.granularity.equals(Constants.GRANULARITY.EXPRESSION)){
            new ClusterTopDown(Update.class, mad).doClusterTopDown();
        }
    }


}
