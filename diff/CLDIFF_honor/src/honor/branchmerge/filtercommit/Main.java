package honor.branchmerge.filtercommit;

import honor.branchmerge.filtercommit.CommitTagger.*;
import honor.branchmerge.filtercommit.Config.TopConfig;
import honor.branchmerge.filtercommit.util.FilterCommitJsonOutputUtil;
import honor.branchmerge.filtercommit.util.FilterCommitUtil;
import honor.branchmerge.filtercommit.util.FilterConfigReader;
import honor.branchmerge.filtercommit.util.JGitHelperPlus;
import org.eclipse.jgit.revwalk.RevCommit;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Main {
    private String repoPath = "D:\\tmp\\Telephony\\.git";
    private String tagAndroidHighDefault = "refs/tags/android-11.0.0_r1";
    private String tagAndroidLowDefault = "refs/tags/android-12.0.0_r1";
    private String commitIdAndroidHighDefault = "7be6599cbdfecbe0c01438478492deb015846fa8";
    private String commitIdAndroidLowDefault = "2e349325beda37f33d2c1e29b152cabee4e62661";
    private static String configPathDefault = "src/main/java/honor/branchmerge/filtercommit/filtercommit.config.json";
    public static TopConfig config;

    /**
     * @param args 0 => repo.git path
     *             1 => high tag
     *             2 => low tag
     *             3 => configPath
     */
    public static void main(String[] args) throws IOException {
        String repoPath = "C:\\Users\\Administrator\\Desktop\\test\\aosp12_frameworks\\base\\.git";
        String commitIdAndroidHigh = "cebf5c06997b64f4e47a1611edb5f97044509d76";
        String commitIdAndroidLow ="f5184a65fa134718444376ff8eb7944717990a3a";
        String configPath = "C:\\Users\\Administrator\\Desktop\\filtercommit.config.json";
        String outputPath = "C:\\Users\\Administrator\\Desktop\\hhhhhh";

        config = FilterConfigReader.read(configPath);
        JGitHelperPlus helperPlus = new JGitHelperPlus(repoPath);

        RevCommit cmBegin = helperPlus.getCommitById(commitIdAndroidHigh);
        RevCommit cmEnd = helperPlus.getCommitById(commitIdAndroidLow);
        List<RevCommit> commits = helperPlus.getCommitInRange(cmBegin, cmEnd);
        int allRevCommitNum = commits.size();

        FilterCommitUtil.removeMergeCommit(commits);
        List<TaggedRevCommit> taggedRevCommits = FilterCommitUtil.make(commits);
        int taggedRevCommitsNum = taggedRevCommits.size();

        List<IAttachTag> taggers = Main.taggerLoader(helperPlus, commits);
        for (IAttachTag tagger : taggers) {
            tagger.addTagForCommits(taggedRevCommits);
        }

        int counter = 0;
        List<String> commitIds = new ArrayList<>();
        for (TaggedRevCommit taggedRevCommit : taggedRevCommits) {
            if (taggedRevCommit.getTags().size() > 0) {
                System.out.println(taggedRevCommit.getCommit().getId().getName());
                counter++;
            }
            else{
                commitIds.add(taggedRevCommit.getCommit().getId().getName());
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
    }

    /**
     * 根据配置文件装载需要的 tagger
     * @param helperPlus
     * @param commits
     * @return
     * @throws IOException
     */
    public static List<IAttachTag> taggerLoader(JGitHelperPlus helperPlus, List<RevCommit> commits)
            throws IOException {
        List<IAttachTag> taggers = new ArrayList<>();
        if (config.filter.only_comment_update != null) {
            taggers.add(new OnlyCommentUpdateCommitTagger(helperPlus, config.filter, commits));
        }
        if (config.filter.only_javadoc_update != null) {
            taggers.add(new OnlyJavadocUpdateCommitTagger(helperPlus, config.filter, commits));
        }
        if (config.filter.only_test_update != null) {
            taggers.add(new OnlyTestUpdateCommitTagger(helperPlus, config.filter));
        }
        if (config.filter.only_project_unrelated_file_update != null) {
            taggers.add(new OnlyProjectUnrelatedFileUpdateCommitTagger(helperPlus, config.filter));
        }
        if (config.filter.revert_patch != null) {
            taggers.add(new RevertPatchCommitTagger(helperPlus, config.filter, commits));
        }
        if (config.filter.non_existence_in_final != null) {
            if(commits.size()>0)
                taggers.add(new NonExistenceInFinalCommitTagger(helperPlus, config.filter, commits.get(0)));
        }
        if (config.filter.redundant_patch != null) {
            taggers.add(new RedundantPatchCommitTagger(helperPlus, config.filter, commits));
        }
        if (config.filter.only_tool_update != null) {
            taggers.add(new OnlyToolUpdateCommitTagger(helperPlus, config.filter));
        }
        return taggers;
    }
}
