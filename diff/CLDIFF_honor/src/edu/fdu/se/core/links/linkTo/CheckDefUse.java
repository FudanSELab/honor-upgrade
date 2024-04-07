package edu.fdu.se.core.links.linkTo;

import edu.fdu.se.core.links.generator.LinkConstants;
import edu.fdu.se.core.links.linkbean.*;
import edu.fdu.se.core.miningactions.util.AstRelations;
import edu.fdu.se.core.miningchangeentity.base.ChangeEntity;
import edu.fdu.se.core.miningchangeentity.base.ChangeEntityDesc;
import edu.fdu.se.core.miningchangeentity.base.ExpressionPlusChangeEntity;
import edu.fdu.se.core.miningchangeentity.base.StatementPlusChangeEntity;
import edu.fdu.se.core.miningchangeentity.member.ClassChangeEntity;
import edu.fdu.se.core.miningchangeentity.member.FieldChangeEntity;
import edu.fdu.se.core.miningchangeentity.member.MethodChangeEntity;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CheckDefUse {

    /**
     * 太粗
     *
     * @param defList
     * @param useList
     * @param isCrossFile
     * @param arrLink
     */
    public static void checkMethodToMethodFineGrainedDefUse(List<ChangeEntity> defList, List<ChangeEntity> useList, int isCrossFile, List<Link> arrLink) {
        for (int i = 0; i < defList.size(); i++) {
            ChangeEntity def1 = defList.get(i);
            for (int j = 0; j < useList.size(); j++) {
                ChangeEntity use2 = useList.get(j);
                if (isOptConflict(def1, use2)) {
                    continue;
                }
                if (def1.getChangeEntityId() == use2.getChangeEntityId()) {
                    continue;
                }
                boolean flag = false;
                int linkType = 0;
                if(use2 instanceof MethodChangeEntity){
                    if(def1 instanceof MethodChangeEntity){
                        flag = true;
                        linkType = LinkConstants.DEF_METHOD;
                    }else if(def1 instanceof ClassChangeEntity){
                        flag = true;
                        linkType = LinkConstants.DEF_CLASS;
                    }else if(def1 instanceof FieldChangeEntity){
                        flag = true;
                        linkType = LinkConstants.DEF_FIELD;
                    }
                }
                if (flag) {
                    for (Def def : def1.linkBean.defList.getDefs()) {
                        for (Use invokes : use2.linkBean.useList.getUses()) {
                            if(invokes.useTypeInt == LinkConstants.USE_METHOD_INVOCATION){
                                // 方法的def use
                                if(def.defTypeInt != LinkConstants.DEF_METHOD){
                                    continue;
                                }
                                checkMethodUseToDef(def1,use2,def,invokes,arrLink,isCrossFile,LinkConstants.LINK_DEF_USE_TAICU);
                            } else {
                                //other def use
                                if(invokes.useTypeInt == LinkConstants.USE_FIELD_LOCAL && isCrossFile == LinkConstants.AMONG_USE){
                                    continue;
                                }
                                if(invokes.useTypeInt == LinkConstants.USE_FIELD && def.defTypeInt == LinkConstants.DEF_METHOD){
                                    continue;
                                }
                                if(invokes.useTypeInt == LinkConstants.USE_FIELD_LOCAL && def.defTypeInt == LinkConstants.DEF_METHOD){
                                    continue;
                                }
                                if(invokes.useTypeInt == LinkConstants.USE_FIELD_LOCAL && def.defTypeInt == LinkConstants.DEF_FIELD){
                                    if(!isClassMatch(def.defLoc,invokes.locClass)){
                                        continue;
                                    }
                                }
//
                                ifEqual(def, invokes, isCrossFile, def1, use2, arrLink, LinkConstants.LINK_DEF_USE_TAICU);

                            }
                        }
                    }
                }
            }
        }
    }

    /**
     *
     * @param a def
     * @param b use
     * @return
     */
    private static boolean isClassMatch(String a,String b){
        int aa = a.lastIndexOf(".");
        if(aa == -1){
            aa = a.length();
        }
        int bb = b.lastIndexOf(".");
        if(bb == -1){
            bb = b.length();
        }
        String prefixA = a.substring(0,aa);
        String prefixB = b.substring(0,bb);
        if(prefixA.equals(prefixB)){
            return true;
        }
        return false;
    }


    /**
     * 太细
     *
     * @param typeName
     * @param taiXiMethod
     * @param defList
     * @param useList
     * @param isCrossFile
     * @param arrLink
     */
    public static void checkStmtToStmtResidingMethod(String typeName, String taiXiMethod, List<MyParameters> myParameters, List<ChangeEntity> defList, List<ChangeEntity> useList, int isCrossFile, List<Link> arrLink) {
        for (int j = 0; j < useList.size(); j++) {
            ChangeEntity useCE = useList.get(j);
            if (useCE instanceof StatementPlusChangeEntity || useCE instanceof MethodChangeEntity || useCE instanceof ClassChangeEntity) {
                for (Use use2 : useCE.linkBean.useList.getUses()) {
                    if (use2.varName.equals(taiXiMethod)) {
                        String desc = String.format(ChangeEntityDesc.StageIIILinkType.DEF_USE, use2.varName, taiXiMethod);
                        if (use2 instanceof MethodUse) {
                            MethodUse mu = (MethodUse) use2;
                            if (mu.useMethodVarTypeName != null && typeName != null) {
                                if (!mu.useMethodVarTypeName.equals(typeName)) {
                                    continue;
                                }
                            }
                            // param list should match
                            if (myParameters.size() != mu.paramList.size()) {
                                continue;
                            }
                        } else{
                            if(use2.useTypeInt == LinkConstants.USE_FIELD_LOCAL || use2.useTypeInt == LinkConstants.USE_FIELD || use2.useTypeInt == LinkConstants.USE_LOCAL_VAR) {
                                continue;
                            }
                        }
                        for (int i = 0; i < defList.size(); i++) {
                            ChangeEntity defCE = defList.get(i);
                            if (isOptConflict(useCE, defCE)) {
                                continue;
                            }
                            Link link = new Link(defCE, useCE, LinkConstants.LINK_DEF_USE_TAIXI, desc, isCrossFile);
                            arrLink.add(link);
                        }
                    }
                }
            }
        }
    }

    private static boolean isOptConflict(ChangeEntity ce1, ChangeEntity ce2) {
        String opt1 = ce1.getStageIIBean().getOpt();
        String opt2 = ce2.getStageIIBean().getOpt();
        if (ChangeEntityDesc.StageIIOpt.OPT_INSERT.equals(opt1) && ChangeEntityDesc.StageIIOpt.OPT_DELETE.equals(opt2)) {
            return true;
        } else if (ChangeEntityDesc.StageIIOpt.OPT_INSERT.equals(opt2) && ChangeEntityDesc.StageIIOpt.OPT_DELETE.equals(opt1)) {
            return true;
        }
        return false;
    }

    /**
     * loca var def use
     *
     * @param defList
     * @param useList
     */
    public static void checkStmtLocalVarDefUse
    (List<ChangeEntity> defList, List<ChangeEntity> useList, List<Link> arrLink) {
        for (int i = 0; i < defList.size(); i++) {
            ChangeEntity defCE = defList.get(i);
            for (int j = 0; j < useList.size(); j++) {
                ChangeEntity useCE = useList.get(j);
                if (defCE instanceof StatementPlusChangeEntity && useCE instanceof StatementPlusChangeEntity) {
                    if (isOptConflict(defCE, useCE)) {
                        continue;
                    }
                    localVarDefUse(defCE, useCE, arrLink);
                }
            }
        }
    }

    private static void localVarDefUse(ChangeEntity def, ChangeEntity use, List<Link> arrList) {
        isDefUsePairList(def, use, arrList);
    }

    private static List<Link> isDefUsePairList(ChangeEntity def1, ChangeEntity use2, List<Link> arrList) {
        if (def1.linkBean.defList != null && use2.linkBean.useList != null) {
            Set<String> defUseVar = new HashSet<>();
            for (Def def : def1.linkBean.defList.getDefs()) {
                for (Use use : use2.linkBean.useList.getUses()) {
                    Link link = isDefUsePair(def1, use2, def, use);
                    if (link != null && !(defUseVar.contains(def.varName))) {
                        defUseVar.add(def.varName);
                        arrList.add(link);
                    }
                }
            }
        }
        return null;
    }

    private static Link isDefUsePair(ChangeEntity a, ChangeEntity b, Def def, Use use) {
        if (def.defTypeInt == LinkConstants.DEF_LOCAL_VAR && use.useTypeInt == LinkConstants.USE_LOCAL_VAR) {
            if (def.varName.equals(use.varName)) {
                int rangeInt = AstRelations.rangeCompare(a.lineRange, b.lineRange);
                if ((def.defTree == ChangeEntityDesc.TreeType.PREV_TREE_NODE && use.useTree == ChangeEntityDesc.TreeType.CURR_TREE_NODE) || (def.defTree == ChangeEntityDesc.TreeType.CURR_TREE_NODE && use.useTree == ChangeEntityDesc.TreeType.PREV_TREE_NODE) || (def.defTree == ChangeEntityDesc.TreeType.BOTH && use.useTree == ChangeEntityDesc.TreeType.PREV_TREE_NODE)) {
                    return null;
                }
                if (rangeInt > 0) {
                    String desc = String.format(ChangeEntityDesc.StageIIILinkType.DEF_USE, def.varName, def.defLoc);
                    Link link = new Link(a, b, LinkConstants.LINK_DEF_USE, desc, LinkConstants.INSIDE_USE);
                    return link;
                }
            }
        }
        return null;
    }


    /**
     * method def use
     *
     * @param defMethod
     * @param useList
     */
    public static void checkStmtExpMethodDefUse(List<ChangeEntity> defMethod, List<ChangeEntity> useList,
                                                int isCrossFile, List<Link> arrLink) {
        for (ChangeEntity methodChange : defMethod) {
            if (methodChange instanceof MethodChangeEntity) {
                for (ChangeEntity stmt : useList) {
                    if (stmt instanceof StatementPlusChangeEntity || stmt instanceof ExpressionPlusChangeEntity) {
                        if (isOptConflict(stmt, methodChange)) {
                            continue;
                        }
                        stmtExpMethodDefUse(stmt, (MethodChangeEntity) methodChange, arrLink, isCrossFile);
                    }
                }
            }
        }
    }

    private static void stmtExpMethodDefUse(ChangeEntity stmt, MethodChangeEntity method, List<Link> arrLink,
                                            int isCrossFile) {
        DefList defs = method.linkBean.defList;
        for (Def def : defs.getDefs()) {
            for (Use invokes : stmt.linkBean.useList.getUses()) {
                if (def.defTypeInt == LinkConstants.DEF_METHOD && invokes.useTypeInt == LinkConstants.USE_METHOD_INVOCATION) {
                    checkMethodUseToDef(method,stmt,def,invokes,arrLink,isCrossFile,LinkConstants.LINK_DEF_USE_METHOD);
                }
            }
        }
    }

    private static void checkMethodUseToDef(ChangeEntity defCE,ChangeEntity useCE,Def def,Use invokes,List<Link> arrLink,int isCrossFile, int linkType){
        MethodUse methodUse = (MethodUse) invokes;
        if (methodUse.useMethodVarName == null || "unknown".equals(methodUse.useMethodVarName)) {
            ifEqual(def, invokes, isCrossFile, defCE, useCE, arrLink, linkType);
        } else {
            if (methodUse.useMethodVarTypeName == null) {
                ifEqual(def, invokes, isCrossFile, defCE, useCE, arrLink, linkType);
            } else {
                if (def.defLoc.contains(methodUse.useMethodVarTypeName)) {// same class
                    if (def.myParameters.size() == ((MethodUse) invokes).paramList.size()) {
                        ifEqual(def, invokes, isCrossFile, defCE, useCE, arrLink, linkType);
                    }
                }
            }
        }
    }

    /**
     * field def use
     *
     * @param stmts
     * @param defField
     */
    public static void checkStmtExpFieldDefUse(List<ChangeEntity> defField, List<ChangeEntity> stmts, int isCrossFile, List<Link> arrLink) {
        for (ChangeEntity fieldChange : defField) {
            if (fieldChange instanceof FieldChangeEntity) {
                for (ChangeEntity ce : stmts) {
                    if (ce instanceof StatementPlusChangeEntity || ce instanceof ExpressionPlusChangeEntity) {
                        if (isOptConflict(fieldChange, ce)) {
                            continue;
                        }
                        stmtExpFieldDefUse(ce, (FieldChangeEntity) fieldChange, arrLink, isCrossFile);
                    }
                }
            }
        }
    }

    private static void stmtExpFieldDefUse(ChangeEntity stmt, FieldChangeEntity field, List<Link> arrLink,
                                           int isCrossFile) {
        DefList defs = field.linkBean.defList;
        for (Def def : defs.getDefs()) {
            for (Use use : stmt.linkBean.useList.getUses()) {
                if (def.defTypeInt == LinkConstants.DEF_FIELD && use.useTypeInt == LinkConstants.USE_FIELD) {
                    if(use.qualifier !=null){
                        if(!def.defLoc.contains(use.qualifier)){
                            continue;
                        }
                    }
                    ifEqual(def, use, isCrossFile, field, stmt, arrLink, LinkConstants.LINK_DEF_USE_FIELD);
                }
                if (def.defTypeInt == LinkConstants.DEF_FIELD && use.useTypeInt == LinkConstants.USE_FIELD_LOCAL && isCrossFile == LinkConstants.INSIDE_USE) {
                    ifEqual(def, use, isCrossFile, field, stmt, arrLink, LinkConstants.LINK_DEF_USE_FIELD);
                }
                if (LinkConstants.AMONG_USE == isCrossFile) {
                    if (use.useTypeInt == LinkConstants.USE_STATIC_IMPORT_FIELD) {
                        String a = def.defLoc;
                        StaticFieldUse sfu = (StaticFieldUse) use;
                        String b = sfu.className + "." + sfu.varName;
                        if (a.contains(b)) {
                            ifEqual(def, use, isCrossFile, field, stmt, arrLink, LinkConstants.LINK_DEF_USE_FIELD);
                        }
                    }
                }

            }
        }
    }

    /**
     * class def use
     *
     * @param defClass
     * @param useList
     */
    public static void checkStmtExpClassDefUse(List<ChangeEntity> defClass, List<ChangeEntity> useList,
                                               int isCrossFile, List<Link> arrLink) {
        for (ChangeEntity classChange : defClass) {
            if (classChange instanceof ClassChangeEntity) {
                for (ChangeEntity ce : useList) {
                    if (ce instanceof StatementPlusChangeEntity || ce instanceof ExpressionPlusChangeEntity) {
                        if (isOptConflict(classChange, ce)) {
                            continue;
                        }
                        stmtExpClassDefUse(ce, (ClassChangeEntity) classChange, arrLink, isCrossFile);
                    }
                }
            }
        }
    }

    private static void stmtExpClassDefUse(ChangeEntity ce, ClassChangeEntity cce, List<Link> arrLink,
                                           int isCrossFile) {
        for (Use use : ce.linkBean.useList.getUses()) {
            for (Def def : cce.linkBean.defList.getDefs()) {
                ifEqual(def, use, isCrossFile, cce, ce, arrLink, LinkConstants.LINK_DEF_USE_CLASS);
            }
        }

    }


    /**
     * exp field,method,method, taicu
     *
     * @param def
     * @param use
     * @param isCrossFile
     * @param defCE
     * @param useCe
     * @param arrLink
     * @param linkType
     */
    private static void ifEqual(Def def, Use use, int isCrossFile, ChangeEntity defCE, ChangeEntity
            useCe, List<Link> arrLink, int linkType) {
        if (use.varName.equals(def.varName)) {
            if(def.defTypeInt == LinkConstants.DEF_METHOD && use.useTypeInt == LinkConstants.USE_METHOD_INVOCATION){
                MethodUse useM = (MethodUse)use;

                if(def.myParameters.size() != useM.paramList.size()){
                    return;
                }
            }
            String desc = String.format(ChangeEntityDesc.StageIIILinkType.DEF_USE, use.varName, def.defLoc);
            Link link = new Link(defCE, useCe, linkType, desc, isCrossFile);
            arrLink.add(link);
        }
        if (use.useTypeInt == LinkConstants.USE_METHOD_INVOCATION) {
            MethodUse methodUse = (MethodUse) use;
            if (methodUse.useMethodVarName != null) {
                if (methodUse.useMethodVarName.equals(def.varName)) {
                    String desc = String.format(ChangeEntityDesc.StageIIILinkType.DEF_USE, use.varName, def.defLoc);
                    Link link = new Link(defCE, useCe, linkType, desc, isCrossFile);
                    arrLink.add(link);
                }
            }
        }

    }


}
