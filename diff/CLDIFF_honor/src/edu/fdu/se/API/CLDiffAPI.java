package edu.fdu.se.API;

//import com.alibaba.fastjson.JSONObject;

import com.google.gson.GsonBuilder;
import edu.fdu.se.core.links.generator.*;
import edu.fdu.se.core.miningactions.bean.MiningActionData;
import edu.fdu.se.fileutil.FileRWUtil;
import edu.fdu.se.global.Constants;
import edu.fdu.se.global.FilePairData;
import edu.fdu.se.global.Global;
import edu.fdu.se.core.preprocessingfile.data.FileOutputLog;
import edu.fdu.se.fileutil.ResponseTextToFiles;
import edu.fdu.se.net.Meta;
import edu.fdu.se.net.CommitFile;
//import edu.fdu.se.util.JSONObject;
//import jdk.internal.vm.compiler.collections.EconomicMap;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * Created by huangkaifeng on 2018/4/12.
 */
public class CLDiffAPI {

    private Map<String, MiningActionData> fileNameMadMap;
    private LinkGraph linkGraph;
    public CLDiffCore clDiffCore;
    private List<FilePairData> filePairDatas;

    /**
     * output path +"proj_name" + "commit_id"
     *
     * @param outputDir
     */
    public CLDiffAPI(String outputDir, Meta meta) {
        filePairDatas = new ArrayList<>();
        Global.filePairDatas = filePairDatas;
        clDiffCore = new CLDiffCore();
        Global.fileOutputLog = new FileOutputLog(outputDir, meta.getProject_name());
        Global.fileOutputLog.setCommitId(meta.getCommit_hash(), meta.getParents());
        fileNameMadMap = new HashMap<>();
        initDataFromJson(meta);
        ResponseTextToFiles.createFile(Constants.META_JSON, new GsonBuilder().setPrettyPrinting().create().toJson(meta), new File(Global.fileOutputLog.metaLinkPath));
    }

    public CLDiffAPI(String outputDir, Map<String, Set<String>> map) {
        filePairDatas = new ArrayList<>();
        Global.filePairDatas = filePairDatas;
        clDiffCore = new CLDiffCore();
        Global.fileOutputLog = new FileOutputLog(outputDir, "hh");
        //在输出目录中写入error log
        Global.fileOutputLog.debugModeFilePath = outputDir + File.separator + "log";
        //Global.fileOutputLog.setCommitId(meta.getCommit_hash(), meta.getParents());
        fileNameMadMap = new HashMap<>();
        initData(map);
        //ResponseTextToFiles.createFile(Constants.META_JSON, new GsonBuilder().setPrettyPrinting().create().toJson(meta), new File(Global.fileOutputLog.metaLinkPath));
    }

    /**
     * 初始化FilePairData数据，里面存储了bytes和path数据
     * @param map
     */
    public void initData(Map<String, Set<String>> map) {
        Set<String> set = map.get("modify");
        Set<String> set2 = map.get("add");
        Set<String> set3 = map.get("delete");

        for (String file : set) {
            String prevFilePath = Global.prevDir + file;
            String currFilePath = Global.currDir + file;
            byte[] prevBytes = null;
            byte[] currBytes = null;
            try {
                prevBytes = Files.readAllBytes(Paths.get(prevFilePath));
                currBytes = Files.readAllBytes(Paths.get(currFilePath));
            } catch (Exception e) {
                e.printStackTrace();
            }
            FilePairData fp = new FilePairData(prevBytes, currBytes,
                    prevFilePath, currFilePath
            );
            filePairDatas.add(fp);
        }

        for (String file : set2) {
            String prevFilePath = Global.prevDir + file;
            String currFilePath = Global.currDir + file;

            byte[] prevBytes = null;
            byte[] currBytes = null;
            try {
                currBytes = Files.readAllBytes(Paths.get(currFilePath));
            } catch (Exception e) {
                e.printStackTrace();
            }
            FilePairData fp = new FilePairData(prevBytes, currBytes,
                    prevFilePath, currFilePath
            );
            filePairDatas.add(fp);
        }

        for (String file : set3) {
            String prevFilePath = Global.prevDir + file;
            String currFilePath = Global.currDir + file;

            byte[] prevBytes = null;
            byte[] currBytes = null;
            try {
                Paths.get(prevFilePath);
                prevBytes = Files.readAllBytes(Paths.get(prevFilePath));
            } catch (Exception e) {
                e.printStackTrace();
            }
            FilePairData fp = new FilePairData(prevBytes, currBytes,
                    prevFilePath, currFilePath
            );
            filePairDatas.add(fp);
        }
    }

