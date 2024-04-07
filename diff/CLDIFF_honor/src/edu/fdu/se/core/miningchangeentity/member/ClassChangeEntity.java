package edu.fdu.se.core.miningchangeentity.member;

import edu.fdu.se.core.miningactions.bean.MiningActionData;
import edu.fdu.se.core.miningactions.bean.MyRange;
import edu.fdu.se.core.miningchangeentity.generator.ClusteredActionBean;
import edu.fdu.se.core.miningchangeentity.base.ChangeEntityDesc;
import edu.fdu.se.core.miningchangeentity.base.MemberPlusChangeEntity;
import edu.fdu.se.core.preprocessingfile.data.BodyDeclarationPair;
import edu.fdu.se.global.Global;
import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTCompositeTypeSpecifier;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import java.util.List;

/**
 * Created by huangkaifeng on 2018/1/23.
 *
 */
public class ClassChangeEntity extends MemberPlusChangeEntity {

    private String className;

    private List<String> methodNames;

    private List<String> fieldNames;

    public String getClassName() {
        return this.className;
    }

    public List<String> getMethodNames() {
        return this.methodNames;
    }

    @Override
    public void initDefs(MiningActionData mad) {
        Global.processUtil.initDefs(this,mad);
    }


    public void setClassName(String className) {
        this.className = className;
    }


    /**
     * 预处理 识别的 / C
     */
    public ClassChangeEntity(BodyDeclarationPair bodyDeclarationPair, String changeType, MyRange myRange){
        super(bodyDeclarationPair.getCanonicalName().getPrefixName(),changeType,myRange);
        IASTNode cod = null;
        this.stageIIBean.setCanonicalName(bodyDeclarationPair.getCanonicalName());
        if( bodyDeclarationPair.getBodyDeclaration() instanceof IASTSimpleDeclaration iastSimpleDeclaration) {
            //todo 目前不考虑内部
            if(iastSimpleDeclaration.getDeclSpecifier() instanceof CPPASTCompositeTypeSpecifier) {
                this.stageIIBean.setChangeEntity(ChangeEntityDesc.StageIIENTITY.ENTITY_DECLARATION);
            } else {
                this.stageIIBean.setChangeEntity(ChangeEntityDesc.StageIIENTITY.ENTITY_SIMPLEDECLARATION);
            }

        } else if( bodyDeclarationPair.getBodyDeclaration() instanceof IASTFunctionDefinition) {
            this.stageIIBean.setChangeEntity(ChangeEntityDesc.StageIIENTITY.ENTITY_FUNCTIONDEFINITION);
        }

        this.stageIIBean.setThumbnail(bodyDeclarationPair.getCanonicalName().getSelfName());
        this.bodyDeclarationPair = bodyDeclarationPair;
    }

    /**
     * 预处理 识别的  / Java
     */
    public ClassChangeEntity(BodyDeclarationPair bodyDeclarationPair, String changeType, MyRange myRange, int na) {
        super(bodyDeclarationPair.getCanonicalName().getPrefixName(), changeType, myRange);
        TypeDeclaration cod = (TypeDeclaration) bodyDeclarationPair.getBodyDeclaration();
        this.stageIIBean.setCanonicalName(bodyDeclarationPair.getCanonicalName());
        if (cod.isInterface()) {
            this.stageIIBean.setChangeEntity(ChangeEntityDesc.StageIIENTITY.ENTITY_INTERFACE);
        } else if (na == 100) {
            this.stageIIBean.setChangeEntity(ChangeEntityDesc.StageIIENTITY.ENTITY_CLASS);
        } else {
            this.stageIIBean.setChangeEntity(ChangeEntityDesc.StageIIENTITY.ENTITY_INNER_CLASS);
        }
        this.stageIIBean.setThumbnail(cod.getName().toString());
        this.bodyDeclarationPair = bodyDeclarationPair;
    }


    /**
     * gumtree 识别的 add/remove/modify
     * @param bean
     */
    public ClassChangeEntity(ClusteredActionBean bean){
        super(bean);
    }




}
