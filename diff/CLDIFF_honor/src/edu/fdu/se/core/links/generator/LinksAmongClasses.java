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
 * Created by huangkaifeng on 2018/4/16.
 */
public class LinksAmongClasses {

    private MiningActionData mad1;
    private MiningActionData mad2;

    private LinkGraph graph;
    private String fileNameA;
    private String fileNameB;

    public LinksAmongClasses(ChangeEntityTreeContainer changeEntityTreeContainer, int i, int j, LinkGraph graph) {
        this.mad1 = changeEntityTreeContainer.getMiningActionData(i);
        this.mad2 = changeEntityTreeContainer.getMiningActionData(j);
        this.graph = graph;
        this.fileNameA = changeEntityTreeContainer.getFileName(i);
        this.fileNameB = changeEntityTreeContainer.getFileName(j);
    }

    private List<Map.Entry<BodyDeclarationPair, List<ChangeEntity>>> getEntryList(MiningActionData mad) {
        Map<BodyDeclarationPair, List<ChangeEntity>> mMap1;
        mMap1 = mad.preCacheData.getEntityTree().getLayerChangeEntityMap();
        List<Map.Entry<BodyDeclarationPair, List<ChangeEntity>>> entryList1 = new ArrayList<>(mMap1.entrySet());
        return entryList1;
    }

    /**
     * two file change entities
     * A,B
     */
    public void generateLink() {
        List<Link> arrLink = new ArrayList<>();
        List<Map.Entry<BodyDeclarationPair, List<ChangeEntity>>> l1 = getEntryList(this.mad1);
        List<Map.Entry<BodyDeclarationPair, List<ChangeEntity>>> l2 = getEntryList(this.mad2);
        defUse(arrLink, l1, l2);// A def, B use
        graph.addEntry(arrLink);

    }

    /**
     * 对 TreeA TreeB 中的Key排列组合
     * @param arrLink
     * @param entryList1
     * @param entryList2
     */
    private void defUse(List<Link> arrLink, List<Map.Entry<BodyDeclarationPair, List<ChangeEntity>>> entryList1, List<Map.Entry<BodyDeclarationPair, List<ChangeEntity>>> entryList2) {
        for (int i = 0; i < entryList1.size(); i++) {
            for (int j = 0; j < entryList2.size(); j++) {
                BodyDeclarationPair blockAKey = entryList1.get(i).getKey();
                List<ChangeEntity> blockAList = entryList1.get(i).getValue();
                BodyDeclarationPair blockBKey = entryList2.get(j).getKey();
                List<ChangeEntity> blockBList = entryList2.get(j).getValue();
                LinkDefUse.isDefUseMain((BodyDeclaration) blockAKey.getBodyDeclaration(), blockAList, (BodyDeclaration) blockBKey.getBodyDeclaration(), blockBList, LinkConstants.AMONG_USE, arrLink);
            }
        }

    }


}
