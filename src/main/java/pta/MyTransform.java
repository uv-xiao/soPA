package pta;

import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.TreeSet;

import soot.Local;
import soot.MethodOrMethodContext;
import soot.Scene;
import soot.SceneTransformer;
import soot.SootMethod;
import soot.SootClass;
import soot.Unit;
import soot.Value;
import soot.jimple.DefinitionStmt;
import soot.jimple.IntConstant;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.NewExpr;
import soot.jimple.toolkits.callgraph.ReachableMethods;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.UnitGraph;
import soot.util.queue.QueueReader;

public class MyTransform extends SceneTransformer {

    @Override
    protected void internalTransform(String arg0, Map<String, String> arg1) {


        SootMethod m =  Scene.v().getMainMethod();

        UnitGraph mgraph=new ExceptionalUnitGraph(m.getActiveBody());
        PointerAnalysis x=new PointerAnalysis(mgraph);
        x.print();

    }

}
