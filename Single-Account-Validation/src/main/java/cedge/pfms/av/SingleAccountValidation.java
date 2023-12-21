package cedge.pfms.av;

import static com.diogonunes.jcolor.Ansi.colorize;
import static com.diogonunes.jcolor.Attribute.BLUE_TEXT;
import static com.diogonunes.jcolor.Attribute.BOLD;
import static com.diogonunes.jcolor.Attribute.BRIGHT_WHITE_TEXT;
import static com.diogonunes.jcolor.Attribute.CYAN_TEXT;
import static com.diogonunes.jcolor.Attribute.GREEN_TEXT;
import static com.diogonunes.jcolor.Attribute.MAGENTA_TEXT;
import static com.diogonunes.jcolor.Attribute.RED_TEXT;
import static com.diogonunes.jcolor.Attribute.YELLOW_TEXT;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Array;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.logging.FileHandler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;

import com.google.common.collect.ImmutableList;

import oracle.jdbc.OracleConnection;
import oracle.jdbc.OracleTypes;

public class SingleAccountValidation {

	static String ABSPATH = "";
	static String OSTYPE = "";
	static String RespFileName = "";

	public static void main(String[] args) {

		String AbsolutePath = "";
		if (System.getProperty("os.name").toUpperCase().contains("LINUX")) {
			System.out.println("OS : " + System.getProperty("os.name"));
			AbsolutePath = SingleAccountValidation.class.getProtectionDomain().getCodeSource().getLocation().getPath();
			OSTYPE = "LINUX";
		} else if (System.getProperty("os.name").toUpperCase().contains("AIX")) {
			System.out.println("OS : " + System.getProperty("os.name"));
			AbsolutePath = SingleAccountValidation.class.getProtectionDomain().getCodeSource().getLocation().getPath();
			OSTYPE = "AIX";
		} else if (System.getProperty("os.name").toUpperCase().contains("WINDOWS")) {
			System.out.println("OS : " + System.getProperty("os.name"));
			AbsolutePath = SingleAccountValidation.class.getProtectionDomain().getCodeSource().getLocation().getPath();
			AbsolutePath = AbsolutePath.substring(1, AbsolutePath.length());
			OSTYPE = "WIN";
		} else {
			System.out.println("Invalid OS");
			OSTYPE = "INVALID";
		}

		ABSPATH = AbsolutePath;
		String DecodePath = null;
		boolean Running = true;
		int SleepTime = 10000, Enable = 1;
		try {
			while (Running) {

				DecodePath = URLDecoder.decode(AbsolutePath, "UTF-8");

				InitVar.setDECODEPATH(DecodePath);
				System.out.println("Current Running Path: " + InitVar.getDECODEPATH());
				WrLog("Current Running Path: " + InitVar.getDECODEPATH(), "I");
				ArrayList<String> DBProperties = ReadConfig(DecodePath);
				if (getCount(DBProperties)) {
					Enable = Integer.parseInt(DBProperties.get(0));
					SleepTime = Integer.parseInt(DBProperties.get(1));
					// System.out.println(DBProperties);
					if (Enable == 1) {
						if (TimeIntChk(DBProperties.get(2), DBProperties.get(3))) {

							try {
								// Program Start From Here
								SingleAccountValidation SAV = new SingleAccountValidation();
								SAV.Runner(DBProperties);
								SAV=null;
								//
								}
							catch(Exception e)
							{
								WrLog(e.toString(), "W");
							}

						} else {
							System.out.println("Time Not In Between StartTime & EndTime");
							WrLog("Time Not In Between StartTime & EndTime", "W");
						}

					} else {
						System.out.println(
								"Application Not Enabled By Configuration File, Please Check In The Property File.");
						System.out.println("Exiting From Application ..");
						WrLog("Application Not Enabled By Configuration File, Please Check In The Property File.", "W");
						WrLog("Exiting From Application ..", "W");
						break;
					}

					try {
						System.out.println("Thread Sleeping For " + (SleepTime / 1000) + " Seconds.");
						WrLog("Thread Sleeping For " + (SleepTime / 1000) + " Seconds.", "I");
						Thread.sleep(SleepTime);
						System.gc();
					} catch (InterruptedException e) {
						System.out.println("Time Out: " + SleepTime + " Error while Sleep Thread");
					}

				} else {
					System.out.println("Parameters Missing In Property File.");
					WrLog("Parameters Missing In Property File.", "W");
					System.exit(0);
				}

			}

		} catch (UnsupportedEncodingException e) {
			System.out.println("[ " + e + " ]");
			WrLog("[ " + e + " ]", "W");
		}

	}

