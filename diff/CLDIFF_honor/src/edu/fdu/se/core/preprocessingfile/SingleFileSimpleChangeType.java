package edu.fdu.se.core.preprocessingfile;

import com.github.gumtreediff.actions.model.Insert;
import edu.fdu.se.ASTParser.JDTParserFactory;
import edu.fdu.se.core.json.GenerateChangeEntityJson;
import edu.fdu.se.core.links.generator.ChangeEntityTree;
import edu.fdu.se.core.links.generator.LinkConstants;
import edu.fdu.se.core.links.linkbean.LinkBean;
import edu.fdu.se.core.miningactions.bean.MyRange;
import edu.fdu.se.core.miningactions.bean.MiningActionData;
import edu.fdu.se.core.miningchangeentity.base.CanonicalName;
import edu.fdu.se.core.miningchangeentity.base.ChangeEntity;
import edu.fdu.se.core.miningchangeentity.base.ChangeEntityDesc;
import edu.fdu.se.core.miningchangeentity.member.ClassChangeEntity;
import edu.fdu.se.core.preprocessingfile.data.BodyDeclarationPair;
import edu.fdu.se.ASTParser.CDTParserFactory;
import edu.fdu.se.core.preprocessingfile.data.PreCacheData;
import edu.fdu.se.global.Constants;
import edu.fdu.se.global.Global;
import edu.fdu.se.lang.java.generatingactions.JavaPartialVisitor;
import org.eclipse.cdt.core.dom.ast.*;
import org.eclipse.cdt.internal.core.dom.parser.cpp.*;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jgit.diff.DiffEntry;
import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by huangkaifeng on 2018/4/19.
 */
public class SingleFileSimpleChangeType {

    /**
     * reflection
     * edu.fdu.se.API.CLDiffCore#dooSingleFile
     * @param content
     * @param fileName
     * @param changeType
     * @return
     */
    public static MiningActionData createMadOfSimpleJavaFileChange(byte[] content, String fileName, String changeType) {
        //来判断这是什么树，如果是add，则是curr，如果是delete，则是pre
        int treeType = getTreeType(changeType);
        MiningActionData mad = createMadFromJavaSource(content, treeType);
        if (mad == null) {
            return null;
        }
        mad.fileFullPackageName = fileName;
        return mad;
    }

    /**
     * reflection
     * edu.fdu.se.API.CLDiffCore#dooSingleFile
     * @param content
     * @param fileName
     * @param changeType
     * @return
     */
    public static MiningActionData createMadOfSimpleCFileChange(byte[] content, String fileName, String changeType) {
        int treeType = getTreeType(changeType);
        MiningActionData mad = createMadFromCSource(content, treeType);
        if(mad == null) {
            return null;
        }
        mad.fileFullPackageName = fileName;
        return mad;
    }

