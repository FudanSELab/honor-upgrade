package edu.fdu.se.lang.common;

import com.github.gumtreediff.tree.ITree;
import com.github.gumtreediff.tree.Tree;
import edu.fdu.se.core.links.linkbean.MyParameters;
import edu.fdu.se.core.miningactions.bean.MiningActionData;
import edu.fdu.se.core.miningactions.bean.MyRange;
import edu.fdu.se.core.miningchangeentity.base.CanonicalName;
import org.eclipse.jdt.core.dom.BodyDeclaration;

import java.util.List;

public interface IASTNodeUtil {

    Object getPrevCu(Object data);

    Object getCurrCu(Object data);

    Object parseCu(String path);

    Object parseCu(byte[] raw);

    Object parseCu(byte[] raw, String s);

    int getNodeTypeId(Object node);

    boolean isIf(Object o);

    boolean isTypeDeclaration(Object o);

    boolean isMethodDeclaration(Object o);

    boolean isFieldDeclaration(Object o);

    boolean isEnumDeclaration(Object o);

    boolean isMethodInvocation(Object o);

    boolean isClassInstanceCreation(Object o);

    boolean isSingleVariableDeclaration(Object o);

    boolean isLiteralOrName(Tree tree);

    boolean isLiteralOrName(int nodetype);

    String literalOrSimpleNameAsString(Object object);

    boolean isCompilationUnit(Object o);

    boolean isBlock(Object o);

    boolean isSwitchCase(Object o);

    boolean isASTNodeSameAsClass(Object o, Class c);


    int getLineNumber(Object cu, int num);

    int getStartPosition(Object node);

    int getNodeLength(Object node);

    int getPositionFromLine(Object cu, int line);

    /**
     * 指示该对象所代表的类或接口是否为抽象类型
     * @param o
     * @return
     */
    int isTypeAbstract(Object o);

    Object getParent(Object o);


    /**
     * @param o class
     * @return
     */
    String getTypeName(Object o);

    /**
     * @param o MethodDeclaration
     * @return
     */
    String getMethodName(Object o);

    String getTypeNameOfChildrenASTNode(Object o);

    /**
     *
     * @param o method invocation
     * @return
     */
    String getMethodInvocationName(Object o);


//    String getMethodInvocationVar(Object o);

    List<String> getMethodInvocationVarAndName(Object o);

    String[] resolveTypeOfVariable(String variableName, Object stmtNode, MiningActionData mad);

    /**
     *
     * @param o class creation
     * @return
     */
    String getClassCreationName(Object o);

    /**
     *
     * @param fd field
     * @return
     */
    List<String> getFieldDeclarationNames(Object fd);

    /**
     *
     * @param o enum
     * @return
     */
    String getEnumName(Object o);

    /**
     *
     * @param fd field
     * @return
     */
    String getFieldType(Object fd);

    /**
     *
     * @param o single variable declaration
     * @return
     */
    List<Object> getSingleVariableDeclarations(Object o);

    /**
     *
     * @param o single variable declaration
     * @return
     */
    String getSingleVariableDeclarationName(Object o);

    /**
     *
     * @param o single variable declaration
     * @return
     */
    String getSingleVariableDeclarationTypeName(Object o);

    /**
     *
     * @param o method
     * @return
     */
    Object getMethodType(Object o);

    /**
     *
     * @param o class
     * @return
     */
    String getBaseType(Object o);

    /**
     *
     * @param o class
     * @return
     */
    List<String> getInterfaces(Object o);

    /**
     *
     * @param o class
     * @return
     */
    List<Object> getMethodsFromType(Object o);

    /**
     *
     * @param o class
     * @return
     */
    List<Object> getFieldFromType(Object o);


    /**
     * @param o
     * @param treeType
     * @return
     */
    MyRange getRange(Object o, int treeType);



    CanonicalName getCanonicalNameFromTree(Tree tree);




    Object searchBottomUpFindExpression(Object object);

    Object searchBottomUpFindNoneSimpleNameOrLiteralExpression(Object obj);

    Object searchBottomUpFindTypeDeclaration(Object body, boolean isDirect);

    Object searchBottomUpFindCompilationUnit(Object o);

    /**
     *
     * @param objNode
     * @param nodes corresponding node
     * @return
     */
    Object searchBottomUpFindCorresASTNode(Object objNode, int[] nodes);

    /**
     * 获取一个方法的参数列表
     * @param o
     * @return
     */
    List<MyParameters> getMethodDeclarationParameters(Object o);

    Tree findCommonRootNode(ITree node, List<Integer> nodeTypes);

    Object searchBottomUpFindBodyDeclaration(Object node, boolean isDirect);

    /**
     * 获得 object 的声明名字、变量声明列表、方法参数列表、静态方法块等内容
     * 内容取决于 object 是什么
     * @param object
     * @return String
     */
    String getBodyDeclarationUniqueKey(Object object);

    void getAllChildren(ITree tree,List<ITree> result);

}
