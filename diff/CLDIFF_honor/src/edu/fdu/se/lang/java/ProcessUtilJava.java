package edu.fdu.se.lang.java;

import com.github.gumtreediff.actions.model.Action;
import com.github.gumtreediff.actions.model.Update;
import com.github.gumtreediff.tree.Tree;
import edu.fdu.se.core.links.generator.LinkConstants;
import edu.fdu.se.core.links.linkbean.Inheritance;
import edu.fdu.se.core.links.linkbean.LinkBean;
import edu.fdu.se.core.links.linkbean.MyParameters;
import edu.fdu.se.core.miningchangeentity.base.MemberPlusChangeEntity;
import edu.fdu.se.core.miningchangeentity.member.ClassChangeEntity;
import edu.fdu.se.core.miningchangeentity.member.FieldChangeEntity;
import edu.fdu.se.core.miningchangeentity.member.MethodChangeEntity;
import edu.fdu.se.core.miningchangeentity.statement.VariableChangeEntity;
import edu.fdu.se.core.preprocessingfile.SingleFileSimpleChangeType;
import edu.fdu.se.global.Constants;
import edu.fdu.se.global.Global;
import edu.fdu.se.core.miningactions.bean.MiningActionData;
import edu.fdu.se.core.miningactions.statement.*;
import edu.fdu.se.core.miningactions.util.AstRelations;
import edu.fdu.se.core.miningchangeentity.base.ChangeEntity;
import edu.fdu.se.core.miningchangeentity.base.ChangeEntityDesc;
import edu.fdu.se.core.preprocessingfile.FilePairPreDiff;
import edu.fdu.se.core.preprocessingfile.data.PreCacheData;
import edu.fdu.se.core.preprocessingfile.data.PreCacheTmpData;
import edu.fdu.se.lang.common.ProcessUtil;
import edu.fdu.se.lang.java.generatingactions.JavaPartialVisitor;
import edu.fdu.se.lang.java.preprocess.TypeNodesTraversalJava;
import org.eclipse.jdt.core.dom.*;

import java.util.*;
import java.lang.reflect.Method;

public class ProcessUtilJava implements ProcessUtil {


    /**
     * 将cu中的注释、import部分进行筛选加入到 RemoveList 中去进行处理。
     * 这里需要改写为通用的方法
     * @param tempData 现在是currData
     * @param o 通用的cu
     * @param lineList
     */
    @Override
    public void removeAllComments(PreCacheTmpData tempData, Object o, List<Integer> lineList, int treeType,PreCacheData preCacheData) {
        CompilationUnit cu = (CompilationUnit) o;
        //获取与该编译单元相关联的包的信息
        PackageDeclaration packageDeclaration = cu.getPackage();
        if (packageDeclaration != null)
            tempData.addToRemoveList(packageDeclaration, treeType);
        //获取源代码中的注释列表
        List<ASTNode> commentList = cu.getCommentList();
        for (int i = commentList.size() - 1; i >= 0; i--) {
            if (commentList.get(i) instanceof Javadoc && commentList.get(i).getParent() != null) {
                tempData.addToRemoveList(commentList.get(i), treeType);
                if (!Global.isMethodRangeContainsJavaDoc) {
                    int len = commentList.get(i).getParent().getLength() - commentList.get(i).getLength();
                    int start = commentList.get(i).getStartPosition() + commentList.get(i).getLength();
                    //Sets the source range of the original source file where the source fragment corresponding to this node was found.
                    //把注释所属的代码段range设为代码开始的位置而不是注释开始的位置
                    commentList.get(i).getParent().setSourceRange(start + 1, len);
                }
            }
        }
        //获取源代码文件中的导入语句列表
        List<ImportDeclaration> imprortss = cu.imports();
        for (int i = imprortss.size() - 1; i >= 0; i--) {
            ImportDeclaration im = imprortss.get(i);
            //检查是否为静态导入
            if(im.isStatic()){
                if(preCacheData.getStaticImports()==null){
                    preCacheData.setStaticImports(new ArrayList<>());
                }
                preCacheData.getStaticImports().add(im.getName().toString());
            }
            tempData.addToRemoveList(im, treeType);
        }
        removeRemovalList(tempData, cu, lineList, treeType,preCacheData);
    }

