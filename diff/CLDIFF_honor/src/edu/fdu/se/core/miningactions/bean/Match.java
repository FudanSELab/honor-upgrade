package edu.fdu.se.core.miningactions.bean;

import com.github.gumtreediff.actions.model.Action;
import com.github.gumtreediff.actions.model.Move;
import com.github.gumtreediff.tree.Tree;
import edu.fdu.se.core.miningactions.util.*;
import edu.fdu.se.core.miningchangeentity.base.CanonicalName;
import edu.fdu.se.core.miningchangeentity.generator.ClusteredActionBean;
import edu.fdu.se.core.miningchangeentity.base.ChangeEntity;
import edu.fdu.se.core.miningchangeentity.base.ChangeEntityDesc;
import edu.fdu.se.core.miningchangeentity.base.StageIIBean;
import edu.fdu.se.global.Constants;
import edu.fdu.se.global.Global;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class Match {

    public ChangePacket changePacket;
    public List<Action> subActions;

    public Match(){

    }

    public ClusteredActionBean mBean;

    /**
     * Top down
     *
     * @param curAction
     */
    public void newBean(Action curAction) {
        mBean = new ClusteredActionBean(ChangeEntityDesc.StageITraverseType.TRAVERSE_TOP_DOWN, curAction, subActions, changePacket);

    }

    /**
     * bottom up
     *
     * @param curAction
     * @param queryFather
     * @param treeType
     */
    public void newBean(Action curAction, Tree queryFather, int treeType) {
        mBean = new ClusteredActionBean(ChangeEntityDesc.StageITraverseType.TRAVERSE_BOTTOM_UP, curAction, subActions, changePacket, queryFather, treeType);

    }

    public void init(){
        changePacket = new ChangePacket();
        subActions = new ArrayList<>();
    }

    public void initFromCurrEntity(ChangeEntity changeEntity){
        changePacket = changeEntity.clusteredActionBean.changePacket;
        subActions = changeEntity.clusteredActionBean.actions;
    }


    /**
     * @param stageIIBean
     * @param creationStage
     * @param entityGranularity
     * @param entityName
     * @param optName
     * @param lineRange
     * @param location
     */
    public void setStageIIBean(StageIIBean stageIIBean, String creationStage, String entityGranularity, String optName, String entityName, String lineRange, CanonicalName location, String subEntity) {
        stageIIBean.setEntityCreationStage(creationStage);
        stageIIBean.setGranularity(entityGranularity);
        stageIIBean.setOpt(optName);
        stageIIBean.setChangeEntity(entityName);
        stageIIBean.setSubEntity(null);
        stageIIBean.setLineRange(lineRange);
        stageIIBean.setCanonicalName(location);
        stageIIBean.setSubEntity(subEntity);
    }


    /**
     * top down
     *
     * @param mad
     * @param action
     * @param traverseWays
     * @param booleanFlag  -1: direct, no check; 1: check true; 0: check false
     */
    public void traverseTopDown(MiningActionData mad, Action action, int traverseWays, int booleanFlag) {
        if (booleanFlag == -2) {
            return;
        }
        if (booleanFlag != -1) {
            boolean param = booleanFlag == 1;
            boolean ret = BasicTreeTraversal.traverseWhenActionIsMove(action, subActions, changePacket, param);
            if (ret) {
                mad.setActionTraversedMap(subActions);
                return;
            }
        }
        String topDownClass = TraverseWays.TOPDOWNCLASSPATH;
        String methodName = Global.iLookupTbl.callTable.get(Constants.TRAVERSE_WAYS).get(traverseWays);
        try {
            Class<?> printClass = Class.forName(topDownClass);
            Method printMethod = printClass.getMethod(methodName, Action.class, List.class, ChangePacket.class);
            printMethod.invoke(printClass.getDeclaredConstructor().newInstance(), action, subActions, changePacket);
        } catch (Exception e) {
            e.printStackTrace();
        }
        mad.setActionTraversedMap(subActions);
    }

    /**
     *
     * @param fp
     * @param a
     * @param traverseFather
     * @param traverseWays
     * @param flag
     */
    public void traverseBottomUp(MiningActionData fp, Action a, Tree traverseFather, int traverseWays, boolean flag) {
        bottomUpReflectionCall(a, traverseFather, this.subActions, changePacket, traverseWays,flag);
        fp.setActionTraversedMap(subActions);
    }

    /**
     * @param fp
     * @param a
     * @param traverseFather
     * @param flag
     */
    public void traverseBottomUpCurr(MiningActionData fp, Action a, Tree traverseFather, int traverseWays, boolean flag) {
        List<Action> newActions = new ArrayList<>();
        bottomUpReflectionCall(a, traverseFather, newActions, changePacket, traverseWays,flag);
        for (Action tmp : newActions) {
            if (fp.mActionsMap.getAllActionMap().get(tmp) == 1) {
                continue;
            }
            subActions.add(tmp);
        }
        fp.setActionTraversedMap(newActions);
    }


    private void bottomUpReflectionCall(Action a, Tree traverseFather, List<Action> newActions, ChangePacket changePacket, int traverseWays, boolean flag) {
        if (!BasicTreeTraversal.traverseWhenActionIsMove(a, newActions, changePacket, flag)) {
            String bottomUpClass = TraverseWays.BOTTOMUPCLASSPATH;
            String methodName = Global.iLookupTbl.callTable.get(Constants.TRAVERSE_WAYS).get(traverseWays);
            try {
                Class<?> printClass = Class.forName(bottomUpClass);
                Method printMethod = printClass.getMethod(methodName, Tree.class, List.class, ChangePacket.class);
                printMethod.invoke(printClass.getDeclaredConstructor().newInstance(), traverseFather, newActions, changePacket);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }




    public String getBottomUpOptName(Action a){
        // either move or change
        if(a instanceof Move){
            return ChangeEntityDesc.StageIIOpt.OPT_CHANGE_MOVE;
        }else{
            return ChangeEntityDesc.StageIIOpt.OPT_CHANGE;
        }
    }

    /**
     * @param a
     * @param index -1:default
     * @return
     */
    public String getBottomUpEntityName(Action a, int index){
        // either move or change
        if(a instanceof Move){
            return ((Tree)a.getNode()).getAstClass().getSimpleName();
        }else{
            return getMultiple(index);
        }
    }

    private String getMultiple(int index) {
        String content = Global.iLookupTbl.classNameToEntity.get(this.getClass().getSimpleName());
        String[] contentData = content.split(ChangeEntityDesc.SPLITTER);
        if (index >= 0 && index < contentData.length) {
            return contentData[index];
        }
        return null;
    }


    public String getTopDownEntityName(int index) {
        return getMultiple(index);
    }

    public String getLineRange(ChangeEntity code) {
        return code.lineRange.toString();
    }


    public String changeEntityDescString(Action a) {
        return ChangeEntityDesc.getChangeEntityDescString(a);
    }


}
