package edu.fdu.se.global;

import edu.fdu.se.net.CommitFile;

import java.io.File;

/**
 * Created by huangkaifeng on 2018/8/21.
 */
public class FilePairData {
    @Override
    public String toString() {
        return "FilePairData{" +
                "commitFile=" + commitFile +
                ", prevPath='" + prevPath + '\'' +
                ", currPath='" + currPath + '\'' +
                '}';
    }

    public FilePairData(byte[] prevv, byte[] currr, String prevPathh, String currPathh,
                        CommitFile commitFile) {
        prev = prevv;
        curr = currr;
        prevPath = prevPathh;
        currPath = currPathh;
        this.commitFile = commitFile;
    }

    public FilePairData(byte[] prevv, byte[] currr, String prevPathh, String currPathh
    ) {
        prev = prevv;
        curr = currr;
        prevPath = prevPathh;
        currPath = currPathh;
        //this.commitFile = commitFile;
    }


    public byte[] getPrev() {
        return prev;
    }

    public byte[] getCurr() {
        return curr;
    }

    public String getPrevPath() {
        return prevPath;
    }

    public String getCurrPath() {
        return currPath;
    }


    private CommitFile commitFile;
    private byte[] prev;
    private byte[] curr;
    private String prevPath;
    private String currPath;

    public String getParentCommit() {
        return commitFile.getParent_commit();
    }


    public String getFileFullPackageName() {
        return commitFile.getFile_full_name();
    }


    public String getFileShortName() {
        return commitFile.getFile_short_name();
    }

    /**
     * 重写，因为不确定要提取.java .c .cpp .h 哪个文件，分开写又很繁琐，于是直接提取“.”以后的内容来确定后缀名
     * 但是，如果 prevPath 字符串中包含多个点号 . 或者点号 . 后面的字符串不是文件后缀名，这里就会出问题
     */
    public String getFileName() {
        //index代表最后一个文件路径分隔符的索引位置
        int index = Global.prevDir.lastIndexOf(File.separator);
        String s = Global.prevDir.substring(index);
        //获取后缀名标记"."的出现位置
        return prevPath.substring(prevPath.indexOf(s) + 1);
        //return prevPath.substring(prevPath.indexOf(s) + 1, prevPath.indexOf(".java") + 5);
    }

    public String getMd5FileName() {
        return commitFile.getmd5FileName();
    }

}
