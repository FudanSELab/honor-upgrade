package honor.branchmerge.filtercommit.CommitTagger;

import com.google.gson.Gson;
import honor.branchmerge.filtercommit.Config.FilterConfig;
import honor.branchmerge.filtercommit.Tag.CommitTag.OnlyToolUpdateCommitTag;
import honor.branchmerge.filtercommit.util.JGitHelperPlus;
import org.eclipse.jgit.revwalk.RevCommit;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OnlyToolUpdateCommitTagger extends BaseCommitTagger
        implements IAttachTag<OnlyToolUpdateCommitTag> {
    public static int tagProducedNum = 0;

    public OnlyToolUpdateCommitTagger(JGitHelperPlus cmd, FilterConfig config) {
        super(cmd, config);
        System.out.println("OnlyToolUpdateCommitTagger has set up.");
    }

    @Override
    public OnlyToolUpdateCommitTag produceCommitTag(RevCommit commit) {
        OnlyToolUpdateCommitTag tag = new OnlyToolUpdateCommitTag();
        Gson gson = new Gson();
        List<String> changeFiles = this.cmd.getChangeFilesByCommit(commit);

        OnlyToolUpdateCommitTag.TagDescription description =
                new OnlyToolUpdateCommitTag.TagDescription(commit.getShortMessage(), changeFiles);

        tag.setCommitId(commit.getId());
        tag.setDescription(gson.toJson(description));
        tagProducedNum++;
        return tag;
    }

    @Override
    public boolean isCommitNeedTag(RevCommit commit) {
        if (commit.getParents().length != 1) {
            // TODO merge 节点不提供任何 tag
            return false;
        }

        List<String> changeFiles = this.cmd.getChangeFilesByCommit(commit);
        if (changeFiles.size() == 0 || changeFiles.size() > 10000) {
            return false;
        }
        for (String filePath : changeFiles) {
            if (!this.isToolFile(filePath)) {
                return false;
            }
        }
        return true;
    }

    private boolean isToolFile(String filePath) {
        String pattern = this.config.only_tool_update.pattern;
        Pattern p = Pattern.compile(pattern);
        Matcher m = p.matcher(filePath);
        return m.find();
    }
}
