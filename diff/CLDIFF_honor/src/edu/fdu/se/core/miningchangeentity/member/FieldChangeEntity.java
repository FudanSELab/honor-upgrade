package edu.fdu.se.core.miningchangeentity.member;

import edu.fdu.se.core.miningactions.bean.MiningActionData;
import edu.fdu.se.global.Global;
import edu.fdu.se.core.miningactions.bean.MyRange;
import edu.fdu.se.core.miningchangeentity.generator.ClusteredActionBean;
import edu.fdu.se.core.miningchangeentity.base.ChangeEntityDesc;
import edu.fdu.se.core.miningchangeentity.base.MemberPlusChangeEntity;
import edu.fdu.se.core.preprocessingfile.data.BodyDeclarationPair;

import java.util.List;

/**
 * Created by huangkaifeng on 2018/1/16.
 *
 */
public class FieldChangeEntity extends MemberPlusChangeEntity {

    private String fieldNames;// a,b,c
    /**
     * 预处理识别的
     */
    public FieldChangeEntity(BodyDeclarationPair fieldDeclarationPair, String changeType, MyRange myRange){
        super(fieldDeclarationPair.getCanonicalName().getPrefixName(),changeType,myRange);

        this.stageIIBean.setChangeEntity(ChangeEntityDesc.StageIIENTITY.ENTITY_FIELD);
        this.bodyDeclarationPair = fieldDeclarationPair;
        this.stageIIBean.setCanonicalName(fieldDeclarationPair.getCanonicalName());
        this.stageIIBean.setCanonicalName(bodyDeclarationPair.getCanonicalName());
        this.stageIIBean.setThumbnail(fieldDeclarationPair.getCanonicalName().getSelfName());

    }

    @Override
    public void initDefs(MiningActionData mad) {
        Global.processUtil.initDefs(this, mad);
    }

    public String getFieldNames() {
        return this.fieldNames;
    }

    /**
     * gumtree 识别的
     * @param bean
     */
    public FieldChangeEntity(ClusteredActionBean bean){
        super(bean);
    }


    public void setFieldName(List<String> fieldName) {
        this.fieldNames = "";
        for (String name : fieldName) {
            this.fieldNames += name;
        }
    }





}
