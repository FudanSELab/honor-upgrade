package edu.fdu.se.lang.java.generatingactions;

import edu.fdu.se.core.links.generator.LinkConstants;
import edu.fdu.se.core.links.linkbean.Def;
import edu.fdu.se.core.links.linkbean.MethodUse;
import edu.fdu.se.core.miningchangeentity.base.ChangeEntity;
import edu.fdu.se.core.preprocessingfile.data.PreCacheData;
import edu.fdu.se.global.Global;
import org.eclipse.jdt.core.dom.*;

import java.util.*;

/**
 * Created by huangkaifeng on 2018/1/23.
 */
public class JavaPartialVisitor extends ASTVisitor {

    public StringBuilder sb;
    private int depth;

    /**
     * vars, either could be local vars or field or parameter
     */
    public List<String> useLocalVarList;
    public List<String> useFieldList;

    private final int FIELD = 0;
    private final int POTENTIAL_FIELD = 1;

    public List<String> useClassList;
    /**
     * use method
     */
    public List<String> useMethodList;
    public List<ASTNode> methodInvocationExps;

    public List<String> paramNameList;

    private Set<String> fieldNameList;


    public void initFields(PreCacheData preCacheData) {
        if (fieldNameList == null) {
            fieldNameList = new HashSet<>();
        }
        fieldNameList.addAll(preCacheData.getPrevCurrFieldNames());

        fieldNameList.addAll(preCacheData.getPrevFieldNames());

        fieldNameList.addAll(preCacheData.getCurrFieldNames());
    }


    /**
     * visit TypeDeclration MethodDeclaration FieldDeclaration VariableDeclaration
     *
     * @param visitNode
     */
    public JavaPartialVisitor(ASTNode visitNode, PreCacheData preCacheData) {
        super();
        useClassList = new ArrayList<>();
        useFieldList = new ArrayList<>();
        useLocalVarList = new ArrayList<>();
        useMethodList = new ArrayList<>();
        paramNameList = new ArrayList<>();
        depth = 0;
        sb = new StringBuilder();
        if (preCacheData != null) {
            this.initFields(preCacheData);
        }
        methodInvocationExps = new ArrayList<>();
        if (visitNode instanceof TypeDeclaration) {
            acceptTypeDeclaration(visitNode);
        } else if (visitNode instanceof MethodDeclaration) {
            acceptMethodDeclaration(visitNode);
        } else {
            acceptStmtOrExp(visitNode);
        }
    }

    /**
     * list of stmts
     *
     * @param stmts
     */
    public JavaPartialVisitor(List<Statement> stmts, PreCacheData preCacheData) {
        super();
        useClassList = new ArrayList<>();
        useFieldList = new ArrayList<>();
        useLocalVarList = new ArrayList<>();
        useMethodList = new ArrayList<>();
        paramNameList = new ArrayList<>();
        depth = 0;
        if (preCacheData != null) {
            this.initFields(preCacheData);
        }
        sb = new StringBuilder();
        methodInvocationExps = new ArrayList<>();
        for (ASTNode visitNode : stmts) {
            acceptStmtOrExp(visitNode);
        }
    }

    /**
     * accept Statement Or Expression
     */
    private void acceptStmtOrExp(ASTNode astNode) {
        astNode.accept(this);
    }

    private void acceptMethodDeclaration(ASTNode astNode) {
        astNode.accept(this);
    }

    private void acceptTypeDeclaration(ASTNode astNode) {
        astNode.accept(this);
    }


    @Override
    public void preVisit(ASTNode n) {

        for (int i = 0; i < depth; i++) {
            sb.append("    ");
        }
        if (n instanceof SimpleName) {
            SimpleName ss = (SimpleName) n;
            sb.append(n.getClass().getSimpleName() + ": " + ss.getIdentifier() + "\n");
        } else if (n instanceof QualifiedName) {
            QualifiedName ss = (QualifiedName) n;
            sb.append(n.getClass().getSimpleName() + ": " + ss.getFullyQualifiedName() + "\n");
        } else {
            sb.append(n.getClass().getSimpleName() + "\n");

        }
        depth += 1;
    }

    @Override
    public boolean visit(QualifiedName name) {
        if (!this.useFieldList.contains(name.getName().getIdentifier() + FIELD)) {
            this.useFieldList.add(name.getName().getIdentifier() + FIELD);
        }
        return false;
    }

    @Override
    public boolean visit(MethodDeclaration md) {
        List params = md.parameters();
        for (Object obj : params) {
            SingleVariableDeclaration svd = (SingleVariableDeclaration) obj;
            String name = svd.getName().toString();
            String typeName = svd.getType().toString();
            paramNameList.add(name);
        }
        return true;
    }

    @Override
    public boolean visit(VariableDeclarationFragment node) {
        return true;
    }


    @Override
    public boolean visit(MethodInvocation methodInvocation) {
        // Def VariableDeclaration
        // Use variable field method
        this.methodInvocationExps.add(methodInvocation);
        this.useMethodList.add(methodInvocation.getName().toString());
        return true;
    }

