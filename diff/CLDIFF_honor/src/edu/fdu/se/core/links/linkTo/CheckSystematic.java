package edu.fdu.se.core.links.linkTo;

import com.github.gumtreediff.actions.model.Action;
import com.github.gumtreediff.actions.model.Move;
import com.github.gumtreediff.tree.Tree;
import edu.fdu.se.core.links.generator.ChangeEntityTreeContainer;
import edu.fdu.se.core.links.generator.LinkConstants;
import edu.fdu.se.core.links.generator.LinkGraph;
import edu.fdu.se.core.links.linkbean.Link;
import edu.fdu.se.core.links.similarity.TreeDistance;
import edu.fdu.se.core.miningactions.bean.MiningActionData;
import edu.fdu.se.core.miningchangeentity.base.ChangeEntity;
import edu.fdu.se.core.miningchangeentity.base.ChangeEntityDesc;
import edu.fdu.se.core.miningchangeentity.base.MemberPlusChangeEntity;
import edu.fdu.se.core.miningchangeentity.member.MethodChangeEntity;
import edu.fdu.se.core.preprocessingfile.data.BodyDeclarationPair;
import edu.fdu.se.global.Constants;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CheckSystematic {

    private static int isCrossFile(List<Integer> sizes,int a,int b){
        int aa = 0;
        int bb = 0;
        for(int i = 0;i<sizes.size();i++){
            int left = sizes.get(i);
            if(a>=left){
                aa = left;
            }
            if(b>=left){
                bb = left;
            }
        }
        if(aa == bb){
            return LinkConstants.INSIDE_USE;
        }
        return LinkConstants.AMONG_USE;
    }

    public static void checkSimi(ChangeEntityTreeContainer container, LinkGraph graph) {
        List<Integer> sizes = new ArrayList<>();
        List<ChangeEntity> ceList = new ArrayList<>();
        for (int i = 0; i < container.getMinintActionDataSize(); i++) {
            MiningActionData mad = container.getMiningActionData(i);
            List<ChangeEntity> entity = mad.getChangeEntityList();
            ceList.addAll(entity);
            sizes.add(ceList.size());
        }
        for (int i = 0; i < ceList.size(); i++) {
            for (int j = i + 1; j < ceList.size(); j++) {
                ChangeEntity ce1 = ceList.get(i);
                ChangeEntity ce2 = ceList.get(j);
                checkSimilarity(ce1, ce2, graph,isCrossFile(sizes,i,j));
            }
        }
    }

    private static void checkSimilarity(ChangeEntity a, ChangeEntity b, LinkGraph graph,int isCrossFile) {
        if (!a.getClass().equals(b.getClass())) {
            return;
        }
        if (!a.stageIIBean.getOpt().equals(b.stageIIBean.getOpt())) {
//                || a.stageIIBean.getOpt().equals(ChangeEntityDesc.StageIIOpt.OPT_CHANGE_MOVE)
//                || b.stageIIBean.getOpt().equals(ChangeEntityDesc.StageIIOpt.OPT_CHANGE_MOVE)) {
            return;
        }
        if (a.clusteredActionBean != null && b.clusteredActionBean != null) {
            if (a.clusteredActionBean.fafather.getAstClass().equals(b.clusteredActionBean.fafather.getAstClass()) && a.clusteredActionBean.actions.size() == b.clusteredActionBean.actions.size()) {
                mightSimilar(a,b,graph,isCrossFile);

            }
        }else{
            if(a.stageIIBean.getChangeEntity().equals(b.stageIIBean.getChangeEntity())) {
                if (a instanceof MemberPlusChangeEntity && b instanceof MemberPlusChangeEntity) {
                    MemberPlusChangeEntity am = (MemberPlusChangeEntity) a;
                    MemberPlusChangeEntity bm = (MemberPlusChangeEntity) b;
                    if(am.bodyDeclarationPair ==null || bm.bodyDeclarationPair == null){
                        return;
                    }
                    BodyDeclaration bodyA = (BodyDeclaration) am.bodyDeclarationPair.getBodyDeclaration();
                    BodyDeclaration bodyB = (BodyDeclaration) bm.bodyDeclarationPair.getBodyDeclaration();
                    if(bodyA.toString().equals(bodyB.toString())){
                        newLink(a, b, isCrossFile, graph);
                    }

                }


            }
        }
    }

    private static void mightSimilar(ChangeEntity a, ChangeEntity b, LinkGraph graph,int isCrossFile){
        if(ChangeEntityDesc.StageIIOpt.OPT_MOVE.equals(a.stageIIBean.getOpt())){
            Action aa = a.clusteredActionBean.curAction;
            Action ab = b.clusteredActionBean.curAction;
            if(aa instanceof Move && ab instanceof Move){
                Move maa = (Move)aa;
                Move mab = (Move)ab;
                if(maa.cNum == mab.cNum){
                    if(maa.cNum ==1 ){
                        genSimpleNameLink(a,b,isCrossFile,graph);
                    }else{
                        useTreeDistanceLinkSimilar(a,b,isCrossFile,graph,(Tree)maa.getNode(), (Tree)mab.getNode());

                    }
                }
            }
        }else {
            if (a.clusteredActionBean.actions.size() == 1) {
                genSimpleNameLink(a,b,isCrossFile,graph);
            } else {
                useTreeDistanceLinkSimilar(a,b,isCrossFile,graph,a.clusteredActionBean.fafather,b.clusteredActionBean.fafather);

            }
        }

    }

    private static void genSimpleNameLink(ChangeEntity a,ChangeEntity b,int isCrossFile,LinkGraph graph){
        Tree t1 = (Tree) a.clusteredActionBean.actions.get(0).getNode();
        Tree t2 = (Tree) b.clusteredActionBean.actions.get(0).getNode();
        if (t1.getNode() instanceof SimpleName && t2.getNode() instanceof SimpleName) {
            String v1 = ((SimpleName) t1.getNode()).getIdentifier();
            String v2 = ((SimpleName) t2.getNode()).getIdentifier();
            if (v1.equals(v2)) {
                newLink(a, b, isCrossFile, graph);
            }
        }

    }

    private static void useTreeDistanceLinkSimilar(ChangeEntity a,ChangeEntity b,int isCrossFile,LinkGraph graph, Tree na, Tree nb){
        TreeDistance treeDistance = new TreeDistance(na, nb);
        float distance = treeDistance.calculateTreeDistance();
        if (distance <= 0.5) {
            newLink(a, b, isCrossFile, graph);
        }

    }


    private static void newLink(ChangeEntity a,ChangeEntity b,int isCrossFile,LinkGraph graph){
        Link link = new Link(a, b, LinkConstants.LINK_SYSTEMATIC, ChangeEntityDesc.StageIIILinkType.SYSTEMATIC_CHANGE, isCrossFile);
        graph.addEntry(link);
    }
}
