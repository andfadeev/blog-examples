package org.acme;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.util.Random;

@Path("/fibonacci")
public class RandomFibonacciResource {

    public static long fibonacci(long n) {
        if (n == 1 || n == 2) {
            return 1;
        }
        return fibonacci(n - 1) + fibonacci(n - 2);
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String fibonacci() {
        Random random = new Random();
        int n = random.ints(0, 50)
            .findFirst()
            .getAsInt();
        return String.format("fib(%s) = %s", n, fibonacci(n));
    }
}
