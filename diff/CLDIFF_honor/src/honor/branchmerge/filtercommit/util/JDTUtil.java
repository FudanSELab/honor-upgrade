package honor.branchmerge.filtercommit.util;

import javafx.util.Pair;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

import java.util.Map;

public class JDTUtil {

    /**
     * 直接从 byte[] 获得 cu 的 getCompilationUnit
     * @param input
     * @return
     */
    public static CompilationUnit getCompilationUnit(byte[] input) {
        Map<String, String> options = JavaCore.getOptions();
        options.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_8);
        options.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_8);
        options.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_8);
        options.put(JavaCore.COMPILER_DOC_COMMENT_SUPPORT, JavaCore.ENABLED);

        ASTParser astParser = ASTParser.newParser(AST.JLS8);
        astParser.setKind(ASTParser.K_COMPILATION_UNIT);
        astParser.setCompilerOptions(options);
        astParser.setSource(new String(input).toCharArray());
        return (CompilationUnit) (astParser.createAST(null));
    }

    /**
     * 根据一个给定的 ASTNode，通过 cu 中的 LineEndTable，获取 ASTNode 的行数。
     * 这里获得了 ASTNode 的起始行和终止行。
     * @param cu
     * @param node
     * @return Pair<Integer, Integer> 前为起始行，后为终止行。
     */
    public static Pair<Integer, Integer> getBeginEndOfASTNode(CompilationUnit cu, ASTNode node) {
        int begPos = node.getStartPosition();
        int endPos = begPos + node.getLength();
        return new Pair<>(cu.getLineNumber(begPos), cu.getLineNumber(endPos));
    }

    /**
     * 根据一个给定的 ASTNode，直接获得该 node 的起始终止 position 信息
     * @param node
     * @return Pair<Integer, Integer> 前为起始 pos，后为终止 pos。
     */
    public static Pair<Integer, Integer> getBeginEndPosOfASTNode(ASTNode node) {
        int begPos = node.getStartPosition();
        int endPos = begPos + node.getLength();
        return new Pair<>(begPos, endPos);
    }
}
