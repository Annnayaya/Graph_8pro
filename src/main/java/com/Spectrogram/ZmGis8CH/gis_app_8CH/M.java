package com.Spectrogram.ZmGis8CH.gis_app_8CH;
import java.io.*;

public class M
{

	static String M[] = new String[1000];

	public M()
	{
	}

	static void ReadMsg(String s)
	{
		try
		{
			BufferedReader bufferedreader = new BufferedReader(new FileReader(s));
			String str;
			while((str = bufferedreader.readLine())!=null)
			{
				String[] as = str.split("[\t]");
				if(as.length == 2)
				{
					int i = Integer.parseInt(as[0]);
					if((i<0) || (M.length<=i))
					{
						System.out.println("idx error. idx=" + i + "(0~" + (M.length - 1) + ")");
					}
					else
					{
						M[i] = as[1];
					}
				}
			}			
			bufferedreader.close();
		}
		catch (Exception exception)
		{
			exception.printStackTrace();
		}
	}

}
