package edu.fdu.se.core.preprocessingfile.data;


import com.github.gumtreediff.actions.model.Delete;
import com.github.gumtreediff.actions.model.Insert;
import edu.fdu.se.core.links.generator.ChangeEntityTree;
import edu.fdu.se.core.miningactions.bean.MyRange;
import edu.fdu.se.core.miningchangeentity.base.ChangeEntity;
import edu.fdu.se.core.miningchangeentity.base.ChangeEntityDesc;
import edu.fdu.se.core.miningchangeentity.member.ClassChangeEntity;
import edu.fdu.se.fileutil.FileRWUtil;
import edu.fdu.se.global.Constants;
import edu.fdu.se.global.Global;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;


import java.util.*;

/**
 * Created by huangkaifeng on 2018/1/16.
 */
public class PreCacheData {

    /**
     * AST Compilation Unit
     */

    private Object currCu;
    private Object prevCu;

    /**
     * TypeDeclaration
     */
    private List<Object> currTypeDeclaration;
    private List<Object> prevTypeDeclaration;


    /**
     * String content of prev/curr file
     */
    private String fullStringPrev;

    public String getFullStringPrev() {
        return fullStringPrev;
    }

    public String getFullStringCurr() {
        return fullStringCurr;
    }

    private String fullStringCurr;
    /**
     * list line representation of prev/curr file
     */
    private List<String> currLines;
    private List<String> prevLines;
    /**
     * line number,  negative for not visible.
     */
    private List<Integer> currLineNums;
    private List<Integer> prevLineNums;


    /**
     * field name only in prev
     */
    private Set<String> prevFieldNames;
    /**
     * field name only in curr
     */
    private Set<String> currFieldNames;

    /**
     * field name in both.
     */
    private Set<String> prevCurrFieldNames;

    /**
     * curr 删除的added的body
     */
    private List<BodyDeclarationPair> mBodiesAdded;
    private Map<BodyDeclarationPair, Object> addBodiesTypeDeclration;
    /**
     * prev 删除的removed body
     */
    private List<BodyDeclarationPair> mBodiesDeleted;
    private Map<BodyDeclarationPair, Object> deletedBodiesTypeDeclration;

    /**
     * curr 删除的Field
     */
    private List<Object> mCurrFields;
    /**
     * prev 删除的field
     */
    private List<Object> mPrevFields;

    /**
     * item 和 item的Parent
     */
    private Map<Object,Object> deletionKVMap;


    private List<ChangeEntity> preChangeEntity;

    private ChangeEntityTree entityTree;

    private List<String> staticImports;


    public PreCacheData() {
        mBodiesAdded = new ArrayList<>();
        mBodiesDeleted = new ArrayList<>();
        addBodiesTypeDeclration = new HashMap<>();
        deletedBodiesTypeDeclration = new HashMap<>();
        entityTree = new ChangeEntityTree();
        prevFieldNames = new HashSet<>();
        currFieldNames = new HashSet<>();
        prevCurrFieldNames = new HashSet<>();
        currTypeDeclaration = new ArrayList<>();
        prevTypeDeclaration = new ArrayList<>();
        deletionKVMap = new HashMap<>();
        staticImports = new ArrayList<>();
    }

    public PreCacheData(Object compilationUnit, Object pathContent, int treeType, Object typeDeclaration) {
        if (ChangeEntityDesc.TreeType.PREV_TREE_NODE == treeType) {
            this.prevCu = compilationUnit;
            this.prevLines = new ArrayList<>();
            this.fullStringPrev = FileRWUtil.getLinesOfFile(pathContent, this.prevLines);
            this.prevLineNums = FileRWUtil.getLinesList(prevLines.size());
            this.prevTypeDeclaration = new ArrayList<>();
            this.prevTypeDeclaration.add(typeDeclaration);
        } else {
            this.currCu = compilationUnit;
            this.currLines = new ArrayList<>();
            this.fullStringCurr = FileRWUtil.getLinesOfFile(pathContent, this.currLines);
            this.currLineNums = FileRWUtil.getLinesList(currLines.size());
            this.currTypeDeclaration = new ArrayList<>();
            this.currTypeDeclaration.add(typeDeclaration);
        }
        staticImports = new ArrayList<>();
    }

    public void initPreprocessChangeEntity() {
        if (this.preChangeEntity == null) {
            this.preChangeEntity = new ArrayList<>();
        }
    }

