package edu.fdu.se.core.links.generator;

import edu.fdu.se.core.links.linkbean.Link;
import edu.fdu.se.core.miningactions.bean.MiningActionData;
import edu.fdu.se.core.miningchangeentity.base.ChangeEntity;
import edu.fdu.se.core.preprocessingfile.data.BodyDeclarationPair;
import org.eclipse.jdt.core.dom.BodyDeclaration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by huangkaifeng on 2018/3/19.
 *
 */
public class LinksInsideClass {

    private MiningActionData mMad;
    private String fileName;
    private LinkGraph graph;

    public LinksInsideClass(String fileName, MiningActionData ced, LinkGraph graph) {
        this.mMad = ced;
        this.fileName = fileName;
        this.graph = graph;
    }

    /**
     * main entrance
     */
    public void generateLink() {
        if (this.mMad.getChangeEntityList().size() <= 1) {
            return;
        }
        List<Link> arrLink = new ArrayList<>();
        List<Map.Entry<BodyDeclarationPair, List<ChangeEntity>>> entryList = new ArrayList<>(this.mMad.preCacheData.getEntityTree().getLayerChangeEntityMap().entrySet());
        defUse(arrLink, entryList);
        graph.addEntry(arrLink);
    }


    /**
     *  对TreeA 中的Key 排列组合
     * @param arrLink
     * @param entryList
     */
    private void defUse(List<Link> arrLink, List<Map.Entry<BodyDeclarationPair, List<ChangeEntity>>> entryList) {
        for (int i = 0; i < entryList.size(); i++) {
            BodyDeclarationPair blockA = entryList.get(i).getKey();
            List<ChangeEntity> blockAList = entryList.get(i).getValue();
            for (int j = i ; j < entryList.size(); j++) {
                BodyDeclarationPair blockB = entryList.get(j).getKey();
                List<ChangeEntity> blockBList = entryList.get(j).getValue();
                LinkDefUse.isDefUseMain((BodyDeclaration) blockA.getBodyDeclaration(), blockAList, (BodyDeclaration) blockB.getBodyDeclaration(), blockBList, LinkConstants.INSIDE_USE, arrLink);
            }
        }

    }




}
