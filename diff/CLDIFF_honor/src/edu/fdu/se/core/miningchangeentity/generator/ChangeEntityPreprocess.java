package edu.fdu.se.core.miningchangeentity.generator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.github.gumtreediff.actions.model.*;
import edu.fdu.se.core.miningactions.bean.MiningActionData;
import edu.fdu.se.global.Constants;
import edu.fdu.se.global.Global;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import com.github.gumtreediff.tree.Tree;

import edu.fdu.se.core.links.generator.ChangeEntityTree;
import edu.fdu.se.core.miningchangeentity.base.ChangeEntity;
import edu.fdu.se.core.miningchangeentity.base.ChangeEntityDesc;
import edu.fdu.se.core.miningchangeentity.base.StageIIBean;
import edu.fdu.se.core.miningchangeentity.statement.ForChangeEntity;
import edu.fdu.se.core.miningchangeentity.statement.IfChangeEntity;
import edu.fdu.se.core.miningchangeentity.statement.SynchronizedChangeEntity;
import edu.fdu.se.core.miningchangeentity.statement.WhileChangeEntity;
import edu.fdu.se.core.preprocessingfile.data.BodyDeclarationPair;

/**
 * Created by huangkaifeng on 2018/4/7.
 *
 *
 */
public class ChangeEntityPreprocess {

    public ChangeEntityPreprocess(MiningActionData mad) {
        this.mad = mad;
    }

    public MiningActionData mad;


    public void preprocessChangeEntity() {
        //把changeentity按照层级结构放入preCacheData的entityTree
        this.initContainerEntityData();
        this.setChangeEntitySub();
        this.setChangeEntityOpt2Opt2Exp();
    }



    public void setChangeEntityOpt2Opt2Exp(){
        ChangeEntityTree container = this.mad.preCacheData.getEntityTree();
        for (Map.Entry<BodyDeclarationPair, List<ChangeEntity>> entry : container.getLayerChangeEntityMap().entrySet()) {
            BodyDeclarationPair bodyDeclarationPair = entry.getKey();
            List<ChangeEntity> mList = entry.getValue();
            for(ChangeEntity ce:mList){
                if(!ce.stageIIBean.getEntityCreationStage().equals(ChangeEntityDesc.StageIIGenStage.ENTITY_GENERATION_STAGE_PRE_DIFF)){
                    if(ce.stageIIBean.getOpt().equals(ChangeEntityDesc.StageIIOpt.OPT_CHANGE)){
                        if(ce.stageIIBean.getGranularity().equals(ChangeEntityDesc.StageIIGranularity.GRANULARITY_CLASS)){

                        }else if(ce.stageIIBean.getGranularity().equals(ChangeEntityDesc.StageIIGranularity.GRANULARITY_MEMBER)){
                            // method signature
                        }else if(ce.stageIIBean.getGranularity().equals(ChangeEntityDesc.StageIIGranularity.GRANULARITY_STATEMENT)){
                            // stmt
                            List<Action> actions = ce.clusteredActionBean.actions;
                            generatingExpressions(actions,ce.stageIIBean);
                        }
                    }
                }
            }
        }
    }

    public void generatingExpressions(List<Action> actions,StageIIBean bean){
        for(Action a:actions){
            Tree tree = (Tree) a.getNode();
            int nodeType = Global.astNodeUtil.getNodeTypeId(tree.getNode());
//            System.out.println(tree.getAstNode());
            boolean flag = Global.astNodeUtil.isLiteralOrName(nodeType);
            if (flag) {
                String name =a.getClass().getSimpleName();
                String exp = null;
                if(a instanceof Update){
                    Update up = (Update)a;
                    exp = tree.getLabel()+"->"+up.getValue();
//                    exp = tree.getAstNode().getClass().getSimpleName();
                }else{
                    exp = tree.getLabel();
//                    String exp = tree.getAstNode().getClass().getSimpleName();
                }
                if(exp.equals("Object")){
                    continue;//hot fix
                }
                bean.addOpt2AndOpt2Expression(name,exp);
            } else {
                String name =a.getClass().getSimpleName();
                String exp = tree.getNode().getClass().getSimpleName();
                bean.addOpt2AndOpt2Expression(name,exp);
            }
        }
    }

