package edu.fdu.se.core.generatingactions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.gumtreediff.actions.model.Action;
import com.github.gumtreediff.actions.model.Delete;
import com.github.gumtreediff.actions.model.Insert;
import com.github.gumtreediff.actions.model.Move;
import com.github.gumtreediff.actions.model.Update;
import com.github.gumtreediff.tree.ITree;

/**
 * GumTree现存问题
 * Move的误识别
 * 和block相搭配后很容易出现误识别
 *
 * */
public class ActionsMap {

	public List<Action> insertActions;
	public List<Action> updateActions;
	public List<Action> moveActions;
	public List<Action> deleteActions;
	public List<Action> allActions;

	private Map<Action, Integer> allActionMap;

    public ActionsMap() {
		super();
		this.insertActions = new ArrayList<>();
		this.moveActions = new ArrayList<>();
		this.updateActions = new ArrayList<>();
		this.deleteActions = new ArrayList<>();
		this.allActions = new ArrayList<>();
		this.allActionMap = new HashMap<>();
	}

	public void addAction(Action action) {
		if (action instanceof Insert) {
			this.insertActions.add(action);
		} else if (action instanceof Update) {
			this.updateActions.add(action);
		} else if (action instanceof Move) {
			this.moveActions.add(action);
		} else if (action instanceof Delete) {
            this.deleteActions.add(action);
		}
		this.allActions.add(action);
	}

	public List<Action> getInsertActions() {
		return insertActions;
	}


	public List<Action> getUpdateActions() {
		return updateActions;
	}


	public List<Action> getMoveActions() {
		return moveActions;
	}


	public List<Action> getDeleteActions() {
		return deleteActions;
	}

	public List<Action> getAllActions() {
		return allActions;
	}


	public void generateActionMap() {
        for (Action tmp : this.allActions) {
			this.allActionMap.put(tmp, 0);
		}
    }

    public void addActionMap(Action a) {
        this.allActionMap.put(a, 0);
	}

	public Map<Action, Integer> getAllActionMap() {
		return allActionMap;
	}


}
