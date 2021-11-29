package pta;

import soot.*;
import soot.jimple.IntConstant;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.*;

public class PessiAlg {
    private static Set<Integer> allocatedIds = new HashSet<Integer>();
    private static Set<Integer> queryIds = new HashSet<Integer>();

    public PessiAlg(SootClass mainClass) {
        for (SootMethod method : mainClass.getMethods()) {
            if (method.hasActiveBody())
            for (Unit unit : method.getActiveBody().getUnits()) {
                if (unit instanceof InvokeStmt) {
                    InvokeStmt stmt = (InvokeStmt) unit;
                    InvokeExpr expr = stmt.getInvokeExpr();
                    if(expr.getMethod().toString().contains("<benchmark.internal") && expr.getMethod().toString().contains("void alloc(int")) {
                        int allocId = ((IntConstant) expr.getArg(0)).value;
                        allocatedIds.add(new Integer(allocId));
                    }
                    else if(expr.getMethod().toString().contains("<benchmark.internal") && expr.getMethod().toString().contains("void test(int")){
                        int queryId = ((IntConstant) expr.getArg(0)).value;
                        queryIds.add(new Integer(queryId));
                    }
                }
            }
        }
    }

    public void print() {
        try {
            PrintStream ps = new PrintStream(
                    new FileOutputStream("result.txt"));
            List<Integer> ids = new ArrayList<>(queryIds);
            ids.sort(Integer::compareTo);
            StringBuilder sb = new StringBuilder();
            for (Integer id : ids) {
                sb.append(id).append(":");
                for (Integer pt : allocatedIds) {
                    sb.append(" ").append(pt);
                }
                sb.append("\n");
            }
            ps.print(sb);
            ps.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