    public void mergeMoveAndWrapper() {
        ChangeEntityTree container = this.mad.preCacheData.getEntityTree();
        for (Map.Entry<BodyDeclarationPair, List<ChangeEntity>> entry : container.getLayerChangeEntityMap().entrySet()) {
            BodyDeclarationPair bodyDeclarationPair = entry.getKey();
            if (bodyDeclarationPair.getBodyDeclaration() instanceof MethodDeclaration) {
                //每个method里面
                List<ChangeEntity> mList = entry.getValue();
                List<ChangeEntity> moveList = new ArrayList<>();
                List<ChangeEntity> stmtWrapperList = new ArrayList<>();
                List<ChangeEntity> deletedMove = new ArrayList<>();
                for (ChangeEntity ce : mList) {
                    int resultCode = ChangeEntityUtil.checkEntityCode(ce);
                    if (resultCode == 1) {
                        moveList.add(ce);
                    } else if (resultCode == 2) {
                        stmtWrapperList.add(ce);
                    }
                }
                for (ChangeEntity ce : stmtWrapperList) {
                    for (ChangeEntity mv : moveList) {
                        if (ChangeEntityUtil.isMoveInWrapper(mad, ce, mv)) {
                            deletedMove.add(mv);
                            ce.clusteredActionBean.changePacket.getChangeSet2().add(Move.class.getSimpleName());
                        }
                    }
                }
                for (ChangeEntity e : deletedMove) {
                    mList.remove(e);
                    mad.getChangeEntityList().remove(e);
                }

            }
        }
    }


    public void initContainerEntityData() {
        if (Global.granularity.equals(Constants.GRANULARITY.TYPE)) {
            if (mad.getChangeEntityList().size() != 0) {
                ChangeEntity index0 = mad.getChangeEntityList().get(0);
                mad.getChangeEntityList().clear();
                mad.getChangeEntityList().add(index0);
                return;
            } else if (mad.preCacheData.getPreChangeEntity() != null) {
                if (mad.preCacheData.getPreChangeEntity().size() != 0) {
                    mad.getChangeEntityList().add(mad.preCacheData.getPreChangeEntity().get(0));
                }
            }
            return;
        }
        mad.preCacheData.getmBodiesAdded().forEach(a -> {
            ChangeEntity ce = mad.addOneBody(a, Insert.class.getSimpleName());
            mad.preCacheData.getEntityTree().addPreDiffChangeEntity(ce);
            if(ce!=null){
                   mad.getChangeEntityList().add(ce);
            }
        });
        mad.preCacheData.getmBodiesDeleted().forEach(a -> {
            ChangeEntity ce = mad.addOneBody(a,Delete.class.getSimpleName());
            mad.preCacheData.getEntityTree().addPreDiffChangeEntity(ce);
            if(ce!=null){
                mad.getChangeEntityList().add(ce);
            }
        });
        if (mad.preCacheData.getPreChangeEntity() != null) {
            mad.preCacheData.getPreChangeEntity().forEach(a -> {
                mad.preCacheData.getEntityTree().addPreDiffChangeEntity(a);
                mad.getChangeEntityList().add(a);
            });
        }
        //todo 在这里对method的add和del做过滤？
        mad.getChangeEntityList().forEach(a -> {
            if (!a.stageIIBean.getEntityCreationStage().equals(ChangeEntityDesc.StageIIGenStage.ENTITY_GENERATION_STAGE_PRE_DIFF)) {
                mad.preCacheData.getEntityTree().addCLDiffChangeEntity(a, mad);
            }
        });
    }


    public void printNaturalEntityDesc(){
        ChangeEntityUtil.printContainerEntityNatural(mad.preCacheData.getEntityTree());
    }

    public void setChangeEntitySub(){
        mad.getChangeEntityList().forEach(a -> {
            if (!a.stageIIBean.getEntityCreationStage().equals(ChangeEntityDesc.StageIIGenStage.ENTITY_GENERATION_STAGE_PRE_DIFF)) {
                if(a instanceof ForChangeEntity || a instanceof WhileChangeEntity
                        || a instanceof SynchronizedChangeEntity || a instanceof IfChangeEntity){
                    if(a.stageIIBean.getOpt().equals(ChangeEntityDesc.StageIIOpt.OPT_INSERT)||a.stageIIBean.getOpt().equals(ChangeEntityDesc.StageIIOpt.OPT_DELETE)){

                        if(a.clusteredActionBean.changePacket.getChangeSet2().contains(Move.class.getSimpleName())){
                            if(a.clusteredActionBean.changePacket.getChangeSet2().contains(Insert.class.getSimpleName())||
                                    a.clusteredActionBean.changePacket.getChangeSet2().contains(Delete.class.getSimpleName())){
                                a.stageIIBean.setSubEntity(ChangeEntityDesc.StageIISub.SUB_CONDITION_AND_PARTIAL_BODY);
                            }else{
                                a.stageIIBean.setSubEntity(ChangeEntityDesc.StageIISub.SUB_CONDITION);
                            }
                        }else{
                            a.stageIIBean.setSubEntity(ChangeEntityDesc.StageIISub.SUB_CONDITION_AND_BODY);
                        }
                    }
                }
            }
        });
    }





}
