package com.Spectrogram.ZmGis8CH.gis_app_8CH;

class GisChannelData
{

	int chNo;
	float freq;
	int sync;
	int ChStatus[] = {
		72, 66, 0, 0, 72, 66, 0, 0
	};
	int currentCycle;
	int AecurrentCycle;
	int phaseShift[];
	int offset[];
	int ioffset[];
	GisRealtimeBuffer rtb[];
	GisPeriodBuffer pb[];
	GisAnalyzer an[];
	GisEventTrigger trigger;
	GisFlyBuffer fb[];
	int buffer1[][];
	int buffer2[][];
	int bufferd[][];
	int bufferdf1[][];//频率成分缓存
	int buffer_320[][];
	int buffer_640[][];
	int buffer_utral[][];
	int buffer_colum[][];//柱状图数据
	int Aebuffer1[][];//超声波数据
	boolean pause[];
	boolean denoise1[];
	boolean denoise2[];
	GisDenoiser denoiser;
	GisXYZ mmi;
	int cnt;
	int p_cnt;
	int oldSec;
	int curSec;
	double max_utral[];
	int temp_ultral[];
	int temp_flygraphic[];
	static long delt=0;
	static int ph1;//最大值相位
	static int ph2;//次大值相位
	int ii[][];
	int Rmf1;//频率成分1
	int Rmf2;//频率成分2
	double Rf[];//n次实部
	double If[];//n次虚部
	int Rmf[];//n次谐波
	static double pi = 3.1415926d;

	public final PersistentPeakService persistentPeakService = new PersistentPeakService();
	private final int[] currentCyclePeakRaw = new int[8];
	private final int[] cachedGain = new int[8];
	private final boolean[] cachedEnabled = new boolean[8];

	public GisChannelData(int i, GisXYZ gismmi)
	{
		chNo = 8;
		freq = 60F;
		sync = 1;
		currentCycle = 0;
		AecurrentCycle = 0;
		phaseShift = new int[8];
		offset = new int[8];
		ioffset = new int[8];
		cnt = 0;
		p_cnt = 0;
		oldSec = 0;
		curSec = 0;
		chNo = i;
		//buffer1 = new int[i][8192];
		//buffer2 = new int[i][8192];
		//bufferd = new int[i][8192];
		buffer1 = new int[i][GisXYZ.bufferLen];
		buffer2 = new int[i][GisXYZ.bufferLen];
		bufferd = new int[i][GisXYZ.bufferLen];
		Aebuffer1 = new int[i][640];
		bufferdf1 = new int[i][64];
		buffer_320 = new int[i][320];//5个周期数据
		buffer_640 = new int[i][640];//10个周期数据
		buffer_utral = new int[i][640];
		buffer_colum = new int[i][64];
		phaseShift = new int[i];
		offset = new int[i];
		ioffset = new int[i];
		rtb = new GisRealtimeBuffer[i];
		pb = new GisPeriodBuffer[i];
		an = new GisAnalyzer[i];
		fb = new GisFlyBuffer[i];
		pause = new boolean[i];
		denoise1 = new boolean[i];
		denoise2 = new boolean[i];
		denoiser = new GisDenoiser(i);
		denoiser.smoothOffset = true;
		max_utral = new double[i];
		temp_ultral = new int[i];
		temp_flygraphic = new int[i];
		ii = new int[i][64];
		//Rf = new double[10];
		//If = new double[10];
		//Rmf = new int[10];
		
		for (int j = 0; j < i; j++)
		{
			rtb[j] = new GisRealtimeBuffer(buffer2[j]);
			fb[j] = new GisFlyBuffer(buffer_640[j]);
 			pb[j] = new GisPeriodBuffer();
			an[j] = new GisAnalyzer(rtb[j]);//ʵʱ��ݻ���
			denoise1[j] = true;
			denoise2[j] = true;	
			max_utral[j] = GisSignal.calMin_dBm;
			
		}

		trigger = new GisEventTrigger(this);
		mmi = gismmi;
	}

	
	
