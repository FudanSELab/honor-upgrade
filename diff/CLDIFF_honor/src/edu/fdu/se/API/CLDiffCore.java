package edu.fdu.se.API;

import edu.fdu.se.global.Constants;
import edu.fdu.se.global.Global;
import edu.fdu.se.core.generatingactions.ActionsMap;
import edu.fdu.se.core.generatingactions.GumTreeDiffParser;
import edu.fdu.se.core.generatingactions.MyActionGenerator;
import edu.fdu.se.core.miningactions.generator.ActionAggregationGenerator;
import edu.fdu.se.core.miningactions.bean.MiningActionData;
import edu.fdu.se.core.miningchangeentity.generator.ChangeEntityPreprocess;
import edu.fdu.se.core.preprocessingfile.FilePairPreDiff;
import edu.fdu.se.core.preprocessingfile.data.PreCacheData;
import edu.fdu.se.core.preprocessingfile.data.PreCacheTmpData;
import edu.fdu.se.core.json.GenerateChangeEntityJson;
import edu.fdu.se.lang.common.generatingactions.ParserTreeGenerator;
//import edu.fdu.se.util.JSONObject;
import edu.fdu.se.util.GetInfo;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Method;
import java.util.logging.Logger;

/**
 * Created by huangkaifeng on 2018/2/27.
 */
public class CLDiffCore {

    public MiningActionData mad;

    /**
     * filter out non-java files or test files
     *
     * @param filePathName
     * @return true: non-java files or test files, false:java files
     */
    public static boolean isFilter(String filePathName) {
        String name = filePathName.toLowerCase();
        //System.out.println(name);
        if (!name.endsWith(".java") && !name.endsWith(".cpp") && !name.endsWith(".c")) {
            return true;
        }
        if (name.contains("\\test\\") || name.contains("/test/")) {
            return true;
        }
        String[] data = filePathName.split("/");
        String fileName = data[data.length - 1];
        if (filePathName.endsWith("Test.java") || fileName.startsWith("Test") || filePathName.endsWith("Tests.java")) {
            return true;
        }
        return false;
    }

    public JSONObject dooDiff(String fileName, Object prev, Object curr, String output) {
        //生成AST，FilePairPreDiff 的 initFile 会生成 cu，放到 PreCacheData 的 currCu 和 preCu 中去。
        FilePairPreDiff preDiff = new FilePairPreDiff(prev, curr);
        System.out.println("dodiff: " + fileName);
        int result = preDiff.compareTwoFile();
        if (result == -1) {
            return null;
        }
        return runDiff(preDiff, fileName);
    }

    /**
     * 处理单个文件的情况
     */
    void dooSingleFile(String fileName, byte[] fileContent, String output, String addOrDelete) {
        Object obj = null;
        try {
            //动态加载对应语言的类，获得相关方法，并通过类的实例化来使用方法，将返回值赋给obj
            Class<?> printClass = Class.forName(Constants.SINGLEFILECHANGETYPE);
            String methodName = String.format(Constants.SINGLEFILECHANGETYPEMETHOD, Global.lang);
            Method printMethod = printClass.getMethod(methodName, byte[].class, String.class, String.class);
            obj = printMethod.invoke(printClass.newInstance(), fileContent, fileName, addOrDelete);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (obj != null) {
            this.mad = (MiningActionData) obj;
        }
    }

    private ParserTreeGenerator genTree(PreCacheData preData) {
        ParserTreeGenerator parserTreeGenerator = null;
        try {
            Class clazz = Class.forName(String.  format(Constants.LANG_PACKAGE_TREEGENERATOR, Global.lang.toLowerCase(), Global.lang));
            Class[] argClazz = {PreCacheData.class};
            Object[] obj = {preData};
            parserTreeGenerator = (ParserTreeGenerator) clazz.getConstructor(argClazz).newInstance(obj);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return parserTreeGenerator;
    }

    /**
     * main
     */
    private JSONObject runDiff(FilePairPreDiff preDiff, String fileName) {

        PreCacheData preData = preDiff.getPreCacheData();
//        PreCacheTmpData preTempData = preDiff.getPreCacheTmpData();
//        Global.processUtil.preProcess(preTempData);
        ParserTreeGenerator parserTreeGenerator = genTree(preData);
        parserTreeGenerator.setFileName(fileName);
        MyActionGenerator actionGenerator = new MyActionGenerator(parserTreeGenerator);
        ActionsMap actionsData = actionGenerator.generate();
        //printActions(actionsData, parserTreeGenerator);
        //cluster
        this.mad = new MiningActionData(preData, actionsData, parserTreeGenerator);
        Global.mad = this.mad;
        ActionAggregationGenerator aag = new ActionAggregationGenerator();
        aag.doCluster(mad);
        //data process
        this.mad.fileFullPackageName = fileName;
        ChangeEntityPreprocess cep = new ChangeEntityPreprocess(mad);
        cep.preprocessChangeEntity();
        return jsonOutput(this.mad);
    }

    private JSONObject jsonOutput(MiningActionData mad) {
        GenerateChangeEntityJson generate = new GenerateChangeEntityJson(Global.granularity);
        generate.setStageIIIBean(mad);
        JSONObject json = GetInfo.generateEntityJson(mad);
        return json;
    }
}
