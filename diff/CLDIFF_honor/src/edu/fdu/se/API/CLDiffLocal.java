package edu.fdu.se.API;

import edu.fdu.se.fileutil.FileRWUtil;
import edu.fdu.se.global.Constants;
import edu.fdu.se.global.Global;
import edu.fdu.se.fileutil.PathUtil;
import edu.fdu.se.git.IHandleCommit;
import edu.fdu.se.git.JGitHelper;
import edu.fdu.se.net.CommitFile;
import edu.fdu.se.net.Meta;
import honor.branchmerge.filtercommit.CommitTagger.IAttachTag;
import honor.branchmerge.filtercommit.Config.TopConfig;

import honor.branchmerge.filtercommit.Main;
import honor.branchmerge.filtercommit.TaggedRevCommit;
import honor.branchmerge.filtercommit.util.FilterCommitJsonOutputUtil;
import honor.branchmerge.filtercommit.util.FilterCommitUtil;
import honor.branchmerge.filtercommit.util.FilterConfigReader;
import honor.branchmerge.filtercommit.util.JGitHelperPlus;
import org.eclipse.jdt.internal.compiler.env.ISourceType;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.revwalk.RevCommit;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by huangkaifeng on 2018/8/23.
 * cmd entrance for API
 */
public class CLDiffLocal extends IHandleCommit {

    public JGitHelperPlus jGitHelper;
    public Meta meta;

    CLDiffAPI clDiffAPI;


    public CLDiffLocal(String repoPath) {
        jGitHelper = new JGitHelperPlus(repoPath);
    }
    /**
     * run diff / one commit
     * @param commitId
     * @param repo
     * @param outputDir
     */
    public void run(String commitId,String repo,String outputDir){
        try {
            Global.selfCommit = commitId;
            initMeta(repo, commitId, outputDir);
            jGitHelper.analyzeOneCommit(this, commitId);
            clDiffAPI = new CLDiffAPI(outputDir, meta);
            clDiffAPI.generateDiffMinerOutput();
        } catch (Exception e) {
            e.printStackTrace();
        }
        FileRWUtil.writeInAll(Global.outputDir + "/" + Global.fileOutputLog.projName +
                        "/diff"+Global.round+"__"+commitId.substring(0,8)+"_"+".json",
                Global.result.toString(4) );
    }

