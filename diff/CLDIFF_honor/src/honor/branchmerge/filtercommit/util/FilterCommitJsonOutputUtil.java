package honor.branchmerge.filtercommit.util;

import com.google.gson.*;
import honor.branchmerge.filtercommit.Tag.CommitTag.*;
import honor.branchmerge.filtercommit.TaggedRevCommit;
import org.eclipse.jgit.revwalk.RevCommit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FilterCommitJsonOutputUtil {
    public static class JsonOutput {
        public class CommitIdTagKeyListPair {
            public String commitId;
            public List<String> tagKeys;
            public CommitIdTagKeyListPair(String commitId, List<BaseCommitTag> tags) {
                this.commitId = commitId;
                this.tagKeys = new ArrayList<>();
                for (BaseCommitTag tag : tags) {
                    this.tagKeys.add(tag.getOutputKey());
                }
            }
        }

        public class TagKeyTagProperty {
            public String tagType;
            public Map<String, String> properties;
            public TagKeyTagProperty(BaseCommitTag tag) {
                this.tagType = CommitTagType.getStringType(tag.getTagType());
                this.properties = new HashMap<>();
                FilterCommitJsonOutputUtil.loadTagProperties(properties, tag);
            }
        }

        List<CommitIdTagKeyListPair> commitIdTagKeyListPairs;
        // key is tagKey in CommitIdTagKeyListPair
        Map<String, TagKeyTagProperty> tagPropertyMap;
        public JsonOutput(List<TaggedRevCommit> taggedRevCommits) {
            this.commitIdTagKeyListPairs = new ArrayList<>();
            this.tagPropertyMap = new HashMap<>();
            for (TaggedRevCommit taggedRevCommit : taggedRevCommits) {
                String commitId = taggedRevCommit.getCommit().getId().getName();
                List<BaseCommitTag> tags = taggedRevCommit.getTags();
                CommitIdTagKeyListPair pair = new CommitIdTagKeyListPair(commitId, tags);
                this.commitIdTagKeyListPairs.add(pair);

                for (BaseCommitTag tag : tags) {
                    TagKeyTagProperty property = new TagKeyTagProperty(tag);
                    tagPropertyMap.put(tag.getOutputKey(), property);
                }
            }
        }
    }

    public static String genOutputJson(List<TaggedRevCommit> taggedRevCommits) {
        Gson gson = new Gson();
        JsonOutput json = new JsonOutput(taggedRevCommits);
        String result = gson.toJson(json);
        return FilterCommitJsonOutputUtil.formatJson(result);
    }

    public static void loadTagProperties(Map<String, String> properties, BaseCommitTag tag) {
        if (tag instanceof RevertPatchCommitTag) {
            RevertPatchCommitTag subTag = (RevertPatchCommitTag) tag;
            List<RevCommit> revertList = subTag.getCommitRevertList();

            String revertListStr = "|";
            Integer commitPositionInRevertList = 0;
            for (int i = 0; i < revertList.size(); i++) {
                RevCommit revertCommit = revertList.get(i);
                if (tag.getCommitId().getName().equals(revertCommit.getId().getName())) {
                    // 看当前 commit 位于 revertList 中的第几个
                    commitPositionInRevertList = i;
                }
                revertListStr += revertCommit.getId().getName() + "|";
            }
            properties.put("revertList", revertListStr);
            properties.put("index", commitPositionInRevertList.toString());
            properties.put("originCommit", "right");

            if (revertList.size() > commitPositionInRevertList + 1) {
                RevCommit nextCommit = revertList.get(commitPositionInRevertList + 1);
                properties.put("next", nextCommit.getId().getName());
            }
        } else if (tag instanceof RedundantPatchCommitTag) {
            RedundantPatchCommitTag subTag = (RedundantPatchCommitTag) tag;
            String redundants = "|";
            Map<TwoRevCommit, List<TwoRevCommit>> redundantCommits = subTag.getRedundantCommits();
            for (Map.Entry<TwoRevCommit, List<TwoRevCommit>> entry : redundantCommits.entrySet()) {
                List<TwoRevCommit> values = entry.getValue();
                for (TwoRevCommit pair : values) {
                    redundants += pair.current.getId().getName() + "|";
                }
            }
            properties.put("redundants", redundants);
        }
        properties.put("description", tag.getDescription());
    }

    public static String formatJson(String jsonStr) {
        Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
        String result = "";
        JsonElement jElement = new JsonParser().parse(jsonStr);
        if (jElement instanceof JsonObject) {
            JsonObject jsonObject = jElement.getAsJsonObject();
            result = gson.toJson(jsonObject);
        } else if (jElement instanceof JsonArray) {
            JsonArray jsonArray = jElement.getAsJsonArray();
            result = gson.toJson(jsonArray);
        }
        return result;
    }
}
