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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import net.sourceforge.easyml.testmodel.FacultyDTO;
import net.sourceforge.easyml.testmodel.StudentPersonDTO;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author victor
 */
public class MultiThreadEasyMLTest {

    private EasyML easyml;

    @Test
    public void testMultiThreadsCtorFieldCaching() throws Exception {
        easyml = new EasyML();

        final WorkerThread<FacultyDTO> t1 = new WorkerThread(new FacultyDTO(144, "Faculty Name", new StudentPersonDTO[]{new StudentPersonDTO()}));
        final WorkerThread<StudentPersonDTO> t2 = new WorkerThread(new StudentPersonDTO(1, "fn1", "ln1", true, null));
        final WorkerThread<StudentPersonDTO> t3 = new WorkerThread(new StudentPersonDTO(2, "fn2", "ln2", false, new FacultyDTO(22, "Faculty")));
        t1.start();
        t2.start();
        t3.start();
        t1.join();
        t2.join();
        t3.join();
        assertEquals(t1.src, t1.dest);
        assertEquals(t2.src, t2.dest);
        assertEquals(t3.src, t3.dest);
    }

    @Test
    public void testMultiThreadsCtorFieldCachingNewWritersReaders() throws Exception {
        easyml = new EasyML();

        final WorkerThread<FacultyDTO> t1 = new NewWorkerThread(new FacultyDTO(144, "Faculty Name", new StudentPersonDTO[]{new StudentPersonDTO()}));
        final WorkerThread<StudentPersonDTO> t2 = new NewWorkerThread(new StudentPersonDTO(1, "fn1", "ln1", true, null));
        final WorkerThread<StudentPersonDTO> t3 = new NewWorkerThread(new StudentPersonDTO(2, "fn2", "ln2", false, new FacultyDTO(22, "Faculty")));
        t1.start();
        t2.start();
        t3.start();
        t1.join();
        t2.join();
        t3.join();
        assertEquals(t1.src, t1.dest);
        assertEquals(t2.src, t2.dest);
        assertEquals(t3.src, t3.dest);
    }

    @Test
    public void testMultiThreads1() throws Exception {
        easyml = new EasyML();

        final WorkerThread<List<Integer>> t1 = new WorkerThread(new ArrayList(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8)));
        final WorkerThread<List<Integer>> t2 = new WorkerThread(new ArrayList(Arrays.asList(8, 7, 6, 5, 4, 3, 2, 1)));
        final WorkerThread<List<Integer>> t3 = new WorkerThread(new ArrayList(Arrays.asList(1, 1, 1, 1, 1, 1, 1, 1)));
        t1.start();
        t2.start();
        t3.start();
        t1.join();
        t2.join();
        t3.join();
        List<Integer> result = new ArrayList();
        for (int i = 0; i < 8; i++) {
            result.add(t1.dest.get(i) + t2.dest.get(i) + t3.dest.get(i));
        }
        System.out.println("Threads result:" + result);
        assertEquals(new ArrayList(Arrays.asList(10, 10, 10, 10, 10, 10, 10, 10)), result);
    }

    @Test
    public void testMultiThreads2() throws Exception {
        easyml = new EasyMLBuilder().withDateFormat("dd-MM-yyyy'T'HH:mm:ss:SSS").build();

        final WorkerThread<List<Integer>> t1 = new WorkerThread(new ArrayList(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8)));
        final WorkerThread<List<Integer>> t2 = new WorkerThread(new ArrayList(Arrays.asList(8, 7, 6, 5, 4, 3, 2, 1)));
        final WorkerThread<List<Integer>> t3 = new WorkerThread(new ArrayList(Arrays.asList(1, 1, 1, 1, 1, 1, 1, 1)));
        final WorkerThread<Date> t4 = new WorkerThread(new Date());
        t1.start();
        t2.start();
        t3.start();
        t4.start();
        t1.join();
        t2.join();
        t3.join();
        t4.join();
        List<Integer> result = new ArrayList();
        for (int i = 0; i < 8; i++) {
            result.add(t1.dest.get(i) + t2.dest.get(i) + t3.dest.get(i));
        }
        System.out.println("Threads result:" + result);
        assertEquals(new ArrayList(Arrays.asList(10, 10, 10, 10, 10, 10, 10, 10)), result);
        assertEquals(t4.src, t4.dest);
    }

    @Test
    public void testMultiThreads1new() throws Exception {
        easyml = new EasyML();

        final WorkerThread<List<Integer>> t1 = new NewWorkerThread(new ArrayList(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8)));
        final WorkerThread<List<Integer>> t2 = new NewWorkerThread(new ArrayList(Arrays.asList(8, 7, 6, 5, 4, 3, 2, 1)));
        final WorkerThread<List<Integer>> t3 = new NewWorkerThread(new ArrayList(Arrays.asList(1, 1, 1, 1, 1, 1, 1, 1)));
        t1.start();
        t2.start();
        t3.start();
        t1.join();
        t2.join();
        t3.join();
        List<Integer> result = new ArrayList();
        for (int i = 0; i < 8; i++) {
            result.add(t1.dest.get(i) + t2.dest.get(i) + t3.dest.get(i));
        }
        System.out.println("Threads result:" + result);
        assertEquals(new ArrayList(Arrays.asList(10, 10, 10, 10, 10, 10, 10, 10)), result);
    }

    @Test
    public void testMultiThreads2new() throws Exception {
        easyml = new EasyMLBuilder().withDateFormat("dd-MM-yyyy'T'HH:mm:ss:SSS").build();

        final WorkerThread<List<Integer>> t1 = new NewWorkerThread(new ArrayList(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8)));
        final WorkerThread<List<Integer>> t2 = new NewWorkerThread(new ArrayList(Arrays.asList(8, 7, 6, 5, 4, 3, 2, 1)));
        final WorkerThread<List<Integer>> t3 = new NewWorkerThread(new ArrayList(Arrays.asList(1, 1, 1, 1, 1, 1, 1, 1)));
        final WorkerThread<Date> t4 = new NewWorkerThread(new Date());
        t1.start();
        t2.start();
        t3.start();
        t4.start();
        t1.join();
        t2.join();
        t3.join();
        t4.join();
        List<Integer> result = new ArrayList();
        for (int i = 0; i < 8; i++) {
            result.add(t1.dest.get(i) + t2.dest.get(i) + t3.dest.get(i));
        }
        System.out.println("Threads result:" + result);
        assertEquals(new ArrayList(Arrays.asList(10, 10, 10, 10, 10, 10, 10, 10)), result);
        assertEquals(t4.src, t4.dest);
    }

    private class WorkerThread<T> extends Thread {

        protected final T src;
        protected final ByteArrayOutputStream out;
        protected T dest;

        public WorkerThread(T src) {
            this.src = src;
            this.out = new ByteArrayOutputStream();
        }

        @Override
        public void run() {
            easyml.serialize(src, out);
            dest = (T) easyml.deserialize(new ByteArrayInputStream(out.toByteArray()));
        }
    }

    private class NewWorkerThread<T> extends WorkerThread<T> {

        public NewWorkerThread(T src) {
            super(src);
        }

        @Override
        public void run() {
            final XMLWriter w = easyml.newWriter(out);
            w.write(src);
            w.close();
            final XMLReader r = easyml.newReader(new ByteArrayInputStream(out.toByteArray()));
            dest = (T) r.read();
            r.close();
        }
    }
}
