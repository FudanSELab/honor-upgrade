package honor.branchmerge.filtercommit.CommitTagger;

import com.google.gson.Gson;
import honor.branchmerge.filtercommit.Config.FilterConfig;
import honor.branchmerge.filtercommit.Tag.CommitTag.OnlyJavadocUpdateCommitTag;
import honor.branchmerge.filtercommit.util.FilterCommitUtil;
import honor.branchmerge.filtercommit.util.JDTUtil;
import honor.branchmerge.filtercommit.util.JGitHelperPlus;
import javafx.util.Pair;
import org.eclipse.jdt.core.dom.Comment;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Javadoc;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.diff.EditList;
import org.eclipse.jgit.revwalk.RevCommit;

import java.util.*;

public class OnlyJavadocUpdateCommitTagger extends BaseCommitTagger
        implements IAttachTag<OnlyJavadocUpdateCommitTag> {

    public static int tagProducedNum = 0;
    private Set<String> javadocUpdateCommitIdSet;

    public OnlyJavadocUpdateCommitTagger(JGitHelperPlus cmd, FilterConfig config, List<RevCommit> commits) {
        super(cmd, config);
        this.javadocUpdateCommitIdSet = new HashSet<>();

        int counter = 0;
        for (RevCommit commit : commits) {
            counter++;
            // 首先跳过所有合并节点。
            if (commit.getParents().length != 1) {
                continue;
            }
            RevCommit parent = commit.getParent(0);
            // 判断一个 commit 是否所有 diffEntry 都和 JavaDoc 有关。
            if (this.isCommitOnlyRelatedToJavadoc(commit, parent)) {
                this.javadocUpdateCommitIdSet.add(commit.getId().getName());
            }
        }
        System.out.println("OnlyJavadocUpdateCommitTagger has set up.");
    }

    @Override
    public boolean isCommitNeedTag(RevCommit commit) {
        return this.javadocUpdateCommitIdSet.contains(commit.getId().getName());
    }

    @Override
    public OnlyJavadocUpdateCommitTag produceCommitTag(RevCommit commit) {
        OnlyJavadocUpdateCommitTag tag = new OnlyJavadocUpdateCommitTag();
        Gson gson = new Gson();

        OnlyJavadocUpdateCommitTag.TagDescription desc =
                new OnlyJavadocUpdateCommitTag.TagDescription(commit.getShortMessage());
        tag.setCommitId(commit.getId());
        tag.setDescription(gson.toJson(desc));
        tagProducedNum++;
        return tag;
    }

    /**
     * 看是不是所有的 diffEntries 里面的 DiffEntry 都和 JavaDoc 有关。
     *
     * 注：这个函数只处理一个 commit 及其一个 parent 之间的关系。
     * 如果一个 commit 有 2 个 parent，说明是 merge 节点，不考虑。
     *
     * commit 和 parent 的关系必须是 current 和 parent 的关系。
     * @param commit
     * @return
     */
    private boolean isCommitOnlyRelatedToJavadoc(RevCommit commit, RevCommit parent) {
        // 首先检验是否 parent 数目为 1，以及给定的 commit 和 parent 是否是 parent 关系。
        if (commit.getParents().length != 1) {
            return false;
        }
        if (!commit.getParent(0).getId().getName()
                .equals(parent.getId().getName())) {
            return false;
        }

        List<DiffEntry> entries = this.cmd.getCommitParentMappedDiffEntry(commit, parent);
        // 先去除掉所有非 java 类型文件。这些文件不纳入考虑范围，
        // 如果后面要求 JavaDoc 全部都是 java 文件才行，那就这里加 if stmt，返回 false。
        entries.removeIf(entry -> !FilterCommitUtil.getNeedPathFromDiff(entry).endsWith(".java"));
        // 因为修改 Javadoc 对于 ChangeType 来说属于 MODIFY，
        // 去除所有不是 modify 的 DiffEntry。
        // 注：removeIf 返回 true，当且仅当存在符合条件的元素被除去。
        if (entries.removeIf(entry -> entry.getChangeType() != DiffEntry.ChangeType.MODIFY)) {
            return false;
        }
        // 如果此时 entries 已经为空了，说明不存在得到修改的源文件（修改的都是无关文件，或增删 java 文件）
        // 直接返回 false。
        if (entries.size() == 0) {
            return false;
        }

        // 对于每一个 DiffEntry / 文件修改 的操作。
        for (DiffEntry diffEntry : entries) {
            if (!this.isModifyDiffEntryOnlyRelatedToJavadoc(diffEntry, parent, commit)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 针对一个 ChangeType 是 MODIFY 的 DiffEntry，看是否仅和 Javadoc 有关。
     *
     * 需要注意的是，一个 RevCommit 和另外一个 RevCommit 之间存在的许多差异，使用 DiffEntry 表示文件层次的差异。
     * 一个 DiffEntry 中存在许多修改，使用 EditList 表示文件内 “代码修改块”（就是 +++/---） 层次的修改。
     *
     * @param diffEntry 表示一个修改文件的 Diff，一个 diffEntry 仅仅表示一个文件。
     * @param prevCommit parent
     * @param currCommit current
     * @return
     */
    private boolean isModifyDiffEntryOnlyRelatedToJavadoc(DiffEntry diffEntry, RevCommit prevCommit, RevCommit currCommit) {
        Map<Javadoc, Pair<Integer, Integer>> prevJavadocPos = new HashMap<>();
        Map<Javadoc, Pair<Integer, Integer>> currJavadocPos = new HashMap<>();

        String filePath = diffEntry.getNewPath(); // newPath 和 oldPath 在 MODIFY 时相等。
        byte[] prevContent = this.cmd.extract(filePath, prevCommit.getId().getName());
        byte[] currContent = this.cmd.extract(filePath, currCommit.getId().getName());
        CompilationUnit prevCu = JDTUtil.getCompilationUnit(prevContent);
        CompilationUnit currCu = JDTUtil.getCompilationUnit(currContent);

        List<Javadoc> prevJavadocList = this.getJavadocFromCu(prevCu);
        for (Javadoc javadoc : prevJavadocList) {
            prevJavadocPos.put(javadoc, JDTUtil.getBeginEndOfASTNode(prevCu, javadoc));
        }
        List<Javadoc> currJavadocList = this.getJavadocFromCu(currCu);
        for (Javadoc javadoc : currJavadocList) {
            currJavadocPos.put(javadoc, JDTUtil.getBeginEndOfASTNode(currCu, javadoc));
        }

        EditList editList = this.cmd.getEditListByDiffEntry(diffEntry);
        for (Edit edit : editList) {
            int beginA = edit.getBeginA(), endA = edit.getEndA();
            int beginB = edit.getBeginB(), endB = edit.getEndB();
            if (beginA == endA) {
                // Edit 属于新增 insert，
                // 新增部分需要位于 current commit file 中某个 Javadoc 的范围内。
                if (!this.existIntervalWrapInput(currJavadocPos, beginB, endB)) {
                    return false;
                }
            } else if (beginB == endB) {
                // Edit 属于删减 delete，
                // 删减部分需要位于 previous commit file 中某个 Javadoc 的范围内。
                if (!this.existIntervalWrapInput(prevJavadocPos, beginA, endA)) {
                    return false;
                }
            } else {
                // Edit 属于替换 replace，
                // 修改部分应该完全位于 current commit file 和 previous commit file 中的某 Javadoc 范围内。
                if (!this.existIntervalWrapInput(prevJavadocPos, beginA, endA)
                        || !this.existIntervalWrapInput(currJavadocPos, beginB, endB)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 根据 cu，获得其中所有的 javadoc
     * @param cu
     * @return
     */
    private List<Javadoc> getJavadocFromCu(CompilationUnit cu) {
        List<Javadoc> result = new ArrayList<>();
        List<Comment> commentList = cu.getCommentList();
        for (Comment comment : commentList) {
            if (comment instanceof Javadoc) {
                result.add((Javadoc) comment);
            }
        }
        return result;
    }

    /**
     * 对于传入的给定区间 a [begin, end]，
     * 判断是否存在一个区间 b \in 区间集合 m，s.t. b 完全包含 a。（b.begin <= a.begin < a.end <= b.end）
     * @param m 区间集合 m
     * @param begin 传入区间的左边界
     * @param end 传入区间的右边界
     * @return
     */
    private boolean existIntervalWrapInput(Map<Javadoc, Pair<Integer, Integer>> m, int begin, int end) {
        for (Map.Entry<Javadoc, Pair<Integer, Integer>> entry : m.entrySet()) {
            Pair<Integer, Integer> interval = entry.getValue();
            int intervalBegin = interval.getKey();
            int intervalEnd = interval.getValue();
            if (intervalBegin <= begin && end <= intervalEnd) {
                return true;
            }
        }
        return false;
    }
}
