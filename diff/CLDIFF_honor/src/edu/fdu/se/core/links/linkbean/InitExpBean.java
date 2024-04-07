package edu.fdu.se.core.links.linkbean;

import com.github.gumtreediff.actions.model.Move;
import edu.fdu.se.core.miningactions.bean.MiningActionData;
import edu.fdu.se.core.miningchangeentity.base.ChangeEntity;
import edu.fdu.se.core.miningchangeentity.base.ExpressionPlusChangeEntity;

public class InitExpBean {


    public static void parse(ChangeEntity en, MiningActionData mad) {
        ExpressionPlusChangeEntity ce = (ExpressionPlusChangeEntity)en;
        InitStmtBean initStmt = new InitStmtBean();
        if(ce.clusteredActionBean.curAction instanceof Move){
            initStmt.parseMove(en,mad);
        }else{
            initStmt.parseNonMove(en,mad);
        }
    }



}
