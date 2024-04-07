package honor.branchmerge.filtercommit.CommitTagger;

import com.google.gson.Gson;
import honor.branchmerge.filtercommit.Config.FilterConfig;
import honor.branchmerge.filtercommit.Tag.CommitTag.OnlyTestUpdateCommitTag;
import honor.branchmerge.filtercommit.util.JGitHelperPlus;
import org.eclipse.jgit.revwalk.RevCommit;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OnlyTestUpdateCommitTagger extends BaseCommitTagger
    implements IAttachTag<OnlyTestUpdateCommitTag> {

    public static int tagProducedNum = 0;
    public Map<String, List<String>> commitIdChangeFilesMap;

    public OnlyTestUpdateCommitTagger(JGitHelperPlus cmd, FilterConfig config) {
        super(cmd, config);
        System.out.println("OnlyTestUpdateCommitTagger has set up.");
    }

    @Override
    public OnlyTestUpdateCommitTag produceCommitTag(RevCommit commit) {
        OnlyTestUpdateCommitTag tag = new OnlyTestUpdateCommitTag();
        Gson gson = new Gson();
        List<String> changeFiles = this.cmd.getChangeFilesByCommit(commit);

        OnlyTestUpdateCommitTag.TagDescription description =
                new OnlyTestUpdateCommitTag.TagDescription(commit.getShortMessage(), changeFiles);

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
            // 一个比较 limited 的优化。
            // 因为即使是 base 仓，也只有 40000 左右数量的文件。测试文件在任何时间节点应该都不会超过 10000。
            // 只要修改文件个数超过了 10000，那么就不考虑其修改 Test 文件的可能。
            // TODO isCommitNeedTag 的问题，未来想想如何处理这里
            return false;
        }
        for (String filePath : changeFiles) {
            if (!isContainsTest(filePath)) {
                return false;
            }
        }
        return true;
    }

    private boolean isContainsTest(String input) {
        String pattern = this.config.only_test_update.pattern;
        Pattern p = Pattern.compile(pattern);
        Matcher m = p.matcher(input);
        return m.find();
    }
}
