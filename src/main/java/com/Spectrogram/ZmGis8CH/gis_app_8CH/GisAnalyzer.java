package com.Spectrogram.ZmGis8CH.gis_app_8CH;

class GisAnalyzer
{

	GisRealtimeBuffer rtb;
	PDResult pr;
	public int sig_trg;
	int offset;
	int ioffset;
	int sig_cnt;
	int sig_max;
	int sig_avg;
	int lastCycle[];
	LTMap ltm;
	int analysisResult[];
	int analMax;
	int p_cnt;
	int prob;
	int isAddPattern;
	double pfdata[];

	public GisAnalyzer(GisRealtimeBuffer gisrealtimebuffer)
	{
		pr = new PDResult();
		sig_trg = 50;
		offset = 0;
		ioffset = 0;
		sig_cnt = 0;
		sig_max = 0;
		sig_avg = 0;
		lastCycle = new int[512];
		ltm = new LTMap();
		analysisResult = new int[32];
		analMax = 0;
		p_cnt = 0;
		prob = 0;
		isAddPattern = 0;
		pfdata = new double[5];
		rtb = gisrealtimebuffer;
	}

	private void savePatternFile(double ad[])
	{
        // 【新增拦截】：如果提取出来的第一个特征是 NaN，说明这个通道没信号，直接跳过，不保存！
        if (Double.isNaN(ad[0])) {
            return;
        }

		// 已根据需求彻底移除 pattern.txt 的输出
	}

	public void gaProc(int i, int j, int k)
	{
		offset = i;
		ioffset = j;
		int l2 = GisXYZ.bufferLen - 512 - 1;
		for (int l = 0; l < 512; l++)
			lastCycle[l] = rtb.internalPRPS[l2 + l];

		int j3 = 0;
		float f = 0.0F;
		sig_max = 0;
		for (int i1 = 0; i1 < GisXYZ.bufferLen; i1++)
		{
			int i3 = rtb.internalPRPS[i1];
			if (i3 > sig_trg)
			{
				j3++;
				f += i3;
			}
			if (i3 > sig_max)
				sig_max = i3;
		}

		sig_avg = (int)(f / (float)j3);
		sig_cnt = j3;
		if (++p_cnt >= 4)
		{
			ltm.ltMapCore(rtb.internalPRPS);
			p_cnt = 0;
			if (0 < isAddPattern)
			{
				for (int j1 = 0; j1 < 5; j1++)
					pfdata[j1] += ltm.values[j1];

				isAddPattern++;
			}
			if (isAddPattern == 5)
			{
				GisSignal.savePattern++;
				for (int k1 = 0; k1 < 5; k1++)
					pfdata[k1] /= 5D;

				savePatternFile(pfdata);
				isAddPattern = 0;
			}
			if (k == GisSignal.savePattern)
			{
				GisSignal.savePattern++;
				for (int l1 = 0; l1 < 5; l1++)
					pfdata[l1] = ltm.values[l1];

				isAddPattern = 1;
			}
			pr.prProcess(ltm.values);
			if (sig_cnt >= 50 && sig_max >= 300 || sig_cnt > 300)
			{
				for (int i2 = 0; i2 < 8; i2++)
					analysisResult[i2] = (int)(pr.result[i2] * 100F);

				analMax = analysisResult[0];
				for (int j2 = 0; j2 < 7; j2++)
					if (analMax < analysisResult[j2 + 1])
						analMax = analysisResult[j2 + 1];//ȡ���

				prob = pr.prob;
			} else
			{
				for (int k2 = 0; k2 < 8; k2++)
					analysisResult[k2] = 0;

				prob = 0;
			}
			analysisResult[26] = (int)(ltm.values[0] * 100F);
			analysisResult[27] = (int)(ltm.values[1] * 100F);
			analysisResult[28] = (int)(ltm.values[2] * 100F);
			analysisResult[29] = (int)(ltm.values[3] * 100F);
			analysisResult[30] = (int)(ltm.values[4] * 100F);
			analysisResult[31] = pr.prob;
		}
	}
}
