package org.example.emirp_number;

import javafx.concurrent.Task;

final class EmirpWorker extends Task<EmirpWorker.Result> {
    record Result(String numbers, int matches) {}

    private final EmirpSearch.Range range;

    EmirpWorker(EmirpSearch.Range range) {
        this.range = range;
        updateProgress(0, range.size());
    }

    @Override
    protected Result call() {
        StringBuilder numbers = new StringBuilder();
        int matches = 0;
        int checked = 0;
        long lastUpdate = System.nanoTime();

        for (long number = range.first(); number <= range.last(); number++) {
            if (isCancelled()) {
                return null;
            }
            if (EmirpSearch.isEmirp((int) number)) {
                if (matches > 0) {
                    numbers.append(", ");
                }
                numbers.append(number);
                matches++;
            }
            checked++;
            if (checked % 128 == 0) {
                long now = System.nanoTime();
                if (now - lastUpdate >= 50_000_000L) {
                    updateProgress(checked, range.size());
                    lastUpdate = now;
                }
            }
        }
        updateProgress(checked, range.size());
        return new Result(numbers.toString(), matches);
    }
}
