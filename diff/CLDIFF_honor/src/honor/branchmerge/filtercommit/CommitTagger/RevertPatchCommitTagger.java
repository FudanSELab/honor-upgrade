package honor.branchmerge.filtercommit.CommitTagger;

import com.google.gson.Gson;
import honor.branchmerge.filtercommit.Config.FilterConfig;
import honor.branchmerge.filtercommit.Tag.CommitTag.RevertPatchCommitTag;
import honor.branchmerge.filtercommit.util.JGitHelperPlus;
import org.eclipse.jgit.revwalk.RevCommit;

import java.util.*;

public class RevertPatchCommitTagger extends BaseCommitTagger
        implements IAttachTag<RevertPatchCommitTag> {
    public static int tagProducedNum = 0;
    // revert commit message -> revert commit
    // begin commit id (each commit id in above) -> List<RevCommit> from begin to end
    private Map<String, RevCommit> revertCommitMessageMap;

    // 这个变量是用来构建 revertCommitListMap 的中间成员变量，
    // 多个 private 方法都需要使用，所以为了方便写在这里。
    // 内部存储的每个元素都是一个 “revert 关系链表”，
    // 由于一串 git-history 可能存在多个 “revert 关系链表”，所以是 Map<String, List<String>>。
    // key 是 commit message 的 core content。
    private Map<String, List<RevCommit>> revertCommitMsgLists;

    // TODO 可能出现完全一样的 commit message，并且恰好是做 revert 的。
    private Map<String, List<RevCommit>> revertCommitListMap;

    public RevertPatchCommitTagger(JGitHelperPlus cmd, FilterConfig config, List<RevCommit> commits) {
        super(cmd, config);
        this.revertCommitListMap = new HashMap<>();
        this.revertCommitMessageMap = new HashMap<>();
        this.revertCommitMsgLists = new HashMap<>();

        this.initRevertCommitMessageMap(commits);
        this.initRevertCommitMsgLists();
        this.initRevertCommitListMap();
        System.out.println("RevertPatchCommitTagger has set up.");
    }

    /**
     * 使用 produceCommitTag 需要保证 commit 的 msg 必须存在于 revertCommitListMap 中。
     * @param commit
     * @return
     */
    @Override
    public RevertPatchCommitTag produceCommitTag(RevCommit commit) {
        RevertPatchCommitTag tag = new RevertPatchCommitTag();
        Gson gson = new Gson();
        String commitMsg = commit.getShortMessage();
        List<RevCommit> commits = this.revertCommitListMap.get(commitMsg);

        RevertPatchCommitTag.TagDescription description =
                new RevertPatchCommitTag.TagDescription(commitMsg, commits);

        tag.setCommitId(commit.getId());
        tag.setCommitRevertList(commits);
        tag.setDescription(gson.toJson(description));
        tagProducedNum++;
        return tag;
    }

    @Override
    public boolean isCommitNeedTag(RevCommit commit) {
        String commitMsg = commit.getShortMessage();
        String commitId = commit.getId().getName();
        if (this.revertCommitListMap.containsKey(commitMsg)) {
            List<RevCommit> revertCommitsWithGivenMsg = this.revertCommitListMap.get(commitMsg);
            for (RevCommit revertCommitWithGivenMsg : revertCommitsWithGivenMsg) {
                if (revertCommitWithGivenMsg.getId().getName().equals(commitId)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 初始化 revertCommitMessageMap
     * @param commits
     */
    private void initRevertCommitMessageMap(List<RevCommit> commits) {
        // 用于存在未被处理的 revert inner commit message -> RevCommit
        Map<String, RevCommit> revertCommitMsgTodoMap = new HashMap<>();
        // 所有 RevCommit 都会被暂存在这里 normal commit message -> RevCommit
        Map<String, RevCommit> commitMessageMap = new HashMap<>();
        for (RevCommit commit : commits) {
            String commitMsg = commit.getShortMessage();
            // 如果以 Revert 开头，就判断是否处理过具备 inner message 的 commit。
            // 有则加到 revertCommitMessageMap 中，无则加到 revertCommitMsgTodoMap。
            if (commitMsg.startsWith("Revert")) {
                String innerMsg = this.getRevertMessageInnerContent(commitMsg);
                if (!commitMessageMap.containsKey(innerMsg)) {
                    revertCommitMsgTodoMap.put(commitMsg, commit);
                } else {
                    RevCommit revertCommit = commit;
                    RevCommit revertedCommit = commitMessageMap.get(innerMsg);
                    this.revertCommitMessageMap.put(commitMsg, revertCommit);
                    this.revertCommitMessageMap.put(innerMsg, revertedCommit);
                }
            }

            if (!commitMessageMap.containsKey(commitMsg)) {
                commitMessageMap.put(commitMsg, commit);
            }
        }

        // 处理 revertCommitMsgTodoMap 里面剩余的 commit，
        // 这些 commit 是未被处理过的，也就是说未在 commitMessageMap 找到对应者。
        for (Map.Entry<String, RevCommit> entry : revertCommitMsgTodoMap.entrySet()) {
            String commitMsg = entry.getKey();
            String innerMsg = this.getRevertMessageInnerContent(commitMsg);
            if (!commitMessageMap.containsKey(innerMsg)) {
                continue;
            }
            RevCommit revertCommit = entry.getValue();
            RevCommit revertedCommit = commitMessageMap.get(innerMsg);
            this.revertCommitMessageMap.put(commitMsg, revertCommit);
            this.revertCommitMessageMap.put(innerMsg, revertedCommit);
        }
    }

    /**
     * 初始化 revertCommitMsgLists
     */
    private void initRevertCommitMsgLists() {
        for (Map.Entry<String, RevCommit> entry : this.revertCommitMessageMap.entrySet()) {
            String commitMsg = entry.getKey();
            String coreContent = this.getRevertMessageCoreContent(commitMsg);
            RevCommit commit = this.revertCommitMessageMap.get(commitMsg);
            if (commit == null) {
                // 直接不管找不到的情况，不重要。
                continue;
            }

            if (this.revertCommitMsgLists.containsKey(coreContent)) {
                this.revertCommitMsgLists.get(coreContent).add(commit);
            } else {
                List<RevCommit> newCommitMsgList = new ArrayList<>();
                newCommitMsgList.add(commit);
                this.revertCommitMsgLists.put(coreContent, newCommitMsgList);
            }
        }

        for (Map.Entry<String, List<RevCommit>> entry : this.revertCommitMsgLists.entrySet()) {
            List<RevCommit> commitMsgList = entry.getValue();
            // 按照字符串长度降序排列
            commitMsgList.sort(new Comparator<RevCommit>() {
                @Override
                public int compare(RevCommit o1, RevCommit o2) {
                    return o2.getShortMessage().length() - o1.getShortMessage().length();
                }
            });
        }
    }

    /**
     * 初始化 revertCommitListMap
     */
    private void initRevertCommitListMap() {
        for (Map.Entry<String, RevCommit> entry : this.revertCommitMessageMap.entrySet()) {
            String commitMsg = entry.getKey();
            String coreContent = this.getRevertMessageCoreContent(commitMsg);
            if (!this.revertCommitMsgLists.containsKey(coreContent)) {
                // 直接不管，不重要。
                continue;
            }
            List<RevCommit> commitRevertList = this.revertCommitMsgLists.get(coreContent);
            this.revertCommitListMap.put(commitMsg, commitRevertList);
        }
    }

    /**
     * 获取 x 层 revert commit 中最核心的 inner commit message，
     * 这里称其为 core content。
     * 传入 "Revert "Revert "the message of commit that will be reverted"""
     * 会得到 "the message of commit that will be reverted"s
     *
     * 如果传入的不以 Revert 开头，那么会直接返回本身，表示自己就是 core content
     * @param commitMsg
     * @return
     */
    private String getRevertMessageCoreContent(String commitMsg) {
        while (commitMsg.startsWith("Revert ")) {
            commitMsg = this.getRevertMessageInnerContent(commitMsg);
        }
        return commitMsg;
    }

    /**
     * 获取 revert commit 中的 commit message。
     * 一般 Revert commit 的 commit message 都是：
     * "Revert "the message of commit that will be reverted""
     * 这里就不考虑 Revert 之后没有引号的情况。
     *
     * 如果传入的 message 不以 Revert 开头，返回空字符串
     * @param commitMsg
     * @return
     */
    private String getRevertMessageInnerContent(String commitMsg) {
        if (!commitMsg.startsWith("Revert ")) {
            return "";
        }
        int begin = commitMsg.indexOf("\"");
        int end = commitMsg.lastIndexOf("\"");
        if (begin > 0 && end == commitMsg.length() - 1) {
            return commitMsg.substring(begin + 1, end);
        }
        // 如果 Revert 这个 patch 中不存在引号，那么就视为无效
        return "";
    }
}
