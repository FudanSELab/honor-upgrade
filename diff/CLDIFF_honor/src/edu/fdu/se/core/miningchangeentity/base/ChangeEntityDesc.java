package edu.fdu.se.core.miningchangeentity.base;

import com.github.gumtreediff.actions.model.Action;
import com.github.gumtreediff.actions.model.Delete;
import com.github.gumtreediff.actions.model.Insert;
import com.github.gumtreediff.actions.model.Move;

/**
 * Created by huangkaifeng on 2018/3/27.
 *
 */
public class ChangeEntityDesc {

    public static String getTreeTypeString(int treeType) {
        if (TreeType.PREV_TREE_NODE == treeType) {
            return "SRC";
        } else if (TreeType.CURR_TREE_NODE == treeType) {
            return "DST";
        }
        return null;
    }

    /**
     * 根据 opt 内容确定 TreeType
     * @param opt
     * @return
     */
    public static int optStringToTreeType(String opt){
        if(StageIIOpt.OPT_INSERT.equals(opt)){
            return TreeType.CURR_TREE_NODE;
        }else if(StageIIOpt.OPT_DELETE.equals(opt)){
            return TreeType.PREV_TREE_NODE;
        }
        return TreeType.BOTH;
    }

    public static int optStringToTreeType(Class opt){
        if(Insert.class.equals(opt)){
            return TreeType.CURR_TREE_NODE;
        }else if(Delete.class.equals(opt)){
            return TreeType.PREV_TREE_NODE;
        }else if(Move.class.equals(opt)){
            return TreeType.CURR_TREE_NODE;
        }
        return TreeType.BOTH;
    }

    public static final String SPLITTER = "__CLDIFF__";

    public static class TreeType {
        public static final int PREV_TREE_NODE = 3;
        public static final int CURR_TREE_NODE = 4;
        public static final int BOTH = 6;


    }
    public static class StageITraverseType{
        public static final int TRAVERSE_TOP_DOWN = 1;
        public static final int TRAVERSE_BOTTOM_UP = 2;
    }

    public static class StageIIOpt {
        public static final String OPT_INSERT = "Insert";
        public static final String OPT_MOVE = "Move";
        public static final String OPT_DELETE = "Delete";
        public static final String OPT_CHANGE = "Change";
        public static final String OPT_CHANGE_MOVE = "Change.Move";
        public static final String OPT_UPDATE = "Update";

        // opt 2 :
        public static final String OPT_ADD = "add";
        public static final String OPT_DEL = "delete";
        public static final String OPT_UPD = "update";
        public static final String OPT_MOV = "move";

    }


    public static class StageIIENTITY {

        public static final String ENTITY_CLASS = "ClassDeclaration";

        public static final String ENTITY_INNER_CLASS = "InnerClassDeclaration";

        public static final String ENTITY_INTERFACE = "Interface";

        public static final String ENTITY_FIELD = "FieldDeclaration";

        public static final String ENTITY_ENUM = "Enum";

        public static final String ENTITY_INITIALIZER = "Initializer";

        public static final String ENTITY_METHOD = "MethodDeclaration";

        public static final String ENTITY_ASSERT = "Assert";

        public static final String ENTITY_BREAK = "Break";

        public static final String ENTITY_CONTINUE = "Continue";

        public static final String ENTITY_EXPRESSION_STMT = "ExpressionStatement";

        public static final String ENTITY_CONSTRUCTOR_INVOCATION = "ConstructorInvocation";

        public static final String ENTITY_SUPER_CONSTRUCTOR_INVOCATION = "SuperConstructorInvocation";

        public static final String ENTITY_LABELED_STATEMENT = "LabeledStatement";

        public static final String ENTITY_FOR_STMT = "For";

        public static final String ENTITY_IF_STMT = "If";

        public static final String ENTITY_RETURN_STMT = "Return";

        public static final String ENTITY_SWITCH_STMT = "Switch";

        public static final String ENTITY_SWITCH_CASE = "SwitchCase";

        public static final String ENTITY_SYNCHRONIZED_STMT = "Synchronized";

        public static final String ENTITY_TRY_STMT = "Try";

        public static final String ENTITY_VARIABLE_STMT = "VariableDeclaration";

        public static final String ENTITY_WHILE_STMT = "While";

        public static final String ENTITY_DO_STMT = "Do";

        public static final String ENTITY_EMPTY_STMT = "Empty";

        public static final String ENTITY_THROW_STMT = "Throw";

        public static final String ENTITY_ENHANCED_FOR_STMT = "EnhancedFor";

