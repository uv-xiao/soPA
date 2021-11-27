package test;

import benchmark.internal.Benchmark;

/*
 * @testcase Recursion1
 *
 * @version 1.0
 *
 * @author Johannes Späth, Nguyen Quang Do Lisa (Secure Software Engineering Group, Fraunhofer
 * Institute SIT)
 *
 * @description The analysis must support recursion
 */
public class Recursion {

    public Recursion() {}

    public class N {
        public String value;
        public N next;

        public N(String value) {
            this.value = value;
            next = null;
        }
    }

    public N recursive(int i, N m) {
        if (i < 10) {
            int j = i + 1;
            return recursive(j, m.next);
        }
        return m;
    }

    public void test() {
        Benchmark.alloc(1);
        N node = new N("");

        Recursion r1 = new Recursion();
        N n = r1.recursive(0, node);

        N o = node.next;
        N p = node.next.next;
        N q = node.next.next.next;

        Benchmark.test(1, o);
        Benchmark.test(2, p);
        Benchmark.test(3, q);
    }

    public static void main(String[] args) {
        Recursion r1 = new Recursion();
        r1.test();
    }
}