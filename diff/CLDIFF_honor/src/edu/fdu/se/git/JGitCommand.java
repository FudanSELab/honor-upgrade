package edu.fdu.se.git;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jgit.api.CheckoutCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.CheckoutConflictException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRefNameException;
import org.eclipse.jgit.api.errors.RefAlreadyExistsException;
import org.eclipse.jgit.api.errors.RefNotFoundException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.errors.AmbiguousObjectException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.errors.RevisionSyntaxException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevObject;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.util.io.DisabledOutputStream;
import edu.fdu.se.fileutil.FileRWUtil;

public class JGitCommand {

	public Repository repository;
	public RevWalk revWalk;
	public String repoPath;
	public Git git;

	final static public int ALL_FILE = 0;
	final static public int JAVA_FILE = 1;
	final static public int CORE_JAVA_FILE = 2;

	/**
	 * Constructor
	 * 
	 * @param repopath
	 */
	public JGitCommand(String repopath) {
		FileRepositoryBuilder builder = new FileRepositoryBuilder();
		try {
			repository = builder.setGitDir(new File(repopath)).readEnvironment() // scan
					.findGitDir() // scan up the file system tree
					.build();
			revWalk = new RevWalk(repository);
			git = new Git(repository);
			repoPath = repopath;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private RevCommit revCommit;



	/**
	 * 带有parent id的file list,返回值为DiffEntry的map
	 */
	public Map<String, List<DiffEntry>> getCommitParentMappedDiffEntry(String commmitid) {
		Map<String, List<DiffEntry>> result = new HashMap<>();
		ObjectId commitId = ObjectId.fromString(commmitid);
		RevCommit commit = null;
		try {
			commit = revWalk.parseCommit(commitId);
			RevCommit[] parentsCommits = commit.getParents();
			for (RevCommit parent : parentsCommits) {
				ObjectReader reader = git.getRepository().newObjectReader();
				CanonicalTreeParser newTreeIter = new CanonicalTreeParser();
				ObjectId newTree = commit.getTree().getId();
				newTreeIter.reset(reader, newTree);
				CanonicalTreeParser oldTreeIter = new CanonicalTreeParser();
				RevCommit pCommit = revWalk.parseCommit(parent.getId());
				ObjectId oldTree = pCommit.getTree().getId();
				oldTreeIter.reset(reader, oldTree);
				DiffFormatter diffFormatter = new DiffFormatter(DisabledOutputStream.INSTANCE);
				diffFormatter.setRepository(git.getRepository());
				List<DiffEntry> entries = diffFormatter.scan(oldTreeIter, newTreeIter);
				result.put(pCommit.getName(), entries);
				diffFormatter.close();
			}
			return result;
		} catch (MissingObjectException e) {
			e.printStackTrace();
		} catch (IncorrectObjectTypeException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}


	/**
	 *  比对两个commit,寻找出其中不同的files
	 */
	public Map<String, List<DiffEntry>> getTwoCommitsMappedFileList(String currCommitIdInString, String nextCommitIdInString) {
		Map<String, List<DiffEntry>> result = new HashMap<>();
		ObjectId currCommitId = ObjectId.fromString(currCommitIdInString);
		ObjectId nextCommitId = ObjectId.fromString(nextCommitIdInString);
		RevCommit currCommit = null;
		RevCommit nextCommit = null;
		DiffFormatter diffFormatter = null;
		try {
			currCommit = revWalk.parseCommit(currCommitId);
			nextCommit = revWalk.parseCommit(nextCommitId);
			ObjectReader reader = git.getRepository().newObjectReader();
			CanonicalTreeParser currTreeIter = new CanonicalTreeParser();
			ObjectId currTree = currCommit.getTree().getId();
			currTreeIter.reset(reader, currTree);
			CanonicalTreeParser nextTreeIter = new CanonicalTreeParser();
			ObjectId nextTree = nextCommit.getTree().getId();
			nextTreeIter.reset(reader, nextTree);
			diffFormatter = new DiffFormatter(DisabledOutputStream.INSTANCE);
			diffFormatter.setRepository(git.getRepository());
			List<DiffEntry> entries = diffFormatter.scan(currTreeIter, nextTreeIter);
			result.put(currCommit.getName(), entries);
			return result;
		} catch (MissingObjectException e) {
			e.printStackTrace();
		} catch (IncorrectObjectTypeException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}finally {
			diffFormatter.close();
		}
		return null;
	}


	/**
	 * read commit time
	 * 
	 * @param commitId
	 * @return seconds since epoch
	 */
	public int readCommitTime(ObjectId commitId) {
		try {
			RevCommit revCommit = revWalk.parseCommit(commitId);
			int time = revCommit.getCommitTime();
			return time;
		} catch (MissingObjectException e) {
			e.printStackTrace();
		} catch (IncorrectObjectTypeException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return 0;
	}

	/**
	 * read commit time
	 * 
	 * @param commitId
	 * @return seconds since epoch
	 */
	public int readCommitTime(String commitId) {
		try {
			ObjectId id = ObjectId.fromString(commitId);
			RevCommit revCommit = revWalk.parseCommit(id);
			int time = revCommit.getCommitTime();
			return time;
		} catch (MissingObjectException e) {
			e.printStackTrace();
		} catch (IncorrectObjectTypeException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return 0;
	}


	/**
	 * get parents commit id in string
	 * 
	 * @param commitId
	 * @return
	 */
	public String[] getCommitParents(String commitId) {
		ObjectId id = ObjectId.fromString(commitId);
		try {
			RevCommit commit = revWalk.parseCommit(id);
			RevCommit[] parentCommits = commit.getParents();
			String[] parentCommitsString = new String[parentCommits.length];
			for (int i = 0; i < parentCommits.length; i++) {
				parentCommitsString[i] = parentCommits[i].getName();
			}
			return parentCommitsString;
		} catch (MissingObjectException e) {
			e.printStackTrace();
		} catch (IncorrectObjectTypeException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * checkout to one commit
	 * 
	 * @param commitid
	 */
	public void checkout(String commitid) {
		try {
			CheckoutCommand checkoutCommand = git.checkout();
			checkoutCommand.setName(commitid).call();
		} catch (RefAlreadyExistsException e) {
			e.printStackTrace();
		} catch (RefNotFoundException e) {
			e.printStackTrace();
		} catch (InvalidRefNameException e) {
			e.printStackTrace();
		} catch (CheckoutConflictException e) {
			e.printStackTrace();
		} catch (GitAPIException e) {
			e.printStackTrace();
		}
	}

	/**
	 * extract file given file path and commit id
	 * core/java/android/app/Activity.java
	 * 
	 * @param fileName
	 * @param revisionId
	 * @return
	 */
	public byte[] extract(String fileName, String revisionId) {
		if (revisionId == null || fileName == null) {
            System.err.println("revisionId/fileFullPackageName is null..");
			return null;
		}
		if (repository == null || git == null || revWalk == null) {
			System.err.println("git repo is null..");
			return null;
		}
		RevWalk walk = new RevWalk(repository);
		try {
			ObjectId objId = repository.resolve(revisionId);
			if (objId == null) {
				System.err.println("The revision:" + revisionId + " does not exist.");
				walk.close();
				return null;
			}
			RevCommit commit = walk.parseCommit(repository.resolve(revisionId));
			if (commit != null) {
				RevTree tree = commit.getTree();
				TreeWalk treeWalk = TreeWalk.forPath(repository, fileName, tree);
				ObjectId id = treeWalk.getObjectId(0);

				InputStream is = FileRWUtil.open(id, repository);
				byte[] res = FileRWUtil.toByteArray(is);
				return res;
			} else {
				System.err.println("Cannot found file(" + fileName + ") in revision(" + revisionId + "): " + revWalk);
			}
		} catch (RevisionSyntaxException e) {
			e.printStackTrace();
		} catch (MissingObjectException e) {
			e.printStackTrace();
		} catch (IncorrectObjectTypeException e) {
			e.printStackTrace();
		} catch (AmbiguousObjectException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		walk.close();
		return null;
	}

	/**
	 * extract file given file path and commit id
	 * core/java/android/app/Activity.java
	 * 
	 * @param fileName
	 * @param revisionId
	 * @return
	 */
	public InputStream extractAndReturnInputStream(String fileName, String revisionId) {
		if (revisionId == null || fileName == null) {
            System.err.println("revisionId/fileFullPackageName is null..");
			return null;
		}
		if (repository == null || git == null || revWalk == null) {
			System.err.println("git repo is null..");
			return null;
		}
		RevWalk walk = new RevWalk(repository);
		try {
			ObjectId objId = repository.resolve(revisionId);
			if (objId == null) {
				System.err.println("The revision:" + revisionId + " does not exist.");
				walk.close();
				return null;
			}
			RevCommit commit = walk.parseCommit(repository.resolve(revisionId));
			if (commit != null) {
				RevTree tree = commit.getTree();
				TreeWalk treeWalk = TreeWalk.forPath(repository, fileName, tree);
				ObjectId id = treeWalk.getObjectId(0);

				InputStream is = FileRWUtil.open(id, repository);
				return is;
			} else {
				System.err.println("Cannot found file(" + fileName + ") in revision(" + revisionId + "): " + revWalk);
			}
		} catch (RevisionSyntaxException e) {
			e.printStackTrace();
		} catch (MissingObjectException e) {
			e.printStackTrace();
		} catch (IncorrectObjectTypeException e) {
			e.printStackTrace();
		} catch (AmbiguousObjectException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		walk.close();
		return null;
	}


	public boolean filterRule(int flag,String oldPath){
		if (JGitCommand.ALL_FILE == flag) {
			return true;
		} else if (JGitCommand.JAVA_FILE == flag && oldPath.endsWith(".java")) {
			return true;
		} else if (JGitCommand.CORE_JAVA_FILE == flag && oldPath.startsWith("core/java/") && oldPath.endsWith(".java")) {
			return true;
		}
		return false;
	}

	
	public List<Ref> getAllBranches(){
		List<Ref> branches = null;
		try {
			branches = git.branchList().call();
		} catch (GitAPIException e) {
			e.printStackTrace();
		}
		return branches;
	}
	public static String stampToDate(Long s) {
		String res;
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date date = new Date(s);
		res = simpleDateFormat.format(date);
		return res;
	}
	
	public static String stampToDate(RevCommit rc) {
		Long s = rc.getCommitTime()*1000L;
		String res;
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date date = new Date(s);
		res = simpleDateFormat.format(date);
		return res;
	}
	
	public RevCommit revCommitOfBranchRef(Ref branch) {
		try {
			RevObject object;
			object = revWalk.parseAny(branch.getObjectId());
			if (object instanceof RevCommit) {
				RevCommit commit = (RevCommit) object;
				return commit;
			} else {
				System.err.println("invalid");
			}
			return null;
		} catch (MissingObjectException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}


}
