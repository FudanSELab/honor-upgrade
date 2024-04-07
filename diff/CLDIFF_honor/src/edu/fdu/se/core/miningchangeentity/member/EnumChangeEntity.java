package edu.fdu.se.core.miningchangeentity.member;

import edu.fdu.se.global.Global;
import edu.fdu.se.core.miningactions.bean.MyRange;
import edu.fdu.se.core.miningchangeentity.generator.ClusteredActionBean;
import edu.fdu.se.core.miningchangeentity.base.ChangeEntityDesc;
import edu.fdu.se.core.miningchangeentity.base.MemberPlusChangeEntity;
import edu.fdu.se.core.preprocessingfile.data.BodyDeclarationPair;

import java.util.List;

/**
 * Created by huangkaifeng on 2018/3/21.
 *
 */
public class EnumChangeEntity extends MemberPlusChangeEntity{

    public EnumChangeEntity(ClusteredActionBean bean){
        super(bean);
    }

    final public static String enumStr = "Enum";

    public List<String> variableList;
    public List<String> methodList;
    public MyRange dstRange;

    public EnumChangeEntity(BodyDeclarationPair bodyDeclarationPair, String changeType, MyRange myRange1,MyRange myRange2){
        super(bodyDeclarationPair.getCanonicalName().getPrefixName(),changeType,myRange1);
        Object ed = bodyDeclarationPair.getBodyDeclaration();
        this.stageIIBean.setCanonicalName(bodyDeclarationPair.getCanonicalName());
        this.stageIIBean.setChangeEntity(ChangeEntityDesc.StageIIENTITY.ENTITY_ENUM);
        this.stageIIBean.setThumbnail(bodyDeclarationPair.getCanonicalName().getSelfName());
        this.dstRange = myRange2;

    }

    public EnumChangeEntity(BodyDeclarationPair bodyDeclarationPair, String changeType, MyRange myRange1){
        super(bodyDeclarationPair.getCanonicalName().getPrefixName(),changeType,myRange1);
        this.stageIIBean.setCanonicalName(bodyDeclarationPair.getCanonicalName());
        this.stageIIBean.setCanonicalName(bodyDeclarationPair.getCanonicalName());
        this.stageIIBean.setChangeEntity(ChangeEntityDesc.StageIIENTITY.ENTITY_ENUM);
        this.stageIIBean.setThumbnail(bodyDeclarationPair.getCanonicalName().getSelfName());

    }


}
