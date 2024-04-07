package edu.fdu.se.core.links.linkbean;

public class LinkBean {

    public Inheritance inheritance;
    public DefList defList;
    public UseList useList;

    public LinkBean() {
        defList = new DefList();
        useList = new UseList();
    }

    public DefList getDefList() {
        if (defList == null) {
            defList = new DefList();
        }
        return defList;
    }

    public UseList getUseList() {
        if (useList == null) {
            useList = new UseList();
        }
        return useList;
    }
}
