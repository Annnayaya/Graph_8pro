package com.Spectrogram.ZmGis8CH.gis_app_8CH;
import java.io.*;

class Option
{

	static int unitType = 0;
	static int Az = 0;
	static boolean gt = true;//设置默认颜色显示

	static void readOption()
	{
		try
		{
			DataInputStream datainputstream = new DataInputStream(new BufferedInputStream(new FileInputStream("option.dat")));
			unitType = datainputstream.readInt();
			Az = datainputstream.readInt();
			gt = datainputstream.readBoolean();
			datainputstream.close();
		}
		catch (Exception exception)
		{
			unitType = 0;
			Az = 0;
			gt = true;
		}
		if (unitType < 0 || 4 < unitType)
			unitType = 0;
		if(Az < 0 || 3 < Az)
			Az = 0;
	}
	
	static void readOption(int i)
	{
		try
		{
			DataInputStream datainputstream = new DataInputStream(new BufferedInputStream(new FileInputStream("option.dat")));
			unitType = datainputstream.readInt();
			Az = datainputstream.readInt();
			gt = datainputstream.readBoolean();
			datainputstream.close();
		}
		catch (Exception exception)
		{
			unitType = 0;
			Az = 0;
			gt = true;
		}
		if (unitType < 0 || 4 < unitType)
			unitType = 0;
		if(Az < 0 || 3 < Az)
			Az = 0;
	}

	static void writeOption()
	{
		try
		{
			DataOutputStream dataoutputstream = new DataOutputStream(new BufferedOutputStream(new FileOutputStream("option.dat")));
			dataoutputstream.writeInt(unitType);
			dataoutputstream.writeInt(Az);
			dataoutputstream.writeBoolean(gt);
			dataoutputstream.flush();
			dataoutputstream.close();
		}
		catch (Exception exception)
		{
			exception.printStackTrace();
		}
	}
	
	static void writeOption(int i)
	{
		try
		{
			DataOutputStream dataoutputstream = new DataOutputStream(new BufferedOutputStream(new FileOutputStream("option.dat")));
			dataoutputstream.writeInt(i);
			dataoutputstream.writeInt(unitType);
			dataoutputstream.writeInt(Az);
			dataoutputstream.writeBoolean(gt);
			dataoutputstream.flush();
			dataoutputstream.close();
		}
		catch (Exception exception)
		{
			exception.printStackTrace();
		}
	}

}
