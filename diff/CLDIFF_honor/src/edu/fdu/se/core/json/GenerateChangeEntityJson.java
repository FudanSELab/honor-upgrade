package edu.fdu.se.core.json;

//import com.alibaba.fastjson.JSONObject;
import com.github.gumtreediff.actions.model.*;
import com.github.gumtreediff.tree.Tree;
import edu.fdu.se.core.miningchangeentity.member.*;
import edu.fdu.se.global.Constants;
import edu.fdu.se.core.miningactions.bean.MiningActionData;
import edu.fdu.se.core.miningchangeentity.base.ChangeEntity;
//import edu.fdu.se.util.JSONObject;
import edu.fdu.se.util.GetInfo;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.json.JSONArray;

import org.json.JSONObject;

import java.util.*;

/**
 * Created by huangkaifeng on 2018/4/11.
 */
public class GenerateChangeEntityJson {

    private BeanSetter beanSetter;

    /**
     * 根据输入的 granularity 是什么粒度来决定 beanSetter 是什么 Level,
     * 目前粒度分类有：TYPE DECLARATION STATEMENT EXPRESSION 四种
     * @param granularity 粒度
     */
    public GenerateChangeEntityJson(String granularity) {
        switch (granularity) {
            case Constants.GRANULARITY.TYPE:
                beanSetter = new TypeLevelBeanSetter();
                break;
            case Constants.GRANULARITY.DECLARATION:
                beanSetter = new BodyLevelBeanSetter();
                break;
            case Constants.GRANULARITY.STATEMENT:
                beanSetter = new StmtLevelBeanSetter();
                break;
            case Constants.GRANULARITY.EXPRESSION:
                beanSetter = new ExpsLevelBeanSetter();
                break;
            default:
                break;
        }
    }

    /**
     * 对 changeEntityData 进行处理，再存回来
     * @param changeEntityData
     */
    public void setStageIIIBean(MiningActionData changeEntityData) {
        beanSetter.setChangeEntityOpt(changeEntityData);
        beanSetter.setChangeEntitySubRange(changeEntityData);
    }




    public static int[] maxminLineNumber(List<Integer[]> mList, CompilationUnit cu) {
        int max = Integer.MIN_VALUE;
        int min = Integer.MAX_VALUE;
        for (Integer[] tmp : mList) {
            if (tmp[0] < min) {
                min = tmp[0];
            }
            if (tmp[1] > max) {
                max = tmp[1];
            }
        }
        int a = cu.getLineNumber(min);
        int b = cu.getLineNumber(max);
        return new int[]{a, b};
    }

    public static String toConsoleString(JSONArray jsonArray) {
        StringBuffer sb = new StringBuffer();
        sb.append("Concise Code Differences:\n");
        for (Object o : jsonArray) {
            JSONObject jo = (JSONObject) o;
            sb.append(jo.get("id"));
            sb.append(". ");
            sb.append(jo.get("description"));

            sb.append("\n");
//            if (jo.has("opt2-exp2")) {
//                JSONArray arr = (JSONArray) jo.get("opt2-exp2");
//                Iterator iter2 = arr.iterator();
//                while (iter2.hasNext()) {
//                    String s = (String) iter2.next();
//                    sb.append("\t" + s);
//                    sb.append("\n");
//                }
//            }
            sb.append("\t" + jo.get("range"));
            sb.append("\n");
        }
        sb.append("\n");
        return sb.toString();
    }


}
