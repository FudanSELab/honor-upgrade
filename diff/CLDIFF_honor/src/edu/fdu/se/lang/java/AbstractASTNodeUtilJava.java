package edu.fdu.se.lang.java;

import com.github.gumtreediff.tree.ITree;
import com.github.gumtreediff.tree.Tree;
import edu.fdu.se.ASTParser.JDTParserFactory;
import edu.fdu.se.core.links.linkbean.MyParameters;
import edu.fdu.se.global.Global;
import edu.fdu.se.lang.common.IASTNodeUtil;
import org.eclipse.jdt.core.dom.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Simple AST operation here
 */
public abstract class AbstractASTNodeUtilJava implements IASTNodeUtil {


    @Override
    public CompilationUnit getPrevCu(Object data) {
        return (CompilationUnit) data;
    }

    @Override
    public CompilationUnit getCurrCu(Object data) {
        return (CompilationUnit) data;
    }

    @Override
    public Object parseCu(String path) {
        return JDTParserFactory.getCompilationUnit(path);
    }


    @Override
    public Object parseCu(byte[] raw, String s) {
        return parseCu(raw);
    }

    @Override
    public Object parseCu(byte[] raw) {
        Object o = null;
        try {
            o = JDTParserFactory.getCompilationUnit(raw);
        } catch (Exception e) {

        }
        assert (o != null);
        return o;
    }

    @Override
    public int getLineNumber(Object o, int num) {
        assert (o instanceof CompilationUnit);
        CompilationUnit cu = (CompilationUnit) o;
        return cu.getLineNumber(num);
    }


    @Override
    public int getStartPosition(Object o) {
        ASTNode node = (ASTNode) o;
        return node.getStartPosition();
    }

    @Override
    public int getNodeLength(Object o) {
        ASTNode node = (ASTNode) o;
        return node.getLength();
    }


    /**
     * 调用的ast的getNodeType()方法返回的是一个int类型的值，这个值是一个枚举值，表示了当前节点的类型。
     * @param o ASTNode or Tree
     * @return
     */
    @Override
    public int getNodeTypeId(Object o) {
        if (o instanceof Tree tree){
            ASTNode astNode = (ASTNode) tree.getNode();
            return astNode.getNodeType();
        }else if(o instanceof ASTNode astNode){
            return astNode.getNodeType();
        }
        return 0;
    }

    @Override
    public String getMethodName(Object o) {
        if (o instanceof MethodDeclaration) {
            MethodDeclaration md = (MethodDeclaration) o;
            return md.getName().toString();
        }
        return null;
    }

    /**
     * 获取一个方法的参数列表
     * @param o MethodDeclaration
     * @return
     */
    @Override
    public List<MyParameters> getMethodDeclarationParameters(Object o){
        MethodDeclaration md = (MethodDeclaration) o;
        List<MyParameters> myParameters = new ArrayList<>();
        //获取一个方法的参数列表
        List params = md.parameters();
        for(Object svd :params){
            //判断是否是SingleVariableDeclaration类，用于表示方法或构造函数中的单个参数声明。
            if (Global.astNodeUtil.isSingleVariableDeclaration(svd)) {
                MyParameters myParam = new MyParameters(Global.astNodeUtil.getSingleVariableDeclarationName(svd), Global.astNodeUtil.getSingleVariableDeclarationTypeName(svd));
                myParameters.add(myParam);
            }
        }
        return myParameters;
    }


    @Override
    public String getEnumName(Object o) {
        EnumDeclaration md = (EnumDeclaration) o;
        return md.getName().toString();
    }

    @Override
    public String getMethodInvocationName(Object o) {
        if(o instanceof MethodInvocation) {
            MethodInvocation mi = (MethodInvocation) o;
            return mi.getName().toString();
        }else if(o instanceof SuperMethodInvocation){
            SuperMethodInvocation superMethodInvocation = (SuperMethodInvocation)o;
            return superMethodInvocation.getName().toString();
        }
        return null;
    }

    @Override
    public boolean isASTNodeSameAsClass(Object o, Class c) {
        if (o.getClass().equals(c)) {
            return true;
        }
        return false;
    }

    @Override
    public boolean isIf(Object o) {
        return isASTNodeSameAsClass(o, IfStatement.class);
    }

    @Override
    public boolean isTypeDeclaration(Object o) {
        return isASTNodeSameAsClass(o, TypeDeclaration.class);
    }

    @Override
    public boolean isMethodDeclaration(Object o) {
        return isASTNodeSameAsClass(o, MethodDeclaration.class);
    }

    @Override
    public boolean isFieldDeclaration(Object o) {
        return isASTNodeSameAsClass(o, FieldDeclaration.class);
    }

    @Override
    public boolean isEnumDeclaration(Object o) {
        return isASTNodeSameAsClass(o, EnumDeclaration.class);
    }

    @Override
    public boolean isMethodInvocation(Object o) {
        return isASTNodeSameAsClass(o, MethodInvocation.class);
    }

    @Override
    public boolean isClassInstanceCreation(Object o) {
        return isASTNodeSameAsClass(o, ClassInstanceCreation.class);
    }

    @Override
    public boolean isSingleVariableDeclaration(Object o) {
        return isASTNodeSameAsClass(o, SingleVariableDeclaration.class);
    }

    /**
     *
     * @param tree tree
     * @return
     */
    @Override
    public boolean isLiteralOrName(Tree tree) {
        int nodeId = getNodeTypeId(tree.getNode());
        return isLiteralOrName(nodeId);
    }

