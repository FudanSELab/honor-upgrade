package honor.branchmerge.filtercommit.Tag.CommitTag;

public class OnlyJavadocUpdateCommitTag extends BaseCommitTag {
    public static class TagDescription {
        public String message;
        public TagDescription(String message) {
            this.message = message;
        }
    }

    public static final CommitTagType TAG_TYPE = CommitTagType.ONLY_JAVADOC_UPDATE;

    @Override
    public CommitTagType getTagType() {
        return TAG_TYPE;
    }
}
