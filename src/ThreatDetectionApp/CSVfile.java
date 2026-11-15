package ThreatDetectionApp;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CSVfile {

    private String fileName;
    private String path;

    private final List<String> errors = new ArrayList<>();

    public CSVfile(String fileName, String path) {
        this.fileName = fileName;
        this.path = path;
    }

    // Represents one row from the CSV dataset
    public static class CSVRecord {

        private final input input;
        private final String expectedRiskLevel;
        private final String expectedCategory;
        private final String triggeredRulesDescription;

        public CSVRecord(input input, String expectedRiskLevel,
                         String expectedCategory,
                         String triggeredRulesDescription) {

            this.input = input;
            this.expectedRiskLevel = expectedRiskLevel;
            this.expectedCategory = expectedCategory;
            this.triggeredRulesDescription = triggeredRulesDescription;
        }

        public input getInput() {
            return input;
        }

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

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public List<String> getErrors() {
        return List.copyOf(errors);
    }

    public boolean exists() {

        if (path == null || path.isBlank()) {
            return false;
        }

        Path filePath = Path.of(path);

        return Files.exists(filePath)
                && Files.isRegularFile(filePath)
                && Files.isReadable(filePath);
    }

    public List<CSVRecord> readDataset() throws IOException {

        errors.clear();

        List<CSVRecord> dataset = new ArrayList<>();

        if (!exists()) {
            errors.add("CSV file does not exist or cannot be read: " + path);
            return dataset;
        }

        List<String> lines = Files.readAllLines(
                Path.of(path), StandardCharsets.UTF_8);

        if (lines.isEmpty()) {
            errors.add("CSV file is empty.");
            return dataset;
        }

        List<String> headers;

        try {
            String headerLine = lines.get(0).replace("\uFEFF", "");
            headers = parseCSVLine(headerLine);
        } catch (IllegalArgumentException e) {
            errors.add("Invalid CSV header: " + e.getMessage());
            return dataset;
        }

        Map<String, Integer> columns = new HashMap<>();

        for (int i = 0; i < headers.size(); i++) {
            columns.put(headers.get(i).trim().toLowerCase(), i);
        }

        String[] requiredColumns = {
                "id",
                "input_type",
                "content",
                "expected_risk_level",
                "expected_category",
                "triggered_rules_description"
        };

        for (String column : requiredColumns) {

            if (!columns.containsKey(column)) {
                errors.add("Missing CSV column: " + column);
                return dataset;
            }
        }

        for (int i = 1; i < lines.size(); i++) {

            int lineNumber = i + 1;
            String line = lines.get(i);

            if (line.isBlank()) {
                continue;
            }

            try {

                List<String> values = parseCSVLine(line);

                // Ignore extra trailing empty columns
                while (values.size() > headers.size()
                        && values.get(values.size() - 1).isEmpty()) {
                    values.remove(values.size() - 1);
                }

                if (values.size() != headers.size()) {
                    throw new IllegalArgumentException(
                            "Incorrect number of columns.");
                }

                int ID = Integer.parseInt(
                        values.get(columns.get("id")).trim());

                String inputType = values.get(
                        columns.get("input_type")).trim();

                String content = values.get(
                        columns.get("content"));

                String expectedRiskLevel = values.get(
                        columns.get("expected_risk_level")).trim();

                String expectedCategory = values.get(
                        columns.get("expected_category")).trim();

                String triggeredRulesDescription = values.get(
                        columns.get("triggered_rules_description")).trim();

                input currentInput;

                if (inputType.equalsIgnoreCase("URL")) {

                    currentInput = new URL(
                            ID, content, determineURLFormat(content));

                } else if (inputType.equalsIgnoreCase("MESSAGE")) {

                    currentInput = new Message(ID, content);

                } else {

                    throw new IllegalArgumentException(
                            "Unknown input type: " + inputType);
                }

                dataset.add(new CSVRecord(
                        currentInput,
                        expectedRiskLevel,
                        expectedCategory,
                        triggeredRulesDescription
                ));

            } catch (NumberFormatException e) {

                errors.add("Line " + lineNumber + ": Invalid ID.");

            } catch (IllegalArgumentException e) {

                errors.add("Line " + lineNumber + ": " + e.getMessage());
            }
        }

        return dataset;
    }

    private String determineURLFormat(String content) {

        if (content == null || content.isBlank()) {
            return "UNKNOWN";
        }

        if (content.regionMatches(true, 0, "https://", 0, 8)) {
            return "HTTPS";
        }

        if (content.regionMatches(true, 0, "http://", 0, 7)) {
            return "HTTP";
        }

        return "UNKNOWN";
    }

    // Supports quoted fields, commas inside quotes and escaped quotes
    private List<String> parseCSVLine(String line) {

        List<String> values = new ArrayList<>();
        StringBuilder field = new StringBuilder();

        boolean insideQuotes = false;

        for (int i = 0; i < line.length(); i++) {

            char current = line.charAt(i);

            if (current == '"') {

                if (insideQuotes && i + 1 < line.length()
                        && line.charAt(i + 1) == '"') {

                    field.append('"');
                    i++;

                } else {
                    insideQuotes = !insideQuotes;
                }

            } else if (current == ',' && !insideQuotes) {

                values.add(field.toString());
                field.setLength(0);

            } else {
                field.append(current);
            }
        }

        if (insideQuotes) {
            throw new IllegalArgumentException(
                    "Unclosed quotation mark.");
        }

        values.add(field.toString());

        return values;
    }
}