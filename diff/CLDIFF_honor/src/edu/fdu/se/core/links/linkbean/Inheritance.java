package edu.fdu.se.core.links.linkbean;

import java.util.List;

public class Inheritance extends LinkType {

    /**
     * 本 class name
     */
    public String className;
    public int isClassAbstract;

    /**
     * 继承
     */
    public String superClass;
    public List<String> interfazz;

    public Inheritance(String className, int isClassNameAbs, String superClass, List<String> interfazz) {
        this.className = className;
        this.superClass = superClass;
        this.interfazz = interfazz;
        this.isClassAbstract = isClassNameAbs;
    }

}
