package edu.fdu.se.core.miningactions.expression;

import com.github.gumtreediff.actions.model.Action;
import com.github.gumtreediff.tree.Tree;
import edu.fdu.se.core.miningactions.bean.Match;
import edu.fdu.se.core.miningactions.bean.MatchExpression;
import edu.fdu.se.core.miningactions.bean.MiningActionData;
import edu.fdu.se.core.miningactions.util.AstRelations;
import edu.fdu.se.core.miningactions.util.TraverseWays;
import edu.fdu.se.core.miningchangeentity.base.ChangeEntity;
import edu.fdu.se.core.miningchangeentity.base.ChangeEntityDesc;
import edu.fdu.se.core.miningchangeentity.expression.LiteralChangeEntity;
import edu.fdu.se.lang.common.ClassLookupTbl;
import edu.fdu.se.lang.common.MethodLookupTbl;
import org.eclipse.jdt.core.dom.ASTNode;

@ClassLookupTbl(key = ChangeEntityDesc.StageIIENTITY.ENTITY_BOOLEANLITERAL_EXP + ChangeEntityDesc.SPLITTER + ChangeEntityDesc.StageIIENTITY.ENTITY_CHARACTERLITERAL_EXP + ChangeEntityDesc.SPLITTER + ChangeEntityDesc.StageIIENTITY.ENTITY_NULLLITERAL_EXP + ChangeEntityDesc.StageIIENTITY.ENTITY_NUMBERLITERAL_EXP + ChangeEntityDesc.SPLITTER + ChangeEntityDesc.StageIIENTITY.ENTITY_STRINGLITERAL_EXP + ChangeEntityDesc.SPLITTER + ChangeEntityDesc.StageIIENTITY.ENTITY_TYPELITERAL_EXP)
public class MatchLiteral extends MatchExpression {

    public LiteralChangeEntity newEntity() {
        return new LiteralChangeEntity(mBean);
    }

    @MethodLookupTbl(key = ASTNode.BOOLEAN_LITERAL)
    public void matchBooleanLiteralTopDown(MiningActionData fp, Action a) {
        init();
        traverseTopDown(fp, a, TraverseWays.TopDown.TYPE_I, 0);
        newBean(a);
        LiteralChangeEntity code = newEntity();
        setTopDown(code, a, fp, 0);
    }


    @MethodLookupTbl(key = ASTNode.CHARACTER_LITERAL)
    public void matchCharacterLiteralLiteralTopDown(MiningActionData fp, Action a) {
        init();
        traverseTopDown(fp, a, TraverseWays.TopDown.TYPE_I, 0);
        newBean(a);
        LiteralChangeEntity code = newEntity();
        setTopDown(code, a, fp, 1);
    }

    @MethodLookupTbl(key = ASTNode.NULL_LITERAL)
    public void matchNullLiteralTopDown(MiningActionData fp, Action a) {
        init();
        traverseTopDown(fp, a, TraverseWays.TopDown.TYPE_I, 0);
        newBean(a);
        LiteralChangeEntity code = newEntity();
        setTopDown(code, a, fp, 2);
    }


    @MethodLookupTbl(key = ASTNode.NUMBER_LITERAL)
    public void matchNumberLiteralTopDown(MiningActionData fp, Action a) {
        init();
        traverseTopDown(fp, a, TraverseWays.TopDown.TYPE_I, 0);
        newBean(a);
        LiteralChangeEntity code = newEntity();
        setTopDown(code, a, fp, 3);
    }

    @MethodLookupTbl(key = ASTNode.STRING_LITERAL)
    public void matchStringLiteralTopDown(MiningActionData fp, Action a) {
        init();
        traverseTopDown(fp, a, TraverseWays.TopDown.TYPE_I, 0);
        newBean(a);
        LiteralChangeEntity code = newEntity();
        setTopDown(code, a, fp, 4);
    }

    @MethodLookupTbl(key = ASTNode.TYPE_LITERAL)
    public void matchTypeLiteralTopDown(MiningActionData fp, Action a) {
        init();
        traverseTopDown(fp, a, TraverseWays.TopDown.TYPE_I, 0);
        newBean(a);
        LiteralChangeEntity code = newEntity();
        setTopDown(code, a, fp, 5);
    }


}