    private void removeRemovalList(PreCacheTmpData tempData, CompilationUnit cu, List<Integer> lineList, int prevOrCurr,PreCacheData preCacheData) {

        List<Object> nodesToBeRemoved;
        if (ChangeEntityDesc.TreeType.PREV_TREE_NODE == prevOrCurr) {
            nodesToBeRemoved = tempData.prevRemovalNodes;
        } else if (ChangeEntityDesc.TreeType.CURR_TREE_NODE == prevOrCurr) {
            nodesToBeRemoved = tempData.currRemovalNodes;
        } else {
            return;
        }
        for (Object o : nodesToBeRemoved) {
            ASTNode item = (ASTNode) o;
            //getLineNumber 返回与原始源字符串中给定的源字符位置对应的行号。
            tempData.setLinesFlag(lineList, cu.getLineNumber(item.getStartPosition()),
                    cu.getLineNumber(item.getStartPosition() + item.getLength() - 1));
            //Map的添加
            preCacheData.addDeletionKey(item,item.getParent());
            item.delete();
        }
        nodesToBeRemoved.clear();
    }


    class PrevCurrPair {
        TypeDeclaration tpPrev;
        TypeDeclaration tpCurr;
    }

    class PrevCurrPair2{
        EnumDeclaration tpPrev;
        EnumDeclaration tpCurr;
    }

    @Override
    public int compareTwoFile(FilePairPreDiff preDiff, PreCacheTmpData preCacheTmpData, PreCacheData preCacheData) {
        CompilationUnit cuPrev = (CompilationUnit) preCacheData.getPrevCu();
        CompilationUnit cuCurr = (CompilationUnit) preCacheData.getCurrCu();
        Queue<PrevCurrPair> queue = new LinkedList<>();
        //myflag
        Queue<PrevCurrPair2> queue2 = new LinkedList<>();
        // todo 如果新增的文件内多写了一个类，应该是不检查的
        if (cuPrev.types().size() != cuCurr.types().size()) {
            return -1;
        }
        //遍历cu内的所有类型列表，并都加入PrevTypeDeclaration/CurrTypeDeclaration
        //如果是 TypeDeclaration ，则封装 prevCurrPair 加入到 queue
        for (int i = 0; i < cuPrev.types().size(); i++) {
            BodyDeclaration bodyDeclarationPrev = (BodyDeclaration) cuPrev.types().get(i);
            BodyDeclaration bodyDeclarationCurr = (BodyDeclaration) cuCurr.types().get(i);
            preCacheData.getPrevTypeDeclaration().add(bodyDeclarationPrev);
            preCacheData.getCurrTypeDeclaration().add(bodyDeclarationCurr);
            if ((bodyDeclarationPrev instanceof TypeDeclaration) && (bodyDeclarationCurr instanceof TypeDeclaration)) {
                PrevCurrPair prevCurrPair = new PrevCurrPair();
                prevCurrPair.tpPrev = (TypeDeclaration) bodyDeclarationPrev;
                prevCurrPair.tpCurr = (TypeDeclaration) bodyDeclarationCurr;
                queue.offer(prevCurrPair);
            }
            else {
                return -1;
            }
        }
        while (queue.size() != 0) {
            PrevCurrPair tmp = queue.poll();
            compare(cuPrev, cuCurr, tmp.tpPrev, tmp.tpCurr, preCacheData, preCacheTmpData, preDiff);
        }
        return 0;
    }

