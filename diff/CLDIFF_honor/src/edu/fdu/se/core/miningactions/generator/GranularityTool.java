package edu.fdu.se.core.miningactions.generator;

import edu.fdu.se.global.Constants;
import edu.fdu.se.global.Global;
import edu.fdu.se.lang.java.LookupTableJavaDT;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class GranularityTool {

    public static List<Integer> getCommonRootNodeList(String rootNodeGranulatiry) {
        List<Integer> commonRootNodeList = new ArrayList<>();
        int[] types = Global.iLookupTbl.astNodeMap.get("types");
        int[] delcarations = Global.iLookupTbl.astNodeMap.get("declarations");
        int[] statements = Global.iLookupTbl.astNodeMap.get("statements");
        int[] expressions = Global.iLookupTbl.astNodeMap.get("expressions");
        Integer[] itypes = IntStream.of(types).boxed().toList().toArray(new Integer[0]);
        Integer[] ideclarations = IntStream.of(delcarations).boxed().toList().toArray(new Integer[0]);
        Integer[] istatements = IntStream.of(statements).boxed().toList().toArray(new Integer[0]);
        Integer[] iexpressions = IntStream.of(expressions).boxed().toList().toArray(new Integer[0]);

        if (Constants.GRANULARITY.TYPE.equals(rootNodeGranulatiry)) {
            commonRootNodeList.addAll(Arrays.asList(itypes));
        } else if (Constants.GRANULARITY.DECLARATION.equals(rootNodeGranulatiry)) {
            commonRootNodeList.addAll(Arrays.asList(itypes));
            commonRootNodeList.addAll(Arrays.asList(ideclarations));

        } else if (Constants.GRANULARITY.STATEMENT.equals(rootNodeGranulatiry)) {
            commonRootNodeList.addAll(Arrays.asList(itypes));
            commonRootNodeList.addAll(Arrays.asList(ideclarations));
            commonRootNodeList.addAll(Arrays.asList(istatements));

        } else if (Constants.GRANULARITY.EXPRESSION.equals(rootNodeGranulatiry)) {
            commonRootNodeList.addAll(Arrays.asList(itypes));
            commonRootNodeList.addAll(Arrays.asList(ideclarations));
            commonRootNodeList.addAll(Arrays.asList(istatements));
            commonRootNodeList.addAll(Arrays.asList(iexpressions));
        }
        return commonRootNodeList;
    }
}
