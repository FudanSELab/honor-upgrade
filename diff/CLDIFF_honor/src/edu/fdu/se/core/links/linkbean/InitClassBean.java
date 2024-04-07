package edu.fdu.se.core.links.linkbean;

import com.github.gumtreediff.actions.model.Move;
import com.github.gumtreediff.tree.Tree;
import edu.fdu.se.core.miningchangeentity.base.ChangeEntity;
import edu.fdu.se.global.Global;
import edu.fdu.se.core.miningchangeentity.base.ChangeEntityDesc;
import edu.fdu.se.core.miningchangeentity.member.ClassChangeEntity;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import java.util.List;

/**
 * Created by huangkaifeng on 2018/4/7.
 *
 */
public class InitClassBean {


    public static void parse(ChangeEntity en) {
        ClassChangeEntity ce = (ClassChangeEntity) en;
        if (ce.stageIIBean == null || ce.stageIIBean.getEntityCreationStage().equals(ChangeEntityDesc.StageIIGenStage.ENTITY_GENERATION_STAGE_PRE_DIFF)) {
            Object td =  ce.bodyDeclarationPair.getBodyDeclaration();
            List<Object> methods = Global.astNodeUtil.getMethodsFromType(td);
            for (Object n : methods) {
                Global.processUtil.addMethodDef(ce, n);
            }
            List<Object> fieldzz = Global.astNodeUtil.getFieldFromType(td);
            for (Object n : fieldzz) {
                Global.processUtil.addFieldDef(ce, n);
            }
        }else{
            if(ce.clusteredActionBean.curAction instanceof Move){
                Global.fileOutputLog.writeErrFile("[ERR]curAction move");
            }else{
                parseNonMove(ce);
            }
        }
    }


    public static void parseNonMove(ClassChangeEntity ce) {
        Tree tree = (Tree)ce.clusteredActionBean.curAction.getNode();
//        if (Global.astNodeUtil.isTypeDeclaration(tree.getNode())) {
//            Object td =  ce.bodyDeclarationPair.getBodyDeclaration();
//            String className = Global.astNodeUtil.getTypeName(td);
//            List<String> baseInterfaces = Global.astNodeUtil.getInterfaces(td);
//            String base = Global.astNodeUtil.getBaseType(td);
//            int bo = Global.astNodeUtil.isTypeAbstract(td);
//            ce.linkBean.inheritance = new Inheritance(className, bo, base, baseInterfaces);
//        }
    }



}