    private void compare(CompilationUnit cuPrev, CompilationUnit cuCurr, TypeDeclaration tdPrev,
                         TypeDeclaration tdCurr, PreCacheData preCacheData, PreCacheTmpData tempData, FilePairPreDiff preDiff) {
        TypeNodesTraversalJava astTraversal = new TypeNodesTraversalJava();
        astTraversal.traversePrevTypeDeclarationInit(preCacheData, tempData, tdPrev, "^");
        astTraversal.traverseCurrTypeDeclarationComparePrev(preCacheData, tempData, tdCurr, "^");
        preDiff.iterateVisitingMap();
        preDiff.undeleteSignatureChange();
        sameFieldNodeToCache(tempData,preCacheData);
        removeRemovalList(tempData, cuPrev, preCacheData.getPrevLineNums(), ChangeEntityDesc.TreeType.PREV_TREE_NODE,preCacheData);
        removeRemovalList(tempData, cuCurr, preCacheData.getCurrLineNums(), ChangeEntityDesc.TreeType.CURR_TREE_NODE,preCacheData);
        preDiff.iterateVisitingMap2LoadContainerMap();
    }

    private void sameFieldNodeToCache(PreCacheTmpData preCacheTmpData,PreCacheData preCacheData){
        List<Object> currList = new ArrayList<>();
        List<Object> prevList = new ArrayList<>();
        preCacheData.setmCurrFields(currList);
        preCacheData.setmPrevFields(prevList);
        for(Object o :preCacheTmpData.currRemovalNodes){
            if(o instanceof FieldDeclaration){
                currList.add((FieldDeclaration)o);
            }
        }
        for(Object o :preCacheTmpData.prevRemovalNodes){
            if(o instanceof FieldDeclaration){
                prevList.add((FieldDeclaration)o);
            }
        }
    }


    public void handleMethodNameModification(ChangeEntity changeEntity,ChangeEntity tempCE){
        if(((MethodChangeEntity) tempCE).bodyDeclarationPair != null && ((MethodChangeEntity) changeEntity).bodyDeclarationPair!= null){
            MethodDeclaration bdTmp = (MethodDeclaration)((MethodChangeEntity) tempCE).bodyDeclarationPair.getBodyDeclaration();
            MethodDeclaration bdCe = (MethodDeclaration)((MethodChangeEntity) changeEntity).bodyDeclarationPair.getBodyDeclaration();
            if(compareMethodBody(bdCe,bdTmp)){
                if(changeEntity.getFrontData().getFile().equals("dst")){
                    changeEntity.getFrontData().setMethodFrom(bdTmp);
                    tempCE.getFrontData().setMethodTo(bdCe);
                }
                else {
                    changeEntity.getFrontData().setMethodTo(bdTmp);
                    tempCE.getFrontData().setMethodFrom(bdCe);
                }
            }
        }
    }

    public String methodDeclarationToString(Object method){
        MethodDeclaration methodDeclaration = (MethodDeclaration) method;
        StringBuilder sb = new StringBuilder();
        String methodName = methodDeclaration.getName().toString();
        sb.append(methodName);
        List pList = methodDeclaration.parameters();
        Iterator iter2 = pList.iterator();
        sb.append("(");

        while (iter2.hasNext()){
            // short param
            SingleVariableDeclaration var = (SingleVariableDeclaration) iter2.next();
            String varString = var.getType().toString();
            int index = varString.lastIndexOf(".");
            sb.append(varString.substring(index+1));
            sb.append(",");
        }
        if(pList.size()!=0){
            sb.deleteCharAt(sb.length()-1);
        }
        sb.append(")");
        return sb.toString();
    }

    public static boolean compareMethodBody(MethodDeclaration md1,MethodDeclaration md2){
        if(md1.getBody() != null && md2.getBody() != null)
            return md1.getBody().toString().hashCode()==md2.getBody().toString().hashCode();
        else
            return false;
    }

    @Override
    public String bodyDeclarationToString(Object o) {
        return o.toString();
    }


