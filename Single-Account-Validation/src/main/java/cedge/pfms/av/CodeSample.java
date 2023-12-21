package cedge.pfms.av;

import java.math.BigInteger;

public class CodeSample {
    public static void main(String[] args) throws Exception {

    	System.out.println(FmtAcctNo("0000010101000222201201011000"));
    }
    
	public static String FmtAcctNo(String AcctNo)
	{
		BigInteger bigInteger = new BigInteger(AcctNo.replaceAll("[^0-9]", ""));
		String PaddedValAcct=String.format("%050d",bigInteger);
		int strlen=PaddedValAcct.length();
		String Result=PaddedValAcct.substring(strlen-17, strlen);
		return Result;
	}
}
