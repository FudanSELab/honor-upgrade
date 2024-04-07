package edu.fdu.se.core.json;

import edu.fdu.se.core.miningactions.bean.MiningActionData;
import edu.fdu.se.core.miningchangeentity.base.ChangeEntity;
import edu.fdu.se.core.miningchangeentity.base.ChangeEntityDesc;

import java.util.List;

public class TypeLevelBeanSetter extends BeanSetter {

    public void setChangeEntityOpt(MiningActionData mad) {
        List<ChangeEntity> changeEntityList = mad.getChangeEntityList();
        if (changeEntityList.size() == 0) {
            return;
        }
        ChangeEntity changeEntity = changeEntityList.get(0);
        changeEntity.frontData.setChangeEntityId(changeEntity.changeEntityId);
        changeEntity.frontData.setKey("preprocess");
        changeEntity.frontData.setFile(ChangeEntityDesc.StageIIIFile.SRC_DST);
        String rangeStr = "($,$)" + "-" + "($,$)";
        changeEntity.frontData.setType1(ChangeEntityDesc.StageIIGranularity.GRANULARITY_CLASS);
        changeEntity.frontData.setType2(ChangeEntityDesc.StageIIOpt.OPT_CHANGE);
        changeEntity.frontData.setRange(rangeStr);
        changeEntity.frontData.setDisplayDesc("change ClassOrInterface");
    }

    public void setChangeEntitySubRange(MiningActionData mad) {

    }

}