    public List<String> getStaticImports() {
        return staticImports;
    }

    public void setStaticImports(List<String> staticImports) {
        this.staticImports = staticImports;
    }

    public List<Integer> getCurrLineNums() {
        return currLineNums;
    }

    public List<Integer> getPrevLineNums() {
        return prevLineNums;
    }

    public Set<String> getPrevFieldNames() {
        return prevFieldNames;
    }

    public Set<String> getCurrFieldNames() {
        return currFieldNames;
    }

    public Set<String> getPrevCurrFieldNames() {
        return prevCurrFieldNames;
    }

    public Object getCurrCu() {
        return currCu;
    }

    public Object getPrevCu() {
        return prevCu;
    }

    public void setCurrCu(Object cu) {
        this.currCu = cu;
    }

    public void setPrevCu(Object cu) {
        this.prevCu = cu;
    }


    public Map<BodyDeclarationPair, Object> getAddBodiesTypeDeclration() {
        return addBodiesTypeDeclration;
    }

    public Map<BodyDeclarationPair, Object> getDeletedBodiesTypeDeclration() {
        return deletedBodiesTypeDeclration;
    }

//    public void addTypeDeclaration(String locationKey, Object a) {
//        if (this.classOrInterfaceOrEnum.containsKey(locationKey)) {
//            classOrInterfaceOrEnum.get(locationKey).add(a);
//        } else {
//            this.classOrInterfaceOrEnum.put(locationKey,new ArrayList<>());
//            this.classOrInterfaceOrEnum.get(locationKey).add(a);
//        }
//    }

    public List<ChangeEntity> getPreChangeEntity() {
        return preChangeEntity;
    }


    public void loadTwoCompilationUnits(Object src, Object dst, String srcPath, String dstPath) {
        loadTwoCompilationUnitsObj(src, dst, srcPath, dstPath);
    }

    /**
     * 加载两个cu对象以及相关信息
     * @param pre              CompilationUnit
     * @param curr              CompilationUnit
     * @param srcPathOrContent path string or bytes
     * @param dstPathOrContent
     */
    public void loadTwoCompilationUnitsObj(Object pre, Object curr, Object srcPathOrContent, Object dstPathOrContent) {
        this.prevCu = pre;
        this.prevLines = new ArrayList<>();
        this.fullStringPrev = FileRWUtil.getLinesOfFile(srcPathOrContent, this.prevLines);
        this.prevLineNums = FileRWUtil.getLinesList(prevLines.size());

        this.currCu = curr;
        this.currLines = new ArrayList<>();
        this.fullStringCurr = FileRWUtil.getLinesOfFile(dstPathOrContent, this.currLines);
        this.currLineNums = FileRWUtil.getLinesList(currLines.size());

    }


    public void addBodiesAdded(Object bodyDeclaration, String prefix) {
        BodyDeclarationPair newBody = new BodyDeclarationPair(bodyDeclaration, prefix, ChangeEntityDesc.TreeType.CURR_TREE_NODE);
        this.addmBodiesAdded(newBody);
    }

    private ChangeEntity initClassChangeEntity(BodyDeclarationPair bodyDeclarationPair, String type, int treeType) {
        ChangeEntity ce;
        MyRange myRange = null;
        BodyDeclaration bodyDeclaration = (BodyDeclaration) bodyDeclarationPair.getBodyDeclaration();
        TypeDeclaration typeDeclaration = null;
        Object obj = Global.astNodeUtil.searchBottomUpFindTypeDeclaration(bodyDeclaration,false);
        if (obj != null) {
            typeDeclaration = (TypeDeclaration) obj;
        }
        BodyDeclarationPair newTypeBody = new BodyDeclarationPair(typeDeclaration, bodyDeclarationPair.getCanonicalName().getPrefixName(), treeType);
        if (Insert.class.getSimpleName().equals(type)) {
            myRange = Global.astNodeUtil.getRange(typeDeclaration,ChangeEntityDesc.TreeType.CURR_TREE_NODE);
        } else if (Delete.class.getSimpleName().equals(type)) {
            myRange = Global.astNodeUtil.getRange(typeDeclaration,ChangeEntityDesc.TreeType.PREV_TREE_NODE);
        }
        ce = new ClassChangeEntity(newTypeBody, ChangeEntityDesc.StageIIOpt.OPT_CHANGE, myRange, 0);
        return ce;
    }

