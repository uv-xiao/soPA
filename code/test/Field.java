//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package test;

import benchmark.internal.BenchmarkN;
import benchmark.objects.A;
import benchmark.objects.B;

public class Field {
    public Field() {
    }

    public static void main(String[] args) {
        BenchmarkN.alloc(1);
        B b = new B();
        BenchmarkN.alloc(2);
        A a = new A(b);
        BenchmarkN.alloc(3);
        A c = new A();
        BenchmarkN.alloc(4);
        new B();
        c.f = a.f;
        B d = c.f;
        BenchmarkN.test(1, d);
        BenchmarkN.alloc(5);
        B e = new B();
        BenchmarkN.alloc(6);
        B f = new B();
        BenchmarkN.alloc(7);
        A a1 = new A();
        BenchmarkN.alloc(8);
        A a2 = new A();
        a1.f = e;
        a2.f = f;
        BenchmarkN.test(2, a1.f);
        BenchmarkN.test(3, a2.f);
    }
}
