package honor.branchmerge.filtercommit.Config;

import honor.branchmerge.filtercommit.Tag.CommitTag.OnlyCommentUpdateCommitTag;

import java.util.List;

public class FilterConfig {
    public NodeConfig begin;
    public NodeConfig end;
    public OnlyTestUpdatePluginConfig only_test_update;
    public NonExistenceInFinalPluginConfig non_existence_in_final;
    public OnlyProjectUnrelatedFileUpdatePluginConfig only_project_unrelated_file_update;
    public OnlyToolUpdatePluginConfig only_tool_update;
    public RedundantPatchPluginConfig redundant_patch;
    public RevertPatchPluginConfig revert_patch;
    public OnlyJavadocUpdatePluginConfig only_javadoc_update;
    public OnlyCommentUpdateCommitTag only_comment_update;

    public static class NodeConfig {
        public static final String FILTER_NODE_TAG_TYPE = "tag";
        public static final String FILTER_NODE_BRANCH_TYPE = "branch";
        public static final String FILTER_NODE_COMMIT_TYPE = "commit";
        public String type;
        public String value;
    }

    public static class PluginConfig {
    }

    public static class OnlyTestUpdatePluginConfig extends PluginConfig {
        public String pattern;
    }

    public static class NonExistenceInFinalPluginConfig extends PluginConfig {
    }

    public static class OnlyProjectUnrelatedFileUpdatePluginConfig extends PluginConfig {
        public List<String> binaryFileSuffix;
        public List<String> textFileSuffix;
        public List<String> unrelatedTextFile;
    }

    public static class OnlyToolUpdatePluginConfig extends PluginConfig {
        public String pattern;
    }

    public static class RedundantPatchPluginConfig extends PluginConfig {
    }

    public static class RevertPatchPluginConfig extends PluginConfig {
    }

    public static class OnlyJavadocUpdatePluginConfig extends PluginConfig {
    }

    public static class OnlyCommentUpdatePluginConfig extends PluginConfig {
    }
}
