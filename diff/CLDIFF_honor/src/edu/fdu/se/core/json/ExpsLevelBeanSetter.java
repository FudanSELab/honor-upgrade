package edu.fdu.se.core.json;

import edu.fdu.se.core.miningactions.bean.MiningActionData;
import edu.fdu.se.core.miningchangeentity.base.ChangeEntity;
import edu.fdu.se.core.miningchangeentity.base.ChangeEntityDesc;
import edu.fdu.se.core.miningchangeentity.member.EnumChangeEntity;

import java.util.List;

public class ExpsLevelBeanSetter extends BeanSetter {

    public void setChangeEntityOpt(MiningActionData miningActionData) {
        setChangeEntityOptDetail(miningActionData);
    }

    public void setChangeEntitySubRange(MiningActionData mad) {
    }
}
