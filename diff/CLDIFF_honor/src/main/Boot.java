package main;

import edu.fdu.se.API.CLDiffAPI;
import edu.fdu.se.fileutil.FileRWUtil;
import edu.fdu.se.git.IHandleCommit;
import edu.fdu.se.global.Constants;
import edu.fdu.se.global.Global;
import edu.fdu.se.util.DirUtil;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.lib.Repository;
import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.*;
import java.util.Date;
import java.util.logging.Level;

public class Boot extends IHandleCommit {
    final static Logger logger = LoggerFactory.getLogger(Boot.class);
    public static Properties initProp(String configFilePath){
        try {
            // 1.通过当前类获取类加载器
            ClassLoader classLoader = Boot.class.getClassLoader();
            // 2.通过类加载器的方法获得一个输入流
            //InputStream in = classLoader.getResourceAsStream(configFilePath);
            InputStream in =  new FileInputStream(configFilePath);
            // 3.创建一个properties对象
            Properties props = new Properties();
            // 4.加载输入流
            props.load(in);
            // 5.获取相关参数的值
            return props;

        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static int jxJson(Properties map) throws Exception {
        // 创建Statement用于执行SQL语句
        Statement stmt = null;
        String strSQL = "";
        Connection connection = null;

        //定义同步数据的条数
        int count = 0;

        try {
            Class.forName(map.getProperty("driverClassName"));
            connection = DriverManager.getConnection(map.getProperty("url"), map.getProperty("username"), map.getProperty("password"));
        }
        catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }

        // 先关闭Statement
        if (stmt != null)
            try {
                stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        // 后关闭Connection
        if (connection != null)
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        System.out.println("当前时间===" + new Date());
        System.out.println("同步结束");
        System.out.println("共更新了" + count + "条数据");
        return count;
    }


    public static void main(String[] args) {
        int argLen = args.length;
        if (argLen != 4) {
            //System.out.println("invalid args");
            logger.error("Invalid args");
            return;
        }
//        Global.task_id = args[0];
//        Global.repoId = args[1];
//        Properties props = initProp("config.properties");
//        Global.prevCommitID = args[2];
//        Global.currCommitID = args[3];
        //jxJson(props);
//        Global.driver = props.getProperty("driverClassName");
//        Global.url = props.getProperty("url");
//        Global.username = props.getProperty("username");
//        Global.password = props.getProperty("password");
        //args initialization
//        Global.outputDir = props.getProperty("outputDir");
//        Global.relativeRoot = props.getProperty("relativeRoot");
//        Global.diffDbName = props.getProperty("diffDbName");
//        Global.initDb();
        Global.relativeRoot = "/test";
        //指定prev与curr文件夹地址，校验是否合法
        Global.prevDir = formatPath(args[0]);
        Global.currDir = formatPath(args[1]);

        //尝试获取git信息
        addGit();

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
        Global.outputDir = formatPath(args[3]);
        File outputDir = new File(Global.outputDir);
        if (outputDir.exists()) {
            FileRWUtil.removeDir(Global.outputDir);
        }
        outputDir.mkdirs();
        File outputJson = new File(Global.outputDir + File.separator + "/diff");
        if (!outputJson.exists()) {
            outputJson.mkdirs();
        }
        logger.info("输出文件路径：" + outputJson.getAbsolutePath());

//        String language = args[6];
//        String prePath = formatPath(args[4]);
//        String currPath = formatPath(args[5]);
//        if(currPath.endsWith("/") || currPath.endsWith("\\")){
//            Global.currGitRepo = currPath + ".git";
//        }
//        else {
//            Global.currGitRepo = currPath+"/.git";
//        }
//
//        try {
//            Global.git = Git.open(new File(Global.currGitRepo));
//        }
//        catch (Exception e){
//            e.printStackTrace();
//        }


//        Global.prevDir = prePath ;
//        Global.currDir = currPath ;
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

//        // result判空，非空才输出
//        if (Global.result.length() != 0 && Global.result != null) {
//            StringBuilder sb = new StringBuilder();
//            sb.append(File.separator);
//            String outputFileName =  "diff_result.json";
//            String outputFileName1 =  "align_result.json";
//            String outputPath = Global.outputDir + File.separator + "diff" + File.separator + outputFileName;
//            String outputAlignPath = Global.outputDir + File.separator + "align" + File.separator + outputFileName1;
//            File outputFileParent =new File(Global.outputDir+File.separator + "diff") ;
//            File outputFile = new File(outputPath);
//            File outputAlignFile = new File(outputAlignPath);
//            if (!outputFile.exists()) {
//            } else {
//                outputPath = sb.toString() + numFile + ".json";
//            }
//            if (!outputAlignFile.exists()) {
//            } else {
//                outputAlignPath = sb.toString() + numFile + ".json";
//            }
//
//            System.out.println(Global.prevDir + " -- " + Global.currDir + "   --> ." + outputFileName);
//            FileRWUtil.writeInAll(outputPath, Global.result.toString(4));
//            FileRWUtil.writeInAll(outputAlignPath, Global.alignResult.toString(4));
//        } else {
//            System.out.println(Global.prevDir + " -- " + Global.currDir + "   --> no diff");
//        }
    }

    //通过prev和curr地址获取.git信息
    public static void addGit(){
        Global.currGitRepo = Global.currDir + File.separator + ".git";
        Global.prevGitRepo = Global.prevGitRepo + File.separator + ".git";
        try{
            Global.git = Git.open(new File(Global.currGitRepo));
            //获取最新的commitId
            Global.currCommitID = Global.git.log().call().iterator().next().getId().getName();
            Git git = Git.open(new File(Global.prevGitRepo));
            Global.prevCommitID = git.log().call().iterator().next().getId().getName();
        } catch (IOException e) {
            System.out.println("没有对应的git信息");
            //后续git相关操作会先判断git是否为空，为空就不做了
            Global.git = null;
        } catch (NoHeadException e) {
            e.printStackTrace();
        } catch (GitAPIException e) {
            e.printStackTrace();
        }

    }

    private static Set<String> generateSubDir(String prePath, String currPath, int subDirLevel) {
//                判断prePath和currPath是否为特殊字符
        List<String> childPre = new ArrayList<>();
        List<String> childCurr = new ArrayList<>();

//        根据拆分level，获取目录
        if (!isSpecChar(prePath)) {
            childPre = getSubDirByLevel(new File(prePath), subDirLevel);
        }
        if (!isSpecChar(currPath)) {
            childCurr = getSubDirByLevel(new File(currPath), subDirLevel);
        }

//            pre有curr没有的是delete，pre没有curr有的是add，pre有curr有的是共同的 是需要遍历的
//            Map<String, Set<String>> subDirAll = coarseDirList(childPre,childCurr,prePath,currPath);
//            获取所有子路径
        return coarseDirSet(childPre, childCurr, prePath, currPath);
    }

    private static boolean isSpecChar(String prePath) {
        return Objects.equals(prePath, "-");
    }

    //      删除传入参数末尾的路径分隔符"\"
    private static String formatPath(String path) {
        File file = new File(path);
        if(!file.exists()){
            logger.error(path + " is an invalid path", path);
            System.exit(-1);
        }
        path = file.getAbsolutePath();
        if (path.endsWith(File.separator) || path.endsWith("\\") || path.endsWith("/") || path.endsWith("\\\\")) {
            return path.substring(0, path.length() - 1);
        }
        return path;
    }

    private static List<String> getSubDirByLevel(File filePath, int subDirLevel) {
        List<String> filePathList = new ArrayList<>();
        if (subDirLevel == 0) {
            filePathList.add(filePath.getAbsolutePath());
        } else {
            filePathList.addAll(getFileList(filePath, subDirLevel));
        }
        return filePathList;
    }

    private static List<String> getFileList(File filePath, int subDirLevel) {
        List<String> filePathList = new ArrayList<>();
//        File[] children = orderByName(filePath.listFiles());
        File[] children = filePath.listFiles();


        String outputFilterStr = Global.outputDir + File.separator + "/diff" + File.separator + "exclude.txt";
        File outputFilter = new File(outputFilterStr);
        if (outputFilter.exists()) outputFilter.delete();
        try {
            outputFilter.createNewFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

//        children判空
        if (children != null) {
            for (File childFile : children) {
                if (childFile.isFile() && childFile.getAbsolutePath().endsWith(".java")) {
                    filePathList.add(childFile.getAbsolutePath());
                } else if (childFile.isDirectory()) {
//                    过滤test和.git文件夹
                    String tmpPath = childFile.getAbsolutePath();
                    if (tmpPath.endsWith(".git") || tmpPath.endsWith("test") || tmpPath.endsWith("tests")) {
                        String excludeStr = "exclude dir: " + tmpPath + "\n";
                        FileRWUtil.writeInAll(outputFilterStr, excludeStr, true);
                        continue;
                    }

                    if (subDirLevel > 1) {
                        filePathList.addAll(getFileList(childFile, subDirLevel - 1));
                    } else {
                        filePathList.add(tmpPath);
                    }
                }
            }
        }
        return filePathList;
    }


    private static Set<String> coarseDirSet(List<String> decomPrevPath, List<String> decomCurrPath,
                                            String prePath, String currPath) {

//        截取path
        Set<String> prevPaths = subPathOfDirs(decomPrevPath, prePath);
        Set<String> currPaths = subPathOfDirs(decomCurrPath, currPath);
        Set<String> intersection = new HashSet<>();
        intersection.addAll(prevPaths);
        intersection.retainAll(currPaths);
        prevPaths.removeAll(intersection);
        currPaths.removeAll(intersection);
        Set<String> result = new HashSet<>();
        result.addAll(currPaths);
        result.addAll(prevPaths);
        result.addAll(intersection);

//        删除out目录
        return result;
    }

    public static Set<String> subPathOfDirs(List<String> mList, String path) {
        int len = path.length();
//        String separator = "/|//|\\\\";
//        if (!path.endsWith(File.separator)) {
//            len += +1;
//        }
        Set<String> s = new HashSet<>();
        for (String m : mList) {
            s.add(m.substring(len));
        }
        return s;
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
