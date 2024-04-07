package edu.fdu.se.core.miningactions.bean;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.github.gumtreediff.actions.model.*;
import com.github.gumtreediff.matchers.MappingStore;
import com.github.gumtreediff.tree.ITree;
import com.github.gumtreediff.tree.TreeContext;

import edu.fdu.se.core.generatingactions.ActionsMap;
import edu.fdu.se.core.miningchangeentity.base.ChangeEntity;
import edu.fdu.se.core.miningchangeentity.base.ChangeEntityDesc;
import edu.fdu.se.core.miningchangeentity.member.*;
import edu.fdu.se.core.preprocessingfile.data.BodyDeclarationPair;
import edu.fdu.se.core.preprocessingfile.data.PreCacheData;
import edu.fdu.se.global.Constants;
import edu.fdu.se.global.Global;
import edu.fdu.se.lang.common.generatingactions.ParserTreeGenerator;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jgit.diff.DiffEntry;

public class MiningActionData {

	public DiffEntry.ChangeType changeType;

    public String fileFullPackageName;

    public ActionsMap mActionsMap;
    private MappingStore mMapping;

	protected TreeContext mDstTree;
	protected TreeContext mSrcTree;

	private List<ChangeEntity> mChangeEntityList;

	public PreCacheData preCacheData;


    public MiningActionData(List<ChangeEntity> mList, int treeType){
		this.mChangeEntityList = mList;
		if(treeType == ChangeEntityDesc.TreeType.PREV_TREE_NODE){
			changeType = DiffEntry.ChangeType.DELETE;
		}else{
			changeType = DiffEntry.ChangeType.ADD;
		}
	}

	public MiningActionData(PreCacheData preCacheData, ActionsMap agb, ParserTreeGenerator treeGenerator) {
		this.preCacheData = preCacheData;
        this.mActionsMap = agb;
		this.mMapping = treeGenerator.mapping;
		this.mDstTree = treeGenerator.dstTC;
		this.mSrcTree = treeGenerator.srcTC;
        this.mActionsMap.generateActionMap();
		this.mChangeEntityList = new ArrayList<>();
		changeType = DiffEntry.ChangeType.MODIFY;
	}



	public void setActionTraversedMap(List<Action> mList) {
		for (Action tmp : mList) {
            if (this.mActionsMap.getAllActionMap().containsKey(tmp)) {
                this.mActionsMap.getAllActionMap().put(tmp, 1);
			} else {
				Global.fileOutputLog.writeErrFile("[ERR]action not added@setActionTraversedMap");
			}
		}
	}

	public void setActionTraversedMap(Action a) {
        if (this.mActionsMap.getAllActionMap().containsKey(a)) {
            this.mActionsMap.getAllActionMap().put(a, 1);
		} else {
			Global.fileOutputLog.writeErrFile("[ERR]action not added@setActionTraversedMap");
		}
	}



	public void addOneChangeEntity(ChangeEntity changeEntity){
		this.mChangeEntityList.add(changeEntity);

	}


	public ChangeEntity getEntityByNode(ITree tree){
		if(tree==null) return null;
		for(ChangeEntity changeEntity:this.getChangeEntityList()){
			if(changeEntity.clusteredActionBean.curAction instanceof Move){
				continue;
			}
			if(changeEntity.clusteredActionBean.fafather == tree){
				return changeEntity;
			}
		}
		return null;
	}


	public List<ChangeEntity> getChangeEntityList() {
		return this.mChangeEntityList;
	}

	public ITree getMappedPrevOfCurrNode(ITree curr){
		return this.mMapping.getSrc(curr);
	}
	public ITree getMappedCurrOfPrevNode(ITree prev){
		return this.mMapping.getDst(prev);
	}


    public ChangeEntity addOneBody(BodyDeclarationPair item, String type) {
        ChangeEntity ce = null;
        MyRange myRange = null;
        Object n = item.getBodyDeclaration();
        if (Insert.class.getSimpleName().equals(type)) {
        	myRange = Global.astNodeUtil.getRange(n,ChangeEntityDesc.TreeType.CURR_TREE_NODE);
        } else if (Delete.class.getSimpleName().equals(type)) {
        	myRange = Global.astNodeUtil.getRange(n,ChangeEntityDesc.TreeType.PREV_TREE_NODE);
        }
        if (Global.astNodeUtil.isFieldDeclaration(n)) {
            ce = new FieldChangeEntity(item, type, myRange);
        } else if (Global.astNodeUtil.isMethodDeclaration(n)) {
            ce = new MethodChangeEntity(item, type, myRange);
        } else if (item.getBodyDeclaration() instanceof Initializer) {  //Java专用
            ce = new InitializerChangeEntity(item, type, myRange);
        } else if (Global.astNodeUtil.isTypeDeclaration(n)) {
			ce = Objects.equals(Global.lang, Constants.RUNNING_LANG.JAVA) ? new ClassChangeEntity(item, type, myRange, 0) :new ClassChangeEntity(item, type, myRange);
            //ce = new ClassChangeEntity(item, type, myRange, 0);
        } else if (Global.astNodeUtil.isEnumDeclaration(n)) {
            ce = new EnumChangeEntity(item, type, myRange);
        }
        return ce;
    }


	/**
	 * Move nodes are src, we need node mapped on Curr tree.
	 * @return
	 */
	public List<ITree> getMoveDstNodes(){
		List<ITree> result = new ArrayList<>();
		for(Action a:this.mActionsMap.getMoveActions()){
			Move mv = (Move) a;
			result.add(this.getMappedCurrOfPrevNode(mv.getNode()));
		}
		return result;
	}

	public List<ITree> getUpdateDstNodes(){
		List<ITree> result = new ArrayList<>();
		for(Action a:this.mActionsMap.getUpdateActions()){
			Update up = (Update) a;
			result.add(this.getMappedCurrOfPrevNode(up.getNode()));
		}
		return result;

	}
}
