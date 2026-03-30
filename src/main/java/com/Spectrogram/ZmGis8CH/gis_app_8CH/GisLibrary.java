package com.Spectrogram.ZmGis8CH.gis_app_8CH;
import java.io.*;

import javax.swing.DefaultListModel;

class GisLibrary extends DefaultListModel<LibFile>
{
	private static final long serialVersionUID = 197307030008L;
	public GisLibrary()
	{
		clear();
		String s = "lib"+File.separator;
		(new File(s)).mkdir();
		String[] as = (new File(s)).list();
		if (as == null)
			return;
		int j = as.length;
		for (int i = 0; i < j; i++)
			if ((as[i].endsWith(".gd1")) || (as[i].endsWith(".gd6")))
				add(size(), new LibFile(as[i]));


	}

	public void loadData(int i, GisGraph gisgraph)
	{
		String s = ((LibFile)get(i)).fileName;
		LibFile libfile = (LibFile)get(i);
		gisgraph.stringToDraw = (new StringBuilder()).append(ChooseLang.M[262]).append(" : ").append(libfile.name).toString();
		for (int j = 0; j < 8; j++)
			gisgraph.resultString[j] = libfile.comment[j];

		File file = new File((new StringBuilder()).append("lib"+File.separator).append(s).toString());
		try
		{
			@SuppressWarnings("resource")
			DataInputStream datainputstream = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));
			for (int l = 0; l < 5120; l++)
				gisgraph.pdData[l] = datainputstream.readShort();
			/*
			int l3 = 5055;
			for (int l1 = 0; l1 < 64;)
			{				
				if(l3 > 5120)
					l3 = 5055;
				gisgraph.columdata[l1++] = gisgraph.pdData[l3++];//柱形图数据
				//l3 += 80;
			}
			*/
			
			//拆分5120个数据成80组比较，为生成柱状图准备
		    int[][] an = new int[80][64];
	    	for(int cycle = 0; cycle < 80; cycle++)
			{
		    	for(int j = 0; j < 64; j++)
		    	{    		
	    			an[cycle][j] = gisgraph.pdData[cycle*64 + j];
	    		}
	    	}
	    	//计算柱形图
	    	for(int kn = 0; kn < 64; kn++)
	    	{
	    		int k3 = 0;
	    		for(int m = 0; m < 80; m++)
	    		{
	    			
	    			int k4 = an[m][kn];
	    			if(k3 < k4)
	        		{
	        			k3 = k4;        			
	        		} 
	    			gisgraph.columdata[kn] = k3;
	    		}
	    	}
			
			for (int k = 0; k < 10240; k++)
				datainputstream.readShort();

			for (int i1 = 0; i1 < 4096; i1++)
				datainputstream.readShort();

			int k1 = datainputstream.readShort();
			if (-90 < k1 && k1 < 0)
			{
				gisgraph.mindBm = k1;
			} else
			{
				for (int j1 = 0; j1 < 5120; j1++)
					if (k1 < gisgraph.pdData[j1])
						gisgraph.pdData[j1] -= k1;
					else
						gisgraph.pdData[j1] = 0;

				gisgraph.mindBm = GisSignal.calMid_dBm;
			}
			gisgraph.maxdBm = GisSignal.calMax_dBm;
			datainputstream.readShort();
			gisgraph.phaseDelay = datainputstream.readShort();
		}
		catch (Exception exception)
		{
			exception.printStackTrace();
		}
	}
	//PRPD
	public void loadData_PRPD(int i, PRPDGraph prpdgraph)
	{
		String s = ((LibFile)get(i)).fileName;
		//LibFile libfile = (LibFile)get(i);
		//gisgraph.stringToDraw = (new StringBuilder()).append(ChooseLang.M[262]).append(" : ").append(libfile.name).toString();
		//for (int j = 0; j < 4; j++)
			//gisgraph.resultString[j] = libfile.comment[j];

		File file = new File((new StringBuilder()).append("lib"+File.separator).append(s).toString());
		try
		{
			@SuppressWarnings("resource")
			DataInputStream datainputstream = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));
			for (int l = 0; l < 320; l++)
				prpdgraph.ultralpdata[l] =datainputstream.readShort();//超声波数据
			
		}
		catch (Exception exception)
		{
			exception.printStackTrace();
		}
	}
	
}
