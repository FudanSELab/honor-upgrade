package edu.fdu.se.ASTParser;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.*;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
/**
 * Created by huangkaifeng on 2018/3/12.
 *
 */
public class JDTParserFactory {

    public static CompilationUnit getCompilationUnit(InputStream is) throws Exception{

        ASTParser astParser = ASTParser.newParser(AST.JLS8);
        astParser.setKind(ASTParser.K_COMPILATION_UNIT);
        BufferedInputStream bufferedInputStream = new BufferedInputStream(is);
        byte[] input = new byte[bufferedInputStream.available()];
        bufferedInputStream.read(input);
        bufferedInputStream.close();
        Map options = JavaCore.getOptions();
        options.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_8);
        options.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_8);
        options.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_8);
        options.put(JavaCore.COMPILER_DOC_COMMENT_SUPPORT, JavaCore.ENABLED);

//        astParser.setEnvironment(null, null, null, true);
//        astParser.setUnitName("ClusterAction2");//需要与代码文件的名称一致
//        astParser.setResolveBindings(true);
//        astParser.setBindingsRecovery(true);

        astParser.setCompilerOptions(options);
        astParser.setSource(new String(input).toCharArray());
        CompilationUnit result = (CompilationUnit) (astParser.createAST(null));
        return result;
    }

    /**
     * 设置astParser，获取java语法树
     * @param fileContent
     * @return
     * @throws Exception
     */
    public static CompilationUnit getCompilationUnit(byte[] fileContent) throws Exception{
        ASTParser astParser = ASTParser.newParser(AST.JLS8);
        Map options = JavaCore.getOptions();
        options.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_8);
        options.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_8);
        options.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_8);
        options.put(JavaCore.COMPILER_DOC_COMMENT_SUPPORT, JavaCore.ENABLED);
//        JavaCore.setComplianceOptions(JavaCore.VERSION_1_8, options);
        astParser.setCompilerOptions(options);
        astParser.setSource(new String(fileContent).toCharArray());
        CompilationUnit result = (CompilationUnit) (astParser.createAST(null));
        return result;
    }

    public static CompilationUnit getCompilationUnit(String filePath){
        try {
            return getCompilationUnit(new FileInputStream(filePath));
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public static void main(String args[]) {
        try {
            CompilationUnit cu = getCompilationUnit("/Users/huangkaifeng/Documents/IDEAWorkspace/IDEACE/CLDIFF/src/edu/fdu/se/lang/java/ASTNodeUtilJava.java");
            String result = "";
            TypeDeclaration ty = (TypeDeclaration) cu.types().get(0);

            ASTNode node = (ASTNode) ty.getMethods()[0].getBody().statements().get(0);
            while (!(node instanceof CompilationUnit)) {
                if (node instanceof TypeDeclaration) {
                    TypeDeclaration tp = (TypeDeclaration) node;
                    result = tp.getName().toString() + "." + result;
                }
                node = node.getParent();
            }

            String a = cu.getPackage().getName().toString();
            System.out.println("aaa");
        }catch(Exception e){
            e.printStackTrace();
        }
    }

}
