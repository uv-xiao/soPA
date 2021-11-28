//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package test;

import benchmark.internal.BenchmarkN;
import benchmark.objects.A;
import benchmark.objects.B;

public class StaticVar {
    public StaticVar() {
    }
    static A a;

    static void myadd(A b){
        a=b;
    }

    public static void main(String[] args) {
        BenchmarkN.alloc(1);
        A b=new A();
        myadd(b);
        BenchmarkN.test(1, a);
    }
}
