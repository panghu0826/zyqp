package Config;

public class ProductConfig {

	public static String GetPriceByProductId(String strProductId)
	{
		return "0.01";
	}
	
	public static String GetWXPriceByProductId(String strProductId)
	{
		return "1";
	}
	
	public static String GetSubjectByProductId(String strProductId)
	{
		return "Subject";
	}
	
	public static String GetBodyByProductId(String strProductId)
	{
		return "BodyContent";
	}
}
