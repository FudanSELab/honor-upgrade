package edu.fdu.se.core.miningchangeentity.base;

import edu.fdu.se.core.miningactions.bean.MyRange;
import edu.fdu.se.core.miningchangeentity.generator.ClusteredActionBean;
import edu.fdu.se.core.preprocessingfile.data.BodyDeclarationPair;

/**
 * Created by huangkaifeng on 2018/2/8.
 *
 */
public class MemberPlusChangeEntity extends ChangeEntity {

    public BodyDeclarationPair bodyDeclarationPair;

    public MemberPlusChangeEntity(ClusteredActionBean bean){
        super(bean);
    }

    public MemberPlusChangeEntity(String location,String changeType,MyRange myRange){
        super(location,changeType,myRange);

    }




}