	public boolean Runner(ArrayList<String> DBProperties) {

		boolean RetTyp = false;
		String XSLFile = DBProperties.get(11);
		ArrayList<String> INFDBProperties = new ArrayList<String>(DBProperties);
		ArrayList<String> CBSDBProperties = new ArrayList<String>(DBProperties);

		SingleAccountValidation SAV = new SingleAccountValidation();

		List<List<Object>> BankDetails = SAV.getOraData(DBProperties,
				InitVar.getBANKLIST(DBProperties.get(8), DBProperties.get(9)));

		for (int i = 0; i < BankDetails.size(); i++) {
			WrLog("[ PFMS-BANK-CODE::" + BankDetails.get(i).get(0).toString() + " ] [ BANK-CODE::"
					+ BankDetails.get(i).get(1).toString() + " ] [ BANK-NAME::" + BankDetails.get(i).get(2).toString()
					+ " ] ", "I");
			String PFMSBANKCODE = BankDetails.get(i).get(0).toString();

			// Set Interface DB Properties
			INFDBProperties.set(6, BankDetails.get(i).get(3).toString());
			INFDBProperties.set(7, BankDetails.get(i).get(4).toString());
			INFDBProperties.set(5, BankDetails.get(i).get(5).toString());

			// Set CBS DB Properties
			CBSDBProperties.set(6, BankDetails.get(i).get(6).toString());
			CBSDBProperties.set(7, BankDetails.get(i).get(7).toString());
			CBSDBProperties.set(5, BankDetails.get(i).get(8).toString());

			if (TestBankCon(INFDBProperties,CBSDBProperties)) {
			
			List<List<Object>> FilePaths = SAV.getOraData(DBProperties, InitVar.getFILEPATHS(PFMSBANKCODE));
			for (int k = 0; k < FilePaths.size(); k++) {
				String REQFILPATH = FilePaths.get(k).get(2).toString();
				String REQFILPATH_FAIL = FilePaths.get(k).get(3).toString();
				String REQFILPATH_ARCH = FilePaths.get(k).get(4).toString();
				String RESFILPATH = FilePaths.get(k).get(5).toString();
				String RESFILPATH_ARCH = FilePaths.get(k).get(6).toString();

				if (!REQFILPATH.equals("NA") && !REQFILPATH_FAIL.equals("NA") && !REQFILPATH_ARCH.equals("NA")
						&& !RESFILPATH.equals("NA") && !RESFILPATH_ARCH.equals("NA")) {

					List<String> FileList = FindFiles(REQFILPATH, PFMSBANKCODE + "*AVREQ*.xml", OSTYPE,
							DBProperties.get(10));
					WrLog("[ NO OF FILES FOUND::" + FileList.size() + " ]", "S");
					for (int f = 0; f < FileList.size(); f++) {
						WrLog("[ FILE::" + FileList.get(f) + " ]", "S");
						if (SAV.CheckProcInInfDB(INFDBProperties)) {
							if (FileExist(XSLFile)) {

								String[] ReqFilRec = ReadXMLFile(FileList.get(f));
								long FilCntAftrRead = ReqFilRec.length;

								long FilCntInReqFil = -1;
								long FilCntAftrUpd = -1;
								long InsrtRecCnt = -1;
								long AccValResCnt = -1;

								if (FilCntAftrRead > 0) {
									FilCntInReqFil = Long.parseLong(ReqFilRec[0].split("\\|")[5]);
								}

								if (FilCntAftrRead == FilCntInReqFil) {

									String[] InsReqFilRec = SAV.BulkPush2DB(INFDBProperties, ReqFilRec, "INS");
									ReqFilRec=null;
									//System.out.println(" -- After Insert --");
									//Arrays.stream(InsReqFilRec).forEach(System.out::println);
									//System.out.println(" ++ After Insert ++");
									String[] Qrys = new String[InsReqFilRec.length];
									InsrtRecCnt = InsReqFilRec.length;

									if (FilCntAftrRead == InsrtRecCnt) {
										WrLog("[ FILE COUNT AFTER READ::" + FilCntAftrRead + " RECORD COUNT IN FILE::"
												+ FilCntInReqFil + " INSERTED RECORD COUNT::" + InsrtRecCnt + " ]",
												"I");
	
										for (int arr = 0; arr < InsReqFilRec.length; arr++) {
											if (InsReqFilRec[arr].split("\\|").length >= 9) {
												Qrys[arr] = InitVar.getCBSQUERY(FmtAcctNo(InsReqFilRec[arr].split("\\|")[7]),
														InsReqFilRec[arr].split("\\|")[3],
														InsReqFilRec[arr].split("\\|")[9]);
											}

										}
										List<String> ValRes = SAV.getBulkData(CBSDBProperties, Qrys);
										Qrys=null;
										//System.out.println(" -- After CBS Response --");
										//ValRes.forEach(System.out::println);
										//System.out.println(" ++ After CBS Response ++");
										String[] AccValRes = ValRes.toArray(new String[ValRes.size()]);
										AccValResCnt = AccValRes.length;

										if (AccValResCnt == InsrtRecCnt) {

											String[] UpdateRec = new String[InsReqFilRec.length];
											for (int r = 0; r < InsReqFilRec.length; r++) {
												
												String PaddedInsAcct = FmtAcctNo(InsReqFilRec[r].split("\\|")[7]);
												String PaddedValAcct = FmtAcctNo(AccValRes[r].split("\\|")[0]);
												
												if (PaddedInsAcct.equalsIgnoreCase(PaddedValAcct)) {

													UpdateRec[r] = InsReqFilRec[r].split("\\|")[0] + "|"
															+ InsReqFilRec[r].split("\\|")[3] + "|"
															+ InsReqFilRec[r].split("\\|")[5] + "|"
															+ InsReqFilRec[r].split("\\|")[7] + "|"
															+ InsReqFilRec[r].split("\\|")[8] + "|"
															+ InsReqFilRec[r].split("\\|")[9] + "|"
															+ InsReqFilRec[r].split("\\|")[10] + "|"
															+ AccValRes[r].replace("||", "|");
												}

											}
											//System.out.println("-------Before--------");
											//Arrays.stream(UpdateRec).forEach(System.out::println);
											String[] UpdReqRec = SAV.BulkPush2DB(INFDBProperties, UpdateRec, "UPD");
											UpdateRec=null;
											//System.out.println("-------After--------");
											//Arrays.stream(UpdReqRec).forEach(System.out::println);

											FilCntAftrUpd = UpdReqRec.length;

											if (FilCntAftrRead == FilCntAftrUpd) {
												if (WriteXMLFile(RESFILPATH, InsReqFilRec[0], UpdReqRec)) {
													RetTyp = true;
													SAV.putOraData(DBProperties,InitVar.getLOGQRY2MASTERDB(UpdReqRec[0].split("\\|")[1],
																	UpdReqRec[0].split("\\|")[0],
																	UpdReqRec[0].split("\\|")[2],
																	UpdReqRec[0].split("\\|")[39]));
												}
												// -- END OF EXECUTION OF RUNNER
												InsReqFilRec=null;
												UpdReqRec=null;
											
										// -- ELSE PART OF AFTER CBS VALIDATION
											} else {
												WrLog("[ FILE COUNT AFTER READ::" + FilCntAftrRead +
													  " RECORD COUNT IN FILE::" + FilCntInReqFil +
													  " RECORD RECEIVED AFTER CBS VALIDATION::" + AccValResCnt +
													  " UPDATED RECORD COUNT::" + FilCntAftrUpd + " ]", "W");
												WrLog("[ Readed Record Count and Updated Record Count Doesn't Match ! ]","W");
												RetTyp = false;
											}
											// -- ELSE PART OF AFTER CBS VALIDATION
											} else {
												WrLog("[ FILE COUNT AFTER READ::" + FilCntAftrRead +
													  " RECORD RECEIVED AFTER CBS VALIDATION::" + AccValResCnt + " ]", "W");
												WrLog("[ Readed Record Count and CBS Validated Record Count Doesn't Match ! ]", "W");
												RetTyp = false;
											}

									} else {
										WrLog("[ FILE COUNT AFTER READ::" + FilCntAftrRead +
											  " RECORD COUNT IN FILE::"+ FilCntInReqFil +
											  " INSERTED RECORD COUNT::" + InsrtRecCnt + " ]","W");
										WrLog("[ Readed Record Count and Inserted Record Count Doesn't Match ! ]", "W");
										RetTyp = false;
									}

									// ---- File Movement Between Internal Directories -----//
									if (RetTyp == true) {

										if (FileExist(RESFILPATH + File.separator.toString() + RespFileName)) {
											File_CtrlCorV("cp", RESFILPATH + File.separator.toString() + RespFileName,
													RESFILPATH_ARCH);
										} else {
											System.out.println("File::" + RESFILPATH + File.separator.toString() + RespFileName + " Not Exist!");
										}
										File_CtrlCorV("mv", FileList.get(f), REQFILPATH_ARCH);

									} else {

										File_CtrlCorV("mv", FileList.get(f), REQFILPATH_FAIL);

									}
									// ----------------------------------------------------//
									// ---------------Zip Files In Directory---------------//

									if (FileExist(REQFILPATH_FAIL)) {
										zipOlderFiles(REQFILPATH_FAIL);
									}
									if (FileExist(REQFILPATH_ARCH)) {
										zipOlderFiles(REQFILPATH_ARCH);
									}
									if (FileExist(RESFILPATH_ARCH)) {
										zipOlderFiles(RESFILPATH_ARCH);
									}
									if (FileExist("Logs")) {
										zipOlderFiles("Logs");
									}

									// ----------------------------------------------------//

								} else {
									WrLog("[ Readed Record Count and File Record Count Doesn't Match ! ]", "W");
									File_CtrlCorV("mv", FileList.get(f), REQFILPATH_FAIL);
									RetTyp = false;
								}

							} else {
								WrLog("[ Parser File " + XSLFile + " Not Found ! ]", "W");
								RetTyp = false;
								break;
							}

						} else {
							WrLog("[ Procedure::SINGLE_AV_INS_UPD_PROC Not Found in Interface Database ! ]", "W");
							RetTyp = false;
							break;
						}
					} // -- FILE LOOP END

				} else {
					WrLog("No Request File Path Found in Database !", "W");
				}

			}
			
			}

		} // -- BANKS LOOP END;
		
		

		return RetTyp;
	}

