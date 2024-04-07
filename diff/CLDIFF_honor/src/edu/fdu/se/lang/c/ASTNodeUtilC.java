package edu.fdu.se.lang.c;

import com.github.gumtreediff.tree.AbstractTree;
import com.github.gumtreediff.tree.ITree;
import com.github.gumtreediff.tree.Tree;
import edu.fdu.se.core.links.linkbean.MyParameters;
import edu.fdu.se.core.miningactions.bean.MiningActionData;
import edu.fdu.se.core.miningactions.bean.MyRange;
import edu.fdu.se.core.miningchangeentity.base.CanonicalName;
import edu.fdu.se.core.miningchangeentity.base.ChangeEntityDesc;
import edu.fdu.se.core.preprocessingfile.data.BodyDeclarationPair;
import edu.fdu.se.global.Global;
import edu.fdu.se.lang.c.generatingactions.CParserVisitor;
import edu.fdu.se.lang.common.IASTNodeUtil;
import org.eclipse.cdt.core.dom.IName;
import org.eclipse.cdt.core.dom.ast.*;
import org.eclipse.cdt.core.dom.ast.cpp.*;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTExpressionStatement;
import org.eclipse.cdt.internal.core.dom.parser.cpp.*;
import org.eclipse.jdt.core.dom.*;

import java.util.*;

public class ASTNodeUtilC implements IASTNodeUtil {

    @Override
    public int isTypeAbstract(Object o) {
        return 0;
    }

    @Override
    public List<String> getInterfaces(Object o) {
        return null;
    }

    @Override
    public IASTTranslationUnit searchBottomUpFindCompilationUnit(Object o) {
        //todo
        return null;
    }


    /**
     *
     * @param o item.getBodyDeclaration()
     */
    @Override
    public MyRange getRange(Object o, int type) {
        if(o instanceof IASTNode) {
            int start = ((IASTNode) o).getFileLocation().getStartingLineNumber();
            int end = ((IASTNode) o).getFileLocation().getEndingLineNumber();
            return new MyRange(start, end,type);
        } else if ( o instanceof Tree) {
            Tree tree = (Tree)o;
            Integer[] range = tree.getRange();
            return new MyRange(range[0],range[1],type);
        }
        return null;
    }

    @Override
    public Object searchBottomUpFindTypeDeclaration(Object node, boolean isDirect) {
        int[] a = {CParserVisitor.TYPE_DECLARATION};
        Object result = this.searchBottomUpFindCorresASTNode(node, a);
        if (isDirect) {
            return result;
        }
        result = isDirectFalseFindNode(result,a);
        return result;
    }

    @Override
    public IASTTranslationUnit getPrevCu(Object data) {
        return (IASTTranslationUnit) data;
    }
    @Override
    public IASTTranslationUnit getCurrCu(Object data) {
        return (IASTTranslationUnit) data;
    }

    @Override
    public Object parseCu(String path){
        Object o = null;
        try {
            o = CDTParserFactory.getTranslationUnit(path);
        }catch (Exception e){

        }
        assert (o!=null);
        return o;

    }

    @Override
    public Object parseCu(byte[] raw) {

        return null;
    }



    @Override
    public Object parseCu(byte[] raw,String s){
        Object object = null;
        try {
            object = CDTParserFactory.getTranslationUnit(raw, Global.repository + s + Global.fileShortName);
        }catch (Exception e){

        }
        assert (object!=null);
        return object;
    }

    @Override
    public int getLineNumber(Object o,int num){
        assert(o instanceof IASTTranslationUnit);
        IASTTranslationUnit cu = (IASTTranslationUnit) o;
        String[] s= cu.getRawSignature().split("\n");
        int[] lineCnt = new int[s.length];
        for(int i = 0;i<s.length;i++){
            lineCnt[i] = s[i].length()+1;
        }
        int cnt = 0;
        for(int i = 0;i<s.length;i++){
            cnt += lineCnt[i];
            if(cnt>num){
                return i+1;
            }
        }
        return -1;
    }

    @Override
    public int getStartPosition(Object o){
        IASTNode node =  (IASTNode)o;
        return node.getFileLocation().getNodeOffset();
    }

    @Override
    public int getNodeLength(Object o){
        IASTNode node =  (IASTNode)o;
        return node.getFileLocation().getNodeLength();
    }

    @Override
    public int getPositionFromLine(Object o,int line){
        IASTTranslationUnit cu = (IASTTranslationUnit)o;
        String[] s= cu.getRawSignature().split("\n");
        int[] lineCnt = new int[s.length];
        for(int i = 0;i<s.length;i++){
            lineCnt[i] = s[i].length()+1;
        }
        if(line>s.length){
            return -1;
        }
        int cnt = 0;
        for(int i = 0;i<line;i++){
            cnt += lineCnt[i];
        }
        return cnt;
    }

