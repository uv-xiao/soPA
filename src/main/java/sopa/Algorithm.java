package sopa;

import soot.Local;
import soot.Unit;
import soot.Value;
import soot.jimple.*;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.ForwardFlowAnalysis;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.*;

public class Algorithm extends ForwardFlowAnalysis
{
    static Map<String, Set<String>> analysisResult= new HashMap<>();
    static Map<String, Integer> object2Line = new HashMap<>();
    private static int allocId = 0;


    public Algorithm(UnitGraph  graph){
        super(graph);
        doAnalysis();
    }

    static private void add2Result(String id, Set<String> set) {
        if (!analysisResult.containsKey(id)) {
            analysisResult.put(id, new HashSet<>());
        }
        analysisResult.get(id).addAll(set);
    }

    @Override
    protected void flowThrough(Object _inset, Object _unit, Object _outset) {
        HashMap<String,Set<String>> inset=(HashMap<String, Set<String>>) _inset;
        HashMap<String,Set<String>> outset=(HashMap<String, Set<String>>) _outset;
        Unit unit=(Unit)_unit;

        copy(inset,outset);

        System.out.print("Inset: ");
        printSet(inset);
        System.out.println("Unit " + unit);

        if(unit instanceof InvokeStmt){
            InvokeExpr expr=((InvokeStmt) unit).getInvokeExpr();
            if(expr.getMethod().toString().equals("<benchmark.internal.BenchmarkN: void alloc(int)>")){
                allocId = ((IntConstant)expr.getArg(0)).value;
            }
            else if(expr.getMethod().toString().equals("<benchmark.internal.BenchmarkN: void test(int,java.lang.Object)>")){
                int qid=((IntConstant)expr.getArg(0)).value;
                Value var=expr.getArg(1);
                Set<String> result = inset.get(((Local)var).getName());
                add2Result(Integer.toString(qid), result);
            }
            else if (expr instanceof SpecialInvokeExpr) {
                SpecialInvokeExpr construct = (SpecialInvokeExpr) expr;
                String base = ((Local)construct.getBase()).getName();
                System.out.println(construct.getBase().getType().toString());
                if (construct.getBase().getType().toString().equals("benchmark.objects.A")) {
                    Set<String> set = new HashSet<>();
                    if (construct.getArgs().size() == 1) {
                       Value rhs = construct.getArg(0);
                       if (rhs instanceof Local) {
                           String name = ((Local) rhs).getName();
                           if (inset.containsKey(name))
                               set.addAll(inset.get(name));
                           outset.put(base+".f", set);
                       }
                    }
                }
            }
        }
        else if (unit instanceof DefinitionStmt){
            DefinitionStmt stmt = (DefinitionStmt) unit;
            Value rhs = stmt.getRightOp();
            Value lhs = stmt.getLeftOp();

            Set<String> set = new HashSet<>();

            if (rhs instanceof NewExpr){
                String name = ((Local)lhs).getName();
                set.add(name);
                object2Line.put(name, allocId);
                allocId = 0;
            }
            else if (rhs instanceof Local){
                String rName=((Local)rhs).getName();
                set.addAll(inset.get(rName));
            }
            else if (rhs instanceof InstanceFieldRef) {
                InstanceFieldRef fieldRef = (InstanceFieldRef) rhs;
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
//            else if (rhs instanceof ThisRef) {
//                if (inset.containsKey("%this")) {
//                   set = new HashSet<>(inset.get("%this"));
//                }
//            }
            else if (rhs instanceof ParameterRef) {
                String name = "%" + ((ParameterRef) rhs).getIndex();
                if (inset.containsKey(name)) {
                    set.addAll(inset.get(name));
                }
            }
            else if (rhs instanceof NullConstant) {
            }
            else {
                System.err.println("Meet unknown rhs");
                // TODO: throw exception
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
            }
        }
        else if(unit instanceof ReturnStmt || unit instanceof ReturnVoidStmt){

        }
        else{

        }

        System.out.print("Outset: ");
        printSet(outset);
        System.out.println();
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
        HashMap<String,HashSet<String>> ret = new HashMap<>();
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

    public void print(){
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