	public static ArrayList<String> ReadConfig(String PropPath) {
		StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
		encryptor.setPassword("PFMS-SINGLE-ACCOUNT-VALIDATION");

		File ConfigDir = new File(PropPath + "Config/");
		File PropFile = new File(PropPath + "Config/config.properties");
		ArrayList<String> DBPropArrList = new ArrayList<String>();

		if (ConfigDir.isDirectory()) {
			if (PropFile.isFile()) {
				try {
					FileReader PropFileReader = new FileReader(PropFile);
					Properties DBProp = new Properties();
					DBProp.load(PropFileReader);
					DBPropArrList.addAll(Arrays.asList(DBProp.getProperty("Enable"),
							DBProp.getProperty("WaitTime"),
							DBProp.getProperty("StartTime"),
							DBProp.getProperty("EndTime"),
							encryptor.decrypt(DBProp.getProperty("Driver")),
							encryptor.decrypt(DBProp.getProperty("URL")),
							encryptor.decrypt(DBProp.getProperty("UserName")),
							encryptor.decrypt(DBProp.getProperty("Password")),
							DBProp.getProperty("BankGroup"),
							DBProp.getProperty("BankSubGroup"),
							DBProp.getProperty("FileLimit"),
							DBProp.getProperty("XMLParserFile"),
							DBProp.getProperty("InfProcFile")));
				} catch (Exception e) {
					System.out.println("Error while reading : ==>'db.properties'");
					WrLog("Error while reading : ==>'db.properties'", "W");
				}
			}
		} else {
			System.out.println("There is no directory: ==>" + ConfigDir);
			System.out.println("Create Diretory: '" + ConfigDir + "' & add property file: 'db.properties'");
			WrLog("There is no directory: ==>" + ConfigDir, "W");
			WrLog("Create Diretory: '" + ConfigDir + "' & add property file: 'db.properties'", "W");
		}

		return DBPropArrList;

	}

