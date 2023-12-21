package cedge.pfms.av;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CodeSample3 {

	public static void main(String[] args) {
		
		CodeSample3 CS = new CodeSample3();
		
		System.out.println(CS.OraConCloseReq("java error: ORA-12170 : Error"));
		
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

}
