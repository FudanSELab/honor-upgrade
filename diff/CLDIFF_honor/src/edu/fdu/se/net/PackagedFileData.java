package edu.fdu.se.net;

/**
 * 请求文件 diff link 的返回结果
 */
public class PackagedFileData {

    String prev;
    String curr;
    String diff;
    String link;

    public PackagedFileData() {
    }

    public PackagedFileData(String prev, String curr, String diff, String link) {
        this.prev = prev;
        this.curr = curr;
        this.diff = diff;
        this.link = link;
    }

    public String getPrev() {
        return prev;
    }

    public void setPrev(String prev) {
        this.prev = prev;
    }

    public String getCurr() {
        return curr;
    }

    public void setCurr(String curr) {
        this.curr = curr;
    }

    public String getDiff() {
        return diff;
    }

    public void setDiff(String diff) {
        this.diff = diff;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }
}
