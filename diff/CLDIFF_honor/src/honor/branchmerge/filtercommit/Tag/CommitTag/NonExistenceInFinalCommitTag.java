package honor.branchmerge.filtercommit.Tag.CommitTag;

import java.util.List;

public class NonExistenceInFinalCommitTag extends BaseCommitTag {
    public static class TagDescription {
        public String message;
        public List<String> changeFiles;

        public TagDescription(String message, List<String> changeFiles) {
            this.message = message;
            this.changeFiles = changeFiles;
        }
    }

    public static final CommitTagType TAG_TYPE = CommitTagType.NONEXISTENCE_IN_FINAL;

    @Override
    public CommitTagType getTagType() {
        return TAG_TYPE;
    }
}
