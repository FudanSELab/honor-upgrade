package edu.fdu.se.core.miningactions.statement;

import com.github.gumtreediff.actions.model.Action;
import edu.fdu.se.core.miningactions.bean.Match;
import edu.fdu.se.core.miningactions.bean.MatchStatement;
import edu.fdu.se.core.miningactions.bean.MiningActionData;
import edu.fdu.se.core.miningactions.util.AstRelations;
import edu.fdu.se.core.miningactions.util.TraverseWays;
import edu.fdu.se.core.miningchangeentity.base.ChangeEntityDesc;
import edu.fdu.se.core.miningchangeentity.statement.BreakContinueEntity;
import edu.fdu.se.lang.common.ClassLookupTbl;
import edu.fdu.se.lang.common.MethodLookupTbl;
import org.eclipse.jdt.core.dom.ASTNode;

/**
 * Created by huangkaifeng on 2018/2/7.
 *
 */
@ClassLookupTbl(key = ChangeEntityDesc.StageIIENTITY.ENTITY_BREAK + ChangeEntityDesc.SPLITTER + ChangeEntityDesc.StageIIENTITY.ENTITY_CONTINUE)
public class MatchControlStatements extends MatchStatement {

    public BreakContinueEntity newEntity() {
        return new BreakContinueEntity(mBean);
    }


    @MethodLookupTbl(key = ASTNode.BREAK_STATEMENT)
    public void matchBreakTopDown(MiningActionData fp, Action a) {
        init();
        traverseTopDown(fp, a, TraverseWays.TopDown.BREAK_CONTINUE, -2);
        newBean(a);
        BreakContinueEntity code = newEntity();
        setTopDown(code, a, fp, 0);
    }

    @MethodLookupTbl(key = ASTNode.CONTINUE_STATEMENT)
    public void matchContinueTopDown(MiningActionData fp, Action a) {

        init();
        traverseTopDown(fp, a, TraverseWays.TopDown.BREAK_CONTINUE, -2);
        newBean(a);
        BreakContinueEntity code = newEntity();
        setTopDown(code, a, fp, 1);

    }
}
