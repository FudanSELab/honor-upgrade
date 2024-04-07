package edu.fdu.se.lang.java;

import com.github.gumtreediff.tree.ITree;
import com.github.gumtreediff.tree.Tree;
import edu.fdu.se.core.links.generator.LinkConstants;
import edu.fdu.se.core.links.linkbean.MyParameters;
import edu.fdu.se.core.miningactions.bean.MiningActionData;
import edu.fdu.se.core.miningactions.bean.MyRange;
import edu.fdu.se.core.miningchangeentity.base.CanonicalName;
import edu.fdu.se.global.Constants;
import edu.fdu.se.global.Global;
import org.eclipse.jdt.core.dom.*;

import java.util.ArrayList;
import java.util.List;

public class ASTNodeUtilJava extends AbstractASTNodeUtilJava {


    @Override
    public int getPositionFromLine(Object o, int line) {
        CompilationUnit cu = (CompilationUnit) o;
        String[] s = cu.toString().split("\n");
        int[] lineCnt = new int[s.length];
        for (int i = 0; i < s.length; i++) {
            lineCnt[i] = s[i].length() + 1;
        }
        if (line > s.length) {
            return -1;
        }
        int cnt = 0;
        for (int i = 0; i < line; i++) {
            cnt += lineCnt[i];
        }
        return cnt;
    }

    @Override
    public String getTypeNameOfChildrenASTNode(Object o) {
        Object object = this.searchBottomUpFindTypeDeclaration(o,true);
        if (object == null) {
            return null;
        }
        TypeDeclaration td = (TypeDeclaration)object;

        return td.getName().toString();
    }

    private void addArguments(List<String> result, List arguments, List typeArguments) {
        for (Object o : arguments) {
            result.add(this.literalOrSimpleNameAsString(o));
            result.add("null");
        }
    }

    /**
     * @param o MethodInvocation or SuperMethodInvocation
     * @return 1. var name 2. method name
     */
    @Override
    public List<String> getMethodInvocationVarAndName(Object o) {
        List<String> result = new ArrayList<>();
        if (o instanceof MethodInvocation) {
            MethodInvocation mi = (MethodInvocation) o;
            List arguments = mi.arguments();
            List typeArguments = mi.typeArguments();
            String methodName = mi.getName().toString();
            if (mi.getExpression() == null) {
                result.add("null");
                result.add(methodName);
                addArguments(result, arguments, typeArguments);
                return result;
            }
            Expression exp = mi.getExpression();
            if (exp instanceof QualifiedName) {
                QualifiedName qn = (QualifiedName) exp;
                while (!qn.getQualifier().isSimpleName()) {
                    qn = (QualifiedName) qn.getQualifier();
                }
                result.add(qn.getQualifier().toString());
                result.add(methodName);
                addArguments(result, arguments, typeArguments);
                return result;
            }
            if (exp instanceof ThisExpression) {
                result.add("this");
                result.add(methodName);
                addArguments(result, arguments, typeArguments);
                return result;

            }
            if (exp instanceof FieldAccess) {
                FieldAccess fa = (FieldAccess) exp;
                while (fa.getExpression() instanceof FieldAccess) {
                    fa = (FieldAccess) fa.getExpression();
                }
                result.add(fa.getName().toString());
                result.add(methodName);
                addArguments(result, arguments, typeArguments);
                return result;
            }
            if (exp instanceof SuperFieldAccess) {
                SuperFieldAccess sfa = (SuperFieldAccess) exp;
                result.add(sfa.getName().toString());
                result.add(methodName);
                addArguments(result, arguments, typeArguments);
                return result;
            }
            if (exp instanceof SimpleName) {
                SimpleName sn = (SimpleName) exp;
                result.add(sn.toString());
                result.add(methodName);
                addArguments(result, arguments, typeArguments);
                return result;
            }

            result.add("unknown");
            result.add(methodName);
            addArguments(result, arguments, typeArguments);
            return result;
        } else if (o instanceof SuperMethodInvocation) {
            SuperMethodInvocation superMethodInvocation = (SuperMethodInvocation) o;
            List arguments = superMethodInvocation.arguments();
            List typeArguments = superMethodInvocation.typeArguments();
            String methodName = superMethodInvocation.getName().toString();
            result.add("super");
            result.add(methodName);
            addArguments(result, arguments, typeArguments);
            return result;

        }
        return null;
    }

