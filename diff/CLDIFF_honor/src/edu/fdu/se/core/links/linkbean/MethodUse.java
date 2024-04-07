package edu.fdu.se.core.links.linkbean;

import edu.fdu.se.core.links.generator.LinkConstants;
import edu.fdu.se.core.miningactions.bean.MyRange;

import java.util.ArrayList;
import java.util.List;

public class MethodUse extends Use {

    /**
     * Use local var, use field , use method invocation, use class creation
     *
     * @param canonicalName
     * @param locRange
     * @param varName methodName
     */
    public MethodUse(String canonicalName, MyRange locRange, String varName, int useTypeInt, int treeType,String useMethodVarName) {
        super(canonicalName,locRange,varName,useTypeInt,treeType);
        this.useMethodVarName = useMethodVarName;
        paramList = new ArrayList<>();
    }

    public String useMethodVarName;
    public String useMethodVarTypeName;

    /**
     * variable of a method invocation is: field, local var, param
     */
    public int useType;
    public List<MyParameters> paramList;



    /**
     * varName = data[0], varType =  data[1], rest are params
     * @param data
     */
    public void addToParamList(List<String> data){
        if (data == null){
            return;
        }
        int size = data.size()/2;
        for(int i=1;i<size;i++){
            String name = data.get(i*2);
            String type = data.get(i*2+1);
            MyParameters p = new MyParameters(name,type);
            this.paramList.add(p);
        }
    }

    public void setTypeNameAndUseType(String[] data){
        if(data== null){
            return;
        }
        if(data[0]!=null) {
            useMethodVarTypeName = data[0];
            useType = Integer.valueOf(data[1]);
        }

    }

    @Override
    public String toString(){
        String res = "MethodUse \"%s\" as \"%s:%s:%s\" in %s %s %d";
        String typpe = " method invoke";
        String result = String.format(res, typpe,useMethodVarTypeName,useMethodVarName, varName, locClass, locRange.toString(),useTree);
        return result;
    }
}
