package edu.fdu.se.core.links.linkTo;

import edu.fdu.se.core.links.generator.LinkConstants;
import edu.fdu.se.core.links.linkbean.Link;
import edu.fdu.se.core.miningchangeentity.base.ChangeEntity;
import edu.fdu.se.core.miningchangeentity.base.ChangeEntityDesc;
import edu.fdu.se.core.miningchangeentity.member.MethodChangeEntity;
import edu.fdu.se.global.Global;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by huangkaifeng on 2018/4/7.
 */
public class CheckInheritance {

    /**
     * inheritance
     *
     * @param methodListA
     * @param methodListB
     */
    public static void checkMethodInheritance(List<ChangeEntity> methodListA, List<ChangeEntity> methodListB, List<Link> arrLink) {
        if (methodListA.size() == 0 || methodListB.size() == 0) {
            return;
        }
        List<MethodChangeEntity> mc1 = new ArrayList<>();
        List<MethodChangeEntity> mc2 = new ArrayList<>();
        for (ChangeEntity ce : methodListA) {
            if (ce instanceof MethodChangeEntity) {
                MethodChangeEntity mce = (MethodChangeEntity) ce;
                mc1.add(mce);
            }
        }
        for (ChangeEntity ce : methodListB) {
            if (ce instanceof MethodChangeEntity) {
                MethodChangeEntity mce = (MethodChangeEntity) ce;
                mc2.add(mce);
            }
        }
        for (MethodChangeEntity m : mc1) {
            for (MethodChangeEntity n : mc2) {
                Link link = null;
                if (m.getMethodName() == null || n.getMethodName() == null) {
                    Global.fileOutputLog.writeErrFile("[ERR]Method name null-- @checkMethodInheritance");
                    continue;
                }
                //todo 后面需要考虑不同参数的相同方法名
                if (m.getMethodName().equals(n.getMethodName())) {
                    link = checkInheritance(m, n);
                }
                if (link != null) {
                    boolean doAdd= true;
                    for(Link l:arrLink){
                        if(l.c1 == link.c2 && l.c2 == link.c1 && l.type == link.type && link.type == LinkConstants.LINK_OVERRIDE_METHOD){
                            doAdd = false;

                        }
                    }
                    if(doAdd) {
                        arrLink.add(link);
                    }
                }
            }
        }
    }

    private static Link genLink(MethodChangeEntity m, MethodChangeEntity n, String linkType, int linkTypeFlag) {
        String desc = String.format(linkType, m.getMethodName(), n.linkBean.inheritance.className);
        Link link = new Link(m, n, linkTypeFlag, desc, LinkConstants.AMONG_USE);
        return link;
    }


    private static Link checkInheritance(MethodChangeEntity a, MethodChangeEntity b) {
        Link link = null;
        if (a.linkBean.inheritance.interfazz != null && a.linkBean.inheritance.interfazz.contains(b.linkBean.inheritance.className)) {
            link = genLink(a, b, ChangeEntityDesc.StageIIILinkType.IMPLEMENT_METHOD, LinkConstants.LINK_IMPLEMENTING_INTERFACE);
        } else if (a.linkBean.inheritance.superClass != null && a.linkBean.inheritance.superClass.equals(b.linkBean.inheritance.className)) {
            if (a.linkBean.inheritance.isClassAbstract == 1) {
                link = genLink(a, b, ChangeEntityDesc.StageIIILinkType.ABSTRACT_METHOD, LinkConstants.LINK_ABSTRACT_METHOD);
            } else {
                link = genLink(a, b, ChangeEntityDesc.StageIIILinkType.OVERRIDE_METHOD, LinkConstants.LINK_OVERRIDE_METHOD);
            }
        } else if (a.linkBean.inheritance.interfazz!=null && b.linkBean.inheritance.interfazz!=null){
            List<String> interfazzA = a.linkBean.inheritance.interfazz;
            List<String> interfazzB = b.linkBean.inheritance.interfazz;
            for(String ai:interfazzA){
                    if(interfazzB.contains(ai)){
                        link = genLink(a, b, ChangeEntityDesc.StageIIILinkType.OVERRIDE_METHOD, LinkConstants.LINK_OVERRIDE_METHOD);
                    }
            }
        }
        return link;
    }

}
