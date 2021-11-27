package sopa;

import jdk.nashorn.internal.runtime.AllocationStrategy;
import soot.Local;
import soot.SootMethod;
import soot.Unit;
import soot.Value;
import soot.jimple.*;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.ForwardFlowAnalysis;

import javax.swing.plaf.synth.SynthDesktopIconUI;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.*;

public class Algorithm extends ForwardFlowAnalysis
{
    static Integer DEBUG = 1;
    static Map<String, Set<String>> analysisResult= new HashMap<>();
    static Map<String, Integer> object2Line = new HashMap<>();
    private static int allocId = 0;
    private static int noAllocID = 0;
    static Set<String>callstack = new HashSet<>();
    Map<String, Set<String>> entrySet;
    Set<String> returnSet = new HashSet<>();

    public Algorithm(UnitGraph graph){
        super(graph);
        entrySet =new HashMap<>();
        doAnalysis();
    }

    public Algorithm(UnitGraph graph, Map<String, Set<String>>_entry){
        super(graph);
        entrySet =_entry;
        doAnalysis();
    }

    public Map<String, Set<String>> getExitSet(){
        Map<String,Set<String>>set = new HashMap<>();
        List<Unit> tails=graph.getTails();
        for(Unit tail:tails){
            set.putAll((Map<String, Set<String>>) getFlowAfter(tail));
        }
        return set;
    }

    static private void add2Result(String id, Set<String> set) {
        if (!analysisResult.containsKey(id)) {
            analysisResult.put(id, new HashSet<>());
        }
        analysisResult.get(id).addAll(set);
    }

    Set<String> getValueSet(Value val,Map<String,Set<String>>inset){
        Set<String> set = new HashSet<>();
        if (val instanceof Local){
            String rName=((Local)val).getName();
            set.addAll(inset.get(rName));
        }
        else if (val instanceof InstanceFieldRef) {
            InstanceFieldRef fieldRef = (InstanceFieldRef) val;
            String base = ((Local)fieldRef.getBase()).getName();
            String field = fieldRef.getField().getName();
            Set<String> basePointsTo = inset.get(base);
            for (String pointsTo : basePointsTo) {
                String pointsToName = pointsTo + "." + field;
                if (inset.containsKey(pointsToName)) {
                    set.addAll(inset.get(pointsToName));
                }
            }
        }
        else if (val instanceof ParameterRef) {
            String name = "%" + ((ParameterRef) val).getIndex();
            if (inset.containsKey(name)) {
                set.addAll(inset.get(name));
            }
        }
        return set;
    }

    private void enterInvoke(InstanceInvokeExpr expr,Map<String,Set<String>>inset,Map<String,Set<String>>outset,Set<String> ret){
        SootMethod method=expr.getMethod();
        Value base=expr.getBase();
        Set<String> basePointTo=getValueSet(base,inset);
        // Debug
        if (DEBUG > 0)
            System.out.println("call to " + method.toString());

        if(callstack.contains(method.toString())){
            System.err.println("exist recursive function");
            throw new RuntimeException("exist recursive function");
        }
        UnitGraph ugraph=new ExceptionalUnitGraph(method.getActiveBody());
        Map<String,Set<String>> newentryset=new HashMap<>();

        for(Map.Entry<String,Set<String>> entry:inset.entrySet()) {
            String name = entry.getKey();
            Set<String> pos = entry.getValue();
            if (name.contains(".")) {
                newentryset.put(name, pos);
            }
        }

        List<Value> args= expr.getArgs();
        int id=0;
        for(Value arg: args) {
            Set<String> set = getValueSet(arg, inset);
            newentryset.put("%" + id, set);
            id += 1;
        }
        newentryset.put("%this",basePointTo);
        callstack.add(method.toString());
        Algorithm callee=new Algorithm(ugraph,newentryset);
        callstack.remove(method.toString());
        Map<String,Set<String>> set=callee.getExitSet();
        for(Map.Entry<String,Set<String>> entry:set.entrySet()) {
            String name = entry.getKey();
            Set<String> pos = entry.getValue();
            if (name.contains(".")) {
                outset.put(name, pos);
            }
        }
        if(ret != null) {
            ret.addAll(callee.returnSet);
        }
    }

