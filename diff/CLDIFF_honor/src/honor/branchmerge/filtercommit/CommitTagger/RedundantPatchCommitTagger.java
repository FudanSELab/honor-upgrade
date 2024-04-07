package honor.branchmerge.filtercommit.CommitTagger;

import com.google.gson.Gson;
import honor.branchmerge.filtercommit.Config.FilterConfig;
import honor.branchmerge.filtercommit.Tag.CommitTag.RedundantPatchCommitTag;
import honor.branchmerge.filtercommit.util.JGitHelperPlus;
import honor.branchmerge.filtercommit.util.TwoRevCommit;
import org.eclipse.jgit.revwalk.RevCommit;

import java.util.*;

public class RedundantPatchCommitTagger extends BaseCommitTagger
        implements IAttachTag<RedundantPatchCommitTag> {

    public static int tagProducedNum = 0;
    // Pair: key is parent, value is now
    private Map<Integer, List<TwoRevCommit>> commitDiffHashId;
    private Set<String> commitRedundant;

    public RedundantPatchCommitTagger(JGitHelperPlus cmd, FilterConfig config, List<RevCommit> commits) {
        super(cmd, config);
        this.commitDiffHashId = new HashMap<>();
        this.commitRedundant = new HashSet<>();
        for (RevCommit commit : commits) {
            this.updateCommitDiffHashIdMapByCommit(commit);
        }

        // init commitRedundant
        for (Map.Entry<Integer, List<TwoRevCommit>> entry : this.commitDiffHashId.entrySet()) {
            Integer hash = entry.getKey();
            List<TwoRevCommit> diffCommits = entry.getValue();
            if (hash == 0 || diffCommits.size() <= 1) {
                continue;
            }
            for (TwoRevCommit parentAndNow : diffCommits) {
                this.commitRedundant.add(parentAndNow.current.getId().getName());
            }
        }
        System.out.println("RedundantPatchCommitTagger has set up.");
    }

    @Override
    public RedundantPatchCommitTag produceCommitTag(RevCommit commit) {
        RedundantPatchCommitTag tag = new RedundantPatchCommitTag();
        Gson gson = new Gson();
        String commitId = commit.getId().getName();

        Map<TwoRevCommit, List<TwoRevCommit>> redundantCommits = new HashMap<>();
        RevCommit[] parents = commit.getParents();
        for (RevCommit parent : parents) {
            String parentId = parent.getId().getName();
            Integer hash = this.cmd.getCommitDiffHashByTwoCommit(parentId, commitId);
            // 如果 hash 为 0，说明两个 commit 间没有修改，属于是 merge 时带来的无效 edge；
            // 如果不存在于 commitDiffHashId 中，其实是存在问题的，但这里不处理。
            if (hash == 0 || !this.commitDiffHashId.containsKey(hash)) {
                continue;
            }
            List<TwoRevCommit> diffCommits = this.commitDiffHashId.get(hash);
            // 如果 hash 值对应的 diff 只有一个，说明这个 hash 对应的 diff 不存在 redundant commit。
            if (diffCommits.size() <= 1) {
                continue;
            }
            redundantCommits.put(new TwoRevCommit(commit, parent), diffCommits);
        }

        RedundantPatchCommitTag.TagDescription description =
                new RedundantPatchCommitTag.TagDescription(commit.getShortMessage(), redundantCommits);

        tag.setRedundantCommits(redundantCommits);
        tag.setCommitId(commit.getId());
        tag.setDescription(gson.toJson(description));
        tagProducedNum++;
        return tag;
    }

    @Override
    public boolean isCommitNeedTag(RevCommit commit) {
        return this.commitRedundant.contains(commit.getId().getName());
    }

    /**
     * 初始化。输入 commit，查看它的所有 parent commit。
     * 对于每一个 parent，都看其 diff 是否已在 commitDiffHashId 中。
     * 如果不存在则创建，存在则追加到 List 中。
     * @param commit
     */
    private void updateCommitDiffHashIdMapByCommit(RevCommit commit) {
        RevCommit[] parents = commit.getParents();
        // TODO 如果是分支合并，那么或许比较容易出现重复。
        // 事实证明对于一个 5000 commit / node 的仓库，会有大概 3000~4000 个 diff / edge 是重复的
        // 也就是说中间有 1500~2000 个 diff / edge 是冗余的。
        // 但如果把要求缩小到无复数 parent 的 commit 上，即不是 git 分支合并带来的合码冗余，
        // 那么重复 diff 个数会缩小到 100 以内。
        //
        // 所以如果还是要考虑 merge 的情况，就直接把这个 if-return 去掉。
        if (parents.length > 1) {
            return;
        }

        String commitId = commit.getId().getName();
        for (RevCommit parent : parents) {
            String parentId = parent.getId().getName();
            Integer hash = this.cmd.getCommitDiffHashByTwoCommit(parentId, commitId);
            List<TwoRevCommit> value = (this.commitDiffHashId.containsKey(hash))
                    ? this.commitDiffHashId.get(hash)
                    : new ArrayList<>();
            value.add(new TwoRevCommit(commit, parent));
            this.commitDiffHashId.put(hash, value);
        }
    }
}