	public static boolean getCount(ArrayList<String> Config) {
		int cnt = 0;
		boolean ret = false;
		for (int i = 0; i < Config.size(); i++) {
			boolean res = (Config.get(i) == null || Config.get(i).length() == 0);
			if (!res) {
				cnt++;
			}
		}
		if (Config.size() == cnt) {
			ret = true;
		}
		return ret;
	}

	@SuppressWarnings("deprecation")
	public static boolean TimeIntChk(String ExeFromDate, String ExeToDate) {
		boolean stat = false;

		SimpleDateFormat SDF = new SimpleDateFormat("kk:mm:ss");
		SimpleDateFormat CSDF = new SimpleDateFormat("ddMMyyyy kk:mm:ss");

		Date ExeFTime = new Date();
		Date ExeTTime = new Date();
		Date CurrDate = new Date();

		try {
			ExeFTime = SDF.parse(ExeFromDate + ":00");
			ExeTTime = SDF.parse(ExeToDate + ":00");
			CurrDate = CSDF.parse(
					"01011970 " + CurrDate.getHours() + ":" + CurrDate.getMinutes() + ":" + CurrDate.getSeconds());
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(e);
		}

		if (CurrDate.after(ExeFTime) && CurrDate.before(ExeTTime)) {
			stat = true;
			System.out.println("Current Time : " + CurrDate.toString());
			WrLog("Current Time : " + CurrDate.toString(), "I");
		}

		return stat;
	}

