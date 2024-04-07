package edu.fdu.se.lang.java;

import edu.fdu.se.lang.common.ILookupTbl;
import org.eclipse.jdt.core.dom.ASTNode;

import static org.eclipse.jdt.core.dom.ASTNode.*;

public class LookupTableJavaDT extends ILookupTbl {

    public LookupTableJavaDT() {
        super();
        initASTNodeInfo();
        initASTGranularities();
        parseMatchClass();
        initTraverseWaysTbl();

    }

    private void initASTNodeInfo() {
        int[] arrAnonymousClassDeclaration = {ANONYMOUS_CLASS_DECLARATION};
        int[] arrBodyDeclaration = {ANNOTATION_TYPE_DECLARATION, ENUM_DECLARATION, TYPE_DECLARATION, ANNOTATION_TYPE_MEMBER_DECLARATION, ENUM_CONSTANT_DECLARATION, FIELD_DECLARATION, INITIALIZER, METHOD_DECLARATION};
        int[] arrCatchClause = {CATCH_CLAUSE};
        int[] arrComment = {BLOCK_COMMENT, JAVADOC, LINE_COMMENT};
        int[] arrCompilationUnit = {COMPILATION_UNIT};
        int[] arrDimension = {DIMENSION};
        int[] arrExpression = {NORMAL_ANNOTATION, MARKER_ANNOTATION, SINGLE_MEMBER_ANNOTATION, ARRAY_ACCESS, ARRAY_CREATION,
                ARRAY_INITIALIZER, ASSIGNMENT, BOOLEAN_LITERAL, CAST_EXPRESSION,
                CHARACTER_LITERAL, CLASS_INSTANCE_CREATION, CONDITIONAL_EXPRESSION, CREATION_REFERENCE,
                EXPRESSION_METHOD_REFERENCE, FIELD_ACCESS, INFIX_EXPRESSION, INSTANCEOF_EXPRESSION,
                LAMBDA_EXPRESSION, METHOD_INVOCATION, SIMPLE_NAME, QUALIFIED_NAME, NULL_LITERAL, NUMBER_LITERAL, PARENTHESIZED_EXPRESSION,
                POSTFIX_EXPRESSION, PREFIX_EXPRESSION, STRING_LITERAL, SUPER_FIELD_ACCESS,
                SUPER_METHOD_INVOCATION, SUPER_METHOD_REFERENCE, THIS_EXPRESSION,
                TYPE_LITERAL, TYPE_METHOD_REFERENCE, VARIABLE_DECLARATION_EXPRESSION};
        int[] arrImportDeclaration = {IMPORT_DECLARATION};
        int[] arrMemberRef = {MEMBER_REF};
        int[] arrMemberValuePair = {MEMBER_VALUE_PAIR};
        int[] arrMethodRef = {METHOD_REF};
        int[] arrMethodRefParameter = {METHOD_REF_PARAMETER};
        int[] arrModifier = {MODIFIER};
        int[] arrPackageDeclaration = {PACKAGE_DECLARATION};
        int[] arrStatement = {ASSERT_STATEMENT, BLOCK, BREAK_STATEMENT, CONSTRUCTOR_INVOCATION,
                CONTINUE_STATEMENT, DO_STATEMENT, EMPTY_STATEMENT, ENHANCED_FOR_STATEMENT,
                EXPRESSION_STATEMENT, FOR_STATEMENT, IF_STATEMENT, LABELED_STATEMENT,
                RETURN_STATEMENT, SUPER_CONSTRUCTOR_INVOCATION, SWITCH_CASE,
                SWITCH_STATEMENT, SYNCHRONIZED_STATEMENT, THROW_STATEMENT,
                TRY_STATEMENT, TYPE_DECLARATION_STATEMENT, VARIABLE_DECLARATION_STATEMENT,
                WHILE_STATEMENT
        };
        int[] arrTagElement = {TAG_ELEMENT};
        int[] arrTextElement = {TEXT_ELEMENT};
        int[] arrType = {NAME_QUALIFIED_TYPE, PRIMITIVE_TYPE, QUALIFIED_TYPE, SIMPLE_TYPE, WILDCARD_TYPE, ARRAY_TYPE, INTERSECTION_TYPE, PARAMETERIZED_TYPE, UNION_TYPE};
        int[] arrTypeParameter = {TYPE_PARAMETER};
        int[] arrVariableDeclaration = {SINGLE_VARIABLE_DECLARATION, VARIABLE_DECLARATION_FRAGMENT};
        astDict.put("AnonymousClassDeclaration", arrAnonymousClassDeclaration);
        astDict.put("BodyDeclaration", arrBodyDeclaration);
        astDict.put("CatchClause", arrCatchClause);
        astDict.put("Comment", arrComment);
        astDict.put("CompilationUnit", arrCompilationUnit);
        astDict.put("Dimension", arrDimension);
        astDict.put("Expression", arrExpression);
        astDict.put("ImportDeclaration", arrImportDeclaration);
        astDict.put("MemberRef", arrMemberRef);
        astDict.put("MemberValuePair", arrMemberValuePair);
        astDict.put("MethodRef", arrMethodRef);
        astDict.put("MethodRefParameter", arrMethodRefParameter);
        astDict.put("Modifier", arrModifier);
        astDict.put("PackageDeclaration", arrPackageDeclaration);
        astDict.put("Statement", arrStatement);
        astDict.put("TagElement", arrTagElement);
        astDict.put("TextElement", arrTextElement);
        astDict.put("Type", arrType);
        astDict.put("TypeParameter", arrTypeParameter);
        astDict.put("VariableDeclaration", arrVariableDeclaration);
    }

    private void initASTGranularities() {
        int[] types = {TYPE_DECLARATION,ANNOTATION_TYPE_DECLARATION};
        int[] declarations = {ANNOTATION_TYPE_DECLARATION, ENUM_CONSTANT_DECLARATION, INITIALIZER,FIELD_DECLARATION,ENUM_DECLARATION,METHOD_DECLARATION};
        astNodeMap.put("types", types);
        astNodeMap.put("declarations", declarations);
        astNodeMap.put("statements", astDict.get("Statement"));
        int[] exps = astDict.get("Expression");
        int[] modifiers = astDict.get("Modifier");
        int[] catchClause = astDict.get("CatchClause");
        int[] res = new int[exps.length+modifiers.length+catchClause.length];
        System.arraycopy(exps,0,res,0,exps.length);
        System.arraycopy(modifiers,0,res,exps.length,modifiers.length);
        System.arraycopy(catchClause,0,res,exps.length+modifiers.length,catchClause.length);
        astNodeMap.put("expressions", res);
    }

    public int getCompilationUnitTypeId() {
        return COMPILATION_UNIT;
    }

    public int getTypeDeclarationTypeId() {
        return TYPE_DECLARATION;
    }




}
