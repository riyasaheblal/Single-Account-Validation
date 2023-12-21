package cedge.pfms.av;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jasypt.properties.EncryptableProperties;

public class BuildProperties {

	public static void main(String[] args) throws FileNotFoundException, IOException {

		String AbsolutePath = BuildProperties.class.getProtectionDomain().getCodeSource().getLocation().getPath();
		System.out.println(AbsolutePath);

		StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
		encryptor.setPassword("PFMS-SINGLE-ACCOUNT-VALIDATION");
		MakeDir("Config/");
		FileOutputStream out = new FileOutputStream(AbsolutePath + "Config/config.properties");
		FileInputStream in = new FileInputStream(AbsolutePath + "Config/config.properties");
		Properties props = new EncryptableProperties(encryptor);
		props.load(in);
		in.close();
		props.setProperty("Enable", "1");
		props.setProperty("WaitTime", "50000");
		props.setProperty("StartTime", "00:01");
		props.setProperty("EndTime", "23:59");
		props.setProperty("Driver", encryptor.encrypt("oracle.jdbc.driver.OracleDriver"));
		props.setProperty("URL", encryptor.encrypt("jdbc:oracle:thin:@10.43.1.114:1531:cpprod"));
		props.setProperty("UserName", encryptor.encrypt("CPSMS_MASTER"));
		props.setProperty("Password", encryptor.encrypt("CPSMS_MASTER"));
		props.setProperty("BankGroup", "RRB SBI");
		props.setProperty("BankSubGroup", "GRP1");
		props.setProperty("FileLimit", "20");
		props.setProperty("XMLParserFile", "Config/SINGLE-AV-REQ.xsl");
		props.setProperty("InfProcFile", "Config/SINGLE-AV-INF-PROC.sql");
		props.store(out, null);
		out.close();

		props.load(new FileInputStream(AbsolutePath + "Config/config.properties"));
		String Enable = props.getProperty("Enable");
		String WaitTime = props.getProperty("WaitTime");
		String StartTime = props.getProperty("StartTime");
		String EndTime = props.getProperty("EndTime");
		String Driver = encryptor.decrypt(props.getProperty("Driver"));
		String URL = encryptor.decrypt(props.getProperty("URL"));
		String UserName = encryptor.decrypt(props.getProperty("UserName"));
		String Password = encryptor.decrypt(props.getProperty("Password"));
		String BankGroup = props.getProperty("BankGroup");
		String BankSubGroup = props.getProperty("BankSubGroup");
		String FileLimit = props.getProperty("FileLimit");
		String XMLParserFile = props.getProperty("XMLParserFile");

		System.out.println(Enable + " " + WaitTime + " " + StartTime + " " + EndTime + " " + Driver + " " + URL + " "
				+ UserName + " " + Password+" "+BankGroup+" "+BankSubGroup+" "+FileLimit+" "+XMLParserFile);
	}
	
	public static boolean MakeDir(String DirName) {

        // Create a File object for the directory
        File newDirectory = new File(DirName);

        // Check if the directory already exists
        if (!newDirectory.exists()) {
            // Create the directory
            if (newDirectory.mkdirs()) {
                System.out.println("Directory created successfully.");
            } else {
                System.out.println("Failed to create the directory.");
            }
        } else {
            System.out.println("Directory already exists.");
        }
		return true;
	}

}

