package sopa;

import java.util.Map;

import org.jf.dexlib2.iface.ExceptionHandler;
import soot.*;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.UnitGraph;

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
        UnitGraph mgraph = new ExceptionalUnitGraph(m.getActiveBody());
        try {
            Algorithm x = new Algorithm(mgraph);
            x.print();
        }
        catch (Exception e) {
            PessiAlg handler = new PessiAlg(Scene.v().getMainClass());
            handler.print();
        }

    }

}
