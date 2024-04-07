package honor.branchmerge.filtercommit.Tag.CommitTag;

import org.eclipse.jgit.lib.ObjectId;

public abstract class BaseCommitTag {
    public static final CommitTagType TAG_TYPE = CommitTagType.BASE;
    private ObjectId commitId;
    private String description;

    /**
     * 使用 abstract method 实现 java 中的静态域多态。
     * @return 那个派生类对应的 CommitType
     */
    public abstract CommitTagType getTagType();

    public void setCommitId(ObjectId commitId) {
        this.commitId = commitId;
    }

    public ObjectId getCommitId() {
        return commitId;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 用于输出到最终 json 文件中 tagPropertyMap 的 key 的生成
     * 同时该 key 作为 commitIdTagKeyListPairs 的 tagKeys 当中的元素
     */
    public String getOutputKey() {
        String tagType = CommitTagType.getStringType(this.getTagType());
        return tagType + "_" + this.commitId.getName();
    }
}
