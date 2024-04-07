package edu.fdu.se.core.miningchangeentity.base;

import com.github.gumtreediff.actions.model.Move;
import com.github.gumtreediff.tree.Tree;
import edu.fdu.se.core.miningactions.bean.MiningActionData;
import edu.fdu.se.global.Global;
import edu.fdu.se.core.links.linkbean.LinkBean;
import edu.fdu.se.core.miningactions.bean.MyRange;
import edu.fdu.se.core.miningactions.util.AstRelations;
import edu.fdu.se.core.miningchangeentity.generator.ClusteredActionBean;

/**
 * Created by huangkaifeng on 2018/1/16.
 * 父类 其他的Entity都继承于此Entity
 */
public abstract class ChangeEntity {


    public int changeEntityId;

    /**
     * 记录节点和它的father节点以及节点下的所有action
     */
    public ClusteredActionBean clusteredActionBean;

    public StageIIBean stageIIBean;
    public FrontData frontData;
    public LinkBean linkBean;
    public MyRange lineRange;


    @Override
    public String toString() {
        return changeEntityId + ". " + this.stageIIBean.toString();
    }



    private void init(){
        changeEntityId = Global.changeEntityId;
        Global.changeEntityId++;
        frontData = new FrontData();
        stageIIBean = new StageIIBean();
        frontData.setChangeEntityId(changeEntityId);

    }

    public void initDefs(MiningActionData mad){

    }



    /**
     * 预处理
     * @param location
     * @param changeType
     * @param myRange
     */
    public ChangeEntity(String location,String changeType,MyRange myRange){
        init();
        this.lineRange = myRange;
        if(myRange != null ){
            this.stageIIBean.setLineRange("("+this.lineRange.startLineNo +","+ this.lineRange.endLineNo+")");
        }
        //myflag
        else {
            this.stageIIBean.setLineRange("(0,0)");
        }
        this.stageIIBean.setEntityCreationStage(ChangeEntityDesc.StageIIGenStage.ENTITY_GENERATION_STAGE_PRE_DIFF);
        this.stageIIBean.setGranularity(ChangeEntityDesc.StageIIGranularity.GRANULARITY_MEMBER);
        this.stageIIBean.setOpt(ChangeEntityDesc.getKeyNameByValue(changeType));
    }


    public ChangeEntity(ClusteredActionBean bean){
        init();
        this.clusteredActionBean = bean;
        if(bean.curAction instanceof Move){
            this.lineRange = Global.astNodeUtil.getRange(bean.curAction.getNode(),bean.nodeType);
        }else{
            this.lineRange = Global.astNodeUtil.getRange(bean.fafather,bean.nodeType);
        }
    }

    public ChangeEntity(){
        init();
    }


    public int getChangeEntityId() {
        return changeEntityId;
    }

    public ClusteredActionBean getClusteredActionBean() {
        return clusteredActionBean;
    }

    public FrontData getFrontData() {
        return frontData;
    }

    public StageIIBean getStageIIBean() {
        return stageIIBean;
    }

    public LinkBean getLinkBean() {
        return linkBean;
    }

    public MyRange getLineRange() {
        return lineRange;
    }


}
