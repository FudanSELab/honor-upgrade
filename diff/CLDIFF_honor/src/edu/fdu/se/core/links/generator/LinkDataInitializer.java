package edu.fdu.se.core.links.generator;

import edu.fdu.se.core.links.linkbean.*;
import edu.fdu.se.core.miningactions.bean.MiningActionData;
import edu.fdu.se.core.miningchangeentity.base.*;
import edu.fdu.se.core.miningchangeentity.member.ClassChangeEntity;
import edu.fdu.se.core.miningchangeentity.member.EnumChangeEntity;
import edu.fdu.se.core.miningchangeentity.member.FieldChangeEntity;
import edu.fdu.se.core.miningchangeentity.member.MethodChangeEntity;
import edu.fdu.se.core.miningchangeentity.statement.VariableChangeEntity;
import edu.fdu.se.global.Global;
import org.eclipse.jgit.diff.DiffEntry;

import java.util.List;

public class LinkDataInitializer {


    public static int init(ChangeEntityTreeContainer changeEntityTreeContainer, String fileName, MiningActionData mad) {
        //initSingleFileChange(mad);
        int index = changeEntityTreeContainer.addOneTree(fileName, mad);
        Global.mad = mad;
        List<ChangeEntity> entities = mad.getChangeEntityList();
        for (ChangeEntity a : entities) {
            initLinkBean(a, mad);
        }
        return index;
    }



    private static void initLinkBean(ChangeEntity ce, MiningActionData miningActionData) {
        if (ce.linkBean == null) {
            ce.linkBean = new LinkBean();
        }
        if(miningActionData.changeType != DiffEntry.ChangeType.MODIFY){
            return;
        }
        InitInheritanceBean.init(ce, miningActionData);

        if (ce instanceof MemberPlusChangeEntity) {
            if (ChangeEntityDesc.StageIIOpt.OPT_INSERT.equals(ce.stageIIBean.getOpt())
                    || ChangeEntityDesc.StageIIOpt.OPT_DELETE.equals(ce.stageIIBean.getOpt())) {
                ce.initDefs(miningActionData);
            } else {
                ce.initDefs(miningActionData);
                if (ce instanceof ClassChangeEntity) {
                    InitClassBean.parse(ce);
                } else if (ce instanceof FieldChangeEntity) {
                    InitFieldBean.parse(ce);
                } else if (ce instanceof MethodChangeEntity) {
                    InitMethodBean.parse(ce, miningActionData);
                } else if (ce instanceof EnumChangeEntity) {
                    ce.linkBean = null;
                }
            }
        } else {
            if (ce instanceof StatementPlusChangeEntity) {
                InitStmtBean initStmt = new InitStmtBean();
                if (ce instanceof VariableChangeEntity) {
                    if (ChangeEntityDesc.StageIIOpt.OPT_INSERT.equals(ce.stageIIBean.getOpt())
                            || ChangeEntityDesc.StageIIOpt.OPT_DELETE.equals(ce.stageIIBean.getOpt())
                            || ChangeEntityDesc.StageIIOpt.OPT_MOVE.equals(ce.stageIIBean.getOpt())) {
                        ce.initDefs(miningActionData);
                        initStmt.parse(ce, miningActionData,true);

                    } else if (ChangeEntityDesc.StageIIOpt.OPT_CHANGE.equals(ce.stageIIBean.getOpt())) {
                        ce.initDefs(miningActionData);
                        initStmt.parse(ce, miningActionData,true);
                    } else {
                        initStmt.parse(ce, miningActionData,false);
                    }
                } else {
                    //stmts
                    initStmt.parse(ce, miningActionData,false);
                }
            } else if (ce instanceof ExpressionPlusChangeEntity) {
                InitExpBean.parse(ce, miningActionData);
            } else {
                System.out.println("");
            }
        }


    }
}
