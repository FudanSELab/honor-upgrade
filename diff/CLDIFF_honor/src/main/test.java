package main;

import edu.fdu.se.API.CLDiffAPI;
import edu.fdu.se.core.preprocessingfile.FilePairPreDiff;
import edu.fdu.se.fileutil.FileRWUtil;
import edu.fdu.se.global.Constants;
import edu.fdu.se.global.Global;
import edu.fdu.se.util.DirUtil;
import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Level;

public class test {
    public static void main(String[] args) throws IOException {
        Global.initLangAndLogger(Constants.RUNNING_LANG.C, Level.ALL);

//        String prevFilePath = "D:\\test\\prev\\Section_test.cpp";
//        String currFilePath = "D:\\test\\curr\\Section_test.cpp";
//        String outputDir = "D:\\test\\res";
        String prevFilePath = "D:\\1yanjiusheng\\baseline\\test\\diff\\prev\\load.cpp";
        String currFilePath = "D:\\1yanjiusheng\\baseline\\test\\diff\\curr\\load.cpp";
        String outputDir = "D:\\1yanjiusheng\\baseline\\test\\res\\single";

        String[] str = new String[4];
        str[0] = prevFilePath;
        str[1] = currFilePath;
        str[2] = "c";
        str[3] = outputDir;
        test(str);
//        byte[] prevBytes =  Files.readAllBytes(Paths.get(prevFilePath));
//        byte[] currBytes = Files.readAllBytes(Paths.get(currFilePath));
//        FilePairPreDiff preDiff = new FilePairPreDiff(prevBytes, currBytes);
//        preDiff.compareTwoFile();
//        System.out.println(preDiff.getPreCacheData().getEntityTree());
    }

    final static Logger logger = LoggerFactory.getLogger(test.class);
    public static void test (String[] args) {
        int argLen = args.length;
        if (argLen != 4) {
            //System.out.println("invalid args");
            logger.error("Invalid args");
            return;
        }
        //指定prev与curr文件夹地址，校验是否合法
        Global.prevDir = formatPath(args[0]);
        Global.currDir = formatPath(args[1]);

        //指定语言
        String language = args[2];
        if(language.equals("java")) {
            Global.initLangAndLogger(Constants.RUNNING_LANG.JAVA, Level.ALL);
        }
        else if (language.equals("c")) {
            Global.initLangAndLogger(Constants.RUNNING_LANG.C, Level.ALL);
        }
        else {
            logger.error("Invalid language: " + language);
            return;
        }
        //指定输出目录
        Global.outputDir = args[3];
        Global.relativeRoot = "/test";
        File outputDir = new File(Global.outputDir);
        if (outputDir.exists()) {
            FileRWUtil.removeDir(Global.outputDir);
        }
        outputDir.mkdirs();
        File outputJson = new File(Global.outputDir + File.separator + "diff");
        if (!outputJson.exists()) {
            outputJson.mkdirs();
        }
        System.out.println("输出文件路径：" + outputJson.getAbsolutePath());

        Global.result = new JSONArray();
        Global.changeEntityId = 0;
        Global.runningMode = Constants.COMMAND_LINE;
        Global.granularity = Constants.GRANULARITY.DECLARATION;
        Global.isMethodRangeContainsJavaDoc = false;
        Global.isLink = true;

        //生成map，表示 <修改状态, set<路径>>
        //done
        Map<String, Set<String>> map = coarseDiffOfDir(Global.prevDir, Global.currDir);
        //将map数据分成三类输入到属性filePairDatas
        CLDiffAPI clDiffAPI = new CLDiffAPI(Global.outputDir, map);

        clDiffAPI.generateDiffMinerOutput();
    }

    //      删除传入参数末尾的路径分隔符"\"
    private static String formatPath(String path) {
        File file = new File(path);
        if(!file.exists()){
            logger.error(path + " is an invalid path", path);
            System.exit(-1);
        }
        if (path.endsWith(File.separator) || path.endsWith("\\") || path.endsWith("/") || path.endsWith("\\\\")) {
            return path.substring(0, path.length() - 1);
        }
        return path;
    }
    /**
     *
     * 1、获取文件路径并处理
     * 2、找到pre和curr的交集和两集合交集的两个补集，分别表示修改的文件、删除的文件、新增的文件
     */
    private static Map<String,Set<String>> simpleDiff(List<File> decomPrevFiles,List<File> decomCurrFiles,
                                                      int preLength,int currLength) {
        Set<String> prevPaths = subPathOfFiles(decomPrevFiles, preLength);
        Set<String> currPaths = subPathOfFiles(decomCurrFiles, currLength);
        //获取相同的文件路径
        Set<String> intersection = new HashSet<>(prevPaths);
        intersection.retainAll(currPaths);
        //获取其他两个集合
        prevPaths.removeAll(intersection);
        currPaths.removeAll(intersection);
        Map<String, Set<String>> result = new HashMap<>();
        result.put("add", currPaths);
        result.put("delete", prevPaths);
        result.put("modify", intersection);
        return result;
    }


    private static Map<String, Set<String>> coarseDiffOfDir(String decomPrevPath, String decomCurrPath) {
        //在windows运行，会出现把转义符算作字符串长度的问题
        File fileCurr = new File(decomCurrPath);
        int lenCurr = fileCurr.getAbsolutePath().length();
        File filePrev = new File(decomPrevPath);
        int lenPrev = filePrev.getAbsolutePath().length();
        if(Objects.equals(Global.lang, Constants.RUNNING_LANG.JAVA)) {
            List<File> decomPrevFiles = DirUtil.getAllJavaFilesOfADirectory(decomPrevPath);
            List<File> decomCurrFiles = DirUtil.getAllJavaFilesOfADirectory(decomCurrPath);
            return simpleDiff(decomPrevFiles,decomCurrFiles,lenPrev, lenCurr);
        }
        else if(Objects.equals(Global.lang, Constants.RUNNING_LANG.C)) {
            List<File> decomPrevFiles = DirUtil.getAllCFileOfADirectory(decomPrevPath);
            List<File> decomCurrFiles = DirUtil.getAllCFileOfADirectory(decomCurrPath);
            return simpleDiff(decomPrevFiles,decomCurrFiles,lenPrev,lenCurr);
        }
        return null;
    }

    /**
     * 获取文件相对路径
     */
    public static Set<String> subPathOfFiles(List<File> mList, int len) {
        Set<String> s = new HashSet<>();
        for (File f : mList) {
            s.add(f.getAbsolutePath().substring(len));
        }
        return s;
    }

}
