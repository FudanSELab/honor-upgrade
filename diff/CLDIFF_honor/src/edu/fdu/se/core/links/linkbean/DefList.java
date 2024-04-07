package edu.fdu.se.core.links.linkbean;

import edu.fdu.se.core.links.generator.LinkConstants;
import edu.fdu.se.core.miningactions.bean.MyRange;

import java.util.ArrayList;
import java.util.List;

public class DefList {

    /**
     * List of : def method; def field; def local var
     */
    private List<Def> defs;

    public DefList() {
        defs = new ArrayList<>();
    }

    public int size() {
        return defs.size();
    }


    /**
     * 包装为def，添加到 defs <List>
     * varName 不能为 null
     * defs 不能含有相同的 def
     */
    public void addDef(String loc, String defType, String varName, MyRange myRange,int treeType) {
        if (nullCheck(varName)) {
            return;
        }
        Def def = new Def(loc, defType, varName, myRange,treeType);
        if (this.defs.contains(def)) {
            return;
        }
        this.defs.add(def);
    }


//    /**
//     * Field
//     *
//     * @param loc
//     * @param defType
//     * @param varName
//     */
//    public void addDef(String loc, String defType, List<String> varName) {
//        for (String name : varName) {
//            addDef(loc, defType, name);
//        }
//    }


    /**
     * 存入方法的 CanonicalName、返回类型、方法名、参数列表、lineRange、文件所属类型
     *
     * @param loc
     * @param returnType
     * @param varName
     * @param myParameters
     */
    public void addDef(String loc, String returnType, String varName, List<MyParameters> myParameters, MyRange myRange, int treeType) {
        if (nullCheck(varName)) {
            return;
        }
        Def def = new Def(loc, returnType, varName, myParameters, myRange,treeType);
        if (this.defs.contains(def)) {
            return;
        }
        this.defs.add(def);
    }

    /**
     * local var
     *
     * @param loc
     * @param varName
     */
    public void addDef(String loc, String varName, MyRange myRange, int treeType) {
        if (nullCheck(varName)) {
            return;
        }
        Def def = new Def(loc, varName, myRange,treeType);
        if (this.defs.contains(def)) {
            return;
        }
        this.defs.add(def);

    }

    /**
     * 将参数信息包装为一个 Def
     * 如果：varName 不为空且 def 内不包含该 Def
     * 则加入defs
     * @param varName
     */
    public void addDef(String loc, String varName, int defTypeInt, MyRange myRange, int treeType) {
        if (nullCheck(varName)) {
            return;
        }
        Def def = new Def(loc, varName, LinkConstants.DEF_CLASS, myRange, treeType);
        if (this.defs.contains(def)) {
            return;
        }
        this.defs.add(def);
    }

    private boolean nullCheck(String varName) {
        if (("").equals(varName)) {
            return true;
        }
        if (varName == null) {
            return true;
        }
        return false;

    }


    public List<Def> getDefs() {
        return this.defs;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Def def : defs) {
            sb.append(def.toString());
            sb.append("\n");
        }
        return sb.toString();
    }

}