    public void addmBodiesAdded(BodyDeclarationPair bodyDeclarationPair) {
        if (Constants.GRANULARITY.TYPE.equals(Global.granularity)) {
            ChangeEntity ce = initClassChangeEntity(bodyDeclarationPair, Insert.class.getSimpleName(), ChangeEntityDesc.TreeType.CURR_TREE_NODE);
            addToPreprocessChangeEntity(ce);
            return;
        }
        this.mBodiesAdded.add(bodyDeclarationPair);
        //找到该bd所在的type declaration
        Object td = Global.astNodeUtil.searchBottomUpFindTypeDeclaration(bodyDeclarationPair.getBodyDeclaration(),true);
        this.addBodiesTypeDeclration.put(bodyDeclarationPair, td);
    }


    public void addBodiesDeleted(BodyDeclarationPair bodyDeclarationPair) {
        if (Constants.GRANULARITY.TYPE.equals(Global.granularity)) {
            ChangeEntity ce = initClassChangeEntity(bodyDeclarationPair, Insert.class.getSimpleName(), ChangeEntityDesc.TreeType.PREV_TREE_NODE);
            addToPreprocessChangeEntity(ce);
            return;
        }
        this.mBodiesDeleted.add(bodyDeclarationPair);
        Object td = Global.astNodeUtil.searchBottomUpFindTypeDeclaration(bodyDeclarationPair.getBodyDeclaration(),true);
        this.deletedBodiesTypeDeclration.put(bodyDeclarationPair, td);
    }

    private void addToPreprocessChangeEntity(ChangeEntity ce) {
        if (preChangeEntity == null) {
            initPreprocessChangeEntity();
        }
        if (preChangeEntity.size() == 0) {
            preChangeEntity.add(ce);
        }
    }


    public void printAddedRemovedBodies() {
        Global.logger.info("Added Bodies: ");
        for (BodyDeclarationPair item : this.mBodiesAdded) {
            Global.logger.info(item.getBodyDeclaration().toString());
        }
        System.out.print("-----------------------------\n");
        Global.logger.info("Deleted Bodies: ");
        for (BodyDeclarationPair item : this.mBodiesDeleted) {
            Global.logger.info(item.getBodyDeclaration().toString());
        }
    }

    public List<BodyDeclarationPair> getmBodiesAdded() {
        return mBodiesAdded;
    }

    public List<BodyDeclarationPair> getmBodiesDeleted() {
        return mBodiesDeleted;
    }

    public String getSoureCodeFromRange(MyRange myRange, int srcOrDst) {
        StringBuilder sb = new StringBuilder();
        List<String> lineList;
        if (ChangeEntityDesc.TreeType.PREV_TREE_NODE == srcOrDst) {
            lineList = prevLines;
        } else {
            lineList = currLines;
        }
        for (int i = myRange.startLineNo - 1; i < myRange.endLineNo; i++) {
            sb.append(lineList.get(i));
            sb.append("\n");
        }
        return sb.toString();
    }


    public ChangeEntityTree getEntityTree() {
        return entityTree;
    }

    public void setEntityTree(ChangeEntityTree entityTree) {
        this.entityTree = entityTree;
    }

    public List<Object> getmCurrFields() {
        return mCurrFields;
    }

    public void setmCurrFields(List<Object> mCurrFields) {
        this.mCurrFields = mCurrFields;
    }

    public List<Object> getmPrevFields() {
        return mPrevFields;
    }

    public void setmPrevFields(List<Object> mPrevFields) {
        this.mPrevFields = mPrevFields;
    }

    public List<Object> getCurrTypeDeclaration() {
        return currTypeDeclaration;
    }

    public List<Object> getPrevTypeDeclaration() {
        return prevTypeDeclaration;
    }

    public TypeDeclaration getTypeDeclarationOfAddedOrDeletedFile() {
        if (prevTypeDeclaration != null) {
            return (TypeDeclaration) prevTypeDeclaration.get(0);
        } else {
            return (TypeDeclaration) currTypeDeclaration.get(0);
        }
    }

    public Map<Object, Object> getDeletionKVMap() {
        return deletionKVMap;
    }

    public void addDeletionKey(Object key,Object value){
        this.deletionKVMap.put(key,value);

    }


}
