package edu.fdu.se.core.miningactions.util;


import com.github.gumtreediff.actions.model.Action;
import com.github.gumtreediff.tree.ITree;
import com.github.gumtreediff.tree.Tree;
import edu.fdu.se.global.Global;
import edu.fdu.se.core.miningactions.bean.MyRange;
import edu.fdu.se.core.miningchangeentity.base.ChangeEntityDesc;

public class AstRelations {

	public static boolean isFatherXXXStatement(Action a,int astNodeType) {
		Tree parentTree = (Tree) a.getNode().getParent();
        int type = Global.astNodeUtil.getNodeTypeId(parentTree.getNode());
		if (astNodeType == type) {
			return true;
		}
		return false;
	}


	public static boolean isFatherXXXStatement(Tree node,int astNodeType){
        int type = Global.astNodeUtil.getNodeTypeId(node);
		if (astNodeType == type) {
			return true;
		}
		return false;
	}

	public static int rangeCompare(MyRange m,MyRange n){
		if(m.endLineNo<=n.startLineNo){
			return 1;
		}else if(n.endLineNo<=m.startLineNo){
			return -1;
		}
		return 0;
	}



	public static MyRange getMyRange(Tree tree, int treeType){
		Integer[] range =  tree.getRange();

		int start = range[0];
		int end = range[1];
		MyRange myRange = null;
		if (treeType == ChangeEntityDesc.TreeType.PREV_TREE_NODE) {
			myRange = new MyRange(start, end, ChangeEntityDesc.TreeType.PREV_TREE_NODE);
		}else{
			myRange = new MyRange(start, end, ChangeEntityDesc.TreeType.PREV_TREE_NODE);
		}
		return myRange;
	}



	public static int countChildrenNumber(ITree t){
		int num = 0;
		if(t.getChildren()!=null){
			num += t.getChildren().size();
			for(ITree tt:t.getChildren()){
				int c = countChildrenNumber(tt);
				num += c;
			}
		}
		return num;
	}
}
