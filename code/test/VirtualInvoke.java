package test;

import benchmark.internal.BenchmarkN;
import benchmark.objects.A;
import benchmark.objects.B;
import benchmark.objects.H;
import benchmark.objects.I;

/*
 * @testcase FieldSensitivity2
 *
 * @version 1.0
 *
 * @author Johannes Späth, Nguyen Quang Do Lisa (Secure Software Engineering Group, Fraunhofer
 * Institute SIT)
 *
 * @description Field Sensitivity without static method
 */
public class VirtualInvoke {

  A x;

  public VirtualInvoke() {
    x=new A();
  }



  public static void main(String[] args) {

    BenchmarkN.alloc(1);
    A a=new A();
    BenchmarkN.alloc(2);
    I h=new H();
    A c=h.foo(a);

    BenchmarkN.test(1, c);
  }

}
