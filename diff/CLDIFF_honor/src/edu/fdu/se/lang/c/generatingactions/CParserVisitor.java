package edu.fdu.se.lang.c.generatingactions;

import com.github.gumtreediff.tree.ITree;
import com.github.gumtreediff.tree.Tree;
import com.github.gumtreediff.tree.TreeContext;
import edu.fdu.se.core.preprocessingfile.data.PreCacheData;
import org.eclipse.cdt.core.dom.IName;
import org.eclipse.cdt.core.dom.ast.*;
import org.eclipse.cdt.core.dom.ast.c.ICASTDesignator;
import org.eclipse.cdt.core.dom.ast.cpp.*;
import org.eclipse.cdt.internal.core.dom.parser.ASTProblem;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTExpressionStatement;
import org.eclipse.cdt.internal.core.dom.parser.cpp.*;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Created by huangkaifeng on 2018/1/23.
 *
 */
public class CParserVisitor extends ASTVisitor {


    private int treeType;
    private PreCacheData preCacheData;

    public CParserVisitor(int treeType, PreCacheData preCacheData) {
        super();
        this.treeType = treeType;
        this.preCacheData = preCacheData;
    }


    public CParserVisitor() {
        super();
    }

//    @Override
//    public void preVisit(IASTNode n) {
//        pushNode(n, getLabel(n));
//    }

    private boolean inRemoveList(IASTNode node){
//        int a;
//        if(node == null)
//            a = 1;
//        if(node.getFileLocation() == null)
//            return false;
//        if(node.getFileLocation().getFileName() == null)
//            a = 3;
//
//        if(!node.getFileLocation().getFileName().replace("\\","/").equals(Global.repository+"prev/"+Global.name)
//        &&!node.getFileLocation().getFileName().replace("\\","/").equals(Global.repository+"cur/"+Global.name))
//            return true;
        IASTFileLocation f = node.getFileLocation();
        if(preCacheData.getDeletionKVMap().containsKey(node)){
            return true;
        }
        return false;
    }

    //Visit leave Pairs
    //1
    @Override
    public int visit(IASTTranslationUnit node){
        return visit0(node);
    }

    @Override
    public int leave(IASTTranslationUnit node){
        return leave0(node);
    }

    //2
    @Override
    public int 	visit(IASTArrayModifier node) {
        return visit0(node);
    }

    @Override
    public int 	leave(IASTArrayModifier node) {
        return leave0(node);
    }


    //3
    @Override
    public int 	visit(IASTAttribute node) {
        return visit0(node);
    }

    @Override
    public int 	leave(IASTAttribute node) {
        return leave0(node);
    }



    //4
    @Override
    public int 	visit(IASTDeclaration node) {
        return visit0(node);
    }

    @Override
    public int 	leave(IASTDeclaration node) {
        return leave0(node);
    }


    //5
    @Override
    public int 	visit(IASTDeclarator node) {
        return visit0(node);
    }

    @Override
    public int 	leave(IASTDeclarator node) {
        return leave0(node);
    }



    //6
    @Override
    public int 	visit(IASTDeclSpecifier node) {
        return visit0(node);
    }

    @Override
    public int 	leave(IASTDeclSpecifier node) {
        return leave0(node);
    }


    //7
    @Override
    public int 	visit(IASTEnumerationSpecifier.IASTEnumerator node) {
        return visit0(node);
    }

    @Override
    public int 	leave(IASTEnumerationSpecifier.IASTEnumerator node) {
        return leave0(node);
    }


    //8
    @Override
    public int 	visit(IASTExpression node) {
        return visit0(node);
    }

    @Override
    public int 	leave(IASTExpression node) {
        return leave0(node);
    }



    //9
    @Override
    public int 	visit(IASTInitializer node) {
        return visit0(node);
    }

    @Override
    public int 	leave(IASTInitializer node) {
        return leave0(node);
    }



    //10
    @Override
    public int 	visit(IASTName node) {
        return visit0(node);
    }

    @Override
    public int 	leave(IASTName node) {
        return leave0(node);
    }



    //11
    @Override
    public int 	visit(IASTParameterDeclaration node) {
        return visit0(node);
    }

    @Override
    public int 	leave(IASTParameterDeclaration node) {
        return leave0(node);
    }



    //12
    @Override
    public int 	visit(IASTPointerOperator node) {
        return visit0(node);
    }