        public static final String ENTITY_TYPE_DECLARATION_STMT = "TypeDeclaration(ClassDeclaration)";
        //not distinguish annotation
        public static final String ENTITY_MARKERANNOTATION_EXP = "MarkerAnnotation";
        public static final String ENTITY_NORMALANNOTATION_EXP = "NormalAnnotation";
        public static final String ENTITY_SINGLEMEMBERANNOTATION_EXP = "SingleMemberAnnotation";
        public static final String ENTITY_ARRAYACCESS_EXP = "ArrayAccess";
        public static final String ENTITY_ARRAYCREATION_EXP = "ArrayCreation";
        public static final String ENTITY_ARRAYINITIALIZER_EXP = "ArrayInitializer";
        public static final String ENTITY_ASSIGNMENT_EXP = "Assignment";
        public static final String ENTITY_BOOLEANLITERAL_EXP = "BooleanLiteral";
        public static final String ENTITY_CASTEXPRESSION_EXP = "CastExpression";
        public static final String ENTITY_CHARACTERLITERAL_EXP = "CharacterLiteral";
        public static final String ENTITY_CLASSINSTANCECREATION_EXP = "ClassInstanceCreation";
        public static final String ENTITY_CONDITIONALEXPRESSION_EXP = "ConditionalExpression";
        public static final String ENTITY_FIELDACCESS_EXP = "FieldAccess";
        public static final String ENTITY_INFIXEXPRESSION_EXP = "InfixExpression";
        public static final String ENTITY_INSTANCEOFEXPRESSION_EXP = "InstanceOfExpression";
        public static final String ENTITY_LAMBDAEXPRESSION_EXP = "LambdaExpression";
        public static final String ENTITY_METHODINVOCATION_EXP = "MethodInvocation";
        public static final String ENTITY_QUALIFIEDNAME_EXP = "QualifiedName";
        public static final String ENTITY_SIMPLENAME_EXP = "SimpleName";
        public static final String ENTITY_NULLLITERAL_EXP = "NullLiteral";
        public static final String ENTITY_NUMBERLITERAL_EXP = "NumberLiteral";
        public static final String ENTITY_PARENTHESIZEDEXPRESSION_EXP = "ParenthesizedExpression";
        public static final String ENTITY_POSTFIXEXPRESSION_EXP = "PostfixExpression";
        public static final String ENTITY_STRINGLITERAL_EXP = "StringLiteral";
        public static final String ENTITY_SUPERFIELDACCESS_EXP = "SuperFieldAccess";
        public static final String ENTITY_SUPERMETHODINVOCATION_EXP = "SuperMethodInvocation";
        public static final String ENTITY_THISEXPRESSION_EXP = "ThisExpression";
        public static final String ENTITY_TYPELITERAL_EXP = "TypeLiteral";
        public static final String ENTITY_VARIABLEDECLARATIONEXPRESSION_EXP = "VariableDeclarationExpression";

        public static final String ENTITY_MODIFIER_EXP = "Modifier";


        // C
        public static final String ENTITY_SIMPLEDECLARATION = "SimpleDeclaration";
        public static final String ENTITY_DECLARATION = "Declaration";
        public static final String ENTITY_FUNCTIONDEFINITION = "FunctionDefinition";



    }



    public static String getChangeEntityDescString(Action a){
        switch (a.getClass().getSimpleName()){
            case "Insert":return StageIIOpt.OPT_INSERT;
            case "Move":return StageIIOpt.OPT_MOVE;
            case "Delete":return StageIIOpt.OPT_DELETE;
            case "Update":return StageIIOpt.OPT_CHANGE;
        }
        return null;
    }

    /**
     * @param type
     * @return Insert Delete Move Change
     */
    public static String getKeyNameByValue(String type){
        return type;
    }



    public static class StageIISub {

        public static final String SUB_DECLARATION = "declaration";


        /**
         * 针对类似于if结构
         */
        public static final String SUB_CONDITION = "condition";

        public static final String SUB_CONDITION_AND_BODY = "condition and body";

        public static final String SUB_CONDITION_AND_PARTIAL_BODY = "condition and body(with moved statements)";

        public static final String SUB_ELSE = "Else";

        /**
         * 针对class
         */

        public static final String SUB_SIGNATURE = "Signature";

        public static final String SUB_SIGNATURE_AND_BODY = "Signature and body";

        /**
         * Try
         */
        public static final String SUB_TRY_WITH = "try_with clause";
        public static final String SUB_CATCH_CLAUSE = "Catch clause";
        public static final String SUB_BODY_AND_CATCH_CLAUSE = "declaration and Catch Clause";

        public static final String SUB_BODY_AND_CATCH_CLAUSE_AND_FINALLY = "declaration and Catch Clause and Finally";

        public static final String SUB_FINALLY = "Finally";


        /**
         * switch
         */
        public static final String SUB_SWITCH_CASE = "switch case";
        public static final String SUB_SWITCH_CASE_DEFAULT = "default switch case";

    }


    public static class StageIIGranularity{
        public static final String GRANULARITY_MEMBER = "Member";
        public static final String GRANULARITY_CLASS = "ClassOrInterface";
        public static final String GRANULARITY_STATEMENT = "Statement";
        public static final String GRANULARITY_EXPRESSION = "Expression";
    }




    public static class StageIIGenStage{

        public static final String ENTITY_GENERATION_STAGE_PRE_DIFF = "PRE_DIFF";
        public static final String ENTITY_GENERATION_STAGE_GT_TD = "TOP_DOWN";
        public static final String ENTITY_GENERATION_STAGE_GT_BUD = "BOTTOM_UP_DOWN";
    }


    public static class StageIIILinkType {
        public static final String DEF_USE = "Def-Use: %s invoked in %s";
        public static final String ABSTRACT_METHOD = "Abstract-Method: %s implemented abstract method in %s";
        public static final String OVERRIDE_METHOD = "Override-Method: %s overridden method in %s";
        public static final String IMPLEMENT_METHOD = "Implement-Method: %s implemented interface in %s";
        public static final String SYSTEMATIC_CHANGE = "Systematic-Change";
    }

    public static class StageIIIKeys{
        public static final String ID = "id";
        public static final String KEYY = "key";
        public static final String FILE = "file";
        public static final String RANGE = "range";
        public static final String TYPE1 = "type1";
        public static final String TYPE2 = "type2";
        public static final String DESCRIPTION = "description";
        public static final String SUB_RANGE = "sub-range";
        public static final String SUB_RANGE_CODE = "sub-range-code";
        public static final String SUB_TYPE  = "sub-type";
        public static final String OPT2EXP2 = "opt2-exp2";
    }



    public static class StageIIIFile{
        public static final String SRC = "src";
        public static final String DST = "dst";
        public static final String SRC_DST = "src-dst";
    }




}
