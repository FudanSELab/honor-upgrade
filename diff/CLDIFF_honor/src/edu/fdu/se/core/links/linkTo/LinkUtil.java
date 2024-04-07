package edu.fdu.se.core.links.linkTo;

import com.github.gumtreediff.tree.Tree;
import edu.fdu.se.global.Global;
import edu.fdu.se.core.miningactions.bean.MyRange;
import edu.fdu.se.core.miningchangeentity.base.ChangeEntity;
import org.eclipse.jdt.core.dom.MethodDeclaration;

/**
 * Created by huangkaifeng on 2018/4/16.
 *
 */
public class LinkUtil {

    public static int isRangeWithin(ChangeEntity ce1, ChangeEntity ce2) {
        MyRange myRange1 = ce1.getLineRange();
        MyRange myRange2 = ce2.getLineRange();
        int res= myRange1.isRangeWithin(myRange2);
        if ( res!= 0) {
            return res;
        } else {
            return res;
        }
    }

    public static String findResidingMethodName(Tree t){

        while(true){
            if (Global.astNodeUtil.isCompilationUnit(t.getNode())) {
                break;
            }
            if (Global.astNodeUtil.isMethodDeclaration(t.getNode())) {
                Object md = t.getNode();
                return Global.astNodeUtil.getMethodName(md);
            }
            t = (Tree) t.getParent();
        }
        return null;

    }

    public static String[] findResidingClassAndSuperClass(Tree t){

        while(true){
            if (t.getNode().getClass().toString().endsWith("CompilationUnit")) {
                break;
            }
            if (t.getNode().getClass().toString().endsWith("Declaration")) {
                MethodDeclaration md = (MethodDeclaration) t.getNode();
//                return md.getName().toString();
            }
            t = (Tree) t.getParent();
        }
        return null;

    }
}
