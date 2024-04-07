package edu.fdu.se.core.links.generator;

import edu.fdu.se.core.links.linkbean.Link;
import edu.fdu.se.core.miningactions.bean.MiningActionData;
import edu.fdu.se.core.miningactions.bean.MyRange;
import edu.fdu.se.core.miningchangeentity.base.ChangeEntity;
import edu.fdu.se.core.miningchangeentity.base.ChangeEntityDesc;
import edu.fdu.se.global.Constants;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

/**
 * Created by huangkaifeng on 2018/4/16.
 */
public class LinkGraph {
    /**
     * core graph
     */
    private List<ChangeEntity> nodes;
    private List<Link> edges;

    /**
     * change entity -> List<Link>
     */
    private Map<ChangeEntity, List<Link>> edgeTbl;
    /**fx
     * file id-> file name
     */
    private Map<Integer, String> fileFullNameMap;
    /**
     * file id -> List entity id
     */
    private Map<Integer, List<Integer>> fileNameEntityIdList;
    /**
     * id+SPLITTER+ID   or ID
     */
    private Map<String, List<Link>> fileNameLinkMap;

    public LinkGraph() {
        nodes = new ArrayList<>();
        edges = new ArrayList<>();
        fileNameEntityIdList = new HashMap<>();
        fileNameLinkMap = new HashMap<>();
        edgeTbl = new HashMap<>();
        fileFullNameMap = new HashMap<>();
    }

    public void initNodes(List<ChangeEntity> newNodes, String fileName) {
        nodes.addAll(newNodes);
        List<Integer> integerList = new ArrayList<>();
        for (ChangeEntity ce : newNodes) {
            integerList.add(ce.getChangeEntityId());
        }
        int size = fileFullNameMap.size();
        fileFullNameMap.put(size, fileName);
        fileNameEntityIdList.put(size, integerList);
    }

    public ChangeEntity getChangeEntityById(int id) {
        for (ChangeEntity node : nodes) {
            if (node.changeEntityId == id) {
                return node;
            }
        }
        return null;
    }

    public String getFileNameByIndex(int index) {
        return fileFullNameMap.get(index);
    }

    /**
     * @param id change entity id
     * @return
     */
    public String getFileNameByChangeEntityId(int id) {
        for (Map.Entry<Integer, List<Integer>> entry : fileNameEntityIdList.entrySet()) {
            String fileName = getFileNameByIndex(entry.getKey());
            List<Integer> list = entry.getValue();
            for (Integer i : list) {
                if (i.intValue() == id) {
                    return fileName;
                }
            }
        }
        return null;
    }

    public void addEntry(List<Link> links) {
        for (Link link : links) {
            addEntry(link);
        }
    }

    private boolean checkLinkDuplicates(Link link) {
        for(Link e:edges){
            if(link.c1.getChangeEntityId() == e.c1.getChangeEntityId() && link.c2.getChangeEntityId() == e.c2.getChangeEntityId()) {
                if (e.desc.equals(link.desc)) {
                    return true;
                }
            }
        }
        return false;
    }

    public void addEntry(Link link) {
        if (link != null) {
            if (checkLinkDuplicates(link)) {
                return;
            }
            edges.add(link);
            if (!edgeTbl.containsKey(link.c1)) {
                edgeTbl.put(link.c1, new ArrayList<>());
            }
            edgeTbl.get(link.c1).add(link);

            if (!edgeTbl.containsKey(link.c2)) {
                edgeTbl.put(link.c2, new ArrayList<>());
            }
            edgeTbl.get(link.c2).add(link);
            int[] fileId = this.getFileIdsOfLink(link);
            if (fileId.length == 2) {
                if (!this.fileNameLinkMap.containsKey(fileId[0] + Constants.TOOLSPLITTER + fileId[1])) {
                    this.fileNameLinkMap.put(fileId[0] + Constants.TOOLSPLITTER + fileId[1], new ArrayList<>());
                }
                this.fileNameLinkMap.get(fileId[0] + Constants.TOOLSPLITTER + fileId[1]).add(link);
            } else {
                if (!this.fileNameLinkMap.containsKey(String.valueOf(fileId[0]))) {
                    this.fileNameLinkMap.put(String.valueOf(fileId[0]), new ArrayList<>());
                }
                this.fileNameLinkMap.get(String.valueOf(fileId[0])).add(link);

            }

        }
    }



    public String linkToString(Link link) {
        String result = link.c1.getChangeEntityId() + ". " + link.c1.getClass().getSimpleName()
                + " -> " + link.c2.getChangeEntityId() + ". " + link.c2.getClass().getSimpleName() + " : " + link.type;
        result += " in " + getFileNameByChangeEntityId(link.c1.getChangeEntityId());
        return result;

    }

