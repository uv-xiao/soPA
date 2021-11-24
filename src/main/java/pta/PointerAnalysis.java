package pta;

import fj.Hash;
import soot.*;
import soot.jimple.*;
import soot.jimple.internal.JReturnStmt;
import soot.jimple.spark.ondemand.genericutil.Stack;
import soot.jimple.toolkits.typing.fast.QueuedSet;
import soot.tagkit.Tag;
import soot.toolkits.graph.DirectedGraph;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.ArraySparseSet;
import soot.toolkits.scalar.FlowSet;
import soot.toolkits.scalar.ForwardFlowAnalysis;
import soot.toolkits.scalar.Pair;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.*;

public class PointerAnalysis extends ForwardFlowAnalysis
{
    int Allocid;
    Set<Pair<Integer,Local>>Query;
    public PointerAnalysis(UnitGraph  graph){
        super(graph);
        Allocid=0;
        Query=new HashSet<>();
        doAnalysis();

    }
    @Override
    protected void flowThrough(Object _inset, Object _unit, Object _outset) {
        HashMap<String,HashSet<Integer>> inset=(HashMap<String, HashSet<Integer>>) _inset;
        Unit unit=(Unit)_unit;
        HashMap<String,HashSet<Integer>> outset=(HashMap<String, HashSet<Integer>>) _outset;
        copy(inset,outset);
        if(unit instanceof InvokeStmt){
            //int Allocid=0;
            InvokeExpr expr=((InvokeStmt) unit).getInvokeExpr();
            if(expr.getMethod().toString().equals("<benchmark.internal.BenchmarkN: void alloc(int)>")){
                int id=((IntConstant)expr.getArg(0)).value;
                Allocid=id;
            }
            else if(expr.getMethod().toString().equals("<benchmark.internal.BenchmarkN: void test(int,java.lang.Object)>")){
                int qid=((IntConstant)expr.getArg(0)).value;
                Value var=expr.getArg(1);
                Query.add(new Pair<>(new Integer(qid),(Local)var));
            }
        }
        else if(unit instanceof DefinitionStmt){
            System.out.println("This is Def: "+unit.toString());
            Value Rop=((DefinitionStmt) unit).getRightOp();
            Value Lop=((DefinitionStmt) unit).getLeftOp();
            if (Rop instanceof NewExpr){
                outset.put(((Local)Lop).getName(),new HashSet<>(Allocid));
            }
            else if(Rop instanceof Local){
                String lvar=((Local)Lop).getName();
                String rvar=((Local)Rop).getName();
                outset.put(lvar,new HashSet<>(outset.get(rvar)));
            }
        }
        else if(unit instanceof ReturnStmt || unit instanceof ReturnVoidStmt){

        }
        else{

        }
    }

    @Override
    protected Object newInitialFlow() {
        HashMap<String,HashSet<Integer>> ret = new HashMap<>();
        return ret;
    }
    @Override
    protected Object entryInitialFlow(){
        HashMap<String,HashSet<Integer>> ret = new HashMap<>();
        return ret;
    }

    @Override
    protected void merge(Object _inset1, Object _inset2, Object _outset) {
        HashMap<String, Set<Integer>>inset1=(HashMap<String, Set<Integer>>)_inset1;
        HashMap<String, Set<Integer>>inset2=(HashMap<String, Set<Integer>>)_inset2;
        HashMap<String, Set<Integer>>outset=(HashMap<String, Set<Integer>>)_outset;

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
        List<Unit> tail=graph.getTails();
        HashMap<String,HashSet<Integer>> result=new HashMap<>();
        for(Unit x:tail){
            merge(result,getFlowAfter(x),result);
        }
        try {
            PrintStream ps = new PrintStream(
                    new FileOutputStream(new File("result.txt")));
            for(Pair<Integer,Local> x: Query){
                Integer id = x.getO1();
                String var = x.getO2().toString();
                ps.print(id+":");
                Set<Integer> ans=result.get(var);
                for(Integer pos:ans)
                    ps.print(" "+pos);
                ps.println();
            }
            ps.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
