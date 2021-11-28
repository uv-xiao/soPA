package test;

import benchmark.internal.BenchmarkN;
import benchmark.objects.A;
import benchmark.objects.B;
import benchmark.objects.H;
import benchmark.objects.I;
import benchmark.objects.P;

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
public class Subclass {


  public Subclass() {
  }



  public static void main(String[] args) {

    BenchmarkN.alloc(1);
    A x=new A();
    P p=new P(x);
    BenchmarkN.alloc(2);
    A a=new A();
    p.alias(a);
    A b=p.getA();

    BenchmarkN.test(1,b);
  }

}
