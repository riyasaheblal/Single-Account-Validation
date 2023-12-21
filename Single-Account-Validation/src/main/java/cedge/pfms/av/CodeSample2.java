package cedge.pfms.av;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CodeSample2 {
    public static void main(String[] args) {
        // Specify the directory path
        String directoryPath = "D:\\PYtest";

        Path dir = Paths.get(directoryPath);

		if (Files.exists(dir) && Files.isDirectory(dir)) {
		    try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
		        for (Path file : stream) {
		            
		            Pattern pattern = Pattern.compile(".xml", Pattern.CASE_INSENSITIVE);
		            Matcher matcher = pattern.matcher(file.getFileName().toString());
		            boolean matchFound = matcher.find();
		            if(matchFound) {
			            System.out.println(directoryPath.toString()+File.separator+file.getFileName());
		            }
		            
		        }
		    } catch (IOException e) {
		        e.printStackTrace();
		    }
		} else {
		    System.out.println("The specified directory does not exist.");
		}
    }
}
