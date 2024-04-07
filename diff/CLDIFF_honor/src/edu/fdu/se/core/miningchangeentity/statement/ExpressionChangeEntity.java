package edu.fdu.se.core.miningchangeentity.statement;

import edu.fdu.se.core.miningchangeentity.generator.ClusteredActionBean;
import edu.fdu.se.core.miningchangeentity.base.ChangeEntityDesc;
import edu.fdu.se.core.miningchangeentity.base.StatementPlusChangeEntity;

/**
 * Created by huangkaifeng on 2018/1/23.
 *
 */
public class ExpressionChangeEntity extends StatementPlusChangeEntity{


    public ExpressionChangeEntity(ClusteredActionBean bean) {
        super(bean);
    }


    public String toString2(){
        StringBuilder sb = new StringBuilder();
        sb.append(this.changeEntityId);
        sb.append(". ");
        sb.append(this.stageIIBean.getOpt());
        sb.append(" ");
        sb.append(this.stageIIBean.getChangeEntity());
        if(this.stageIIBean.getOpt().equals(ChangeEntityDesc.StageIIOpt.OPT_CHANGE)){
            sb.append("'s expression ");
//            sb.append("");
            sb.append("with/by...");
        }
        return sb.toString();
    }






}
