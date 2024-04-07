import edu.fdu.se.fileutil.JsonFileUtil;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import java.util.Iterator;

public class RQ1_Accuracy_cal {


    public static void main(String args[]){
        String path = "/Users/huangkaifeng/Library/Mobile Documents/com~apple~CloudDocs/Workspace/CLDIFF/commits-accuracy.json";
        JSONObject ja = JsonFileUtil.readJsonFileAsObject(path);

        Iterator iterator = ja.keys();
        while(iterator.hasNext()){
            int[] res = new int[8];
            String key = (String)iterator.next();
            JSONObject projCommits =  ja.getJSONObject(key);
            JSONArray jarr = projCommits.getJSONArray("commits");
            Iterator iterator2 = jarr.iterator();
            System.out.printf("%s\t\t\t\t",key);
            while(iterator2.hasNext()){
                JSONObject commit =  (JSONObject) iterator2.next();
                String stmtAcur = commit.getString("stmt-accuracy");
                String expAcur = commit.getString("exp-accuracy");
                String linkAcur = commit.getString("link-accuracy");
                String linkAcur2 = commit.getString("link-accuracy2");
                int[] d1 = toInt(stmtAcur);
                int[] d2 = toInt(expAcur);
                int[] d3 = toInt(linkAcur);
                int[] d4 = toInt(linkAcur2);
                res[0] += d1[0];
                res[1] += d1[1];
                res[2] += d2[0];
                res[3] += d2[1];
                res[4] += d3[0];
                res[5] += d3[1];
                res[6] += d4[0];
                res[7] += d4[1];
            }
            System.out.printf("%.2f ",res[0]*1.0/res[1]);
            System.out.printf("%.2f ",res[2]*1.0/res[3]);
            System.out.printf("%.2f ",res[4]*1.0/res[5]);
            System.out.printf("%.2f\n",res[6]*1.0/res[7]);
        }


    }

    public static int[] toInt(String code){
        String[] d = code.split("/");
        int[] res = new int[2];
        if(d.length ==1){
            res[0] = 0;
            res[1] = 0;
        }else{
            res[0] = valueOf(d[0].trim());
            res[1] = valueOf(d[1].trim());
        }
        return res;
    }

    public static int valueOf(String s){
        if("".equals(s)){
            return 0;
        }
        return Integer.valueOf(s);


    }
}
