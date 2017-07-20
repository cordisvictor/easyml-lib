/*
 * Copyright 2012 Victor Cordis
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Please contact the author ( cordis.victor@gmail.com ) if you need additional
 * information or have any questions.
 */
package net.sourceforge.easyml;

/**
 *
 * @author victor
 */
public class FacadeMain {

//    public static void main(String[] args) {
//        final Person in = new Person(1, "da");
//
//        final EasyML easyml = new EasyMLBuilder()
//                .withStyle(EasyML.Style.PRETTY)
//                .build();
//        long time = System.currentTimeMillis();
//        for (int i = 0; i < 100000; i++) {
//            easyml.deserialize(easyml.serialize(in));
//        }
//        System.out.println(System.currentTimeMillis() - time);
//    }
    
    public static final class Person {

        public int id;
        public String name;

        private Person(){}
        public Person(int id, String name) {
            this.id = id;
            this.name = name;
        }
    }
}