	public void proc1Cycle(int ai[][])
	{
		currentCycle = (currentCycle + 1) % 80;
		//AecurrentCycle = (AecurrentCycle + 1)% 10;
		for (int i = 0; i < chNo; i++)
		{
			int i4;//最大值
			int eff;//有效值
			int l32;
			int i44;
			int l3 = i4 = l32 = i44 = Rmf1 = Rmf2 = eff = 0;
			int k = 0;
			Rf = new double[10];
			If = new double[10];
			Rmf = new int[10];
			int i3 = currentCycle * 64 - 1;
			//int Aei3 = AecurrentCycle * 64 - 1;
			for (; k < 64; k++)
			{
				if (i3 >= buffer1[i].length - 1)
					i3 = -1;
				//if (Aei3 > Aebuffer1[i].length - 1)
					//Aei3 = -1;
				int k3 = ai[i][k];			
				buffer1[i][++i3] = k3;
				//Aebuffer1[i][++Aei3] = k3;
				//mmi.gui.prps[i].columdata[k] = ai[i][k]-denoiser.gdOffset[i];//柱形图数据
				//mmi.gui.prps[i].columdata[k] = ai[i][k] + denoiser.gain_int[i];//加上增益
				//mmi.gui.prps[i].columdata[k] = ai[i][k]-denoiser.gdOffset[i]; //+ denoiser.gain_int[i];//柱形图数据
				//mmi.gui.prps[i].columdata[k] = ai[i][k];//柱形图数据
				//mmi.gui.prpd[i].ultralpdata[k] = buffer1[i][i3];
				//mmi.gui.prps[i].columdata[k] = buffer_colum[i][k]-denoiser.gdOffset[i];

				mmi.gui.prps[i].columdata[k] = rtb[i].columdata[k] + denoiser.gain_int[i];//从分析数组中取数据
				mmi.gui.prpd[i].columdata[k] = rtb[i].columdata[k] + denoiser.gain_int[i];
				l3 += k3;
				if (i4 < k3)
					i4 = k3;//最大值
				ii[i][k] = k3;				
			}
			currentCyclePeakRaw[i] = i4;
			//i5 = find_sec_max(ii[i],64);
			//dft(ii[i], 64);

			for(int nf = 1; nf < 7; nf++)
			{
				dftn(ii[i], 64, nf);
				eff += Rmf[nf]*Rmf[nf];
			}
			eff = (int)(Math.sqrt((double)(eff)));
			Rmf1 = Rmf[1];
			Rmf2 = Rmf[2];
			//i5 = find_f(ii,64);
			//i5 = find_sec_max(bufferdf1[i],64);
			//超声波320组数据右移		
			//int k6 = 320;
			//int k8 = 320;
			/*
			for(int k3 = 0; k3 < 320;)
			{
				if(k3 < 5)
				{
					buffer_320[i][k3++] = (GisSignal.dBm2Int(max_utral[i])-denoiser.gdOffset[i])/k3;
					k3 = 1;
				}
				//buffer_320[i][k3] = (GisSignal.dBm2Int(max_utral[i]))/k3;
				else
					buffer_320[i][0] = kk;
			}
			*/
			/*
			for(int k2 = 0;k2 < 320; k2++)
			{				
				if(k6 < 2)
					k6 = 320;
				if(k8 <= 1)
					k8 = 320;												
				temp_ultral[i] = GisSignal.dBm2Int(max_utral[i])-denoiser.gdOffset[i]+ denoiser.gain_int[i];//减去偏移加上增益
				//buffer_320[i][--k6] = (GisSignal.dBm2Int(max_utral[i])-denoiser.gdOffset[i]+ denoiser.gain_int[i])/k6;
				buffer_320[i][--k8] = buffer_320[i][k8-1];
				buffer_320[i][0] = temp_ultral[i];
				mmi.gui.prpd[i].ultralpdata[k2] = buffer_320[i][k2] + denoiser.gain_int[i];//加上增益
				//mmi.gui.prpd[i].ultralpdata[k2] = buffer_utral[i][k2];
				
			}
			//正弦波移动步进
			if(mmi.gui.prpd[i].movestep >= 319)
				mmi.gui.prpd[i].movestep = 0;
			mmi.gui.prpd[i].movestep++;
			mmi.gui.prpd[i].sycfreq = freq;
			*/
			/*
			int k2 = 0;
			int k6 = 0;
			int k8 = 320;
			for(;k2 < 320; k2++)
			{
				if(k6 >=64)
					k6 = 0;
				if(k8 <= 1)
					k8 = 320;
				if(mmi.gui.prpd[i].movestep >= 319)
					mmi.gui.prpd[i].movestep = 0;
				int kk = buffer_utral[i][k6++]-denoiser.gdOffset[i];//减去偏移
				buffer_320[i][--k8] = buffer_320[i][k8 - 1];
				buffer_320[i][0] = kk;
				mmi.gui.prpd[i].ultralpdata[k2] = buffer_320[i][k2] + denoiser.gain_int[i];//加上增益
				
				mmi.gui.prpd[i].movestep++;
				mmi.gui.prpd[i].sycfreq = freq;
			}
			*/
			//飞行图640组数据右移
			/*
			int k21 = 0;
			int k81 = 640;
			for(;k21 < 640; k21++)
			{
				if(k81 <= 1)
					k81 = 640;
				temp_flygraphic[i] = GisSignal.dBm2Int(max_utral[i])- denoiser.gdOffset[i] + denoiser.gain_int[i];//减去偏移加上增益
				int t = --k81;
				
				if(t <=1)
					t = 320;
				//buffer_640[i][--k81] = buffer_640[i][k81 - 1];
				buffer_640[i][--t] = buffer_640[i][t - 1];
				buffer_640[i][0] = temp_flygraphic[i];
				mmi.gui.prps[i].flygraphicdata[k21] = buffer_640[i][k21] + denoiser.gain_int[i];//加上增益
			}
			*/
			/*
			int k21 = 0;
			int k81 = 640;
			//int time = 0;
			Random rd = new Random();
			for(;k21 < 640; k21++)
			{
				if(k81 <= 1)
					k81 = 640;
				temp_flygraphic[i] = GisSignal.dBm2Int(max_utral[i])- denoiser.gdOffset[i] + denoiser.gain_int[i];//减去偏移加上增益
				//temp_flygraphic[i] = GisSignal.dBm2Int(rtb[i].internalPRPSMax)- denoiser.gdOffset[i] + denoiser.gain_int[i];//减去偏移加上增益
				
				if(temp_flygraphic[i] <= (0 + denoiser.gdOffset[i] - denoiser.gain_int[i]))
					continue;//这里可以使数据暂存
									
				int time = GisRealtimeBuffer.delt;
				//int time = rd.nextInt(640);
				buffer_640[i][--k81] = buffer_640[i][k81 - 1];
				buffer_640[i][0] = temp_flygraphic[i];
				
				for(int kk = 640; kk > 100*time;)
				{
					buffer_640[i][--kk]= 0  - denoiser.gain_int[i];
				}
				
				mmi.gui.prps[i].flygraphicdata[k21] = buffer_640[i][k21] + denoiser.gain_int[i];//加上增益
			}
			*/
			/*
			int k21 = 0;
			for(;k21 < 640; k21++)
			{
				
				if(buffer_640[i][k21] <= 0 + denoiser.gdOffset[i] - denoiser.gain_int[i])
					continue;
				mmi.gui.prps[i].flygraphicdata[k21] = buffer_640[i][k21] + denoiser.gain_int[i];//加上增益
			}
			*/
			
			int k21 = 0;
			for(;k21 < 640; k21++)
			{
				
				//if(buffer_640[i][k21] <= 0 + denoiser.gdOffset[i] - denoiser.gain_int[i])
					//continue;
				//mmi.gui.prps[i].flygraphicdata[k21] = rtb[i].FlyAedata[k21] + denoiser.gain_int[i];//加上增益
				mmi.gui.prps[i].flygraphicdata[k21] = fb[i].accflydata[k21] + denoiser.gain_int[i];//加上增益
			}
			
			//mmi.gui.prps[i].flygraphicdata = fb[i].accflydata;//加上增益
//***************************************以下数据图形显示刷新快**********************************//	
			//有效值
			/*
			for(int x1 = 0; x1 < 64; x1++)
			{
				eff += ai[i][x1]-denoiser.gdOffset[i]+ denoiser.gain_int[i];
			}
			*/
			//System.out.println("通道：" + i +"的有效值：" + eff);
			//mmi.gui.prps[i].effdBm = (double)((eff /64.0D) *(GisSignal.calMax_dBm - GisSignal.calMin_dBm)) / 4095.0D + (double)GisSignal.calMin_dBm;
/*
			//峰值			
			int max = 0;
			for(int x2 = 0; x2 < 64; x2++)
			{				
				if(max < ai[i][x2]-denoiser.gdOffset[i]+ denoiser.gain_int[i])
					max = ai[i][x2]-denoiser.gdOffset[i]+ denoiser.gain_int[i];				
			}
			//mmi.gui.prpd[i].amxdBm = (double)(max * (GisSignal.calMax_dBm - GisSignal.calMin_dBm)) / 4095.0D + (double)GisSignal.calMin_dBm;//峰值
			
			//频率成份1 共64组数据=平均值
			int f1 = 0;
			for(int x3 = 0; x3 < 64; x3++)
			{
				if(f1 < ai[i][x3])
					f1 = ai[i][x3] - denoiser.gdOffset[i]+ denoiser.gain_int[i];
			}
			//mmi.gui.prps[i].f1dBm = (((double)f1) * (double)(GisSignal.calMax_dBm - GisSignal.calMin_dBm)) / 4095.0D + (double)GisSignal.calMin_dBm;
			
			//频率成份2 正半周期32组数据
			int f2 = 0;
			for(int x4 = 0; x4 < 32; x4++)
			{
				if(f2 < ai[i][x4])
					f2 = ai[i][x4] - denoiser.gdOffset[i]+ denoiser.gain_int[i];
				//f2 += ai[i][x4]-denoiser.gdOffset[i]+ denoiser.gain_int[i];
			}
			//mmi.gui.prps[i].f2dBm = mmi.gui.prpd[i].f2dBm = (((double)f2) * (double)(GisSignal.calMax_dBm - GisSignal.calMin_dBm)) / 4095.0D + (double)GisSignal.calMin_dBm;
*/
//**************************************************以上数据图形显示刷新快**************************//		
			/*
			for(int x = 0; x < 32; x++)
			{
				int k32 =ai[i][x];
				l32 += k32;
			}
			 */
			for (int l = 78; 0 <= l; l--)
			{
				mmi.gui.prps[i].cycleSum[l + 1] = mmi.gui.prps[i].cycleSum[l];
				mmi.gui.prps[i].cycleMax[l + 1] = mmi.gui.prps[i].cycleMax[l];				
				//mmi.gui.prpd[i].cycleSum32[l + 1] = mmi.gui.prpd[i].cycleSum32[l];
				mmi.gui.prps[i].cyclef1Sum[l + 1] = mmi.gui.prps[i].cyclef1Sum[l];//频率成分1
				mmi.gui.prps[i].cyclef2Sum[l + 1] = mmi.gui.prps[i].cyclef2Sum[l];//频率成分2				
				mmi.gui.prps[i].cycleeffSum[l + 1] = mmi.gui.prps[i].cycleeffSum[l];//有效值
			}

			mmi.gui.prps[i].cycleSum[0] = l3;
			mmi.gui.prps[i].cycleMax[0] = i4;
			mmi.gui.prps[i].cyclef1Sum[0] = Rmf1;
			mmi.gui.prps[i].cyclef2Sum[0] = Rmf2;
			mmi.gui.prps[i].cycleeffSum[0] = eff;
			//mmi.gui.prpd[i].cycleSum32[0] = l32;
			mmi.gui.prpd[i].cycleMax[0] = i4;
			l3 = i4 = i44 = eff = Rmf1 = Rmf2 = 0;
			for (int i1 = 0; i1 < 80; i1++)
			{
				l3 += mmi.gui.prps[i].cycleSum[i1];
				//l32 += mmi.gui.prpd[i].cycleSum32[i1];
				if (i4 < mmi.gui.prps[i].cycleMax[i1])
					i4 = mmi.gui.prps[i].cycleMax[i1];
				if(Rmf1 < mmi.gui.prps[i].cyclef1Sum[i1])
					Rmf1 = mmi.gui.prps[i].cyclef1Sum[i1];//频率成分1
				if(Rmf2 < mmi.gui.prps[i].cyclef2Sum[i1])
					Rmf2 = mmi.gui.prps[i].cyclef2Sum[i1];//频率成分2
				if(eff < mmi.gui.prps[i].cycleeffSum[i1])
					eff = mmi.gui.prps[i].cycleeffSum[i1];
			}
			/*
			//PRPD5个周波最大值
			for (int i1 = 0; i1 < 5; i1++)
			{
				if (i44 < mmi.gui.prpd[i].cycleMax[i1])
					i44 = mmi.gui.prpd[i].cycleMax[i1];
			}
			*/
			//PRPS平均值
			mmi.gui.prps[i].avrdBm = (double)((l3 /5120D) *(GisSignal.calMax_dBm - GisSignal.calMin_dBm)) / 4095D + (double)GisSignal.calMin_dBm + mmi.gui.gainScrollBar[i].getValue();
			//PRPS最大值
			double rawPrpsMax = (double)(i4 * (GisSignal.calMax_dBm - GisSignal.calMin_dBm)) / 4095D + (double)GisSignal.calMin_dBm + mmi.gui.gainScrollBar[i].getValue();
			PersistentPeakSnapshot snap = persistentPeakService != null ? persistentPeakService.getLastSnapshot() : null;
			if (AppGlobalConfig.isSpectrogramPeakFollowFiltered() && snap != null && snap.isValid(i) && !Double.isNaN(snap.getPublishedDbm(i))) {
				mmi.gui.prps[i].amxdBm = snap.getPublishedDbm(i);
				mmi.gui.prpd[i].amxdBm = snap.getPublishedDbm(i);
			} else {
				mmi.gui.prps[i].amxdBm = rawPrpsMax;
				//PRPD5个周波最大值
				mmi.gui.prpd[i].amxdBm = mmi.gui.prps[i].amxdBm + mmi.gui.gainScrollBar[i].getValue();
			}
			max_utral[i] = (double)(i44 * (GisSignal.calMax_dBm - GisSignal.calMin_dBm)) / 4095D + (double)GisSignal.calMin_dBm;;//峰值
			mmi.gui.prpd[i].avrdBm = mmi.gui.prps[i].avrdBm + mmi.gui.gainScrollBar[i].getValue();//有效值
			mmi.gui.prpd[i].Countprpd = rtb[i].Countprpd;//放电次数
			
			mmi.gui.prps[i].f1dBm = mmi.gui.prpd[i].f1dBm = (double)(Rmf1* (GisSignal.calMax_dBm - GisSignal.calMin_dBm)) / 4095D + (double)GisSignal.calMin_dBm + mmi.gui.gainScrollBar[i].getValue();
			mmi.gui.prps[i].f2dBm = mmi.gui.prpd[i].f2dBm = (double)(Rmf2 * (GisSignal.calMax_dBm - GisSignal.calMin_dBm)) / 4095D + (double)GisSignal.calMin_dBm + mmi.gui.gainScrollBar[i].getValue();

			
			if (GisSignal.isAuto[i])
			{
				denoiser.offset_dBm[i] = denoiser.dnMin_dBm[i];
				mmi.gui.offsetAdjustScrollBar[i].setValue((int)(denoiser.offset_dBm[i] - 0.5D));
			} else
			{
				denoiser.offset_dBm[i] = mmi.gui.offsetAdjustScrollBar[i].getValue();//
				denoiser.gain_int[i] = mmi.gui.gainScrollBar[i].getValue()*4095/(GisSignal.calMax_dBm - GisSignal.calMin_dBm);//增益
			}
			mmi.gui.prps[i].mindBm = (int)(denoiser.offset_dBm[i] - 0.5D);
			mmi.gui.prps[i].maxdBm = GisSignal.calMax_dBm;
			mmi.gui.prps[i].phaseDelay = mmi.gui.phaseScrollBar[i].getValue();
			phaseShift[i] = -mmi.gui.phaseScrollBar[i].getValue();
			denoise2[i] = mmi.gui.noisegate[i].isSelected();
		}

		if (mmi != null && mmi.gui != null)
		{
			mmi.gui.checkAlarms();
		}

		if (persistentPeakService != null && mmi != null && mmi.gui != null && AppGlobalConfig.isPersistentPeakEnabled())
		{
			for (int j = 0; j < chNo; j++)
			{
				if (mmi.gui.gainScrollBar != null && j < mmi.gui.gainScrollBar.length && mmi.gui.gainScrollBar[j] != null)
				{
					cachedGain[j] = mmi.gui.gainScrollBar[j].getValue();
				} else {
					cachedGain[j] = 0;
				}
				if (mmi.gui.suspendButtonPRPS != null && j < mmi.gui.suspendButtonPRPS.length && mmi.gui.suspendButtonPRPS[j] != null)
				{
					cachedEnabled[j] = !mmi.gui.suspendButtonPRPS[j].isSelected();
				} else {
					cachedEnabled[j] = true;
				}
			}
			boolean gap = GisUDP.hasPacketGap;
			if (gap)
			{
				GisUDP.hasPacketGap = false;
			}
			PeakMeasurementContext context = new PeakMeasurementContext(
					GisSignal.calMin_dBm,
					GisSignal.calMax_dBm,
					cachedGain,
					cachedEnabled,
					freq,
					gap,
					System.currentTimeMillis()
			);
			persistentPeakService.acceptCyclePeaks(currentCyclePeakRaw, context);
		}
		
		if (AppGlobalConfig.isSpectrogramRollingEnabled())
		{
			for (int j = 0; j < chNo; j++)
			{
				if (!mmi.gui.suspendButtonPRPS[j].isSelected())
				{
					// 1. 3D绘图数据 FIFO 前移 1 个周波 (64点)
					System.arraycopy(bufferd[j], 64, bufferd[j], 0, 5056);

					// 2. 将当前最新周波数据经过阈值去噪与相位偏置计算后，填充到末尾 (5056..5119)
					int j3 = denoiser.gdOffset[j];
					int l2 = phaseShift[j];
					for (int p = 0; p < 64; p++)
					{
						int rawVal = ai[j][p];
						int j6 = rawVal - j3;
						if (j6 < 0) j6 = 0;
						if (j3 < 4095) j6 = (4095 * j6) / (4095 - j3);
						bufferd[j][5056 + (p + l2) % 64] = j6;
					}

					System.arraycopy(bufferd[j], 0, buffer2[j], 0, 5120);
					System.arraycopy(buffer2[j], 5056, bufferdf1[j], 0, 64);
					System.arraycopy(buffer2[j], 4480, buffer_utral[j], 0, 640);

					int k = 0;
					int k1 = 0;
					int n = 576;
					for(; ;)
					{
						buffer_640[j][k1++] = buffer_utral[j][n];
						n++;
						if(n > 640 - 64 * k - 1)
						{
							k++;
							if(k > 9)
								break;
							n = 576 - 64 * k;
						}
					}
				}

				if (!mmi.gui.suspendButtonPRPD[j].isSelected())
				{
					rtb[j].rtbProc();
					fb[j].fbpro();
				}
				an[j].gaProc(offset[j], ioffset[j], j);
				pb[j].gpbProc(an[j]);
				if (denoise2[j])
				{
					if (an[j].sig_cnt + denoiser.gatedSignal[j] > 30)
						mmi.gui.prps[j].stringToDraw2 = (new StringBuilder()).append((int)(((float)denoiser.gatedSignal[j] * 100F) / (float)(an[j].sig_cnt + denoiser.gatedSignal[j]))).append("% ").append(ChooseLang.M[237]).toString();
					else
						mmi.gui.prps[j].stringToDraw2 = ChooseLang.M[200];
				} else
				{
					mmi.gui.prps[j].stringToDraw2 = "";
				}
			}

			if ((++cnt & 7) == 7)
			{
				offset = denoiser.gdProc(buffer1, buffer2, buffer_utral, denoise1, denoise2, chNo - 1, phaseShift, currentCycle, false);
			}
			GisXYZ.refresh = true;
		}
		else
		{
			switch (++cnt & 7)
			{
			case 0: // '\0'
			case 2: // '\002'
			case 4: // '\004'
			case 6: // '\006'                
				//int j = cnt >> 1 & 3;//mana-{1-4通道}
				int j = cnt >> 1 & 7;//mana-{1-8通道}
				if (!mmi.gui.suspendButtonPRPS[j].isSelected())
				{
					System.arraycopy(buffer2[j], 0, bufferd[j], 0, 5120);
					System.arraycopy(buffer2[j], 5056, bufferdf1[j], 0, 64);
					//System.arraycopy(buffer_utral[j], 0, buffer_640[j], 0, 640);
					System.arraycopy(buffer2[j], 4480, buffer_utral[j], 0, 640);
					
					int k = 0;
					int k1 = 0;
					int n = 576;
									
					for(; ;)
					{					
						buffer_640[j][k1++] = buffer_utral[j][n];
						n++;
						if(n > 640 - 64 * k - 1)
						{
							k++;
							if(k > 9)
								break;
							n = 576 - 64 * k;
						}
					}
					
				}
				if (!mmi.gui.suspendButtonPRPD[j].isSelected())
				{
					rtb[j].rtbProc();
					fb[j].fbpro();
				}
				an[j].gaProc(offset[j], ioffset[j], j);
				pb[j].gpbProc(an[j]);
				if (denoise2[j])
				{
					if (an[j].sig_cnt + denoiser.gatedSignal[j] > 30)
						mmi.gui.prps[j].stringToDraw2 = (new StringBuilder()).append((int)(((float)denoiser.gatedSignal[j] * 100F) / (float)(an[j].sig_cnt + denoiser.gatedSignal[j]))).append("% ").append(ChooseLang.M[237]).toString();//
					else
						mmi.gui.prps[j].stringToDraw2 = ChooseLang.M[200];//mana-0%门限
					
				} else
				{
					mmi.gui.prps[j].stringToDraw2 = "";
				}
				break;

			case 7: // '\007'
				offset = denoiser.gdProc(buffer1, buffer2, buffer_utral, denoise1, denoise2, chNo - 1, phaseShift, currentCycle, false);
				GisXYZ.refresh = true;
				break;
			}
		}
		
		long l4 = System.currentTimeMillis();
		int j3 = trigger.triggerProc();
		if (0 < j3 && GisXYZ.hwtest == 0)
		{
			System.out.println((new StringBuilder()).append("triggered : ").append(j3).toString());
			int ai1[] = new int[280];
			for (int j1 = 0; j1 < 8; j1++)
				ai1[j1] = phaseShift[j1];//相位偏移

			for (int k1 = 0; k1 < 8; k1++)
				ai1[k1 + 4] = offset[k1];//阀值

			for (int l1 = 0; l1 < 32; l1++)
				ai1[l1 + 8] = an[0].analysisResult[l1];

			for (int i2 = 0; i2 < 32; i2++)
				ai1[i2 + 8 + 32] = an[1].analysisResult[i2];

			for (int j2 = 0; j2 < 32; j2++)
				ai1[j2 + 8 + 64] = an[2].analysisResult[j2];

			for (int k2 = 0; k2 < 32; k2++)
				ai1[k2 + 8 + 96] = an[3].analysisResult[k2];

			for (int k21 = 0; k21 < 32; k21++)
				ai1[k21 + 8 + 128] = an[4].analysisResult[k21];

			for (int k22 = 0; k22 < 32; k22++)
				ai1[k22 + 8 + 160] = an[5].analysisResult[k22];

			for (int k23 = 0; k23 < 32; k23++)
				ai1[k23 + 8 + 192] = an[6].analysisResult[k23];

			for (int k24 = 0; k24 < 32; k24++)
				ai1[k24 + 8 + 224] = an[7].analysisResult[k24];

			for (int l2 = 0; l2 < 8; l2++)
				ai1[l2 + 8 + 256] = an[l2].prob;//分析

//			for (int abc=0;abc<ai1.length;abc++) {
//				System.out.println(abc+"      输出：     "+ai1[abc]);
//				
//			}
			mmi.currentOpenSite.saveEvent(l4, buffer1[0], buffer1[1], buffer1[2], buffer1[3], buffer1[4], buffer1[5], buffer1[6], buffer1[7], currentCycle, ai1);
			mmi.ge.updateCurrent();
		}
	}

	
	//查找次大值
	private int find_sec_max(int array[], int len)
	{
		int i = 0;    
		int maxnumber = array[0];//把数组0赋予最大值    
		int sec_max = 0;//置第二大值为0        
		for (i = 1; i < len; i++) 
		{        
			if (array[i] > maxnumber) //如果下一个数据大于最大值
			{            
				sec_max = maxnumber;  //上个数据赋予次大值
				maxnumber = array[i]; //下个数据赋予最大值           
				
				ph1 = i;//最大值得相位
				//System.out.println("Ph1:" + ph1);
			} //如果下个数据小于最大值执行下步，判断数据是否大于次大值
			else if(array[i] > sec_max) //若数据大于次大值
			{                
				sec_max = array[i];//此数据赋予次大值
				ph2 = i;//次大值相位
				//System.out.println("Ph2:" + ph2);
			} 
			/*
			else 
			{         
				;        
			} 
			*/   
		}    
		return sec_max;
		
	}