    @Override
    public int 	leave(IASTPointerOperator node) {
        return leave0(node);
    }



    //13
    @Override
    public int 	visit(IASTProblem node) {
        return visit0(node);
    }

    @Override
    public int 	leave(IASTProblem node) {
        return leave0(node);
    }



    //14
    @Override
    public int 	visit(IASTStatement node) {
        return visit0(node);
    }

    @Override
    public int 	leave(IASTStatement node) {
        return leave0(node);
    }



    //15
    @Override
    public int 	visit(IASTToken node) {
        return visit0(node);
    }

    @Override
    public int 	leave(IASTToken node) {
        return leave0(node);
    }



    //16
    @Override
    public int 	visit(IASTTypeId node) {
        return visit0(node);
    }

    @Override
    public int 	leave(IASTTypeId node) {
        return leave0(node);
    }



    //17
    @Override
    public int 	visit(ICASTDesignator node) {
        return visit0(node);
    }

    @Override
    public int 	leave(ICASTDesignator node) {
        return leave0(node);
    }



    //18
    @Override
    public int 	visit(ICPPASTCapture node) {
        return visit0(node);
    }

    @Override
    public int 	leave(ICPPASTCapture node) {
        return leave0(node);
    }



    //19
    @Override
    public int 	visit(ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier node) {
        return visit0(node);
    }

    @Override
    public int 	leave(ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier node) {
        return leave0(node);
    }


    //20
    @Override
    public int 	visit(ICPPASTNamespaceDefinition node) {
        return visit0(node);
    }

    @Override
    public int 	leave(ICPPASTNamespaceDefinition node) {
        return leave0(node);
    }



    //21
    @Override
    public int 	visit(ICPPASTTemplateParameter node) {
        return visit0(node);
    }

    @Override
    public int 	leave(ICPPASTTemplateParameter node) {
        return leave0(node);
    }






    public int leave0(IASTNode node){
        popNode();
        return 3;
    }

    public int visit0(IASTNode node){
        if(inRemoveList(node)){
            return 1;
        }
        if(node.getFileLocation() == null)
            return 1;
        pushNode(node,getLabel((node)));
        return 3;
    }

    protected String getLabel(IASTNode n) {
        if (n instanceof IASTName) return ((IASTName) n).toString();
        if (n instanceof IASTTranslationUnit) return "*TranslationUnit*";
        if (n instanceof IASTLiteralExpression) return String.valueOf(((IASTLiteralExpression) n).getValue());
        if (n instanceof IASTBinaryExpression) return String.valueOf(((IASTBinaryExpression)n).getOperator());
        if(n instanceof  IASTUnaryExpression) return String.valueOf(((IASTUnaryExpression) n).getOperator());
        return "";
    }
    public static final int AST_TRANSLATION_UNIT = -1;
    //------------------- Declaration
    public static final int AST_AS_DECLARATION = 0;
    public static final int AST_FUNCTION_DEFINITION =1;
    public static final int AST_PROBLEM_DECLARATION =2;
    public static final int AST_SIMPLE_DECLARATION = 3;
    public static final int CPPASTALIAS_DECLARATION = 4;
    public static final int CPPAST_EXPLICIT_TEMPLATE_INSTANTIATION = 5;
    public static final int CPPAST_FUNCTION_DEFINITION = 6;
    public static final int CPPAST_FUNCTION_WITH_TRY_BLOCK = 7;
    public static final int CPPAST_LINK_AGE_SPECIFICATION = 8;
    public static final int CPPAST_NAMESPACE_ALIAS = 9;
    public static final int CPPAST_NAMESPACE_DEFINITION = 10;
    public static final int CPPAST_STATIC_ASSERT_DECLARATION = 11;
    public static final int CPPAST_TEMPLATE_DECLARATION = 12;
    public static final int CPPAST_TEMPLATE_SPECIALIZATION = 13;
    public static final int CPPAST_USING_DECLARATION = 14;
    public static final int CPPAST_USING_DIRECTIVE = 15;
    public static final int CPPAST_VISIBILITY_LABEL = 16;
    public static final int GPPAST_EXPLICIT_TEMPLATE_INSTANTIATION =17;

