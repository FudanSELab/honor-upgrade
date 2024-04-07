package edu.fdu.se.core.links.generator;

import edu.fdu.se.core.miningchangeentity.base.ChangeEntity;
import edu.fdu.se.global.Global;


public class SimpleLinkPrinter {

    public static void printDefUses(ChangeEntity ce) {
        if (ce.linkBean.defList != null && ce.linkBean.defList.size() != 0) {
            Global.logger.info(ce.linkBean.defList.toString());
        } else {
//            Global.logger.info("Def None");
        }
        if (ce.linkBean.useList != null && ce.linkBean.useList.size() != 0) {
            Global.logger.info(ce.linkBean.useList.toString());
        } else {
//            Global.logger.info("Use None");
        }
        Global.logger.info("\n");
    }
}
