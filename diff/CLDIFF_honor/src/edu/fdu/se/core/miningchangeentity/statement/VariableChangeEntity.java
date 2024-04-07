package edu.fdu.se.core.miningchangeentity.statement;

import edu.fdu.se.core.miningactions.bean.MiningActionData;
import edu.fdu.se.core.miningchangeentity.generator.ClusteredActionBean;
import edu.fdu.se.core.miningchangeentity.base.ChangeEntityDesc;
import edu.fdu.se.core.miningchangeentity.base.StatementPlusChangeEntity;
import edu.fdu.se.global.Global;

import java.util.List;

/**
 * Created by huangkaifeng on 2018/1/23.
 *
 */
public class VariableChangeEntity extends StatementPlusChangeEntity {

    final static public String VARIABLEDECLARATION = "VariableDeclaration";

    public VariableChangeEntity(ClusteredActionBean bean) {
        super(bean);
    }

    private List<String> variableNames;

    public List<String> getVariableNames() {
        return variableNames;
    }

    public void initDefs(MiningActionData mad) {
        Global.processUtil.initDefs(this,mad);
    }

    public String toString2(){
        StringBuilder sb = new StringBuilder();
        sb.append(this.changeEntityId);
        sb.append(". ");
        sb.append(this.stageIIBean.getOpt());
        sb.append(" ");
        sb.append(this.stageIIBean.getChangeEntity());
        if(this.stageIIBean.getOpt().equals(ChangeEntityDesc.StageIIOpt.OPT_CHANGE)){
            sb.append("  ");

            sb.append("with ");
        }
        return sb.toString();
    }


}
