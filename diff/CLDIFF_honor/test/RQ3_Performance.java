import edu.fdu.se.fileutil.JsonFileUtil;
import net.sf.json.JSONObject;

import java.util.Iterator;

public class RQ3_Performance {

    public static void main(String args[]){
        String rootDir = "/Users/huangkaifeng/Library/Mobile Documents/com~apple~CloudDocs/Workspace/CLDIFF-Extend";
        JSONObject jsonObject = JsonFileUtil.readJsonFileAsObject(rootDir+"/commits.json");
        Iterator iter = jsonObject.keys();
        while(iter.hasNext()){
            String proj = (String)iter.next();
            String projRepo = rootDir+"/projects/"+proj+"/.git";
            System.out.println(projRepo);
        }
    }
}
