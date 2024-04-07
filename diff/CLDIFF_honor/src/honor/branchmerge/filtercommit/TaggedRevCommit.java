package honor.branchmerge.filtercommit;

import honor.branchmerge.filtercommit.Tag.CommitTag.BaseCommitTag;
import honor.branchmerge.filtercommit.Tag.CommitTag.CommitTagType;
import org.eclipse.jgit.revwalk.RevCommit;

import java.util.ArrayList;
import java.util.List;

public class TaggedRevCommit {
    private RevCommit commit;
    private List<BaseCommitTag> tags;

    public TaggedRevCommit(RevCommit commit) {
        this.commit = commit;
        this.tags = new ArrayList<>();
    }

    public TaggedRevCommit(RevCommit commit, List<BaseCommitTag> tags) {
        this.commit = commit;
        this.tags = tags;
    }

    public RevCommit getCommit() {
        return this.commit;
    }

    public List<BaseCommitTag> getTags() {
        return this.tags;
    }

    /**
     * 往 tagged revision commit 里面添加一个新的 tag
     * @param tag
     * @return true-说明添加成功；false-说明 tags 中已存在 tag
     */
    public boolean addTag(BaseCommitTag tag) {
        return this.tags.add(tag);
    }

    public boolean hasTagType(BaseCommitTag tag) {
        return this.hasTagType(tag.getTagType());
    }

    public boolean hasTagType(CommitTagType tagType) {
        for (BaseCommitTag tag : tags) {
            CommitTagType t = tag.getTagType();
            if (t == tagType) {
                return true;
            }
        }
        return false;
    }


    @Override
    public String toString() {
        String commitStr = this.commit.toString();
        String tagsStr = "";
        for (BaseCommitTag tag : this.tags) {
            tagsStr += tag.toString();
        }
        return commitStr + ": " + tagsStr;
    }
}
