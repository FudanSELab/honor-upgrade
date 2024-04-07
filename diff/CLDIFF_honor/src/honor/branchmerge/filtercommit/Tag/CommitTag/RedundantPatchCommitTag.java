package honor.branchmerge.filtercommit.Tag.CommitTag;

import honor.branchmerge.filtercommit.util.TwoRevCommit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RedundantPatchCommitTag extends BaseCommitTag {

    public static class TagDescription {
        public String message;
        public Map<String, List<String>> diffs;
        public TagDescription(String message, Map<TwoRevCommit, List<TwoRevCommit>> diffMap) {
            this.message = message;
            this.diffs = new HashMap<>();

            for (Map.Entry<TwoRevCommit, List<TwoRevCommit>> entry : diffMap.entrySet()) {
                List<String> redundants = new ArrayList<>();
                List<TwoRevCommit> diffs = entry.getValue();
                for (TwoRevCommit diff : diffs) {
                    String value = diff.parent.getId().getName()
                            + " -> "
                            + diff.current.getId().getName();
                    redundants.add(value);
                }

                String key = entry.getKey().current.getId().getName()
                        + " -> "
                        + entry.getKey().parent.getId().getName();
                this.diffs.put(key, redundants);
            }
        }
    }

    public static final CommitTagType TAG_TYPE = CommitTagType.REDUNDANT_PATCH;
    // 表示与 “被该 tag 标记的 commit” 拥有相同 diff 的 commits。
    // 而且要表明前后两个 commit，以表示 diff
    private Map<TwoRevCommit, List<TwoRevCommit>> redundantCommits;

    public void setRedundantCommits(Map<TwoRevCommit, List<TwoRevCommit>> redundantCommits) {
        this.redundantCommits = redundantCommits;
    }

    public Map<TwoRevCommit, List<TwoRevCommit>> getRedundantCommits() {
        return redundantCommits;
    }

    @Override
    public CommitTagType getTagType() {
        return TAG_TYPE;
    }
}