    public static final int TYPE_DECLARATION = 80;
    public static final int METHOD_DECLARATION = 81;
    public static final int FIELD_DECLARATION = 82;
    public static final int ENUM_DECLARATION = 83;
    //------------------  Statement
    public static final int AST_BREAK_STATEMENT = 18;
    public static final int AST_CASE_STATEMENT = 19;
    public static final int AST_COMPOUND_STATEMENT = 20;
    public static final int AST_CONTINUE_STATEMENT = 21;
    public static final int AST_DECLARATION_STATEMENT = 22;
    public static final int AST_DEFAULT_STATEMENT = 23;
    public static final int AST_DO_STATEMENT = 24;
    public static final int AST_EXPRESSION_STATEMENT = 25;
    public static final int AST_FOR_STATEMENT = 26;
    public static final int AST_GOTO_STATEMENT = 27;
    public static final int AST_IF_STATEMENT = 28;
    public static final int AST_LABEL_STATEMENT = 29;
    public static final int AST_NULL_STATEMENT = 30;
    public static final int AST_PROBLEM_STATEMENT = 31;
    public static final int AST_RETURN_STATEMENT = 32;
    public static final int AST_SWITCH_STATEMENT = 33;
    public static final int AST_WHILE_STATEMENT = 34;
    public static final int CPPAST_CATCH_HANDLER = 35;
    public static final int CPPAST_COMPOUND_STATEMENT = 36;
    public static final int CPPAST_FOR_STATEMENT = 37;
    public static final int CPPAST_IF_STATEMENT = 38;
    public static final int CPPAST_RANGE_BASED_FOR_STATEMENT = 39;
    public static final int CPPAST_SWITCH_STATEMENT = 40;
    public static final int CPPAST_TRY_BLOCK_STATEMENT = 41;
    public static final int CPPAST_WHILE_STATEMENT = 42;
    public static final int GNUAST_GOTO_STATEMENT = 43;

    public static final int CPPAST_DECLARATION_STATEMENT = 78;
    public static final int CAST_EXPRESSION_STATEMENT = 85;
    //------------------- Expression
    public static final int AST_ARRAY_SUBSCRIPT_EXPRESSION = 42;
    public static final int AST_BINARY_EXPRESSION = 43;
    public static final int AST_BINARY_TYPE_ID_EXPRESSION = 44;
    public static final int AST_CAST_EXPRESSION =45;
    public static final int AST_CONDITIONAL_EXPRESSION = 46;
    public static final int AST_EXPRESSION_LIST = 47;
    public static final int AST_FIELD_REFERENCE = 48;
    public static final int AST_FUNCTION_CALL_EXPRESSION = 49;
    public static final int AST_ID_EXPRESSION = 50;
    public static final int AST_LITERAL_EXPRESSION = 51;
    public static final int AST_PROBLEM_EXPRESSION = 52;
    public static final int AST_TYPE_ID_EXPRESSION = 53;
    public static final int AST_TYPE_ID_INITIALIZER_EXPRESSION = 54;
    public static final int AST_UNARY_EXPRESSION = 55;
    public static final int CAST_TYPE_ID_INITIALIZER_EXPRESSION = 56;
    public static final int CPPAST_ARRAY_SUBSCRIPT_EXPRESSION = 57;
    public static final int CPPAST_BINARY_EXPRESSION = 58;
    public static final int CPPAST_CAST_EXPRESSION = 59;
    public static final int CPPAST_DELETE_EXPRESSION = 60;
    public static final int CPPAST_EXPRESSION = 61;
    public static final int CPPAST_EXPRESSIONLIST =62;
    public static final int CPPAST_FIELD_REFERENCE = 63;
    public static final int CPPAST_FUNCTION_CALL_EXPRESSION = 64;
    public static final int CPPAST_LAMBDA_EXPRESSION = 65;
    public static final int CPPAST_LITERAL_EXPRESSION = 66;
    public static final int CPPAST_NEW_EXPRESSION = 67;
    public static final int CPPAST_PACK_EXPANSION_EXPRESSION = 68;
    public static final int CPPAST_SIMPLE_TYPE_CONSTRUC_TO_REXPRESSION = 69;
    public static final int CPPAST_TYPE_ID_EXPRESSION = 70;
    public static final int CPPAST_TYPE_NAME_EXPRESSION = 71;
    public static final int CPPAST_UNARY_EXPRESSION = 72;
    public static final int GNUAST_COMPOUND_STATEMENT_EXPRESSION = 73;
    public static final int GNUAST_TYPE_ID_EXPRESSION = 74;
    public static final int GNUAST_UNARY_EXPRESSION = 75;
    public static final int GPPAST_BINARY_EXPRESSION = 76;

