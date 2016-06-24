package net.sourceforge.easyml;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author victor
 */
public class MultiThreadEasyMLTest {

    private final EasyML easyml = new EasyML();

    @Test
    public void testMultiThreads1() throws Exception {
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
        final WorkerThread<List<Integer>> t1 = new WorkerThread(new ArrayList(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8)));
        final WorkerThread<List<Integer>> t2 = new WorkerThread(new ArrayList(Arrays.asList(8, 7, 6, 5, 4, 3, 2, 1)));
        final WorkerThread<List<Integer>> t3 = new WorkerThread(new ArrayList(Arrays.asList(1, 1, 1, 1, 1, 1, 1, 1)));
        final WorkerThread<Date> t4 = new WorkerThread(new Date());
        easyml.setDateFormat("dd-MM-yyyy'T'HH:mm:ss:SSS");
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
        final WorkerThread<List<Integer>> t1 = new NewWorkerThread(new ArrayList(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8)));
        final WorkerThread<List<Integer>> t2 = new NewWorkerThread(new ArrayList(Arrays.asList(8, 7, 6, 5, 4, 3, 2, 1)));
        final WorkerThread<List<Integer>> t3 = new NewWorkerThread(new ArrayList(Arrays.asList(1, 1, 1, 1, 1, 1, 1, 1)));
        final WorkerThread<Date> t4 = new NewWorkerThread(new Date());
        easyml.setDateFormat("dd-MM-yyyy'T'HH:mm:ss:SSS");
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
