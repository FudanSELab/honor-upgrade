package edu.fdu.se.global;

import edu.fdu.se.core.miningactions.bean.MiningActionData;
import edu.fdu.se.core.preprocessingfile.data.FileOutputLog;
import edu.fdu.se.lang.common.IASTNodeUtil;
import edu.fdu.se.lang.common.ILookupTbl;
import edu.fdu.se.lang.common.ProcessUtil;
import edu.fdu.se.net.Meta;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.blame.BlameResult;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.json.JSONArray;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.*;
import java.util.logging.*;

/**
 * Created by huangkaifeng on 2018/4/7.
 *
 *
 */
public class Global {

    public static String prevGitRepo;
    public static String currGitRepo;

    public static String driver;

    public static String url ;
    public static String username ;
    public static String password ;

    //args initialization
    public static String outputDir ;
    public static String relativeRoot ;
    public static String diffDbName;
    public static String repoId;
    //myflag
    public static String prevDir;
    public static String currDir;

    public static String rootPath;
    public static String task_id;

    public static Logger logger;

    /**
     * 处理的代码粒度
     */
    public static String granularity;

    public static String lang;

    public static Connection conn;

    public static ProcessUtil processUtil;
    public static IASTNodeUtil astNodeUtil;
    public static ILookupTbl iLookupTbl;


    public static List<FilePairData> filePairDatas;

    public static JSONArray result = new JSONArray();

    public static JSONArray alignResult = new JSONArray();

    public static String fileName;

    public static Git git;

    public static void initLogger(Level level) {
        logger = Logger.getLogger("default");
        logger.setLevel(level);
        logger.setUseParentHandlers(false);
        ConsoleHandler handler = new ConsoleHandler();
        handler.setFormatter(new SimpleFormatter() {
            private static final String format = "%1$s %n";
//            private static final String format = "[%1$tF %1$tT] [%2$-7s] %3$s %n";

            @Override
            public synchronized String format(LogRecord lr) {
                return String.format(format,
//                        new Date(lr.getMillis()),
//                        lr.getLevel().getName(),
                        lr.getMessage());
            }
        });
        logger.addHandler(handler);

    }

    public static void initLangAndLogger(String lang, Level level) {
        initLogger(level);
        initLang(lang);
    }

