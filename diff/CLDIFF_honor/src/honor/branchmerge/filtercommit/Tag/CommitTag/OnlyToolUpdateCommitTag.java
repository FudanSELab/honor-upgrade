package honor.branchmerge.filtercommit.Tag.CommitTag;

import java.util.List;

public class OnlyToolUpdateCommitTag extends BaseCommitTag {
    public static class TagDescription {
        public String message;
        public List<String> toolFiles;
        public TagDescription(String message, List<String> toolFiles) {
            this.message = message;
            this.toolFiles = toolFiles;
        }
    }

    public static final CommitTagType TAG_TYPE = CommitTagType.ONLY_TOOL_UPDATE;

    @Override
    public CommitTagType getTagType() {
        return TAG_TYPE;
    }
}
