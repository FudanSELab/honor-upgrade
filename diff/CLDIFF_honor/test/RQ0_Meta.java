import edu.fdu.se.fileutil.JsonFileUtil;
import net.sf.json.JSONObject;

import java.util.Iterator;

public class RQ0_Meta {

    public static void main(String args[]){
//         creation date, loc , commits
        JSONObject jo = JsonFileUtil.readJsonFileAsObject("/Users/huangkaifeng/Desktop/Workspace/CLDIFF-Extend/evaluationdata.json");
        Iterator iter = jo.keys();
        while(iter.hasNext()){
            String k = (String) iter.next();
            System.out.println(k);

        }

    }
}
