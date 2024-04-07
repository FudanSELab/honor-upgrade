//package edu.fdu.se.util;
//
//import com.alibaba.fastjson.JSONArray;
//import com.alibaba.fastjson.JSONObject;
//import com.google.gson.annotations.Expose;
//import com.google.gson.annotations.SerializedName;
//import edu.fdu.se.repfinder.server.MethodBean;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//public class DiffInfo {
//
//    /**
//     * 增加的class
//     */
//    @Expose(serialize = true)
//    @SerializedName("ADDED_FILES")
//    public List<ClassBean> addedClassesList;
//
//    /**
//     * 删除的class
//     */
//    @Expose(serialize = true)
//    @SerializedName("REMOVED_FILES")
//    public List<ClassBean> deletedClassList;
//
//    /**
//     * key:
//     * addedMethod
//     * deletedMethod
//     * modifiedMethodPrev
//     * modifiedMethodCurr
//     *
//     */
//    @Expose(serialize = true)
//    @SerializedName("MODIFIED_FILES")
//    public Map<String,List<ClassBean>> modifiedClassMap;
//
//
//    public DiffInfo(){
//        this.modifiedClassMap = new HashMap<>();
//        this.modifiedClassMap.put("addedMethod",new ArrayList<>());
//        this.modifiedClassMap.put("deletedMethod",new ArrayList<>());
//        this.modifiedClassMap.put("modifiedMethodPrev",new ArrayList<>());
//        this.modifiedClassMap.put("modifiedMethodCurr",new ArrayList<>());
//    }
//
//    @Expose(serialize = true)
//    private String prevRootPath;
//
//    @Expose(serialize = true)
//    private String currRootPath;
//
//
//    public String getPrevRootPath() {
//        return prevRootPath;
//    }
//
//    public void setPrevRootPath(String prevRootPath) {
//        this.prevRootPath = prevRootPath;
//    }
//
//    public String getCurrRootPath() {
//        return currRootPath;
//    }
//
//    public void setCurrRootPath(String currRootPath) {
//        this.currRootPath = currRootPath;
//    }
//
////
////    @Expose(serialize = true)
////    public String prevShortName;
////
////    @Expose(serialize = true)
////    public String currShortName;
//
//    @Expose(serialize = true)
//    private String prevDecompilePath;
//
//
//    @Expose(serialize = true)
//    private String currDecompilePath;
//
//
//    public String getPrevDecompilePath() {
//        return prevDecompilePath;
//    }
//
//    public void setPrevDecompilePath(String prevDecompilePath) {
//        this.prevDecompilePath = prevDecompilePath;
//    }
//
//    public String getCurrDecompilePath() {
//        return currDecompilePath;
//    }
//
//    public void setCurrDecompilePath(String currDecompilePath) {
//        this.currDecompilePath = currDecompilePath;
//    }
//
//
//    /**
//     * {
//     *     "deleted_classes":...,
//     *     "undeleted_class_deleted_items":...,
//     *     "undeleted_class_modified_items_prev":...,
//     *     "undeleted_class_modified_items_curr",
//     * }
//     * @return
//     */
//    public JSONObject deletedAndModifiedItemtoJSONObject(){
//        JSONObject result = new JSONObject();
//        // added class
//        result.put("added_classes", new JSONArray());
//        // added classes
//        for(ClassBean classBean:this.addedClassesList){
//            JSONObject classJson = new JSONObject();
//            String prefix = getClassFullName(classBean);
//            classJson.put("class_name",prefix);
//            JSONArray fieldArr = toFieldArray(classBean.getFieldBeans(),prefix);
//            JSONArray methodArr = toMethodArray(classBean.getMethodBeans(),prefix);
//            classJson.put("added_fields_in_class",fieldArr);
//            classJson.put("added_method_in_class",methodArr);
//            result.getJSONArray("added_classes").add(classJson);
//        }
//        // deleted class
//        result.put("deleted_classes", new JSONArray());
//        // deleted classes
//        for(ClassBean classBean:this.deletedClassList){
//            JSONObject classJson = new JSONObject();
//            String prefix = getClassFullName(classBean);
//            classJson.put("class_name",prefix);
//            JSONArray fieldArr = toFieldArray(classBean.getFieldBeans(),prefix);
//            JSONArray methodArr = toMethodArray(classBean.getMethodBeans(),prefix);
//            classJson.put("deleted_fields_in_class",fieldArr);
//            classJson.put("deleted_method_in_class",methodArr);
//            result.getJSONArray("deleted_classes").add(classJson);
//        }
//        //input
//        // deleted methods in modified classes
//        List<ClassBean> deletedMethodInExistingClass = this.modifiedClassMap.get("deletedMethod");
//        result.put("undeleted_class_deleted_items",new JSONArray());
//        for(ClassBean classBean:deletedMethodInExistingClass){
//            JSONObject classJson = new JSONObject();
//            String prefix = getClassFullName(classBean);
//            classJson.put("class_name", prefix);
//            JSONArray fieldArr = toFieldArray(classBean.getFieldBeans(),prefix);
//            JSONArray methodArr = toMethodArray(classBean.getMethodBeans(),prefix);
//            classJson.put("deleted_fields_in_class",fieldArr);
//            classJson.put("deleted_method_in_class",methodArr);
//            result.getJSONArray("undeleted_class_deleted_items").add(classJson);
//        }
//        // added methods in modified classes
//        List<ClassBean> addedMethodInExistingClass = this.modifiedClassMap.get("addedMethod");
//        result.put("undeleted_class_added_items",new JSONArray());
//        for(ClassBean classBean:addedMethodInExistingClass){
//            JSONObject classJson = new JSONObject();
//            String prefix = getClassFullName(classBean);
//            classJson.put("class_name", prefix);
//            JSONArray fieldArr = toFieldArray(classBean.getFieldBeans(),prefix);
//            JSONArray methodArr = toMethodArray(classBean.getMethodBeans(),prefix);
//            classJson.put("added_fields_in_class",fieldArr);
//            classJson.put("added_method_in_class",methodArr);
//            result.getJSONArray("undeleted_class_added_items").add(classJson);
//        }
//        // modified methods in modified classes
//        result.put("undeleted_class_modified_items_prev",new JSONArray());
//        result.put("undeleted_class_modified_items_curr",new JSONArray());
//        List<ClassBean> modifiedMethodPrev = this.modifiedClassMap.get("modifiedMethodPrev");
//        List<ClassBean> modifiedMethodCurr = this.modifiedClassMap.get("modifiedMethodCurr");
//        for(ClassBean classBean:modifiedMethodPrev){
//            JSONObject classJson = new JSONObject();
//            String prefix = getClassFullName(classBean);
//            classJson.put("class_name",prefix);
//            JSONArray methodArr = toMethodArray(classBean.getMethodBeans(),prefix);
//            classJson.put("modified_methods_in_class",methodArr);
//            result.getJSONArray("undeleted_class_modified_items_prev").add(classJson);
//        }
//        for(ClassBean classBean:modifiedMethodCurr){
//            JSONObject classJson = new JSONObject();
//            String prefix = getClassFullName(classBean);
//            classJson.put("class_name",prefix);
//            JSONArray methodArr = toMethodArray(classBean.getMethodBeans(),prefix);
//            classJson.put("modified_methods_in_class",methodArr);
//            result.getJSONArray("undeleted_class_modified_items_curr").add(classJson);
//        }
//
//        return result;
//    }
//
//    private JSONArray toFieldArray(List<FieldBean> fieldBeans, String prefix){
//        JSONArray jsonArray = new JSONArray();
//        for(FieldBean fieldBean:fieldBeans){
//            String s = fieldBean.getFieldType() + "__fdse__" + prefix + "." +fieldBean.getFieldName();
//            jsonArray.add(s);
//
//        }
//        return jsonArray;
//
//    }
//
//    private JSONArray toMethodArray(List<MethodBean> methodBeans, String prefix){
//        JSONArray jsonArray = new JSONArray();
//        for(MethodBean methodBean: methodBeans){
//            String s = methodBean.getReturnType() + "__fdse__" + methodBean.getMethodSignatureWithNewClassAndPackageName(prefix);
//            if(methodBean.getModifier().contains("public")) {
//                jsonArray.add(s);
//            }
//        }
//        return jsonArray;
//    }
//
//
//    private String getClassFullName(ClassBean classBean){
////        String name = classBean.getClassName();
//        String path = classBean.getFileSubPath();
//        path = path.replace("/",".");
//        return path.substring(0,path.length()-5);
//    }
//
//
//}