    public static final int BINARY_EXPRESSION = 89;
    public static final int ASSIGNMENT = 88;

    // OTHERS
    public static final int CPP_BLOCK_SCOPE = 77;

    public static final int UNKNOWN = 79;

    public static final int NAME = 84;
    public static final int CPPAST_EQUALS_INITIALIER = 86;
    public static final int CPPAST_CONSTRUCTOR_INITIALIZER = 87;
//    public static final int SWITCH_STATEMENT = 11;
//    public static final int SWITCH_CASE = 12;
//    public static final int CATCH_CLAUSE = 13;
//    public static final int EXPRESSION_STATEMENT = 14;
//    public static final int LABELED_STATEMENT = 15;
//    public static final int CAST_EXPRESSION = 22;
//    public static final int EQUALS_INITIALIZER = 23;
//    public static final int FUNCTION_CALL_EXPRESSION = 24;
//    public static final int NEW_EXPRESSION = 25;
//    public static final int UNARY_EXPRESSION = 33;
//


    public static int getNodeTypeId(IASTNode n){
//        if(n instanceof IASTCompoundStatement){
//            int i = 1;
//        }
        // To Do
        //把下面的if语句变成switch

        if(n instanceof IASTTranslationUnit)
            return AST_TRANSLATION_UNIT;
        if (n instanceof IASTSimpleDeclaration && ((IASTSimpleDeclaration)n).getDeclSpecifier() instanceof IASTCompositeTypeSpecifier){
            return TYPE_DECLARATION;
        }
        if(n instanceof CPPASTNamespaceDefinition){
            return CPPAST_NAMESPACE_DEFINITION;
        }
        if(n instanceof CPPASTNamespaceAlias){
            return CPPAST_NAMESPACE_ALIAS;
        }
        if(n instanceof CPPASTExplicitTemplateInstantiation){
            return CPPAST_EXPLICIT_TEMPLATE_INSTANTIATION;
        }
        if(n instanceof ASTProblem){
            return AST_PROBLEM_DECLARATION;
        }
        if(n instanceof IASTFunctionDefinition){
            return METHOD_DECLARATION;
        }
        if (n instanceof IASTSimpleDeclaration && (((IASTSimpleDeclaration)n).getDeclSpecifier() instanceof IASTSimpleDeclSpecifier||((IASTSimpleDeclaration)n).getDeclSpecifier() instanceof IASTNamedTypeSpecifier)){
            return FIELD_DECLARATION;
        }
        if (n instanceof IASTSimpleDeclaration && ((IASTSimpleDeclaration)n).getDeclSpecifier() instanceof IASTEnumerationSpecifier){
            return ENUM_DECLARATION;
        }
        if(n instanceof IASTReturnStatement){
            return AST_RETURN_STATEMENT;
        }
        if(n instanceof IASTDoStatement){
            return AST_DO_STATEMENT;
        }
        if(n instanceof IASTIfStatement){
            return AST_IF_STATEMENT;
        }
        if(n instanceof IASTWhileStatement){
            return AST_WHILE_STATEMENT;
        }
        if(n instanceof IASTForStatement){
            return AST_FOR_STATEMENT;
        }
        if(n instanceof ICPPASTTryBlockStatement){
            return CPPAST_TRY_BLOCK_STATEMENT;
        }
        if(n instanceof IASTSwitchStatement){
            return AST_SWITCH_STATEMENT;
        }
        if(n instanceof IASTCaseStatement){
            return AST_CASE_STATEMENT;
        }
        if(n instanceof ICPPASTCatchHandler){
            return CPPAST_CATCH_HANDLER;
        }
        if(n instanceof IASTExpressionStatement){
            return AST_EXPRESSION_STATEMENT;
        }
        if(n instanceof IASTLabelStatement){
            return AST_LABEL_STATEMENT;
        }
        if(n instanceof  ICPPBlockScope){
            return  CPP_BLOCK_SCOPE;
        }
        if(n instanceof CPPASTDeclarationStatement){
            return CPPAST_DECLARATION_STATEMENT;
        }
        if(n instanceof CPPASTCompoundStatement){
            return CPPAST_COMPOUND_STATEMENT;
        }
        if(n instanceof IName){
            return NAME;
        }
        if(n instanceof IASTBreakStatement){
            return AST_BREAK_STATEMENT;
        }
        if(n instanceof IASTContinueStatement){
            return AST_CONTINUE_STATEMENT;
        }
        if(n instanceof CASTExpressionStatement) {
            return CAST_EXPRESSION_STATEMENT;
        }
        if(n instanceof CPPASTEqualsInitializer){
            return CPPAST_EQUALS_INITIALIER;
        }
        if(n instanceof IASTFunctionCallExpression){
            return AST_FUNCTION_CALL_EXPRESSION;
        }
        if(n instanceof  ICPPASTNewExpression){
            return CPPAST_NEW_EXPRESSION;
        }
        if(n instanceof  ICPPASTNewExpression){
            return CPPAST_NEW_EXPRESSION;
        }
        if(n instanceof CPPASTConstructorInitializer){
            return CPPAST_CONSTRUCTOR_INITIALIZER;
        }
        if(n instanceof IASTBinaryExpression){
            if(((IASTBinaryExpression) n).getOperator() == 17){
                return ASSIGNMENT;
            }
            return BINARY_EXPRESSION;
        }
        if(n instanceof IASTConditionalExpression){
            return AST_CONDITIONAL_EXPRESSION;
        }
        if(n instanceof IASTFieldReference){
            return AST_FIELD_REFERENCE;
        }
        if(n instanceof CPPASTLambdaExpression){
            return CPPAST_LAMBDA_EXPRESSION;
        }
        if(n instanceof IASTLiteralExpression) {
            return AST_LITERAL_EXPRESSION;
        }
        if(n instanceof IASTUnaryExpression) {
            return AST_UNARY_EXPRESSION;
        }
        return UNKNOWN;
    }

//    @Override
//    public boolean visit(TagElement e) {
//        return true;
//    }

//    @Override
//    public boolean visit(QualifiedName name) {
//        return false;
//    }

//    @Override
//    public boolean visit(MethodInvocation methodInvocation){
////        System.out.println(methodInvocation.toString());
////        if(methodInvocation.getName()!=null)
////            System.out.println("Method Name:"+methodInvocation.getName().toString());
////        if(methodInvocation.getExpression()!=null)
////            System.out.println("Expression:"+methodInvocation.getExpression().toString()+" "+methodInvocation.getExpression().getClass().getSimpleName());
////        if(methodInvocation.arguments()!=null)
////            System.out.println("Arguments:"+methodInvocation.arguments().toString());
////        System.out.println();
//        return true;
//    }


