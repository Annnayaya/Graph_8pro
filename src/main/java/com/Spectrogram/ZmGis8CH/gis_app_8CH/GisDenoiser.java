package com.Spectrogram.ZmGis8CH.gis_app_8CH;
class GisDenoiser
{

	double dnMin_dBm[];
	int gdOffset[];
	int chno;
	double offset_dBm[] = {
		0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D
	};
	int gatedSignal[] = {
		0, 0, 0, 0, 0, 0, 0, 0
	};
	int gain_int[] = {
		0, 0, 0, 0, 0, 0, 0, 0	
	};
	boolean findOffset;
	boolean smoothOffset;
	int preDefinedOffset[] = {
		0, 0, 0, 0, 0, 0, 0, 0
	};
	boolean direction[] = {
			false, false, false, false, false, false, false, false
	};

	public GisDenoiser(int i)
	{
		findOffset = true;
		smoothOffset = false;
		chno = i;
		dnMin_dBm = new double[i];
		gdOffset = new int[i];
		for (int j = 0; j < i; j++)
		{
			dnMin_dBm[j] = GisSignal.calMid_dBm;
			gdOffset[j] = 1024;
			//gdOffset[j] = 0;
		}

	}

	public int[] gdProc(int ai[][], int ai1[][], int ai4[][], boolean aflag[], boolean aflag1[], int i, int ai2[], int j, 
			boolean flag)
	{
		//int j1 = ai[0].length / 64;
		int ai3[] = new int[128];
		aflag1[i] = false;
		
		
		/*
		 * aflag1[0] = false; aflag1[1] = false; aflag1[2] = false; aflag1[3] = false;
		 */
		 
		 
		if (findOffset)
		{
			for (int k = 0; k < chno; k++)//mana-绘图
			{
				int l4 = 0;
				for (int k1 = 0; k1 < 128; k1++)
					ai3[k1] = 0;

				for (int l1 = 0; l1 < ai[0].length; l1++)
				{
					int l5 = ai[k][l1] / 32;
					if (0 < l5)
					{
						ai3[l5]++;
						l4++;
					}
				}

				int k3 = 0;
				int i2 = 0;
				do
				{
					if (i2 >= 127)
						break;
					k3 += ai3[i2];
					if (l4 / 20 <= k3)
						break;
					i2++;
				} while (true);
				int l3 = i2 - 1;
				int j4 = 0;
				i2 = 127;
				do
				{
					if (1 > i2)
						break;
					j4 += ai3[i2];
					if (l4 / 20 <= j4)
						break;
					i2--;
				} while (true);
				int k4 = i2 + 1;
				int i4 = (l3 + k4) / 2;
				if (i4 < 1)
					i4 = 1;
				if (126 < i4)
					i4 = 126;
				k3 = 0;
				i2 = i4;
				do
				{
					if (1 > i2)
						break;
					k3 += ai3[i2];
					if (l4 / 20 <= k3)
						break;
					i2--;
				} while (true);
				i4 = i2 + 1;
				if (126 < i4)
					i4 = 126;
				if (l4 == 0)
					dnMin_dBm[k] = GisSignal.calMid_dBm;
				else
				if (smoothOffset)
					dnMin_dBm[k] = dnMin_dBm[k] * 0.90000000000000002D + GisSignal.settleAutoOffset(GisSignal.Int2dBm(i4 * 32)) * 0.10000000000000001D;
				else
					dnMin_dBm[k] = GisSignal.settleAutoOffset(GisSignal.Int2dBm(i4 * 32));
				if (flag)
					offset_dBm[k] = dnMin_dBm[k];
				gdOffset[k] = GisSignal.dBm2Int(offset_dBm[k]);
			}

		} else
		{
			for (int l = 0; l < 8; l++)
			{
				gdOffset[l] = preDefinedOffset[l];
				if (gdOffset[l] < 0)
					gdOffset[l] = 0;
				offset_dBm[l] = GisSignal.Int2dBm(gdOffset[l]);
			}

		}
label0:
		for (int i1 = 0; i1 < chno; i1++)
		{
			gatedSignal[i1] = 0;
			int i5 = ai[0].length - 1;
			int l2 = ai2[i1];
			int i3 = ((j + 1) * 64) % ai[0].length;
			int j3 = gdOffset[i1];
			int j5 = 0;
			int k5 = 0;
			/*
			int k = 0;
			int k1 = 0;
			int n = 576;
							
			for(; ;)
			{					
				ai4[i1][k1++] = ai[i1][n];
				n++;
				if(n > 640 - 64 * k - 1)
				{
					k++;
					if(k > 9)
						break;
					n = 576 - 64 * k;
				}
			}
			*/
			
			if(direction[i1])
			{
				j5 = 63;
				k5 = 79;
			}
			else
			{
				j5 = 0;
				k5 = 0;
			}
						
			if (aflag1[i1])//消噪音通道定义
			{
				int j2 = 0;
				do
				{
					if (j2 >= ai[0].length)
						continue label0;
					int i6 = ai[i1][i3] - j3;
					if (i6 < 0)
						i6 = 0;
					if (i6 < ai[i][i3] - gdOffset[i])
					{
						i6 = 0;
						gatedSignal[i1]++;
					}
					if (j3 < 4095)
						i6 = (4095 * i6) / (4095 - j3);
					ai1[i1][k5 * 64 + (j5 + l2) % 64] = i6;
					if (j5 < 63)
					{
						j5++;
					} else
					{
						j5 = 0;
						k5++;
					}
					if (++i3 > i5)
						i3 = 0;
					j2++;
				} while (true);
			}
			for (int k2 = 0; k2 < ai[0].length; k2++)//数据移动
			{
				int j6 = ai[i1][i3] - j3;
				
				if (j6 < 0)
					j6 = 0;
				if (j3 < 4095)
					j6 = (4095 * j6) / (4095 - j3);				
				
				if(direction[i1])
				{
					//从首部平移数据
					ai1[i1][k5 * 64 + (j5 + l2) % 64] = j6;
					
					if (j5 > 0)
					{
						j5--;
					} else
					{
						j5 = 63;
						k5--;
					}
				}
				else
				{
					//从尾部平移数据
					ai1[i1][k5 * 64 + (j5 + l2) % 64] = j6;
					
					if (j5 < 63)
					{
						j5++;
					} else
					{
						j5 = 0;
						k5++;
					}
				}
				
				if (++i3 > i5)
					i3 = 0;
			}

		}

		return gdOffset;
	}
}
