package honor.branchmerge.filtercommit.Tag.CommitTag;

public class OnlyCommentUpdateCommitTag extends BaseCommitTag {
    public static class TagDescription {
        public String message;
        public TagDescription(String message) {
            this.message = message;
        }
    }

    public static final CommitTagType TAG_TYPE = CommitTagType.ONLY_COMMENT_UPDATE;

    @Override
    public CommitTagType getTagType() {
        return TAG_TYPE;
    }
}