    @Override
    protected void flowThrough(Object _inset, Object _unit, Object _outset) {
        HashMap<String,Set<String>> inset=(HashMap<String, Set<String>>) _inset;
        HashMap<String,Set<String>> outset=(HashMap<String, Set<String>>) _outset;
        Unit unit=(Unit)_unit;

        copy(inset,outset);

        // Debug
        if (DEBUG > 0) {
            System.out.print("Inset: ");
            printSet(inset);
            System.out.println("Unit " + unit);
        }

        if(unit instanceof InvokeStmt){
            InvokeExpr expr=((InvokeStmt) unit).getInvokeExpr();
            if(expr.getMethod().toString().contains("<benchmark.internal") && expr.getMethod().toString().contains("void alloc(int)>")){
                allocId = ((IntConstant)expr.getArg(0)).value;
            }
            else if(expr.getMethod().toString().contains("<benchmark.internal") && expr.getMethod().toString().contains("void test(int,java.lang.Object)>")){
                int qid=((IntConstant)expr.getArg(0)).value;
                Value var=expr.getArg(1);
                Set<String> result = inset.get(((Local)var).getName());
                add2Result(Integer.toString(qid), result);
            }
            else if (expr instanceof SpecialInvokeExpr) {
                SpecialInvokeExpr construct = (SpecialInvokeExpr) expr;
                Set<String> basePointTo = inset.get(((Local)construct.getBase()).getName());
                System.out.println(construct.getBase().getType().toString());
                // TODO: support general cases
                if (construct.getBase().getType().toString().equals("benchmark.objects.A")) { //处理构造函数
                    Set<String> set = new HashSet<>();
                    if (construct.getArgs().size() == 1) {
                       Value rhs = construct.getArg(0);
                       if (rhs instanceof Local) {
                           String name = ((Local) rhs).getName();
                           if (inset.containsKey(name))
                               set.addAll(inset.get(name));
                           for(String pointTo:basePointTo)
                               outset.put(pointTo+".f", set);
                       }
                    }
                }
                else{
                    enterInvoke((InstanceInvokeExpr)expr,inset,outset,null);
                }
            }
            else if (expr instanceof InstanceInvokeExpr){ //处理函数调用
                enterInvoke((InstanceInvokeExpr)expr,inset,outset,null);
            }
        }
        else if (unit instanceof DefinitionStmt){
            DefinitionStmt stmt = (DefinitionStmt) unit;
            Value rhs = stmt.getRightOp();
            Value lhs = stmt.getLeftOp();

            Set<String> set = new HashSet<>();
            // Debug
            if (DEBUG > 1)
                System.out.println("rhs: "+rhs.getClass());
            //
            if (rhs instanceof NewExpr){
                String name = ((Local)lhs).getName();
                String pointTo=new String();

                if(allocId != 0)
                    pointTo=""+allocId;
                else {
                    pointTo = "" + (--noAllocID);
                }
                set.add(pointTo);
                object2Line.put(pointTo, allocId);
                allocId = 0;
            }
            else if (rhs instanceof Local){
                String rName=((Local)rhs).getName();
                // Debug
                if (DEBUG > 1)
                    System.out.println("Local: "+rName);
                //
                set.addAll(inset.get(rName));
            }
            else if (rhs instanceof InstanceFieldRef) {
                InstanceFieldRef fieldRef = (InstanceFieldRef) rhs;
                String base = ((Local)fieldRef.getBase()).getName();
                // Debug
                if (DEBUG > 1)
                    System.out.println("basename: "+base);
                String field = fieldRef.getField().getName();
                Set<String> basePointsTo = inset.get(base);
                for (String pointsTo : basePointsTo) {
                    String pointsToName = pointsTo + "." + field;
                    if (inset.containsKey(pointsToName)) {
                        set.addAll(inset.get(pointsToName));
                    }
                }
            }
            else if (rhs instanceof ParameterRef) {
                String name = "%" + ((ParameterRef) rhs).getIndex();
                if (inset.containsKey(name)) {
                    set.addAll(inset.get(name));
                }
            }
            else if (rhs instanceof NullConstant) {
            }
            else if(rhs instanceof LengthExpr){

            }
            else if(rhs instanceof ThisRef){
                String name="%this";
                if (inset.containsKey(name)) {
                    set.addAll(inset.get(name));
                }
            }
            else if(rhs instanceof InvokeExpr){
                enterInvoke((InstanceInvokeExpr) rhs,inset,outset,set);
            }
            else {
                System.err.println("Meet unknown rhs");
                // TODO: throw exception
                throw new RuntimeException("Unknown rhs");
            }

            if (lhs instanceof Local) {
               String name = ((Local) lhs).getName();
               outset.put(name, set);
            }
            else if (lhs instanceof InstanceFieldRef) {
                InstanceFieldRef fieldRef = (InstanceFieldRef) lhs;
                String base = ((Local)fieldRef.getBase()).getName();
                String field = fieldRef.getField().getName();
                Set<String> basePointsTo = inset.get(base);
                for (String pointsTo : basePointsTo) {
                    String pointsToName =  pointsTo + "." + field;
                    if (basePointsTo.size() == 1) {
                        outset.put(pointsToName, set);
                    } else {
                        if (!outset.containsKey(pointsToName))
                            outset.put(pointsToName, new HashSet<>());
                        outset.get(pointsToName).addAll(set);
                    }
                }
            }
            else {
                System.err.println("Meet unknown lhs");
                // TODO: throw exception
                throw new RuntimeException("Unknown lhs");
            }
        }
        else if(unit instanceof ReturnStmt){
            // TODO: process function return
            Value x=((ReturnStmt)unit).getOp();
            //debug
            System.out.println("returnOP: "+x.toString());
            //
            returnSet.addAll(getValueSet(x,inset));
        }
        else if(unit instanceof ReturnVoidStmt){

        }
        else{
            // TODO: process unsupported stmt
            throw new RuntimeException("Unknown unit");

        }

        // Debug
        if (DEBUG > 1) {
            System.out.println("object2Line: " + object2Line);
            System.out.print("Outset: ");
            printSet(outset);
            System.out.println();
        }
    }

