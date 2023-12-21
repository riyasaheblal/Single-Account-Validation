package cedge.pfms.av;

import java.time.Year;

public class InitVar {
	
	private static String DECODEPATH;
	private static String BANKLIST;
	private static String FILEPATHS;
	private static String CBSQUERY;
	private static String XMLACCOUNTSOPENTAG;
	private static String XMLACCOUNTTAG;
	private static String XMLACCOUNTSCLOSETAG;
	private static String LOGQRY2MASTERDB;
	private static String OBJSTATUS;
	private static String TESTCONN;

	public static String getDECODEPATH() {
		return DECODEPATH;
	}

	public static void setDECODEPATH(String dECODEPATH) {
		DECODEPATH = dECODEPATH;
	}
	
	public static String getBANKLIST(String BankGroup, String BankSubGroup) {
		BANKLIST="SELECT CPSMSBANKCODE ,BANKCODE,BANKNAME,REGEXP_SUBSTR(CPSMS_USRPWD, '[^/]+', 1, 1) INFUSER,REGEXP_SUBSTR(CPSMS_USRPWD, '[^/]+', 1, 2) INFPWD,'jdbc:oracle:thin:@'||TRIM(REPLACE(CPSMS_SERVICE_DETAILS,'/',':')) INFSERVICE,REGEXP_SUBSTR(CBS_USRPWD, '[^/]+', 1, 1) CBSUSER,REGEXP_SUBSTR(CBS_USRPWD, '[^/]+', 1, 2) CBSPWD,'jdbc:oracle:thin:@'||TRIM(REPLACE(CBS_SERVICE_DETAILS,'/',':')) CBSSERVICE,AV_ENABLE FROM ALL_BANKS WHERE ENABLE='Y' AND OWN_BANK='Y' AND AV_ENABLE='Y' AND BANKGROUP=TRIM('"+BankGroup+"') AND BANKSUBGROUP=TRIM('"+BankSubGroup+"')";
		return BANKLIST;
	}
	
	public static String getTESTCONN() {
		TESTCONN="SELECT * FROM DUAL";
		return TESTCONN;
	}

	public static String getFILEPATHS(String PFMSBankCode) {
		FILEPATHS="SELECT PFMSBANKCODE,APPTYPE,NVL(TRIM(REQFILPATH),'NA') REQFILPATH,NVL(TRIM(REQFILPATH_FAIL),'NA') REQFILPATH_FAIL,NVL(TRIM(REQFILPATH_ARCH),'NA') REQFILPATH_ARCH,NVL(TRIM(RESFILPATH),'NA') RESFILPATH,NVL(TRIM(RESFILPATH_ARCH),'NA') RESFILPATH_ARCH FROM ALL_APPDIR WHERE ENABLE='Y' AND APPTYPE='AVREQ' AND PFMSBANKCODE=TRIM('"+PFMSBankCode+"')";
		return FILEPATHS;
	}

	public static String getCBSQUERY(String AccNo,String BankCd,String DataReq) {
		CBSQUERY="SELECT PFMS_ACCTVAL(TRIM('" + AccNo + "'),TRIM('" + BankCd + "'),TRIM('" + DataReq + "')) FROM DUAL";
		return CBSQUERY;
	}
	
	public static String getLOGQRY2MASTERDB(String BANKCODE,String REQMSGID,String RECCNT,String RESMSGID) {
		LOGQRY2MASTERDB="INSERT INTO AVSUMMARY(BANKCODE, REQMSGID,RECCNT, RESMSGID, CRDATE) VALUES('" + BANKCODE + "','" + REQMSGID + "','" + RECCNT + "','" + RESMSGID + "',SYSTIMESTAMP)";
		return LOGQRY2MASTERDB;
	}
	
	public static String getOBJSTATUS(String OBJ_NAME) {
		OBJSTATUS="SELECT STATUS FROM USER_OBJECTS WHERE OBJECT_NAME=TRIM('" + OBJ_NAME + "') AND OBJECT_TYPE='PROCEDURE'";
		return OBJSTATUS;
	}

	public static String getXMLACCOUNTSOPENTAG(String InpStr) {
		XMLACCOUNTSOPENTAG=" ";
		try 
		{
		String MESSAGEID=InpStr.split("\\|")[0].substring(0, 8)+"RES"+InpStr.split("\\|")[10];
		String BANKCODE=InpStr.split("\\|")[3];
		String BANKNAME=InpStr.split("\\|")[4];
		String RECORDSCOUNT=InpStr.split("\\|")[5];
		String DESTINATION=InpStr.split("\\|")[1];
		String SOURCE=InpStr.split("\\|")[2];
		String XMLNS=InpStr.split("\\|")[6].replace("Request","Response");
		XMLACCOUNTSOPENTAG="<Accounts MessageId=\""+MESSAGEID+"\" BankCode=\""+BANKCODE+"\" BankName=\""+BANKNAME+"\" RecordsCount=\""+RECORDSCOUNT+"\" Destination=\""+DESTINATION+"\" Source=\""+SOURCE+"\" xmlns=\""+XMLNS+"\">\r\n";
		}
		catch(Exception e)
		{
			SingleAccountValidation.WrLog("[ " + e + " ] [ " + InpStr + " ]", "W");
			e.printStackTrace();
		}
		return XMLACCOUNTSOPENTAG;
	}