    @Override
    public boolean visit(SuperMethodInvocation superMethodInvocation) {
        this.methodInvocationExps.add(superMethodInvocation);
        this.useMethodList.add(superMethodInvocation.getName().toString());
        return true;
    }

    @Override
    public boolean visit(SimpleName name) {
        if (this.useMethodList.contains(name.getIdentifier())) {
            return true;
        }
        if (name.getParent() instanceof SimpleType) {
            if (name.getParent().getParent() instanceof ClassInstanceCreation) {
                this.useClassList.add(name.getIdentifier());
            }
        } else if (name.getParent() instanceof FieldAccess) {
            if (!useFieldList.contains(name.getIdentifier() + FIELD)) {
                this.useFieldList.add(name.getIdentifier() + FIELD);
            }
        } else if (this.fieldNameList != null && this.fieldNameList.contains(name.getIdentifier())) {
            // 和FieldName 重叠不代表的确用了这个field name
            if (!useFieldList.contains(name.getIdentifier() + POTENTIAL_FIELD)) {
                this.useFieldList.add(name.getIdentifier() + POTENTIAL_FIELD);
            }
        } else {
            this.useLocalVarList.add(name.getIdentifier());
        }
        return true;

    }

    @Override
    public void postVisit(ASTNode n) {
        depth -= 1;
    }

    /**
     * 过滤 defList 中的 DEF_METHOD
     * 将过滤后的 filter 加入到 uses
     * @param ce
     * @param treeType
     */
    public void addUses(ChangeEntity ce, int treeType) {
        List<String> filter = new ArrayList<>();
        if (ce.linkBean.defList.getDefs() != null) {
            for (Def def : ce.linkBean.defList.getDefs()) {
                if (def.defTypeInt == LinkConstants.DEF_METHOD) {
                    continue;
                }
                filter.add(def.varName);
            }
        }
        addUses(ce, treeType, filter);
    }

    /**
     * 根据key值把参数放入到ce.linkBean.useList
     */
    public void addUses(ChangeEntity ce, int treeType, List<String> filter) {
        createUse(ce, treeType, this.useClassList, filter, LinkConstants.USE_CLASS_CREATION);
        createMethodUse(ce, treeType, this.useMethodList, filter, LinkConstants.USE_METHOD_INVOCATION);
        createUse(ce, treeType, this.useFieldList, filter, LinkConstants.USE_FIELD);
        createUse(ce, treeType, this.useLocalVarList, filter, LinkConstants.USE_LOCAL_VAR);
    }

    //todo 注释补充
    /**
     *
     * @param varList    use列表
     * @param filterList 过滤后的列表
     * @param type       LinkConstants USE key
     */
    private void createUse(ChangeEntity ce, int treeType, List<String> varList, List<String> filterList, int type) {
        if (varList.size() != 0) {
            for (String var : varList) {
                int pot = 0;
                if (LinkConstants.USE_FIELD == type) {
                    //获取最后一个字符（数字部分）并转为整数类型，例如 out0
                    pot = Integer.parseInt(var.substring(var.length() - 1));
                    //获取删掉最后一个数字
                    var = var.substring(0, var.length() - 1);
                }
                if (LinkConstants.USE_FIELD == type && this.paramNameList.contains(var)) {
                    if (pot == POTENTIAL_FIELD) {
                        continue;
                    }
                }
                if (this.paramNameList.contains(var)) {
                    continue;
                }
                if (filterList != null && filterList.contains(var)) {
                    continue;
                }
                if (LinkConstants.USE_FIELD == type && pot == POTENTIAL_FIELD) {
                    type = LinkConstants.USE_FIELD_LOCAL;
                }
                ce.linkBean.useList.addUse(ce.stageIIBean.getCanonicalName().getLongName(), ce.getLineRange(), var, type, treeType, null, null);
            }
        }
    }

    private void createMethodUse(ChangeEntity ce, int treeType, List<String> varList, List<String> filterList, int type) {
        if (varList.size() != 0) {
            for (int i = 0; i < this.useMethodList.size(); i++) {
                String var = this.useMethodList.get(i);
                if (filterList != null && filterList.contains(var)) {
                    continue;
                }
                List<String> data = Global.astNodeUtil.getMethodInvocationVarAndName(methodInvocationExps.get(i));
                MethodUse use = ce.linkBean.useList.addMethodInvocationUse(ce.stageIIBean.getCanonicalName().getLongName(), ce.getLineRange(), var, treeType, data.get(0));
                if (use == null) {
                    continue;
                }
                use.addToParamList(data);
                if (!"unknown".equals(data.get(0)) && data.get(0) != null) {
                    filterList.add(data.get(0));
                    String[] data2 = Global.astNodeUtil.resolveTypeOfVariable(data.get(0), methodInvocationExps.get(i), Global.mad);
                    use.setTypeNameAndUseType(data2);
                }
            }
        }
    }


    public void addMethodParams(MethodDeclaration md) {
        List params = md.parameters();
        for (Object obj : params) {
            SingleVariableDeclaration svd = (SingleVariableDeclaration) obj;
            String name = svd.getName().toString();
            String typeName = svd.getType().toString();
            paramNameList.add(name);
        }
        return;
    }


}
