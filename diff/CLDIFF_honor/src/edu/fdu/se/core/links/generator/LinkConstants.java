package edu.fdu.se.core.links.generator;

public class LinkConstants {
    /**
     * link file keys
     */
    public static final String LINK_TYPE = "link-type";
    public static final String LINK_TYPE_1 = "one-file-link";
    public static final String LINK_TYPE_2 = "two-file-link";
    public static final String FILE_NAME_A = "file-name";
    public static final String FILE_NAME_B = "file-name2";
    public static final String PARENT_COMMIT_A = "parent-commit";
    public static final String PARENT_COMMIT_B = "parent-commit2";
    public static final String LINK_KEY = "links";

    public static final String FROM = "from";
    public static final String TO = "to";

    public static final String DESC = "desc";

    public static final int DEF_FIELD = 0;
    public static final int DEF_LOCAL_VAR = 1;
    public static final int DEF_METHOD = 2;
    public static final int DEF_CLASS = 3;

    public static final int USE_LOCAL_VAR = 10;
    public static final int USE_FIELD = 11;
    public static final int USE_FIELD_LOCAL = 103;
    public static final int USE_METHOD_INVOCATION = 12;
    public static final int USE_CLASS_CREATION = 13;
    public static final int USE_PARAMS = 100;
    public static final int USE_SUPER_TYPE = 101;
    public static final int USE_STATIC_IMPORT_FIELD = 102;

    public static final int INSIDE_USE = 14;
    public static final int AMONG_USE = 15;

    public static final int IS_INTERFACE = 20;
    public static final int IS_REGULAR_CLASS = 21;
    public static final int IS_ABSTRACT_CLASS = 22;


    public static String getLinkDescString(int type) {
        String res = null;

        switch (type) {
            case LINK_DEF_USE:
                res = "defUse";
                break;
            case LINK_DEF_USE_TAICU:
                res = "defUseTaicu";
                break;
            case LINK_DEF_USE_TAIXI:
                res = "defUseTaixi";
                break;
            case LINK_DEF_USE_CF:
                res = "defUseControl";
                break;
            case LINK_DEF_USE_DF:
                res = "defUseData";
                break;
            case LINK_DEF_USE_CLASS:
                res = "defUseClass";
                break;
            case LINK_DEF_USE_FIELD:
                res = "defUseField";
                break;
            case LINK_DEF_USE_METHOD:
                res = "defUseMethod";
                break;
            case LINK_ABSTRACT_METHOD:
                res = "abstractMethod";
                break;
            case LINK_OVERRIDE_METHOD:
                res = "overrideMethod";
                break;
            case LINK_IMPLEMENTING_INTERFACE:
                res = "implementInterface";
                break;
            case LINK_SYSTEMATIC:
                res = "systematic";
                break;
        }
        return res;
    }

    public static final int LINK_DEF_USE = 5;
    public static final int LINK_DEF_USE_TAICU = 51;
    public static final int LINK_DEF_USE_TAIXI = 52;
    public static final int LINK_DEF_USE_CF = 53;
    public static final int LINK_DEF_USE_DF = 54;
    public static final int LINK_DEF_USE_CLASS = 55;
    public static final int LINK_DEF_USE_METHOD = 56;
    public static final int LINK_DEF_USE_FIELD = 57;

    public static final int LINK_ABSTRACT_METHOD = 41;
    public static final int LINK_OVERRIDE_METHOD = 42;
    public static final int LINK_IMPLEMENTING_INTERFACE = 43;
    public static final int LINK_SYSTEMATIC = 44;



}
