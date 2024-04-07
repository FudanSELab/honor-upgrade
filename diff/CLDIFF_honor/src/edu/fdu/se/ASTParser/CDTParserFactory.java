package edu.fdu.se.ASTParser;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.gnu.cpp.GPPLanguage;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.parser.DefaultLogService;
import org.eclipse.cdt.core.parser.FileContent;
import org.eclipse.cdt.core.parser.IncludeFileContentProvider;
import org.eclipse.cdt.core.parser.ScannerInfo;
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
public class CDTParserFactory {

    public static String getUnit(String s){
        String[] temp = s.split("/");
        String t = temp[temp.length-1];
        return t.substring(0,t.length()-5);
    }

    static String getContentFile(File file) throws IOException {
        StringBuilder content = new StringBuilder();
        String line;

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(file)))) {
            while ((line = br.readLine()) != null)
                content.append(line).append('\n');
        }catch (Exception e){

        }

        return content.toString();
    }

    public static IASTTranslationUnit getTranslationUnit(InputStream is) throws Exception{
        BufferedInputStream bufferedInputStream = new BufferedInputStream(is);
        byte[] input = new byte[bufferedInputStream.available()];
        bufferedInputStream.read(input);
        bufferedInputStream.close();
       return  getTranslationUnit(input);
    }

    public static IASTTranslationUnit getTranslationUnit(String filePath) throws Exception{
        File source = new File(filePath);
        return getTranslationUnit(getContentFile(source).getBytes());
    }

    public static IASTTranslationUnit getTranslationUnit(byte[] fileContent) throws Exception{

        FileContent reader = FileContent.create(
                "",
                new String(fileContent).toCharArray());

        return GPPLanguage.getDefault().getASTTranslationUnit(
                reader,
                new ScannerInfo(),
                IncludeFileContentProvider.getSavedFilesProvider(),
                null,
                ILanguage.OPTION_IS_SOURCE_UNIT,
                new DefaultLogService());
    }



    @Deprecated
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

    @Deprecated
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

    @Deprecated
    public static CompilationUnit getCompilationUnit(String filePath){
        try {
            return getCompilationUnit(new FileInputStream(filePath));
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }



    public static String getLinesOfFile(String filePath,List<String> fileList){
        try {
            FileInputStream fis = new FileInputStream(filePath);
            StringBuilder sb = new StringBuilder();
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);
            String line;
            while((line = br.readLine())!= null){
                fileList.add(line);
                sb.append(line);
                sb.append("\n");
            }
            return sb.toString();
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }


    public static String getLinesOfFile(byte[] fileContent,List<String> fileList){
        try {
            InputStream fis = new ByteArrayInputStream(fileContent);
            StringBuilder sb = new StringBuilder();
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);
            String line;
            while((line = br.readLine())!= null){
                fileList.add(line);
                sb.append(line);
                sb.append("\n");
            }
            return sb.toString();
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public static List<Integer> getLinesList(int line){
        List<Integer> mList = new ArrayList<>();
        for(int i=0;i<line;i++){
            mList.add(i);
        }
        return mList;
    }

}
