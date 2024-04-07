package edu.fdu.se.core.links.linkbean;

import edu.fdu.se.core.links.generator.LinkConstants;
import edu.fdu.se.core.miningactions.bean.MyRange;
import edu.fdu.se.core.miningchangeentity.base.ChangeEntityDesc;

import java.util.List;

/**
 * def method; def field; def local var
 */
public class Def extends LinkType {


    /**
     * LinkConstants.DEF_FIELD
     * LinkConstants.DEF_METHOD
     * LinkConstants.DEF_LOCAL_VAR
     * LinkConstants.DEF_CLASS
     */
    public int defTypeInt;


    /**
     * 如果是def method/field defLoc就是class路径
     * 如果是def local bar defLoc就是class路径加方法名
     */
    public String defLoc;
    public String defType;
    /**
     * varName or methodName
     */
    public String varName;

    public MyRange locRange;


    /**
     * ChangeEntityDesc.TreeType
     */
    public int defTree;

    /**
     * method def parameters
     */
    public List<MyParameters> myParameters;

    @Override
    public String toString() {
        String res = "Def \"%s\" as \"%s\" in %s %s %d";
        String typeStr = null;
        switch (defTypeInt) {
            case LinkConstants.DEF_FIELD:
                typeStr = "field";
                break;
            case LinkConstants.DEF_METHOD:
                typeStr = "method";
                break;
            case LinkConstants.DEF_LOCAL_VAR:
                typeStr = "local var";
                break;
            case LinkConstants.DEF_CLASS:
                typeStr = "class";
                break;
            default:
                break;
        }
        String result = String.format(res, typeStr.toUpperCase(), varName, defLoc, locRange,defTree);
        return result;
    }


    /**
     * field
     *
     * @param loc     classname
     * @param defType
     * @param varName
     */
    public Def(String loc, String defType, String varName, MyRange myRange,int treeType) {
        this.defLoc = loc;
        this.defTypeInt = LinkConstants.DEF_FIELD;
        this.defType = defType;
        this.varName = varName;
        this.locRange = myRange;
        this.defTree = treeType;
    }

    /**
     * class
     *
     * @param loc
     * @param varName
     * @param defTypeInt
     */
    public Def(String loc, String varName, int defTypeInt, MyRange myRange, int treeType) {
        this.defTypeInt = LinkConstants.DEF_CLASS;
        this.varName = varName;
        this.defLoc = loc;
        this.locRange = myRange;
        this.defTree = treeType;
    }


    /**
     * def method
     * @param loc
     * @param returnType
     * @param varName
     * @param myParameters
     */
    public Def(String loc, String returnType, String varName, List<MyParameters> myParameters, MyRange myRange,int treeType) {
        this.defLoc = loc;
        this.defTypeInt = LinkConstants.DEF_METHOD;
        this.varName = varName;
        this.locRange = myRange;
        this.defTree = treeType;
        this.myParameters = myParameters;
    }


    /**
     * @param loc
     * @param varName
     */
    public Def(String loc, String varName, MyRange myRange, int treeType) {
        this.defLoc = loc;
        this.defTypeInt = LinkConstants.DEF_LOCAL_VAR;
        this.varName = varName;
        this.locRange = myRange;
        this.defTree = treeType;
    }


    @Override
    public boolean equals(Object de) {
        Def dee = (Def) de;
        switch (dee.defTypeInt) {
            case LinkConstants.DEF_LOCAL_VAR:
            case LinkConstants.DEF_METHOD:
            case LinkConstants.DEF_FIELD:
            case LinkConstants.DEF_CLASS:
                if (this.varName.equals(dee.varName)) {
                    return true;
                }
        }
        return false;
    }


}
