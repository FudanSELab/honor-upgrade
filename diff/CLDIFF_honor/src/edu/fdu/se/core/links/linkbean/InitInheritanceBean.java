package edu.fdu.se.core.links.linkbean;

import com.github.gumtreediff.tree.Tree;
import edu.fdu.se.core.miningactions.bean.MiningActionData;
import edu.fdu.se.core.miningchangeentity.base.ChangeEntity;
import edu.fdu.se.core.miningchangeentity.base.ChangeEntityDesc;
import edu.fdu.se.core.miningchangeentity.base.MemberPlusChangeEntity;
import edu.fdu.se.core.miningchangeentity.member.ClassChangeEntity;
import edu.fdu.se.global.Global;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jgit.diff.DiffEntry;

public class InitInheritanceBean {

    public static void init(ChangeEntity ce, MiningActionData mad) {
        if(Global.mad.changeType != DiffEntry.ChangeType.MODIFY){
            return;
        }
        if (ce instanceof MemberPlusChangeEntity && ((MemberPlusChangeEntity) ce).bodyDeclarationPair != null) {
            initMember(ce, mad);
        } else {
            initMemberStmtExps(ce, mad);
        }
    }

    public static void initMember(ChangeEntity ce, MiningActionData mad) {
        MemberPlusChangeEntity mpce = (MemberPlusChangeEntity) ce;
        Object td;
        if (mpce instanceof ClassChangeEntity && (mpce.stageIIBean.getChangeEntity().equals(ChangeEntityDesc.StageIIENTITY.ENTITY_CLASS) || mpce.stageIIBean.getChangeEntity().equals(ChangeEntityDesc.StageIIENTITY.ENTITY_INTERFACE))) {
            td = mad.preCacheData.getTypeDeclarationOfAddedOrDeletedFile();
        } else {
            td = mad.preCacheData.getAddBodiesTypeDeclration().get(mpce.bodyDeclarationPair);
            if (td == null) {
                td = mad.preCacheData.getDeletedBodiesTypeDeclration().get(mpce.bodyDeclarationPair);
            }
        }
        setter(ce, td);

    }

    public static void setter(ChangeEntity ce, Object typeDeclaration) {
        Global.processUtil.initInheritance(ce, typeDeclaration);

    }

    public static void initMemberStmtExps(ChangeEntity ce, MiningActionData mad) {
        Tree tree = (Tree) ce.clusteredActionBean.curAction.getNode();
        Object typeDeclaration = Global.astNodeUtil.searchBottomUpFindTypeDeclaration(tree.getNode(),true);
        setter(ce, typeDeclaration);

    }
}