    @Override
    public boolean isLiteralOrName(int type) {
        boolean a = false;
        switch (type) {
            case ASTNode.CHARACTER_LITERAL:
            case ASTNode.BOOLEAN_LITERAL:
            case ASTNode.STRING_LITERAL:
            case ASTNode.NULL_LITERAL:
            case ASTNode.TYPE_LITERAL:
            case ASTNode.NUMBER_LITERAL:
            case ASTNode.SIMPLE_NAME:
            case ASTNode.QUALIFIED_NAME:
                a = true;
                break;
            default:
                break;
        }
        return a;
    }

    @Override
    public String literalOrSimpleNameAsString(Object object){
        ASTNode n;
        if(object instanceof Tree) {
            n = (ASTNode) ((Tree)object).getNode();
        }else{
            n = (ASTNode)object;
        }
        if (n instanceof Name) return ((Name) n).getFullyQualifiedName();
        if (n instanceof Type) return n.toString();
        if (n instanceof Modifier) return n.toString();
        if (n instanceof NullLiteral) return n.toString();
        if (n instanceof StringLiteral) return n.toString();
        if (n instanceof NumberLiteral) return ((NumberLiteral) n).getToken();
        if (n instanceof CharacterLiteral) return ((CharacterLiteral) n).getEscapedValue();
        if (n instanceof BooleanLiteral) return ((BooleanLiteral) n).toString();
        if (n instanceof InfixExpression) return ((InfixExpression) n).getOperator().toString();
        if (n instanceof PrefixExpression) return ((PrefixExpression) n).getOperator().toString();
        if (n instanceof PostfixExpression) return ((PostfixExpression) n).getOperator().toString();
        if (n instanceof Assignment) return ((Assignment) n).getOperator().toString();
        if (n instanceof TextElement) return n.toString();
        if (n instanceof TagElement) return ((TagElement) n).getTagName();
        return "";
    }

    @Override
    public boolean isCompilationUnit(Object o) {
        return isASTNodeSameAsClass(o, CompilationUnit.class);
    }

    @Override
    public boolean isBlock(Object o) {
        return isASTNodeSameAsClass(o, Block.class);
    }

    @Override
    public boolean isSwitchCase(Object o) {
        return isASTNodeSameAsClass(o, SwitchCase.class);
    }

    @Override
    public String getClassCreationName(Object o) {
        ClassInstanceCreation n = (ClassInstanceCreation) o;
        return n.getType().toString();
    }

    @Override
    public String getFieldType(Object o) {
        FieldDeclaration fd = (FieldDeclaration) o;
        return fd.getType().toString();
    }

    @Override
    public List<Object> getSingleVariableDeclarations(Object o) {
        MethodDeclaration md = (MethodDeclaration) o;
        return md.parameters();
    }

    @Override
    public String getSingleVariableDeclarationName(Object o) {
        SingleVariableDeclaration n = (SingleVariableDeclaration) o;
        return n.getName().toString();
    }

    @Override
    public String getSingleVariableDeclarationTypeName(Object o) {
        SingleVariableDeclaration n = (SingleVariableDeclaration) o;
        return n.getType().toString();
    }

    @Override
    public Object getMethodType(Object o) {
        MethodDeclaration md = (MethodDeclaration) o;
        return md.getReturnType2();
    }

    /**
     * 获得指定FieldDeclaration中变量声明的列表
     * @param o FieldDeclaration
     * @return
     */
    @Override
    public List<String> getFieldDeclarationNames(Object o) {
        FieldDeclaration fd = (FieldDeclaration) o;
        //返回字段声明中变量声明的列表
        List<VariableDeclarationFragment> list = fd.fragments();
        List<String> s = new ArrayList<>();
        for (VariableDeclarationFragment vd : list) {
            s.add(vd.getName().toString());
        }
        return s;
    }

    /**
     *
     * @param o class TypeDeclaration
     * @return
     */
    @Override
    public List<Object> getMethodsFromType(Object o) {
        TypeDeclaration n = (TypeDeclaration) o;
        return new ArrayList<>(Arrays.asList(n.getMethods()));
    }

    /**
     *
     * @param o class TypeDeclaration
     * @return
     */
    @Override
    public List<Object> getFieldFromType(Object o) {
        TypeDeclaration n = (TypeDeclaration) o;
        return new ArrayList<>(Arrays.asList(n.getFields()));
    }

    /**
     *
     * @param o Tree or ASTNode
     * @return
     */
    @Override
    public Object getParent(Object o){
        if (o instanceof Tree){
            Tree t = (Tree)o;
            return t.getParent();
        }
        if (o instanceof ASTNode){
            ASTNode astNode = (ASTNode)o;
            ASTNode parent = astNode.getParent();
            if(parent == null){
                //myflag
                if(Global.mad == null){
                    return null;
                }
                if(Global.mad.preCacheData.getDeletionKVMap() == null){
                    return null;
                }
                if(Global.mad.preCacheData.getDeletionKVMap().containsKey(astNode)){
                    return Global.mad.preCacheData.getDeletionKVMap().get(astNode);
                }
                return null;
            }
            return parent;
        }
        return null;
    }

    @Override
    public String getTypeName(Object o) {
        TypeDeclaration td = (TypeDeclaration) o;
        return td.getName().toString();
    }

    /**
     * 获取父类的toString()
     * @param o class
     * @return
     */
    @Override
    public String getBaseType(Object o) {
        TypeDeclaration td = (TypeDeclaration) o;
        List<String> s = new ArrayList<>();
        //获取父类
        Type aa = td.getSuperclassType();
        if (aa != null) {
            return aa.toString();
        }
        return null;
    }

    @Override
    public void getAllChildren(ITree tree,List<ITree> result){
        List<ITree> sub = tree.getChildren();
        result.add(tree);
        if(sub!=null && sub.size()!=0){
            for(ITree t:sub){
                getAllChildren(t,result);
            }
        }
    }


}