    public static void initLang(String language) {
        lang = language;
        try {
            processUtil = (ProcessUtil) Class.forName(String.format(Constants.LANG_PACKAGE_PROCESSUTIL, Global.lang.toLowerCase(), Global.lang)).newInstance();
            astNodeUtil = (IASTNodeUtil) Class.forName(String.format(Constants.LANG_PACKAGE_ASTNODEUTIL, Global.lang.toLowerCase(), Global.lang)).newInstance();
            //iLookupTbl = new edu.fdu.se.lang.c.LookupTableCDT();
            iLookupTbl = (ILookupTbl) Class.forName(String.format(Constants.LANG_PACKAGE_LOOKUPTBL, Global.lang.toLowerCase(), Global.lang)).newInstance();
                    //(ILookupTbl) Class.forName(String.format(Constants.LANG_PACKAGE_LOOKUPTBL, Global.lang.toLowerCase(), Global.lang)).newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static String repository;


    public static String formatLang(String s){
        //here
        if(s.toLowerCase().equals("c")||s.toLowerCase().equals("cpp")){
            return "C";
        }
        if(s.toLowerCase().equals("java")){
            return "Java";
        }
        assert(false);
        return null;
    }

    public static void initDb(){
        try {
            Class.forName(Global.driver).newInstance();

            String databaseName = Global.diffDbName;// 已经在MySQL数据库中创建好的数据库。
            String userName = "root";// MySQL默认的root账户名
            String password = "";// 默认的root账户密码为空


            Connection conn = DriverManager.getConnection(Global.url, Global.username, Global.password);
            Global.conn = conn;
            boolean dbExist = false;
            ResultSet resultSet = conn.getMetaData().getCatalogs();
            while (resultSet.next()) {
                String dbName = resultSet.getString(1);
                if(dbName.equals(databaseName)){
                    dbExist = true;
                    break;
                }
            }
            resultSet.close();
            if(!dbExist){
                Statement statement = conn.createStatement();
                int myResult = statement.executeUpdate(String.format("CREATE DATABASE %s",Global.diffDbName));
            }

            Statement stmt1 = conn.createStatement();
            stmt1.execute("USE "+Global.diffDbName);

            stmt1.executeUpdate("SET GLOBAL sql_mode='STRICT_TRANS_TABLES';");
            conn.close();

            Connection conn1 = DriverManager.getConnection(Global.url, Global.username, Global.password);
            Global.conn = conn1;
            Statement stmt = conn1.createStatement();
            stmt.execute("USE "+Global.diffDbName);

            String sql = "CREATE TABLE IF NOT EXISTS changed_file(id int (11)  auto_increment, task_id varchar (64) ,repo_id longtext,file_name longtext NOT NULL,lang varchar(8) NOT NULL,prev_path varchar(256),curr_path varchar(256),change_type char(16),key (id),PRIMARY KEY (`id`),\n" +
                    "  UNIQUE KEY `tid_file_name_idx` (`task_id`,repo_id(160),file_name(160))) ENGINE= INNODB DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci";
            int result = stmt.executeUpdate(sql);

            sql = "CREATE TABLE IF NOT EXISTS changed_method(id int (11) auto_increment,task_id varchar (64),repo_id varchar (220),file_id int(11)," +
                    "lang varchar(8) NOT NULL,package longtext,class varchar(128), entity longtext,prev_signature longtext,curr_signature longtext," +
                    "change_type char(16), prev_line_range varchar(64),curr_line_range varchar(64),description varchar(256),commits longtext," +
                    "PRIMARY KEY (`id`),\n" +
                    " UNIQUE KEY `tid_eid_men_idx` (`task_id`,repo_id(160),package(160),entity(160)),\n" +
                    "  KEY `FK_changed_method_file_id` (`file_id`)," +
                    "CONSTRAINT fm_id FOREIGN KEY (file_id) REFERENCES changed_file (id) on delete cascade on update cascade ) DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci";
            stmt.executeUpdate(sql);

            sql = "CREATE TABLE IF NOT EXISTS changed_field(id int (11)  auto_increment,task_id varchar (64),repo_id varchar (220),file_id int(11)," +
                    "lang varchar(8) NOT NULL,package longtext,class varchar(128), entity longtext,signature longtext,change_type char(16), " +
                    "prev_line_range varchar(64),curr_line_range varchar(64),description varchar(256), commits longtext,PRIMARY KEY (`id`),\n" +
                    "  UNIQUE KEY `tid_signature_idx` (`task_id`,repo_id(160),signature(160)),\n" +
                    "  KEY `FK_changed_field_file_id` (`file_id`)," +
                    "CONSTRAINT ff_id FOREIGN KEY (file_id) REFERENCES changed_file (id) on delete cascade on update cascade ) DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci";
            stmt.executeUpdate(sql);

            sql = "CREATE TABLE IF NOT EXISTS changed_enum(id int (11)  auto_increment,task_id varchar (64) ,repo_id varchar (220),file_id int(11)," +
                    "lang varchar(8) NOT NULL,package longtext,class varchar(128), entity longtext,signature longtext,change_type char(16), " +
                    "prev_line_range varchar(64),dcurr_line_range varchar(64),escription varchar(256),commits longtext,PRIMARY KEY (`id`),\n" +
                    "  UNIQUE KEY `task_id_signature_idx` (`task_id`,repo_id(160),signature(160)),\n" +
                    "  KEY `FK_changed_enum_file_id` (`file_id`)," +
                    "CONSTRAINT fe_id FOREIGN KEY (file_id) REFERENCES changed_file (id) on delete cascade on update cascade ) DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci";
            stmt.executeUpdate(sql);

            sql = "CREATE TABLE IF NOT EXISTS changed_initializer(id int (11)  auto_increment,task_id varchar (64),repo_id varchar (220),file_id int(11)," +
                    "lang varchar(8) NOT NULL,package longtext,class varchar(128), entity longtext,signature longtext,change_type char(16), " +
                    "prev_line_range varchar(64),curr_line_range varchar(64),description varchar(156) ,commits longtext, PRIMARY KEY (`id`),\n" +
                    "  KEY `file_id_idx` (`file_id`),\n" +
                    " UNIQUE KEY `tid_eid_men_idx` (`task_id`,signature(160),`description`,repo_id(160))," +
                    "CONSTRAINT fi_id FOREIGN KEY (file_id) REFERENCES changed_file (id) on delete cascade on update cascade ) DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci";
            stmt.executeUpdate(sql);

            sql = "CREATE TABLE IF NOT EXISTS changed_class(id int (11)  auto_increment,task_id varchar (64),repo_id varchar (220),file_id int(11)," +
                    "lang varchar(8) NOT NULL,package longtext,class varchar(128), entity longtext,signature longtext,change_type char(16), " +
                    "prev_line_range varchar(64),curr_line_range varchar(64),description varchar(256) ,commits longtext,PRIMARY KEY (`id`),\n" +
                    "  UNIQUE KEY `tid_signature_idx` (`task_id`,repo_id(160),signature(160)),\n" +
                    "  KEY `FK_changed_class_file_id` (`file_id`)," +
                    "CONSTRAINT fc_id FOREIGN KEY (file_id) REFERENCES changed_file (id) on delete cascade on update cascade ) DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci";
            stmt.executeUpdate(sql);

            sql = "CREATE TABLE IF NOT EXISTS changed_method_info(id int (11) auto_increment,task_id varchar (64),repo_id varchar (220),entity_id int(11)," +
                    "lang varchar(8) NOT NULL,modified_entity_name longtext,modification varchar (128),detail_action varchar(128), line_num varchar (32),  PRIMARY KEY (`id`),\n" +
                    "  UNIQUE KEY `entity_id_idx` (task_id,repo_id(160),entity_id,modified_entity_name(160)),\n" +
                    "  KEY `tid_eid_men_idx` (entity_id)," +
                    "CONSTRAINT method_id FOREIGN KEY (entity_id) REFERENCES changed_method (id) on delete cascade on update cascade ) DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci";
            stmt.executeUpdate(sql);

            sql = "CREATE TABLE IF NOT EXISTS changed_field_info(id int (11) auto_increment,task_id varchar (64),repo_id varchar (220),entity_id int(11)," +
                    "lang varchar(8) NOT NULL,modified_entity_name longtext,modification varchar (128),detail_action varchar(128), line_num varchar (32), PRIMARY KEY (`id`),\n" +
                    "  KEY `entity_id_idx` (`entity_id`),\n" +
                    "  UNIQUE KEY `tid_eid_men_idx` (`task_id`,repo_id(160),`entity_id`,modified_entity_name(160))," +
                    "CONSTRAINT field_id FOREIGN KEY (entity_id) REFERENCES changed_field (id) on delete cascade on update cascade ) DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci";
            stmt.executeUpdate(sql);

            sql = "CREATE TABLE IF NOT EXISTS changed_class_info(id int (11) auto_increment,task_id varchar (64),repo_id varchar (220),entity_id int(11)," +
                    "lang varchar(8) NOT NULL,modified_entity_name longtext,modification varchar (128),detail_action varchar(128), line_num varchar (32), PRIMARY KEY (`id`),\n" +
                    "  KEY `entity_id_idx` (`entity_id`),\n" +
                    "  UNIQUE KEY `tid_eid_men_idx` (`task_id`,repo_id(160),`entity_id`,modified_entity_name(160))," +
                    "CONSTRAINT class_id FOREIGN KEY (entity_id) REFERENCES changed_class (id) on delete cascade on update cascade )DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci ";
            stmt.executeUpdate(sql);

//            sql = "alter table changed_method_info ADD CONSTRAINT method_id FOREIGN KEY (entity_id) REFERENCES changed_method (id)";
//            stmt.executeUpdate(sql);
//
//            sql = "alter table changed_field_info ADD CONSTRAINT field_id FOREIGN KEY (entity_id) REFERENCES changed_field (id)";
//            stmt.executeUpdate(sql);
//
//            sql = "alter table changed_method_info ADD CONSTRAINT class_id FOREIGN KEY (entity_id) REFERENCES changed_class (id)";
//            stmt.executeUpdate(sql);

//            if (result != -1) {
//                System.out.println("创建数据表成功");
//
//                sql = "INSERT INTO person(uid,name) VALUES('1','somebody1')";
//                result = stmt.executeUpdate(sql);
//
//                sql = "INSERT INTO person(uid,name) VALUES('2','somebody2')";
//                result = stmt.executeUpdate(sql);
//
////                sql = "alter table person add primary key (uid)";
////                result = stmt.executeUpdate(sql);
//
//                sql = "SELECT * FROM person";
//
//
//                ResultSet rs = stmt.executeQuery(sql);
//                System.out.println("uid\t姓名");
//
//                while (rs.next()) {
//                    System.out.println(rs.getString(1) + "\t" + rs.getString(2));
//                }
//            }

//            conn.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static Meta mmeta;

    public static int changeEntityId = 0;

    public static int RQ2 = 0;

    public static String fileShortName;
    public static String fileFullPathName;
    public static String md5FileName;


    public static String parentCommit;
    public static String selfCommit;
    public static int round;
    public static FileOutputLog fileOutputLog;


    public static Map<Integer,String> changeEntityFileNameMap;
    /**
     * running mode
     * 0 command mode
     * 1 offline mode
     * 2 online mode
     */
    public static int runningMode;
    /**
     * input configs
     */

    public static String prevCommitID; // null in online mode
    public static String currCommitID;
    public static String projectName;
    public static String outputJson;

    /**
     * running vars
     */
    public static MiningActionData mad;


    public static boolean isMethodRangeContainsJavaDoc;

    public static boolean isLink;
    public static boolean isNotGraphJson;

    public static void f() throws Exception{
        Git git = Git.open(new File("C:\\Users\\Tristan\\Desktop\\merge-tool"));
        Iterable<RevCommit> logs = git.log().addPath("src/main/java/Main.java").call();

        for (RevCommit commit : logs) {
            // 获取当前提交的树对象
            RevTree tree = commit.getTree();

            // 使用TreeWalk遍历提交的文件
            try (TreeWalk treeWalk = new TreeWalk(git.getRepository())) {
                treeWalk.addTree(tree);
                treeWalk.setRecursive(true);
                treeWalk.setPostOrderTraversal(false);
                while (treeWalk.next()) {
                    // 获取当前文件的路径
                    String path = treeWalk.getPathString();

                    // 如果当前文件是我们要查找的文件，就查找特定行的历史记录
                    if (path.equals("src/main/java/Main.java")) {
                        ObjectId objectId = treeWalk.getObjectId(0);
                        try (ObjectReader reader = git.getRepository().newObjectReader()) {
                            // 获取当前文件的内容
                            ObjectLoader loader = reader.open(objectId);
                            ByteArrayOutputStream stream = new ByteArrayOutputStream();
                            loader.copyTo(stream);

                            // 使用字符串分隔符分隔内容的行
                            String[] lines = stream.toString().split("\\r?\\n");

                            // 检查每一行是否包含特定的内容
                            for (int i = 0; i < lines.length; i++) {
                                if (lines[i].contains("public static")) {
                                    // 如果当前行包含特定的内容，就输出提交的信息
                                    System.out.println("Commit ID: " + commit.getId().getName());
                                    System.out.println("Commit message: " + commit.getFullMessage());
                                    System.out.println("Commit author: " + commit.getAuthorIdent().getName());
                                    System.out.println("Commit date: " + commit.getAuthorIdent().getWhen());
                                }
                            }
                        }
                    }
                }
            }
        }


    }

    /**
     * 对比git提交记录，寻找与行号匹配的delete操作的ID
     * @param line1 行号
     * @param line2 行号
     * @param pre id
     * @param curr id
     * @return
     */
    public static String searchDeleteCommitId(int line1, int line2, Git git, String pre, String curr, String filePath) {
        try {
            //解析指定的字符串 pre 为对应的 Git commit ID（对象ID）
            ObjectId lastCommitIdPre = git.getRepository().resolve(pre);
            ObjectId lastCommitIdCurr = git.getRepository().resolve(curr);
            //获取指定文件在指定起始提交之后的 Git blame 信息。BlameResult 对象中包含了每行代码的作者、提交信息和行号等详细信息。
            BlameResult res = git.blame().setFilePath(filePath).setStartCommit(lastCommitIdPre).call();
            //获取指定行的代码内容
            String target = res.getResultContents().getString(line1-1,line2,true);
            //获取指定文件的 Git 日志信息，包括与该文件相关的提交记录。每个 RevCommit 对象代表一个 Git 提交记录
            //JGit 返回的提交记录日志是按照时间顺序（最新的提交在前）进行排序的，也就是逆序（倒序）。在迭代 logs 对象时会首先得到最新提交记录，然后按照时间递减的顺序获取之前的提交记录。
            Iterable<RevCommit> logs = git.log().addPath(filePath).call();
            RevCommit prev = null ;
            for (RevCommit commit : logs) {
                //对每一个log信息获取对应的result
                //fixme honoraosp error
                BlameResult result = git.blame().setStartCommit(commit.getId()).setFilePath(filePath).call();
                String s = result.getResultContents().getString(line1-1,line2,true);
                //当检查到内容一致，意味着本条log内并没有对这段进行操作，则上一个检查的log（比本条log较新的那条）是进行修改操作的。break结束查找。
                if(s.equals(target)){
                    break;
                }
                prev = commit;
            }
            return Objects.isNull(prev)?lastCommitIdCurr.getName():prev.getName();
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return "";
    }



    public static void main(String[] args) {
        try {
            Git git = Git.open(new File("C:\\Users\\Tristan\\Desktop\\merge-tool"));
            String s = searchDeleteCommitId(21,2100,git,"aaf3f7d6ecc53a4ef09b25c63f4e309a65d8c12c","HEAD","src/main/java/merge/Rep.java");
            System.out.println("-----"+s);
            //f2(11,20,Git.open(new File("C:\\Users\\Tristan\\Desktop\\merge-tool\\.git")),"src/main/java/Main.java","HEAD");
        }
        catch (Exception e){
            e.printStackTrace();
        }

    }


    public static String f2(int line1,int line2,Git git,String filePath,String currCommitID)  {
        StringBuilder res = new StringBuilder();
        Set<RevCommit> set = new HashSet<>();

        try (Repository repository = git.getRepository()) {
            ObjectId lastCommitId = repository.resolve(currCommitID);
            //ObjectId id = repository.resolve("f8c56100009e932730217cf375a5035ce1761a31");
            BlameResult result = git.blame().setFilePath(filePath).setStartCommit(lastCommitId).setTextComparator(RawTextComparator.DEFAULT).call();

            for(int i=line1-1;i <= line2-1;i++){
                try {
                    RevCommit commit = result.getSourceCommit(i);
                    set.add(commit);
                    //System.out.println(filePath  + " was last modified by " + commit.getAuthorIdent().getName() + " in commit " + commit.getName());

                }
                catch (Exception e){
                    //e.printStackTrace();
                }



            }
            for(RevCommit commit: set){
                res.append(commit.getName()).append(",");
                //System.out.println(result.getResultContents());

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return res.toString();
    }



//    public void f23(){
//        // 打开一个现有的本地仓库
//        Git git = Git.open(new File("/path/to/your/local/repo"));
//
//// 或者创建一个新的仓库
//        //Git git = Git.init().setDirectory(new File("/path/to/your/new/repo")).call();
//
//        RevCommit commit = new RevWalk(git.getRepository()).parseCommit(commitId);
//        RevCommit parent = new RevWalk(git.getRepository()).parseCommit(commit.getParent(0).getId());
//
//        DiffFormatter formatter = new DiffFormatter(DisabledOutputStream.INSTANCE);
//        formatter.setRepository(git.getRepository());
//
//        List<DiffEntry> diffs = formatter.scan(parent.getTree(), commit.getTree());
//
//
//        for (DiffEntry diff : diffs) {
//            if (diff.getChangeType() != DiffEntry.ChangeType.DELETE) {
//                ObjectId objectId = diff.getNewId().toObjectId();
//                try (ObjectReader reader = git.getRepository().newObjectReader()) {
//                    ObjectLoader loader = reader.open(objectId);
//                    byte[] bytes = loader.getBytes();
//                    String content = new String(bytes, StandardCharsets.UTF_8);
//                    System.out.println(content);
//                }
//            }
//        }
//
//    }


    public void  f4() throws Exception{
        // 1. 打开 Git 仓库
        Git git = Git.open(new File("/path/to/your/local/repo"));

// 2. 获取指定提交 ID 的 ObjectId
        ObjectId commitId = git.getRepository().resolve("commitId");

// 3. 创建一个 TreeWalk，遍历该提交的所有文件
        TreeWalk treeWalk = new TreeWalk(git.getRepository());
        treeWalk.addTree(commitId);
        treeWalk.setRecursive(true);

        while (treeWalk.next()) {
            // 获取文件内容并输出
            ObjectId objectId = treeWalk.getObjectId(0);
            try (ObjectReader reader = git.getRepository().newObjectReader()) {
                ObjectLoader loader = reader.open(objectId);
                byte[] bytes = loader.getBytes();
                String content = new String(bytes, StandardCharsets.UTF_8);
                System.out.println("File path: " + treeWalk.getPathString());
                System.out.println("File content: ");
                System.out.println(content);
            }
        }

    }



}
