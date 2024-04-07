package honor.branchmerge.filtercommit.CommitTagger;

import com.google.gson.Gson;
import honor.branchmerge.filtercommit.Config.FilterConfig;
import honor.branchmerge.filtercommit.Tag.CommitTag.OnlyCommentUpdateCommitTag;
import honor.branchmerge.filtercommit.util.FilterCommitUtil;
import honor.branchmerge.filtercommit.util.JDTUtil;
import honor.branchmerge.filtercommit.util.JGitHelperPlus;
import javafx.util.Pair;
import org.eclipse.jdt.core.dom.BlockComment;
import org.eclipse.jdt.core.dom.Comment;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.LineComment;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.diff.EditList;
import org.eclipse.jgit.revwalk.RevCommit;

import java.util.*;

public class OnlyCommentUpdateCommitTagger extends BaseCommitTagger
    implements IAttachTag<OnlyCommentUpdateCommitTag> {

    public static int tagProducedNum = 0;
    private Set<String> commentUpdateCommitIdSet;

    public OnlyCommentUpdateCommitTagger(JGitHelperPlus cmd, FilterConfig config, List<RevCommit> commits) {
        super(cmd, config);
        this.commentUpdateCommitIdSet = new HashSet<>();

        int counter = 0;
        for (RevCommit commit : commits) {
            counter++;
            if (commit.getParents().length != 1) {
                continue;
            }
            RevCommit parent = commit.getParent(0);
            if (this.isCommitOnlyRelatedToNormalComment(commit, parent)) {
                this.commentUpdateCommitIdSet.add(commit.getId().getName());
            }
        }
        System.out.println("OnlyCommentUpdateCommitTagger has set up.");
    }

    @Override
    public boolean isCommitNeedTag(RevCommit commit) {
        return this.commentUpdateCommitIdSet.contains(commit.getId().getName());
    }

    @Override
    public OnlyCommentUpdateCommitTag produceCommitTag(RevCommit commit) {
        OnlyCommentUpdateCommitTag tag = new OnlyCommentUpdateCommitTag();
        Gson gson = new Gson();

        OnlyCommentUpdateCommitTag.TagDescription desc =
                new OnlyCommentUpdateCommitTag.TagDescription(commit.getShortMessage());
        tag.setCommitId(commit.getId());
        tag.setDescription(gson.toJson(desc));
        tagProducedNum++;
        return tag;
    }

    /**
     * 主要注释详见 OnlyJavadocUpdateCommitTagger 中的 isCommitOnlyRelatedToJavadoc。
     * 这里只给出与上述方法不同且必要过程的注释。
     * @param curr 当前 commit
     * @param prev 当前 commit 的唯一 parent commit
     * @return 该 commit-parent 确实是只有普通注释的修改，暨单行注释与多行注释的修改。
     */
    private boolean isCommitOnlyRelatedToNormalComment(RevCommit curr, RevCommit prev) {
        if (curr.getParents().length != 1) {
            return false;
        }
        if (!curr.getParent(0).getId().getName().equals(prev.getId().getName())) {
            return false;
        }

        List<DiffEntry> entries = this.cmd.getCommitParentMappedDiffEntry(curr, prev);
        entries.removeIf(entry -> !FilterCommitUtil.getNeedPathFromDiff(entry).endsWith(".java"));
        if (entries.removeIf(entry -> entry.getChangeType() != DiffEntry.ChangeType.MODIFY)) {
            return false;
        }
        if (entries.size() == 0) {
            return false;
        }

        for (DiffEntry entry : entries) {
            // 对于每一个文件修改，如果存在一个文件并不只和注释修改有关，那么就返回 false。
            if (!this.isModifyDiffEntryOnlyRelatedToNormalComment(entry, curr, prev)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 针对一个 ChangeType 是 MODIFY 的 DiffEntry，看是否仅和 LineComment 和 BlockComment 有关。
     * @param diffEntry 表示一个修改文件的 Diff，一个 diffEntry 仅仅表示一个文件。
     * @param curr 当前 commit。
     * @param prev 当前 commit 的唯一 parent commit。
     * @return 返回 true，当且仅当整个 DiffEntry 中的所有 Edit 都只和 LineComment 和 BlockComment 有关。
     */
    private boolean isModifyDiffEntryOnlyRelatedToNormalComment(DiffEntry diffEntry, RevCommit curr, RevCommit prev) {
        Map<LineComment, Pair<Integer, Integer>> prevLineCommentPos = new HashMap<>();
        Map<LineComment, Pair<Integer, Integer>> currLineCommentPos = new HashMap<>();
        Map<BlockComment, Pair<Integer, Integer>> prevBlockCommentPos = new HashMap<>();
        Map<BlockComment, Pair<Integer, Integer>> currBlockCommentPos = new HashMap<>();

        String filePath = diffEntry.getNewPath();
        byte[] prevContent = this.cmd.extract(filePath, prev.getId().getName());
        byte[] currContent = this.cmd.extract(filePath, curr.getId().getName());
        CompilationUnit prevCu = JDTUtil.getCompilationUnit(prevContent);
        CompilationUnit currCu = JDTUtil.getCompilationUnit(currContent);

        this.initNormalCommentPosMap(prevCu, prevLineCommentPos, prevBlockCommentPos);
        this.initNormalCommentPosMap(currCu, currLineCommentPos, currBlockCommentPos);

        EditList editList = this.cmd.getEditListByDiffEntry(diffEntry);
        for (Edit edit : editList) {
            if (!this.isEditForBlockComment(edit, currBlockCommentPos, prevBlockCommentPos)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 使用 cu，初始化 LineComment 和 BlockComment 的 Position 信息。
     * @param cu 源文件对应的 CompilationUnit，可能是 prev 或者 curr。
     * @param lineCommentPairMap cu 中的 LineComment Position 的 Map。
     * @param blockCommentPairMap cu 中的 BlockComment Position 的 Map。
     */
    private void initNormalCommentPosMap(CompilationUnit cu,
                                         Map<LineComment, Pair<Integer, Integer>> lineCommentPairMap,
                                         Map<BlockComment, Pair<Integer, Integer>> blockCommentPairMap) {
        List<Comment> comments = cu.getCommentList();
        for (Comment comment : comments) {
            if (comment instanceof LineComment) {
                // LineComment lineComment = (LineComment) comment;
                // lineCommentPairMap.put(lineComment, JDTUtil.getBeginEndPosOfASTNode(lineComment));
            } else if (comment instanceof BlockComment) {
                BlockComment blockComment = (BlockComment) comment;
                blockCommentPairMap.put(blockComment, JDTUtil.getBeginEndOfASTNode(cu, blockComment));
            }
            // ignore Javadoc
        }
    }

    /**
     * 对于一个给定的 Edit，检查其是否在修改 BlockComment。
     * @param edit
     * @param currBlockCommentLineNoMap
     * @param prevBlockCommentLineNoMap
     * @return 返回是 true，则说明该 Edit 是对 BlockComment 的修改。
     */
    private boolean isEditForBlockComment(Edit edit,
                                          Map<BlockComment, Pair<Integer, Integer>> currBlockCommentLineNoMap,
                                          Map<BlockComment, Pair<Integer, Integer>> prevBlockCommentLineNoMap) {
        int beginA = edit.getBeginA(), endA = edit.getEndA();
        int beginB = edit.getBeginB(), endB = edit.getEndB();
        if (beginA == endA) {
            if (!this.isBlockCommentModification(currBlockCommentLineNoMap, beginB, endB)) {
                return false;
            }
        } else if (beginB == endB) {
            if (!this.isBlockCommentModification(prevBlockCommentLineNoMap, beginA, endA)) {
                return false;
            }
        } else {
            if (!this.isBlockCommentModification(prevBlockCommentLineNoMap, beginA, endA)
                || !this.isBlockCommentModification(currBlockCommentLineNoMap, beginB, endB)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 判断传入的行号是否在 BlockComment 范围中
     * @param m BlockComment 区间集合
     * @param begin 修改的起始行号
     * @param end 修改的终止行号
     * @return
     */
    private boolean isBlockCommentModification(Map<BlockComment, Pair<Integer, Integer>> m, int begin, int end) {
        for (Map.Entry<BlockComment, Pair<Integer, Integer>> entry : m.entrySet()) {
            Pair<Integer, Integer> interval = entry.getValue();
            int intervalBegin = interval.getKey();
            int intervalEnd = interval.getValue();
            if (intervalBegin <= begin && end <= intervalEnd) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断传入的 position 是否在 LineComment 范围中
     *
     * 不过 LineComment 修改判断很麻烦。
     * @param m LineComment 的 position 范围区间集合
     * @param beginPos 修改的起始位置
     * @param endPos 修改的终止位置
     * @return
     */
    @Deprecated
    private boolean isLineCommentModification(Map<LineComment, Pair<Integer, Integer>> m, int beginPos, int endPos) {
        for (Map.Entry<LineComment, Pair<Integer, Integer>> entry : m.entrySet()) {
            Pair<Integer, Integer> posInterval = entry.getValue();
            int posIntervalBegin = posInterval.getKey();
            int posIntervalEnd = posInterval.getValue();
            if (posIntervalBegin <= beginPos && endPos <= posIntervalEnd) {
                return true;
            }
        }
        return false;
    }
}
