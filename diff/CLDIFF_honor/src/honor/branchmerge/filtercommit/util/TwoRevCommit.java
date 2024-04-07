package honor.branchmerge.filtercommit.util;

import org.eclipse.jgit.revwalk.RevCommit;

/**
 * current 和 parent commit id 的 data class
 */
public class TwoRevCommit {
    public RevCommit current;
    public RevCommit parent;
    public TwoRevCommit(RevCommit current, RevCommit parent) {
        this.current = current;
        this.parent = parent;
    }
}