    /**
     * @param variableName
     * @param node ASTNode
     * @return
     */
    @Override
    public String[] resolveTypeOfVariable(String variableName, Object node, MiningActionData mad) {
        int[] a = {ASTNode.METHOD_DECLARATION};
        MethodDeclaration md = (MethodDeclaration) this.searchBottomUpFindCorresASTNode(node, a);
        TypeDeclaration td = (TypeDeclaration) this.searchBottomUpFindTypeDeclaration(md, true);
        if ("super".equals(variableName)) {
            Type t = td.getSuperclassType();
            String superType = null;
            if(t== null) {
                List li = td.superInterfaceTypes();
                for(Object o:li){
                    if(o instanceof ParameterizedType){
                        ParameterizedType pt = (ParameterizedType)o;
                        superType =  pt.getType().toString();
                    }else{
                        superType = o.toString();
                    }
                }
            }else {
                superType = td.getSuperclassType().toString();
            }
            String[] res = {superType, String.valueOf(LinkConstants.USE_SUPER_TYPE)};
            return res;
        }
        if (md != null) {
            if(md.getBody() == null){
                return null;
            }
            List stmts = md.getBody().statements();
            for (Object obj : stmts) {
                if (obj instanceof VariableDeclarationStatement) {
                    List<String> s = new ArrayList<>();
                    VariableDeclarationStatement vds = (VariableDeclarationStatement) obj;
                    List<VariableDeclarationFragment> list = vds.fragments();
                    for (VariableDeclarationFragment vd : list) {
                        s.add(vd.getName().toString());
                    }
                    String typeName = vds.getType().toString();
                    if (s.contains(variableName)) {
                        String[] res = {typeName, String.valueOf(LinkConstants.USE_LOCAL_VAR)};
                        return res;
                    }
                }
            }
            List params = md.parameters();
            for (Object obj : params) {
                SingleVariableDeclaration svd = (SingleVariableDeclaration) obj;
                String name = svd.getName().toString();
                String typeName = svd.getType().toString();
                if (name.equals(variableName)) {
                    String[] res = {typeName, String.valueOf(LinkConstants.USE_PARAMS)};
                    return res;
                }
            }

        }

//        FieldDeclaration[] fds = td.getFields();
//        for(FieldDeclaration fd:fds){
//            List<String> names = this.getFieldDeclarationNames(fd);
//            if(names.contains(variableName)){
//                String typeName = fd.getType().toString();
//                return typeName;
//            }
//        }
//        if(mad.preCacheData.getCurrFieldNames().contains(variableName){

//        }
        List<Object> fds = mad.preCacheData.getmCurrFields();
        if (fds != null) {
            for (Object temp : fds) {
                FieldDeclaration fd = (FieldDeclaration)temp;
                List<String> names = this.getFieldDeclarationNames(fd);
                if (names.contains(variableName)) {
                    String typeName = fd.getType().toString();
                    String[] res = {typeName, String.valueOf(LinkConstants.USE_FIELD)};
                    return res;
                }
            }
        }
        List<Object> fds2 = mad.preCacheData.getmPrevFields();
        if (fds2 != null) {
            for (Object temp : fds2) {
                FieldDeclaration fd = (FieldDeclaration)temp;
                List<String> names = this.getFieldDeclarationNames(fd);
                if (names.contains(variableName)) {
                    String typeName = fd.getType().toString();
                    String[] res = {typeName, String.valueOf(LinkConstants.USE_FIELD)};
                    return res;
                }
            }
        }

        return null;
    }

