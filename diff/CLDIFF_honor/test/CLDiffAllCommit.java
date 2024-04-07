import edu.fdu.se.API.CLDiffAPI;
import edu.fdu.se.API.CLDiffCore;
import edu.fdu.se.fileutil.PathUtil;
import edu.fdu.se.git.IHandleCommit;
import edu.fdu.se.git.JGitHelper;
import edu.fdu.se.global.Constants;
import edu.fdu.se.global.Global;
import edu.fdu.se.net.CommitFile;
import edu.fdu.se.net.Meta;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.revwalk.RevCommit;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

/**
 * Created by huangkaifeng on 2018/8/23.
 * cmd entrance for API
 */
@SuppressWarnings("Duplicates")
public class CLDiffAllCommit extends IHandleCommit {

    public JGitHelper jGitHelper;
    public Meta meta;
    CLDiffAPI  clDiffAPI;

    public CLDiffAllCommit(String repoPath) {
        jGitHelper = new JGitHelper(repoPath);
    }


    public void runAll(String repo, String outputDir) {
        clDiffAPI = new CLDiffAPI(outputDir, meta);
        jGitHelper.walkRepoFromBackwards(this);
    }

    @Override
    public void handleCommit(Map<String, List<DiffEntry>> changedFiles, String commitId, RevCommit commit){
        int cnt = 0;
        for (Map.Entry<String, List<DiffEntry>> entry : changedFiles.entrySet()) {
            String parentCommitId = entry.getKey();
            List<DiffEntry> diffEntryList = entry.getValue();
            cnt = processParentCommitPair(cnt, diffEntryList, parentCommitId, commitId);
        }
        try {
            clDiffAPI.generateDiffMinerOutput();
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    private int processParentCommitPair(int cnt, List<DiffEntry> diffEntryList, String prevCommitId, String currCommitId) {
        meta.addParentCommit(prevCommitId);
        for (DiffEntry de : diffEntryList) {
            String changeType = Constants.getChangeTypeString(de.getChangeType());
            if (de.getChangeType() == DiffEntry.ChangeType.MODIFY) {
                String file = de.getNewPath();
                boolean isFiltered = CLDiffCore.isFilter(file);
                setCommitFileInfo(cnt, prevCommitId, currCommitId, file, isFiltered, changeType);
                meta.addAction(Constants.ChangeTypeString.MODIFY);
                cnt += 1;
            } else if (de.getChangeType() == DiffEntry.ChangeType.ADD) {
                String file = de.getNewPath();
                boolean isFiltered = CLDiffCore.isFilter(file);
                setCommitFileInfo(cnt, prevCommitId, currCommitId, file, isFiltered, changeType);
                meta.addAction(Constants.ChangeTypeString.ADD);
                cnt += 1;
            } else if (de.getChangeType() == DiffEntry.ChangeType.DELETE) {
                String file = de.getOldPath();
                boolean isFiltered = CLDiffCore.isFilter(file);
                setCommitFileInfo(cnt, prevCommitId, prevCommitId, file, isFiltered, changeType);
                cnt += 1;
                meta.addAction(Constants.ChangeTypeString.DELETE);
            }
        }
        return cnt;

    }


    public void setCommitFileInfo(int cnt, String parentCommitId, String commitId, String filePackagePath, boolean isFiltered, String changeTypeString) {
        try {
            CommitFile commitFile = null;
            Path path = null;
            String fileShortName = path.getFileName().toString();
            DiffEntry.ChangeType changeType = Constants.getChangeTpye(changeTypeString);
            if (DiffEntry.ChangeType.ADD.equals(changeType)) {
                commitFile = new CommitFile(cnt,filePackagePath,fileShortName,false,true,parentCommitId);
                path = extractToLocalPath(commitFile.getFile_full_name(),commitFile.getCurr_file_path(), commitId);

            } else if (DiffEntry.ChangeType.DELETE.equals(changeType)) {
                commitFile = new CommitFile(cnt,filePackagePath,fileShortName,true,false,parentCommitId);
                path = extractToLocalPath(commitFile.getFile_full_name(),commitFile.getPrev_file_path(), parentCommitId);

            } else if (DiffEntry.ChangeType.MODIFY.equals(changeType)) {
                commitFile = new CommitFile(cnt,filePackagePath,fileShortName,true,true,parentCommitId);
                extractToLocalPath(commitFile.getFile_full_name(),commitFile.getPrev_file_path(), parentCommitId);
                path = extractToLocalPath(commitFile.getFile_full_name(),commitFile.getCurr_file_path(), commitId);
            }

            if (!isFiltered) {
                commitFile.setDiffPath(true);
            }
            meta.addFile(commitFile);
        }catch (Exception e){
            e.printStackTrace();
        }

    }


    private Path extractToLocalPath(String filePackagePath,String fileRealPath, String extractId) {
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

