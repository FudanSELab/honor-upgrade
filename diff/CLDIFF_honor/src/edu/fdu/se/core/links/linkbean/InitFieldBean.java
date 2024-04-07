package edu.fdu.se.core.links.linkbean;

import com.github.gumtreediff.tree.Tree;
import edu.fdu.se.core.miningchangeentity.base.ChangeEntity;
import edu.fdu.se.global.Global;
import edu.fdu.se.core.miningchangeentity.base.ChangeEntityDesc;
import edu.fdu.se.core.miningchangeentity.member.FieldChangeEntity;
import edu.fdu.se.lang.c.generatingactions.CParserVisitor;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by huangkaifeng on 2018/4/7.
 */
public class InitFieldBean extends LinkBean {

    public static void parse(ChangeEntity en) {
        FieldChangeEntity ce = (FieldChangeEntity) en;
        Object fd = null;
        switch(ce.stageIIBean.getOpt()) {
            case ChangeEntityDesc.StageIIOpt.OPT_CHANGE:
                Tree t = ce.clusteredActionBean.fafather;
                if (Global.astNodeUtil.getNodeTypeId(t.getNode()) == CParserVisitor.FIELD_DECLARATION) {
                    fd = t.getNode();
                }
                break;
            case ChangeEntityDesc.StageIIOpt.OPT_DELETE:
            case ChangeEntityDesc.StageIIOpt.OPT_INSERT:
                if (ce.stageIIBean.getEntityCreationStage().equals(ChangeEntityDesc.StageIIGenStage.ENTITY_GENERATION_STAGE_PRE_DIFF)) {
                    fd = ce.bodyDeclarationPair.getBodyDeclaration();
                }
                break;

        }
        if(fd!=null){
            Global.processUtil.addFieldDef(ce, fd);
        }


    }

    public List<String> fieldName;

    public String fieldType;
}