	//dft变换
	private void dft(int array[], int len)
	{
		double Rf1 = 0.0d;//基波实部
		double If1 = 0.0d;//基波虚部
		double Rf2 = 0.0d;//2次谐波实部
		double If2 = 0.0d;//2次谐波虚部
		for(int k = 0; k < len; k++)
		{
			Rf1 += (double)array[k]*Math.cos(2*GisSignal.PI*1*k/len);
			If1 += (double)array[k]*Math.sin(2*GisSignal.PI*1*k/len);
			Rf2 += (double)array[k]*Math.cos(2*GisSignal.PI*2*k/len);
			If2 += (double)array[k]*Math.sin(2*GisSignal.PI*2*k/len);			
		}
		
		Rmf1 = (int) (10.0D*(Math.sqrt(Rf1*Rf1 + If1*If1)/len/2));
		Rmf2 = (int) (10.0D*(Math.sqrt(Rf2*Rf2 + If2*If2)/len/2));
		//System.out.println("Rmf1=" + Rmf1);
		//System.out.println("Rmf2=" + Rmf2);
	}
	
	//dft变换
	private void dftn(int array[], int len, int i)
	{			
		for(int k = 0; k < len; k++)
		{
			Rf[i] += (double)array[k]*Math.cos(2*GisSignal.PI*i*k/len);
			If[i] += (double)array[k]*Math.sin(2*GisSignal.PI*i*k/len);			
		}
		
		Rmf[i] = (int) (10.0D*(Math.sqrt(Rf[i]*Rf[i] + If[i]*If[i])/len/2));
	}
}
