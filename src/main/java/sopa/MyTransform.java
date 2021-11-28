package sopa;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


import soot.util.Chain;
import soot.*;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.UnitGraph;

public class MyTransform extends SceneTransformer {


    public static Integer DEBUG=0;
    static public Map<String,SootClass> Classes = new HashMap<>();
    @Override
    protected void internalTransform(String arg0, Map<String, String> arg1) {


        if (DEBUG > 0) {
            try {
                System.out.println("Get Program Structure: \n++++++++++");
                PrintStream ps = new PrintStream(
                        new FileOutputStream("jimple.txt"));
                SootClass mainClass = Scene.v().getMainClass();
                ps.println("\tMain class is " + mainClass);
                for (SootMethod m : mainClass.getMethods()) {
                    ps.println("\t\tMethod " + m);
                    if (m.hasActiveBody())
                        for (Unit u : m.getActiveBody().getUnits()) {
                            ps.println("\t\t\tUnit " + u);
                        }
                }
                for (SootField f : mainClass.getFields()) {
                    ps.println("\t\tField " + f);
                }
                ps.println("\tSuper class is " + mainClass.getSuperclass());
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        SootMethod mainMethod = Scene.v().getMainMethod();
        Chain<SootClass> CClass=Scene.v().getClasses();
//        //
        try {

            Algorithm.starttime = System.nanoTime();
            Map<String, Set<String>> entrySet = new HashMap<>();
            for (SootClass cl : CClass) {
                Classes.put(cl.toString(), cl);
//                System.out.println(cl.toString());
//                System.out.println(cl.toString().split("\\.").length);
//                System.out.flush();
                if (cl.toString().split("\\.")[0].equals("test")) {
                    try {
                        SootMethod clinit = cl.getMethod("void <clinit>()");

                        UnitGraph mGraph = new ExceptionalUnitGraph(clinit.getActiveBody());
                        Algorithm clInitAnalysis = new Algorithm(mGraph, cl.toString());
                        entrySet.putAll((clInitAnalysis.getExitSet()));
                    } catch (Throwable e) {

                    }

                }
            }
//        //
            SootMethod m = Scene.v().getMainMethod();
            UnitGraph mgraph = new ExceptionalUnitGraph(m.getActiveBody());
            Algorithm x = new Algorithm(mgraph, mainMethod.getDeclaringClass().toString(), entrySet);
            x.print();
        }
        catch (Throwable e) {
            e.printStackTrace(System.err);
            System.err.println("print the soundest result");
            PessiAlg handler = new PessiAlg(Scene.v().getMainClass());
            handler.print();
        }

    }

}
