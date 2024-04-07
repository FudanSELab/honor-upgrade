package edu.fdu.se.util;

//import edu.fdu.se.ASTParser.CDTParserFactory;
//import main.Boot;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import edu.fdu.se.fileutil.FileRWUtil;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.formatter.CodeFormatter;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.text.edits.TextEdit;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static edu.fdu.se.ASTParser.JDTParserFactory.getCompilationUnit;


public class MaskAst {

    /** Get all changed files with their entities **/
    public static List<Pair<List<Object>, Pair<String,String>>> getEntityList(String s){

        List<Pair<List<Object>, Pair<String,String>>> ret = new ArrayList<>();
        String pathCurr;
        String pathPrev;
        try {
            Reader reader = new InputStreamReader(new FileInputStream(s), "Utf-8");
            int ch ;
            StringBuffer sb = new StringBuffer();
            while ((ch = reader.read()) != -1) {
                sb.append((char) ch);
            }
            String jsonStr = sb.toString();
            JSONArray jsonArray = JSON.parseArray(jsonStr);
            JSONObject obj = (JSONObject) jsonArray.get(0);
            JSONArray arr = (JSONArray)obj.get("MODIFIED_FILES");
            for(Object object : arr){
                List<String> l = new ArrayList<>();
                JSONObject obj1 = (JSONObject) object;
                pathCurr = obj1.getString("CURR_PATH");
                pathPrev = obj1.getString("PREV_PATH");
                JSONArray arr1 = (JSONArray)obj1.get("MODIFIED_FIELD");
                JSONArray arr2 = (JSONArray)obj1.get("MODIFIED_METHOD");
                JSONArray arr3 = (JSONArray)obj1.get("MODIFIED_ENUM");
                JSONArray arr4 = (JSONArray)obj1.get("MODIFIED_CLASS");
                List<Object> temp =Stream.concat(Stream.of(arr4.toArray()),Stream.concat(Stream.of(arr3.toArray()),Stream.concat(Stream.of(arr1.toArray()),Stream.of(arr2.toArray())))).collect(Collectors.toList());

//                for(Object jsonObject : temp){
//                    JSONObject t = (JSONObject)jsonObject;
//                    String name = (String) t.get("ENTITY");
//                    if(!(name.contains("@")))
//                        l.add(name.substring(name.lastIndexOf(".") + 1));
//                    else{
//                        name = name.substring(0,name.lastIndexOf("@"));
//                        name = name.substring(name.lastIndexOf(".") + 1);
//                        l.add(name);
//                    }
//                }
                Pair<String,String> pair1 = new Pair<>(pathPrev,pathCurr);
                Pair<List<Object>, Pair<String,String>> pair = new Pair<>(temp,pair1);
                ret.add(pair);
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return ret;
    }

    public static void main(String[] args) {
        generateSingleFile("C:\\Users\\Tristan\\Desktop\\tt\\out\\diff","C:\\Users\\Tristan\\Desktop\\tt\\out1");
    }


    public static boolean matchAllEntity(TypeDeclaration typeDeclaration,String s){
        Set filtered = (Set) typeDeclaration.bodyDeclarations().stream().filter(t -> {
            if (t instanceof FieldDeclaration)
                return s.contains(((VariableDeclarationFragment) ((FieldDeclaration) t).fragments().get(0)).getName().toString());
            else if (t instanceof MethodDeclaration) {
                Object tt = (TypeDeclaration) ((MethodDeclaration) t).getParent();
                StringBuilder sb = new StringBuilder();
                while(tt != null &&  (! (tt instanceof CompilationUnit))){
                    sb.insert(0, ((TypeDeclaration)tt).getName().toString() + ".");
                    tt = ((TypeDeclaration)tt).getParent();
                }
                try {
                    String methodName = GetInfo.methodDeclarationToString((MethodDeclaration) t);
                    String pkg = sb.toString();
                    String sig = pkg + methodName;
                    return s.contains(methodName);
                }
                catch (Exception e){
                    System.out.println(s);
//                    System.out.println();
                    return false;
                }
            }
            else if(t instanceof TypeDeclaration){

                return matchAllEntity((TypeDeclaration) t,s);

            }
            else {
                return false;
            }
        }).collect(Collectors.toSet());
        typeDeclaration.bodyDeclarations().retainAll(filtered);
        return filtered.size() != 0;
    }

    public static void generateSingleFile(String inputPath, String outputPath){
        List<File> l = DirUtil.getAllSuffixFilesOfADirectory(inputPath,".json");
        for(File f : l){
            for(Pair<List<Object>,Pair<String,String>> pair : getEntityList(f.getAbsolutePath())){
                String currPath = pair.second.second;
                String prevPath = pair.second.first;
                String name = currPath.substring(currPath.lastIndexOf(File.separator) + 1, currPath.lastIndexOf("."));

                ASTNode cuCurr = getCompilationUnit(currPath);
                ASTNode cuPrev = getCompilationUnit(prevPath);
//                System.out.println("----" + name + "----");
                int count = 0;
                for(Object object : pair.first){
                    count++;
                    JSONObject jsonObject = (JSONObject) object;
                    JSONObject object1;
                    String sigPrev;
                    String sigCurr;
                    if(jsonObject.containsKey("MODIFIED_FROM")){
                        object1 = (JSONObject) jsonObject.get("MODIFIED_FROM");
                        sigPrev = object1.getString("PREV_SIGNATURE");
                        sigCurr = object1.getString("CURR_SIGNATURE");

                    }
                    else{
                        sigPrev = (String)jsonObject.get("ENTITY");
                        sigCurr = (String)jsonObject.get("ENTITY");

                    }
                    if(((String)jsonObject.get("DESC")).contains("update") || ((String)jsonObject.get("DESC")).contains("move")){
                        generate(outputPath, name, cuCurr, count, sigCurr,"curr");
                        generate(outputPath, name, cuPrev, count, sigPrev, "prev");

                    }
                    else if(((String)jsonObject.get("DESC")).contains("add")){
                        generate(outputPath, name, cuCurr, count, sigCurr,"curr");
                    }
                    else if(((String)jsonObject.get("DESC")).contains("delete")){
                        generate(outputPath, name, cuPrev, count, sigPrev, "prev");
                    }
                }

            }
        }
    }

    public static void generate(String outputPath, String name, ASTNode cu, int count, String sig,String prevOrCurr) {
//        System.out.println("NO." + count + ", Try generating file : "  + prevOrCurr + File.separator + name + "." + sig.substring(sig.lastIndexOf(".") + 1));
        CompilationUnit cuPrevCopy = (CompilationUnit) ASTNode.copySubtree(cu.getAST(),cu);
        matchAllEntity((TypeDeclaration) cuPrevCopy.types().get(0),sig);

        String fileName1 = outputPath + File.separator + "data" + File.separator + prevOrCurr + File.separator + name  + "-" + count + "-" + sig.substring(sig.lastIndexOf(".") + 1) + ".java";
        writeData(cuPrevCopy,fileName1);
    }

    public static void writeData(CompilationUnit cu,String fileName){
        if(((TypeDeclaration) cu.types().get(0)).bodyDeclarations().size() > 0){
            File f1 = new File(fileName);
            String code = cu.toString();
            CodeFormatter codeFormatter = ToolFactory.createCodeFormatter(null);

            TextEdit textEdit = codeFormatter.format(CodeFormatter.K_COMPILATION_UNIT, code, 0, code.length(), 0, null);
            IDocument doc = new Document(code);
            try {
                textEdit.apply(doc);
                FileRWUtil.writeInAll(f1,doc.get().getBytes());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void generateSingle(String jsonPath,String outputPath){
        List<File> l = DirUtil.getAllSuffixFilesOfADirectory(jsonPath,".json");
//        MapperFactory mapperFactory = new DefaultMapperFactory.Builder().build();
//
//        mapperFactory.classMap(CompilationUnit.class, CompilationUnit.class)
//                .byDefault()
//                .register();
//        ConverterFactory converterFactory = mapperFactory.getConverterFactory();
//        MapperFacade mapper = mapperFactory.getMapperFacade();
        for(File f : l){
            for(Pair<List<Object>,Pair<String,String>> pair : getEntityList(f.getAbsolutePath())){
                String currPath = pair.second.second;
                String prevPath = pair.second.first;
                String name = currPath.substring(currPath.lastIndexOf(File.separator) + 1, currPath.lastIndexOf("."));

                ASTNode cuCurr = getCompilationUnit(currPath);
                ASTNode cuPrev = getCompilationUnit(prevPath);
//                System.out.println("----" + name + "----");
                int count = 0;
                for(Object s : pair.first){
                    CompilationUnit cu = (CompilationUnit)ASTNode.copySubtree(cuCurr.getAST(),cuCurr);

                    Set ll = (Set) ((TypeDeclaration) cu.types().get(0)).bodyDeclarations().stream().filter(t -> {
                        if (t instanceof FieldDeclaration)
                            return ((VariableDeclarationFragment) ((FieldDeclaration) t).fragments().get(0)).getName().toString().equals(s);
                        else if (t instanceof MethodDeclaration) {
                            String methodName = ((MethodDeclaration) t).getName().toString();
                            String params = "(";
                            for (Object o : ((MethodDeclaration) t).parameters())
                                params += ((SingleVariableDeclaration) o).getType().toString() + ",";
                            if (params.contains(","))
                                params = params.substring(0, params.lastIndexOf(","));
                            params += ")";
                            String sig = methodName + params;
                            return s.equals(sig);
                        }
                        else
                            return false;

                    }).collect(Collectors.toSet());
                    ((TypeDeclaration) cu.types().get(0)).bodyDeclarations().retainAll(ll);
                    if(((TypeDeclaration) cu.types().get(0)).bodyDeclarations().size() > 0){
//                        System.out.println(count++);
                        File f1 = new File(outputPath + File.separator + "data" + File.separator + name  + "-" + count + "-" + s + ".java");
                        String code = cu.toString();
                        CodeFormatter codeFormatter = ToolFactory.createCodeFormatter(null);

                        TextEdit textEdit = codeFormatter.format(CodeFormatter.K_COMPILATION_UNIT, code, 0, code.length(), 0, null);
                        IDocument doc = new Document(code);
                        try {
                            textEdit.apply(doc);
//                        System.out.println(doc.get());
                            FileRWUtil.writeInAll(f1,doc.get().getBytes());
                            //Thread.sleep(2000);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }



        }
    }


    public static boolean containDec(List<String> l, String str){
        for(String s : l){
            if(str.contains(s)){
                return true;
            }
        }
        return false;
    }
}



//            for(Object dec:((TypeDeclaration)cu.types().get(0)).bodyDeclarations()){
//
//                System.out.println(dec.toString());
//                AST ast = AST.newAST(AST.JLS8);
//                CompilationUnit compilationUnit = ast.newCompilationUnit();
//                TypeDeclaration programClass = ast.newTypeDeclaration();
//                programClass.setName(ast.newSimpleName(name));
//                programClass.modifiers().add(ast.newModifier(Modifier.ModifierKeyword.PUBLIC_KEYWORD));
//                compilationUnit.types().add(programClass);
//                if(dec instanceof FieldDeclaration){
//                    programClass.bodyDeclarations().add(cu);
//                }
//                else if(dec instanceof MethodDeclaration){
//
//                }
//                else if(dec instanceof EnumDeclaration){
//
//                }
//                else if(dec instanceof TypeDeclaration){
//
//                }
//FieldDeclaration fff= ast.newFieldDeclaration(((FieldDeclaration)dec).fragments().get(0));
//                MethodDeclaration helloMethod= ast.newMethodDeclaration();
//                helloMethod.setName(ast.newSimpleName("hello"));
//                helloMethod.modifiers().add(ast.newModifier(Modifier.ModifierKeyword.PUBLIC_KEYWORD));
//                helloMethod.setReturnType2(ast.newPrimitiveType(PrimitiveType.VOID));
//// 将方法装入类中
//                programClass.bodyDeclarations().add(helloMethod);
//// 为方法增加语句块
//                Block helloBlock = ast.newBlock();
//                helloMethod.setBody(helloBlock);


// 最后打印出创建的代码内容
//                System.out.println(compilationUnit.toString());
//new FieldDeclaration(dec);