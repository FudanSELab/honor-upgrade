package edu.fdu.se.core.links.similarity;

import at.unisalzburg.dbresearch.apted.node.Node;
import at.unisalzburg.dbresearch.apted.node.StringNodeData;
import com.github.gumtreediff.tree.ITree;
import com.github.gumtreediff.tree.Tree;
import edu.fdu.se.global.Global;

import java.util.List;

// [TODO] Make this parser independent from FormatUtilities - move here relevant elements.

/**
 * @see Node
 * @see StringNodeData
 */
public class TreeInputParser {

    /**
     * Parses the input tree(gumtree) and converts it to our tree(node<stringNodeData></stringNodeData>
     * representation using the {@link Node} class.
     *
     * @param t input tree.
     * @return tree representation of the tree.
     * @see Node
     */
    public Node<StringNodeData> fromTree(Tree t) {
        String nodeName = t.getAstClass().getSimpleName();
        Node<StringNodeData> node = new Node<>(new StringNodeData(nodeName));
        List<ITree> children = t.getChildren();
        if (children != null && children.size() != 0) {
            for (int i = 0; i < children.size(); i++)
                node.addChild(fromTree((Tree) children.get(i)));
        }
        if (Global.astNodeUtil.isLiteralOrName(t)){
            if(t.getLabel()!=null){
                node.addChild(new Node<>(new StringNodeData(t.getLabel())));
            }
    }
    return node;
}

}
