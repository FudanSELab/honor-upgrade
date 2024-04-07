package edu.fdu.se.core.miningchangeentity.base;


import edu.fdu.se.global.Global;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by huangkaifeng on 2018/3/28.
 */
public class FrontData {

    private int changeEntityId;

    private String key;

    public String getFile() {
        return file;
    }

    private String file;

    private String range;

    private String type1;
    private String type2;

    private String displayDesc;

    private List<SubRange> subRange;

    private JSONArray opt2Exp2;

    public Object getMethodFrom() {
        return methodFrom;
    }

    public void setMethodFrom(Object methodFrom) {
        this.methodFrom = methodFrom;
    }


    public Object getMethodTo() {
        return methodTo;
    }

    public void setMethodTo(Object methodTo) {
        this.methodTo = methodTo;
    }

    private Object methodFrom;

    private Object methodTo;

    public FrontData() {

    }

    class SubRange {
        String file2;
        String subRangeCode;
        String subType;
    }

    private void addRangeResultList(Integer[] range, Object cu, String file, String type) {
        if (subRange == null) {
            subRange = new ArrayList<>();
        }
        int startLine = Global.astNodeUtil.getLineNumber(cu, range[0]);
        int startColumn = Global.astNodeUtil.getLineNumber(cu, range[0]);
        int endLine = Global.astNodeUtil.getLineNumber(cu, range[1]);
        int endColumn = Global.astNodeUtil.getLineNumber(cu, range[1]);

        List<Integer> endColumnList = new ArrayList<>();
        int i = 1;
        while (startLine + i <= endLine) {
            int lineNum = startLine + i;
            int pos = Global.astNodeUtil.getPositionFromLine(cu, lineNum);
            pos--;
            endColumnList.add(pos);
            i++;
        }
        for (int j = startLine, m = 0; j < endLine; j++, m++) {
            int pos = endColumnList.get(m);
            if (j == startLine) {
                SubRange subRangeItem = new SubRange();
                subRangeItem.file2 = file;
                subRangeItem.subType = type;
                subRangeItem.subRangeCode = startLine + "," + startColumn + "," + pos;
                subRange.add(subRangeItem);
            } else {
                SubRange subRangeItem = new SubRange();
                subRangeItem.file2 = file;
                subRangeItem.subType = type;
                subRangeItem.subRangeCode = startLine + "," + 0 + "," + pos;
                subRange.add(subRangeItem);
            }
        }
        int startColumn2 = endLine > startLine ? 0 : startColumn;
        SubRange subRangeItem = new SubRange();
        subRangeItem.file2 = file;
        subRangeItem.subType = type;
        subRangeItem.subRangeCode = endLine + "," + startColumn2 + "," + endColumn;
        subRange.add(subRangeItem);
    }


    private void addRangesResultList(List<Integer[]> ranges, Object cu, String file, String type) {
        for (Integer[] range : ranges) {
            addRangeResultList(range, cu, file, type);
        }
    }

    public void addInsertList(List<Integer[]> ranges, Object cu) {
        String file = ChangeEntityDesc.StageIIIFile.DST;
        String type = "insert";
        addRangesResultList(ranges, cu, file, type);
    }

    public void addUpdateList(List<Integer[]> ranges, Object cu) {
        String file = ChangeEntityDesc.StageIIIFile.SRC;
        String type = "update";
        addRangesResultList(ranges, cu, file, type);
    }

    public void addDeleteList(List<Integer[]> ranges, Object cu) {
        String file = ChangeEntityDesc.StageIIIFile.SRC;
        String type = "delete";
        addRangesResultList(ranges, cu, file, type);
    }

    public void addMoveListSrc(Integer[] range, Object cu) {
        String file = ChangeEntityDesc.StageIIIFile.SRC;
        String type = "move";
        addRangeResultList(range, cu, file, type);
    }

    public void addMoveListDst(Integer[] range, Object cu) {
        String file = ChangeEntityDesc.StageIIIFile.DST;
        String type = "move";
        addRangeResultList(range, cu, file, type);
    }

    public String getDisplayDesc() {
        return this.displayDesc;
    }


    public void setDisplayDesc(String displayDesc) {
        this.displayDesc = displayDesc;
    }


    public void setChangeEntityId(int changeEntityId) {
        this.changeEntityId = changeEntityId;
    }


    public void setKey(String key) {
        this.key = key;
    }


    public void setFile(String file) {
        this.file = file;
    }


    public void setRange(String range) {
        this.range = range;
    }

    public void setOpt2Exp2(JSONArray opt2Exp2) {
        this.opt2Exp2 = opt2Exp2;
    }

    public void setType1(String type) {
        this.type1 = type;
    }

    public void setType2(String type) {
        this.type2 = type;
    }

    @Override
    public String toString() {
        String format = String.format("%d. %s\n", changeEntityId, displayDesc);
        StringBuilder sb = new StringBuilder();
        if (this.opt2Exp2 != null) {
            for (Object s : this.opt2Exp2) {
                sb.append("\t" + s + "\n");
            }
        }
        sb.append("\t" + range);
        sb.append("\n");
        String result = format + sb.toString();
        return result;
    }

    public JSONObject genJSONObject() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(ChangeEntityDesc.StageIIIKeys.KEYY, key);
        jsonObject.put(ChangeEntityDesc.StageIIIKeys.FILE, file);
        jsonObject.put(ChangeEntityDesc.StageIIIKeys.RANGE, range);
        jsonObject.put(ChangeEntityDesc.StageIIIKeys.TYPE1, type1);
        jsonObject.put(ChangeEntityDesc.StageIIIKeys.TYPE2, type2);
        if(methodFrom != null){
            displayDesc = "update from : "+ methodFrom;
        }
        if(methodTo != null){
            displayDesc = "update to : " + methodTo;
        }
        jsonObject.put(ChangeEntityDesc.StageIIIKeys.DESCRIPTION, displayDesc);
        jsonObject.put(ChangeEntityDesc.StageIIIKeys.ID, changeEntityId);
        if (subRange != null) {
            JSONArray jsonArray1 = new JSONArray();
            for (SubRange subRange1 : subRange) {
                JSONObject jsonObject1 = new JSONObject();
                jsonObject1.put(ChangeEntityDesc.StageIIIKeys.FILE, subRange1.file2);
                jsonObject1.put(ChangeEntityDesc.StageIIIKeys.SUB_RANGE_CODE, subRange1.subRangeCode);
                jsonObject1.put(ChangeEntityDesc.StageIIIKeys.SUB_TYPE, subRange1.subType);
                jsonArray1.put(jsonObject1);
            }
            jsonObject.put(ChangeEntityDesc.StageIIIKeys.SUB_RANGE, jsonArray1);
        }
        if (this.opt2Exp2 != null) {
            jsonObject.put(ChangeEntityDesc.StageIIIKeys.OPT2EXP2, this.opt2Exp2);
        }
        return jsonObject;
    }

    public String getRange() {
        return range;
    }
}