    /**
     * 筛选出存在于mMap中的granularity
     */
    @Override
    public Map<Integer, String> filterByGranularity(String granularity, Map<Integer, String> mMap) {
        Set<Integer> keys = mMap.keySet();
        int[] elements = Global.iLookupTbl.astNodeMap.get(granularity);
        Map<Integer, String> res = new HashMap<>();
        for (int a : elements) {
            if (keys.contains(a)) {
                res.put(a, mMap.get(a));
            }
        }
        return res;
    }


    @Override
    public int matchEntityTopDown(MiningActionData mad, Action action, int nodeType, String granularity) {
        int res = 0;
        //Global.iLookupTbl.callTable是一个map，key是String，value是map
        Map<Integer, String> mMap = Global.iLookupTbl.callTable.get(Constants.MATCH_TOP_DOWN);
        Map<Integer, String> newMap = filterByGranularity(granularity, mMap);
        if (nodeType == ASTNode.EXPRESSION_STATEMENT &&
                newMap.containsKey(ASTNode.EXPRESSION_STATEMENT) &&
                AstRelations.isFatherXXXStatement(action, ASTNode.IF_STATEMENT) &&
                action.getNode().getParent().getChildPosition(action.getNode()) == 2) {
            new MatchIfElse().matchElseTopDown(mad, action);
            return 1;
        }

        if (newMap.containsKey(nodeType)) {
            String call = newMap.get(nodeType);
            int index = call.lastIndexOf(".");
            String className = call.substring(0, index);
            String methodName = call.substring(index + 1);
            try {
                Class<?> printClass = Class.forName(className);
                Method printMethod = printClass.getMethod(methodName, MiningActionData.class, Action.class);
                printMethod.invoke(printClass.getDeclaredConstructor().newInstance(), mad, action);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return res;
    }

    @Override
    public void matchEntityBottomUpCurr(MiningActionData fp, Action a, ChangeEntity changeEntity, int nodeType, Tree traverseFather) {
        if (nodeType == ASTNode.METHOD_DECLARATION && Global.astNodeUtil.isBlock(((Tree) a.getNode()).getNode())) {
            return;
        }
        Map<Integer, String> mMap = Global.iLookupTbl.callTable.get(Constants.MATCH_BOTTOM_UP_CURR);
        if (mMap.containsKey(nodeType)) {
            String call = mMap.get(nodeType);
            int index = call.lastIndexOf(".");
            String className = call.substring(0, index);
            String methodName = call.substring(index + 1);
            try {
                Class<?> printClass = Class.forName(className);
                Method printMethod = printClass.getMethod(methodName, MiningActionData.class, Action.class, ChangeEntity.class, Tree.class);
                printMethod.invoke(printClass.newInstance(), fp, a, changeEntity, traverseFather);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void matchEntityBottomUpNew(MiningActionData fp, Action a, Tree queryFather, int treeType, Tree traverseFather, List<Integer> commonRootNodeTypes) {
        int nodeType = Global.astNodeUtil.getNodeTypeId(traverseFather.getNode());
        Map<Integer, String> mMap = Global.iLookupTbl.callTable.get(Constants.MATCH_BOTTOM_UP_NEW);
        if (nodeType == ASTNode.METHOD_DECLARATION && Global.astNodeUtil.getNodeTypeId(((Tree) a.getNode()).getNode()) == ASTNode.BLOCK) {
            return;
        }
        if (mMap.containsKey(nodeType)) {
            String call = mMap.get(nodeType);
            int index = call.lastIndexOf(".");
            String className = call.substring(0, index);
            String methodName = call.substring(index + 1);
            try {
                Class<?> printClass = Class.forName(className);
                Method printMethod = printClass.getMethod(methodName, MiningActionData.class, Action.class, Tree.class, int.class, Tree.class);
                printMethod.invoke(printClass.newInstance(), fp, a, queryFather, treeType, traverseFather);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    public void matchBlock(MiningActionData fp, Action a, int type, Tree fatherNode) {
        switch (type) {
            case ASTNode.SWITCH_STATEMENT:
//                MatchSwitch.matchSwitchCaseNewEntity(mad,a);
                fp.setActionTraversedMap(a);
                break;
            case ASTNode.IF_STATEMENT:
                //Pattern 1.2 Match else
                if (fatherNode.getChildPosition(a.getNode()) == 2) {
                    new MatchIfElse().matchElseTopDown(fp, a);
                }
                fp.setActionTraversedMap(a);
                break;
            case ASTNode.TRY_STATEMENT:
                ////Finally块
                if (fatherNode.getChildPosition(a.getNode()) == fatherNode.getChildren().size() - 1) {
                    new MatchTry().matchFinallyTopDown(fp, a);
                }
                fp.setActionTraversedMap(a);
                break;
            default:
                fp.setActionTraversedMap(a);
                break;
        }

    }

    @Override
    public void initDefs(ChangeEntity ce, MiningActionData mad) {
        if (ce instanceof MemberPlusChangeEntity) {
            if (ce instanceof ClassChangeEntity) {
                handleClassChange((ClassChangeEntity) ce);
            } else if (ce instanceof MethodChangeEntity) {
                handleMethodChange((MethodChangeEntity) ce,mad);
            } else if (ce instanceof FieldChangeEntity) {
                handleFieldChange((FieldChangeEntity) ce);
            }
        }
        if (ce instanceof VariableChangeEntity) {
            handleVariableLocalChange((VariableChangeEntity) ce);
        }
    }

    private void handleClassChange(ClassChangeEntity cce) {
        TypeDeclaration td = null;
        if (cce.bodyDeclarationPair != null) {
            td = (TypeDeclaration) cce.bodyDeclarationPair.getBodyDeclaration();
            cce.setClassName(td.getName().toString());
        } else {
            td = (TypeDeclaration) Global.astNodeUtil.searchBottomUpFindTypeDeclaration(((Tree) cce.clusteredActionBean.curAction.getNode()).getNode(),true);
            cce.setClassName(Global.astNodeUtil.getTypeName(td));
        }
        if (td != null) {
            int treeType = ChangeEntityDesc.optStringToTreeType(cce.stageIIBean.getOpt());
            cce.linkBean.defList.addDef(cce.stageIIBean.getCanonicalName().getLongName(), td.getName().toString(), LinkConstants.DEF_CLASS, cce.lineRange,treeType);
            SingleFileSimpleChangeType.addClassDefUse(cce, td);
        }
    }

    private void handleMethodChange(MethodChangeEntity mce,MiningActionData mad) {
        MethodDeclaration md;
        if (mce.bodyDeclarationPair != null) {
            md = (MethodDeclaration) mce.bodyDeclarationPair.getBodyDeclaration();
            mce.setMethodName(md.getName().toString());
            this.addMethodDef(mce, md);
            addMethodDefUse(md, mce,mad);
        } else {
            int[] a = {ASTNode.METHOD_DECLARATION};
            Tree methodTreeNode = (Tree) Global.astNodeUtil.searchBottomUpFindCorresASTNode(mce.clusteredActionBean.curAction.getNode(), a);
            md = (MethodDeclaration) methodTreeNode.getNode();
            String currMethodName= null;
            MethodDeclaration methodDeclarationCurr = null;
            if(methodTreeNode.getTreePrevOrCurr() == ChangeEntityDesc.TreeType.PREV_TREE_NODE){
                Tree methodTreeCurr = (Tree) Global.mad.getMappedCurrOfPrevNode(methodTreeNode);
                if(methodTreeCurr == null){
                    return;//hot fix
                }
                methodDeclarationCurr = (MethodDeclaration) methodTreeCurr.getNode();
                currMethodName = methodDeclarationCurr.getName().toString();
            }
            String prevMethodName = md.getName().toString();
            if(currMethodName !=null && !prevMethodName.equals(currMethodName)){
                md = methodDeclarationCurr;
            }
            mce.setMethodName(md.getName().toString());
            this.addMethodDef(mce, md);
            addMethodDefUse(md, mce,mad);
        }
    }

    private void addMethodDefUse(MethodDeclaration md, MethodChangeEntity mce,MiningActionData mad) {
        if (md.getBody() != null) {
            if(mce.stageIIBean.getOpt().equals(ChangeEntityDesc.StageIIOpt.OPT_CHANGE)){
                return;
            }
            int treeType = ChangeEntityDesc.optStringToTreeType(mce.stageIIBean.getOpt());
            List<Statement> statements = new ArrayList<>();
            List stmts = md.getBody().statements();
            for (Object s : stmts) {
                if (s instanceof VariableDeclarationStatement) {
                    Global.processUtil.addVarDeclarationDef(mce, s);
                }
                statements.add((Statement) s);
            }
            JavaPartialVisitor javaPartialVisitor = new JavaPartialVisitor(statements,mad.preCacheData);
            javaPartialVisitor.addMethodParams(md);
            javaPartialVisitor.addUses(mce,treeType);
        }

    }

    private void handleFieldChange(FieldChangeEntity fce) {
        FieldDeclaration fd;
        if (fce.bodyDeclarationPair != null) {
            fd = (FieldDeclaration) fce.bodyDeclarationPair.getBodyDeclaration();
            List<String> names = Global.astNodeUtil.getFieldDeclarationNames(fd);
            fce.setFieldName(names);
        } else {
            int[] a = {ASTNode.FIELD_DECLARATION};
            fd = (FieldDeclaration) Global.astNodeUtil.searchBottomUpFindCorresASTNode(((Tree) fce.clusteredActionBean.curAction.getNode()).getNode(), a);
            List<String> names = Global.astNodeUtil.getFieldDeclarationNames(fd);
            fce.setFieldName(names);
        }
        if (fd != null) {

            this.addFieldDef(fce, fd);
        }

    }

    private void handleVariableLocalChange(VariableChangeEntity ce) {
        //var declaration def
        // update的那个变量
        List<Action> actions = ce.clusteredActionBean.actions;
        boolean foundDefFlag = false;
        List<String> variables = new ArrayList<>();
        for (Action a : actions) {
            Tree tree = (Tree) a.getNode();
            if (a instanceof Update) {
                Tree currNode = (Tree) Global.mad.getMappedCurrOfPrevNode(tree);
                Object exp = Global.astNodeUtil.searchBottomUpFindNoneSimpleNameOrLiteralExpression(tree.getNode());
                if (exp != null && Global.astNodeUtil.isASTNodeSameAsClass(exp, VariableDeclarationFragment.class)) {
                    ce.linkBean.defList.addDef(ce.stageIIBean.getCanonicalName().getLongName(), currNode.getLabel(), ce.lineRange,ChangeEntityDesc.TreeType.CURR_TREE_NODE);
                    foundDefFlag = true;
                }
            }
            if(tree.getAstClass().equals(SimpleName.class)){
                variables.add(tree.getLabel());
            }
        }
        if (foundDefFlag) {
            return;
        }
        int[] a = {ASTNode.VARIABLE_DECLARATION_STATEMENT};
        VariableDeclarationStatement vds = (VariableDeclarationStatement) Global.astNodeUtil.searchBottomUpFindCorresASTNode(((Tree)ce.clusteredActionBean.curAction.getNode()).getNode(), a);
        List fragments = vds.fragments();
        int treeType = ChangeEntityDesc.optStringToTreeType(ce.stageIIBean.getOpt());
        for (Object ob : fragments) {
            VariableDeclarationFragment vdf = (VariableDeclarationFragment) ob;
            Expression e = vdf.getInitializer();
            if (e != null) {
                JavaPartialVisitor javaPartialVisitor = new JavaPartialVisitor(e,null);
                javaPartialVisitor.addUses(ce, treeType, variables);
            }
            ce.linkBean.defList.addDef(ce.stageIIBean.getCanonicalName().getLongName(), vdf.getName().toString(), ce.lineRange,treeType);
        }

    }

    /**
     * 为方法添加相关信息，
     * 例如：CanonicalName、返回类型、方法名、参数列表、lineRange、文件所属类型
     */
    @Override
    public void addMethodDef(ChangeEntity ce, Object methodDeclaration) {
        MethodDeclaration md = (MethodDeclaration) methodDeclaration;
        String returnType = null;
        //getReturnType2() 方法返回一个 Type 对象，该对象表示方法的返回类型。
        //如果方法返回 void 类型，则返回 null。
        if (md.getReturnType2() != null) {
            returnType = md.getReturnType2().toString();
        }
        List<MyParameters> myParams = Global.astNodeUtil.getMethodDeclarationParameters(md);
        int treeType = ChangeEntityDesc.optStringToTreeType(ce.getStageIIBean().getOpt());
        ce.linkBean.defList.addDef(ce.stageIIBean.getCanonicalName().getLongName(), returnType, md.getName().toString(), myParams, ce.lineRange,treeType);
    }

    /**
     * 获取 fieldDeclaration 中的变量列表，并加入defList
     */
    @Override
    public void addFieldDef(ChangeEntity ce, Object fd) {
        int treeType = ChangeEntityDesc.optStringToTreeType(ce.getStageIIBean().getOpt());
        FieldDeclaration fieldDeclaration = (FieldDeclaration) fd;
        List fragments = fieldDeclaration.fragments();
        for (Object o : fragments) {
            VariableDeclarationFragment vdf = (VariableDeclarationFragment) o;
            ce.linkBean.defList.addDef(ce.stageIIBean.getCanonicalName().getLongName(), fieldDeclaration.getType().toString(), vdf.getName().toString(), ce.lineRange,treeType);
        }
    }

    /**
     * 以字符串的形式将 VariableDeclarationFragment 对象 加入 defList
     * @param ce
     * @param variableDeclarationStatement
     */
    @Override
    public void addVarDeclarationDef(ChangeEntity ce, Object variableDeclarationStatement) {
        VariableDeclarationStatement vds = (VariableDeclarationStatement) variableDeclarationStatement;
        //fragments() 获得表示变量声明语句中所有变量的 VariableDeclarationFragment 对象
        //返回 List
        List frags = vds.fragments();
        int treeType = ChangeEntityDesc.optStringToTreeType(ce.stageIIBean.getOpt());
        for (Object f : frags) {
            VariableDeclarationFragment vdf = (VariableDeclarationFragment) f;
            ce.linkBean.defList.addDef(ce.stageIIBean.getCanonicalName().getLongName(), vdf.getName().toString(), ce.lineRange,treeType);
        }
    }

    /**
     * 对 td 处理，得到 类名、是否为抽象类、父类名、父接口名
     * 将这些内容封装到 ce.linkBean.inheritance
     * @param ce
     * @param td
     */
    @Override
    public void initInheritance(ChangeEntity ce, Object td) {
        if (ce.linkBean == null) {
            ce.linkBean = new LinkBean();
        }
        //获取类名
        String className = Global.astNodeUtil.getTypeName(td);
        //获取父类名，如果没有会返回null
        String baseName = Global.astNodeUtil.getBaseType(td);
        //检查变量中是否包含 < 符号，以判断是否存在泛型类型参数列表
        //如果 baseName 变量包含泛型参数列表，使用 substring() 方法提取 < 之前的子字符串，
        //从而获取不包含泛型参数列表的类名。
        if(baseName != null && baseName.contains("<")){
            int index = baseName.indexOf("<");
            baseName = baseName.substring(0,index);
        }
        //获取父接口，如果没有会返回null
        List<String> interfaces = Global.astNodeUtil.getInterfaces(td);
        //指示该对象所代表的类或接口是否为抽象类型
        int flag = Global.astNodeUtil.isTypeAbstract(td);
        ce.linkBean.inheritance = new Inheritance(className, flag, baseName, interfaces);
    }


}