    public void run2(String commitId,String commitId2,String repo,String outputDir){
        try {
            initMeta(repo, commitId, outputDir);
            LinkedHashMap commits = jGitHelper.walkRepoFromBackwards(this,commitId,commitId2);
            if(commits.keySet().contains(commitId) || commitId.equals("test")){
                Object[] arr =commits.keySet().toArray();
                int count =0;
                boolean flag = false;
                for(Object ss : arr){
                    if(!flag){
                        if(!ss.equals(commitId)){
                            continue;
                        }
                        else {
                            flag = true;
                        }
                    }

                    if(ss == arr[arr.length-1]){
                        break;
                    }
                    if((ss).equals(commitId2)){
                        System.out.println("end in "+commitId2);
                        break;
                    }
                    initMeta(repo, (String)(ss), outputDir);
                    jGitHelper.analyzeOneCommit(this, (String)ss);
                    clDiffAPI = new CLDiffAPI(outputDir, meta);
                    clDiffAPI.generateDiffMinerOutput();
                    System.out.println((String) ss);
                    count++;
                }
            }
            else {
                System.out.println("commitId invalid");
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void run3(String commitId,String commitId2,String repo,String outputDir,String configFile){

        try {

            //List commitIdsAll = jGitHelper.test(commitId,commitId2);
            String repoPath = repo;
            String commitIdAndroidHigh = commitId2;
            String commitIdAndroidLow =commitId;
            String configPath = "C:\\Users\\Administrator\\Desktop\\filtercommit.config.json";
            String outputPath = "C:\\Users\\Administrator\\Desktop\\hhhhhh";
            Main.config = FilterConfigReader.read(configPath);
            JGitHelperPlus helperPlus = new JGitHelperPlus(repoPath);

            RevCommit cmBegin = helperPlus.getCommitById(commitIdAndroidHigh);
            RevCommit cmEnd = helperPlus.getCommitById(commitIdAndroidLow);
            List<RevCommit> commitsAll = helperPlus.getCommitInRange(cmBegin, cmEnd);
            int allRevCommitNum = commitsAll.size();
            FilterCommitUtil.removeMergeCommit(commitsAll);
            List<TaggedRevCommit> taggedRevCommits = FilterCommitUtil.make(commitsAll);
            int taggedRevCommitsNum = taggedRevCommits.size();

            List<IAttachTag> taggers = Main.taggerLoader(helperPlus, commitsAll);
            for (IAttachTag tagger : taggers) {
                tagger.addTagForCommits(taggedRevCommits);
            }

            int counter = 0;
            List<String> commitsNeed = new ArrayList<>();
            for (TaggedRevCommit taggedRevCommit : taggedRevCommits) {
                if (taggedRevCommit.getTags().size() > 0) {
                    //System.out.println(taggedRevCommit.getCommit().getId().getName());
                    counter++;
                }
                else{
                    commitsNeed.add(taggedRevCommit.getCommit().getId().getName());
                }
            }
            double tagCommitRate = (double) counter / (double) taggedRevCommitsNum;
            System.out.println("The number of all commits is:       " + allRevCommitNum);
            System.out.println("The number of non-merge commits is: " + taggedRevCommitsNum);
            System.out.println("The number of tagged commits is:    " + counter);
            System.out.println("The rate of taggedCommit is:        " + tagCommitRate);

            String result = FilterCommitJsonOutputUtil.genOutputJson(taggedRevCommits);
            FileWriter writer = new FileWriter(outputPath);
            writer.write(result);
            writer.close();
            System.out.println(commitsNeed.size());


            int commitsNeedNum = commitsNeed.size();
//            int round = 100;
//            int gap = Math.max(commitsNeedNum / round,1);
            for(int i = 0;i < commitsNeedNum;i ++){
                initMeta(repo, commitId, outputDir);
                String id= commitsNeed.get(i);
                System.out.println(id);
                Global.selfCommit = id;
                Global.round = i;
                jGitHelper.analyzeOneCommit(this, id);
                if(meta.getParents() == null) break;
                clDiffAPI = new CLDiffAPI(outputDir, meta);
                clDiffAPI.generateDiffMinerOutput();

            }
            FileRWUtil.writeInAll(Global.outputDir + "/" + Global.fileOutputLog.projName +
                            "/diff"+Global.round+"__"+commitId.substring(0,8)+"_"+commitId2.substring(0,8)+".json",
                    Global.result.toString(4) );
//            if(commitsNeedNum > round && commitsNeedNum % 20 != 0){
//                String id= commitsNeed.get(commitsNeedNum-1);
//                Global.selfCommit = id;
//                jGitHelper.analyzeOneCommit(this, (String)id);
//                clDiffAPI = new CLDiffAPI(outputDir, meta);
//                clDiffAPI.generateDiffMinerOutput();
//                System.out.println((String) id);
//            }
        }
        catch (Exception e){
            e.printStackTrace();
        }

    }

    public void run4(String tagHigh, String tagLow, String repo, String outputDir,String conf){
        try {
            //List commitIdsAll = jGitHelper.test(commitId,commitId2);
            String repoPath = repo;
            String configPath = conf;
            String outputPath = outputDir + Global.projectName + "/commits.txt";
            Main.config = FilterConfigReader.read(configPath);
            JGitHelperPlus helperPlus = new JGitHelperPlus(repoPath);
            RevCommit cmBegin = helperPlus.getCommitByTag(tagHigh);
            RevCommit cmEnd = helperPlus.getCommitByTag(tagLow);
            List<String> commitsNeed = new ArrayList<>();
            File file = new File(outputPath);
            if(file.exists()){
                List arr = FileRWUtil.getLines(file);
                String[] arr1= ((String)arr.get(0)).substring(1,((String) arr.get(0)).length()-1).replaceAll(" ","").split(",");
                commitsNeed = Arrays.asList(arr1);
                if(commitsNeed.size() == 1 && commitsNeed.get(0).equals(""))
                    return;
            }
            else{
                List<RevCommit> commitsAll = helperPlus.getCommitInRange(cmBegin, cmEnd);
                int allRevCommitNum = commitsAll.size();
                FilterCommitUtil.removeMergeCommit(commitsAll);
                List<TaggedRevCommit> taggedRevCommits = FilterCommitUtil.make(commitsAll);
                int taggedRevCommitsNum = taggedRevCommits.size();
                List<IAttachTag> taggers = Main.taggerLoader(helperPlus, commitsAll);
                for (IAttachTag tagger : taggers) {
                    tagger.addTagForCommits(taggedRevCommits);
                }

                int counter = 0;

                for (TaggedRevCommit taggedRevCommit : taggedRevCommits) {
                    if (taggedRevCommit.getTags().size() > 0) {
                        //System.out.println(taggedRevCommit.getCommit().getId().getName());
                        counter++;
                    }
                    else{
                        commitsNeed.add(taggedRevCommit.getCommit().getId().getName());
                    }
                }
                double tagCommitRate = (double) counter / (double) taggedRevCommitsNum;
                System.out.println("The number of all commits is:       " + allRevCommitNum);
                System.out.println("The number of non-merge commits is: " + taggedRevCommitsNum);
                System.out.println("The number of tagged commits is:    " + counter);
                System.out.println("The rate of taggedCommit is:        " + tagCommitRate);

                File directory = new File(outputDir + Global.projectName);
                if(!directory.exists()){
                    directory.mkdirs();
                }
                FileWriter writer = new FileWriter(outputPath);
                writer.write(commitsNeed.toString());
                writer.close();
                System.out.println(commitsNeed.size());
            }

            //String result = FilterCommitJsonOutputUtil.genOutputJson(taggedRevCommits);

            System.out.println("end filter");
            int commitsNeedNum = commitsNeed.size();
            for(int i = 0;i < commitsNeedNum;i ++){

                String id= commitsNeed.get(i);
                initMeta(repo, id, outputDir);
                System.out.println(id);
                Global.selfCommit = id;
                jGitHelper.analyzeOneCommit(this, (String)id);
                if(meta.getParents() == null){
                    break;
                }
                if(meta.getFiles() == null){
                    continue;
                }
                clDiffAPI = new CLDiffAPI(outputDir, meta);
                clDiffAPI.generateDiffMinerOutput();

            }
            if(commitsNeed.size() > 0){
                FileRWUtil.writeInAll(Global.outputDir + "/" + Global.fileOutputLog.projName +
                                "/diff"+ "__" + cmBegin.getName().substring(0,8) + "_" + cmEnd.getName().substring(0,8)+".json",
                        Global.result.toString(4) );
            }

        }
        catch (Exception e){
            e.printStackTrace();
        }
    }


    /**
     * run diff / two commits
     * @param currCommitId
     * @param nextCommitId
     * @param repo
     * @param outputDir
     */
    public void run(String currCommitId, String nextCommitId,String repo,String outputDir){
        try {
            initMeta(repo, nextCommitId, outputDir);
            meta.addParentCommit(currCommitId);
            jGitHelper.analyzeTwoCommits(this, currCommitId, nextCommitId);
            Global.selfCommit = currCommitId;
            clDiffAPI = new CLDiffAPI(outputDir, meta);
            clDiffAPI.generateDiffMinerOutput();
        } catch (Exception e) {
            e.printStackTrace();
        }
        FileRWUtil.writeInAll(Global.outputDir + "/" + Global.fileOutputLog.projName +
                        "/diff"+"_"+currCommitId.substring(8)+"__"+nextCommitId.substring(8)+".json",
                Global.result.toString(4) );
    }

    public void initMeta(String repo,String commitId,String outputDir){
        meta = new Meta();
        meta.setCommit_hash(commitId);
        meta.setProject_name(PathUtil.getGitProjectNameFromGitFullPath(repo));
        meta.setActions(null);
        meta.setAuthor(null);
        meta.setCommit_log(null);
        meta.setCommitter(null);
        meta.setDate_time(null);
        meta.setOutputDir(outputDir+'/'+PathUtil.getGitProjectNameFromGitFullPath(repo)+'/'+commitId);
        meta.setLinkPath(Constants.LINK_JSON);
        Global.mmeta = meta;

    }

    public void loadCommitMeta(String author,int timeSeconds,String committer,String commitLog){
        meta.setAuthor(author);
        meta.setCommitter(committer);
        meta.setCommit_log(commitLog);
        Calendar c=Calendar.getInstance();
        long millions=new Long(timeSeconds).longValue()*1000;
        c.setTimeInMillis(millions);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateString = sdf.format(c.getTime());
        meta.setDate_time(dateString);
    }

    @Override
    public void handleCommit(Map<String, List<DiffEntry>> changedFiles, String commitId, RevCommit commit){
        if (commit != null) {
            loadCommitMeta(commit.getAuthorIdent().getName(), commit.getCommitTime(), commit.getCommitterIdent().getName(), commit.getShortMessage() + "\n\n\n" + commit.getFullMessage());
        }
        int cnt = 0;
        for (Map.Entry<String, List<DiffEntry>> entry : changedFiles.entrySet()) {
            String parentCommitId = entry.getKey();
            List<DiffEntry> diffEntryList = entry.getValue();
            cnt = processParentCommitPair(cnt, diffEntryList, parentCommitId, commitId);
        }
        if (commit == null) {
            try {
                clDiffAPI.generateDiffMinerOutput();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void handleCommit(Map<String, List<DiffEntry>> changedFiles, String prevCommitId, RevCommit prevCommit, String currCommitId, RevCommit currCommit) {

        loadCommitMeta(currCommit.getAuthorIdent().getName(), currCommit.getCommitTime(),
                currCommit.getCommitterIdent().getName(), currCommit.getShortMessage() + "\n\n\n" + currCommit.getFullMessage());
        int cnt = 0;
        meta.addParentCommit(prevCommitId);
        List<DiffEntry> changedFileEntry = changedFiles.get(prevCommitId);
        processParentCommitPair(cnt, changedFileEntry, prevCommitId, currCommitId);


    }

    private int processParentCommitPair(int cnt, List<DiffEntry> diffEntryList, String prevCommitId, String currCommitId) {
        meta.addParentCommit(prevCommitId);
        for (DiffEntry de : diffEntryList) {
            if (de.getChangeType() == DiffEntry.ChangeType.MODIFY) {
                String file = de.getNewPath();
                boolean isFiltered = CLDiffCore.isFilter(file);
                addCommitFileInfo(cnt, prevCommitId, currCommitId, file, isFiltered, Constants.ChangeTypeString.MODIFY);
                meta.addAction(Constants.ChangeTypeString.MODIFY);
                cnt += 1;
            } else if (de.getChangeType() == DiffEntry.ChangeType.ADD) {
                String file = de.getNewPath();
                boolean isFiltered = CLDiffCore.isFilter(file);
                addCommitFileInfo(cnt, prevCommitId, currCommitId, file, isFiltered, Constants.ChangeTypeString.ADD);
                meta.addAction(Constants.ChangeTypeString.ADD);
                cnt += 1;
            } else if (de.getChangeType() == DiffEntry.ChangeType.DELETE) {
                String file = de.getOldPath();
                boolean isFiltered = CLDiffCore.isFilter(file);
                addCommitFileInfo(cnt, prevCommitId, prevCommitId, file, isFiltered, Constants.ChangeTypeString.DELETE);
                cnt += 1;
                meta.addAction(Constants.ChangeTypeString.DELETE);
            }
        }
        return cnt;

    }


    public void addCommitFileInfo(int cnt, String parentCommitId, String commitId, String filePackagePath, boolean isFiltered, String changeTypeString) {
        try {
            CommitFile commitFile = null;
            String fileShortName = PathUtil.getFileShortNameFromPackagePath(filePackagePath);
            if (Constants.ChangeTypeString.ADD.equals(changeTypeString)) {
                commitFile = new CommitFile(cnt,filePackagePath,fileShortName,false,true,parentCommitId);
                extractToLocalPath(filePackagePath,commitFile.getCurr_file_path(), commitId);

            } else if (Constants.ChangeTypeString.DELETE.equals(changeTypeString)) {
                commitFile = new CommitFile(cnt,filePackagePath,fileShortName,true,false,parentCommitId);
                extractToLocalPath(filePackagePath,commitFile.getPrev_file_path(), parentCommitId);

            } else if (Constants.ChangeTypeString.MODIFY.equals(changeTypeString)) {
                commitFile = new CommitFile(cnt,filePackagePath,fileShortName,true,true,parentCommitId);
                extractToLocalPath(filePackagePath,commitFile.getCurr_file_path(), commitId);
                extractToLocalPath(filePackagePath,commitFile.getPrev_file_path(), parentCommitId);

            }

            if (!isFiltered) {
                commitFile.setDiffPath(true);
            }
            meta.addFile(commitFile);
        }catch (Exception e){
            e.printStackTrace();
        }

    }


    private Path extractToLocalPath(String filePackagePath, String fileRealPath,String extractId) {
        try {
            byte[] file = jGitHelper.extract(filePackagePath, extractId);
            Path filePath = Paths.get(meta.getOutputDir() + "/" + fileRealPath);
            if (!filePath.toFile().exists()) {
                int index2 = filePath.toFile().getAbsolutePath().lastIndexOf(File.separator);
                String prevDir = filePath.toFile().getAbsolutePath().substring(0, index2);
                File prev = new File(prevDir);
                if (!prev.exists()) {
                    prev.mkdirs();
                }
            }
            Files.write(filePath, file);
            return filePath;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }





}

