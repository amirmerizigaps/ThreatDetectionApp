Java Used Functions:
Files.exists():	Checks whether a file exists.
Files.isRegularFile():	Checks whether the path points to a regular file.
Files.newBufferedReader():	Opens a file for reading.
split(","): divides a string wherever it finds a comma.
toLowerCase(Locale.ROOT):  Converts the string to lowercase.
startsWith(): Checks whether a string starts with a particular sequence of characters.
URI:URI stands for Uniform Resource Identifier.


input:
 is an abstract base class (ID plus content).
 URL and Message:
 extend it, so both kinds of input go through the same pipeline.
AnalysingList: 
holds the manual inputs. It rejects null inputs and duplicate IDs, and getInputs() returns an unmodifiable list.
CSVfile:
 reads URLs from a CSV file. It checks the path, skips the header and BOM, and collects bad rows in an errors list instead of crashing. It also assigns IDs, labels each URL as HTTP or HTTPS, and closes the reader properly.
AnalysingSystem: is the orchestrator. It has analyzeManualList, analyzeCSV and analyzeEverything, plus a ProgressListener that reports progress. Results come back as AnalysisResult, carrying RiskLevel, VerificationStatus and a cause string.

