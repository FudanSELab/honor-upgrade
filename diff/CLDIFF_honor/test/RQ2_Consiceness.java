import edu.fdu.se.fileutil.JsonFileUtil;
import edu.fdu.se.global.Constants;
import edu.fdu.se.global.Global;
import net.sf.json.JSONObject;

import java.util.Iterator;
import java.util.logging.Level;

public class RQ2_Consiceness {


    public static void main(String args[]) {
        String rootDir = "/Users/huangkaifeng/Library/Mobile Documents/com~apple~CloudDocs/Workspace/CLDIFF-Extend";
        String outputDir = rootDir + "/evaluation-all";
        JSONObject jsonObject = JsonFileUtil.readJsonFileAsObject(rootDir + "/commits.json");
        Iterator iter = jsonObject.keys();
        while (iter.hasNext()) {
            String proj = (String) iter.next();
            String repo = rootDir + "/projects/" + proj + "/.git";
            System.out.println(proj);
            Global.runningMode = Constants.COMMAND_LINE;
            Global.granularity = Constants.GRANULARITY.STATEMENT;
            Global.initLangAndLogger(Constants.RUNNING_LANG.JAVA, Level.ALL);
            Global.isMethodRangeContainsJavaDoc = false;
            Global.isLink = true;
            CLDiffAllCommit clDiffLocal = new CLDiffAllCommit(repo);
            clDiffLocal.runAll(repo, outputDir);
        }
    }
}
