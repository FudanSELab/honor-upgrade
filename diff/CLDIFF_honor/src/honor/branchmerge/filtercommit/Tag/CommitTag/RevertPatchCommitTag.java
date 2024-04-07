package honor.branchmerge.filtercommit.Tag.CommitTag;

import org.eclipse.jgit.revwalk.RevCommit;

import java.util.ArrayList;
import java.util.List;

public class RevertPatchCommitTag extends BaseCommitTag {
    public static class TagDescription {
        public String message;
        public List<String> revertCommitIdList;
        public TagDescription(String message, List<RevCommit> revertCommitList) {
            this.message = message;
            this.revertCommitIdList = new ArrayList<>();
            for (RevCommit commit : revertCommitList) {
                this.revertCommitIdList.add(commit.getId().getName());
            }
        }
    }

    public static final CommitTagType TAG_TYPE = CommitTagType.REVERT_PATCH;
    // 表示被打了该标签的 commit revert 掉的 commit 的链表。
    private List<RevCommit> commitRevertList;

    public void setCommitRevertList(List<RevCommit> commitReverted) {
        this.commitRevertList = commitReverted;
    }

    public List<RevCommit> getCommitRevertList() {
        return commitRevertList;
    }

    @Override
    public CommitTagType getTagType() {
        return TAG_TYPE;
    }
}
