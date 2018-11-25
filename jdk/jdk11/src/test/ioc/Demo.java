package ioc;

public class Demo {

    public static void main(String[] args) {
        var injector = new Injector();
        injector.registerQualifiedClass(Node.class, NodeA.class);
        injector.registerQualifiedClass(Node.class, NodeB.class);
        var root = injector.getInstance(Root.class);
        System.out.println(root);
    }

}
