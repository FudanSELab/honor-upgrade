package honor.branchmerge.filtercommit.util;

import edu.fdu.se.git.JGitHelper;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.EditList;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.patch.FileHeader;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.util.io.DisabledOutputStream;

import java.io.IOException;
import java.util.*;

public class JGitHelperPlus extends JGitHelper {
    public JGitHelperPlus(String repoPath) {
        super(repoPath);
    }

    /**
     * 根据 tag name 获得其所在的 commit
     * @param tagName
     * @return
     */
    public RevCommit getCommitByTag(String tagName) {
        try {
            List<Ref> tags = this.git.tagList().call();
            for (Ref tag : tags) {
                if (tag.getName().split("/")[2].equals(tagName)) {
                    ObjectId taggedCommitId = peelTagOnion(tag);
                    return this.revWalk.parseCommit(taggedCommitId);
                }
            }
        } catch (IOException | GitAPIException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 根据 commit id 获得 RevCommit
     * @param commitId
     * @return
     */
    public RevCommit getCommitById(String commitId) {
        try (RevWalk walk = new RevWalk(this.repository)) {
            return walk.parseCommit(this.repository.resolve(commitId));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 获得两个 commit 间的所有 commit。
     * 用于给定两个 Tag 版本，然后获得中间所有 commit。
     * @param beginId
     * @param endId
     * @return
     */
    public List<RevCommit> getCommitInRange(String beginId, String endId) {
        try {
            ObjectId beginObjId = this.repository.resolve(beginId);
            ObjectId endObjId = this.repository.resolve(endId);
            return getCommitInRange(beginObjId, endObjId);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    /**
     * 获得两个 commit 间的所有 commit。
     * 用于给定两个 Tag 版本，然后获得中间所有 commit。
     * @param beginId
     * @param endId
     * @return
     */
    public List<RevCommit> getCommitInRange(ObjectId beginId, ObjectId endId) {
        try {
            RevCommit begin = this.revWalk.parseCommit(beginId);
            RevCommit end = this.revWalk.parseCommit(endId);
            return getCommitInRange(begin, end);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    /**
     * 获得两个 commit 间的所有 commit。
     * 用于给定两个 Tag 版本，然后获得中间所有 commit。
     *
     * 需要注意的是，walk.iterator() 获得的 Iterator 会去遍历所有在 begin
     * 处合并的 commit 的 parent。也就是说，很可能那个 commit 甚至在 end
     * 提交时间之后，但由于它合入了 begin，也是 end 到 begin 的差异，所以
     * 也会被遍历。这是极其符合需求的。
     * @param begin
     * @param end
     * @return
     */
    public List<RevCommit> getCommitInRange(RevCommit begin, RevCommit end) {
        try (RevWalk walk = new RevWalk(this.repository)) {
            walk.markStart(begin);
            walk.markUninteresting(end);

            List<RevCommit> result = new ArrayList<>();
            Iterator<RevCommit> iter = walk.iterator();
            for (Iterator<RevCommit> it = iter; it.hasNext();) {
                RevCommit commit = it.next();
                result.add(commit);
            }
            return result;
        } catch (MissingObjectException | IncorrectObjectTypeException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    /**
     * 给定两个 commit id，获得两个 commit id 间的 diff 的 hash 化结果。
     * hash 计算方法具体参考 getCommitDiffHashByDiffEntries
     * @param parentId
     * @param commitId
     * @return
     */
    public int getCommitDiffHashByTwoCommit(String parentId,  String commitId) {
        Map<String, List<DiffEntry>> diffEntry = this.getTwoCommitsMappedFileList(parentId, commitId);
        // it must be correct, and diffs should not be null
        List<DiffEntry> diffs = diffEntry.get(parentId);
        return this.getCommitDiffHashByDiffEntries(diffs);
    }

    /**
     * 给定一组 diff entry，获得总的 hash id。
     * 其实对于每个差异都应该获取一个 hash id，但是一次 commit 可能涉及到的修改
     * 包括多个 diff entry。所以应该把多个 entry 的 string 拼接在一起。
     * @param entries
     * @return
     */
    public int getCommitDiffHashByDiffEntries(List<DiffEntry> entries) {
        String result = "";
        for (DiffEntry diff : entries) {
            result += this.getCommitDiffByDiffEntry(diff);
        }
        return result.hashCode();
    }

    /**
     * 根据一个 commit，获得其与先前 parent commit 间所有存在 diff 的文件名。
     * @param commit
     * @return
     */
    public List<String> getChangeFilesByCommit(RevCommit commit) {
        Set<String> result = new HashSet<>();
        Map<String, List<DiffEntry>> diffMap =  this.getCommitParentMappedDiffEntry(commit);
        for (Map.Entry<String, List<DiffEntry>> entry : diffMap.entrySet()) {
            List<DiffEntry> diffs = entry.getValue();
            for (DiffEntry diff : diffs) {
                String filePath = FilterCommitUtil.getNeedPathFromDiff(diff);
                if (!result.contains(filePath)) {
                    result.add(filePath);
                }
            }
        }
        return new ArrayList<>(result);
    }

    /**
     * 根据一个 diff entry，获得能够表征其 diff 的唯一信息。
     * 这里不使用 diff 的具体内容（因为需要计算 hash code，时间复杂度过高）
     * 而是使用 diff 中的 src / dst 文件 objectID。
     * @param entry
     * @return
     */
    private String getCommitDiffByDiffEntry(DiffEntry entry) {
        String result = entry.getNewId().name() + " " + entry.getOldId().name() + "; ";
        return result;
    }

    /**
     * 如果是 annotated tag，就 peel 它，
     * 如果不是就返回 ObjectId
     * @param tag
     * @return
     */
    private ObjectId peelTagOnion(Ref tag) {
        if (tag.isPeeled()) {
            return tag.getPeeledObjectId();
        }
        return tag.getObjectId();
    }

    /**
     * 传入一个 RevCommit，然后获得他的所有 DiffEntry
     * 直接传入 RevCommit 能够避免 RevWalk 在 parseCommit 时带来的开销，极大提高性能
     * @param commit
     * @return Map key 是 parent 的 commit id，value 是那个 commit 对应的 diff
     */
    public Map<String, List<DiffEntry>> getCommitParentMappedDiffEntry(RevCommit commit) {
        Map<String, List<DiffEntry>> result = new HashMap<>();
        RevCommit[] parents = commit.getParents();
        for (RevCommit parent : parents) {
            List<DiffEntry> entries = this.getCommitParentMappedDiffEntry(commit, parent);
            if (entries == null) {
                // 如果 getCommitParentMappedDiffEntry 报错就暂且返回 null
                return null;
            }
            result.put(parent.getName(), entries);
        }
        return result;
    }

    /**
     * 根据 current 和 parent 获取 List<DiffEntry>
     * @param current
     * @param parent
     * @return
     */
    public List<DiffEntry> getCommitParentMappedDiffEntry(RevCommit current, RevCommit parent) {
        ObjectReader reader = this.git.getRepository().newObjectReader();
        ObjectId newTree = current.getTree().getId();
        ObjectId oldTree = parent.getTree().getId();
        CanonicalTreeParser newTreeIter = new CanonicalTreeParser();
        CanonicalTreeParser oldTreeIter = new CanonicalTreeParser();

        try (DiffFormatter diffFormatter = new DiffFormatter(DisabledOutputStream.INSTANCE)) {
            diffFormatter.setRepository(this.git.getRepository());
            newTreeIter.reset(reader, newTree);
            oldTreeIter.reset(reader, oldTree);
            List<DiffEntry> entries = diffFormatter.scan(oldTreeIter, newTreeIter);
            return entries;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 得到给定 entry 的所有 edit。
     * DiffEntry 只针对一个文件，EditList 是一个文件的多个修改。
     * @param entry
     * @return
     */
    public EditList getEditListByDiffEntry(DiffEntry entry) {
        try (DiffFormatter diffFormatter = new DiffFormatter(DisabledOutputStream.INSTANCE)) {
            diffFormatter.setRepository(this.repository);
            FileHeader fh = diffFormatter.toFileHeader(entry);
            return fh.toEditList();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
