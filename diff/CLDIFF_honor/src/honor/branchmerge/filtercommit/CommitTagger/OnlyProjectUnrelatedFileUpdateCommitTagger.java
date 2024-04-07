package honor.branchmerge.filtercommit.CommitTagger;

import com.google.gson.Gson;
import honor.branchmerge.filtercommit.Config.FilterConfig;
import honor.branchmerge.filtercommit.Tag.CommitTag.OnlyProjectUnrelatedFileUpdateCommitTag;
import honor.branchmerge.filtercommit.util.JGitHelperPlus;
import org.eclipse.jgit.revwalk.RevCommit;

import java.util.List;
import java.util.function.Function;

public class OnlyProjectUnrelatedFileUpdateCommitTagger extends BaseCommitTagger
        implements IAttachTag<OnlyProjectUnrelatedFileUpdateCommitTag> {
    public static int tagProducedNum = 0;
    // xml is a file extension that have a huge impact.
    // the number of commits that only modify .xml is bigger than others
    public static List<String> TEXT_FILE_EXT;
    public static List<String> TEXT_FILE_UNRELATED;
    public static List<String> BINARY_FILE_EXT;

    public OnlyProjectUnrelatedFileUpdateCommitTagger(JGitHelperPlus cmd, FilterConfig config) {
        super(cmd, config);
        OnlyProjectUnrelatedFileUpdateCommitTagger.BINARY_FILE_EXT =
                this.config.only_project_unrelated_file_update.binaryFileSuffix;
        OnlyProjectUnrelatedFileUpdateCommitTagger.TEXT_FILE_EXT =
                this.config.only_project_unrelated_file_update.textFileSuffix;
        OnlyProjectUnrelatedFileUpdateCommitTagger.TEXT_FILE_UNRELATED =
                this.config.only_project_unrelated_file_update.unrelatedTextFile;
        System.out.println("OnlyProjectUnrelatedFileUpdateCommitTagger has set up.");
    }

    @Override
    public boolean isCommitNeedTag(RevCommit commit) {
        if (commit.getParents().length != 1) {
            // TODO merge 节点不提供任何 tag
            return false;
        }

        List<String> changeFiles = this.cmd.getChangeFilesByCommit(commit);
        if (changeFiles.size() == 0 || changeFiles.size() > 10000) {
            return false;
        }
        for (String filePath : changeFiles) {
            if (!isResourceFile(filePath)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public OnlyProjectUnrelatedFileUpdateCommitTag produceCommitTag(RevCommit commit) {
        OnlyProjectUnrelatedFileUpdateCommitTag tag = new OnlyProjectUnrelatedFileUpdateCommitTag();
        Gson gson = new Gson();

        OnlyProjectUnrelatedFileUpdateCommitTag.TagDescription description =
                new OnlyProjectUnrelatedFileUpdateCommitTag.TagDescription(commit.getShortMessage());

        tag.setCommitId(commit.getId());
        tag.setDescription(gson.toJson(description));
        tagProducedNum++;
        return tag;
    }

    private boolean isResourceFile(String filePath) {
        return isTextFile(filePath) || isUnrelatedTextFile(filePath) || isBinaryFile(filePath);
    }

    /**
     * 判断是不是文本文件
     * @param filePath
     * @return
     */
    private boolean isTextFile(String filePath) {
        return this.filterTemplate(
                OnlyProjectUnrelatedFileUpdateCommitTagger.TEXT_FILE_EXT, ext -> filePath.endsWith(ext));
    }

    /**
     * 判断是不是无关文本文件
     * @param filePath
     * @return
     */
    private boolean isUnrelatedTextFile(String filePath) {
        return this.filterTemplate(
                OnlyProjectUnrelatedFileUpdateCommitTagger.TEXT_FILE_UNRELATED, name -> filePath.equals(name));
    }

    /**
     * 判断是不是二进制文件
     * @param filePath
     * @return
     */
    private boolean isBinaryFile(String filePath) {
        return this.filterTemplate(
                OnlyProjectUnrelatedFileUpdateCommitTagger.BINARY_FILE_EXT, ext -> filePath.endsWith(ext));
    }

    /**
     * 过滤文件类型和进行文件匹配的模板，对于一系列规则进行相同方法（func）的匹配
     * @param rules
     * @param func
     * @return
     */
    private boolean filterTemplate(List<String> rules, Function<String, Boolean> func) {
        for (String elem : rules) {
            if (func.apply(elem)) {
                return true;
            }
        }
        return false;
    }
}
