import edu.fdu.se.API.CLDiffLocal;
import edu.fdu.se.fileutil.JsonFileUtil;
import edu.fdu.se.global.Constants;
import edu.fdu.se.global.Global;
import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.checkerframework.checker.signature.qual.SignatureUnknown;

import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

@SuppressWarnings("Duplicates")
public class RQ1_Accuracy {


    public static void main(String args[]){
        String rootDir = "/Users/huangkaifeng/iCloud/Workspace/CLDIFF-Extend";
        String outputDir = rootDir+"/evaluation-rq1";
        JSONObject jsonObject = JsonFileUtil.readJsonFileAsObject("/Users/huangkaifeng/iCloud/Workspace/CLDIFF/commits-accuracy.json");
        Iterator iter = jsonObject.keys();
        int index = 0;
        while(iter.hasNext()){
            String proj = (String)iter.next();
            System.out.println(proj);
            String repo = rootDir+"/projects/"+proj+"/.git";
            if(index < 19){
                index++;
                continue;
            }
//          spring-framework
            index++;
            System.err.println("--------------------------------------------------------"+proj);
            JSONArray jarr = jsonObject.getJSONObject(proj).getJSONArray("commits");
            Global.runningMode = Constants.COMMAND_LINE;
            Global.granularity = Constants.GRANULARITY.STATEMENT;
            int COMMIT_INDEX = 8;
            Global.initLangAndLogger(Constants.RUNNING_LANG.JAVA, Level.ALL);
            Global.isMethodRangeContainsJavaDoc = false;
            Global.isLink = true;
            Global.isNotGraphJson = true;
            CLDiffLocal clDiffLocal = new CLDiffLocal(repo);
            for(int i =0;i<jarr.size();i++){
                JSONObject localObj = jarr.getJSONObject(i);
                String commitId = localObj.getString("commit");
                System.err.println("--------------------------------------------------------"+commitId);
                //
                if(i<COMMIT_INDEX){
                    continue;
                }
                clDiffLocal.run(commitId, repo, outputDir);
                break;

            }
            break;

        }

    }


}