    /**
     * 指示该对象所代表的类或接口是否为抽象类型
     * @param o
     * @return
     */
    @Override
    public int isTypeAbstract(Object o) {
        TypeDeclaration td = (TypeDeclaration) o;
        if (td.isInterface()) {
            return LinkConstants.IS_INTERFACE;
        }
        List<ASTNode> modifiers = td.modifiers();
        for (ASTNode node : modifiers) {
            if (node instanceof Modifier) {
                Modifier modifier = (Modifier) node;
                if (modifier.toString().equals("abstract")) {
                    return LinkConstants.IS_ABSTRACT_CLASS;
                }
            }
        }
        return LinkConstants.IS_REGULAR_CLASS;
    }


    /**
     * 获取父接口
     * @param o class TypeDeclaration
     * @return
     */
    @Override
    public List<String> getInterfaces(Object o) {
        TypeDeclaration td = (TypeDeclaration) o;
        List<String> s = new ArrayList<>();
        //获取表示声明类或接口的直接父接口的类型的数组
        List<Type> aList = td.superInterfaceTypes();
        if (aList == null || aList.size() == 0) {
            return null;
        }
        for (Type a : aList) {
            s.add(a.toString());
        }

        return s;
    }

    /**
     * @param o        ASTNode or Tree
     * @param treeType TreeType
     * @return Range
     */
    @Override
    public MyRange getRange(Object o, int treeType) {
        if(o instanceof ASTNode) {
            ASTNode astNode = (ASTNode) o;
            CompilationUnit cu = searchBottomUpFindCompilationUnit(o);
            //myflag
            if(cu == null){
                return null;
            }
            int start = cu.getLineNumber(astNode.getStartPosition());
            int end = cu.getLineNumber(astNode.getStartPosition() + astNode.getLength() - 1);
            return new MyRange(start, end, treeType);
        }else if(o instanceof Tree){
            Tree tree = (Tree)o;
            Integer[] range =  tree.getRange();
            int start = range[0];
            int end = range[1];
            MyRange myRange = new MyRange(start, end, treeType);
            return myRange;
        }
        return null;
    }

    /**
     * @param o ASTNode or Tree
     * @return ASTNode
     */
    @Override
    public CompilationUnit searchBottomUpFindCompilationUnit(Object o) {
        int[] a = {ASTNode.COMPILATION_UNIT};
        Object result = this.searchBottomUpFindCorresASTNode(o, a);
        if (result != null){
            if (result instanceof CompilationUnit) {
                return (CompilationUnit) result;
            }else if(result instanceof Tree){
                Tree t = (Tree) result;
                return (CompilationUnit) t.getNode();
            }
        }
        return null;
    }

    /**
     *
     * @param node Tree or ASTNode
     * @return Tree or ASTNode
     */
    @Override
    public Object searchBottomUpFindBodyDeclaration(Object node, boolean isDirect){

        int[] a = Global.iLookupTbl.astNodeMap.get("declarations");
        int[] b = Global.iLookupTbl.astNodeMap.get("types");
        int[] res = new int[a.length + b.length];
        System.arraycopy(a, 0, res, 0, a.length);
        System.arraycopy(b, 0, res, a.length, b.length);
        Object result = node;
        if (isDirect) {
            result = this.searchBottomUpFindCorresASTNode(node, res);
            return result;
        }
        result = isDirectFalseFindNode(result,res);
        return result;

    }

    /**
     * true：寻找距离该节点最近的类型声明
     * false：寻找距离根节点最近的类型声明
     * @param node     Tree or ASTNode
     * @param isDirect TypeDeclaration could be an innerclass  isDirect=true find a nearest TypeDeclaration for node, else find a nearest TypeDeclaration from root
     * @return         Tree or ASTNode
     */
    @Override
    public Object searchBottomUpFindTypeDeclaration(Object node, boolean isDirect) {
        int[] a = {ASTNode.TYPE_DECLARATION};
        Object result = this.searchBottomUpFindCorresASTNode(node, a);
        if (isDirect) {
            return result;
        }
        result = isDirectFalseFindNode(result,a);
        return result;
    }

