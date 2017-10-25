import io.netty.concurrent.Future;
import io.netty.concurrent.GenericFutureListener;

public class Test {
    public static void main(String[] args) {
        /**
         * 此处对于使用泛型继承的接口，需要指定类型；因此可以通过
         * 在特定场景派生指定类型的接口的标识性接口，从而便于使用,
         * 并且业务含义更明确;
         */
        new GenericFutureListener<Future>() {
            @Override
            public void operationComplete(Future future) throws Exception {

            }
        };
    }
}
