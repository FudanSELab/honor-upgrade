package honor.branchmerge.filtercommit.CommitTagger;

import honor.branchmerge.filtercommit.Tag.CommitTag.BaseCommitTag;
import honor.branchmerge.filtercommit.TaggedRevCommit;
import org.eclipse.jgit.revwalk.RevCommit;

import java.util.List;

public interface IAttachTag<T extends BaseCommitTag> {

    /**
     * 判断一个 commit 是否有资格获得一个 tag
     * @param commit
     * @return
     */
    boolean isCommitNeedTag(RevCommit commit);

    /**
     * 通过一个给定的 tagCommit，获得其对应的 tag，
     * 这里输出的 tag 应该是内部所有 property 都被装填好的
     * @param commit
     * @return
     */
    T produceCommitTag(RevCommit commit);

    /**
     * 给一个 TaggedRevCommit 列表中的每一个 RevCommit 增加 Tag，
     * 是否应该增加该 Tag 的规则 isCommitHasTag 是由子类定义的。
     * @param tagCommits
     */
    default void addTagForCommits(List<TaggedRevCommit> tagCommits) {
        int counter = 0;
        for (TaggedRevCommit taggedRevCommit : tagCommits) {
            counter++;
            if (this.isCommitNeedTag(taggedRevCommit.getCommit())) {
                T tag = this.produceCommitTag(taggedRevCommit.getCommit());
                this.addTagForSingleCommit(taggedRevCommit, tag);
            }
        }
    }

    /**
     * 给一个 TaggedRevCommit，添加对应的 TAG_ATTACHED，
     * TAG_ATTACHED 是子类中重写的。
     * @param tagCommit
     */
    default void addTagForSingleCommit(TaggedRevCommit tagCommit, T tag) {
        if (!tagCommit.hasTagType(tag)) {
            tagCommit.addTag(tag);
        }
    }
}
