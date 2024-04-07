package edu.fdu.se.core.json;

import com.github.gumtreediff.tree.Tree;
import edu.fdu.se.core.miningactions.bean.MiningActionData;
import edu.fdu.se.core.miningchangeentity.base.ChangeEntity;
import edu.fdu.se.core.miningchangeentity.base.ChangeEntityDesc;
import edu.fdu.se.core.miningchangeentity.member.EnumChangeEntity;
import org.json.JSONArray;

import java.util.List;

public abstract class BeanSetter {

    /**
     * 目前如果 BeanSetter 是：BodyLevelBeanSetter ExpsLevelBeanSetter StmtLevelBeanSetter 就会调用这个方法处理。
     * 将 ListChangeEntity 内的 ChangeEntity 分别存入 changeEntity 内,根据 opt 选择对应的file改动处理，并给出行号范围
     */
    abstract public void setChangeEntityOpt(MiningActionData mad);

    abstract public void setChangeEntitySubRange(MiningActionData mad);

    /**
     * 设置 frontData 的 key，
     * 如果 EntityCreationStage 是 "PRE_DIFF"，
     * 则为"preprocess"
     * 否则为 "gumtree"
     * @param changeEntity
     */
    public void setGenStage(ChangeEntity changeEntity) {
        if (changeEntity.stageIIBean.getEntityCreationStage().equals(ChangeEntityDesc.StageIIGenStage.ENTITY_GENERATION_STAGE_PRE_DIFF)) {
            changeEntity.frontData.setKey("preprocess");
        } else {
            changeEntity.frontData.setKey("gumtree");
        }
    }

    /**
     * 根据 "ChangeEntity" 对象中的信息设置对应的代码范围字符串（range string），
     * 并将其存储到 "ChangeEntity" 对象的 "frontData" 属性中
     */
    public void setRangeString(ChangeEntity changeEntity, MiningActionData miningActionData) {
        Tree srcNode;
        //todo 需要审阅
        switch (changeEntity.stageIIBean.getOpt()) {
            case ChangeEntityDesc.StageIIOpt.OPT_MOVE:
                if (changeEntity.clusteredActionBean.fafather.getTreePrevOrCurr() == ChangeEntityDesc.TreeType.PREV_TREE_NODE) {
                    Tree dstNode = (Tree) miningActionData.getMappedCurrOfPrevNode(changeEntity.clusteredActionBean.fafather);
                    srcNode = changeEntity.clusteredActionBean.fafather;
                    String rangeStr = srcNode.getRangeString() + "-" + dstNode.getRangeString();
                    changeEntity.frontData.setRange(rangeStr);
                }
                break;
            case ChangeEntityDesc.StageIIOpt.OPT_CHANGE_MOVE:
                srcNode = (Tree) changeEntity.clusteredActionBean.curAction.getNode();
                if (srcNode.getTreePrevOrCurr() == ChangeEntityDesc.TreeType.PREV_TREE_NODE) {
                    Tree dstNode = (Tree) miningActionData.getMappedCurrOfPrevNode(srcNode);
                    String rangeStr = srcNode.getRangeString() + "-" + dstNode.getRangeString();
                    changeEntity.frontData.setRange(rangeStr);
                }
                break;
            case ChangeEntityDesc.StageIIOpt.OPT_CHANGE:
                //todo 可能还会变 仅仅获取其change的那几行
                String rangeStr = null;
                if (changeEntity instanceof EnumChangeEntity) {
                    //myflag
                    if(changeEntity.lineRange == null){
                        rangeStr = "";
                    }
                    else{
                        rangeStr = changeEntity.lineRange.toString() + "-" + ((EnumChangeEntity) changeEntity).dstRange;
                    }
                } else {
                    if (changeEntity.clusteredActionBean.fafather.getTreePrevOrCurr() == ChangeEntityDesc.TreeType.PREV_TREE_NODE) {
                        Tree dstNode = (Tree) miningActionData.getMappedCurrOfPrevNode(changeEntity.clusteredActionBean.fafather);
                        if (dstNode == null) {
                            rangeStr = changeEntity.clusteredActionBean.fafather.getRangeString() + "-";
                        } else {
                            rangeStr = changeEntity.clusteredActionBean.fafather.getRangeString() + "-" + dstNode.getRangeString();
                        }
                    } else {
                        srcNode = (Tree) miningActionData.getMappedPrevOfCurrNode(changeEntity.clusteredActionBean.fafather);
                        if (srcNode == null) {
                            rangeStr = "-" + changeEntity.clusteredActionBean.fafather.getRangeString();
                        } else {
                            rangeStr = srcNode.getRangeString() + "-" + changeEntity.clusteredActionBean.fafather.getRangeString();
                        }
                    }
                }
                changeEntity.frontData.setRange(rangeStr);
                if (changeEntity.stageIIBean.getOpt2List() != null) {
                    JSONArray jsonArray = changeEntity.stageIIBean.opt2ExpListToJSONArray();
                    changeEntity.frontData.setOpt2Exp2(jsonArray);
                }
                break;
        }

    }

    /**
     * 目前如果 BeanSetter 是：BodyLevelBeanSetter ExpsLevelBeanSetter StmtLevelBeanSetter 就会调用这个方法处理。
     * 将 miningActionData 内的 ChangeEntity 分别存入 changeEntity 内,根据 opt 选择对应的file改动处理，并给出行号范围
     */
    public void setChangeEntityOptDetail(MiningActionData miningActionData) {
        List<ChangeEntity> changeEntityList = miningActionData.getChangeEntityList();
        for (int i = 0; i < changeEntityList.size(); i++) {
            //遍历changeEntity，得到相关的操作内容
            ChangeEntity changeEntity = changeEntityList.get(i);
            changeEntity.frontData.setChangeEntityId(changeEntity.changeEntityId);
            changeEntity.frontData.setType1(changeEntity.stageIIBean.getGranularity());
            changeEntity.frontData.setType2(changeEntity.stageIIBean.getOpt());
            changeEntity.frontData.setDisplayDesc(changeEntity.stageIIBean.toString2());
            //设置key
            setGenStage(changeEntity);
            switch (changeEntity.stageIIBean.getOpt()) {
                case ChangeEntityDesc.StageIIOpt.OPT_INSERT:
                    changeEntity.frontData.setFile(ChangeEntityDesc.StageIIIFile.DST);
                    changeEntity.frontData.setRange(changeEntity.stageIIBean.getLineRange());
                    break;
                case ChangeEntityDesc.StageIIOpt.OPT_DELETE:
                    changeEntity.frontData.setFile(ChangeEntityDesc.StageIIIFile.SRC);
                    changeEntity.frontData.setRange(changeEntity.stageIIBean.getLineRange());
                    break;
                case ChangeEntityDesc.StageIIOpt.OPT_MOVE:
                    //todo 需要补全
                case ChangeEntityDesc.StageIIOpt.OPT_CHANGE_MOVE:
                    //todo 需要补全
                case ChangeEntityDesc.StageIIOpt.OPT_CHANGE:
                    changeEntity.frontData.setFile(ChangeEntityDesc.StageIIIFile.SRC_DST);
                    setRangeString(changeEntity, miningActionData);
                    break;
            }
        }
    }
}
