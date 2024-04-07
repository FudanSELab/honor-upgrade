package edu.fdu.se.lang.common.generatingactions;

import com.github.gumtreediff.matchers.MappingStore;
import com.github.gumtreediff.tree.ITree;
import com.github.gumtreediff.tree.TreeContext;
import edu.fdu.se.core.generatingactions.SimpleActionPrinter;

public abstract class ParserTreeGenerator {
    /**
     * pre树
     */
    public TreeContext srcTC;
    /**
     * curr树
     */
    public TreeContext dstTC;
    /**
     * pre根节点
     */
    public ITree src;
    /**
     * curr根节点
     */
    public ITree dst;
    public MappingStore mapping;

    public String fileName;
    public void setFileName(String fileName){
        String[] s = fileName.split("\\.");

        StringBuilder sb = new StringBuilder();

        for(int i = 0;i<s.length-1;i++){
            sb.append(s[i]);
        }
        this.fileName = sb.toString();
    }

    public void setFileName2(String fileName) {
        this.fileName = fileName.substring(0, fileName.length() - 5);
    }

    public String getPrettyOldTreeString() {
        return SimpleActionPrinter.getPrettyTreeString(src);
    }

    public String getPrettyNewTreeString() {
        return SimpleActionPrinter.getPrettyTreeString(dst);
    }
}
