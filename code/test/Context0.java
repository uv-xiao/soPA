package test;

import benchmark.internal.Benchmark;
import benchmark.objects.A;

/*
 * @testcase ContextSensitivity3
 *
 * @version 1.0
 *
 * @author Johannes Späth, Nguyen Quang Do Lisa (Secure Software Engineering Group, Fraunhofer
 * Institute SIT)
 *
 * @description Object sensitive alias from caller object (3-CS)
 */
public class Context0 {

    public Context0() {}

    public void callee(A a, A b) {
        Benchmark.test(1, a);
        Benchmark.test(2, b);
    }

    public void test1() {
        Benchmark.alloc(1);
        A a1 = new A();
        A b1 = a1;
        test11(a1, b1);
    }

    private void test11(A a1, A b1) {
        test111(a1, b1);
    }

    private void test111(A a1, A b1) {
        callee(a1, b1);
    }

    public void test2() {
        A a2 = new A();
        Benchmark.alloc(2);
        A b2 = new A();
        test22(a2, b2);
    }

    private void test22(A a2, A b2) {
        test222(a2, b2);
    }

    private void test222(A a2, A b2) {
        callee(a2, b2);
    }

    public static void main(String[] args) {
        Context0 cs1 = new Context0();
        cs1.test1();
        cs1.test2();
    }
}
