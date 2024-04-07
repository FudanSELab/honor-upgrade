package edu.fdu.se.lang.common.preprocess;

import edu.fdu.se.core.preprocessingfile.data.PreCacheTmpData;

public interface TypeNodesTraversal {


    void traverseTypeDeclarationSetVisited(PreCacheTmpData compareCache, Object cod, String prefixClassName);
}
