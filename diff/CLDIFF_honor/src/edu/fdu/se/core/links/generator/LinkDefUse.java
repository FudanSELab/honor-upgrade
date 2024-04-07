package edu.fdu.se.core.links.generator;

import edu.fdu.se.core.links.linkTo.CheckDefUse;
import edu.fdu.se.core.links.linkTo.CheckInheritance;
import edu.fdu.se.core.links.linkbean.Link;
import edu.fdu.se.core.links.linkbean.MyParameters;
import edu.fdu.se.core.miningchangeentity.base.ChangeEntity;
import edu.fdu.se.global.Global;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import java.util.ArrayList;
import java.util.List;

public class LinkDefUse {

    public static void isDefUseMain(BodyDeclaration bodyDeclarationA, List<ChangeEntity> listA, BodyDeclaration bodyDeclarationB, List<ChangeEntity> listB, int isCrossFile, List<Link> arrLink) {
        if(bodyDeclarationA.equals(bodyDeclarationB)){
            // method 内部
            if(Global.astNodeUtil.isMethodDeclaration(bodyDeclarationA)){
                List<ChangeEntity> defList = new ArrayList<>();
                List<ChangeEntity> useList = new ArrayList<>();
                for (ChangeEntity ce : listA) {
                    if (ce.linkBean.useList != null) {
                        useList.add(ce);
                    }
                    if (ce.linkBean.defList != null) {
                        defList.add(ce);
                    }
                }
                CheckDefUse.checkStmtLocalVarDefUse(defList, useList, arrLink);

            }else if(Global.astNodeUtil.isTypeDeclaration(bodyDeclarationA)){
                // taicu
                isDefUse(bodyDeclarationA, bodyDeclarationB, listA, listB, isCrossFile, arrLink);
            }
        }else {
            isDefUse(bodyDeclarationA, bodyDeclarationB, listA, listB, isCrossFile, arrLink);
            isDefUse(bodyDeclarationB, bodyDeclarationA, listB, listA, isCrossFile, arrLink);
        }
    }

    private static void isDefUse(BodyDeclaration defBody, BodyDeclaration useBody, List<ChangeEntity> defList, List<ChangeEntity> useList, int isCrossFile, List<Link> arrLink) {
        if (Global.astNodeUtil.isTypeDeclaration(defBody) &&
                Global.astNodeUtil.isMethodDeclaration(useBody)) {
            // field,method,class
            // 跨文件的def use + 文件内部def use
            CheckDefUse.checkStmtExpFieldDefUse(defList, useList, isCrossFile, arrLink);
            CheckDefUse.checkStmtExpMethodDefUse(defList, useList, isCrossFile, arrLink);
            CheckDefUse.checkStmtExpClassDefUse(defList, useList, isCrossFile, arrLink);
        }
        if (Global.astNodeUtil.isMethodDeclaration(defBody)) {
            String methodName = Global.astNodeUtil.getMethodName(defBody);
            String typeName = Global.astNodeUtil.getTypeNameOfChildrenASTNode(defBody);
            List<MyParameters>  params = Global.astNodeUtil.getMethodDeclarationParameters(defBody);

            if (Global.astNodeUtil.isMethodDeclaration(useBody)) {
                //太细
                CheckDefUse.checkStmtToStmtResidingMethod(typeName,methodName,params, defList, useList, isCrossFile, arrLink);
            } else if (Global.astNodeUtil.isTypeDeclaration(useBody)) {
                //太细
                CheckDefUse.checkStmtToStmtResidingMethod(typeName,methodName,params, defList, useList, isCrossFile, arrLink);
            }
        }
        if (Global.astNodeUtil.isTypeDeclaration(defBody) && Global.astNodeUtil.isTypeDeclaration(useBody)) {
            //太粗
            CheckDefUse.checkMethodToMethodFineGrainedDefUse(defList, useList, isCrossFile, arrLink);
        }
        if (LinkConstants.AMONG_USE == isCrossFile) {
            if (Global.astNodeUtil.isTypeDeclaration(defBody) &&
                    Global.astNodeUtil.isTypeDeclaration(useBody)) {
                // field,method,class
                CheckInheritance.checkMethodInheritance(defList, useList, arrLink);
            }
        }
    }
}
