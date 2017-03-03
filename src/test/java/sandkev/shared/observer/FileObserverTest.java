package sandkev.shared.observer;

import org.junit.Before;
import org.junit.Test;
import rx.Observable;
import rx.Subscriber;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static org.junit.Assert.*;

/**
 * Created by kevsa on 18/02/2017.
 */
public class FileObserverTest {


    private String path;

    @Before
    public void setUp() throws Exception {
        path = "D:/Media/photos";
    }

    @Test
    public void canFindFilesWithIo(){

        long start = System.currentTimeMillis();

        FileListener handler = new FileListener() {
            @Override
            public void onFile(File file) {
                System.out.println("onFile: " + file);
            }
        };

        MyScanner scanner = new MyScanner(handler, new Predicate<File>() {
            @Override
            public boolean test(File file) {
                return file.getName().endsWith(".jpg");
            }
        });

        int numFiles = scanner.scan(new File(path));

        System.out.println("total files found: " + numFiles + " in " + (System.currentTimeMillis() - start));

        //total files found: 4849 in 66,655

    }

    @Test
    public void canFindFilesWithNio() throws IOException {

        long start = System.currentTimeMillis();
        FileListener handler = new FileListener() {
            @Override
            public void onFile(File file) {
                System.out.println("onFile: " + file);
            }
        };

        Predicate<Path> filter = new Predicate<Path>() {
            @Override
            public boolean test(Path file) {
                return file.toString().endsWith(".jpg");
                //return file.getFileName().toString().endsWith(".jpg");
            }
        };

        MyNioScanner scanner = new MyNioScanner(handler, filter);

        int numFiles = scanner.scan(Paths.get(path));

        System.out.println("total files found: " + numFiles + " in " + (System.currentTimeMillis() - start));

        //total files found: 4849 in 11,181


    }


    @Test
    public void canUseReative(){

        Predicate<Path> filter = file -> file.toString().endsWith(".jpg");
        Observable<File> fileObservable = fileService(Paths.get(path), filter);

        fileObservable.subscribe(
                new Subscriber<File>() {
                    @Override
                    public void onNext(File item) {
                        System.out.println("Next: " + item);
                    }

                    @Override
                    public void onError(Throwable error) {
                        System.err.println("Error: " + error.getMessage());
                    }

                    @Override
                    public void onCompleted() {
                        System.out.println("Sequence complete.");
                    }
                }
        );


    }

    Observable<File> fileService(Path dir, final Predicate<Path> glob){
        return Observable.create(new Observable.OnSubscribe<File>() {
            @Override
            public void call(Subscriber<? super File> observer) {
                try {
                    if (!observer.isUnsubscribed()) {
                        doScan(dir, observer);
                        observer.onCompleted();
                    }
                } catch (Exception e) {
                    observer.onError(e);
                }
            }

            void doScan(Path dir, Subscriber<? super File> observer){
                try(DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
                    for (Path path : stream) {
                        if(path.toFile().isDirectory()) {
                            doScan(path, observer);
                        } else {
                            if(glob.test(path)){
                                observer.onNext(path.toFile());
                            }
                        }
                    }
                } catch(IOException e) {
                    e.printStackTrace();
                }

            }

        } );
    }



    private interface FileListener{
        void onFile(File file);
    }

    private interface Scanner{
        int scan(File file) throws IOException;
    }

    private static class MyNioScanner {
        private final FileListener fileListener;
        private final Predicate<Path> glob;
        public MyNioScanner(FileListener fileListener, Predicate<Path> filter) {
            this.fileListener = fileListener;
            this.glob = filter;
        }
        //@Override
        public int scan(Path dir) throws IOException {
            AtomicInteger numFiles = new AtomicInteger();
            //Path dir = Paths.get(file.toURI());
            try(DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
                for (Path path : stream) {
                    if(path.toFile().isDirectory()) {
                        numFiles.getAndAdd(scan(path));
                    } else {
                        if(glob.test(path)){
                            numFiles.getAndIncrement();
                            fileListener.onFile(path.toFile());
                        }
                    }
                }
            } catch(IOException e) {
                e.printStackTrace();
            }
            return numFiles.get();
        }
    }

    private static class MyScanner implements Scanner{
        private final FileListener fileListener;
        private final Predicate<File> filter;
        public MyScanner(FileListener fileListener, Predicate<File> filter) {
            this.fileListener = fileListener;
            this.filter = filter;
        }
        public int scan(File path){
            return doScan(path);
        }
        private int doScan(File path){
            AtomicInteger numFiles = new AtomicInteger();
            path.listFiles(new FileFilter() {
                public boolean accept(File file) {
                    if(file.isDirectory()){
                        numFiles.getAndAdd(doScan(file));
                        return false;
                    }else {
                        boolean accept = filter.test(file);
                        if (accept) {
                            numFiles.getAndIncrement();
                            fileListener.onFile(file);
                        }
                        return accept;
                    }
                }
            });
            return numFiles.get();
        }
    }



}