	public static String getXMLACCOUNTTAG(String InpStr) {
		
		XMLACCOUNTTAG=" ";
		try 
		{

		String MESSAGEID=InpStr.split("\\|")[0];
		String PFMSACCTNO=InpStr.split("\\|")[3];
		String ENTITYCODE=InpStr.split("\\|")[4];
		String VALIDATIONRESULT=InpStr.split("\\|")[8];
		String ACCOUNTVALIDSTATUS=InpStr.split("\\|")[9];
		String ACCOUNTTYPE=InpStr.split("\\|")[10];
		String BSRCODE=InpStr.split("\\|")[11];
		String IFSC=InpStr.split("\\|")[12];
		String OPENING_DATE=InpStr.split("\\|")[13];
		String CLOSING_DATE=InpStr.split("\\|")[14].replace("31/12/1899", " ");
		if (ACCOUNTVALIDSTATUS.equalsIgnoreCase("C") && (! SingleAccountValidation.ISNONE(CLOSING_DATE))) {
			Year CURRYEAR = Year.now();
			CLOSING_DATE=OPENING_DATE.substring(0,6)+CURRYEAR.getValue();
		}
		String TAN=InpStr.split("\\|")[15];
		String PAN=InpStr.split("\\|")[16];
		String PINCODE=InpStr.split("\\|")[17];
		String STATELGDCODE=InpStr.split("\\|")[18];
		String STATE=InpStr.split("\\|")[19];
		String DISTRICTLGDCODE=InpStr.split("\\|")[20];
		String DISTRICT=InpStr.split("\\|")[21];
		String ADDRESS1=InpStr.split("\\|")[22];
		String ADDRESS2=InpStr.split("\\|")[23];
		String TELEPHONENO=InpStr.split("\\|")[24];
		String GENDER=InpStr.split("\\|")[25];
		String CUSTSHORTNAME=InpStr.split("\\|")[26];
		String EMAIL=InpStr.split("\\|")[27];
		String NATIONALITY=InpStr.split("\\|")[28];
		String DOB=InpStr.split("\\|")[29];
		String AHTYPE=InpStr.split("\\|")[30];
		String AHDETAIL1=InpStr.split("\\|")[31];
		String AHDETAIL2=InpStr.split("\\|")[32];
		String AHDETAIL3=InpStr.split("\\|")[33];
		String AHDETAIL4=InpStr.split("\\|")[34];
		String AHOTHDTL1=InpStr.split("\\|")[35];
		String AHOTHDTL2=InpStr.split("\\|")[36];
		String AHOTHDTL3=InpStr.split("\\|")[37];
		String AHOTHDTL4=InpStr.split("\\|")[38];
		
		XMLACCOUNTTAG="<Account>\r\n"
				+ "<ReqMsgId>"+MESSAGEID+"</ReqMsgId>\r\n"
				+ "<AccountNumber>"+PFMSACCTNO+"</AccountNumber>\r\n"
				+ "<AccountValidity>"+VALIDATIONRESULT+"</AccountValidity>\r\n"
				+ "<AccountStatus>"+ACCOUNTVALIDSTATUS+"</AccountStatus>\r\n"
				+ "<AccountType>"+ACCOUNTTYPE+"</AccountType>\r\n"
				+ "<BSRCode>"+BSRCODE+"</BSRCode>\r\n"
				+ "<IFSCCode>"+IFSC+"</IFSCCode>\r\n"
				+ "<AccountOpenDate>"+OPENING_DATE+"</AccountOpenDate>\r\n"
				+ "<AccountCloseDate>"+(CLOSING_DATE == null ? "" : CLOSING_DATE.trim())+"</AccountCloseDate>\r\n"
				+ "<EntityCode>"+ENTITYCODE+"</EntityCode>\r\n"
				+ "<AHDetails>\r\n"
				+ "<AH AHDetail1=\""+(AHDETAIL1 == null ? "" : AHDETAIL1.trim() )+"\" AHDetail2=\""+(AHDETAIL2 == null ? "" : AHDETAIL2.trim() )+"\" AHDetail3=\""+(AHDETAIL3 == null ? "" : AHDETAIL3.trim() )+"\" AHDetail4=\""+(AHDETAIL4 == null ? "" : AHDETAIL4.trim() )+"\" AHTYPE=\""+AHTYPE+"\" AddressLine1=\""+ADDRESS1+"\" AddressLine2=\""+ADDRESS2+"\" DOB=\""+DOB+"\" District=\""+DISTRICT+"\" DistrictLGDCode=\""+DISTRICTLGDCODE+"\" Gender=\""+GENDER+"\" Mobile=\""+TELEPHONENO+"\" Name=\""+CUSTSHORTNAME+"\" Nationality=\""+NATIONALITY+"\" PAN=\""+PAN+"\" PinCode=\""+PINCODE+"\" State=\""+STATE+"\" StateLGDCode=\""+STATELGDCODE+"\" TAN=\""+TAN+"\" emailID=\""+EMAIL+"\"/>\r\n"
				+ "</AHDetails>\r\n"
				+ "<AHOTHDTL1>"+AHOTHDTL1+"</AHOTHDTL1>\r\n"
				+ "<AHOTHDTL2>"+AHOTHDTL2+"</AHOTHDTL2>\r\n"
				+ "<AHOTHDTL3>"+AHOTHDTL3+"</AHOTHDTL3>\r\n"
				+ "<AHOTHDTL4>"+(AHOTHDTL4 == null ? "" : AHOTHDTL4.trim())+"</AHOTHDTL4>\r\n"
				+ "</Account>\r\n";
		}
		catch(Exception e)
		{
			SingleAccountValidation.WrLog("[ " + e + " ] [ " + InpStr + " ]", "W");
			e.printStackTrace();
		}
		
		return XMLACCOUNTTAG;
	}

	public static String getXMLACCOUNTSCLOSETAG() {
		XMLACCOUNTSCLOSETAG="</Accounts>";
		return XMLACCOUNTSCLOSETAG;
	}

}
