package edu.fdu.se.lang.c;


import edu.fdu.se.lang.c.generatingactions.CParserVisitor;
import edu.fdu.se.lang.common.ILookupTbl;

import java.util.HashMap;

import static edu.fdu.se.lang.c.generatingactions.CParserVisitor.*;
import static org.eclipse.jdt.core.dom.ASTNode.COMPILATION_UNIT;
import static org.eclipse.jdt.core.dom.ASTNode.TYPE_DECLARATION;

public class LookupTableCDT extends ILookupTbl {

    public LookupTableCDT() {
        super();
        initASTNodeInfo();
        initASTGranularities();
        parseMatchClass();
        initTraverseWaysTbl();
    }


    private void initASTNodeInfo() {
        int[] types = {CParserVisitor.TYPE_DECLARATION};
        int[] declarations = {
                AST_AS_DECLARATION,
                AST_FUNCTION_DEFINITION,
                AST_PROBLEM_DECLARATION,
                AST_SIMPLE_DECLARATION,
                CPPASTALIAS_DECLARATION,
                CPPAST_EXPLICIT_TEMPLATE_INSTANTIATION,
                CPPAST_FUNCTION_DEFINITION,
                CPPAST_FUNCTION_WITH_TRY_BLOCK,
                CPPAST_LINK_AGE_SPECIFICATION,
                CPPAST_NAMESPACE_ALIAS,
                CPPAST_NAMESPACE_DEFINITION,
                CPPAST_STATIC_ASSERT_DECLARATION,
                CPPAST_TEMPLATE_DECLARATION,
                CPPAST_TEMPLATE_SPECIALIZATION,
                CPPAST_USING_DECLARATION,
                CPPAST_USING_DIRECTIVE,
                CPPAST_VISIBILITY_LABEL,
                GPPAST_EXPLICIT_TEMPLATE_INSTANTIATION,
                METHOD_DECLARATION,
                //FIELD_DECLARATION,
                ENUM_DECLARATION };
        int[] statements = {AST_BREAK_STATEMENT,
                AST_CASE_STATEMENT,
                AST_COMPOUND_STATEMENT,
                AST_CONTINUE_STATEMENT,
                AST_DECLARATION_STATEMENT,
                AST_DEFAULT_STATEMENT,
                AST_DO_STATEMENT,
                AST_EXPRESSION_STATEMENT,
                AST_FOR_STATEMENT,
                AST_GOTO_STATEMENT,
                AST_IF_STATEMENT,
                AST_LABEL_STATEMENT,
                AST_NULL_STATEMENT,
                AST_PROBLEM_STATEMENT,
                AST_RETURN_STATEMENT,
                AST_SWITCH_STATEMENT,
                AST_WHILE_STATEMENT,
                CPPAST_CATCH_HANDLER,
                CPPAST_COMPOUND_STATEMENT,
                CPPAST_FOR_STATEMENT,
                CPPAST_IF_STATEMENT,
                CPPAST_RANGE_BASED_FOR_STATEMENT,
                CPPAST_SWITCH_STATEMENT,
                CPPAST_TRY_BLOCK_STATEMENT,
                CPPAST_WHILE_STATEMENT,
                GNUAST_GOTO_STATEMENT,

                CPPAST_DECLARATION_STATEMENT,
                CAST_EXPRESSION_STATEMENT
        };

        int[] expressions = {
                AST_ARRAY_SUBSCRIPT_EXPRESSION ,
                AST_BINARY_EXPRESSION ,
                AST_BINARY_TYPE_ID_EXPRESSION ,
                AST_CAST_EXPRESSION ,
                AST_CONDITIONAL_EXPRESSION ,
                AST_EXPRESSION_LIST ,
                AST_FIELD_REFERENCE ,
                AST_FUNCTION_CALL_EXPRESSION ,
                AST_ID_EXPRESSION ,
                AST_LITERAL_EXPRESSION ,
                AST_PROBLEM_EXPRESSION ,
                AST_TYPE_ID_EXPRESSION ,
                AST_TYPE_ID_INITIALIZER_EXPRESSION ,
                AST_UNARY_EXPRESSION ,
                CAST_TYPE_ID_INITIALIZER_EXPRESSION ,
                CPPAST_ARRAY_SUBSCRIPT_EXPRESSION ,
                CPPAST_BINARY_EXPRESSION ,
                CPPAST_CAST_EXPRESSION ,
                CPPAST_DELETE_EXPRESSION ,
                CPPAST_EXPRESSION ,
                CPPAST_EXPRESSIONLIST ,
                CPPAST_FIELD_REFERENCE ,
                CPPAST_FUNCTION_CALL_EXPRESSION ,
                CPPAST_LAMBDA_EXPRESSION ,
                CPPAST_LITERAL_EXPRESSION ,
                CPPAST_NEW_EXPRESSION ,
                CPPAST_PACK_EXPANSION_EXPRESSION ,
                CPPAST_SIMPLE_TYPE_CONSTRUC_TO_REXPRESSION ,
                CPPAST_TYPE_ID_EXPRESSION ,
                CPPAST_TYPE_NAME_EXPRESSION ,
                CPPAST_UNARY_EXPRESSION ,
                GNUAST_COMPOUND_STATEMENT_EXPRESSION ,
                GNUAST_TYPE_ID_EXPRESSION ,
                GNUAST_UNARY_EXPRESSION ,
                GPPAST_BINARY_EXPRESSION ,

                BINARY_EXPRESSION ,
                ASSIGNMENT
        };
        int[] fafatherNodes = {
                AST_TRANSLATION_UNIT,
                TYPE_DECLARATION,
                METHOD_DECLARATION,
                FIELD_DECLARATION,
                ENUM_DECLARATION,
                AST_RETURN_STATEMENT,
                AST_DO_STATEMENT,
                AST_IF_STATEMENT,
                AST_WHILE_STATEMENT,
                AST_FOR_STATEMENT,
                CPPAST_TRY_BLOCK_STATEMENT,
                AST_SWITCH_STATEMENT,
                AST_CASE_STATEMENT,
                CPPAST_CATCH_HANDLER,
                AST_EXPRESSION_STATEMENT,
                AST_LABEL_STATEMENT
        };
        int[] findexpression = {};
        //astNodeMap = new HashMap<>();
        astDict.put("types", types);
        astDict.put("declarations", declarations);
        astDict.put("statements", statements);
        astDict.put("expressions", expressions);
        astDict.put("find_fafather_nodes", fafatherNodes);
        astDict.put("find_expression", findexpression);

    }

    private void initASTGranularities() {
//        int[] types = {TYPE_DECLARATION,ANNOTATION_TYPE_DECLARATION};
//        int[] declarations = {ANNOTATION_TYPE_DECLARATION, ENUM_CONSTANT_DECLARATION, INITIALIZER,FIELD_DECLARATION,ENUM_DECLARATION,METHOD_DECLARATION};
        astNodeMap.put("types", astDict.get("types"));
        astNodeMap.put("declarations", astDict.get("declarations"));
        astNodeMap.put("statements", astDict.get("statements"));
        astNodeMap.put("expressions", astDict.get("expressions"));
    }

    public int getCompilationUnitTypeId() {
        return COMPILATION_UNIT;
    }

    public int getTypeDeclarationTypeId() {
        return TYPE_DECLARATION;
    }


}
