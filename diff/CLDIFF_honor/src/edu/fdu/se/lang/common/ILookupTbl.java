package edu.fdu.se.lang.common;


import edu.fdu.se.global.Constants;
import edu.fdu.se.global.Global;
import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;

import java.lang.reflect.Method;
import java.util.*;

public abstract class ILookupTbl {

    public Map<String, Map<Integer, String>> callTable;
    public Map<String, int[]> astNodeMap;
    public Map<String, int[]> astDict;
    public Map<String, String> classNameToEntity;


    public ILookupTbl() {
        callTable = new HashMap<>();
        astDict = new HashMap<>();
        astNodeMap = new HashMap<>();
        classNameToEntity = new HashMap<>();
    }

    public void initTraverseWaysTbl() {
        Map<Integer, String> traverseTbl = new HashMap<>();
        Reflections reflections = new Reflections(Constants.TRAVERSEWAYS_PATH, new MethodAnnotationsScanner());
        reflections.getConfiguration();
        Set<Method> methods = reflections.getMethodsAnnotatedWith(MethodLookupTbl.class);
        for (Method m : methods) {
            MethodLookupTbl anno = m.getAnnotation(MethodLookupTbl.class);
            traverseTbl.put(anno.key(), m.getName());
        }
        callTable.put(Constants.TRAVERSE_WAYS, traverseTbl);
    }


    public void parseMatchClass() {
        Map<Integer, String> topDown = new HashMap<>();
        Map<Integer, String> bottomUpNew = new HashMap<>();
        Map<Integer, String> bottomUpCurr = new HashMap<>();
        List<Class<?>> clazzLis = new ArrayList<>();
        Reflections reflections = new Reflections(Constants.STATEMENTS_PATH);
        Set<Class<?>> classes = reflections.getTypesAnnotatedWith(ClassLookupTbl.class);
        clazzLis.addAll(classes);
        reflections = new Reflections(Constants.DECLARATIONS_PATH);
        classes = reflections.getTypesAnnotatedWith(ClassLookupTbl.class);
        clazzLis.addAll(classes);
        if (Constants.GRANULARITY.EXPRESSION.equals(Global.granularity)) {
            reflections = new Reflections(Constants.EXPRESSIONS_PATH);
            classes = reflections.getTypesAnnotatedWith(ClassLookupTbl.class);
            clazzLis.addAll(classes);
        }

        for (Class<?> cl : clazzLis) {
            ClassLookupTbl anno = cl.getAnnotation(ClassLookupTbl.class);
            classNameToEntity.put(cl.getSimpleName(), anno.key());
            Method[] method = cl.getMethods();
            for (Method m : method) {
                MethodLookupTbl methodLookupTbl = m.getAnnotation(MethodLookupTbl.class);
                if (methodLookupTbl == null) {
                    continue;
                }
                String methodName = m.getName();
                int ke = methodLookupTbl.key();
                if (methodName.contains(Constants.MATCH_METHOD_SUFFIX_TOP_DOWN)) {
                    topDown.put(ke, cl.getName() + "." + methodName);
                } else if (methodName.contains(Constants.MATCH_METHOD_SUFFIX_BOTTOM_UP_NEW)) {
                    bottomUpNew.put(ke, cl.getName() + "." + methodName);
                } else if (methodName.contains(Constants.MATCH_METHOD_SUFFIX_BOTTOM_UP_CURR)) {
                    bottomUpCurr.put(ke, cl.getName() + "." + methodName);
                }
            }
        }


        callTable.put(Constants.MATCH_TOP_DOWN, topDown);
        callTable.put(Constants.MATCH_BOTTOM_UP_NEW, bottomUpNew);
        callTable.put(Constants.MATCH_BOTTOM_UP_CURR, bottomUpCurr);
    }

    public abstract int getCompilationUnitTypeId();

    public abstract int getTypeDeclarationTypeId();

}
