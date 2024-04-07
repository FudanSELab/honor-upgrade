package edu.fdu.se.core.miningchangeentity.member;


import edu.fdu.se.core.miningactions.bean.MyRange;
import edu.fdu.se.core.miningchangeentity.generator.ClusteredActionBean;
import edu.fdu.se.core.miningchangeentity.base.ChangeEntityDesc;
import edu.fdu.se.core.miningchangeentity.base.MemberPlusChangeEntity;
import edu.fdu.se.core.preprocessingfile.data.BodyDeclarationPair;
import edu.fdu.se.global.Global;
import org.eclipse.jdt.core.dom.Initializer;

/**
 * Created by huangkaifeng on 2018/1/22.
 *
 */
public class InitializerChangeEntity extends MemberPlusChangeEntity {

    public InitializerChangeEntity(ClusteredActionBean bean){
        super(bean);
    }

    public InitializerChangeEntity(BodyDeclarationPair bodyDeclarationPair, String changeType, MyRange myRange){
        super(bodyDeclarationPair.getCanonicalName().getPrefixName(),changeType,myRange);
        Initializer iid = (Initializer) bodyDeclarationPair.getBodyDeclaration();
        this.stageIIBean.setCanonicalName(bodyDeclarationPair.getCanonicalName());
        this.stageIIBean.setCanonicalName(bodyDeclarationPair.getCanonicalName());
        this.stageIIBean.setChangeEntity(ChangeEntityDesc.StageIIENTITY.ENTITY_INITIALIZER);
        this.stageIIBean.setThumbnail("{}");

    }


}
