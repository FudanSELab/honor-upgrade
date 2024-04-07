package edu.fdu.se.core.miningactions.bean;

import com.github.gumtreediff.actions.model.Action;
import com.github.gumtreediff.actions.model.Delete;
import com.github.gumtreediff.actions.model.Insert;
import com.github.gumtreediff.tree.Tree;
import edu.fdu.se.core.miningchangeentity.base.CanonicalName;
import edu.fdu.se.core.miningchangeentity.base.ChangeEntity;
import edu.fdu.se.core.miningchangeentity.base.ChangeEntityDesc;
import edu.fdu.se.global.Global;

public class MatchDeclaration extends Match {

    public void setTopDown(ChangeEntity code, Action a, MiningActionData fp, int entityIndex) {
        String optName = changeEntityDescString(a);
        CanonicalName canonicalName = Global.astNodeUtil.getCanonicalNameFromTree((Tree) a.getNode());
        setStageIIBean(code.stageIIBean, ChangeEntityDesc.StageIIGenStage.ENTITY_GENERATION_STAGE_GT_TD,
                ChangeEntityDesc.StageIIGranularity.GRANULARITY_MEMBER, optName, getTopDownEntityName(entityIndex),
                code.lineRange.toString(), canonicalName, null);
        fp.addOneChangeEntity(code);

    }

    public void setBottomUpNew(ChangeEntity code, Action a, MiningActionData fp, int entityIndex) {
        String optName = getBottomUpOptName(a);
        String entityName = getBottomUpEntityName(a, entityIndex);
        CanonicalName canonicalName = Global.astNodeUtil.getCanonicalNameFromTree((Tree) a.getNode());
        setStageIIBean(code.stageIIBean, ChangeEntityDesc.StageIIGenStage.ENTITY_GENERATION_STAGE_GT_BUD,
                ChangeEntityDesc.StageIIGranularity.GRANULARITY_MEMBER,
                optName, entityName, getLineRange(code), canonicalName, null);
        fp.addOneChangeEntity(code);
    }

    public void setBottomUpNew1(ChangeEntity code, Action a, MiningActionData fp, int entityIndex){
        String optName = getBottomUpOptName(a);
        String entityName = getBottomUpEntityName(a, entityIndex);
        CanonicalName canonicalName = Global.astNodeUtil.getCanonicalNameFromTree((Tree) a.getNode());
        String subEntity;
        if(a instanceof Delete || a instanceof Insert){
            subEntity = a.toString().replace("SimpleType","class_or_interface");
            String name ="," + a.getNode().getLabel();
            subEntity += name;
        }
        else{
            subEntity = a.toString().replace("SimpleType","class_or_interface").replace("UPD","Update");
        }
        setStageIIBean(code.stageIIBean, ChangeEntityDesc.StageIIGenStage.ENTITY_GENERATION_STAGE_GT_BUD,
                ChangeEntityDesc.StageIIGranularity.GRANULARITY_MEMBER,
                optName, entityName, getLineRange(code), canonicalName, subEntity);
        fp.addOneChangeEntity(code);

    }
}
