package edu.fdu.se.lang.common;

import com.github.gumtreediff.actions.model.Action;
import com.github.gumtreediff.tree.ITree;
import com.github.gumtreediff.tree.Tree;
import edu.fdu.se.core.miningactions.bean.MiningActionData;
import edu.fdu.se.core.miningchangeentity.base.ChangeEntity;
import edu.fdu.se.core.preprocessingfile.FilePairPreDiff;
import edu.fdu.se.core.preprocessingfile.data.BodyDeclarationPair;
import edu.fdu.se.core.preprocessingfile.data.PreCacheData;
import edu.fdu.se.core.preprocessingfile.data.PreCacheTmpData;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import java.util.List;
import java.util.Map;

public interface ProcessUtil {

//
//    /**
//     * preprocess
//     */
//    void preProcess(PreCacheTmpData tempData);

    void removeAllComments(PreCacheTmpData tempData, Object o, List<Integer> lineList, int treeType,PreCacheData preCacheData);

    int compareTwoFile(FilePairPreDiff preDiff, PreCacheTmpData tempData, PreCacheData data);

    void handleMethodNameModification(ChangeEntity changeEntity,ChangeEntity tempCE);

    String methodDeclarationToString(Object methodDeclaration);


    String bodyDeclarationToString(Object pair);

    /**
     * grouping
     */
    int matchEntityTopDown(MiningActionData fp, Action a, int type, String granularity);


    void matchEntityBottomUpNew(MiningActionData fp, Action a, Tree queryFather, int treeType, Tree traverseFather, List<Integer> commonRootNodeTypes);

    void matchEntityBottomUpCurr(MiningActionData fp, Action a, ChangeEntity changeEntity, int nodeType, Tree traverseFather);

    void matchBlock(MiningActionData fp, Action a, int type, Tree fatherNode);

    Map<Integer, String> filterByGranularity(String granularity, Map<Integer, String> mMap);

    /**
     * link
     */
    void initDefs(ChangeEntity ce, MiningActionData mad);

    /**
     * 为方法添加相关信息，
     * 例如：CanonicalName、返回类型、方法名、参数列表、lineRange、文件所属类型
     * @param ce
     * @param md
     */
    void addMethodDef(ChangeEntity ce, Object md);

    /**
     * 获取 fieldDeclaration 中的变量列表，并加入 defList
     */
    void addFieldDef(ChangeEntity ce, Object fd);

    /**
     * VariableDeclarationStatement
     * 以字符串的形式将 对象 加入 defList
     * java ： VariableDeclarationFragment、
     * c :
     * @param ce
     * @param vds
     */
    void addVarDeclarationDef(ChangeEntity ce, Object vds);

    /**
     * 对 td 处理，得到 类名、是否为抽象类、父类名、父接口名
     * 将这些内容封装到 ce.linkBean.inheritance
     * @param ce
     * @param td
     */
    void initInheritance(ChangeEntity ce, Object td);
}
