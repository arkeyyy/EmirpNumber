package org.example.emirp_number;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

final class EmirpSearch {
    private EmirpSearch() {
    }

    record Range(int first, int last) {
        int size() {
            return last - first + 1;
        }
    }

    static List<Range> ranges(int num, int cnt) {
        if (num < 1 || cnt < 1 || cnt > Math.min(num, 64)) {
            throw new IllegalArgumentException("Invalid upper limit or thread count.");
        }

        long remaining = num - cnt;
        int totalWeight = cnt * (cnt + 1) / 2;
        int[] sizes = new int[cnt];
        int[] remainders = new int[cnt];
        List<Integer> byRemainder = new ArrayList<>(cnt);
        int unassigned = num;
        for (int worker = 0; worker < cnt; worker++) {
            long weightedShare = remaining * (worker + 1);
            sizes[worker] = 1 + (int) (weightedShare / totalWeight);
            remainders[worker] = (int) (weightedShare % totalWeight);
            unassigned -= sizes[worker];
            byRemainder.add(worker);
        }

        byRemainder.sort(Comparator.<Integer>comparingInt(worker -> remainders[worker]).reversed());
        for (int i = 0; i < unassigned; i++) {
            sizes[byRemainder.get(i)]++;
        }
        if (cnt > 1 && sizes[0] > 1 && Arrays.stream(sizes).allMatch(size -> size == sizes[0])) {
            sizes[0]--;
            sizes[cnt - 1]++;
        }

        List<Range> ranges = new ArrayList<>(cnt);
        int last = 0;
        for (int size : sizes) {
            int first = last + 1;
            last += size;
            ranges.add(new Range(first, last));
        }
        return ranges;
    }

    static boolean isEmirp(int number) {
        if (!isPrime(number)) {
            return false;
        }

        long reversed = 0;
        for (int digits = number; digits > 0; digits /= 10) {
            reversed = reversed * 10 + digits % 10;
        }
        return reversed != number && isPrime(reversed);
    }

    private static boolean isPrime(long number) {
        if (number < 2 || number % 2 == 0 || number % 3 == 0) {
            return number == 2 || number == 3;
        }
        for (long divisor = 5; divisor * divisor <= number; divisor += 6) {
            if (number % divisor == 0 || number % (divisor + 2) == 0) {
                return false;
            }
        }
        return true;
    }
}