    private Object isDirectFalseFindNode(Object node, int[] a){
        Object result = node;
        Object parent = getParent(node);
        List<Object> foundNodes = new ArrayList<>();
        while (parent != null) {
            parent = this.searchBottomUpFindCorresASTNode(parent, a);
            if(parent!=null) {
                foundNodes.add(parent);
                parent = getParent(parent);
            }
        }
        if(foundNodes.size()==0){
            return node;
        }
        if(foundNodes.size()==1){
            return foundNodes.get(0);
        }
        if(foundNodes.size()==2){
            return foundNodes.get(0);
        }
        return foundNodes.get(0);
//        return foundNodes.get(foundNodes.size()-2);
    }

    /**
     * 如果属于declarations，return， else return expression
     *
     * @param object Tree or ASTNode
     * @return
     */
    @Override
    public Object searchBottomUpFindExpression(Object object) {
        int[] a = Global.iLookupTbl.astNodeMap.get("declarations");
        int[] b = Global.iLookupTbl.astNodeMap.get("statements");
        int[] c = Global.iLookupTbl.astNodeMap.get("expressions");
        int[] res = new int[a.length + b.length + c.length];
        System.arraycopy(a, 0, res, 0, a.length);
        System.arraycopy(b, 0, res, a.length, b.length);
        System.arraycopy(c, 0, res, a.length + b.length, c.length);
        return searchBottomUpFindCorresASTNode(object, res);
    }

    /**
     * @param obj Tree or ASTNode
     * @return
     */
    @Override
    public  Object searchBottomUpFindNoneSimpleNameOrLiteralExpression(Object obj) {
        int[] a = Global.iLookupTbl.astNodeMap.get("declarations");
        int[] b = Global.iLookupTbl.astNodeMap.get("statements");
        int[] c = Global.iLookupTbl.astNodeMap.get("expressions");
        int[] res = new int[a.length + b.length + c.length + 1];
        System.arraycopy(a, 0, res, 0, a.length);
        System.arraycopy(b, 0, res, a.length, b.length);
        System.arraycopy(c, 0, res, a.length + b.length, c.length);
        for (int i = 0; i < res.length; i++) {
            boolean flag = false;
            switch (res[i]) {
                case ASTNode.SIMPLE_NAME:
                case ASTNode.BOOLEAN_LITERAL:
                case ASTNode.CHARACTER_LITERAL:
                case ASTNode.NUMBER_LITERAL:
                case ASTNode.STRING_LITERAL:
                case ASTNode.TYPE_LITERAL:
                case ASTNode.NULL_LITERAL:
                    flag = true;
                    break;
            }
            if (flag) {
                res[i] = -10000;
            }
        }
        res[res.length - 1] = ASTNode.VARIABLE_DECLARATION_FRAGMENT;
        return searchBottomUpFindCorresASTNode(obj, res);
    }

    /**
     * core of seacrh**
     * 由该节点向上寻找ASTNode.TYPE_DECLARATION的节点
     * @param node Tree or ASTNode
     * @param candidates
     * @return Tree or ASTNode
     */
    @Override
    public Object searchBottomUpFindCorresASTNode(Object node, int[] candidates) {
        while (true) {
            int nodeId = this.getNodeTypeId(node);
            boolean found = false;
            for (int a : candidates) {
                if (a == nodeId) {
                    found = true;
                    break;
                }
            }
            if (found) {
                return node;
            }
            Object parent = getParent(node);
            if ( parent == null) {
                break;
            }
            node = parent;
        }
        return null;
    }

