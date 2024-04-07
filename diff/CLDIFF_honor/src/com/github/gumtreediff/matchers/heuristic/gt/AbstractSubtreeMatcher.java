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

package com.github.gumtreediff.matchers.heuristic.gt;

import com.github.gumtreediff.matchers.*;
import com.github.gumtreediff.matchers.MappingStore;
import com.github.gumtreediff.matchers.MultiMappingStore;
import com.github.gumtreediff.tree.ITree;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public abstract class AbstractSubtreeMatcher extends Matcher {

    public static int MIN_HEIGHT = Integer.parseInt(System.getProperty("gt.stm.mh", "1"));

    public AbstractSubtreeMatcher(ITree src, ITree dst, MappingStore store) {
        super(src, dst, store);
    }

    private void popLarger(PriorityTreeList srcTrees, PriorityTreeList dstTrees) {
        if (srcTrees.peekHeight() > dstTrees.peekHeight())
            srcTrees.open();
        else
            dstTrees.open();
    }

    /**
     * 这个方法的流程是：
     * 1. 从src和dst的根节点开始，将根节点的所有子节点放到srcTrees和dstTrees中去，这两个变量是PriorityTreeList类型的，里面存放的是List<ITree>类型的数据
     * 2. 从srcTrees和dstTrees中取出高度最高的节点，然后进行比较，如果是同构的，就将这两个节点放到multiMappings中去，然后将这两个节点的父节点放到srcTrees和dstTrees中去
     * 3. srcTrees和dstTrees中的节点高度不一样，然后将高度最高的节点放到srcTrees和dstTrees中去
     * 4. 重复2，直到srcTrees和dstTrees中的节点高度都为-1
     * 5. 将multiMappings中的节点进行过滤，然后放到store中去
     * 6. 重复1-5，直到src和dst的所有节点都被遍历完
     * 7. 返回store
     */
    public void match() {
        MultiMappingStore multiMappings = new MultiMappingStore();

        PriorityTreeList srcTrees = new PriorityTreeList(src);
        PriorityTreeList dstTrees = new PriorityTreeList(dst);

        while (srcTrees.peekHeight() != -1 && dstTrees.peekHeight() != -1) {
            while (srcTrees.peekHeight() != dstTrees.peekHeight())
                popLarger(srcTrees, dstTrees);
            //这里的currentHeightSrcTrees和currentHeightDstTrees的作用是存放srcTrees和dstTrees中高度最高的节点，
            //用list是因为有可能多个节点的高度都是最高的
            List<ITree> currentHeightSrcTrees = srcTrees.pop();
            List<ITree> currentHeightDstTrees = dstTrees.pop();

            //这里的marksForSrcTrees和marksForDstTrees的作用是标记currentHeightSrcTrees和currentHeightDstTrees中的节点是否已经被匹配过了
            boolean[] marksForSrcTrees = new boolean[currentHeightSrcTrees.size()];
            boolean[] marksForDstTrees = new boolean[currentHeightDstTrees.size()];

            for (int i = 0; i < currentHeightSrcTrees.size(); i++) {
                for (int j = 0; j < currentHeightDstTrees.size(); j++) {
                    ITree src = currentHeightSrcTrees.get(i);
                    ITree dst = currentHeightDstTrees.get(j);

                    if (src.isIsomorphicTo(dst)) {
                        multiMappings.link(src, dst);
                        marksForSrcTrees[i] = true;
                        marksForDstTrees[j] = true;
                    }
                }
            }

            for (int i = 0; i < marksForSrcTrees.length; i++)
                //如果节点没有被匹配过，就将这个节点的所有子节点放到srcTrees中去
                if (!marksForSrcTrees[i])
                    srcTrees.open(currentHeightSrcTrees.get(i));
            for (int j = 0; j < marksForDstTrees.length; j++)
                if (!marksForDstTrees[j])
                    dstTrees.open(currentHeightDstTrees.get(j));
            srcTrees.updateHeight();
            dstTrees.updateHeight();
        }
        //将multiMappings中的节点进行过滤，然后放到store中去
        filterMappings(multiMappings);
    }

    public abstract void filterMappings(MultiMappingStore multiMappings);

    protected double sim(ITree src, ITree dst) {
        double jaccard = jaccardSimilarity(src.getParent(), dst.getParent());
        int posSrc = (src.isRoot()) ? 0 : src.getParent().getChildPosition(src);
        int posDst = (dst.isRoot()) ? 0 : dst.getParent().getChildPosition(dst);
        int maxSrcPos =  (src.isRoot()) ? 1 : src.getParent().getChildren().size();
        int maxDstPos =  (dst.isRoot()) ? 1 : dst.getParent().getChildren().size();
        int maxPosDiff = Math.max(maxSrcPos, maxDstPos);
        double pos = 1D - ((double) Math.abs(posSrc - posDst) / (double) maxPosDiff);
        double po = 1D - ((double) Math.abs(src.getId() - dst.getId()) / (double) this.getMaxTreeSize());
        return 100 * jaccard + 10 * pos + po;
    }

    protected int getMaxTreeSize() {
        return Math.max(src.getSize(), dst.getSize());
    }

    protected void retainBestMapping(List<Mapping> mappings, Set<ITree> srcIgnored, Set<ITree> dstIgnored) {
        while (mappings.size() > 0) {
            Mapping mapping = mappings.remove(0);
            if (!(srcIgnored.contains(mapping.getFirst()) || dstIgnored.contains(mapping.getSecond()))) {
                addMappingRecursively(mapping.getFirst(), mapping.getSecond());
                srcIgnored.add(mapping.getFirst());
                dstIgnored.add(mapping.getSecond());
            }
        }
    }

    private static class PriorityTreeList {

        private List<ITree>[] trees;

        private int maxHeight;

        private int currentIdx;

        @SuppressWarnings("unchecked")
        public PriorityTreeList(ITree tree) {
            int listSize = tree.getHeight() - MIN_HEIGHT + 1;
            if (listSize < 0)
                listSize = 0;
            if (listSize == 0)
                currentIdx = -1;
            trees = (List<ITree>[]) new ArrayList[listSize];
            maxHeight = tree.getHeight();
            addTree(tree);
        }

        private int idx(ITree tree) {
            return idx(tree.getHeight());
        }

        private int idx(int height) {
            return maxHeight - height;
        }

        private int height(int idx) {
            return maxHeight - idx;
        }

        private void addTree(ITree tree) {
            if (tree.getHeight() >= MIN_HEIGHT) {
                int idx = idx(tree);
                if (trees[idx] == null) trees[idx] = new ArrayList<>();
                trees[idx].add(tree);
            }
        }

        /**
         * 将currentIdx指向的List<ITree>中的第一个元素弹出来，然后将这个元素的所有子节点放到trees中去，然后更新currentIdx
         */
        public List<ITree> open() {
            List<ITree> pop = pop();
            if (pop != null) {
                for (ITree tree: pop) open(tree);
                updateHeight();
                return pop;
            } else return null;
        }

        public List<ITree> pop() {
            if (currentIdx == -1)
                return null;
            else {
                List<ITree> pop = trees[currentIdx];
                trees[currentIdx] = null;
                return pop;
            }
        }

        public void open(ITree tree) {
            for (ITree c: tree.getChildren()) addTree(c);
        }

        public int peekHeight() {
            return (currentIdx == -1) ? -1 : height(currentIdx);
        }

        public void updateHeight() {
            currentIdx = -1;
            for (int i = 0; i < trees.length; i++) {
                if (trees[i] != null) {
                    currentIdx = i;
                    break;
                }
            }
        }
    }
}
