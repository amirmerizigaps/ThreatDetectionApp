package ThreatDetectionApp;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AnalysingSystem {

    public enum RiskLevel {
        LOW, MEDIUM, HIGH, NOT_AVAILABLE
    }

    public enum VerificationStatus {
        VERIFIED, NOT_VERIFIED
    }

    public enum InputSource {
        CSV, MANUAL
    }

    public static class AnalysisResult {

        private final int ID;
        private final String content;
        private final String type;
        private final RiskLevel risk;
        private final VerificationStatus status;
        private final String cause;
        private final int progress;
        private final InputSource source;

        private final String expectedRiskLevel;
        private final String expectedCategory;
        private final String triggeredRulesDescription;

        public AnalysisResult(int ID, String content, String type,
                RiskLevel risk, VerificationStatus status,
                String cause, int progress, InputSource source,
                String expectedRiskLevel, String expectedCategory,
                String triggeredRulesDescription) {

            this.ID = ID;
            this.content = content;
            this.type = type;
            this.risk = risk;
            this.status = status;
            this.cause = cause;
            this.progress = progress;
            this.source = source;
            this.expectedRiskLevel = expectedRiskLevel;
            this.expectedCategory = expectedCategory;
            this.triggeredRulesDescription = triggeredRulesDescription;
        }

        public int getID() { return ID; }
        public String getContent() { return content; }
        public String getType() { return type; }
        public RiskLevel getRisk() { return risk; }
        public VerificationStatus getStatus() { return status; }
        public String getCause() { return cause; }
        public int getProgress() { return progress; }
        public InputSource getSource() { return source; }

        public String getExpectedRiskLevel() {
            return expectedRiskLevel;
        }

        public String getExpectedCategory() {
            return expectedCategory;
        }

        public String getTriggeredRulesDescription() {
            return triggeredRulesDescription;
        }
    }

    public static class AnalysisProgress {

        private final int completed;
        private final int total;
        private final int percentage;
        private final int currentID;
        private final InputSource source;

        public AnalysisProgress(int completed, int total,
                int currentID, InputSource source) {

            this.completed = completed;
            this.total = total;
            this.percentage = total == 0 ? 100 : completed * 100 / total;
            this.currentID = currentID;
            this.source = source;
        }

        public int getCompleted() { return completed; }
        public int getTotal() { return total; }
        public int getPercentage() { return percentage; }
        public int getCurrentID() { return currentID; }
        public InputSource getSource() { return source; }
    }

    public interface ProgressListener {
        void onProgressUpdate(AnalysisProgress progress);
    }

    public List<String> getCSVParsingErrors(CSVfile csvFile) {

        if (csvFile == null) {
            return List.of("CSV file is not provided.");
        }

        return csvFile.getErrors();
    }

    private AnalysisResult analyzeInput(
            int ID, String content, String type, InputSource source,
            String expectedRiskLevel, String expectedCategory,
            String triggeredRulesDescription) {

        if (content == null || content.isBlank()) {

            return new AnalysisResult(
                    ID, content, type,
                    RiskLevel.NOT_AVAILABLE,
                    VerificationStatus.NOT_VERIFIED,
                    "Empty input",
                    100, source,
                    expectedRiskLevel, expectedCategory,
                    triggeredRulesDescription
            );
        }

        int Score = 0;
        List<String> causes = new ArrayList<>();

        // URL ANALYSIS
        if ("URL".equalsIgnoreCase(type)) {

            try {

                URI uri = URI.create(content.trim());

                String scheme = uri.getScheme();
                String host = uri.getHost();

                if (scheme == null || host == null || host.isBlank()) {

                    return new AnalysisResult(
                            ID, content, type,
                            RiskLevel.NOT_AVAILABLE,
                            VerificationStatus.NOT_VERIFIED,
                            "Malformed URL or missing host",
                            100, source,
                            expectedRiskLevel, expectedCategory,
                            triggeredRulesDescription
                    );
                }

                scheme = scheme.toLowerCase(Locale.ROOT);

                if (!scheme.equals("http") && !scheme.equals("https")) {

                    return new AnalysisResult(
                            ID, content, type,
                            RiskLevel.NOT_AVAILABLE,
                            VerificationStatus.NOT_VERIFIED,
                            "Unsupported URL protocol",
                            100, source,
                            expectedRiskLevel, expectedCategory,
                            triggeredRulesDescription
                    );
                }

                if (scheme.equals("http")) {
                    Score += 30;
                    causes.add("Uses HTTP instead of HTTPS");
                }

                if (host.matches("^(\\d{1,3}\\.){3}\\d{1,3}$")) {
                    Score += 40;
                    causes.add("Uses an IP address instead of a domain");
                }

                if (uri.getUserInfo() != null) {
                    Score += 40;
                    causes.add("Contains user information in URL");
                }

                if (host.split("\\.").length >= 4) {
                    Score += 30;
                    causes.add("Contains multiple nested subdomains");
                }

            } catch (IllegalArgumentException e) {

                return new AnalysisResult(
                        ID, content, type,
                        RiskLevel.NOT_AVAILABLE,
                        VerificationStatus.NOT_VERIFIED,
                        "Malformed URL",
                        100, source,
                        expectedRiskLevel, expectedCategory,
                        triggeredRulesDescription
                );
            }

        }

        // MESSAGE ANALYSIS
        else if ("MESSAGE".equalsIgnoreCase(type)
                || "Message".equalsIgnoreCase(type)) {

            String message = content.toLowerCase(Locale.ROOT);

            if (message.contains("verify your account")
                    || message.contains("account verification")
                    || message.contains("confirm your password")
                    || message.contains("account suspended")
                    || message.contains("prevent suspension")) {

                Score += 50;
                causes.add("Contains suspicious account verification or suspension language");
            }

            if (message.contains("urgent")
                    || message.contains("immediately")
                    || message.contains("act now")
                    || message.contains("within 24 hours")
                    || message.contains("required within")) {

                Score += 30;
                causes.add("Contains urgency-related language");
            }

            if (message.contains("click here")
                    || message.contains("free gift")
                    || message.contains("claim your prize")) {

                Score += 20;
                causes.add("Contains potentially suspicious promotional language");
            }

            if (message.contains("notification settings")
                    || message.contains("account digest")) {

                Score += 30;
                causes.add("Contains account notification-related language");
            }

            if (message.matches("(?s).*https?://\\d{1,3}(\\.\\d{1,3}){3}.*")) {

                Score += 40;
                causes.add("Contains a URL using an IP address");
            }

        }

        else {

            return new AnalysisResult(
                    ID, content, type,
                    RiskLevel.NOT_AVAILABLE,
                    VerificationStatus.NOT_VERIFIED,
                    "Unsupported input type",
                    100, source,
                    expectedRiskLevel, expectedCategory,
                    triggeredRulesDescription
            );
        }

        RiskLevel risk;

        if (Score >= 50) {
            risk = RiskLevel.HIGH;
        } else if (Score >= 30) {
            risk = RiskLevel.MEDIUM;
        } else {
            risk = RiskLevel.LOW;
        }

        String cause = causes.isEmpty()
                ? "No suspicious patterns detected by the implemented rules"
                : String.join("; ", causes);

        return new AnalysisResult(
                ID, content, type,
                risk,
                VerificationStatus.VERIFIED,
                cause,
                100, source,
                expectedRiskLevel, expectedCategory,
                triggeredRulesDescription
        );
    }

    // MANUAL INPUT ANALYSIS

    public List<AnalysisResult> analyzeManualList(AnalysingList list) {
        return analyzeManualList(list, null);
    }

    public List<AnalysisResult> analyzeManualList(
            AnalysingList list, ProgressListener listener) {

        if (list == null || list.isEmpty()) {
            return new ArrayList<>();
        }

        List<input> inputs = new ArrayList<>(list.getInputs());

        List<InputSource> sources = new ArrayList<>();

        for (int i = 0; i < inputs.size(); i++) {
            sources.add(InputSource.MANUAL);
        }

        List<CSVfile.CSVRecord> records = new ArrayList<>();

        for (int i = 0; i < inputs.size(); i++) {
            records.add(null);
        }

        return analyzeSequentiallyWithSources(
                inputs, sources, records, listener);
    }

    // CSV ANALYSIS

    public List<AnalysisResult> analyzeCSV(CSVfile csvFile)
            throws Exception {

        return analyzeCSV(csvFile, null);
    }

    public List<AnalysisResult> analyzeCSV(
            CSVfile csvFile, ProgressListener listener) throws Exception {

        if (csvFile == null) {
            throw new IllegalArgumentException("CSV file cannot be null");
        }

        List<CSVfile.CSVRecord> dataset = csvFile.readDataset();

        List<input> inputs = new ArrayList<>();
        List<InputSource> sources = new ArrayList<>();

        for (CSVfile.CSVRecord record : dataset) {

            inputs.add(record.getInput());
            sources.add(InputSource.CSV);
        }

        return analyzeSequentiallyWithSources(
                inputs, sources, dataset, listener);
    }

    // ANALYZE MANUAL INPUTS AND CSV TOGETHER

    public List<AnalysisResult> analyzeEverything(
            AnalysingList list, CSVfile csvFile) throws Exception {

        return analyzeEverything(list, csvFile, null);
    }

    public List<AnalysisResult> analyzeEverything(
            AnalysingList list, CSVfile csvFile,
            ProgressListener listener) throws Exception {

        List<input> inputs = new ArrayList<>();
        List<InputSource> sources = new ArrayList<>();
        List<CSVfile.CSVRecord> records = new ArrayList<>();

        if (list != null) {

            for (input currentInput : list.getInputs()) {

                inputs.add(currentInput);
                sources.add(InputSource.MANUAL);
                records.add(null);
            }
        }

        if (csvFile != null) {

            List<CSVfile.CSVRecord> dataset = csvFile.readDataset();

            for (CSVfile.CSVRecord record : dataset) {

                inputs.add(record.getInput());
                sources.add(InputSource.CSV);
                records.add(record);
            }
        }

        return analyzeSequentiallyWithSources(
                inputs, sources, records, listener);
    }

    // SEQUENTIAL ANALYSIS

    private List<AnalysisResult> analyzeSequentiallyWithSources(
            List<input> inputs,
            List<InputSource> sources,
            List<CSVfile.CSVRecord> records,
            ProgressListener listener) {

        List<AnalysisResult> results = new ArrayList<>();

        int total = inputs.size();
        int completed = 0;

        for (int i = 0; i < total; i++) {

            input currentInput = inputs.get(i);

            InputSource source = sources.get(i);

            CSVfile.CSVRecord record = records.get(i);

            String type;

            if (currentInput instanceof URL) {
                type = "URL";
            } else if (currentInput instanceof Message) {
                type = "MESSAGE";
            } else {
                type = "UNKNOWN";
            }

            String expectedRisk = null;
            String expectedCategory = null;
            String triggeredRules = null;

            if (record != null) {

                expectedRisk = record.getExpectedRiskLevel();
                expectedCategory = record.getExpectedCategory();
                triggeredRules = record.getTriggeredRulesDescription();
            }

            AnalysisResult result = analyzeInput(
                    currentInput.getID(),
                    currentInput.getContent(),
                    type,
                    source,
                    expectedRisk,
                    expectedCategory,
                    triggeredRules
            );

            results.add(result);

            completed++;

            if (listener != null) {

                listener.onProgressUpdate(
                        new AnalysisProgress(
                                completed,
                                total,
                                currentInput.getID(),
                                source
                        )
                );
            }
        }

        return results;
    }
}