    /**
     * 自定义的节点id
     * @param o
     * @return
     */
    @Override
    public int getNodeTypeId(Object o){
        if (o instanceof Tree tree){
            return tree.getType();
        }else if(o instanceof IASTNode iastNode){
            return CParserVisitor.getNodeTypeId(iastNode);
        }
        return CParserVisitor.UNKNOWN;
    }

    @Override
    public Object searchBottomUpFindNoneSimpleNameOrLiteralExpression(Object object) {
        return null;
    }

    @Override
    public CanonicalName getCanonicalNameFromTree(Tree tree) {
        List<String> labels = new ArrayList<>();
        while (!isCompilationUnit(tree.getNode())) {
            IASTNode node = (IASTNode) tree.getNode();
            if (node instanceof CPPASTSimpleDeclaration) {
                IASTDeclSpecifier iastDeclSpecifier = ((IASTSimpleDeclaration) node).getDeclSpecifier();
                if (iastDeclSpecifier instanceof CPPASTCompositeTypeSpecifier) {
                    String uniqueKey = getBodyDeclarationUniqueKey(node);
                    labels.add("." +uniqueKey);
                    tree = (Tree) getParent(tree);
                    continue;
                }
                IASTDeclSpecifier iastDeclSpecifier1 = ((IASTSimpleDeclaration) node).getDeclSpecifier();
                if (iastDeclSpecifier1 instanceof CPPASTEnumerationSpecifier) {
                    String uniqueKey = getBodyDeclarationUniqueKey(tree.getNode());
                    labels.add("." + uniqueKey);
                    tree = (Tree) getParent(tree);
                    continue;
                }
            }
            if (node instanceof CPPASTFunctionDefinition functionDefinition) {
                String uniqueKey = getBodyDeclarationUniqueKey(tree.getNode());
                labels.add("." + uniqueKey);
                tree = (Tree) getParent(tree);
                continue;
            }
            if (node instanceof CPPASTFieldDeclarator) {
                String uniqueKey = getBodyDeclarationUniqueKey(tree.getNode());
                labels.add("." + uniqueKey);
                tree = (Tree) getParent(tree);
                continue;
            }
            if(node instanceof CPPASTSimpleDeclaration){
                String uniqueKey = getBodyDeclarationUniqueKey(tree.getNode());
                labels.add("." + uniqueKey);
                tree = (Tree) getParent(tree);
                continue;
            }
            tree = (Tree) getParent(tree);
        }
        if(labels.isEmpty()){
            labels.add("." + "unknown");
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
        if(canonicalName.getSelfName().startsWith(".")){
            canonicalName.setSelfName(canonicalName.getSelfName().substring(1));
        }
        if(canonicalName.getLongName().startsWith("^") && canonicalName.getLongName().length() > 1 && canonicalName.getLongName().charAt(1) == '.'){
            canonicalName.setLongName(canonicalName.getLongName().substring(0, 1) + canonicalName.getLongName().substring(2));
        }
        return canonicalName;
    }


    @Override
    public boolean isASTNodeSameAsClass(Object o, Class c) {
        if (o.getClass().equals(c)) {
            return true;
        }
        return false;
    }

    @Override
    public boolean isIf(Object o){
        if(getNodeTypeId(o)==CParserVisitor.AST_IF_STATEMENT){
            return true;
        }
        return false;
    }

    @Override
    public boolean isTypeDeclaration(Object o){
        if(o instanceof IASTTranslationUnit||(o instanceof IASTSimpleDeclaration && ((IASTSimpleDeclaration) o).getDeclSpecifier() instanceof IASTCompositeTypeSpecifier)){
            return true;
        }
        return false;
    }

    @Override
    public boolean isMethodDeclaration(Object o){
        if(o instanceof IASTFunctionDefinition){
            return true;
        }
        return false;
    }

    @Override
    public boolean isClassInstanceCreation(Object o){
        return o instanceof CPPASTNewExpression;
    }

    @Override
    public boolean isFieldDeclaration(Object o){
        IASTNode n = (IASTNode)o;
        return n instanceof IASTSimpleDeclaration && (((IASTSimpleDeclaration) n).getDeclSpecifier() instanceof IASTSimpleDeclSpecifier || ((IASTSimpleDeclaration) n).getDeclSpecifier() instanceof IASTNamedTypeSpecifier);
    }

    @Override
    public boolean isEnumDeclaration(Object o){
        IASTNode n = (IASTNode)o;
        return n instanceof IASTSimpleDeclaration && ((IASTSimpleDeclaration) n).getDeclSpecifier() instanceof IASTEnumerationSpecifier;
    }

    @Override
    public boolean isMethodInvocation(Object o){
        IASTNode n = (IASTNode)o;
        return n instanceof CPPASTFunctionCallExpression;
    }

    @Override
    public boolean isSingleVariableDeclaration(Object o){
        IASTNode n = (IASTNode)o;
        return n instanceof IASTParameterDeclaration;
    }

    @Override
    public boolean isLiteralOrName(Tree tree) {
        return getNodeTypeId(tree.getNode()) == CParserVisitor.NAME
                || tree.getNode().getClass().getSimpleName().endsWith("Literal");
    }

    @Override
    public boolean isLiteralOrName(int type) {
        return type == CParserVisitor.NAME || type == CParserVisitor.AST_LITERAL_EXPRESSION;
    }

    @Override
    public String literalOrSimpleNameAsString(Object object){
        return null;
    }

    @Override
    public boolean isCompilationUnit(Object o){
        IASTNode n = (IASTNode)o;
        return n instanceof CPPASTTranslationUnit;
    }

    @Override
    public boolean isBlock(Object o){
        IASTNode n = (IASTNode)o;
        return n instanceof IASTCompoundStatement;
    }

    @Override
    public boolean isSwitchCase(Object o){
        IASTNode n = (IASTNode)o;
        return n instanceof IASTCaseStatement;
    }



    @Override
    public String getMethodName(Object o){
        IASTFunctionDefinition md = (IASTFunctionDefinition) o;
        return md.getDeclarator().getName().toString();
    }

    @Override
    public String getEnumName(Object o){
        IASTSimpleDeclaration sd = (IASTSimpleDeclaration) o;
        return ((IASTEnumerationSpecifier)sd.getDeclSpecifier()).getName().toString();
    }

    @Override
    public String getMethodInvocationName(Object o){
        CPPASTFunctionCallExpression mi = (CPPASTFunctionCallExpression)o;
        return mi.getFunctionNameExpression().toString();
    }

//    @Override
//    public String getMethodInvocationVar(Object o){
//        CPPASTFunctionCallExpression mi = (CPPASTFunctionCallExpression)o;
//        return mi.getFunctionNameExpression().toString();
//    }
    @Override
    public String getTypeNameOfChildrenASTNode(Object o){
        return null;
    }

    @Override
    public List<String> getMethodInvocationVarAndName(Object o){
        return null;
    }

    @Override
    public String[] resolveTypeOfVariable(String variableName, Object stmtNode, MiningActionData mad){
        return null;
    }

    @Override
    public String getClassCreationName(Object o){
        CPPASTNewExpression n = (CPPASTNewExpression)o;
        return n.getImplicitNames()[0].toString();
    }

    @Override
    public String getFieldType(Object o){
        IASTSimpleDeclaration fd = (IASTSimpleDeclaration) o;
        return fd.getDeclSpecifier().toString();
    }

    @Override
    public List<Object> getSingleVariableDeclarations(Object o){
        IASTFunctionDefinition md = (IASTFunctionDefinition)o;
        return Arrays.asList(md.getDeclarator().getChildren());
    }

    @Override
    public String getSingleVariableDeclarationName(Object o){
        IASTParameterDeclaration n = (IASTParameterDeclaration)o;
        return n.getDeclarator().getName().toString();
    }

    @Override
    public String getSingleVariableDeclarationTypeName(Object o){
        IASTParameterDeclaration n = (IASTParameterDeclaration)o;
        return n.getDeclSpecifier().toString();
    }

    @Override
    public Object getMethodType(Object o){
        IASTFunctionDefinition md = (IASTFunctionDefinition)o;
        return md.getDeclSpecifier();
    }

    @Override
    public String getTypeName(Object o){
        IASTSimpleDeclaration sd = (IASTSimpleDeclaration) o;
        return ((IASTCompositeTypeSpecifier)sd.getDeclSpecifier()).getName().toString();
    }

    @Override
    public String getBaseType(Object o) {
        IASTSimpleDeclaration sd = (IASTSimpleDeclaration) o;
        List<String> s = new ArrayList<String>();
        List<IASTNode> nodes= Arrays.asList(((ICPPASTCompositeTypeSpecifier)sd.getDeclSpecifier()).getBaseSpecifiers());
        for(IASTNode node:nodes){
            s.add(nodes.toString());
        }
        //todo
        return null;
    }

    //    @Override
    public List<Object> getChildren(Object o){
        IASTNode n = (IASTNode) o;
        return Arrays.asList(n.getChildren());
    }

    @Override
    public List<Object> getMethodsFromType(Object o) {
        IASTNode n = (IASTNode) o;
        IASTNode[] nodes = n.getChildren();
        List<Object> rst = new ArrayList<Object>();
        for(IASTNode node :nodes){
            if(node instanceof IASTFunctionDefinition) {
                rst.add(node);
            }
        }
        return rst;
    }

    @Override
    public List<Object> getFieldFromType(Object o){
        IASTNode n = (IASTNode) o;
        IASTNode[] nodes = n.getChildren();
        List<Object> rst = new ArrayList<Object>();
        for(IASTNode node :nodes){
            if(isFieldDeclaration(node)) {
                rst.add(node);
            }
        }
        return rst;
    }




    @Override
    public List<String> getFieldDeclarationNames(Object o){
        IASTSimpleDeclaration fd = (IASTSimpleDeclaration)o;
        List<IASTDeclarator> list = Arrays.asList(fd.getDeclarators());
        List<String> s = new ArrayList<String>();
        for(IASTDeclarator vd:list){
            s.add(vd.getName().toString());
        }
        return s;
    }

    @Override
    public Object searchBottomUpFindExpression(Object object) {
        int flag = 0;
        //todo
        Tree tree = (Tree)object;
        while (!tree.getNode().getClass().getSimpleName().endsWith("Statement")) {
            tree = (Tree) tree.getParent();
            switch (Global.astNodeUtil.getNodeTypeId(tree.getNode())) {
                // TO ADD
                case CParserVisitor.CPPAST_EQUALS_INITIALIER:
                case CParserVisitor.AST_FUNCTION_CALL_EXPRESSION:
                case CParserVisitor.CPPAST_NEW_EXPRESSION:
                    flag = 1;
                    break;
            }
            if (flag == 1) {
                return tree.getNode();
            }
        }
        return null;
    }

    @Override
    public Object searchBottomUpFindCorresASTNode(Object node, int[] types) {
        Object lastNode = null;
        while (true) {
            int nodeId = this.getNodeTypeId(node);
            if(nodeId == -1) return lastNode;
            boolean found = false;
            for (int a : types) {
                if (a == nodeId ) {
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
            lastNode = node;
            node = parent;
        }
        return null;
    }


    /**
     *
     * @param o
     * @return
     */
    @Override
    public List<MyParameters> getMethodDeclarationParameters(Object o){
        return null;
    }

    //copy的java代码
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
     *
     * @param o
     * @return
     */
    @Override
    public Object getParent(Object o){
        if (o instanceof Tree){
            Tree t = (Tree)o;
            return t.getParent();
        }
        if (o instanceof IASTNode){
            IASTNode astNode = (IASTNode)o;
            IASTNode parent = astNode.getParent();
            if(parent == null){
                //myflag
                if(Global.mad == null){
                    return null;
                }
                if(Global.mad.preCacheData.getDeletionKVMap() == null){
                    return null;
                }
                if(Global.mad.preCacheData.getDeletionKVMap().containsKey(astNode)){
                    return Global.mad.preCacheData.getDeletionKVMap().get(astNode);
                }
                return null;
            }
            return parent;
        }
        return null;
    }
    /**
     *
     * @param node
     * @return
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

    @Override
    public String getBodyDeclarationUniqueKey(Object object){
        String result = "";
        if(object instanceof CPPASTSimpleDeclaration simpleDeclaration ) {
            if(simpleDeclaration.getDeclSpecifier() instanceof CPPASTCompositeTypeSpecifier) {
                return ((CPPASTCompositeTypeSpecifier) (simpleDeclaration.getDeclSpecifier())).getName().toString();
            }
            if(simpleDeclaration.getDeclSpecifier() instanceof IASTEnumerationSpecifier) {
                return (simpleDeclaration.getDeclSpecifier().getChildren())[0].toString();
            }
            if(simpleDeclaration.getDeclSpecifier() instanceof CPPASTElaboratedTypeSpecifier){
                return ((CPPASTElaboratedTypeSpecifier) simpleDeclaration.getDeclSpecifier()).getName().toString();
            }
            //TODO 预防一些莫名出现的空声明节点，之后看看有没有别的方法
            if(simpleDeclaration.getDeclarators().length == 0){
                return "";
            }
            return simpleDeclaration.getDeclarators()[0].getName().toString();
        } else if(object instanceof CPPASTFunctionDefinition functionDefinition) {
            return functionDefinition.getDeclarator().getName().toString();
        } else if(object instanceof CPPASTNamespaceDefinition namespaceDefinition){
            String name = namespaceDefinition.getName().toString().length() == 0 ? " " : namespaceDefinition.getName().toString();
            return name;
        }


        return "fffff";
    }

    @Override
    public void getAllChildren(ITree tree,List<ITree> result){
    }
}