    private void printSet(HashMap<String, Set<String>> inset) {
        List<String> keys = new ArrayList<>(inset.keySet());
        System.out.print("{");
        for (String key : keys) {
            System.out.print("(" + key + ", ");
            Set<String> possible = inset.get(key);
            System.out.print(possible.toString() + ") ");
        }
        System.out.print("}\n");
    }

    @Override
    protected Object newInitialFlow() {
        HashMap<String,HashSet<String>> ret = new HashMap<>();
        return ret;
    }
    @Override
    protected Object entryInitialFlow(){
        Map<String,Set<String>> ret = entrySet;
        return ret;
    }

    @Override
    protected void merge(Object _inset1, Object _inset2, Object _outset) {
        HashMap<String, Set<Integer>>inset1=(HashMap<String, Set<Integer>>)_inset1;
        HashMap<String, Set<Integer>>inset2=(HashMap<String, Set<Integer>>)_inset2;
        HashMap<String, Set<Integer>>outset=new HashMap<>();

        Set<String> k1=inset1.keySet();
        Set<String> k2=inset2.keySet();
        Set<String> outkey=new HashSet<>();
        outkey.addAll(k1);
        outkey.addAll(k2);
        for(String s:outkey){
            Set<Integer>val=new HashSet<>();
            Set<Integer> x= inset1.get(s);
            if(x != null){
                val.addAll(x);
            }
            x= inset2.get(s);
            if(x != null){
                val.addAll(x);
            }
            outset.put(s,val);
        }
        HashMap<String,Set<Integer>>out=(HashMap<String, Set<Integer>>)_outset;
        out.clear();
        out.putAll(outset);
    }

    @Override
    protected void copy(Object _inset, Object _outset) {
        HashMap<String, Set<Integer>>inset=(HashMap<String, Set<Integer>>)_inset;
        HashMap<String, Set<Integer>>outset=(HashMap<String, Set<Integer>>)_outset;
        outset.clear();
        for(Map.Entry<String,Set<Integer>> x:inset.entrySet()){
            outset.put(x.getKey(),new HashSet<>(x.getValue()));
        }
    }

    public void print() throws Exception {
//        if (analysisResult.size() > 0)
//            throw new Exception();

        try {
            PrintStream ps = new PrintStream(
                    new FileOutputStream("result.txt"));
            List<String> keyset = new ArrayList<>(analysisResult.keySet());
            keyset.sort(new Comparator<String>() {
                @Override
                public int compare(String o1, String o2) {
                    if (isNumber(o1) && isNumber(o2))
                        return new Integer(o1).compareTo(new Integer(o2));
                    else
                        return o1.compareTo(o2);
                }

                private boolean isNumber(String o1) {
                    return o1.matches("^-?\\d+$");
                }
            });
            StringBuilder answer = new StringBuilder();

            for (String key : keyset) {
                answer.append(key).append(":");
                List<String> pointsTo = new ArrayList<>(analysisResult.get(key));
                for (String pt : pointsTo) {
                    answer.append(" ").append(object2Line.get(pt));
                }
                answer.append("\n");
            }
            ps.print(answer);
            ps.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
