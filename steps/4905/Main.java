import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@SuppressWarnings("CommentedOutCode")
public class Main {
    public static void main(String[] args) {
        /* Create a callable */
        final Callable<Integer> generator = () -> {
            TimeUnit.SECONDS.sleep(5);
            return 700000;
        };

        /* Invoke callable and receive the number */
        try {
            System.out.println("Trying to receive a result from the Callable...");
            final Integer call = generator.call();
            System.out.println("Callable returned: " + call + "\n");
        } catch (Exception e) {
            e.printStackTrace();
        }

        /* 3.
           Create an executor and a future */
        ExecutorService executor = Executors.newSingleThreadExecutor();

        final Future<Integer> future = executor.submit(() -> {
            /* to get 'TimeoutException' increase this value to 15 */
            TimeUnit.SECONDS.sleep(5);
            /* uncomment next line to get 'ExecutionException', remove return */
//            throw new Exception(Thread.currentThread().getName());
            return 700000;
        });

        /* 4.
           without handling the exception or checking isCancelled()
           the program may run forever and may cause 'CancellationException'
         */
//        future.cancel(false);

        try {
            System.out.println("Trying to receive a result from the Future...");
            if (!future.isCancelled()) {
                final Integer num = future.get(10L, TimeUnit.SECONDS);
                System.out.println("Future returned: " + num);
            } else {
                System.out.println("The Future is cancelled.");
            }
            /* uncomment next lines to get 'InterruptedException' */
//            Thread.currentThread().interrupt();
//            if (Thread.interrupted())
//                throw new InterruptedException();
        } catch (InterruptedException | ExecutionException | TimeoutException | CancellationException e) {
            e.printStackTrace();
        }
        System.out.println("Future is done: " + future.isDone() + "\n");
        executor.shutdown();

        /* 5.
           creating two Futures */
        System.out.println("Calculating result of two Futures...");
        executor = Executors.newFixedThreadPool(4);

        Future<Integer> future1 = executor.submit(() -> {
            TimeUnit.SECONDS.sleep(5);
            return 700000;
        });

        Future<Integer> future2 = executor.submit(() -> {
            TimeUnit.SECONDS.sleep(5);
            return 900000;
        });

        int result = 0; // waiting for both results
        try {
            result = future1.get() + future2.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        System.out.println("The result is: " + result + "\n"); // 1600000
        executor.shutdown();

        /* 6.
           invokeAll and invokeAny */
        System.out.println("Calculations using invokeAll()...");
        executor = Executors.newFixedThreadPool(4);
        List<Callable<Integer>> callables =
                List.of(() -> 1000, () -> 2000, () -> 1500); // three "difficult" tasks

        List<Future<Integer>> futures;
        try {
            futures = executor.invokeAll(callables);
            int sum = 0;
            for (Future<Integer> fut : futures) {
                sum += fut.get(); // blocks on each future to get a result
            }
            System.out.println("The result is: " + sum);
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        /* Shutdown the executor to stop the program */
        executor.shutdown();
    }
}
