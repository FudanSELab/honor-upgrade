package edu.fdu.se.core.links.linkbean;

import com.github.gumtreediff.tree.ITree;

public class ParseTuple {
    ITree node;
    int treeType;
    String value;
    Object exp;
    public ParseTuple(ITree node,int treeType,String value,Object exp){
        this.node = node;
        this.treeType = treeType;
        this.value = value;
        this.exp = exp;
    }
}