    private static MiningActionData createMadFromJavaSource(byte[] content, int treeType) {
        CompilationUnit cu = null;
        try {
            //获取语法树
            cu = JDTParserFactory.getCompilationUnit(content);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //types()返回的是 List<TypeDeclaration>，用来获取cu定义的所有类型。TypeDeclaration表示一个类、接口
        if(cu.types().size()==0){
            return null;
        }
        //BodyDeclaration 类型是一个抽象类，包含了表示成员修饰符、名称、类型、注释等信息的方法和属性。
        //TypeDeclaration 类型继承自 BodyDeclaration 类型
        //get(0)的原因是只有一个元素
        BodyDeclaration bodyDeclaration = (BodyDeclaration) cu.types().get(0);
        if (bodyDeclaration instanceof TypeDeclaration) {
            return handleTypeDeclaration((TypeDeclaration)bodyDeclaration, cu, treeType, content);
        } else if (bodyDeclaration instanceof EnumDeclaration) {
            handleEnumDeclaration(bodyDeclaration, cu, treeType);
        }
        return null;
    }

    private static MiningActionData createMadFromCSource(byte[] content, int treeType) {
        IASTTranslationUnit cu = null;
        try {
            //获取语法树
            cu = CDTParserFactory.getTranslationUnit(content);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (cu.getDeclarations().length == 0) {
            return null;
        }
        List<ChangeEntity> mList = new ArrayList<>();
        return handleC(cu,mList,treeType);
    }


    private static MiningActionData handleC(IASTNode iastNode,List<ChangeEntity> mList,int treeType) {
        //从文件内平行的children开始
        for (IASTNode node : iastNode.getChildren()) {
            //IASTNode node2 = ((IASTCompositeTypeSpecifier) ((IASTSimpleDeclaration) node.
            //CPPASTSimpleDeclaration
            ClassChangeEntity classChangeEntity = null;
            if(node instanceof CPPASTSimpleDeclaration simpleDeclaration){

                IASTDeclSpecifier declSpecifier = ((IASTSimpleDeclaration) node).getDeclSpecifier();
                if(declSpecifier instanceof CPPASTCompositeTypeSpecifier){
                    classChangeEntity = new ClassChangeEntity(new BodyDeclarationPair(simpleDeclaration,
                            ((IASTCompositeTypeSpecifier) ((IASTSimpleDeclaration) node).getDeclSpecifier()).getName().toString() + "/",treeType),
                            Insert.class.getSimpleName(),
                            new MyRange(simpleDeclaration.getFileLocation().getStartingLineNumber(), simpleDeclaration.getFileLocation().getEndingLineNumber(),
                                    treeType));
                }

            } else if (node instanceof CPPASTFunctionDefinition functionDefinition) {
                classChangeEntity = new ClassChangeEntity(new BodyDeclarationPair(functionDefinition, "/",treeType),
                        Insert.class.getSimpleName(),
                        new MyRange(functionDefinition.getFileLocation().getStartingLineNumber(), functionDefinition.getFileLocation().getEndingLineNumber(),
                        treeType));
            }
            mList.add(classChangeEntity);
        }
        return new MiningActionData(mList, treeType);
    }


    private static MiningActionData handleEnumDeclaration(BodyDeclaration bodyDeclaration, CompilationUnit cu, int treeType) {
        return null;
    }

    /**
     *
     */
    private static MiningActionData handleTypeDeclaration(TypeDeclaration typeDeclaration, CompilationUnit cu,
                                                          int treeType, byte[] content) {
        ClassChangeEntity classChangeEntity =
                //100表示ENTITY_CLASS，0表示ENTITY_INNER_CLASS
                //主要给StageIIBean属性赋值
                new ClassChangeEntity(
                        new BodyDeclarationPair(typeDeclaration, "^",treeType),
                        Insert.class.getSimpleName(),
                        new MyRange(
                                //记录了开始和结束行号以及数据所属类型
                                cu.getLineNumber(typeDeclaration.getStartPosition()),
                                cu.getLineNumber(typeDeclaration.getStartPosition() + typeDeclaration.getLength()), treeType),
                        100);
        List<ChangeEntity> mList = new ArrayList<>();
        mList.add(classChangeEntity);
        MiningActionData mad = new MiningActionData(mList, treeType);
        mad.preCacheData = new PreCacheData(cu, content, treeType,typeDeclaration);
        initSingleFileChange(mad);
        Global.mad = mad;
        if(Global.isLink){
            //将 typeDeclaration 的相关信息装入 classChangeEntity
            addClassDefUse(classChangeEntity, typeDeclaration);
        }
        jsonOutput(mad);
        return mad;
    }

    /**
     * 将 typeDeclaration 的相关信息装入 classChangeEntity，
     * 如：name、类接口枚举信息、方法、VariableDeclarationFragment对象
     */
    public static void addClassDefUse(ClassChangeEntity classChangeEntity, TypeDeclaration typeDeclaration) {
        if (classChangeEntity.linkBean == null) {
            classChangeEntity.linkBean = new LinkBean();
        }
        //处理typeDeclaration
        Global.processUtil.initInheritance(classChangeEntity, typeDeclaration);
        //获取TypeDeclaration的名字
        String unqueKey = Global.astNodeUtil.getBodyDeclarationUniqueKey(typeDeclaration);
        classChangeEntity.stageIIBean.setCanonicalName(new CanonicalName("^", unqueKey));
        int treeType = ChangeEntityDesc.optStringToTreeType(classChangeEntity.stageIIBean.getOpt());
        //将类、接口或枚举信息转入defList
        classChangeEntity.linkBean.defList.addDef(
                classChangeEntity.stageIIBean.getCanonicalName().getLongName(),
                typeDeclaration.getName().toString(),
                LinkConstants.DEF_CLASS,
                classChangeEntity.lineRange,treeType);
        //获取该类下的方法
        MethodDeclaration[] lists = typeDeclaration.getMethods();
        List<Statement> statements = new ArrayList<>();
        //对每一个方法进行检查，将变量声明和语句分别存放在 defList 和 statements 中
        for (MethodDeclaration md : lists) {
            addMethodDefUse(md, classChangeEntity, statements);
        }
        JavaPartialVisitor javaPartialVisitor = new JavaPartialVisitor(statements,null);
        //getFields()用于获取该类型声明中定义的所有字段
        //FieldDeclaration 类包含一个或多个 VariableDeclarationFragment 对象，每个对象表示一个变量声明，即一个字段。
        FieldDeclaration[] lists2 = typeDeclaration.getFields();
        initPreCacheDataField(lists2,treeType);
        //添加use
        javaPartialVisitor.addUses(classChangeEntity,treeType);
        for (FieldDeclaration fd : lists2) {
            //添加 fieldDeclaration 到 defList
            Global.processUtil.addFieldDef(classChangeEntity, fd);
        }
    }

    /**
     * 将传入的 FieldDeclaration 数组加入 List<FieldDeclaration>
     * 根据 treeType 放入对应的 Global.mad.preCacheData
     */
    private static void initPreCacheDataField(FieldDeclaration[] fields, int treeType){
        List<Object> mList = new ArrayList<>();
        for(Object fd:fields){
            mList.add(fd);
        }
        if(treeType == ChangeEntityDesc.TreeType.PREV_TREE_NODE){
            Global.mad.preCacheData.setmPrevFields(mList);
        }else{
            Global.mad.preCacheData.setmCurrFields(mList);

        }

    }

    /**
     * 检查方法的每一句
     * 如果是变量声明，则加入 defList
     * 如果不是，则加入 statements
     * @param md
     * @param changeEntity
     * @param statements
     */
    public static void addMethodDefUse(MethodDeclaration md, ChangeEntity changeEntity, List<Statement> statements) {
        Global.processUtil.addMethodDef(changeEntity, md);
        if (md.getBody() == null) {
            return;
        }
        //getBody() 获取方法体，返回为 Body 对象
        //statements() 获取表示代码块中所有语句的列表，返回一个 List 对象
        List stmts = md.getBody().statements();
        //对于每一条语句检查是否为变量声明，是则加入
        for (Object s : stmts) {
            if (s instanceof VariableDeclarationStatement) {
                Global.processUtil.addVarDeclarationDef(changeEntity, s);
            } else {
                statements.add((Statement) s);
            }
        }
    }

    /**
     * 整理mad数据，得到change类型、行号等数据，为 String 形式，方便 json 输出。
     * 目前只有 MODIFIED_FILES
     */
    private static void jsonOutput(MiningActionData mad) {
        //获得GenerateChangeEntityJson并根据粒度生成指定的beanSetter
        GenerateChangeEntityJson generate = new GenerateChangeEntityJson(Global.granularity);
        generate.setStageIIIBean(mad);
        //myflag
        //JSONArray json = GenerateChangeEntityJson.generateEntityJson(mad);
        //Global.fileOutputLog.writeEntityJson(json.toString(4));
    }

    /**
     * 将mad中所有的ChangeEntity都装入ChangeEntityTree，
     * 然后将ChangeEntityTree转入mad.preCacheData.entityTree
     * @param mad
     */
    private static void initSingleFileChange(MiningActionData mad) {
        ChangeEntityTree temp = new ChangeEntityTree();
        TypeDeclaration typeDeclaration = mad.preCacheData.getTypeDeclarationOfAddedOrDeletedFile();
        BodyDeclarationPair key = new BodyDeclarationPair(typeDeclaration, "^",getTreeType(mad.changeType));
        temp.addKey(key);
        for (ChangeEntity ce : mad.getChangeEntityList()) {
            temp.addPreDiffChangeEntity(ce);
        }
        mad.preCacheData.setEntityTree(temp);
    }


    public static int getTreeType(String changeType) {
        int treeType = 0;
        if (changeType.equals(Constants.ChangeTypeString.ADD)) {
            treeType = ChangeEntityDesc.TreeType.CURR_TREE_NODE;
        } else if (changeType.equals(Constants.ChangeTypeString.DELETE)) {
            treeType = ChangeEntityDesc.TreeType.PREV_TREE_NODE;
        }
        return treeType;
    }

    public static int getTreeType(DiffEntry.ChangeType changeType) {
        int treeType = 0;
        if (changeType.equals(DiffEntry.ChangeType.ADD)) {
            treeType = ChangeEntityDesc.TreeType.CURR_TREE_NODE;
        } else if (changeType.equals(DiffEntry.ChangeType.DELETE)) {
            treeType = ChangeEntityDesc.TreeType.PREV_TREE_NODE;
        }
        return treeType;
    }


}
