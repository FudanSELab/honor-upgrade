import com.github.gumtreediff.tree.ITree;
import com.github.gumtreediff.tree.Tree;
import com.github.gumtreediff.tree.TreeContext;
import edu.fdu.se.ASTParser.JDTParserFactory;
import edu.fdu.se.core.generatingactions.SimpleActionPrinter;
import edu.fdu.se.core.miningchangeentity.base.ChangeEntityDesc;
import edu.fdu.se.global.Constants;
import edu.fdu.se.global.Global;
import edu.fdu.se.lang.java.generatingactions.JavaParserVisitor;
import edu.fdu.se.lang.java.generatingactions.JavaPartialVisitor;
import org.eclipse.jdt.core.dom.*;

import javax.sound.midi.SysexMessage;
import java.util.List;
import java.util.logging.Level;
@SuppressWarnings("Duplicates")
public class TestJDT {

    private static TreeContext generateFromCompilationUnit(CompilationUnit cu, int srcOrDst) {
        JavaParserVisitor visitor = new JavaParserVisitor(srcOrDst);
        visitor.getTreeContext().setCu(cu);
        ASTNode astNode = cu;
        astNode.accept(visitor);
        TreeContext ctx = visitor.getTreeContext();
        ctx.validate();
        return ctx;
    }


    public static void main(String args[]){
        Global.initLang(Constants.RUNNING_LANG.JAVA);
        CompilationUnit cu = JDTParserFactory.getCompilationUnit("/Users/huangkaifeng/iCloud/Workspace/CLDIFF-Extend/TestJava.java");

        TreeContext srcTC = generateFromCompilationUnit(cu, ChangeEntityDesc.TreeType.PREV_TREE_NODE);
        ITree src = srcTC.getRoot();
        String s = SimpleActionPrinter.getPrettyTreeString(src);
        System.out.println(s);
//
//        System.out.println("aa");
//        TypeDeclaration td = (TypeDeclaration) cu.types().get(0);
//        // method var
//        MethodDeclaration md = td.getMethods()[0];
//        List params = md.parameters();
//
//        List stmts = md.getBody().statements();
//        List m = md.parameters();
//        for(Object o:stmts){
//            if (o instanceof VariableDeclarationStatement){
//                VariableDeclarationStatement vds = (VariableDeclarationStatement)o;
//                vds.fragments();
//                List fragments = vds.fragments();
//                for (Object ob : fragments) {
//                    VariableDeclarationFragment vdf = (VariableDeclarationFragment) ob;
//                    Expression e = vdf.getInitializer();
//                    System.out.println("aa");
//                }
//            }
////            if(o instanceof ExpressionStatement){
////                ExpressionStatement es = (ExpressionStatement) o;
////                System.out.println("Stmt:"+es.toString());
////                Expression e = es.getExpression();
////                if(e instanceof MethodInvocation){
////                    MethodInvocation mi = (MethodInvocation)e;
////                    IMethodBinding ib = mi.resolveMethodBinding();
////                    String[] data = getVarName((MethodInvocation)e);
////                    System.out.println("Var Name: " + data[0]);
////                    System.out.println("Method Name: " + data[1]);
////                }
////                if(e instanceof SuperMethodInvocation){
////                    String[] data = getVarName((SuperMethodInvocation) e);
////                    System.err.println("Super Var Name: " + data[0]);
////                    System.err.println("Super Method Name: " + data[1]);
////                }
////            }
//            System.out.println();
//        }
//
////        Object f = td.getTypes()[0].getTypes()[0].getTypes()[0].getFields()[0];
////        Object e1 = Global.astNodeUtil.searchBottomUpFindTypeDeclaration(f,true);
////        Object e2 = Global.astNodeUtil.searchBottomUpFindTypeDeclaration(f,false);
////        System.out.println("aa");
//


    }


    public static String[] getVarName(SuperMethodInvocation smi){
        return null;

    }

    public static String[] getVarName(MethodInvocation mi){
        String[] res =  new String[2];
        String methodName = mi.getName().toString();
        res[1] = methodName;
        if(mi.getExpression() == null){
            res[0] = null;
            return res;
        }
        Expression exp = mi.getExpression();
        if(exp instanceof QualifiedName){
            QualifiedName qn = (QualifiedName) exp;
            while(!qn.getQualifier().isSimpleName()){
                qn = (QualifiedName) qn.getQualifier();
            }
            res[0] = qn.getQualifier().toString();
            return res;
        }
        if(exp instanceof ThisExpression){
            res[0] = "this";
            return res;

        }
        if(exp instanceof FieldAccess){
            FieldAccess fa = (FieldAccess) exp;
            while(fa.getExpression() instanceof FieldAccess){
                fa = (FieldAccess) fa.getExpression();
            }
            res[0] = fa.getName().toString();
            return res;
        }
        if(exp instanceof SuperFieldAccess){
            SuperFieldAccess sfa = (SuperFieldAccess) exp;
            res[0] = sfa.getName().toString();
            return res;
        }
        if(exp instanceof SimpleName){
            SimpleName sn = (SimpleName) exp;
            res[0] = sn.toString();
            return res;
        }

        res[0] = "unknown";
        return res;
    }
}
