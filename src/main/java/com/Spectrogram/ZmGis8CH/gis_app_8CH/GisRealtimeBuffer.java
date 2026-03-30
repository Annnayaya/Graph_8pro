package com.Spectrogram.ZmGis8CH.gis_app_8CH;
import java.util.Random;


class GisRealtimeBuffer
{

	int internalPRPS[];
	int AeinternalPRPS[];
	int dispPRPD[][];
	int AedispPRPD[][];
	static float fd[][] = new float[2][2];//数组的大小可以控制显示的分布大小
	int accPrps[][];
	int accAePrps[][];
	boolean refreshAccum;
	boolean magnifyPRPD;
	int columdata[];//柱形数组
	int internalPRPSMax;//周波中最大值
	long oldtime = 0;//原始周波到来时间
	long newtime = 0;//新周波到来时间
	static int delt=320;
	int Countprpd = 0;//总放电次数
	int FlyAedata[];//超声波数据
	int cycle = 0;
	
	public GisRealtimeBuffer(int ai[])
	{
		dispPRPD = new int[64][64];
		accPrps = new int[64][64];
		columdata = new int[64];//实例化柱形数据
		AeinternalPRPS = new int[640];
		refreshAccum = false;
		magnifyPRPD = false;
		internalPRPS = ai;
		FlyAedata = new int[960];
		accAePrps = new int[640][64];
		AedispPRPD = new int[640][64];
		oldtime = System.nanoTime();
		newtime = System.nanoTime();
		
		//for(int k = 0; k < 640; k++)
		//{
			//AeinternalPRPS[k] = ai[k + 4480];
			//cycle++;
		//}
		//float f = (float)Math.sqrt(18D);
		//float f = (float)Math.sqrt(8D);//放大显示系数
		float f = (float)Math.sqrt(2D);//放大显示系数
		for (int i = 0; i <= 1; i++)
		{
			for (int j = 0; j <= 1; j++)
				fd[i][j] = (f - (float)Math.sqrt((float)(i * i + j * j))) / f;

		}

	}

	public void rtbProc()
	{
		if (!refreshAccum)
		{
			for (int i = 0; i < 64; i++)
			{
				for (int k = 0; k < 64; k++)
					accPrps[i][k] = 0;
				Countprpd = 0;
			}
			/*
			for(int a = 0; a < 960; a++)
			{
				//FlyAedata[a] = 0;
				for(int b = 0; b < 64; b++)
				{
					//accAePrps[a][b] = 0;
				}
			}
			*/
		}
		
		for(int m = 0; m < 64; m++)
		{
			columdata[m] = 0;			
		}
		oldtime = newtime;//原尖峰周波时间
		
		for (int j = 0; j < GisXYZ.bufferLen; j++)
		{
			int l1 = internalPRPS[j];
			if (internalPRPS[j] == 0)
				continue;
			int l = (internalPRPS[j] * 64) / 4096;
			//int ll = (AeinternalPRPS[j & 0x27f] * 64) / 4096;
			internalPRPSMax = 0;
			
			if (0 <= l && l < 64)
			{
				accPrps[j & 0x3f][l]++;
				columdata[j & 0x3f] = l1;
				//accAePrps[j & 0x1e0][ll]++;
				//FlyAedata[j & 0x3bf] = l1;
				if(columdata[l] > internalPRPSMax)
				{
					
					internalPRPSMax = columdata[l];
					newtime = System.nanoTime();//新尖峰周波时间
				}
			}
			//if(0 <= ll && ll < 64)
			//{
				//accAePrps[j & 0x27f][ll]++;
			//}
			FlyAedata[j & 0x3bf] = l1;
		}

		//尖峰时间差
		calDel_T(newtime, oldtime);
		
		filter(accPrps, dispPRPD);
		//filter(accAePrps, AedispPRPD);
		
		for(int a = 0; a < 64; a ++)
		{
			for(int b = 0; b < 64; b++)
			{
				Countprpd += dispPRPD[a][b];
			}
		}
		Countprpd /=2;
	}

	public static void filter(int ai[][], int ai1[][])
	{
		int k1 = ai.length;
		int l1 = ai[0].length;
		for (int i = 0; i < k1; i++)
		{
			for (int j = 0; j < l1; j++)
			{
				float f = 0.0F;
				for (int l = -1; l <= 1; l++)
				{
					int j1 = l + j;
					if (0 > j1 || j1 >= l1)
						continue;
					for (int k = -1; k <= 1; k++)
					{
						int i1 = k + i;
						if (0 <= i1 && i1 < k1)
							f += (float)ai[i1][j1] * fd[Math.abs(k)][Math.abs(l)];
					}

				}
				ai1[i][j] = (int)f;
				//System.out.println("Data:"+ i + " " + j +" " + f);
			}			

		}

	}

	//计算第n个尖峰与第n+1个尖峰的时间差
	public static void calDel_T(long starttime, long endtime)
	{
		Random seed = new Random();
		int rd = 0;
		while(rd == 0)
		{
			rd = seed.nextInt(64);
		}
		long del_t = starttime - endtime;
		if(delt < 0)
			delt = 0;
		if(delt > 640)
			delt = 640;		
		delt = (int)(((del_t/100000L)*200L)/(64-rd));
		//System.out.println("时间："+delt);
	}
}
