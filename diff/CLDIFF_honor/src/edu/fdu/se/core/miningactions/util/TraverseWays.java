package edu.fdu.se.core.miningactions.util;


public class TraverseWays {

    public static final String TOPDOWNCLASSPATH = "edu.fdu.se.core.miningactions.util.DefaultTopDownTraversal";
    public static final String BOTTOMUPCLASSPATH = "edu.fdu.se.core.miningactions.util.DefaultBottomUpTraversal";


    public static class TopDown {
        public static final int ONE_TYPE = 1;
        public static final int CLASSD = 2;
        public static final int FIELD = 3;
        public static final int INITIALIZER = 4;
        public static final int METHOD = 5;
        public static final int IF = 6;
        public static final int TYPE_I = 7;
        public static final int TRY = 8;
        public static final int SWITCH = 9;
        public static final int SWITCH_CASE = 10;

        public static final int BREAK_CONTINUE = 11;

    }

    public static class BottomUp {
        public static final int CLASS_SIGNATURE = 101;
        public static final int METHOD_SIGNATURE = 102;
        public static final int FATHER_NODE_GET_SAME_NODE_ACTIONS = 103;
        public static final int IF_PREDICATE = 104;
        public static final int DO_WHILE = 105;
        public static final int SWITCH_CONDITION = 106;


    }
}