	public List<List<Object>> getOraData(ArrayList<String> DBProperties, String Query) {
		Connection OraCon = null;
		boolean CloseConReq=false;
		List<List<Object>> output = new ArrayList<List<Object>>();
		try {
			Class.forName(DBProperties.get(4));
			String URL = DBProperties.get(5);
			String UserName = DBProperties.get(6);
			String Password = DBProperties.get(7);
			OraCon = DriverManager.getConnection(URL, UserName, Password);
			if (OraCon != null) {
				// System.out.println("Connected with URL: ==>"+URL);
				Statement OraSt = OraCon.createStatement();
				ResultSet OraRS = OraSt.executeQuery(Query);

				output = readRows(OraRS);

			}
		} catch (Exception e) {
			e.printStackTrace();
			WrLog("[ " + e + " ] [ " + Query + " ]", "W");
			CloseConReq=OraConCloseReq(e.toString());
		} finally {
			try {
				if(!CloseConReq)
				{
				if (!OraCon.isClosed()) {
					OraCon.close();
				}
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		return output;
	}

	public boolean putOraData(ArrayList<String> DBProperties, String Query) {
		boolean retStat = false;
		boolean CloseConReq=false;
		Connection OraCon = null;
		try {
			Class.forName(DBProperties.get(4));
			String URL = DBProperties.get(5);
			String UserName = DBProperties.get(6);
			String Password = DBProperties.get(7);
			OraCon = DriverManager.getConnection(URL, UserName, Password);
			Statement OraStmt = OraCon.createStatement();
			if (OraCon != null) {
				System.out.println("| Injecting with DML Statement |");
				// WrLog("Injecting with DML Statement: " + Query, "I");
				OraStmt.executeUpdate(Query);
				retStat = true;
			}
		} catch (Exception e) {
			System.out.println("[ " + e + " ] [ " + Query + " ]");
			WrLog("[ " + e + " ] [ " + Query + " ]", "W");
			retStat = false;
			CloseConReq=OraConCloseReq(e.toString());
		} finally {
			try {
				if(!CloseConReq)
				{
				if (!OraCon.isClosed()) {
					OraCon.close();
				}
				}
			} catch (SQLException e) {
				e.printStackTrace();
				WrLog("[ " + e + " ] ", "W");
			}
		}

		return retStat;
	}

	public List<String> getBulkData(ArrayList<String> DBProperties, String[] Querys) {
		Instant stDate = Instant.now();
		Connection OraCon = null;
		boolean CloseConReq=false;
		List<String> output = new ArrayList<String>();
		try {
			Class.forName(DBProperties.get(4));
			String URL = DBProperties.get(5);
			String UserName = DBProperties.get(6);
			String Password = DBProperties.get(7);
			OraCon = DriverManager.getConnection(URL, UserName, Password);
			if (OraCon != null) {
				// System.out.println("Connected with URL: ==>"+URL);
				Statement OraSt = OraCon.createStatement();

				for (String Query : Querys) {
					try {
						ResultSet OraRS = OraSt.executeQuery(Query);
						while (OraRS.next()) {
							output.add(OraRS.getString(1).toString());
						}
					} catch (Exception e) {
						WrLog("[ " + e + " ] [ " + Query + " ]", "W");
						e.printStackTrace();
					}
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
			WrLog("[ " + e + " ]", "W");
			CloseConReq=OraConCloseReq(e.toString());
			
		} finally {
			try {
				if(!CloseConReq)
				{
				if (!OraCon.isClosed()) {
					OraCon.close();
				}
				}
			} catch (SQLException e) {
				e.printStackTrace();
				
			}
		}

		Instant edDate = Instant.now();
		WrLog("[ TOTAL TIME TAKEN FOR GET DATA FROM CBS ::" + TimeDiff(stDate, edDate) + " ! ]", "S");

		output.removeIf(Objects::isNull);
		return output;
	}

	public String[] BulkPush2DB(ArrayList<String> DBProperties, String[] Records, String OprMode) {

		Instant stDate = Instant.now();
		String[] Result = null;
		boolean CloseConReq=false;
		Connection OraCon = null;
		String Query = "";
		try {
			Class.forName(DBProperties.get(4));
			String URL = DBProperties.get(5);
			String UserName = DBProperties.get(6);
			String Password = DBProperties.get(7);
			OraCon = DriverManager.getConnection(URL, UserName, Password);
			if (OraCon != null) {

				// Convert the array to an SQL array
				Array array = ((OracleConnection) OraCon).createOracleArray("STRING_ARRAY", Records);

				// Prepare the SQL statement with the array parameter
				Query = "{ call SINGLE_AV_INS_UPD_PROC(?,?,?) }";
				CallableStatement Stmt = OraCon.prepareCall(Query);
				Stmt.setArray(1, array);
				Stmt.setString(2, OprMode);
				Stmt.registerOutParameter(3, OracleTypes.ARRAY, "STRING_ARRAY");
				// Execute the procedure
				Stmt.execute();
				Array resultArray = Stmt.getArray(3);
				Result = (String[]) resultArray.getArray();
				// Close resources
				Stmt.close();
				OraCon.close();

			}
		} catch (Exception e) {
			System.out.println("[ " + e + " ] [ " + Query + " ]");
			WrLog("[ " + e + " ] [ " + Query + " ]", "W");
			CloseConReq=OraConCloseReq(e.toString());
		} finally {
			try {
				if(!CloseConReq)
				{
				if (!OraCon.isClosed()) {
					OraCon.close();
				}
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		Instant edDate = Instant.now();
		WrLog("[ TOTAL TIME TAKEN FOR " + OprMode + " IN INF DB::" + TimeDiff(stDate, edDate) + " ! ]", "S");

		return Result;
	}

	public boolean CheckProcInInfDB(ArrayList<String> DBProperties) {

		boolean RetStr = false;
		Connection OraCon = null;
		boolean CloseConReq=false;

		List<List<Object>> OBJSTATUS = getOraData(DBProperties, InitVar.getOBJSTATUS("SINGLE_AV_INS_UPD_PROC"));
		String INFPROCSQLFile = DBProperties.get(12);
		String STATUS = " ";

		for (int s = 0; s < OBJSTATUS.size(); s++) {
			STATUS = OBJSTATUS.get(s).get(0).toString();
		}
		if (!STATUS.equalsIgnoreCase("VALID")) {
			if (FileExist(INFPROCSQLFile)) {
				try {
					Class.forName(DBProperties.get(4));
					String URL = DBProperties.get(5);
					String UserName = DBProperties.get(6);
					String Password = DBProperties.get(7);

					File sqlFile = new File(INFPROCSQLFile);
					BufferedReader reader = new BufferedReader(new FileReader(sqlFile));
					OraCon = DriverManager.getConnection(URL, UserName, Password);
					Statement stmt = OraCon.createStatement();
					String line;
					StringBuffer sb = new StringBuffer();
					while ((line = reader.readLine()) != null) {
						if (!line.equalsIgnoreCase("--")) {
							sb.append(line + "\r\n");
						}
						if (line.equalsIgnoreCase("--")) {
							String sql = sb.toString();
							// System.out.println(sql);
							stmt.executeUpdate(sql);
							sb.setLength(0);
						}
					}
					reader.close();

					stmt.close();
					OraCon.close();
					RetStr = true;
				} catch (Exception e) {
					e.printStackTrace();
					CloseConReq=OraConCloseReq(e.toString());
				} finally {
					try {
						if(!CloseConReq)
						{
						if (!OraCon.isClosed()) {
							OraCon.close();
						}
						}
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}

			} else {
				WrLog("[ FILE::" + INFPROCSQLFile + " NOT FOUND ! ]", "S");
				RetStr = false;
			}
		} else {
			RetStr = true;
		}

		return RetStr;
	}

	private List<List<Object>> readRows(ResultSet rs) throws SQLException {
		ImmutableList.Builder<List<Object>> rows = ImmutableList.builder();
		int columnCount = rs.getMetaData().getColumnCount();
		while (rs.next()) {
			List<Object> row = new ArrayList<>();
			for (int i = 1; i <= columnCount; i++) {
				row.add(rs.getObject(i));
			}
			rows.add(row);
		}
		return rows.build();
	}
	
	public boolean TestBankCon(ArrayList<String> INFDBProperties,ArrayList<String> CBSDBProperties) {
		boolean retStr = false;
		SingleAccountValidation SAV = new SingleAccountValidation();
		
		List<List<Object>> InfTestRes= new ArrayList<List<Object>>();
		List<List<Object>> CBSTestRes= new ArrayList<List<Object>>();
		
		try {
			InfTestRes  = SAV.getOraData(INFDBProperties, InitVar.getTESTCONN());
		}
		catch(Exception e)
		{
			InfTestRes= new ArrayList<List<Object>>();
		}
		
		try {
			CBSTestRes  = SAV.getOraData(CBSDBProperties, InitVar.getTESTCONN());
		}
		catch(Exception e)
		{
			CBSTestRes= new ArrayList<List<Object>>();
		}

		if (InfTestRes.size()==0) {
			WrLog("[ NO CONNECTIVITY WITH INTERFACE DATABASE ! ]", "W");
		}
		if (CBSTestRes.size()==0) {
			WrLog("[ NO CONNECTIVITY WITH CBS DATABASE ! ]", "W");
		}
		
		if (InfTestRes.size()>0 && CBSTestRes.size()>=0)
		{
			retStr=true;
		}
		
		return retStr;
	}

	public static void WrLog(String Message, String TYP) {

		boolean append = true;
		FileHandler handler = null;

		Date CurrDate = new Date();
		String FMTDate = new SimpleDateFormat("yyyyMMdd").format(CurrDate);

		try {
			File FWDir = new File(ABSPATH + "Logs/");

			if (!FWDir.exists()) {
				FWDir.mkdirs();
			}
			String DecodePath = URLDecoder.decode(ABSPATH + "Logs/SINGLE-ACCOUNT-VALIDATION-" + FMTDate + ".log",
					"UTF-8");
			handler = new FileHandler(DecodePath, append);
			Logger logger = Logger.getLogger("");

			handler.setFormatter(new SimpleFormatter() {
				private static final String format = "[%1$tF %1$tT] [%2$-7s] %3$s %n";

				@Override
				public synchronized String format(LogRecord lr) {
					return String.format(format, new Date(lr.getMillis()), lr.getLevel().getLocalizedName(),
							lr.getMessage());
				}
			});

			logger.addHandler(handler);

			switch (TYP) {
			case "S":
				logger.severe(colorize(Message, BOLD(), BLUE_TEXT()));
				break;
			case "W":
				logger.warning(colorize(Message, BOLD(), RED_TEXT()));
				break;
			case "I":
				logger.info(colorize(Message, BOLD(), GREEN_TEXT()));
				break;
			case "C":
				logger.config(colorize(Message, BOLD(), YELLOW_TEXT()));
				break;
			case "F":
				logger.fine(colorize(Message, BOLD(), CYAN_TEXT()));
				break;
			case "F+":
				logger.finer(colorize(Message, BOLD(), MAGENTA_TEXT()));
				break;
			case "F++":
				logger.finest(colorize(Message, BOLD(), BRIGHT_WHITE_TEXT()));
			default:
				System.out.println("Unable to Write Log.");
				break;
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (handler != null) {
				try {
					handler.close();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}
	}

	public List<String> FindFiles(String SearchPath, String FilePattern, String OSTYP, String FileReadLimit) {

		long CNTR = 0;
		long Limit = Integer.parseInt(FileReadLimit);
		List<String> FileList = new ArrayList<String>();

		if (OSTYP.equalsIgnoreCase("LINUX") || OSTYP.equalsIgnoreCase("AIX"))

			try {
				ProcessBuilder processBuilder = new ProcessBuilder();
				if (OSTYP.equalsIgnoreCase("LINUX")) {
					// Define the command to execute
					String[] command = { "find", SearchPath, "-iname", FilePattern, "-type", "f", "-mmin", "+1",
							"-maxdepth", "1" };
					// Create a ProcessBuilder and start the process
					processBuilder = new ProcessBuilder(command);
				} else if (OSTYP.equalsIgnoreCase("AIX")) {
					String[] command = { "find", SearchPath, "-name", FilePattern, "-type", "f" };
					// Create a ProcessBuilder and start the process
					processBuilder = new ProcessBuilder(command);
				}
				// Starting Process
				Process process = processBuilder.start();
				// Read the command's output
				InputStream inputStream = process.getInputStream();
				BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
				String line;
				try {
					while ((line = reader.readLine()) != null) {
						if (CNTR < Limit) {
							FileList.add(line);
						}
						CNTR++;
					}
				} catch (Exception e) {
					WrLog("[ " + e + " ]", "W");
				}
				// Wait for the process to complete
				int exitCode = process.waitFor();

				// Check if the command was successful
				if (exitCode != 0) {
					System.err.println("Error executing the find command. Exit code: " + exitCode);
					WrLog("[ " + "Error executing the find command. Exit code: " + exitCode + " ]", "W");
				}
			} catch (IOException | InterruptedException e) {
				e.printStackTrace();
				WrLog("[ " + e + " ]", "W");
			}

		else if (OSTYP.equalsIgnoreCase("WIN")) {
			Path dir = Paths.get(SearchPath);

			if (Files.exists(dir) && Files.isDirectory(dir)) {
				try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
					for (Path file : stream) {

						Pattern pattern = Pattern.compile(".xml", Pattern.CASE_INSENSITIVE);
						Matcher matcher = pattern.matcher(file.getFileName().toString());
						boolean matchFound = matcher.find();
						if (matchFound) {
							if (CNTR < Limit) {
								FileList.add(SearchPath.toString() + File.separator + file.getFileName());
								CNTR++;
							}
						}

					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else {
				System.out.println("The specified directory does not exist.");
			}
		} else {
			System.out.println("Matching code not found for the OS !");
		}

		return FileList;
	}

    public String[] ReadXMLFile(String XMLFile) {

    	List<String> CBMAVREQ = new ArrayList<>();
    	
    	String Tag="";
    	String MessageId="";
    	String Source="";
    	String Destination="";
    	String BankCode="";
    	String BankName="";
    	String RecordsCount="";
    	String xmlns="http://pfms.com/AccountValidationRequest";
    	String AccountNumber="";
    	String EntityCode="";
    	String DataRequired="";
    		

        try (InputStream inputStream = new FileInputStream(XMLFile)) {
            XMLInputFactory inputFactory = XMLInputFactory.newInstance();
            XMLStreamReader xmlReader = inputFactory.createXMLStreamReader(inputStream);

            while (xmlReader.hasNext()) {
                int event = xmlReader.next();

                switch (event) {
                    case XMLStreamConstants.START_ELEMENT:

                        if (xmlReader.getLocalName().equalsIgnoreCase("Accounts"))
                        {
                        	int attributeCount = xmlReader.getAttributeCount();
                            for (int i = 0; i < attributeCount; i++) {
                            	switch (xmlReader.getAttributeLocalName(i).trim())
                            	{
                            		case "MessageId" :
                            			MessageId=xmlReader.getAttributeValue(i);
                            			break;
                            		case "Source" :
                            			Source=xmlReader.getAttributeValue(i);
                            			break;
                            		case "Destination" :
                            			Destination=xmlReader.getAttributeValue(i);
                            			break;
                            		case "BankCode" :
                            			BankCode=xmlReader.getAttributeValue(i);
                            			break;
                            		case "BankName" :
                            			BankName=xmlReader.getAttributeValue(i);
                            			break;
                            		case "RecordsCount" :
                            			RecordsCount=xmlReader.getAttributeValue(i);
                            			break;
                            		
                            		default:
                            	}
                            	
                            }

                        }
                        Tag=xmlReader.getLocalName();
                        break;
                    case XMLStreamConstants.CHARACTERS:
                        String text = xmlReader.getText().trim();
                        if (!text.isEmpty()) {

                        	switch (Tag)
                        	{
                        	case "AccountNumber":
                        		AccountNumber=text;
                        		break;
                        	case "EntityCode":
                        		EntityCode=text;
                        		break;
                        	case "DataRequired":
                        		DataRequired=text;
                        		break;
                        	default:
                        		System.out.println("---");
                        	}
          
                        }
                        break;
                    case XMLStreamConstants.END_ELEMENT:
                        if (xmlReader.getLocalName().equalsIgnoreCase("Account"))
                        {
                        	
                        	CBMAVREQ.add(MessageId + "|" +
                        			Source + "|" +
                        			Destination + "|" +
                        			BankCode + "|" +
                        			BankName + "|" +
                        			RecordsCount + "|" +
                        			xmlns + "|" +
                        			AccountNumber + "|" +
                        			EntityCode + "|" +
                        			DataRequired);
                        }

                        break;
                    default:

                        break;
                }
            }

            xmlReader.close();
        } catch (XMLStreamException | java.io.IOException e) {
            e.printStackTrace();
        }
        return CBMAVREQ.toArray(new String[CBMAVREQ.size()]);
    }

	public boolean WriteXMLFile(String FileWritePath, String HeaderData, String[] AccountData) {
		boolean retStr = false;
		RespFileName = HeaderData.split("\\|")[0].subSequence(0, 8) + "RES" + HeaderData.split("\\|")[10] + ".xml";

		if (FileExist(FileWritePath)) {
			String XMLFilePath = FileWritePath + File.separator.toString() + RespFileName; // Replace with the path to
																							// your file
			try (BufferedWriter writer = new BufferedWriter(new FileWriter(XMLFilePath))) {
				
				String XMLACCOUNTSOPENTAG=InitVar.getXMLACCOUNTSOPENTAG(HeaderData);
				if (! XMLACCOUNTSOPENTAG.equals(" ")){
				writer.write(XMLACCOUNTSOPENTAG);
				}
				for (String AccData : AccountData) {
					//WrLog("[ File::" + AccData +" ]", "I");
					String XMLACCOUNTTAG=InitVar.getXMLACCOUNTTAG(AccData);
					if (! XMLACCOUNTTAG.equals(" ")){
					writer.write(XMLACCOUNTTAG);
					}
					else
					{
						break;
					}
				}
				String XMLACCOUNTSCLOSETAG=InitVar.getXMLACCOUNTSCLOSETAG();
				if (! XMLACCOUNTSCLOSETAG.equals(" ")){
				writer.write(XMLACCOUNTSCLOSETAG);
				retStr = true;
				WrLog("[ File ::" + RespFileName + " Has Been Written Successfully. ]", "I");
				}

			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			WrLog("[ Directory ::" + FileWritePath + " Does Not Exist! ]", "W");
		}
		return retStr;
	}

	public boolean FileExist(String FileName) {
		boolean RetFlg = false;
		// Getting the file
		File f = new File(FileName);
		if (f.exists()) {
			RetFlg = true;
		} else {
			RetFlg = false;
		}
		return RetFlg;
	}
	
	public String FmtAcctNo(String AcctNo)
	{
		BigInteger bigInteger = new BigInteger(AcctNo.replaceAll("[^0-9]", ""));
		String PaddedValAcct=String.format("%050d",bigInteger);
		int strlen=PaddedValAcct.length();
		String Result=PaddedValAcct.substring(strlen-17, strlen);
		return Result;
	}

	public String TimeDiff(Instant D1, Instant D2) {
		Duration duration = Duration.between(D1, D2);
		long seconds = duration.getSeconds();
		long nanoseconds = duration.getNano();
		return " SECONDS::" + seconds + " [ IN NANO SECONDS::" + nanoseconds + " ] ";

	}

	public boolean File_CtrlCorV(String Cmd, String FromFile, String ToPath) {

		boolean retStr = false;
		if (OSTYPE.equalsIgnoreCase("LINUX") || OSTYPE.equalsIgnoreCase("AIX")) {
			if (Cmd.equalsIgnoreCase("mv") || Cmd.equalsIgnoreCase("cp")) {
				if (FileExist(FromFile) && FileExist(ToPath)) {
					try {
						ProcessBuilder processBuilder = new ProcessBuilder(Cmd, FromFile, ToPath);
						Process process = processBuilder.start();

						int exitCode = process.waitFor();
						if (exitCode == 0) {
							WrLog("[ File ::" + FromFile + " Destination Dir::" + ToPath + " Command::" + Cmd
									+ " Successfully Executed. ]", "I");
							retStr = true;
						} else {
							WrLog("[ File ::" + FromFile + " Command::" + Cmd + " Failed With Exit Code! ]", "W");
							System.out.println(Cmd+" "+FromFile+" "+ToPath);
						}
					} catch (IOException | InterruptedException e) {
						e.printStackTrace();
					}
				} else {
					WrLog("[ File ::" + FromFile + " Directory::" + ToPath + " Doesn't Exist. ]", "W");
				}

			}
		}
		return retStr;
	}

	public boolean zipOlderFiles(String SearchPath) {
		boolean retStr = false;
		if (OSTYPE.equalsIgnoreCase("LINUX")) {

			try {
				ProcessBuilder processBuilder = new ProcessBuilder();
				// Define the command to execute
				String[] command = { "find", SearchPath, "-type", "f", "-not", "-iname", "*.gz", "-mtime", "+1","-exec", "gzip", "{}", "+" };
				// Create a ProcessBuilder and start the process
				processBuilder = new ProcessBuilder(command);
				// Starting Process
				Process process = processBuilder.start();
				// Read the command's output
				InputStream inputStream = process.getInputStream();
				BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
				String line;
				try {
					while ((line = reader.readLine()) != null) {
						WrLog("[ " + line + " ]", "S");
					}
				} catch (Exception e) {
					WrLog("[ " + e + " ]", "W");
				}
				// Wait for the process to complete
				int exitCode = process.waitFor();

				// Check if the command was successful
				if (exitCode != 0) {
					System.err.println("Error executing the find command. Exit code: " + exitCode);
					WrLog("[ " + "Error executing the find command. Exit code: " + exitCode + " ]", "W");
					Arrays.stream(command).forEach(System.out::print);
				}
			} catch (IOException | InterruptedException e) {
				e.printStackTrace();
				WrLog("[ " + e + " ]", "W");
			}

			retStr = true;
		}
		return retStr;

	}
	
	   public boolean OraConCloseReq(String OraError)
	   {
		   boolean retVal=false;
		   List<String> OraErrList = new ArrayList<String>();
		   OraErrList.addAll(Arrays.asList(" ORA-03113"," ORA-12154"," ORA-12170"," ORA-12500"," ORA-12514"," ORA-12520"," ORA-12521"," ORA-12525"," ORA-12533"," ORA-12540"," ORA-12541"," ORA-12549"," ORA-12560"," ORA-2800"," ORA-01917"," ORA-12505"," ORA-01017"," ORA-12170"," ORA-12560"));
		   for (String OraErr:OraErrList)
		   {
			   if (OraError.toString().split(":")[1].contains(OraErr)) { retVal=true; }
		   }
		   return retVal;
	   }
	   
		public static boolean ISNONE(String STR) {
			boolean RetVal=false;
			if (STR != null && ! STR.isEmpty() && ! STR.trim().isEmpty()) {
				RetVal=true;
			}
			return RetVal;
		}

}