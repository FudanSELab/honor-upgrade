package edu.fdu.se.core.links.generator;

import edu.fdu.se.core.miningactions.bean.MiningActionData;

import java.util.ArrayList;
import java.util.List;

public class ChangeEntityTreeContainer {

    private List<String> fileNames;

    private List<ChangeEntityTree> treeLists;

    private List<MiningActionData> changeEntityDataList;

    public ChangeEntityTreeContainer() {
        fileNames = new ArrayList<>();
        treeLists = new ArrayList<>();
        changeEntityDataList = new ArrayList<>();
    }

    public int addOneTree(String fileName, MiningActionData changeEntityData) {
        fileNames.add(fileName);
        treeLists.add(changeEntityData.preCacheData.getEntityTree());
        changeEntityDataList.add(changeEntityData);
        return fileNames.size() - 1;
    }

    public int getFileSize() {
        return fileNames.size();
    }

    public String getFileName(int index) {
        return fileNames.get(index);
    }

    public ChangeEntityTree getChangeEntityTree(int index) {
        return treeLists.get(index);
    }

    public MiningActionData getMiningActionData(int index) {
        return changeEntityDataList.get(index);
    }

    public int getMinintActionDataSize() {
        return changeEntityDataList.size();
    }
}
