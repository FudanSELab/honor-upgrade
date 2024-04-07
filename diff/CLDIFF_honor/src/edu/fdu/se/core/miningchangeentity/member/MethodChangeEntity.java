package edu.fdu.se.core.miningchangeentity.member;

import edu.fdu.se.core.miningactions.bean.MiningActionData;
import edu.fdu.se.global.Global;
import edu.fdu.se.core.miningactions.bean.MyRange;
import edu.fdu.se.core.miningchangeentity.generator.ClusteredActionBean;
import edu.fdu.se.core.miningchangeentity.base.ChangeEntityDesc;
import edu.fdu.se.core.miningchangeentity.base.MemberPlusChangeEntity;
import edu.fdu.se.core.preprocessingfile.data.BodyDeclarationPair;

/**
 * Created by huangkaifeng on 2018/1/22.
 *
 */
public class MethodChangeEntity extends MemberPlusChangeEntity {

    private String methodName;

    public MethodChangeEntity(ClusteredActionBean bean){
        super(bean);
    }

    /**
     * @param bodyDeclarationPair
     * @param changeType
     * @param myRange
     */
    public MethodChangeEntity(BodyDeclarationPair bodyDeclarationPair, String changeType, MyRange myRange){
        super(bodyDeclarationPair.getCanonicalName().getPrefixName(),changeType,myRange);
        this.stageIIBean.setCanonicalName(bodyDeclarationPair.getCanonicalName());
        this.stageIIBean.setChangeEntity(ChangeEntityDesc.StageIIENTITY.ENTITY_METHOD);
        this.stageIIBean.setThumbnail(bodyDeclarationPair.getCanonicalName().getSelfName());
        this.bodyDeclarationPair = bodyDeclarationPair;
    }


    public String getMethodName() {
        return this.methodName;
    }

    public void setMethodName(String method) {
        this.methodName = method;
    }

    @Override
    public void initDefs(MiningActionData mad) {
        Global.processUtil.initDefs(this, mad);
    }
}
