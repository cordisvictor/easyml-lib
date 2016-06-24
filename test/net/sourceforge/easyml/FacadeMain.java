package net.sourceforge.easyml;

/**
 *
 * @author victor
 */
public class FacadeMain {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
//        final Person in = new Person(1, "da");
//
//        final EasyML easyml = new EasyML();
//        easyml.setStyle(EasyML.Style.PRETTY);
////        easyml.alias(Person.class, "Persoana");
//        long time = System.currentTimeMillis();
//        for (int i = 0; i < 100000; i++) {
//            easyml.deserialize(easyml.serialize(in));
//        }
//        System.out.println(System.currentTimeMillis() - time);
    }

    private static final class Person {

        public int id;
        public String name;

        public Person(int id, String name) {
            this.id = id;
            this.name = name;
        }
    }
}
