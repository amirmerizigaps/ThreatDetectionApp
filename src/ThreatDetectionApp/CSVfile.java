package ThreatDetectionApp;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CSVfile {

    
    private String fileName;
    private String path;

   
    public CSVfile(String fileName, String path) {
        this.fileName = fileName;
        this.path = path;
    }

   
    public String getFileName() {
        return fileName;
    }

    public String getPath() {
        return path;
    }

   
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setPath(String path) {
        this.path = path;
    }

    
    public boolean exists() {
        Path filePath = Path.of(path);

        return Files.exists(filePath) && Files.isRegularFile(filePath);
    }
    

    
    public List<URL> readURLs() throws IOException  {

        List<URL> urls = new ArrayList<>();

        if (!exists()) {
            throw new IOException("CSV file does not exist: " + path); 
        }

        try (BufferedReader reader = Files.newBufferedReader(Path.of(path))) { //I can read File Line by Line//

            String line;
            int id = 1;

            while ((line = reader.readLine()) != null) {

                line = line.trim();

                
                if (line.isEmpty()) {
                    continue;
                }

                
                if (line.equalsIgnoreCase("url")) {
                    continue;
                }

                
                String urlContent = line.split(",")[0].trim();

                
                String format;

                if (urlContent.toLowerCase(Locale.ROOT).startsWith("https://")) {
                    format = "HTTPS";
                } else if (urlContent.toLowerCase(Locale.ROOT).startsWith("http://")) {
                    format = "HTTP";
                } else {
                    format = "UNKNOWN";
                }

                urls.add(new URL(id++, urlContent, format));
            }
        }

        return urls;
    }
}