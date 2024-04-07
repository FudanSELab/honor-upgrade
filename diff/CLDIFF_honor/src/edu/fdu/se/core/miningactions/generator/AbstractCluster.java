package edu.fdu.se.core.miningactions.generator;

import com.github.gumtreediff.actions.model.Action;
import com.github.gumtreediff.actions.model.Delete;
import com.github.gumtreediff.actions.model.Insert;
import com.github.gumtreediff.actions.model.Move;
import edu.fdu.se.core.miningactions.bean.MiningActionData;

import java.util.List;

/**
 * Created by huangkaifeng on 2018/2/2.
 */
public class AbstractCluster {

    public List<Action> actionList;
    public MiningActionData mad;

    public AbstractCluster(Class mClazz, MiningActionData mminingActionData) {
        this.mad = mminingActionData;
        Class clazz = mClazz;
        if (Insert.class.equals(clazz)) {
            this.actionList = mminingActionData.mActionsMap.getInsertActions();
        } else if (Delete.class.equals(clazz)) {
            this.actionList = mminingActionData.mActionsMap.getDeleteActions();
        } else if (Move.class.equals(clazz)) {
            this.actionList = mminingActionData.mActionsMap.getMoveActions();
        } else {
            this.actionList = mminingActionData.mActionsMap.getUpdateActions();
        }
    }

    /**
     * 检查AllActionMap的key值，判断action是否已经被访问过（==1）
     */
    protected boolean isActionVisited(Action a){
        return mad.mActionsMap.getAllActionMap().get(a) == 1;
    }
}