    public JSONObject linkJsonString(Link link) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(LinkConstants.FROM, link.c2.getChangeEntityId());
        jsonObject.put(LinkConstants.TO, link.c1.getChangeEntityId());
        jsonObject.put(LinkConstants.DESC, link.desc);
        return jsonObject;
    }


    public String toConsoleString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Links:\n");
        int i = 0;
        for (Link as : this.edges) {
            sb.append(i + ". ");
            i++;
            sb.append(as.toString());
            sb.append("\n");
        }
        sb.append("\nLink size: "+this.edges.size());
        return sb.toString();
    }

    /**
     *
     *{
     *     "nodes":[
     *          "id":"",
     *          "desc":"",
     *          "code":"",
     *          "file_name":""
     *              ],
     *     "links":[
     *          "id_from":id,
     *          "id_to":id,
     *          "link_type":22,
     *          "link_type_str":""
     *          ],
     *     "..."
     *}
     * @return
     */
    public String toGraphJSON(Map<String, MiningActionData> fileNameMadMap) {
        JSONObject totalJson = new JSONObject();
        JSONArray jsonNodes = new JSONArray();
        JSONArray jsonLinks = new JSONArray();
        totalJson.put("nodes", jsonNodes);
        totalJson.put("links", jsonLinks);

        for (ChangeEntity ce : nodes) {
            JSONObject jo = new JSONObject();
            jo.put("id", ce.getChangeEntityId());
            jo.put("desc", ce.getFrontData().getDisplayDesc());
            String fileName = getFileNameByChangeEntityId(ce.getChangeEntityId());
            int fileId = getFileIdByChangeEntityId(ce.getChangeEntityId());
            MiningActionData mad = fileNameMadMap.get(fileName);
            jo.put("file_name", fileName);
            jo.put("group", fileId);
            int treeType;

            MyRange myRange = ce.lineRange;

            if (ce.getStageIIBean().getOpt().equals(ChangeEntityDesc.StageIIOpt.OPT_DELETE)) {
                treeType = ChangeEntityDesc.TreeType.PREV_TREE_NODE;
            } else if (ce.getStageIIBean().getOpt().equals(ChangeEntityDesc.StageIIOpt.OPT_INSERT)) {
                treeType = ChangeEntityDesc.TreeType.CURR_TREE_NODE;
            } else {
                String currRange = ce.getFrontData().getRange().split("-")[1];
                myRange = new MyRange(currRange);
                treeType = ChangeEntityDesc.TreeType.CURR_TREE_NODE;
            }
            String codeStr = mad.preCacheData.getSoureCodeFromRange(myRange, treeType);
            jo.put("code", codeStr);
            jsonNodes.put(jo);
        }
        for (Link li : edges) {
            JSONObject jo = new JSONObject();
            jo.put("source", li.c1.changeEntityId);
            jo.put("target", li.c2.changeEntityId);
            jo.put("type", li.type);
            jo.put("text",LinkConstants.getLinkDescString(li.type));
            jo.put("value",1);
            jo.put("link_type_str", LinkConstants.getLinkDescString(li.type));
            jsonLinks.put(jo);

        }
        return totalJson.toString(4);
    }

    private int[] getFileIdsOfLink(Link link) {
        int a = link.c1.changeEntityId;
        int b = link.c2.changeEntityId;
        int nameA = getFileIdByChangeEntityId(a);
        int nameB = getFileIdByChangeEntityId(b);
        int[] res;
        if (nameA == nameB) {
            res = new int[]{nameA};
            return res;
        }
        res = new int[]{nameA, nameB};
        return res;

    }

    private Integer getFileIdByChangeEntityId(int id) {
        for (Map.Entry<Integer, List<Integer>> entry : this.fileNameEntityIdList.entrySet()) {
            int fileId = entry.getKey();
            List<Integer> li = entry.getValue();
            for (Integer i : li) {
                if (i == id) {
                    return fileId;
                }
            }
        }
        return null;
    }



    public String toJsonString() {
        JSONObject jsonObject = new JSONObject();
        JSONArray ja2 = new JSONArray();
        for (Map.Entry<String, List<Link>> entry : this.fileNameLinkMap.entrySet()) {
            JSONObject jo2 = new JSONObject();
            String key = entry.getKey();
            if (key.contains(Constants.TOOLSPLITTER)) {
                jo2.put(LinkConstants.LINK_TYPE, LinkConstants.LINK_TYPE_2);
                String[] data = key.split(Constants.TOOLSPLITTER);
                int a = Integer.valueOf(data[0]);
                int b = Integer.valueOf(data[1]);
                String fileNameA = getFileNameByIndex(a);
                String fileNameB = getFileNameByIndex(b);
                String[] data21 = fileNameA.split(Constants.TOOLSPLITTER);
                String[] data22 = fileNameB.split(Constants.TOOLSPLITTER);
                jo2.put(LinkConstants.FILE_NAME_A, data21[1]);
                jo2.put(LinkConstants.FILE_NAME_B, data22[1]);
                jo2.put(LinkConstants.PARENT_COMMIT_A, data21[0]);
                jo2.put(LinkConstants.PARENT_COMMIT_B, data22[0]);
            } else {
                int a = Integer.valueOf(key);
                String fileNameA = getFileNameByIndex(a);
                jo2.put(LinkConstants.LINK_TYPE, LinkConstants.LINK_TYPE_1);
                String[] data2 = fileNameA.split(Constants.TOOLSPLITTER);
                jo2.put(LinkConstants.FILE_NAME_A, data2[1]);
                jo2.put(LinkConstants.PARENT_COMMIT_A, data2[0]);
            }
            List<Link> links = entry.getValue();
            JSONArray linkArr = new JSONArray();
            for (Link as : links) {
                linkArr.put(linkJsonString(as));
            }
            jo2.put("links", linkArr);
            ja2.put(jo2);
        }
        jsonObject.put("links", ja2);
        return jsonObject.toString(4);
    }

    private String getShortFileName(String fileName) {
        int index = fileName.lastIndexOf("/");
        return fileName.substring(index + 1);
    }

    private String linkTypeToDotProp(int linkType) {
        String res;
        switch (linkType) {
            case LinkConstants.LINK_DEF_USE:
                res = "[color=black]";
                break;
            case LinkConstants.LINK_ABSTRACT_METHOD:
            case LinkConstants.LINK_IMPLEMENTING_INTERFACE:
            case LinkConstants.LINK_OVERRIDE_METHOD:
                res = "[color=green]";
                break;
            case LinkConstants.LINK_SYSTEMATIC:
                res = "[color=red]";
                break;
            default:
                res = "[color=blue]";
                break;
        }
        return res;
    }

    public String toDotString() {
        String dotG = "digraph \"\" {\n" +
                "  node [shape=box];\n";
        int i = 0;
        for (Map.Entry<Integer, List<Integer>> entry : this.fileNameEntityIdList.entrySet()) {
            String subG = "subgraph cluster%d {\nlabel=\"%s\"\n%s\n }\n";
            String fileName = getFileNameByIndex(entry.getKey());
            StringBuilder nodesStr = new StringBuilder();
            for (Integer entityId : entry.getValue()) {
                ChangeEntity currCe = getChangeEntityById(entityId);
                if (!edgeTbl.containsKey(currCe)) {
                    continue;
                }
                String node = String.format("\"%d\" [label=\"%s\",fontcolor=black];\n", entityId, currCe.getFrontData().getDisplayDesc());
                nodesStr.append(node);
            }
            for (Map.Entry<String, List<Link>> entry2 : this.fileNameLinkMap.entrySet()) {
                String key = entry2.getKey();
                if (!key.contains(Constants.TOOLSPLITTER)) {
                    int a = Integer.valueOf(key);
                    String fileNameA = getFileNameByIndex(a);
                    if (!fileNameA.equals(fileName)) {
                        continue;
                    }
                    String[] data2 = fileNameA.split(Constants.TOOLSPLITTER);
                    List<Link> links = entry2.getValue();
                    StringBuilder linkStr = new StringBuilder();
                    for (Link li : links) {
                        String res = linkTypeToDotProp(li.type);
                        String node = String.format("\"%d\"->\"%d\" %s;\n", li.c1.changeEntityId, li.c2.changeEntityId, res);
                        linkStr.append(node);
                    }
                    if (linkStr.length() != 0) {
                        nodesStr.append(linkStr.toString());
                    }
                }
            }
            String subGString = String.format(subG, i, getShortFileName(fileName), nodesStr.toString());
            i++;
            dotG = dotG + subGString;
        }
        StringBuilder outLink = new StringBuilder();
        for (Map.Entry<String, List<Link>> entry3 : this.fileNameLinkMap.entrySet()) {
            String key = entry3.getKey();
            if (key.contains(Constants.TOOLSPLITTER)) {
                List<Link> links = entry3.getValue();
                for (Link li : links) {
                    String res = linkTypeToDotProp(li.type);
                    String node = String.format("\"%d\"->\"%d\" %s;\n", li.c1.changeEntityId, li.c2.changeEntityId, res);
                    outLink.append(node);
                }
            }
        }
        dotG += outLink.toString();
        dotG += "\n}";
        return dotG;
    }


}
