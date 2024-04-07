package honor.branchmerge.filtercommit.CommitTagger;

import honor.branchmerge.filtercommit.Config.FilterConfig;
import honor.branchmerge.filtercommit.util.JGitHelperPlus;

public class BaseCommitTagger {
    public JGitHelperPlus cmd;
    public FilterConfig config;
    public BaseCommitTagger(JGitHelperPlus cmd, FilterConfig config) {
        this.cmd = cmd;
        this.config = config;
    }
}
