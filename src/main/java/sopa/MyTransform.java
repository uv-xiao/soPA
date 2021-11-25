package sopa;

import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.TreeSet;

import soot.*;
import soot.jimple.DefinitionStmt;
import soot.jimple.IntConstant;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.NewExpr;
import soot.jimple.toolkits.callgraph.ReachableMethods;
import soot.toolkits.graph.BriefUnitGraph;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.UnitGraph;
import soot.util.queue.QueueReader;

public class MyTransform extends SceneTransformer {

    @Override
    protected void internalTransform(String arg0, Map<String, String> arg1) {

        System.out.println("Get Program Structure: \n++++++++++");
        SootClass mainClass = Scene.v().getMainClass();
        System.out.println("\tMain class is "+ mainClass);
        for (SootMethod m: mainClass.getMethods()) {
            System.out.println("\t\tMethod "+ m);
        }
        for (SootField f: mainClass.getFields()) {
            System.out.println("\t\tField "+f);
        }
        SootMethod mainMethod = Scene.v().getMainMethod();
        System.out.println("\tMain method is "+ mainMethod);
        for (Unit u: mainMethod.getActiveBody().getUnits()) {
            System.out.println("\t\tUnit "+u);
        }
        System.out.println("\tSuper class is "+mainClass.getSuperclass());
        System.out.println("++++++++++");



        SootMethod m =  Scene.v().getMainMethod();
        UnitGraph mgraph=new ExceptionalUnitGraph(m.getActiveBody());
        PointerAnalysis x=new PointerAnalysis(mgraph);
        x.print();

    }

}
