package edu.fdu.se.core.links.linkbean;

import edu.fdu.se.core.links.generator.LinkConstants;
import edu.fdu.se.core.miningchangeentity.base.ChangeEntity;
import org.json.JSONObject;

/**
 * Created by huangkaifeng on 4/7/18.
 *
 */
public class Link {

    public ChangeEntity c1;
    public ChangeEntity c2;
    /**
     * @link LinkConstants.LINK_DEF_USE
     */
    public int type;
    public String desc;
    public int isCrossFile;

    /**
     * @param changeEntity1 def basetype
     * @param changeEntity2 use subtype
     * @param type
     * @param desc
     * @param isCrossFile
     */
    public Link(ChangeEntity changeEntity1, ChangeEntity changeEntity2, int type, String desc, int isCrossFile) {
        this.c1 = changeEntity1;
        this.c2 = changeEntity2;
        this.type = type;
        this.desc = desc;
        this.isCrossFile = isCrossFile;
    }

    public String isCrossFileString() {
        if (isCrossFile == LinkConstants.INSIDE_USE) {
            return "within file";
        } else {
            return "across file";
        }
    }

    @Override
    public String toString() {
        return c1.getChangeEntityId() + " -> " + c2.getChangeEntityId() + " " + LinkConstants.getLinkDescString(type) + " " + isCrossFileString() + " " + desc;
    }


}
