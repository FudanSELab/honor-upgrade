package edu.fdu.se.core.links.linkbean;

import edu.fdu.se.core.miningactions.bean.MyRange;

import java.util.ArrayList;

public class StaticFieldUse extends Use {


    public StaticFieldUse(String canonicalName, MyRange locRange, String varName, int useTypeInt, int treeType, String staticImportString) {
        super(canonicalName, locRange, varName, useTypeInt, treeType);
        parseString(staticImportString);

    }

    public String packageName;
    public String className;

    private void parseString(String s){
        int i = s.lastIndexOf(".");
        String prefix = s.substring(0,i);
        String name = s.substring(i+1);
        i = prefix.lastIndexOf(".");
        packageName = prefix.substring(0,i);
        className = prefix.substring(i+1);
        System.out.println("a");


    }


    @Override
    public String toString(){
        String res = "StaticFieldUse \"%s\" as \"%s.%s.%s\" in %s %s %d";
        String typpe = " static ref";
        String result = String.format(res, typpe,packageName,className, varName, locClass, locRange.toString(),useTree);
        return result;
    }


}