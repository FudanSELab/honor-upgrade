package edu.fdu.se.lang.c.generatingactions;

import com.github.gumtreediff.matchers.Matcher;
import com.github.gumtreediff.matchers.Matchers;
import com.github.gumtreediff.tree.TreeContext;
import edu.fdu.se.core.miningchangeentity.base.ChangeEntityDesc;
import edu.fdu.se.core.preprocessingfile.data.PreCacheData;
import edu.fdu.se.global.Global;
import edu.fdu.se.lang.common.generatingactions.ParserTreeGenerator;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;

public class CTreeGenerator extends ParserTreeGenerator {



    public CTreeGenerator(PreCacheData preCacheData){
        IASTTranslationUnit prev = (IASTTranslationUnit) Global.astNodeUtil.getPrevCu(preCacheData.getPrevCu());
        IASTTranslationUnit curr = (IASTTranslationUnit) Global.astNodeUtil.getCurrCu(preCacheData.getCurrCu());
        srcTC = generateFromCompilationUnit(prev, ChangeEntityDesc.TreeType.PREV_TREE_NODE, preCacheData);
        src = srcTC.getRoot();
        dstTC = generateFromCompilationUnit(curr, ChangeEntityDesc.TreeType.CURR_TREE_NODE, preCacheData);
        dst = dstTC.getRoot();
        Matcher m = Matchers.getInstance().getMatcher(src, dst);
        m.match();
        mapping = m.getMappings();
    }
    private TreeContext generateFromCompilationUnit(IASTTranslationUnit cu, int treeType,PreCacheData preCacheData) {
        CParserVisitor visitor = new CParserVisitor(treeType,preCacheData);
        visitor.getTreeContext().setTu(cu);
        IASTNode astNode = cu;
        setShouldVisit(visitor);
        astNode.accept(visitor);
        TreeContext ctx = visitor.getTreeContext();
        ctx.validate();
        return ctx;
    }
    private void setShouldVisit(CParserVisitor visitorC){
        visitorC.shouldVisitTranslationUnit = true;
        visitorC.shouldVisitArrayModifiers  = true;
        visitorC.shouldVisitAttributes  = true;
        visitorC.shouldVisitBaseSpecifiers   = true;
        visitorC.shouldVisitCaptures  = true;
        visitorC.shouldVisitDeclarations   = true;
        visitorC.shouldVisitDeclarators  = true;
        visitorC.shouldVisitDeclSpecifiers   = true;
        visitorC.shouldVisitDesignators  = true;
        visitorC.shouldVisitEnumerators  = true;
        visitorC.shouldVisitExpressions = true;
        visitorC.shouldVisitImplicitNameAlternates  = true;
        visitorC.shouldVisitImplicitNames = true;
        visitorC.shouldVisitInitializers  = true;
        visitorC.shouldVisitNames = true;
        visitorC.shouldVisitNamespaces  = true;
        visitorC.shouldVisitParameterDeclarations = true;
        visitorC.shouldVisitPointerOperators   = true;
        visitorC.shouldVisitProblems = true;
        visitorC.shouldVisitStatements  = true;
        visitorC.shouldVisitTemplateParameters = true;
        visitorC.shouldVisitTokens  = true;
        visitorC.shouldVisitTypeIds  = true;
    }
}
