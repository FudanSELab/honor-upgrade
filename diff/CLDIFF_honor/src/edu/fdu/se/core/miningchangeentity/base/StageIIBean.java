package edu.fdu.se.core.miningchangeentity.base;


import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by huangkaifeng on 2018/3/28.
 *
 */
public class StageIIBean {


    private String entityCreationStage;

    /**
     * 粒度
     */
    private String granularity;

    /**
     * operate，例如 “insert”
     */
    private String opt;

    private String changeEntity;

    private String subEntity;

    private List<Opt2Tuple> opt2List;

    public String getThumbnail() {
        return thumbnail == null?"":thumbnail;
    }

    private String thumbnail;

    /**
     * 好像是行号范围。
     * 例如“（3，11）”
     */
    private String lineRange;

    /**
     * 这里有 selfName 和 longName
     */
    private CanonicalName canonicalName;

    class Opt2Tuple {
        private String opt2;
        private String opt2Expression;

        @Override
        public int hashCode() {
            return (opt2 + opt2Expression).hashCode();
        }

        public String toString() {
            return opt2 + " " + opt2Expression;
        }
    }

    public StageIIBean() {
        this.opt2List = new ArrayList<>();
    }

    public CanonicalName getCanonicalName() {
        return canonicalName;
    }

    public void setCanonicalName(CanonicalName location) {
        this.canonicalName = location;
    }

    public void setSubEntity(String subEntity) {
        this.subEntity = subEntity;
    }

    public String getSubEntity() {

        return subEntity;
    }

    public void addOpt2AndOpt2Expression(String opt2,String opt2Expression) {
        Opt2Tuple opt2Class = new Opt2Tuple();
        if (ChangeEntityDesc.StageIIOpt.OPT_INSERT.equals(opt2)) {
            opt2 = ChangeEntityDesc.StageIIOpt.OPT_ADD;
        } else if (ChangeEntityDesc.StageIIOpt.OPT_DELETE.equals(opt2)) {
            opt2 = ChangeEntityDesc.StageIIOpt.OPT_DEL;
        } else if (ChangeEntityDesc.StageIIOpt.OPT_UPDATE.equals(opt2)) {
            opt2 = ChangeEntityDesc.StageIIOpt.OPT_UPD;
        } else if (ChangeEntityDesc.StageIIOpt.OPT_MOVE.equals(opt2)) {
            opt2 = ChangeEntityDesc.StageIIOpt.OPT_MOV;
        }
        opt2Class.opt2 = opt2;
        opt2Class.opt2Expression = opt2Expression;
        for(Opt2Tuple tmp:this.opt2List){
            if(tmp.hashCode() == opt2Class.hashCode()){
                return;
            }
        }
        this.opt2List.add(opt2Class);
    }

    public List<Opt2Tuple> getOpt2List() {
        return opt2List;
    }

    public String getLineRange() {
        return lineRange;
    }

    public void setLineRange(String lineRange) {

        this.lineRange = lineRange;
    }

    public String getChangeEntity() {
        return changeEntity;
    }

    public String getEntityCreationStage() {
        return entityCreationStage;
    }

    /**
     * @return
     * @see ChangeEntityDesc.StageIIOpt
     */
    public String getOpt() {
        return opt;
    }

    /**
     * @see ChangeEntityDesc.StageIIGranularity
     * @return
     */
    public String getGranularity() {
        return granularity;
    }


    public void setChangeEntity(String changeEntity) {
        this.changeEntity = changeEntity;
    }

    public void setEntityCreationStage(String entityCreationStage) {
        this.entityCreationStage = entityCreationStage;
    }

    public void setOpt(String opt) {
        this.opt = opt;
    }

    public void setGranularity(String granularity) {
        this.granularity = granularity;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    @Override
    public String toString() {
        String thumbnailStr = thumbnail;
        if(thumbnail == null){
            thumbnailStr = "";
        }
        String subStr = subEntity;
        if(subEntity == null){
            subStr = "";
        }
        return
                this.granularity + " " +
                        this.opt + " " +
                        this.changeEntity + " " +
                        subStr + " " +

                        thumbnailStr + " " +

                        this.canonicalName.getPrintName()+" "+
                        this.lineRange + " " ;
    }

    /**
     * 这是一个将 StageIIBean 的内容转为字符串的方法。具体为 pot+changeEntity
     * @return
     */
    public String toString2(){
        StringBuffer sb = new StringBuffer();
        //添加修改类型，例如 mov add del upd
        if(this.opt.equals(ChangeEntityDesc.StageIIOpt.OPT_CHANGE_MOVE)||this.opt.equals(ChangeEntityDesc.StageIIOpt.OPT_MOVE)){
            sb.append(ChangeEntityDesc.StageIIOpt.OPT_MOV);
        }else if(this.opt.equals(ChangeEntityDesc.StageIIOpt.OPT_INSERT)){
            sb.append(ChangeEntityDesc.StageIIOpt.OPT_ADD);
        }else if(this.opt.equals(ChangeEntityDesc.StageIIOpt.OPT_DELETE)){
            sb.append(ChangeEntityDesc.StageIIOpt.OPT_DEL);
        }else if(this.opt.equals(ChangeEntityDesc.StageIIOpt.OPT_CHANGE)){
            sb.append(ChangeEntityDesc.StageIIOpt.OPT_UPD);
        }
        sb.append(" "); //update add delete move
        //添加 changeEntity ，举例： “ClassDeclaration”
        //结合上面得到 “addClassDeclaration”
        String entity = this.changeEntity;
        sb.append(entity);
        //如果是change操作，则追加 “ ”+SubEntity+“ by”
        //todo only change opt，need more opt
        if(this.opt.equals(ChangeEntityDesc.StageIIOpt.OPT_CHANGE)){
            sb.append(" ");
            if(this.getSubEntity()!=null) {
                sb.append(this.getSubEntity());
            }
            if (this.opt2List.size() != 0) {
                sb.append("  by");
            }
        }
        return sb.toString();
    }

    public JSONArray opt2ExpListToJSONArray(){
        if(this.opt2List!=null){
            JSONArray jsonArray = new JSONArray();
            for(Opt2Tuple tmp:this.opt2List){
                jsonArray.put(tmp.toString());
            }
            return jsonArray;
        }
        return null;
    }

    public String getEntityWithAction(){
        return getCanonicalName().getPrintName() + getOpt();
    }
}