    //    @Override
//    public void postVisit(ASTNode n) {
//        popNode();
//    }
//
    protected TreeContext context = new TreeContext();

    private Deque<ITree> trees = new ArrayDeque<>();


    public TreeContext getTreeContext() {
        return context;
    }
    //
    protected void pushNode(IASTNode n, String label) {
        int type = getNodeTypeId(n);
        String typeName = n.getClass().getSimpleName();
        if(n.getFileLocation() != null) {
            push(type, typeName, label, n.getFileLocation().getNodeOffset(), n.getFileLocation().getNodeLength(), n);
        }
        else{
            System.out.println("anomaly found"+ label);
        }
    }
    //
    private void push(int type, String typeName, String label, int startPosition, int length, IASTNode node) {
        ITree t = context.createTree(type, label, node);
        t.setPos(startPosition);
        Tree tree = (Tree) t;
        tree.setTreePrevOrCurr(this.treeType);
        t.setLength(length);
        if (trees.isEmpty())
            context.setRoot(t);
        else {
            ITree parent = trees.peek();
            t.setParentAndUpdateChildren(parent);
        }

        trees.push(t);
    }
    private void push(int type, String typeName, String label, int startPosition, int length) {
        ITree t = context.createTree(type, label, typeName);
        t.setPos(startPosition);
        t.setLength(length);

        if (trees.isEmpty())
            context.setRoot(t);
        else {
            ITree parent = trees.peek();
            t.setParentAndUpdateChildren(parent);
        }

        trees.push(t);
    }

    //    protected ITree getCurrentParent() {
//        return trees.peek();
//    }
//
    protected void popNode() {
        trees.pop();
    }

    //    protected void pushFakeNode(EntityType n, int startPosition, int length) {
//        int type = -n.ordinal(); // Fake types have negative types (but does it matter ?)
//        String typeName = n.name();
//        push(type, typeName, "", startPosition, length);
//    }
}