    @Override
    public CanonicalName getCanonicalNameFromTree(Tree tree) {
        List<String> labels = new ArrayList<>();
        while (!isCompilationUnit(tree.getNode())) {
            if (tree.getNode() instanceof TypeDeclaration) {
                String uniqueKey = getBodyDeclarationUniqueKey(tree.getNode());
                labels.add("." +uniqueKey);
            } else if (tree.getNode() instanceof MethodDeclaration) {
                String uniqueKey = getBodyDeclarationUniqueKey(tree.getNode());
                labels.add("." + uniqueKey);
            } else if (tree.getNode() instanceof FieldDeclaration){
                String uniqueKey = getBodyDeclarationUniqueKey(tree.getNode());
                labels.add("." + uniqueKey);
            } else if (tree.getNode() instanceof AnonymousClassDeclaration) {
                AnonymousClassDeclaration td = (AnonymousClassDeclaration) tree.getNode();
                labels.add("#Anonymous");
            } else if (tree.getNode() instanceof Initializer) {
                String uniqueKey = getBodyDeclarationUniqueKey(tree.getNode());
                labels.add("." + uniqueKey);
            } else if (tree.getNode() instanceof VariableDeclarationFragment) {
                VariableDeclarationFragment f = (VariableDeclarationFragment) tree.getNode();
                labels.add("." + f.getName().toString());
            } else if (tree.getNode() instanceof Statement) {
                labels.add("@Statement");
            }
            tree = (Tree) getParent(tree);
        }
        StringBuilder res = new StringBuilder();
        List<String> labels2 = new ArrayList<>();
        boolean stmt = false;
        for (int i = labels.size() - 1; i >= 0; i--) {
            if (stmt && "@Statement".equals(labels.get(i))) {
                continue;
            }
            labels2.add(labels.get(i));
            if ("@Statement".equals(labels.get(i))) {
                stmt = true;
            } else {
                stmt = false;
            }
        }
        for(int i = 0;i<labels2.size()-1;i++){
            res.append(labels2.get(i));
        }
        String prefix = "^";
        if(!res.toString().equals("")){
            prefix += res.toString().substring(1);
        }
        CanonicalName canonicalName = new CanonicalName(prefix,labels2.get(labels2.size()-1));
        return canonicalName;
    }


    @Override
    public Tree findCommonRootNode(ITree node, List<Integer> nodeTypes) {
        int type;
        Tree curNode = (Tree) node;
        while (true) {
            type = Global.astNodeUtil.getNodeTypeId(curNode.getNode());
            if (nodeTypes.contains(type)) {
                break;
            }
            curNode = (Tree) getParent(curNode);
        }
        return curNode;
    }

    /**
     * 获得 object 的声明名字、变量声明列表、方法参数列表、静态方法块等内容
     * 内容取决于 object 是什么
     * @param object
     * @return String
     */
    @Override
    public String getBodyDeclarationUniqueKey(Object object){
        assert object instanceof BodyDeclaration;
        String result = "";
        if (object instanceof TypeDeclaration typeDeclaration){
            //如果是类型声明，则返回声明的名字
            return typeDeclaration.getName().toString();
        } else if (object instanceof FieldDeclaration){
            //如果是字段声明，则获得变量声明的列表
            List<String> fieldName = Global.astNodeUtil.getFieldDeclarationNames(object);
            for (int i =0;i<fieldName.size()-1;i++){
                String a = fieldName.get(i);
                result +=  a + Constants.ATSPLITTER;
            }
            result += fieldName.get(fieldName.size()-1);
            return result;
        } else if (object instanceof MethodDeclaration){
            String methodName = Global.astNodeUtil.getMethodName(object);
            //如果是方法声明，则获得参数列表
            //没有参数就是 名字+（）
            //有参数就是 名字+（参数列表）
            List<MyParameters> params = Global.astNodeUtil.getMethodDeclarationParameters(object);
            if(params.size()==0){
                result = methodName+"()";
            }else {
                StringBuilder paramsStr = new StringBuilder("(");
                for (int i = 0; i< params.size()-1;i++){
                    MyParameters mp = params.get(i);
                    paramsStr.append(mp.type).append(",");
                }
                paramsStr.append(params.get(params.size() - 1).type);
                result += methodName + paramsStr + ")";
            }
            return result;
        } else if (object instanceof Initializer){
            //Initializer用于表示类或接口中的实例初始化块或静态初始化块
            result = "{}";
            return result;
        } else if(object instanceof EnumDeclaration){
            EnumDeclaration ed = (EnumDeclaration)object;
            result = ed.getName().toString();
            return result;
        } else {
            //todo
        }
        return "";
    }


}
