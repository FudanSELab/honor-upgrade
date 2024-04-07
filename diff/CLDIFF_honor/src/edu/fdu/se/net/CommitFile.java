package edu.fdu.se.net;

import edu.fdu.se.fileutil.MyMD5Util;
import edu.fdu.se.global.Constants;

/**
 * 用于生成
 */

public class CommitFile {

    /**
     * file_name : ExecutorConfigurationSupport.java
     * prev_file_path : prev/9d63f805b3b3ad07f102f6df779b852b2d1f306c/ExecutorConfigurationSupport.java
     * curr_file_path : curr/9d63f805b3b3ad07f102f6df779b852b2d1f306c/ExecutorConfigurationSupport.java
     * parent_commit : 9d63f805b3b3ad07f102f6df779b852b2d1f306c
     */
    private int id;
    private String file_full_name;
    private String file_short_name;
    private String prev_file_path;
    private String curr_file_path;
    private String parent_commit;
    private String diffPath;
    private String md5;


    public CommitFile(int id,String fileFullName,String fileShortName,boolean prevPath,boolean currPath,String parentCommit) {
        this.id = id;
        this.file_full_name = fileFullName;
        this.file_short_name = fileShortName;
        this.parent_commit = parentCommit;
        this.setMd5();
        this.setPrev_file_path(prevPath);
        this.setCurr_file_path(currPath);
    }

    public String getmd5FileName(){
        return this.md5.substring(0, 8) + "_" + this.file_short_name;
    }


    private void setMd5() {
        this.md5 = MyMD5Util.encrypt(this.file_full_name);
    }


    public String getDiffPath() {
        return diffPath;
    }

    public void setDiffPath(boolean isExist) {
        if(isExist) {
            this.diffPath = Constants.SaveFilePath.GEN + "/" + parent_commit + "/" + String.format(Constants.DIFF_JSON_FILE, this.md5.substring(0, 8) + "_" + this.file_short_name);
        }else{
            this.diffPath = null;
        }
    }


    public String getFile_full_name() {
        return file_full_name;
    }


    public String getFile_short_name() {
        return file_short_name;
    }


    public String getPrev_file_path() {
        return prev_file_path;
    }

    private void setPrev_file_path(boolean isExist) {
        if(isExist) {
            this.prev_file_path = Constants.SaveFilePath.PREV + "/" + parent_commit + "/" + this.md5.substring(0, 8) + "_" + this.file_short_name;
        }else{
            this.prev_file_path = null;
        }
    }

    public String getCurr_file_path() {
        return curr_file_path;
    }

    private void setCurr_file_path(boolean isExist) {
        if(isExist) {
            this.curr_file_path = Constants.SaveFilePath.CURR + "/" + parent_commit + "/" + this.md5.substring(0, 8) + "_" + this.file_short_name;
        }else{
            this.curr_file_path = null;
        }
    }

    public String getParent_commit() {
        return parent_commit;
    }



}
