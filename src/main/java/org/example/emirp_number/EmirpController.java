package org.example.emirp_number;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

public class EmirpController {
    @FXML private TextField upperLimitField;
    @FXML private TextField threadCountField;
    @FXML private Button startButton;
    @FXML private Button stopButton;
    @FXML private Label errorLabel;
    @FXML private Label statusLabel;
    @FXML private TilePane workerPane;
    @FXML private TextArea resultsArea;
    @FXML private Label emirpCountLabel;
    @FXML private Label nonEmirpCountLabel;
    @FXML private Label totalCountLabel;

    private SearchRun activeRun;

    private static final class SearchRun {
        final int limit;
        final List<EmirpWorker> workers = new ArrayList<>();
        int finished;

        SearchRun(int limit) {
            this.limit = limit;
        }
    }

    @FXML
    private void startSearch() {
        if (activeRun != null) {
            return;
        }
        int limit = positiveInteger(upperLimitField.getText());
        int count = positiveInteger(threadCountField.getText());
        if (limit == 0) {
            showError("Upper limit (num) must be a whole number from 1 to 2147483647.");
            return;
        }
        if (count == 0 || count > Math.min(limit, 64)) {
            showError("Threads (cnt) must be a whole number from 1 to " + Math.min(limit, 64) + ".");
            return;
        }

        errorLabel.setText("");
        errorLabel.setVisible(false);
        clearResults();
        workerPane.getChildren().clear();
        setRunning(true);
        SearchRun run = new SearchRun(limit);
        activeRun = run;

        for (EmirpSearch.Range range : EmirpSearch.ranges(limit, count)) {
            EmirpWorker worker = new EmirpWorker(range);
            run.workers.add(worker);
            Label state = new Label("Running");
            int threadNumber = run.workers.size();
            workerPane.getChildren().add(workerCard(threadNumber, range, worker, state));
            worker.setOnSucceeded(event -> workerFinished(run, state));
            worker.setOnFailed(event -> {
                if (activeRun == run) {
                    cancelRun("Search failed.");
                }
            });
            Thread thread = new Thread(worker, "emirp-worker-" + threadNumber);
            thread.setDaemon(true);
            thread.start();
        }
        updateStatus(run);
    }

    private static VBox workerCard(int number, EmirpSearch.Range range, EmirpWorker worker, Label state) {
        ProgressIndicator progress = new ProgressIndicator(0);
        progress.setPrefSize(48, 48);
        progress.setMaxSize(48, 48);
        progress.progressProperty().bind(worker.progressProperty());

        Label bounds = new Label(range.first() + " to " + range.last());
        bounds.setMaxWidth(Double.MAX_VALUE);
        bounds.setWrapText(true);
        Label size = new Label(range.size() + (range.size() == 1 ? " number" : " numbers"));
        VBox card = new VBox(6, new Label("Thread " + number), progress, bounds, size, state);
        card.setAlignment(Pos.TOP_CENTER);
        card.setPrefWidth(150);
        return card;
    }

    private void workerFinished(SearchRun run, Label state) {
        if (activeRun != run) {
            return;
        }
        state.setText("Done");
        run.finished++;
        updateStatus(run);
        if (run.finished == run.workers.size()) {
            StringJoiner numbers = new StringJoiner(", ");
            int matches = 0;
            for (EmirpWorker worker : run.workers) {
                EmirpWorker.Result result = worker.getValue();
                if (result.matches() > 0) {
                    numbers.add(result.numbers());
                    matches += result.matches();
                }
            }
            resultsArea.setText(numbers.toString());
            setTotals(matches, run.limit - matches, run.limit);
            activeRun = null;
            setRunning(false);
        }
    }

    private void updateStatus(SearchRun run) {
        String status = run.finished == run.workers.size()
                ? "Complete. Checked all " + run.limit + " numbers from 1 to " + run.limit + "."
                : "Checking numbers from 1 to " + run.limit + ".";
        statusLabel.setText(status + " Finished threads: " + run.finished + " of " + run.workers.size() + ".");
    }

    @FXML
    private void stopSearch() {
        cancelRun("Stopped.");
    }

    void shutdown() {
        cancelRun("Stopped.");
    }

    private void cancelRun(String status) {
        SearchRun run = activeRun;
        if (run == null) {
            return;
        }
        activeRun = null;
        for (EmirpWorker worker : run.workers) {
            worker.cancel();
        }
        workerPane.getChildren().clear();
        clearResults();
        statusLabel.setText(status);
        setRunning(false);
    }

    private void clearResults() {
        resultsArea.clear();
        setTotals(0, 0, 0);
    }

    private void setTotals(int matches, int nonMatches, int total) {
        emirpCountLabel.setText("Emirp numbers: " + matches);
        nonEmirpCountLabel.setText("Non-emirp numbers: " + nonMatches);
        totalCountLabel.setText("Total checked: " + total);
    }

    private void setRunning(boolean running) {
        upperLimitField.setDisable(running);
        threadCountField.setDisable(running);
        startButton.setDisable(running);
        stopButton.setDisable(!running);
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }

    private static int positiveInteger(String text) {
        try {
            return Math.max(0, Integer.parseInt(text.trim()));
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }
}
