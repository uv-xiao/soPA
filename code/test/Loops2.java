package basic;

import benchmark.internal.Benchmark;

/*
 * @testcase Loops2
 * 
 * @version 1.0
 * 
 * @author Johannes Späth, Nguyen Quang Do Lisa (Secure Software Engineering Group, Fraunhofer
 * Institute SIT)
 * 
 * @description The analysis must support loop constructs. Allocation site in N
 */
public class Loops2 {

  public class N {
    public String value = "";
    public N next;

    public N() {
      Benchmark.alloc(2);
      next = new N();
    }
  }

  private void test() {
    Benchmark.alloc(1);
    N node = new N();

    int i = 0;
    while (i < 10) {
      node = node.next;
      i++;
    }

    N o = node.next;
    N p = node.next.next;
    Benchmark
            .test(1,node);
    Benchmark
            .test(2,o);
    Benchmark
            .test(3,p);
  }

  public static void main(String[] args) {
    Loops2 l1 = new Loops2();
    l1.test();
  }
}
