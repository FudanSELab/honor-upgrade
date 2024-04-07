package edu.fdu.se.core.miningchangeentity.generator;

import com.github.gumtreediff.tree.ITree;
import com.github.gumtreediff.tree.Tree;
import edu.fdu.se.core.links.generator.ChangeEntityTree;
import edu.fdu.se.core.miningactions.bean.MiningActionData;
import edu.fdu.se.core.miningactions.bean.MyRange;
import edu.fdu.se.core.miningchangeentity.base.ChangeEntity;
import edu.fdu.se.core.miningchangeentity.base.ChangeEntityDesc;
import edu.fdu.se.core.json.MergeIntervals;
import edu.fdu.se.core.preprocessingfile.data.BodyDeclarationPair;
import edu.fdu.se.global.Global;
import org.eclipse.jdt.core.dom.ASTNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by huangkaifeng on 2018/3/31.
 *
 */
public class ChangeEntityUtil {

    public static boolean isTypeIIEntity(String entityName) {
        switch (entityName) {
            case ChangeEntityDesc.StageIIENTITY.ENTITY_IF_STMT:
            case ChangeEntityDesc.StageIIENTITY.ENTITY_FOR_STMT:
            case ChangeEntityDesc.StageIIENTITY.ENTITY_ENHANCED_FOR_STMT:
            case ChangeEntityDesc.StageIIENTITY.ENTITY_WHILE_STMT:
            case ChangeEntityDesc.StageIIENTITY.ENTITY_DO_STMT:
            case ChangeEntityDesc.StageIIENTITY.ENTITY_SYNCHRONIZED_STMT:
            case ChangeEntityDesc.StageIIENTITY.ENTITY_TRY_STMT:
                return true;
            default:
                break;
        }
        return false;
    }

    public static int checkEntityCode(ChangeEntity ce) {
        if (ce.stageIIBean.getEntityCreationStage().equals(ChangeEntityDesc.StageIIGenStage.ENTITY_GENERATION_STAGE_GT_TD)
                && ce.stageIIBean.getOpt().equals(ChangeEntityDesc.StageIIOpt.OPT_MOVE)) {
            return 1;
        }
        if (ce.stageIIBean.getEntityCreationStage().equals(ChangeEntityDesc.StageIIGenStage.ENTITY_GENERATION_STAGE_GT_TD)
                && (ce.stageIIBean.getOpt().equals(ChangeEntityDesc.StageIIOpt.OPT_INSERT) || ce.stageIIBean.getOpt().equals(ChangeEntityDesc.StageIIOpt.OPT_DELETE))) {
            if (isTypeIIEntity(ce.stageIIBean.getChangeEntity())) {
                return 2;
            }
        }
        return 0;
    }

    public static boolean simpleMoveWrapperDecision(MiningActionData fp,ChangeEntity move){
        Tree t = (Tree) move.clusteredActionBean.curAction.getNode();
        Tree dstt = (Tree)fp.getMappedCurrOfPrevNode(t);
        Tree par = (Tree) dstt.getParent();
        boolean flag =false;
        switch (Global.astNodeUtil.getNodeTypeId(par.getNode())) {
            case ASTNode.BLOCK:
            case ASTNode.IF_STATEMENT:
            case ASTNode.FOR_STATEMENT:
            case ASTNode.ENHANCED_FOR_STATEMENT:
            case ASTNode.SYNCHRONIZED_STATEMENT:
            case ASTNode.WHILE_STATEMENT:
            case ASTNode.DO_STATEMENT:
                flag = true;
                break;
            default:break;
        }

        return flag;
    }

    public static boolean isMoveInWrapper(MiningActionData fp, ChangeEntity wrapper, ChangeEntity move) {
        Integer[] range = null;
        if (wrapper.stageIIBean.getOpt().equals(ChangeEntityDesc.StageIIOpt.OPT_INSERT)) {
            ITree dstITree = fp.getMappedCurrOfPrevNode(move.clusteredActionBean.curAction.getNode());
            if (dstITree == null) {
                return false;
            }

            Tree dstTree = (Tree) dstITree;
            range = dstTree.getRange();
        } else if (wrapper.stageIIBean.getOpt().equals(ChangeEntityDesc.StageIIOpt.OPT_DELETE)) {
            range = ((Tree) move.clusteredActionBean.curAction.getNode()).getRange();
        } else {
            return false;
        }
        Integer[] wrapperRange = ((Tree) wrapper.clusteredActionBean.curAction.getNode()).getRange();
        if (wrapperRange[0] <= range[0] && wrapperRange[1] >= range[1]) {
            if(simpleMoveWrapperDecision(fp,move)){
                return true;
            }
            return false;
        }
        return false;

    }





    public static void main(String args[]){

        MergeIntervals mi = new MergeIntervals();
        List<Integer[]> arra = new ArrayList<>();
        Integer[] a = {10,20};
        Integer[] b = {20,30};
        Integer[] c = {30,40};
        Integer[] d = {10,40};
        Integer[] e = {50,60};
        Integer[] f = {61,90};
        arra.add(a);arra.add(b);arra.add(c);arra.add(d);
        arra.add(e);arra.add(f);
        List<Integer[]> result = mi.merge(arra);
        for(Integer[] item:result){
            System.out.println(item[0]+" "+item[1]);
        }
    }


    public static void printContainerEntityNatural(ChangeEntityTree changeEntityTree) {
        Global.logger.info("\nMember key size:" + changeEntityTree.getLayerChangeEntityMap().size());
        Global.logger.info("Change entity size:" + changeEntityTree.getChangeEntitySize());
        List<Map.Entry<BodyDeclarationPair, List<ChangeEntity>>> result =  changeEntityTree.getSortedChangeEntityTree();
        for(Map.Entry<BodyDeclarationPair, List<ChangeEntity>> entry : result){
            BodyDeclarationPair bodyDeclarationPair = entry.getKey();
            List<ChangeEntity> mList = entry.getValue();
            if (mList.size() == 0) {
                continue;
            }
            Object node = bodyDeclarationPair.getBodyDeclaration();
            MyRange myRange =  Global.astNodeUtil.getRange(node, ChangeEntityDesc.TreeType.PREV_TREE_NODE);
            Global.logger.info(bodyDeclarationPair.getCanonicalName().getPrintName() + " "+ myRange.toString() + " list size: " + mList.size());
            for (ChangeEntity ce : mList) {
                Global.logger.info(ce.toString() );
            }
            Global.logger.info("");
        }
    }
}
