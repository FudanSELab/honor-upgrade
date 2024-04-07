package edu.fdu.se.core.links.linkbean;

import com.github.gumtreediff.tree.Tree;
import edu.fdu.se.core.links.generator.LinkConstants;
import edu.fdu.se.core.miningactions.bean.MiningActionData;
import edu.fdu.se.core.miningactions.bean.MyRange;
import edu.fdu.se.global.Global;

import java.time.chrono.MinguoChronology;
import java.util.ArrayList;
import java.util.List;

public class UseList {

    public UseList() {
        uses = new ArrayList<>();
    }

    public int size() {
        return uses.size();
    }

    private List<Use> uses;

    /**
     * 使用参数包装use,加入UseList.uses并返回
     * @param locClass LongName
     * @param locRange LineRange
     * @param varName var
     * @return use
     */
    public Use addUse(String locClass, MyRange locRange, String varName, int useTypeInt, int treeType, Tree tree, MiningActionData mad) {
        if ("".equals(varName)) {
            return null;
        }
        Use use = new Use(locClass, locRange, varName, useTypeInt,treeType);
        if (uses.contains(use)) {
            return null;
        }
        if(LinkConstants.USE_LOCAL_VAR == useTypeInt) {
            if(tree != null && mad !=null) {
                this.resolveAndSetUseVarType(use, tree, mad);
            }
            if (use.useTypeInt == LinkConstants.USE_PARAMS) {
                return null;
            }
        }
        uses.add(use);
        return use;
    }

    private void resolveAndSetUseVarType(Use use, Tree tree, MiningActionData mad){
        String[] data2 = Global.astNodeUtil.resolveTypeOfVariable(use.varName,tree.getNode(),mad);
        use.setTypeNameAndUseType(data2);

    }

    public StaticFieldUse addStaticFieldUse(String canonicalName,MyRange locRange, String varName, int treeType, String staticImportName){
        if("".equals(varName)){
            return null;
        }
        StaticFieldUse use = new StaticFieldUse(canonicalName, locRange,varName, LinkConstants.USE_STATIC_IMPORT_FIELD,treeType, staticImportName);
        if (uses.contains(use)){
            return null;
        }
        uses.add(use);
        return use;
    }

    public MethodUse addMethodInvocationUse(String canonicalName,MyRange locRange, String varName, int treeType, String methodCallVarName){
        if("".equals(varName)){
            return null;
        }
        MethodUse use = new MethodUse(canonicalName, locRange,varName, LinkConstants.USE_METHOD_INVOCATION,treeType, methodCallVarName);
        if (uses.contains(use)){
            return null;
        }
        uses.add(use);
        return use;
    }

    public List<Use> getUses() {
        return uses;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Use use : uses) {
            sb.append(use.toString());
            sb.append("\n");
        }
        return sb.toString();
    }

}
