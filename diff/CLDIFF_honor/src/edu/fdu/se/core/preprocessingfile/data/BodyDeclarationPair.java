package edu.fdu.se.core.preprocessingfile.data;


import edu.fdu.se.core.miningchangeentity.base.CanonicalName;
import edu.fdu.se.global.Global;

/**
 * Created by huangkaifeng on 2018/1/22.
 *
 */
public class BodyDeclarationPair {

    /**
     * body
     */
    private Object body;

    private int treeType;

    private int hashCode;

    private CanonicalName canonicalName;

    public CanonicalName getCanonicalName() {
        return canonicalName;
    }

    /**
     *
     * @param body target
     * @param prefixString target的前缀， 不包括target，'^'
     * @param treeType
     */
    public BodyDeclarationPair(Object body, String prefixString, int treeType) {
        this.body = body;
        String bodyKey = Global.astNodeUtil.getBodyDeclarationUniqueKey(body);
        this.canonicalName = new CanonicalName(prefixString,bodyKey);
        String hashStr = Global.processUtil.bodyDeclarationToString(body).hashCode() + String.valueOf(prefixString.hashCode());
        this.hashCode = hashStr.hashCode();
        this.treeType = treeType;
    }


    public Object getBodyDeclaration() {
        return body;
    }



    @Override
    public boolean equals(Object obj) {
        BodyDeclarationPair bdp = (BodyDeclarationPair) obj;
        if (bdp.hashCode() == this.hashCode) return true;
        return false;
    }

    @Override
    public int hashCode() {
        return hashCode;
    }


}
