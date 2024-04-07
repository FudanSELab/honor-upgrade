package honor.branchmerge.filtercommit.CommitTagger;

import com.google.gson.Gson;
import honor.branchmerge.filtercommit.Config.FilterConfig;
import honor.branchmerge.filtercommit.Tag.CommitTag.NonExistenceInFinalCommitTag;
import honor.branchmerge.filtercommit.util.FilterCommitUtil;
import honor.branchmerge.filtercommit.util.JGitHelperPlus;
import org.eclipse.jgit.revwalk.RevCommit;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class NonExistenceInFinalCommitTagger extends BaseCommitTagger
        implements IAttachTag<NonExistenceInFinalCommitTag> {
    public static int tagProducedNum = 0;
    private Set<String> finalFiles;

    public NonExistenceInFinalCommitTagger(JGitHelperPlus cmd, FilterConfig config, RevCommit finalCommit)
            throws IOException {
        super(cmd, config);
        this.finalFiles = new HashSet<>(FilterCommitUtil.getAllFilesInCommitUnderPath(
                cmd.repository, finalCommit, ""));
        System.out.println("NonExistenceInFinalCommitTagger has set up.");
    }

    @Override
    public NonExistenceInFinalCommitTag produceCommitTag(RevCommit commit) {
        // prepare for gson input
        List<String> changeFiles = this.cmd.getChangeFilesByCommit(commit);
        NonExistenceInFinalCommitTag tag = new NonExistenceInFinalCommitTag();
        Gson gson = new Gson();

        NonExistenceInFinalCommitTag.TagDescription desc =
                new NonExistenceInFinalCommitTag.TagDescription(commit.getShortMessage(), changeFiles);

        tag.setCommitId(commit.getId());
        tag.setDescription(gson.toJson(desc));
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
            if (this.finalFiles.contains(filePath)) {
                return false;
            }
        }
        return true;
    }
}
