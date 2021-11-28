//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package test;

import benchmark.internal.BenchmarkN;
import benchmark.objects.A;
import benchmark.objects.B;

public class StaticVar2 {
    public StaticVar2() {
    }
    static A a = new A();

    static void myadd(B b){
        a.f=b;
    }

    public static void main(String[] args) {
        BenchmarkN.alloc(1);
        B b=new B();
        myadd(b);
        BenchmarkN.test(1, a.f);
    }
}
