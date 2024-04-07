package edu.fdu.se.core.preprocessingfile.data;

import edu.fdu.se.fileutil.ExecuteCmd;
import edu.fdu.se.fileutil.FileRWUtil;
import edu.fdu.se.global.Constants;
import edu.fdu.se.global.Global;

import java.io.File;
import java.util.List;

/**
 * Created by huangkaifeng on 2018/3/18.
 *
 */
public class FileOutputLog {

    public String rootPath;
    public String projName;
    public String commitId;
    public String metaLinkPath;
    public String currSourceFile;
    public String prevSourceFile;
    public String sourceGen;

    public String middleGenPathPrev;
    public String middleGenPathCurr;
    public String debugModeFilePath;

    public FileOutputLog(String rootPath,String projName){
        this.rootPath = rootPath;
        this.projName = projName;
    }

    public void setCommitId(String commit,List<String> parentCommits){
        this.commitId = commit;
        this.metaLinkPath = rootPath+"/"+projName+"/"+ commit;

        this.prevSourceFile = metaLinkPath + "/" + Constants.SaveFilePath.PREV;
        this.currSourceFile = metaLinkPath + "/" + Constants.SaveFilePath.CURR;
        this.sourceGen = metaLinkPath + "/" + Constants.SaveFilePath.GEN;
        for(String s:parentCommits){
            File temp = new File(this.prevSourceFile+"/"+s);
            temp.mkdirs();
            temp = new File(this.currSourceFile+"/"+s);
            temp.mkdirs();
            temp = new File(this.sourceGen+"/"+s);
            temp.mkdirs();
            this.middleGenPathPrev = temp.getAbsolutePath() + "/" + Constants.SaveFilePath.PREV;
            this.middleGenPathCurr = temp.getAbsolutePath() + "/" + Constants.SaveFilePath.CURR;
            temp = new File(this.middleGenPathPrev);
            temp.mkdirs();
            temp = new File(this.middleGenPathCurr);
            temp.mkdirs();
        }
    }


    public void setCommitId(String commit){
        this.commitId = commit;
        this.metaLinkPath = rootPath+"/"+projName+"/"+ commit;

        this.prevSourceFile = metaLinkPath + "/" + Constants.SaveFilePath.PREV;
        this.currSourceFile = metaLinkPath + "/" + Constants.SaveFilePath.CURR;
        this.sourceGen = metaLinkPath+"/gen";

        File temp = new File(this.prevSourceFile);
        temp.mkdirs();
        temp = new File(this.currSourceFile);
        temp.mkdirs();
        temp = new File(this.sourceGen);
        temp.mkdirs();
        this.middleGenPathPrev = temp.getAbsolutePath() + "/" + Constants.SaveFilePath.PREV;
        this.middleGenPathCurr = temp.getAbsolutePath() + "/" + Constants.SaveFilePath.CURR;
        temp = new File(this.middleGenPathPrev);
        temp.mkdirs();
        temp = new File(this.middleGenPathCurr);
        temp.mkdirs();
    }


    public void writeTreeFile(String prevTree,String currTree){
//        FileRWUtil.writeInAll(this.middleGenPathPrev + "/Tree" + Global.md5FileName + ".txt", prevTree);
//        FileRWUtil.writeInAll(this.middleGenPathCurr + "/Tree" + Global.md5FileName + ".txt", currTree);
        FileRWUtil.writeInAll("C:\\Users\\Administrator\\Desktop\\trees\\" +Global.prevDir.substring(Global.prevDir.lastIndexOf("\\")+1) + "-" + Global.currDir.substring(Global.currDir.lastIndexOf("\\")+1)+ "/prev/" + Global.fileShortName + ".txt", prevTree);
        FileRWUtil.writeInAll("C:\\Users\\Administrator\\Desktop\\trees\\" +Global.prevDir.substring(Global.prevDir.lastIndexOf("\\")+1) + "-" + Global.currDir.substring(Global.currDir.lastIndexOf("\\")+1)+ "/curr/" + Global.fileShortName + ".txt", currTree);
    }


    public void writeEntityJson(String json){
        String path = null;
        if (Constants.PARENTCOMMITNULL.equals(Global.parentCommit)) {
            path = this.sourceGen + "/" + String.format(Constants.DIFF_JSON_FILE, Global.fileShortName);
        } else {
            path = this.sourceGen + "/" + Global.parentCommit + "/" + String.format(Constants.DIFF_JSON_FILE, Global.md5FileName);
        }
        FileRWUtil.writeInAll(path, json);
    }

    public void writeSourceFile(byte[] prev,byte[] curr,String fileName){
        if(prev!=null) {
            FileRWUtil.writeInAll(this.prevSourceFile + "/" + fileName, prev);
        }
        FileRWUtil.writeInAll(this.currSourceFile + "/" + fileName, curr);
    }

    public void writeMetaFile(String metaJson){
        String path = this.metaLinkPath + "/" + Constants.META_JSON;
//        Global.outputFilePathList.add(path);
        FileRWUtil.writeInAll(path, metaJson);
    }


    public void writeLinkJson(String link){
        String path = this.metaLinkPath + "/" + Constants.LINK_JSON;
        FileRWUtil.writeInAll(path, link);
    }

    public void writeGraphJson(String graph) {
        String path = this.metaLinkPath + "/" + String.format(Constants.GRAPH_JSON,this.projName,this.commitId);
        FileRWUtil.writeInAll(path, graph);
    }

    public void writeDotFile(String dotFileContent) {
        String path = this.metaLinkPath + "/" + Constants.DOT_FILE;
        String png = this.metaLinkPath + "/" + Constants.PNG_FILE;
        FileRWUtil.writeInAll(path, dotFileContent);
        ExecuteCmd.execCmd(String.format("dot %s -T png -o %s", path, png));
    }

    public void writeErrFile(String message) {
        if (debugModeFilePath != null) {
            System.err.println(message);
            FileRWUtil.writeErrorLog(this.debugModeFilePath, this.commitId + "    " + message + "\n\t");
        }
    }



}
