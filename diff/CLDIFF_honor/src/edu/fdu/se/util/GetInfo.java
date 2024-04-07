package edu.fdu.se.util;

//import com.alibaba.fastjson.JSONObject;
import com.github.gumtreediff.actions.model.*;
import com.github.gumtreediff.tree.Tree;
import com.mysql.cj.jdbc.exceptions.MysqlDataTruncation;
import edu.fdu.se.core.miningchangeentity.generator.ClusteredActionBean;
import edu.fdu.se.core.miningchangeentity.member.*;
import edu.fdu.se.core.miningactions.bean.MiningActionData;
import edu.fdu.se.core.miningchangeentity.base.ChangeEntity;
//import edu.fdu.se.util.JSONObject;
import edu.fdu.se.global.Global;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTSimpleDeclaration;
import org.eclipse.jdt.core.dom.*;
import org.json.JSONArray;

import org.json.JSONObject;

import java.io.File;
import java.sql.*;
import java.sql.Statement;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class GetInfo {

    //db
    static String sql_method = "insert ignore into changed_method (task_id,repo_id,file_id,lang,package,class,entity,prev_signature,curr_signature,change_type,prev_line_range,curr_line_range,description,commits) values ";
    static String sql_field = "insert ignore into changed_field (task_id,repo_id,file_id,lang,package,class,entity,signature,change_type,prev_line_range,curr_line_range,description,commits) values ";
    static String sql_enum = "insert ignore into changed_enum (task_id,repo_id,file_id,lang,package,class,entity,signature,change_type,prev_line_range,curr_line_range,description,commits) values ";
    static String sql_initializer = "insert ignore into changed_initializer (task_id,repo_id,file_id,lang,package,class,entity,signature,change_type,prev_line_range,curr_line_range,description,commits) values ";
    static String sql_class = "insert ignore into changed_class (task_id,repo_id,file_id,lang,package,class,entity,signature,change_type,prev_line_range,curr_line_range,description,commits) values ";
    static String sql_method_info = "insert ignore into changed_method_info (task_id,repo_id,entity_id,lang,modified_entity_name,modification,detail_action,line_num) values ";
    static String sql_field_info = "insert ignore into changed_field_info (task_id,repo_id,entity_id,lang,modified_entity_name,modification,detail_action,line_num) values ";
    static String sql_class_info = "insert ignore into changed_class_info (task_id,repo_id,entity_id,lang,modified_entity_name,modification,detail_action,line_num) values ";



    static String prev_path_full;
    static String curr_path_full;
    static String relative_prev_path;
    static String relative_curr_path;


    static int id = -1;

    /**
     * 通过哈希码对比两个 MethodBody
     */
    public static boolean compareMethodBody(MethodDeclaration md1,MethodDeclaration md2){
        if(md1.getBody() != null && md2.getBody() != null)
            return md1.getBody().toString().hashCode()==md2.getBody().toString().hashCode();
        else
            return false;
    }

    public static String methodDeclarationToString(MethodDeclaration methodDeclaration){
        StringBuilder sb = new StringBuilder();
        String methodName = methodDeclaration.getName().toString();
        sb.append(methodName);
        List pList = methodDeclaration.parameters();
        Iterator iter2 = pList.iterator();
        sb.append("(");

        while (iter2.hasNext()){
            // short param
            SingleVariableDeclaration var = (SingleVariableDeclaration) iter2.next();
            String varString = var.getType().toString();
            int index = varString.lastIndexOf(".");
            sb.append(varString.substring(index+1));
            sb.append(",");
        }
        if(pList.size()!=0){
            sb.deleteCharAt(sb.length()-1);
        }
        sb.append(")");
        return sb.toString();
    }

    public static <T> List<T> testRemove(List<T> list, Function<? super T,?> f) {
        List<T> l = new ArrayList<>();
        list.stream().filter(distinctByKey(f)) //filter保留true的值
                .forEach(l::add);
        return l;
    }

    static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Map<Object,Boolean> seen = new ConcurrentHashMap<>();
        //putIfAbsent方法添加键值对，如果map集合中没有该key对应的值，则直接添加，并返回null，如果已经存在对应的值，则依旧为原来的值。
        //如果返回null表示添加数据成功(不重复)，不重复(null==null :TRUE)
        return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
    }

    public static String getCommitsString(MiningActionData miningActionData,ChangeEntity changeEntity,String curr_path_full,String currCommitID){
        if(Global.git == null) return "no git info";
        String a = changeEntity.getFrontData().toString();

        int idx1 =a.lastIndexOf("(");
        int idx2 =a.lastIndexOf(")");
        int idx3 =a.lastIndexOf(",");
        int line1;
        int line2;
        if(idx1 == -1 || idx2 == -1 || idx3 == -1){
            return currCommitID;

        }
        else {
            line1 = Integer.parseInt(a.substring(idx1+1,idx3));
            line2 = Integer.parseInt(a.substring(idx3+1,idx2));
        }


        //String prefix = Global.currGitRepo.substring(0,Global.currGitRepo.indexOf(".git")).replace("\\","/");
        String prefix = Global.currDir;//GIT地址由currDir拼接出来的
        //把获取currDir下的相对路径
        String fileShortPath = curr_path_full.substring(curr_path_full.indexOf(prefix)+prefix.length());
        String commits = "";
        //如果代码操作含有delete
        if(changeEntity.getFrontData().toString().toLowerCase().contains("delete"))
            commits = Global.searchDeleteCommitId(line1,line2,Global.git,Global.prevCommitID,Global.currCommitID,fileShortPath);
        else
            //获取行对应的commitId
            commits = Global.f2(line1,line2,Global.git,fileShortPath,currCommitID);
        return commits;
    }

    private static void DBOutPut(){
        String prev_path_full = (Global.prevDir + File.separator + Global.fileName).replace("\\","/");
        String curr_path_full = (Global.currDir + File.separator + Global.fileName).replace("\\","/");

        String relative_prev_path = prev_path_full.contains(Global.relativeRoot) ?prev_path_full.substring(prev_path_full.indexOf(Global.relativeRoot)):prev_path_full;
        String relative_curr_path = curr_path_full.contains(Global.relativeRoot)? curr_path_full.substring(curr_path_full.indexOf(Global.relativeRoot)):curr_path_full;
        try {
            Connection conn = Global.conn;
            String sql_increment = "alter table changed_file auto_increment = 1";

            String sql_file =
                    "insert ignore into changed_file (task_id,repo_id,file_name,lang,prev_path,curr_path,change_type) values "
                            + String.format("('%s','%s','%s','%s','%s','%s','%s')", Global.task_id,Global.repoId,Global.fileName,Global.lang,relative_prev_path,relative_curr_path,"modify");
            // String sql_sniff = sql_file.replace("insert ignore","insert");

            String fileNameFixed = Global.fileName.length()>160? Global.fileName.substring(0,160):Global.fileName;
            String taskIdFixed = Global.task_id.length()>64? Global.task_id.substring(0,64):Global.task_id;
            String repoIdFixed = Global.repoId.length()>160? Global.repoId.substring(0,160):Global.repoId;
            String sql_query = String.format("select id from changed_file where file_name = '%s' and task_id = '%s' and repo_id = '%s'",fileNameFixed,taskIdFixed,repoIdFixed);
            Statement stmt = conn.createStatement();

//                 stmt.executeUpdate(sql_increment);
//                stmt.execute(sql_sniff);
            ResultSet rs ;

            stmt.executeUpdate(sql_increment);
            stmt.executeUpdate(sql_file);
            rs= stmt.executeQuery(sql_query);

            if (rs.next()) {
                // 获得id值
                id = rs.getInt("id");
                System.out.println("file id: "+id);
            }
            if(id == -1){
                throw new IllegalStateException("Can't find changeEntity corresponding file id");
            }

        } catch (SQLIntegrityConstraintViolationException e){
            System.out.println("find duplicate record,continue");
        } catch (Exception e){
            e.printStackTrace();
        }
    }


    public static JSONObject generateEntityJson(MiningActionData mad) {
        List<ChangeEntity> changeEntityList = mad.getChangeEntityList();
        //myflag handle method name modification
        if(changeEntityList.size()>1){
            for(int i=0;i<changeEntityList.size();i++){
                ChangeEntity changeEntity = changeEntityList.get(i);
                for(int j=i+1;j<changeEntityList.size();j++){
                    ChangeEntity tempCE = changeEntityList.get(j);
                    if(tempCE instanceof MethodChangeEntity && changeEntity instanceof MethodChangeEntity){
                        //处理拥有相同函数体，名称不同的函数
                        Global.processUtil.handleMethodNameModification(changeEntity,tempCE);
                        //handleMethodNameModification(changeEntity,tempCE,mad);
                    }
                    if(tempCE instanceof FieldChangeEntity && changeEntity instanceof FieldChangeEntity){
                    }
                }
            }
        }
        //去重、去掉包含statement的changeEntity
        List<ChangeEntity> filteredChangeEntityList =
                testRemove(changeEntityList, t -> t.getStageIIBean().getEntityWithAction())
                        .stream().filter(t -> !t.stageIIBean.getChangeEntity().toLowerCase().contains("statement"))
                        .toList();


        if(changeEntityList.size() > 0) {
            JSONObject total = new JSONObject();
            JSONArray classChangeArrDb = new JSONArray();
            JSONArray methodChangeArrDb = new JSONArray();
            JSONArray fieldChangeArrDb= new JSONArray();
            total.put("FILE_NAME", Global.fileName);
            prev_path_full = (Global.prevDir + File.separator + Global.fileName).replace("\\","/");
            curr_path_full = (Global.currDir + File.separator + Global.fileName).replace("\\","/");

            relative_prev_path = prev_path_full.contains(Global.relativeRoot) ?prev_path_full.substring(prev_path_full.indexOf(Global.relativeRoot)):prev_path_full;
            relative_curr_path = curr_path_full.contains(Global.relativeRoot)? curr_path_full.substring(curr_path_full.indexOf(Global.relativeRoot)):curr_path_full;

            total.put("PREV_PATH", relative_prev_path);
            total.put("CURR_PATH", relative_curr_path);

            //DBOutPut();

            JSONArray classChangeArr = new JSONArray();
            JSONArray methodChangeArr = new JSONArray();
            JSONArray fieldChangeArr = new JSONArray();
            JSONArray enumChangeArr = new JSONArray();
            JSONArray initializerChangeArr = new JSONArray();
            total.put("MODIFIED_CLASS", classChangeArr);
            total.put("MODIFIED_METHOD", methodChangeArr);
            total.put("MODIFIED_FIELD", fieldChangeArr);
            total.put("MODIFIED_ENUM", enumChangeArr);
            total.put("MODIFIED_INITIALIZER", initializerChangeArr);
            Set allActionSet = new HashSet(mad.mActionsMap.allActions);
            String currFullContent = mad.preCacheData.getFullStringCurr();
            int index = currFullContent.indexOf("{");
            String s = index > 0 ? currFullContent.substring(0, index) : currFullContent;
            //String s = currFullContent.substring(0,currFullContent.indexOf("{"));
            String pattern = "(package )(.*?)(;)";
            Pattern r = Pattern.compile(pattern);
            Matcher m = r.matcher(s);
            String pkg = "";
            if (m.find()) {
                pkg = m.group(2);
            }
            String className1 = mad.fileFullPackageName.substring(mad.fileFullPackageName.lastIndexOf(File.separator) + 1, mad.fileFullPackageName.lastIndexOf("."));

            Set <String> set1 = new HashSet<>();
            Set <String> set2 = new HashSet<>();
            Set<ChangeEntity> class_changeEntity_set = new HashSet<>();
            Set<ChangeEntity> class_changeEntity_overlapped_set = new HashSet<>();
            for (ChangeEntity changeEntity : filteredChangeEntityList) {

                if (changeEntity.clusteredActionBean != null) {
                    List actions = changeEntity.clusteredActionBean.actions;
                    Set actionsSet = new HashSet<>(actions);
                    allActionSet.removeAll(actionsSet);
                }

                if (changeEntity instanceof ClassChangeEntity) {
                    doClassChangeEntity(changeEntity,set1,set2,class_changeEntity_set,class_changeEntity_overlapped_set);
                }
                else if (changeEntity instanceof MethodChangeEntity) {
                    doMethodChangeEntity(mad,pkg,changeEntity,methodChangeArr,methodChangeArrDb);
                } else if (changeEntity instanceof FieldChangeEntity) {
                    doFieldChangeEntity(mad,pkg,changeEntity,fieldChangeArr,fieldChangeArrDb,className1);
                } else if (changeEntity instanceof EnumChangeEntity) {
                    doEnumChangeEntity(mad,pkg,changeEntity,enumChangeArr,className1);
                } else if (changeEntity instanceof InitializerChangeEntity) {
                    doInitializerChangeEntity(mad,pkg,changeEntity,initializerChangeArr,className1);
                }
            }
            doInsertIntoDB(mad,class_changeEntity_set,classChangeArr,pkg,className1,methodChangeArrDb,fieldChangeArrDb,classChangeArrDb);
            return total;
        }
        else {
            return null;
        }
    }

    private static void doInsertIntoDB(MiningActionData mad,Set<ChangeEntity> class_changeEntity_set,JSONArray classChangeArr,
                                       String pkg,String className1,JSONArray methodChangeArrDb,JSONArray fieldChangeArrDb,JSONArray classChangeArrDb){
        //db
        insertIntoDb("alter table changed_method auto_increment = 1",sql_method);
        insertIntoDb("alter table changed_field auto_increment = 1",sql_field);
        insertIntoDb("alter table changed_enum auto_increment = 1",sql_enum);
        insertIntoDb("alter table changed_initializer auto_increment = 1",sql_initializer);
        handleClassChangeEntity(mad,curr_path_full,class_changeEntity_set,classChangeArr,id,pkg,className1,sql_class );

        //db for change_info
        insertInfoIntoDb("changed_method_info",sql_method_info,methodChangeArrDb);
        insertInfoIntoDb("changed_field_info",sql_field_info,fieldChangeArrDb);
        insertInfoIntoDb("changed_class_info",sql_class_info,classChangeArrDb);
    }

    private static void doClassChangeEntity(ChangeEntity changeEntity,Set <String> set1, Set <String> set2,
                                            Set<ChangeEntity> class_changeEntity_set,Set<ChangeEntity> class_changeEntity_overlapped_set){
        String possibleName = changeEntity.stageIIBean.getThumbnail();
        String entity = changeEntity.stageIIBean.getCanonicalName().getPrintName();
        if(!set1.contains(entity + possibleName)){
            class_changeEntity_set.add(changeEntity);
            set1.add(entity + possibleName);
        }
        else{
            class_changeEntity_overlapped_set.add(changeEntity);
            set2.add(entity + possibleName);
        }
    }

    private static void doInitializerChangeEntity(MiningActionData mad,String pkg,ChangeEntity changeEntity,JSONArray initializerChangeArr,String className1){
        String entityChangeType = "INITIALIZER_CHANGES";
        JSONObject jsonObject = getInfoJson(changeEntity, entityChangeType);
        jsonObject.put("PACKAGE",pkg);
        jsonObject.put("CLASS",className1);
        jsonObject.put("SIGNATURE",pkg+"."+jsonObject.getString("ENTITY"));
        String lineRange = changeEntity.getFrontData().getRange();
        String prevRange = "",currRange = "";
        if(lineRange.contains("-")){
            prevRange = lineRange.substring(0,lineRange.indexOf("-"));
            currRange = lineRange.substring(lineRange.indexOf("-")+1,lineRange.length());
        }
        else {
            if(changeEntity.getFrontData().toString().toLowerCase().contains("add")){
                currRange = lineRange;
            }
            else {
                prevRange = lineRange;
            }
        }
        String commits = getCommitsString(mad,changeEntity,curr_path_full,Global.currCommitID);
        sql_initializer += String.format("('%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s'),",
                Global.task_id,Global.repoId,id,Global.lang,pkg,className1,jsonObject.getString("ENTITY"),pkg+"."+jsonObject.getString("ENTITY"),jsonObject.getString("CHANGE"),prevRange,currRange,changeEntity.getFrontData().toString(),commits);

        initializerChangeArr.put(jsonObject);
    }
    private static void doEnumChangeEntity(MiningActionData mad,String pkg,ChangeEntity changeEntity,JSONArray enumChangeArr,String className1){
        String entityChangeType = "ENUM_CHANGES";
        JSONObject jsonObject = getInfoJson(changeEntity, entityChangeType);
        jsonObject.put("PACKAGE",pkg);
        jsonObject.put("CLASS",className1);
        jsonObject.put("SIGNATURE",pkg+"."+jsonObject.getString("ENTITY"));
        String lineRange = changeEntity.getFrontData().getRange();
        String prevRange = "",currRange = "";
        if(lineRange.contains("-")){
            prevRange = lineRange.substring(0,lineRange.indexOf("-"));
            currRange = lineRange.substring(lineRange.indexOf("-")+1,lineRange.length());
        }
        else {
            if(changeEntity.getFrontData().toString().toLowerCase().contains("add")){
                currRange = lineRange;
            }
            else {
                prevRange = lineRange;
            }
        }
        String commits = getCommitsString(mad,changeEntity,curr_path_full,Global.currCommitID);
        sql_enum += String.format("('%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s'),",
                Global.task_id,Global.repoId,id,Global.lang,pkg,className1,jsonObject.getString("ENTITY"),pkg+"."+jsonObject.getString("ENTITY"),
                jsonObject.getString("CHANGE"),prevRange,currRange,changeEntity.getFrontData().toString(),commits);
        enumChangeArr.put(jsonObject);
    }

    private static void doMethodChangeEntity(MiningActionData mad,String pkg,ChangeEntity changeEntity,JSONArray methodChangeArr,JSONArray methodChangeArrDb) {
        MethodChangeEntity methodChangeEntity = (MethodChangeEntity) changeEntity;
        ClusteredActionBean bean = methodChangeEntity.getClusteredActionBean();
        String prev_sig = "";
        if(bean == null){
            Object methodDel = methodChangeEntity.bodyDeclarationPair.getBodyDeclaration();
            prev_sig = Global.processUtil.methodDeclarationToString(methodDel);
        }else{
            Tree t = bean.fafather;
            Object methodDeclaration = t.node;
            prev_sig = Global.processUtil.methodDeclarationToString(methodDeclaration);
            StringBuilder sb = new StringBuilder();
            //todo 只有java
            while(t.getParent() != null && (((Tree)t.getParent()).node instanceof CompilationUnit || ((Tree)t.getParent()).node instanceof IASTTranslationUnit)){
                try {
                    if(((Tree)t.getParent()).node instanceof TypeDeclaration )
                        sb.insert(0, ((TypeDeclaration)((Tree)t.getParent()).node).getName().toString() + ".");
                    else if (((Tree)t.getParent()).node instanceof AnonymousClassDeclaration){
                        sb.insert(0, "#Anonymous" + ".");
                    }
                    else if(((Tree)t.getParent()).node instanceof ClassInstanceCreation){
                        sb.insert(0,((ClassInstanceCreation)((Tree)t.getParent()).node).getType().toString());
                    } else if(((Tree)t.getParent()).node instanceof CPPASTSimpleDeclaration astNode && astNode.getDeclSpecifier() instanceof CPPASTCompositeTypeSpecifier) {

                    }
                }
                catch (Exception e){
                    System.out.println(t);
                    e.printStackTrace();
                }
                t = (Tree)t.getParent();
            }
            if(pkg.length() > 0)
                prev_sig = pkg  + "." + sb.toString() + prev_sig;
            else
                prev_sig = sb.toString() + prev_sig;

        }
        //AnonymousClassDeclaration
        JSONObject jsonObject = new JSONObject();

        //jsonObject.put(changeEntityType,changeEntity.toString());
        String granularity = changeEntity.stageIIBean.getChangeEntity();
        String change = changeEntity.stageIIBean.getOpt();
        String possibleName = changeEntity.stageIIBean.getThumbnail();
        String entity = changeEntity.stageIIBean.getCanonicalName().getPrintName();
        String lineRange = changeEntity.getFrontData().getRange();
        String prevRange = "",currRange = "";
        if(lineRange.contains("-")){
            prevRange = lineRange.substring(0,lineRange.indexOf("-"));
            currRange = lineRange.substring(lineRange.indexOf("-")+1,lineRange.length());
        }
        else {
            if(changeEntity.getFrontData().toString().toLowerCase().contains("add")){
                currRange = lineRange;
            }
            else {
                prevRange = lineRange;
            }
        }
        String entityString;
        if(entity.endsWith("$")){
            entityString = formatString(entity + possibleName);
            jsonObject.put("ENTITY",entityString);
        }
        else if(entity.endsWith(".")){
            return;
        }
        else {
            entityString=formatString(entity );
            jsonObject.put("ENTITY",entityString);
        }
        jsonObject.put("GRANULARITY",granularity);
        jsonObject.put("CHANGE",change);
        jsonObject.put("PREV_LINE_RANGE",prevRange);
        jsonObject.put("CURR_LINE_RANGE",currRange);
        jsonObject.put("DESC", changeEntity.getFrontData().toString());
        JSONObject tmpObj1 = new JSONObject();
        String fullPath = mad.fileFullPackageName;
        String className = getClassName(fullPath);
        //tmpObj.put("SIGNATURE",(fullPath.substring(0,fullPath.lastIndexOf(".java" ))+(changeEntity.stageIIBean.getCanonicalName().getPrintName())).replace("@Statement",""));
        String s1 = changeEntity.stageIIBean.getCanonicalName().getPrintName().replace("@Statement","");
        //s1 = formatString(s1);
        String tempStr;
        String fullPrevSig;
        if(pkg.length() != 0){
            tempStr = (pkg + "." + s1);
            fullPrevSig =  prev_sig;
        }
        else {
            tempStr = s1;
            fullPrevSig =  prev_sig;

        }

        String removeBracketRegex = "<.*?>";
        tempStr = tempStr.replaceAll(removeBracketRegex,"").replaceAll(">","");
        String curr_sig1 = formatString(tempStr);
        String prev_sig1 = formatString(fullPrevSig);
        if(changeEntity.toString().toLowerCase().contains("delete methoddeclaration")){
            curr_sig1 = "";
        }
        if(changeEntity.toString().toLowerCase().contains("insert methoddeclaration")){
            prev_sig1 = "";
        }
        tmpObj1.put("CURR_SIGNATURE", curr_sig1);
        tmpObj1.put("PREV_SIGNATURE",prev_sig1);
        tmpObj1.put("PACKAGE", pkg);
        tmpObj1.put("CLASS", className);

        jsonObject.put("MODIFIED_FROM", tmpObj1);

        if(changeEntity.toString().toLowerCase().contains("delete methoddeclaration") || changeEntity.toString().toLowerCase().contains("insert methoddelaration")){
            methodChangeArr.put(jsonObject);
            return;
        }
        if ((changeEntity).clusteredActionBean != null) {
            addModificationType(changeEntity, jsonObject);
            //db
            methodChangeArrDb.put(jsonObject);

        }
        methodChangeArr.put(jsonObject);

        String commits = getCommitsString(mad,changeEntity,curr_path_full,Global.currCommitID);

        //db
        sql_method += String.format("('%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s'),",
                Global.task_id,Global.repoId,id,Global.lang,pkg,className,jsonObject.getString("ENTITY"),
                prev_sig1,curr_sig1,change,prevRange,currRange,changeEntity.getFrontData().toString(),commits);

        //for method align
        if(changeEntity.getFrontData().getDisplayDesc().contains("update")){
            JSONObject alignObject = new JSONObject();
            String range = changeEntity.getFrontData().getRange();
            String prevRange1 = range.substring(0,range.indexOf("-"));
            String currRange1 = range.substring(range.indexOf("-") + 1);
            alignObject.put("PREV_RANGE",prevRange1);
            alignObject.put("CURR_RANGE",currRange1);
            alignObject.put("PREV_PATH", Global.prevDir + File.separator + Global.fileName);
            alignObject.put("CURR_PATH", Global.currDir + File.separator + Global.fileName);
            alignObject.put("CURR_SIGNATURE",formatString(tempStr));
            alignObject.put("PREV_SIGNATURE",formatString(fullPrevSig));
            Global.alignResult.put(alignObject);
        }
    }

    private static void doFieldChangeEntity(MiningActionData mad,String pkg,ChangeEntity changeEntity,JSONArray fieldChangeArr,JSONArray fieldChangeArrDb,String className1){
        String entityChangeType = "FIELD_CHANGES";
        JSONObject jsonObject = getInfoJson(changeEntity, entityChangeType);
        //db
        jsonObject.put("PACKAGE",pkg);
        jsonObject.put("CLASS",className1);
        jsonObject.put("SIGNATURE",pkg+"."+jsonObject.getString("ENTITY"));
        if(jsonObject.getString("ENTITY").endsWith(".")){
            return;
        }
        String lineRange = changeEntity.getFrontData().getRange();
        String prevRange = "",currRange = "";
        if(lineRange.contains("-")){
            prevRange = lineRange.substring(0,lineRange.indexOf("-"));
            currRange = lineRange.substring(lineRange.indexOf("-")+1,lineRange.length());
        }
        else {
            if(changeEntity.getFrontData().toString().toLowerCase().contains("add")){
                currRange = lineRange;
            }
            else {
                prevRange = lineRange;
            }
        }
        String commits = getCommitsString(mad,changeEntity,curr_path_full,Global.currCommitID);
        sql_field += String.format("('%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s'),",
                Global.task_id,Global.repoId,id,Global.lang,pkg,className1,jsonObject.getString("ENTITY"),
                pkg+"."+jsonObject.getString("ENTITY"),jsonObject.getString("CHANGE"),prevRange,currRange,changeEntity.getFrontData().toString(),commits);

        if (changeEntity.clusteredActionBean != null) {
            addFieldModificationType(changeEntity, jsonObject);
            fieldChangeArrDb.put(jsonObject);
        }
        fieldChangeArr.put(jsonObject);
    }
    private static void insertIntoDb(String sql_increment,String sql){
        try {
            if(sql.endsWith(",")){
                sql = sql.substring(0,sql.length() -1);
                Connection conn = Global.conn;
                Statement stmt = conn.createStatement();
                stmt.execute(sql_increment);
                stmt.executeUpdate(sql);
            }

        }
        catch (MysqlDataTruncation e){
            e.printStackTrace();
            System.out.println("Data beyond limitation");
            System.exit(-1);
        }
        catch (SQLIntegrityConstraintViolationException e){
            System.out.println("2find duplicate record,continue");
        }
        catch (Exception e){
           // e.printStackTrace();
        }
    }


    private static String getClassName(String path){
        if(path.lastIndexOf(".java") != -1){
            return path.substring(path.lastIndexOf(File.separator) + 1, path.lastIndexOf(".java"));
        } else if(path.lastIndexOf(".cpp") != -1) {
            return path.substring(path.lastIndexOf(File.separator) + 1, path.lastIndexOf(".cpp"));
        } else if (path.lastIndexOf(".h") != -1){
            return path.substring(path.lastIndexOf(File.separator) + 1, path.lastIndexOf(".h"));
        }
        return null;
    }

    private static void insertInfoIntoDb(String table_name,String sql_table,JSONArray arr){
        try{
            Connection conn = Global.conn;
            Statement stmt = conn.createStatement();
            for (Object t : arr) {
                if(((JSONObject)t).has("CHANGE_TYPE")){
                    JSONArray jsonArray = ((JSONObject) t).getJSONArray("CHANGE_TYPE");
                    for(Object obj : jsonArray){
                        String modification = ((JSONObject)obj).getString("TYPE");
                        JSONObject value_obj = ((JSONObject) obj).getJSONObject("VALUE");
                        String line_num = value_obj.getString("LINE_NUM");
                        String detail_action = value_obj.getString("ACTION");
                        String modified_entity_name = value_obj.getString("NAME");

                        int entity_id = -1;
                        String sql;
                        String taskIDFixed =  Global.task_id.length()>64? Global.task_id.substring(0,64):Global.task_id;
                        String repoIdFixed = Global.repoId.length()>160? Global.repoId.substring(0,160):Global.repoId;
                        if(table_name.contains("method")){
                            JSONObject obj1 = ((JSONObject) t).getJSONObject("MODIFIED_FROM");
                            String pkg = obj1.getString("PACKAGE").length()>160?obj1.getString("PACKAGE").substring(0,160):obj1.getString("PACKAGE");
                            String entity = ((JSONObject) t).getString("ENTITY").length()>160?((JSONObject) t).getString("ENTITY").substring(0,160):((JSONObject) t).getString("ENTITY");

                            sql = String.format("select id from %s where task_id = '%s' and repo_id = '%s' and package = '%s' and entity = '%s' ",table_name.substring(0,table_name.indexOf("_info")),taskIDFixed,repoIdFixed,pkg,entity);
                        }
                        else{
                            String sig = ((JSONObject) t).getString("SIGNATURE").length()>160?((JSONObject) t).getString("SIGNATURE").substring(0,160):((JSONObject) t).getString("SIGNATURE");
                            sql = String.format("select id from %s where task_id = '%s' and repo_id = '%s' and signature = '%s' ",table_name.substring(0,table_name.indexOf("_info")),taskIDFixed,repoIdFixed,sig);

                        }
                        ResultSet rs = stmt.executeQuery(sql);

                        while (rs.next()) {
                            // 获得id值
                            entity_id = rs.getInt("id");

                        }
                        if (entity_id == -1) {
                            continue;
                        }
                        if(!modified_entity_name.contains("{"))
                            sql_table += String.format("('%s','%s','%s','%s','%s','%s','%s'),", Global.task_id,Global.repoId, entity_id, modified_entity_name, modification, detail_action, line_num);
                    }

                    if(sql_table.endsWith(",")){
                        String sql_up = String.format("alter table %s auto_increment = 1",table_name);
                        String sql_insert = sql_table.substring(0,sql_table.length()-1);
//                        String sql_sniff = sql_insert.replace("insert ignore","insert");
//                        stmt.executeUpdate(sql_up);
//                        stmt.execute(sql_sniff);
                        stmt.executeUpdate(sql_up);
                        stmt.execute(sql_insert);
                    }
                }

            }


        }

        catch (MysqlDataTruncation e){
            e.printStackTrace();
            System.out.println("Data beyond limitation");
            System.exit(-1);
        }
        catch (SQLIntegrityConstraintViolationException e){
            System.out.println("3find duplicate record,continue");
        }
        catch (Exception e) {
            //System.out.println(sql_table);
            //e.printStackTrace();

        }
    }



    private static void handleClassChangeEntity(MiningActionData miningActionData,String curr_path_full,Set<ChangeEntity> set ,JSONArray arr,int id,String pkg,String className1,String sql){
        StringBuilder sqlBuilder = new StringBuilder(sql);
        for(ChangeEntity changeEntity : set){
            JSONObject jsonObject = new JSONObject();
            //jsonObject.put(changeEntityType,changeEntity.toString());

            String granularity = changeEntity.stageIIBean.getChangeEntity();
            String change = changeEntity.stageIIBean.getOpt();
            String possibleName = changeEntity.stageIIBean.getThumbnail();
            String entity = changeEntity.stageIIBean.getCanonicalName().getPrintName();
            String lineRange = changeEntity.getFrontData().getRange();
            String prevRange = "",currRange = "";
            if(lineRange.contains("-")){
                prevRange = lineRange.substring(0,lineRange.indexOf("-"));
                currRange = lineRange.substring(lineRange.indexOf("-")+1,lineRange.length());
            }
            else {
                if(changeEntity.getFrontData().toString().toLowerCase().contains("add")){
                    currRange = lineRange;
                }
                else {
                    prevRange = lineRange;
                }
            }
            if(entity.endsWith("$")){
                String completeStr = formatString(entity + possibleName);
                jsonObject.put("ENTITY",completeStr);
                jsonObject.put("CHANGE","Change");
                jsonObject.put("DESC",changeEntity.getFrontData().toString().replace("add","update").replace("delete","update"));
            }
            else {
                String completeStr = formatString(entity) ;
                jsonObject.put("ENTITY",completeStr);
                jsonObject.put("CHANGE",change);
                jsonObject.put("DESC",changeEntity.getFrontData().toString());
            }

            jsonObject.put("GRANULARITY",granularity);
            jsonObject.put("LINE_RANGE",lineRange);
            jsonObject.put("PACKAGE",pkg);
            jsonObject.put("CLASS",className1);
            jsonObject.put("SIGNATURE",pkg+"."+jsonObject.getString("ENTITY"));
            String commits = getCommitsString(miningActionData,changeEntity,curr_path_full,Global.currCommitID);
            sqlBuilder.append(String.format("('%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s','%s'),",
                    Global.task_id,Global.repoId, id, Global.lang,pkg, className1, jsonObject.getString("ENTITY"), pkg + "." + jsonObject.getString("ENTITY"), jsonObject.getString("CHANGE"), prevRange,lineRange, changeEntity.getFrontData().toString(),commits));

            sql = sqlBuilder.toString();
            JSONArray change_type_arr = new JSONArray();
            try {
                if(changeEntity.getStageIIBean().getSubEntity() != null){
                    String[] msg = changeEntity.getStageIIBean().getSubEntity().split(";");
                    for(String s  : msg){
                        String[] strs = s.split(",");
                        if(strs.length<2) continue;
                        JSONObject o1 = new JSONObject();
                        JSONObject o2 = new JSONObject();
                        change_type_arr.put(o1);
                        //arr.put(o2);
                        o1.put("TYPE","Class_or_interface modification");
                        o1.put("VALUE",o2);
                        o2.put("ACTION",strs[0].substring(0,strs[0].indexOf(" ")));
                        o2.put("LINE_NUM",changeEntity.getLineRange());
                        o2.put("NAME",strs[1]);
                    }

                }
            }
            catch (Exception e){
               // e.printStackTrace();
            }
            jsonObject.put("CHANGE_TYPE",change_type_arr);
            arr.put(jsonObject);
        }
        sql = sqlBuilder.toString();
        insertIntoDb("alter table changed_class auto_increment = 1",sql);
    }

    private static void addAction(ChangeEntity changeEntity, JSONObject jsonObject) {
        for (Action action:changeEntity.clusteredActionBean.actions){
            String actionType = action.toString();
            //if()
            String actionMsg =((Tree)(action).node).node.toString();
            String lineRange = ((Tree)(action).node).getRangeString();
            jsonObject.append("ACTIONS",actionType+":"+ actionMsg + ";" +"LINE_RANGE" + lineRange);
        }
    }

    public static void addModificationType(ChangeEntity changeEntity,JSONObject jsonObject) {
        JSONArray change_type_arr = new JSONArray();
        if(changeEntity.clusteredActionBean.actions.size() == 0){
            JSONObject o1 = new JSONObject();
            JSONObject o2 = new JSONObject();
            JSONArray arr = new JSONArray();
            change_type_arr.put(o1);
//            arr.put(o2);
            o1.put("TYPE","Method body modification");
            o1.put("VALUE",o2);
            o2.put("ACTION","Change");
            o2.put("LINE_NUM",changeEntity.lineRange.toString());
            o2.put("NAME",formatString(changeEntity.stageIIBean.getCanonicalName().getPrintName()));
            jsonObject.put("CHANGE_TYPE",change_type_arr);
            return;
        }
        for (Action action : changeEntity.clusteredActionBean.actions) {
            String actionType = action.toString();
            String actionMsg =((Tree)(action).node).node.toString();
            String lineRange = ((Tree)(action).node).getRangeString();
            //cpp 是parameter ， 不确定java是不是variable
            if (actionType.toLowerCase().contains("parameterdeclaration") || actionType.toLowerCase().contains("singlevariabledeclaration")) {
                JSONObject o1 = new JSONObject();
                JSONObject o2 = new JSONObject();
                JSONArray arr = new JSONArray();
                change_type_arr.put(o1);
                //arr.put(o2);
                o1.put("TYPE","Method parameters modification");
                o1.put("VALUE",o2);
                o2.put("ACTION",actionType);
                o2.put("LINE_NUM",lineRange);
                o2.put("NAME",actionMsg);
            } else if (actionType.toLowerCase().contains("annotation")) {

                String parent = action.getNode().getParent().toString();
                JSONObject o1 = new JSONObject();
                JSONObject o2 = new JSONObject();
                JSONArray arr = new JSONArray();
                if(parent.equals("MethodDeclaration")){
                    change_type_arr.put(o1);
                    //arr.put(o2);
                    o1.put("TYPE","Method annotation modification");
                    o1.put("VALUE",o2);
                    o2.put("ACTION",actionType);
                    o2.put("LINE_NUM",lineRange);
                    o2.put("NAME",actionMsg);
                }
                else if(parent.equals("SingleVariableDeclaration")){
                    change_type_arr.put(o1);
                    //arr.put(o2);
                    o1.put("TYPE","Method parameters modification");
                    o1.put("VALUE",o2);
                    o2.put("ACTION",actionType);
                    o2.put("LINE_NUM",lineRange);
                    o2.put("NAME",actionMsg);
                }


            } else if (actionType.toLowerCase().contains("modifier")) {
                JSONObject o1 = new JSONObject();
                JSONObject o2 = new JSONObject();
                JSONArray arr = new JSONArray();
                change_type_arr.put(o1);
                //arr.put(o2);
                o1.put("TYPE","Method modifier modification");
                o1.put("VALUE",o2);
                o2.put("ACTION",actionType);
                o2.put("LINE_NUM",lineRange);
                o2.put("NAME",actionMsg);

            }
            //else if(actionType.toLowerCase().contains("upd simplename")){
            else {
                JSONObject o1 = new JSONObject();
                JSONObject o2 = new JSONObject();
                JSONArray arr = new JSONArray();
                change_type_arr.put(o1);
                // arr.put(o2);
                o1.put("TYPE","Method declaration modification");
                o1.put("VALUE",o2);
                o2.put("ACTION",actionType);
                o2.put("LINE_NUM",lineRange);
                o2.put("NAME",actionMsg);

            }
        }
        jsonObject.put("CHANGE_TYPE",change_type_arr);
    }

    public static void addFieldModificationType(ChangeEntity changeEntity,JSONObject jsonObject) {
        if(changeEntity.toString().toLowerCase().contains("delete") || changeEntity.toString().toLowerCase().contains("insert")){
            return;
        }
        JSONArray change_type_arr = new JSONArray();
        for (Action action : changeEntity.clusteredActionBean.actions) {
            String actionType = action.toString();
            String actionMsg =((Tree)(action).node).node.toString();
            String lineRange = ((Tree)(action).node).getRangeString();
            if (actionType.toLowerCase().contains("fielddeclaration") || actionType.toLowerCase().contains("literal") || actionType.toLowerCase().contains("simplename")) {
                JSONObject o1 = new JSONObject();
                JSONObject o2 = new JSONObject();
                JSONArray arr = new JSONArray();
                change_type_arr.put(o1);
                // arr.put(o2);
                o1.put("TYPE","Field value modification");
                o1.put("VALUE",o2);
                o2.put("ACTION",actionType);
                o2.put("LINE_NUM",lineRange);
                o2.put("NAME",actionMsg);

            } else if (actionType.toLowerCase().contains("annotation")) {
                JSONObject o1 = new JSONObject();
                JSONObject o2 = new JSONObject();
                JSONArray arr = new JSONArray();
                change_type_arr.put(o1);
                // arr.put(o2);
                o1.put("TYPE","Field annotation modification");
                o1.put("VALUE",o2);
                o2.put("ACTION",actionType);
                o2.put("LINE_NUM",lineRange);
                o2.put("NAME",actionMsg);
            } else if (actionType.toLowerCase().contains("modifier")) {
                JSONObject o1 = new JSONObject();
                JSONObject o2 = new JSONObject();
                JSONArray arr = new JSONArray();
                change_type_arr.put(o1);
                // arr.put(o2);
                o1.put("TYPE","Field modifier modification");
                o1.put("VALUE",o2);
                o2.put("ACTION",actionType);
                o2.put("LINE_NUM",lineRange);
                o2.put("NAME",actionMsg);

            }
            else {
                JSONObject o1 = new JSONObject();
                JSONObject o2 = new JSONObject();
                JSONArray arr = new JSONArray();
                change_type_arr.put(o1);
                // arr.put(o2);
                o1.put("TYPE","Field name modification");
                o1.put("VALUE",o2);
                o2.put("ACTION",actionType);
                o2.put("LINE_NUM",lineRange);
                o2.put("NAME",actionMsg);
                //msgSet.add("Field name modification");
            }
        }

        jsonObject.put("CHANGE_TYPE",change_type_arr);
    }

    public static void handleFieldNameModification(ChangeEntity changeEntity,ChangeEntity tempCE,MiningActionData miningActionData){

    }

    public static String formatString(String s){
        if(s.contains("@Statement")){
            s= s.substring(0, s.indexOf("@Statement"));
        }
        return s.replace("$",".").replace(".#","#");
    }

    public static void addInfo(MiningActionData miningActionData,JSONObject total){
        JSONObject actionArr = new JSONObject();
        JSONArray moveActionArr = new JSONArray();
        JSONArray updateActionArr = new JSONArray();
        JSONArray insertActionArr = new JSONArray();
        JSONArray deleteActionArr = new JSONArray();
        actionArr.put("moveAction",moveActionArr);
        actionArr.put("updateAction",updateActionArr);
        actionArr.put("insertAction",insertActionArr);
        actionArr.put("deleteAction",deleteActionArr);
        String actionType;
        String actionMsg ;
        String rangeString ;
        String res  ;
        for(Action action :miningActionData.mActionsMap.getMoveActions()){
            actionType = action.toString();
            actionMsg = ((Tree)((Move)action).node).node.toString();
            rangeString = ((Tree)((Move)action).node).getRangeString();
            res = actionType+":"+ actionMsg + ";" +"LINE_RANGE" + rangeString;
            moveActionArr.put(res);
        }

        for(Action action :miningActionData.mActionsMap.getUpdateActions()){
            actionType = action.toString();
            actionMsg = ((Tree)((Update)action).node).node.toString();
            rangeString = ((Tree)((Update)action).node).getRangeString();
            res = actionType+":"+ actionMsg + ";" +"LINE_RANGE" + rangeString;
            updateActionArr.put(res);
        }

        for(Action action :miningActionData.mActionsMap.getInsertActions()){
            actionType = action.toString();
            actionMsg = ((Tree)((Insert)action).node).node.toString();
            rangeString = ((Tree)((Insert)action).node).getRangeString();
            res = actionType+":"+ actionMsg + ";" +"LINE_RANGE" + rangeString;
            insertActionArr.put(res);
        }

        for(Action action :miningActionData.mActionsMap.getDeleteActions()){
            actionType = action.toString();
            actionMsg = ((Tree)((Delete)action).node).node.toString();
            rangeString = ((Tree)((Delete)action).node).getRangeString();
            res = actionType+":"+ actionMsg + ";" +"LINE_RANGE" + rangeString;
            deleteActionArr.put(res);
        }
        total.put("ALL_ACTIONS ",actionArr);
    }

    public static JSONObject getInfoJson(ChangeEntity changeEntity,String changeEntityType){
        JSONObject jsonObject = new JSONObject();
        String granularity = changeEntity.stageIIBean.getChangeEntity();
        String change = changeEntity.stageIIBean.getOpt();
        String possibleName = changeEntity.stageIIBean.getThumbnail();
        String entity = changeEntity.stageIIBean.getCanonicalName().getPrintName();
        String lineRange = changeEntity.stageIIBean.getLineRange();
        if(entity.endsWith("$")){
            String s = formatString(entity + possibleName);
            jsonObject.put("ENTITY",s);
            jsonObject.put("CHANGE","Change");
            jsonObject.put("DESC",changeEntity.getFrontData().toString().replace("add","update").replace("delete","update"));
        }
        else {
            String s = formatString(entity);
            jsonObject.put("ENTITY",s);
            jsonObject.put("CHANGE",change);
            jsonObject.put("DESC",changeEntity.getFrontData().toString());
        }
        jsonObject.put("GRANULARITY",granularity);
        jsonObject.put("LINE_RANGE",lineRange);

        return jsonObject;
    }

}
