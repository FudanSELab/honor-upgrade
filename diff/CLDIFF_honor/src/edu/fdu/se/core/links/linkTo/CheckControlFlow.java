package edu.fdu.se.core.links.linkTo;

import edu.fdu.se.core.links.linkbean.Link;
import edu.fdu.se.core.miningchangeentity.base.ChangeEntity;

import java.util.List;

public class CheckControlFlow {

    public static void checkStmtControlFlow(List<ChangeEntity> stmts, List<Link> arrLink) {
        for (int i = 0; i < stmts.size(); i++) {
            ChangeEntity stmt1 = stmts.get(i);
            for (int j = 0; j < stmts.size(); j++) {
                ChangeEntity stmt2 = stmts.get(j);
            }
        }
    }
}
