package honor.branchmerge.filtercommit.util;

import honor.branchmerge.filtercommit.TaggedRevCommit;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.treewalk.TreeWalk;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FilterCommitUtil {
    /**
     * 去除 commits 中的所有 merge 节点。
     * @param commits
     */
    public static void removeMergeCommit(List<RevCommit> commits) {
        commits.removeIf(commit -> commit.getParents().length > 1);
    }

    /**
     * 把 RevCommit 转化成 TaggedRevCommit
     * @param commits
     * @return
     */
    public static List<TaggedRevCommit> make(List<RevCommit> commits) {
        List<TaggedRevCommit> result = new ArrayList<>();
        for (RevCommit commit : commits) {
            result.add(new TaggedRevCommit(commit));
        }
        return result;
    }

    /**
     * 根据 DiffEntry 获得应该考虑的 Path。
     * 比如说 DELETE 看的就是 oldPath，其是不存在 newPath 的。
     * 具体分类方法可见 class DiffEnrty 的 toString 方法
     * @param diff
     * @return
     */
    public static String getNeedPathFromDiff(DiffEntry diff) {
        switch (diff.getChangeType()) {
            case ADD:
            case COPY:
            case RENAME:
                return diff.getNewPath();
            case MODIFY:
            case DELETE:
            default:
                return diff.getOldPath();
        }
    }

    /**
     * 给定一个仓库，以及里面的一个 commit id，还有仓库下的路径，
     * 获取该 commit 的该路径下的所有文件名
     * @param repository
     * @param commit
     * @param path
     * @return
     * @throws IOException
     */
    public static List<String> getAllFilesInCommitUnderPath(Repository repository, RevCommit commit, String path)
        throws IOException {
        List<String> result = new ArrayList<>();
        TreeWalk walk = new TreeWalk(repository);

        // path is the relative path to directory
        // e.g path is "src".
        // (path means root dir if and only if path is "")
        RevTree tree = commit.getTree();
        if (path.isEmpty()) {
            walk.addTree(tree);
        } else {
            TreeWalk treeWalk = TreeWalk.forPath(repository, path, tree);
            walk.addTree(treeWalk.getObjectId(0));
        }

        // begin to walk directory
        walk.setRecursive(true);
        while (walk.next()) {
            result.add(walk.getPathString());
        }
        return result;
    }
}
