package edu.fdu.se.core.links.linkbean;


import edu.fdu.se.core.links.generator.LinkConstants;
import edu.fdu.se.core.miningactions.bean.MyRange;

public class Use extends LinkType {

    public String locClass;
    public MyRange locRange;
    public String varName;
    public String varNameType;
    public int useTypeInt;

    public int useTree;

    public String qualifier;

    /**
     * Use local var, use field , use method invocation, use class creation
     *
     * @param locClass
     * @param locRange
     * @param varName
     */
    public Use(String locClass, MyRange locRange, String varName, int useTypeInt,int treeType) {
        this.locClass = locClass;
        this.locRange = locRange;
        this.varName = varName;
        this.useTypeInt = useTypeInt;
        this.useTree = treeType;
    }

    public void setTypeNameAndUseType(String[] data){
        if(data== null){
            return;
        }
        if(data[0]!=null) {
            varNameType = data[0];
            int useType = Integer.valueOf(data[1]);
            if(useTypeInt == LinkConstants.USE_LOCAL_VAR){
                if(useType == LinkConstants.USE_PARAMS){
                    useTypeInt = LinkConstants.USE_PARAMS;
                }
            }
        }

    }

    @Override
    public boolean equals(Object e) {
        Use us = (Use) e;
        if (this.varName.equals(us.varName)) {
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        String res = "Use \"%s\" as \"%s\" in %s %s %d";
        String typpe = null;
        switch (useTypeInt) {
            case LinkConstants.USE_FIELD:
                typpe = "field";
                break;
            case LinkConstants.USE_FIELD_LOCAL:
                typpe = "local field";
                break;
            case LinkConstants.USE_LOCAL_VAR:
                typpe = "local var";
                break;
            case LinkConstants.USE_METHOD_INVOCATION:
                typpe = " method invoke";
                break;
            case LinkConstants.USE_CLASS_CREATION:
                typpe = "class creation";
                break;
            case LinkConstants.USE_STATIC_IMPORT_FIELD:
                typpe = "static field import";
                break;
        }
        String result = String.format(res, typpe, varName, locClass, locRange.toString(),useTree);
        return result;
    }


}
