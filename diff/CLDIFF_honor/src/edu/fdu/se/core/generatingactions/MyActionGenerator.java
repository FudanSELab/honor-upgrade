/*
 * This file is part of GumTree.
 *
 * GumTree is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * GumTree is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with GumTree.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright 2011-2015 Jean-Rémy Falleri <jr.falleri@gmail.com>
 * Copyright 2011-2015 Floréal Morandat <florealm@gmail.com>
 */

package edu.fdu.se.core.generatingactions;
/**
 * generate action and cluster
 */
import com.github.gumtreediff.actions.model.*;
import com.github.gumtreediff.matchers.Mapping;
import com.github.gumtreediff.matchers.MappingStore;
import com.github.gumtreediff.tree.AbstractTree;
import com.github.gumtreediff.tree.ITree;
import com.github.gumtreediff.tree.Tree;

import com.github.gumtreediff.tree.TreeUtils;
import edu.fdu.se.lang.common.generatingactions.ParserTreeGenerator;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MyActionGenerator {


    /**
     * pre根节点
     */
    private ITree origSrc;

    /**
     * pre树的副本
     */
    private ITree copySrc;

    /**
     * curr根节点
     */
    private ITree origDst;

    private MappingStore origMappings;

    /**
     * copySrcTrees中对应id的树和dm中的下一棵树建立映射关系
     */
    private MappingStore newMappings;

    private Set<ITree> dstInOrder;

    private Set<ITree> srcInOrder;

    private int lastId;


    private TIntObjectMap<ITree> origSrcTreesMap;

    /**
     * 记录copySrc中的树和id的映射关系
     */
    private TIntObjectMap<ITree> copySrcTreesMap;

    public MyActionGenerator(ITree src, ITree dst, MappingStore mappings) {
        this.origSrc = src;
        this.copySrc = this.origSrc.deepCopy();
        this.origDst = dst;

        origSrcTreesMap = new TIntObjectHashMap<>();
        for (ITree t: origSrc.getTrees())
            origSrcTreesMap.put(t.getId(), t);
        copySrcTreesMap = new TIntObjectHashMap<>();
        for (ITree t: copySrc.getTrees())
            copySrcTreesMap.put(t.getId(), t);

        origMappings = new MappingStore();
        for (Mapping m: mappings)
            this.origMappings.link(copySrcTreesMap.get(m.getFirst().getId()), m.getSecond());
        this.newMappings = origMappings.copy();
        myAgbData = new ActionsMap();
    }

    public MyActionGenerator(ParserTreeGenerator generator) {
        //pre根节点
        this.origSrc = generator.src;
        //copy
        this.copySrc = this.origSrc.deepCopy();
        this.origDst = generator.dst;

        origSrcTreesMap = new TIntObjectHashMap<>();
        //返回一个列表，其中包含按前序排序的每个子树和树
        for (ITree t: origSrc.getTrees())
            origSrcTreesMap.put(t.getId(), t);

        //用于将树的id映射到对象树
        copySrcTreesMap = new TIntObjectHashMap<>();
        for (ITree t: copySrc.getTrees())
            copySrcTreesMap.put(t.getId(), t);

        //建立存储列表前后的映射关系
        origMappings = new MappingStore();
        for (Mapping m: generator.mapping)
            //将copySrcTrees中对应id的树和dm中的下一棵树建立映射关系
            this.origMappings.link(copySrcTreesMap.get(m.getFirst().getId()), m.getSecond());
        this.newMappings = origMappings.copy();
        myAgbData = new ActionsMap();
    }


    public ActionsMap myAgbData;


    /**
     * 用于生成两棵树之间的差异操作序列。
     */
    public ActionsMap generate() {
        //设置虚拟根节点
        myAgbData = new ActionsMap();
        ITree srcFakeRoot = new AbstractTree.FakeTree(copySrc);
        ITree dstFakeRoot = new AbstractTree.FakeTree(origDst);
        copySrc.setParent(srcFakeRoot);
        origDst.setParent(dstFakeRoot);

        dstInOrder = new HashSet<>();
        srcInOrder = new HashSet<>();

        lastId = copySrc.getSize() + 1;
        //建立虚拟根节点的映射关系
        newMappings.link(srcFakeRoot, dstFakeRoot);
        //c or java
        List<ITree> bfsDst = TreeUtils.breadthFirst(origDst);
//        List<ITree> bfsDst = MyTreeUtil.layeredBreadthFirst(origDst, myAgbData.getDstLayerLastNodeIndex());
        for (int i=0;i<bfsDst.size();i++){
        	ITree dstItem = bfsDst.get(i);
            ITree mappedSrcNode;
            ITree parentOfDstNode = dstItem.getParent();
            ITree mappingSrcOfParentDst = newMappings.getSrc(parentOfDstNode);
            if (!newMappings.hasDst(dstItem)) {
            	//item is not in src
                int k = findPos(dstItem);
                // Insertion case : insert new node.
                mappedSrcNode = new AbstractTree.FakeTree();
                mappedSrcNode.setId(newId());
                // In order to use the real nodes from the second tree, we
                // furnish x instead of w and fake that x has the newly
                // generated ID.
                //为了使用第二棵树中的真实节点，我们提供x而不是w，并且假设x具有新生成的ID。
                // insert增加过程中，tree也在更新，mapping也在更新
                Action ins = new Insert(dstItem, origSrcTreesMap.get(mappingSrcOfParentDst.getId()), k);
                Tree tmp = (Tree) dstItem;
                tmp.setDoAction(ins);
                myAgbData.addAction(ins);
                origSrcTreesMap.put(mappedSrcNode.getId(), dstItem);
                newMappings.link(mappedSrcNode, dstItem);
                mappingSrcOfParentDst.getChildren().add(k, mappedSrcNode);
                mappedSrcNode.setParent(mappingSrcOfParentDst);
            } else {
            	//in 
            	// 有mapping
                mappedSrcNode = newMappings.getSrc(dstItem);
                if (!dstItem.equals(origDst)) { // TODO => x != origDst // Case of the root
                    ITree mappedSrcNodeParent = mappedSrcNode.getParent();
                    if (!mappedSrcNode.getLabel().equals(dstItem.getLabel())) {
                    	Update upd = new Update(origSrcTreesMap.get(mappedSrcNode.getId()), dstItem.getLabel());
                    	myAgbData.addAction(upd);
//                        Tree tmp = (Tree) dstItem;
                        ITree srcNode = origSrcTreesMap.get(mappedSrcNode.getId());
                        Tree srcTree = (Tree) srcNode;
                        srcTree.setDoAction(upd);

                        mappedSrcNode.setLabel(dstItem.getLabel());
                    }
                    if (!mappingSrcOfParentDst.equals(mappedSrcNodeParent)) {

                        int k = findPos(dstItem);
                        Action mv = new Move(origSrcTreesMap.get(mappedSrcNode.getId()), origSrcTreesMap.get(mappingSrcOfParentDst.getId()), k);
                        Tree tmp = (Tree) origSrcTreesMap.get(mappedSrcNode.getId());
                        tmp.setDoAction(mv);
                        myAgbData.addAction(mv);

                        int oldk = mappedSrcNode.positionInParent();
                        mappingSrcOfParentDst.getChildren().add(k, mappedSrcNode);
                        mappedSrcNode.getParent().getChildren().remove(oldk);
                        mappedSrcNode.setParent(mappingSrcOfParentDst);
                    }
                }
            }

            //FIXME not sure why :D
            srcInOrder.add(mappedSrcNode);
            dstInOrder.add(dstItem);
            alignChildren(mappedSrcNode, dstItem,i);
        }
        for(ITree w :copySrc.breadthFirst()){
            if (!newMappings.hasSrc(w)) {
            	Delete del = new Delete(origSrcTreesMap.get(w.getId()));
            	Tree tmp = (Tree) origSrcTreesMap.get(w.getId());
            	tmp.setDoAction(del);
        		myAgbData.addAction(del);
            	}
        }
        return myAgbData;
        //FIXME should ensure isomorphism.
    }

    private void alignChildren(ITree w, ITree x,int nodeIndex) {
        srcInOrder.removeAll(w.getChildren());
        dstInOrder.removeAll(x.getChildren());

        List<ITree> s1 = new ArrayList<>();
        for (ITree c: w.getChildren())
            if (newMappings.hasSrc(c))
                if (x.getChildren().contains(newMappings.getDst(c)))
                    s1.add(c);

        List<ITree> s2 = new ArrayList<>();
        for (ITree c: x.getChildren())
            if (newMappings.hasDst(c))
                if (w.getChildren().contains(newMappings.getSrc(c)))
                    s2.add(c);

        List<Mapping> lcs = lcs(s1, s2);

        for (Mapping m : lcs) {
            srcInOrder.add(m.getFirst());
            dstInOrder.add(m.getSecond());
        }

        for (ITree a : s1) {
            for (ITree b: s2 ) {
                if (origMappings.has(a, b)) {
                    if (!lcs.contains(new Mapping(a, b))) {
                        int k = findPos(b);
                        Action mv = new Move(origSrcTreesMap.get(a.getId()), origSrcTreesMap.get(w.getId()), k);
                        Tree tmp = (Tree) origSrcTreesMap.get(a.getId());
                        tmp.setDoAction(mv);
                        myAgbData.addAction(mv);
                        //System.out.println(mv);
                        int oldk = a.positionInParent();
                        w.getChildren().add(k, a);
                        if (k  < oldk ) // FIXME this is an ugly way to patch the index
                            oldk ++;
                        a.getParent().getChildren().remove(oldk);
                        a.setParent(w);
                        srcInOrder.add(a);
                        dstInOrder.add(b);
                    }
                }
            }
        }
    }

    private int findPos(ITree x) {
        ITree y = x.getParent();
        List<ITree> siblings = y.getChildren();

        for (ITree c : siblings) {
            if (dstInOrder.contains(c)) {
                if (c.equals(x)) return 0;
                else break;
            }
        }

        int xpos = x.positionInParent();
        ITree v = null;
        for (int i = 0; i < xpos; i++) {
            ITree c = siblings.get(i);
            if (dstInOrder.contains(c)) v = c;
        }

        //if (v == null) throw new RuntimeException("No rightmost sibling in order");
        if (v == null) return 0;

        ITree u = newMappings.getSrc(v);
        // siblings = u.getParent().getChildren();
        // int upos = siblings.indexOf(u);
        int upos = u.positionInParent();
        // int r = 0;
        // for (int i = 0; i <= upos; i++)
        // if (srcInOrder.contains(siblings.get(i))) r++;
        return upos + 1;
    }

    private int newId() {
        return ++lastId;
    }

    private List<Mapping> lcs(List<ITree> x, List<ITree> y) {
        int m = x.size();
        int n = y.size();
        List<Mapping> lcs = new ArrayList<>();

        int[][] opt = new int[m + 1][n + 1];
        for (int i = m - 1; i >= 0; i--) {
            for (int j = n - 1; j >= 0; j--) {
                if (newMappings.getSrc(y.get(j)).equals(x.get(i))) opt[i][j] = opt[i + 1][j + 1] + 1;
                else  opt[i][j] = Math.max(opt[i + 1][j], opt[i][j + 1]);
            }
        }

        int i = 0, j = 0;
        while (i < m && j < n) {
            if (newMappings.getSrc(y.get(j)).equals(x.get(i))) {
                lcs.add(new Mapping(x.get(i), y.get(j)));
                i++;
                j++;
            } else if (opt[i + 1][j] >= opt[i][j + 1]) i++;
            else j++;
        }

        return lcs;
    }

}
