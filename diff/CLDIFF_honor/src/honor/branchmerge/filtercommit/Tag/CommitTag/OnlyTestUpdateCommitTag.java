package honor.branchmerge.filtercommit.Tag.CommitTag;

import java.util.List;

public class OnlyTestUpdateCommitTag extends BaseCommitTag {
    public static class TagDescription {
        public String message;
        public List<String> testFiles;
        public TagDescription(String message, List<String> testFiles) {
            this.message = message;
            this.testFiles = testFiles;
        }
    }

    public static final CommitTagType TAG_TYPE = CommitTagType.ONLY_TEST_UPDATE;

    @Override
    public CommitTagType getTagType() {
        return TAG_TYPE;
    }
}
