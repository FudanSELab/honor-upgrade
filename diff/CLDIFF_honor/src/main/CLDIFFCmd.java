package main;

import edu.fdu.se.global.Constants;
import edu.fdu.se.global.Global;
import edu.fdu.se.API.CLDiffLocal;
import org.apache.commons.io.FileUtils;
import org.eclipse.cdt.core.dom.ast.IASTNode;

import java.io.File;
import java.util.logging.Level;


/**
 * Created by huangkaifeng on 2018/10/11.
 */
public class CLDIFFCmd {

    public static void main(String[] args) throws Exception {
        Global.runningMode = Constants.COMMAND_LINE;
        Global.granularity = Constants.GRANULARITY.DECLARATION;
        Global.initLangAndLogger(Constants.RUNNING_LANG.JAVA, Level.ALL);
        Global.isMethodRangeContainsJavaDoc = false;
        Global.isLink = true;


//        String repo = "C:\\Users\\Administrator\\Desktop\\test\\aosp12_frameworks\\base\\.git";
//        String commitId = "34a1b9c951c38537ab96b69bc308f6e0884823f5";
//        String commitId2 = "10239a0c9c8cce2f6e6b62944b7f4eea63c8156d";
//        String testId = "acdbd498702b8a7810f19d49e0c16d71f5061880";

//        String repo = "C:\\Users\\Administrator\\Desktop\\1\\testcase\\.git";
//        //String commitId = "35403879b29ece3dca3cbbae9c91ad8c8000621f";
//        String commitId = "8295272c38738aee9752f458ff673f39580de9eb";
//        String commitId2 = "b5aadd6c8ba2f15f8597463f2b386378815d3c2d";


//        String repo = "C:\\Users\\Administrator\\Desktop\\test\\aosp12_frameworks\\base\\.git";
//        String tagHigh = "android-12.0.0_r1";
//        String tagLow = "android-11.0.0_r1";
//        String id = "c6360dababa92a0c09afb3b9569c99a652a2e6cd";
//
//
//
//        String configPath = "C:\\Users\\Administrator\\Desktop\\filtercommit.config.json";
//        String outputDir = "C:\\Users\\Administrator\\Desktop\\result";
        String repo = args[0];
        String tagHigh = args[1];
        String tagLow = args[2];
        String outputDir = args[3];
        //String configPath = args[4];
        String tmpName = repo.substring(0,repo.indexOf(".git")-1);
        String repoName = tmpName.substring(tmpName.lastIndexOf('/'),tmpName.length());
        Global.outputDir = outputDir;
        Global.projectName = repoName;
        CLDiffLocal clDiffLocal = new CLDiffLocal(repo);
        clDiffLocal.run(tagHigh,tagLow, repo, outputDir);
        //clDiffLocal.run(id,repo,outputDir);
    }




}
