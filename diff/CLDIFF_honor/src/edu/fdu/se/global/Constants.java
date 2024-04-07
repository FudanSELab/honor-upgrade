package edu.fdu.se.global;

import org.eclipse.jgit.diff.DiffEntry;

public class Constants {


    public static final String LANG_PACKAGE_ASTNODEUTIL = "edu.fdu.se.lang.%s.ASTNodeUtil%s";
    public static final String LANG_PACKAGE_LOOKUPTBL = "edu.fdu.se.lang.%s.LookupTable%sDT";
    public static final String LANG_PACKAGE_PROCESSUTIL = "edu.fdu.se.lang.%s.ProcessUtil%s";
    public static final String LANG_PACKAGE_TREEGENERATOR = "edu.fdu.se.lang.%s.generatingactions.%sTreeGenerator";
    public static final String SINGLEFILECHANGETYPE = "edu.fdu.se.core.preprocessingfile.SingleFileSimpleChangeType";
    public static final String SINGLEFILECHANGETYPEMETHOD = "createMadOfSimple%sFileChange";
    public static final String META_JSON = "meta.json";

    public static final String LINK_JSON = "link.json";
    public static final String GRAPH_JSON = "graph-%s-%s.json";
    public static final String DOT_FILE = "graph.dot";
    public static final String PNG_FILE = "graph.png";

    public static final String DIFF_JSON_FILE = "Diff%s.json";

    public static final String TRAVERSEWAYS_PATH = "edu.fdu.se.core.miningactions.util";

    public static final String TRAVERSE_WAYS = "traverse_ways_tbl";

    public static final String STATEMENTS_PATH = "edu.fdu.se.core.miningactions.statement";

    public static final String DECLARATIONS_PATH = "edu.fdu.se.core.miningactions.declaration";
    public static final String EXPRESSIONS_PATH = "edu.fdu.se.core.miningactions.expression";

    public static final String MATCH_TOP_DOWN = "match_top_down";
    public static final String MATCH_BOTTOM_UP_NEW = "match_bottom_up_new_entity";
    public static final String MATCH_BOTTOM_UP_CURR = "match_bottom_up_curr_entity";


    public static final String MATCH_METHOD_SUFFIX_TOP_DOWN = "TopDown";
    public static final String MATCH_METHOD_SUFFIX_BOTTOM_UP_NEW = "BottomUpNew";
    public static final String MATCH_METHOD_SUFFIX_BOTTOM_UP_CURR = "BottomUpCurr";


    public static final String TYPE_NODES_TRAVERSAL = "edu.fdu.se.lang.%s.preprocess.TypeNodesTraversal%s";
    public static final String ONLINE_DIVIDER = "--xxx---fdse---xxx";

    public static class GRANULARITY {
        public static final String TYPE = "types";
        public static final String DECLARATION = "declarations";
        public static final String STATEMENT = "statements";
        public static final String EXPRESSION = "expressions";
    }


    public static final int COMMAND_LINE = 0;
    public static final int OFFLINE = 1;
    public static final int ONLINE = 2;
    public static final int EVALUATION = 3;

    public static String getChangeTypeString(DiffEntry.ChangeType changeType){
        String result = null;
        if(DiffEntry.ChangeType.MODIFY.equals(changeType)) {
            result = "MODIFY";
        }else if(DiffEntry.ChangeType.ADD.equals(changeType)) {
            result = "ADD";
        }else if(DiffEntry.ChangeType.DELETE.equals(changeType)) {
            result = "DELETE";
        } else if(DiffEntry.ChangeType.COPY.equals(changeType)){
            result = "COPY";
        }
        return result;
    }

    public static DiffEntry.ChangeType getChangeTpye(String changeType){
        if("MODIFY".equals(changeType)){
            return DiffEntry.ChangeType.MODIFY;
        }else if("ADD".equals(changeType)){
            return DiffEntry.ChangeType.ADD;
        }else if("DELETE".equals(changeType)){
            return DiffEntry.ChangeType.DELETE;
        }else if("COPY".equals(changeType)){
            return DiffEntry.ChangeType.COPY;
        }
        return null;
    }

    public static class ChangeTypeString {
        public static final String ADD = "ADD";
        public static final String MODIFY = "MODIFY";
        public static final String DELETE = "DELETE";
        public static final String COPY = "COPY";



    }

    public static String PARENTCOMMITNULL = "null";
    public static String TOOLSPLITTER = "__CLDIFF__";
    public static String ATSPLITTER = "@@@";

//    public static class ChangeTypeDescString {
//        public static final String ADD = "ADD";
//        public static final String MODIFY = "MODIFY";
//        public static final String DELETE = "DELETE";
//        public static final String COPY = "COPY";
//
//
//    }

    public static class RUNNING_LANG {
        public static final String C = "C";
        public static final String JAVA = "Java";

    }

    public static class SaveFilePath {
        public static final String PREV = "prev";
        public static final String CURR = "curr";
        public static final String GEN = "gen";

    }



    public static class NET {
        public static final String COMMIT_NAME = "commit_name";
        public static final String PROJECT_NAME = "project_name";
        public static final String GRAPH = "graph";
        public static final String FILE_NAME = "file_name";

        public static class BWChangeTypeString{
            public static final String ADD = "added";
            public static final String MODIFY = "modified";
            public static final String DELETE = "removed";
        }
    }


}
