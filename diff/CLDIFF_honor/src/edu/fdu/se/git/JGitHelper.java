package edu.fdu.se.git;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import edu.fdu.se.global.Constants;
import edu.fdu.se.global.Global;
import edu.fdu.se.API.CLDiffCore;
import org.eclipse.jgit.api.ListBranchCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.diff.EditList;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;

import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.TreeWalk;

/**
 * Created by huangkaifeng on 2018/4/6.
 *
 */
public class JGitHelper extends JGitCommand {

    public JGitHelper(String repopath) {
        super(repopath);
    }


    public List<String> rangeCommits(String commitId1, String commitId2) throws Exception {
        ObjectId commitIdStart = ObjectId.fromString(commitId1);
        ObjectId commitIdEnd = ObjectId.fromString(commitId2);
//        RevCommit commit = revWalk.parseCommit(commitId);
        Iterable<RevCommit> iter = git.log().addRange(commitIdStart,commitIdEnd).call();
        Iterator iterator = iter.iterator();
        List<String> commitsIdList = new ArrayList();
        while (iterator.hasNext()){
            commitsIdList.add(((RevCommit)iterator.next()).getName());

        }
        return commitsIdList;
    }

    public void test(){
        System.out.println(git.tag());
    }


    /**
     * 输出output即可
     */
    public LinkedHashMap walkRepoFromBackwards(IHandleCommit iHandleCommit,String commitId1,String commitId2) {
        Map<String, Boolean> isTraversed = new LinkedHashMap<>();
        try {
            int commitNum = 0;
            Queue<RevCommit> commitQueue = new LinkedList<>();
           // List<Ref> mList = this.git.branchList().setListMode(ListBranchCommand.ListMode.ALL).call();
            ObjectId commitId = ObjectId.fromString(commitId1);
            RevCommit commit = revWalk.parseCommit(commitId);

                commitQueue.offer(commit);
                jump:
                while (commitQueue.size() != 0) {
                    RevCommit queueCommitItem = commitQueue.poll();
                    RevCommit[] parentCommits = queueCommitItem.getParents();
                    if (isTraversed.containsKey(queueCommitItem.getName()) || parentCommits == null) {
                        continue;
                    }
                    // Map<String, List<DiffEntry>> changedFiles = this.getCommitParentMappedDiffEntry(queueCommitItem.getName());
                    // iHandleCommit.handleCommit(changedFiles, queueCommitItem.getName(), null);
                    commitNum++;
                    isTraversed.put(queueCommitItem.getName(), true);
                    for (RevCommit item2 : parentCommits) {
                        RevCommit commit2 = revWalk.parseCommit(item2.getId());
                        System.out.println(commit2.getName());
                        if(commit2.getName().equals(commitId2)){
                            break jump;
                        }
                        commitQueue.offer(commit2);
                    }
                }

        } catch (MissingObjectException e) {
            e.printStackTrace();
        } catch (IncorrectObjectTypeException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }

        return (LinkedHashMap) isTraversed;
    }
    public static void quickCreate(File file) {
        File dir = file.getParentFile();
        if (!dir.exists()) {
            dir.mkdirs();
        }
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private Map<String,List<DiffEntry>> doTraverse(String commmitid){
        Map<String, List<DiffEntry>> result = new HashMap<>();
        ObjectId commitId = ObjectId.fromString(commmitid);
        RevCommit commit = null;
        try {
            commit = revWalk.parseCommit(commitId);
            RevTree tree = commit.getTree();
            TreeWalk treeWalk = new TreeWalk(repository);
            treeWalk.addTree(tree);
            treeWalk.setRecursive(false);
            while (treeWalk.next()) {
                if (treeWalk.isSubtree()) {
//                    System.out.println("dir: " + treeWalk.getPathString());
                    File file = new File(Global.repository + Constants.SaveFilePath.CURR + "/" + treeWalk.getPathString());
                    if(!file.exists()){
                        file.mkdirs();
                    }
                    treeWalk.enterSubtree();
                } else {
                    File file = new File(Global.repository + Constants.SaveFilePath.CURR + "/" + treeWalk.getPathString());
                    quickCreate(file);
                    byte[] content = this.extract(treeWalk.getPathString(),commmitid);
                    Files.write(Paths.get(Global.repository + Constants.SaveFilePath.CURR + "/" + treeWalk.getPathString()), content);
//                    System.out.println("file: " + treeWalk.getPathString());
                }
            }
            RevCommit parentsCommit = commit.getParents()[0];
            commmitid = parentsCommit.toString().split(" ")[1];
            tree = commit.getTree();
            treeWalk = new TreeWalk(repository);
            treeWalk.addTree(tree);
            treeWalk.setRecursive(false);
            while (treeWalk.next()) {
                if (treeWalk.isSubtree()) {
//                    System.out.println("dir: " + treeWalk.getPathString());
                    File file = new File(Global.repository + Constants.SaveFilePath.PREV + "/" + treeWalk.getPathString());
                    if(!file.exists()){
                        file.mkdirs();
                    }
                    treeWalk.enterSubtree();
                } else {
                    File file = new File(Global.repository + Constants.SaveFilePath.PREV + "/" + treeWalk.getPathString());
                    quickCreate(file);
                    byte[] content = this.extract(treeWalk.getPathString(),commmitid);
                    Files.write(Paths.get(Global.repository + Constants.SaveFilePath.PREV + "/" + treeWalk.getPathString()), content);
//                    System.out.println("file: " + treeWalk.getPathString());
                }
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
     * 输出debug的文件
     *
     * @param commitString
     */
    public void analyzeOneCommit(IHandleCommit iHandleCommit, String commitString) {
        try {

            ObjectId commitId = ObjectId.fromString(commitString);
            RevCommit commit = revWalk.parseCommit(commitId);
            if (commit.getParents() == null) {
                return;
            }
            Map<String,List<DiffEntry>> changedFiles = this.getCommitParentMappedDiffEntry(commit.getName());
            iHandleCommit.handleCommit(changedFiles, commitString,commit);
        } catch (MissingObjectException e) {
            e.printStackTrace();
        } catch (IncorrectObjectTypeException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public RevCommit getCommitById(String commitId) {
        try (RevWalk walk = new RevWalk(this.repository)) {
            return walk.parseCommit(this.repository.resolve(commitId));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    /**
     * 输出debug的文件
     *
     * @param currentCommitString,nexCommitString
     */
    public void analyzeTwoCommits(IHandleCommit handleDiffCommits, String currentCommitString, String nexCommitString) {
        try {


            ObjectId currCommitId = ObjectId.fromString(currentCommitString);
            RevCommit currCommit = getCommitById(currentCommitString);
            ObjectId nextCommitId = ObjectId.fromString(nexCommitString);
            RevCommit nextCommit = getCommitById(nexCommitString);
            Map<String, List<DiffEntry>> changedFiles = this.getTwoCommitsMappedFileList(currCommit.getName(),nextCommit.getName());
            handleDiffCommits.handleCommit(changedFiles, currentCommitString,currCommit,nexCommitString,nextCommit);

        } catch (Exception e){
            e.printStackTrace();
        }
    }


    /**
     * 输出output即可
     */
    public void walkRepoFromBackwardsCountLineNumber(IHandleCommit iHandleCommit) {
        try {
            int commitNum = 0;
            Queue<RevCommit> commitQueue = new LinkedList<>();
            Map<String, Boolean> isTraversed = new HashMap<>();
            long startTime = System.nanoTime();   //获取开始时间
            List<Ref> mList = this.git.branchList().setListMode(ListBranchCommand.ListMode.ALL).call();
            for (Ref item : mList) {
                RevCommit commit = revWalk.parseCommit(item.getObjectId());
                commitQueue.offer(commit);
                while (commitQueue.size() != 0) {
                    RevCommit queueCommitItem = commitQueue.poll();

                    RevCommit[] parentCommits = queueCommitItem.getParents();
                    if (isTraversed.containsKey(queueCommitItem.getName()) || parentCommits == null) {
                        continue;
                    }
                    int temp2 = getCommitFileEditLineNumber(queueCommitItem);
//                    totalChangedLineNumber += temp2;
                    if(temp2==1) {
                        commitNum++;
                    }
                    isTraversed.put(queueCommitItem.getName(), true);
                    for (RevCommit item2 : parentCommits) {
                        RevCommit commit2 = revWalk.parseCommit(item2.getId());
                        commitQueue.offer(commit2);
                    }
                }
            }
            long endTime = System.nanoTime(); //获取结束时间
//            System.out.println("CommitSum:" + isTraversed.size());
//            System.out.println("totalCommitNum: " + commitNum);
            Global.logger.info("totalChangedLineNumber: " + totalChangedLineNumber);
            Global.logger.info("----total time:" + (endTime - startTime));
            Global.logger.info("----commitnum " + commitNum);
        } catch (MissingObjectException e) {
            e.printStackTrace();
        } catch (IncorrectObjectTypeException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (GitAPIException e1) {
            e1.printStackTrace();
        }
    }

    private long totalChangedLineNumber;




    public int getCommitFileEditLineNumber(RevCommit commit) {
        try {
            Global.logger.info("----commit id:" + commit.getName());
            int count = 0;
            long startTime = System.nanoTime();   //获取开始时间
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
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                DiffFormatter diffFormatter = new DiffFormatter(out);
                diffFormatter.setRepository(git.getRepository());
                List<DiffEntry> entries = diffFormatter.scan(oldTreeIter, newTreeIter);
                diffFormatter.setContext(0);
                for (DiffEntry entry : entries) {
                    switch (entry.getChangeType()) {
                        case MODIFY:
                            String mOldPath = entry.getOldPath();
                            if (CLDiffCore.isFilter(mOldPath)) {
                                continue;
                            }else{
                                //at least one file is changed
                                return 1;
                            }
//                            FileHeader fileHeader = diffFormatter.toFileHeader(entry);
//                            EditList editList = fileHeader.toEditList();
//                            int temp = lineNumber(editList);
//                            count += temp;
//                            System.out.println("----"+mOldPath.substring(mOldPath.lastIndexOf("/") + 1) + " " + count);
//                            diffFormatter.format(entry);
//                            out.reset();
//                            break;
                        case ADD:
                        case DELETE:
                        default:
                            break;
                    }
                }
                diffFormatter.close();
            }
            long endTime = System.nanoTime(); //获取结束时间
            Global.logger.info("----one commit time:" + (endTime - startTime));
            return 0;
        } catch (MissingObjectException e) {
            e.printStackTrace();
        } catch (IncorrectObjectTypeException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int lineNumber(EditList edits) {
        int cnt = 0;
        for (Edit e : edits) {
            if (e.getBeginA() == e.getEndA()) {
                cnt += e.getEndB() - e.getBeginB();

            } else if (e.getBeginB() == e.getEndB()) {
                cnt += e.getEndA() - e.getBeginA();
            } else if (e.getBeginA() < e.getEndA() && e.getBeginB() < e.getEndB()) {
                cnt += e.getEndB() - e.getBeginB() + e.getEndA() - e.getBeginA();
            }
        }
        return cnt;
    }

}
