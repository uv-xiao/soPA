package test;

import benchmark.internal.BenchmarkN;
import benchmark.objects.A;
import benchmark.objects.B;

public class VirtualRecursive {
  interface I {
    I getField();
    void setField(I x, I y);
  }

  static class IA1 implements I {
    I fA1;
    IA1() {
      this.fA1 = null;
    }
    IA1(I fA1) {
      this.fA1 = fA1;
    }
    public void setField(I x, I y) {
      fA1 = f(x, y);
    }
    public I getField() {
      return fA1;
    }
  };

  static class IA2 implements I {
    I fA2;
    IA2() {
      this.fA2 = null;
    }
    IA2(I fA2) {
      this.fA2 = fA2;
    }
    public void setField(I x, I y) {
      fA2 = g(x, y);
    }
    public I getField() {
      return fA2;
    }
  }

  static <T> T f(T x, T y) {
    return x;
  }

  static <T> T g(T x, T y) {
    return y;
  }

  public static void main(String[] args) {
    BenchmarkN.alloc(1);
    I a = new IA1();
    BenchmarkN.alloc(2);
    I b = new IA2(a);
    I f = b.getField();
    BenchmarkN.test(1, f); // 1: 1
    BenchmarkN.alloc(3);
    I c = new IA2(a);
    a.setField(c, b);
    I fa = f.getField();
    BenchmarkN.test(2, fa); // 2: 3
    I d = c.getField().getField();
    d.setField(a, b);
    I fb = fa.getField();
    BenchmarkN.test(3, fb); // 3: 2
  }
}