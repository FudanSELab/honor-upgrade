package honor.branchmerge.filtercommit.Tag.CommitTag;

public enum CommitTagType {
    BASE(1),
    ONLY_TEST_UPDATE(2),
    ONLY_TOOL_UPDATE(4),
    ONLY_PROJECT_UNRELATED_UPDATE(8),
    REVERT_PATCH(16),
    UNCHANGED_AST(32),
    ONLY_LOG_UPDATE(64),
    REDUNDANT_PATCH(128),
    NONEXISTENCE_IN_FINAL(256),
    ONLY_JAVADOC_UPDATE(512),
    ONLY_COMMENT_UPDATE(1024);

    private int commitTagVal;

    CommitTagType(int i) {
        this.commitTagVal = i;
    }

    public int v() {
        return commitTagVal;
    }

    public static String getStringType(CommitTagType tagType) {
        switch (tagType) {
            case ONLY_TEST_UPDATE:
                return "ONLY_TEST_UPDATE";
            case ONLY_TOOL_UPDATE:
                return "ONLY_TOOL_UPDATE";
            case ONLY_PROJECT_UNRELATED_UPDATE:
                return "ONLY_PROJECT_UNRELATED_UPDATE";
            case REVERT_PATCH:
                return "REVERT_PATCH";
            case UNCHANGED_AST:
                return "UNCHANGED_AST";
            case ONLY_LOG_UPDATE:
                return "ONLY_LOG_UPDATE";
            case REDUNDANT_PATCH:
                return "REDUNDANT_PATCH";
            case NONEXISTENCE_IN_FINAL:
                return "NONEXISTENCE_IN_FINAL";
            case ONLY_JAVADOC_UPDATE:
                return "ONLY_JAVADOC_UPDATE";
            case ONLY_COMMENT_UPDATE:
                return "ONLY_COMMENT_UPDATE";
            default:
                return "BASE";
        }
    }
}
