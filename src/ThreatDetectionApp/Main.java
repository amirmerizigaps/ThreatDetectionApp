package ThreatDetectionApp;

import java.util.List;

public class Main {

    public static void main(String[] args) throws Exception {

        // 1. CREATE MANUAL INPUTS

        AnalysingList list = new AnalysingList();

        list.addInput(new URL(
                101,
                "https://www.google.com",
                "HTTPS"
        ));

        list.addInput(new URL(
                102,
                "http://192.168.1.50/login",
                "HTTP"
        ));

        list.addInput(new Message(
                103,
                "Your account verification is required urgently."
        ));

        list.addInput(new Message(
                104,
                "Your weekly notification settings are available."
        ));

        // 2. CREATE CSV FILE

        CSVfile csvFile = new CSVfile(
                "project_test_dataset",
                "C:/Users/hp/Desktop/L3 GL/DAC/Project/project_test_dataset.csv"
        );

        // 3. CREATE ANALYSIS SYSTEM

        AnalysingSystem system = new AnalysingSystem();

        // 4. ANALYZE MANUAL INPUTS

        System.out.println("\n========== MANUAL ANALYSIS ==========\n");

        List<AnalysingSystem.AnalysisResult> manualResults =
                system.analyzeManualList(list, progress -> {

                    System.out.println(
                            "Manual Progress: "
                            + progress.getPercentage()
                            + "% | ID: "
                            + progress.getCurrentID()
                    );
                });

        displayResults(manualResults);

        // 5. ANALYZE CSV DATASET

        System.out.println("\n========== CSV ANALYSIS ==========\n");

        List<AnalysingSystem.AnalysisResult> csvResults =
                system.analyzeCSV(csvFile, progress -> {

                    System.out.println(
                            "CSV Progress: "
                            + progress.getPercentage()
                            + "% | ID: "
                            + progress.getCurrentID()
                    );
                });

        displayResults(csvResults);

        // 6. DISPLAY CSV PARSING ERRORS

        System.out.println("\n========== CSV ERRORS ==========\n");

        List<String> errors = system.getCSVParsingErrors(csvFile);

        if (errors.isEmpty()) {

            System.out.println("No CSV parsing errors.");

        } else {

            for (String error : errors) {
                System.out.println(error);
            }
        }

        // 7. ANALYZE EVERYTHING TOGETHER

        System.out.println("\n========== COMBINED ANALYSIS ==========\n");

        List<AnalysingSystem.AnalysisResult> allResults =
                system.analyzeEverything(list, csvFile, progress -> {

                    System.out.println(
                            "Overall Progress: "
                            + progress.getPercentage()
                            + "% | ID: "
                            + progress.getCurrentID()
                    );
                });

        displayResults(allResults);

        System.out.println("\n========== FINAL SUMMARY ==========");

        System.out.println("Manual inputs: " + manualResults.size());
        System.out.println("CSV inputs: " + csvResults.size());
        System.out.println("Combined inputs: " + allResults.size());
    }

    // DISPLAY ANALYSIS RESULTS

    public static void displayResults(
            List<AnalysingSystem.AnalysisResult> results) {

        for (AnalysingSystem.AnalysisResult result : results) {

            System.out.println("----------------------------------");

            System.out.println("ID: " + result.getID());

            System.out.println("Type: " + result.getType());

            System.out.println("Content: " + result.getContent());

            System.out.println("Source: " + result.getSource());

            System.out.println("Actual Risk: " + result.getRisk());

            System.out.println("Verification: " + result.getStatus());

            System.out.println("Cause: " + result.getCause());

            if (result.getExpectedRiskLevel() != null) {

                System.out.println(
                        "Expected Risk: "
                        + result.getExpectedRiskLevel()
                );

                System.out.println(
                        "Expected Category: "
                        + result.getExpectedCategory()
                );

                System.out.println(
                        "Expected Rules: "
                        + result.getTriggeredRulesDescription()
                );
            }
        }

        System.out.println("----------------------------------");
        System.out.println("Total analyzed: " + results.size());
    }
}