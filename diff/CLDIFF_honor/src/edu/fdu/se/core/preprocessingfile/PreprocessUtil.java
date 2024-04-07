package edu.fdu.se.core.preprocessingfile;

import edu.fdu.se.core.miningactions.bean.MyList;
import edu.fdu.se.core.miningchangeentity.base.ChangeEntityDesc;
import edu.fdu.se.core.miningchangeentity.member.EnumChangeEntity;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.EnumConstantDeclaration;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import java.util.List;

/**
 * Created by huangkaifeng on 2018/3/31.
 *
 */
public class PreprocessUtil {

    public static void  enumDeclarationThumnails(EnumDeclaration ed,List<String> varList,List<String> methodCallsList){
        List<BodyDeclaration> bodyDeclarations = ed.bodyDeclarations();
        List<EnumConstantDeclaration> strings = ed.enumConstants();
        for(EnumConstantDeclaration s:strings){
            varList.add(s.getName().toString());
        }
        if(bodyDeclarations == null) return;
        for(BodyDeclaration bd:bodyDeclarations){
            if(bd instanceof MethodDeclaration){
                methodCallsList.add(((MethodDeclaration)bd).getName().toString());
            }
        }
    }

//    public static void  enumDeclarationThumnailsC(IASTNode ed,List<String> varList,List<String> methodCallsList){
//        List<IASTNode> bodyDeclarations = ((IASTEnumerationSpecifier)((IASTSimpleDeclaration)(ed));
//        List<EnumConstantDeclaration> strings = ed.enumConstants();
//        for(EnumConstantDeclaration s:strings){
//            varList.add(s.getName().toString());
//        }
//        if(bodyDeclarations == null) return;
//        for(BodyDeclaration bd:bodyDeclarations){
//            if(bd instanceof MethodDeclaration){
//                methodCallsList.add(((MethodDeclaration)bd).getName().toString());
//            }
//        }
//    }

    public static void generateEnumChangeEntity(EnumChangeEntity changeEntity, EnumDeclaration e1, EnumDeclaration e2){
        MyList<String> varList = new MyList<>();
        MyList<String> methodDecList = new MyList<>();
        enumDeclarationThumnails(e1,varList,methodDecList);
        enumDeclarationThumnails(e2,varList,methodDecList);
        String name  = e1.getName().toString();
        changeEntity.stageIIBean.setEntityCreationStage(ChangeEntityDesc.StageIIGenStage.ENTITY_GENERATION_STAGE_PRE_DIFF);
        changeEntity.stageIIBean.setGranularity(ChangeEntityDesc.StageIIGranularity.GRANULARITY_MEMBER);
        changeEntity.stageIIBean.setOpt(ChangeEntityDesc.StageIIOpt.OPT_CHANGE);
        changeEntity.stageIIBean.setChangeEntity(ChangeEntityDesc.StageIIENTITY.ENTITY_ENUM);
//        changeEntity.stageIIBean.setOpt2(ChangeEntityDesc.StageIIOpt2.OPT2_CHANGE);
        changeEntity.stageIIBean.setSubEntity(ChangeEntityDesc.StageIISub.SUB_DECLARATION);
        changeEntity.stageIIBean.setThumbnail(name);
        changeEntity.variableList = varList;
        changeEntity.methodList = methodDecList;
    }


//    public static void generateEnumChangeEntityC(EnumChangeEntityC changeEntity, IASTNode e1, IASTNode e2){
//        MyList<String> varList = new MyList<>();
//        MyList<String> methodDecList = new MyList<>();
//        enumDeclarationThumnailsC(e1,varList,methodDecList);
//        enumDeclarationThumnailsC(e2,varList,methodDecList);
//        String name  = ((IASTEnumerationSpecifier)((IASTSimpleDeclaration)(e1)).getDeclSpecifier()).getName().toString();
//        changeEntity.stageIIBean.setEntityCreationStage(ChangeEntityDesc.StageIIGenStage.ENTITY_GENERATION_STAGE_PRE_DIFF);
//        changeEntity.stageIIBean.setGranularity(ChangeEntityDesc.StageIIGranularity.GRANULARITY_MEMBER);
//        changeEntity.stageIIBean.setOpt(ChangeEntityDesc.StageIIOpt.OPT_CHANGE);
//        changeEntity.stageIIBean.setChangeEntity(ChangeEntityDesc.StageIIENTITY.ENTITY_ENUM);
////        changeEntity.stageIIBean.setOpt2(ChangeEntityDesc.StageIIOpt2.OPT2_CHANGE);
//        changeEntity.stageIIBean.setSubEntity(ChangeEntityDesc.StageIISub.SUB_DECLARATION);
//        changeEntity.stageIIBean.setThumbnail(name);
//        changeEntity.variableList = varList;
//        changeEntity.methodList = methodDecList;
//    }
}