    public void initDataFromJson(Meta meta) {
        List<CommitFile> commitFiles = meta.getFiles();
        List<String> actions = meta.getActions();
        for (int i = 0; i < commitFiles.size(); i++) {
            CommitFile file = commitFiles.get(i);
            if (file.getDiffPath() == null) {
                continue;
            }
            String prevFilePath = file.getPrev_file_path();
            String currFilePath = file.getCurr_file_path();
            String basePath = Global.fileOutputLog.metaLinkPath;
            byte[] prevBytes = null;
            byte[] currBytes = null;
            try {
                if (prevFilePath != null) {
                    prevBytes = Files.readAllBytes(Paths.get(basePath + "/" + prevFilePath));
                }
                if (currFilePath != null) {
                    currBytes = Files.readAllBytes(Paths.get(basePath + "/" + currFilePath));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            FilePairData fp = new FilePairData(prevBytes, currBytes,
                    basePath + "/" + prevFilePath, basePath + "/" + currFilePath,
                    file);
            filePairDatas.add(fp);
        }
    }

    public void generateDiffMinerOutput() {
        grouping();
        fileNameMadMap.clear();
    }

    void grouping() {
        String absolutePath = Global.fileOutputLog.metaLinkPath;
        Global.changeEntityFileNameMap = new HashMap<>();
        JSONObject jsonObject = new JSONObject(new LinkedHashMap<>());//LinkedHashMap有插入顺序
        JSONArray nodiff = new JSONArray();
        JSONArray addedFiles = new JSONArray();
        JSONArray deletedFiles = new JSONArray();
        JSONArray modifiedFiles = new JSONArray();
        //将json文件变成三个大结构：ADDED_FILES DELETED_FILES MODIFIED_FILES，其下加入数据
        jsonObject.put("NO_DIFF", nodiff);
        jsonObject.put("ADDED_FILES", addedFiles);
        jsonObject.put("DELETED_FILES", deletedFiles);
        jsonObject.put("MODIFIED_FILES", modifiedFiles);
        StringBuilder sql1 = new StringBuilder();
        int num = 0;
        //遍历每一个FilePairData，也就是每一个prevBytes, currBytes, prevFilePath, currFilePath
        for (FilePairData fp : filePairDatas) {
            //获取文件名字，以pre为准
            String[] s = fp.getFileName().split("\\.");
            //TODO 都需要考虑哪些文件？目前已知C和C++可能需要分别处理
            // TODO java /C 不同设置
            Global.parentCommit = "test";
            Global.fileShortName = fp.getFileName();
            Global.fileFullPathName = fp.getFileName();
            Global.fileName = fp.getFileName().substring(fp.getFileName().indexOf(File.separator) + 1).replace("\\","/");
            //不处理Prev数据和Curr数据都为null的情况
            if (fp.getPrev() == null && fp.getCurr() == null) {
                continue;
            }
            JSONObject jsonObject1 = new JSONObject();
            //如果Prev为null，则为新增文件
            if (fp.getPrev() == null) {
                //added files
                this.clDiffCore.dooSingleFile(fp.getFileName(), fp.getCurr(), absolutePath, Constants.ChangeTypeString.ADD);

                jsonObject1.put("FILE_NAME", fp.getFileName());
                jsonObject1.put("FULL_PATH", fp.getCurrPath());
                addedFiles.put(jsonObject1);

                String s1 = fp.getCurrPath().replace("\\","/");
                if(s1.contains(Global.relativeRoot)){
                    s1 = s1.substring(s1.indexOf(Global.relativeRoot));
                }
                sql1.append(String.format("('%s','%s','%s','%s','%s','%s')", Global.task_id, Global.repoId, Global.fileName, "NULL", s1, "add")).append(",");
            } else if (fp.getCurr() == null) {
                //否则Curr为null是删除文件
                // TODO
                this.clDiffCore.dooSingleFile(fp.getFileName(), fp.getPrev(), absolutePath, Constants.ChangeTypeString.DELETE);

                jsonObject1.put("FILE_NAME", fp.getFileName());
                jsonObject1.put("FULL_PATH", fp.getPrevPath());
                deletedFiles.put(jsonObject1);

                String s2 =fp.getPrevPath().replace("\\","/");
                if(s2.contains(Global.relativeRoot)){
                    s2 = s2.substring(s2.indexOf(Global.relativeRoot));
                }
                sql1.append(String.format("('%s','%s','%s','%s','%s','%s')", Global.task_id, Global.repoId, Global.fileName, s2, "NULL", "delete")).append(",");
            } else {
                //都存在那说明可能是修改文件
                try{
                    jsonObject1 = this.clDiffCore.dooDiff(fp.getFileName(), fp.getPrev(), fp.getCurr(), absolutePath);
                }catch (Exception e){
                    Global.fileOutputLog.writeErrFile("error File: " + fp.getFileName() + "\n\t" + e.getLocalizedMessage());
                    e.printStackTrace();
                }

                if (jsonObject1 != null){
                    modifiedFiles.put(jsonObject1);
                }
                else{
                    nodiff.put(fp.getFileName());
                }
            }
            if (this.clDiffCore.mad != null) {
                this.fileNameMadMap.put("hh" + Constants.TOOLSPLITTER + this.clDiffCore.mad.fileFullPackageName, this.clDiffCore.mad);
            }
            num++;
            if(num%10==0){
                fileOut(jsonObject,num/10);
                jsonObject = new JSONObject(new LinkedHashMap<>());
                nodiff = new JSONArray();
                addedFiles = new JSONArray();
                deletedFiles = new JSONArray();
                modifiedFiles = new JSONArray();
                //将json文件变成三个大结构：ADDED_FILES DELETED_FILES MODIFIED_FILES，其下加入数据
                jsonObject.put("NO_DIFF", nodiff);
                jsonObject.put("ADDED_FILES", addedFiles);
                jsonObject.put("DELETED_FILES", deletedFiles);
                jsonObject.put("MODIFIED_FILES", modifiedFiles);
            }

        }
        fileOut(jsonObject,num/10 + 1);

//        try {
//            if(!sql1.toString().equals("")){
//                sql1 = new StringBuilder(sql1.substring(0, sql1.length() - 1));
//                Connection conn = Global.conn;
//                Statement stmt = conn.createStatement();
//                String sql = "insert ignore into changed_file (task_id,repo_id,file_name,prev_path,curr_path,change_type) values " + sql1;
//                stmt.executeUpdate(sql);
//            }
//        }
//        catch (Exception e){
//            e.printStackTrace();
//        }

        if (Global.parentCommit == null) {
            return;
        }
        Global.result.put(jsonObject);
    }

    private void fileOut(JSONObject jsonObject,int numFile) {
        // result判空，非空才输出
        //if (Global.result.length() != 0 && Global.result != null) {
        if(jsonObject != null){
//            StringBuilder sb = new StringBuilder();
//            sb.append(File.separator);
            String suffix = ".json";

            String outputFileName =  Global.outputDir + File.separator + "diff" + File.separator +"diff_result";
            String outputFileName1 =  Global.outputDir + File.separator + "align" + File.separator + "align_result";
            //File outputFileParent =new File(Global.outputDir+File.separator + "diff") ;

             String outputPath = outputFileName + numFile + suffix;
             String outputAlignPath = outputFileName1 + numFile + suffix;


            System.out.println(Global.prevDir + " -- " + Global.currDir + "   --> " + outputPath);
            //FileRWUtil.writeInAll(outputPath, Global.result.toString(4));
            FileRWUtil.writeInAll(outputPath, jsonObject.toString(4));
            FileRWUtil.writeInAll(outputAlignPath, Global.alignResult.toString(4));
        } else {
            System.out.println(Global.prevDir + " -- " + Global.currDir + "   --> no diff");
        }
    }


}
