import java.io.File;
import java.util.Map;
import java.util.logging.Level;

import com.google.gson.Gson;
import edu.fdu.se.API.CLDiffAPI;
import edu.fdu.se.core.generatingactions.SimpleActionPrinter;
import edu.fdu.se.core.miningactions.bean.MiningActionData;
import edu.fdu.se.fileutil.ResponseTextToFiles;
import edu.fdu.se.global.Constants;
import edu.fdu.se.global.Global;
import edu.fdu.se.core.preprocessingfile.data.FileOutputLog;
import edu.fdu.se.API.CLDiffCore;
import edu.fdu.se.net.Meta;


/**
 * Created by huangkaifeng on 2018/2/27.
 *
 */
public class CLDiffFilePairTest extends CLDiffCore {

    /**
     * 使用修改简化之后的流程，测试单个文件的功能
     */
    private void runSingleFilePair(String file1,String file2,String outputDir) {
        Global.fileOutputLog = new FileOutputLog(outputDir, "testproject");
        Global.fileOutputLog.setCommitId("commitid");
        Global.parentCommit = Constants.PARENTCOMMITNULL;
        File file = new File(file1);
        String[] s = file.getName().split("\\.");
        Global.initLang(Global.formatLang(s[s.length - 1]));
        Global.fileShortName = file.getName();
        Global.fileFullPathName = file.getAbsolutePath();
        dooDiffFile(file.getName(), file1, file2, outputDir);
//        Global.logger.info("Acrtions:");
//        if (mad.mActionsMap != null) {
//            SimpleActionPrinter.printMyActions(this.mad.mActionsMap.getAllActions());
//        }
    }

    /**
     * 使用修改简化之后的流程，测试多个文件的功能
     */
    private void runBatchTest() {
        String outputDir = "/Users/huangkaifeng/Desktop/Workspace/output";
        Global.fileOutputLog = new FileOutputLog(outputDir, "testproject");
        Global.fileOutputLog.setCommitId("commitid");
        Global.parentCommit = Constants.PARENTCOMMITNULL;
        String batchTestFilePath = "/Users/huangkaifeng/Desktop/Workspace/CLDIFFTest/test-cases";
        File currdir = new File(batchTestFilePath + "/" + Constants.SaveFilePath.CURR);
        File[] files = currdir.listFiles();
        try {
            for (int i = 0; i < files.length; i++) {
                File currf1 = files[i];
                String prevFile = batchTestFilePath + "/" + Constants.SaveFilePath.PREV + "/" + currf1.getName();

                Global.fileShortName = currf1.getName();
                Global.fileFullPathName = currf1.getAbsolutePath();
                if (!"TestMain.java".equals(currf1.getName())) {
                    continue;
                    //DeleteLabelStmt DeleteFieldType
                }
                Global.logger.info(i + " " + currf1.getName());
                dooDiffFile(currf1.getName(), prevFile, currf1.getAbsolutePath(), outputDir);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) {
        Global.isMethodRangeContainsJavaDoc = false;
        Global.granularity = Constants.GRANULARITY.STATEMENT;
        Global.runningMode = Constants.COMMAND_LINE;
        Global.isLink = true;
        Global.initLangAndLogger(Constants.RUNNING_LANG.JAVA, Level.ALL);
        CLDiffFilePairTest i = new CLDiffFilePairTest();

//        i.runGumTree(null,null);
        i.runSingleFilePair("/Users/huangkaifeng/iCloud/Workspace/CLDIFF-Extend/CLDIFFTest/test-cases/prev/eb3d8460_DBUtils.java","/Users/huangkaifeng/iCloud/Workspace/CLDIFF-Extend/CLDIFFTest/test-cases/curr/eb3d8460_DBUtils.java","/Users/huangkaifeng/Desktop/output");
//        i.runBatchTest();
//        i.testChangeSets();
    }


    public void testChangeSets(){
        try {
            String root = "/Users/huangkaifeng/iCloud/Workspace/CLDIFF-Extend/CLDIFFTest/test-commit/dee88d931a34a390626f13ed975d0bcf5c761045";
            String metaStr = ResponseTextToFiles.read(root+"/meta.json");
            Meta meta = new Gson().fromJson(metaStr, Meta.class);
            String outputDir = "/Users/huangkaifeng/iCloud/Workspace/CLDIFF-Extend/CLDIFFTest";
            CLDiffAPI clDiffAPI = new CLDiffAPI(outputDir, meta);
            clDiffAPI.generateDiffMinerOutput();
            clDiffAPI.printer(true,true,true,true);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

}
