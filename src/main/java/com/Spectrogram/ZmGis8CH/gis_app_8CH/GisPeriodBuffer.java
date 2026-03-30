package com.Spectrogram.ZmGis8CH.gis_app_8CH;
class GisPeriodBuffer
{

	private int gbpData[][];
	private short periodPRPD[][];
	private int totalCnt;
	private boolean locked;

	GisPeriodBuffer()
	{
		gbpData = new int[64][3];
		periodPRPD = new short[64][64];
		totalCnt = 0;
		locked = false;
	}

	public void gpbProc(GisAnalyzer gisanalyzer)
	{
		int i = 0;
		do
		{
			if (!locked)
				break;
			try
			{
				Thread.sleep(1L);
			}
			catch (Exception exception)
			{
				exception.printStackTrace();
			}
			if (++i > 5)
				locked = false;
		} while (true);
		locked = true;
		totalCnt++;
		for (int j = 0; j < 512; j++)
		{
			int l = gisanalyzer.lastCycle[j] / 64;
			if (l > 63)
				l = 63;
			if (gisanalyzer.lastCycle[j] > 0)
				periodPRPD[j & 0x3f][l]++;
		}

		gbpData[0][0] += gisanalyzer.sig_cnt;
		if (gbpData[0][1] > gisanalyzer.sig_cnt)
			gbpData[0][1] = gisanalyzer.sig_cnt;
		if (gbpData[0][2] < gisanalyzer.sig_cnt)
			gbpData[0][2] = gisanalyzer.sig_cnt;
		gbpData[1][0] += gisanalyzer.sig_avg;
		if (gbpData[1][1] > gisanalyzer.sig_avg)
			gbpData[1][1] = gisanalyzer.sig_avg;
		if (gbpData[1][2] < gisanalyzer.sig_avg)
			gbpData[1][2] = gisanalyzer.sig_avg;
		gbpData[2][0] += gisanalyzer.offset;
		if (gbpData[2][1] > gisanalyzer.offset)
			gbpData[2][1] = gisanalyzer.offset;
		if (gbpData[2][2] < gisanalyzer.offset)
			gbpData[2][2] = gisanalyzer.offset;
		gbpData[3][0] += gisanalyzer.offset;
		if (gbpData[3][1] > gisanalyzer.ioffset)
			gbpData[3][1] = gisanalyzer.ioffset;
		if (gbpData[3][2] < gisanalyzer.ioffset)
			gbpData[3][2] = gisanalyzer.ioffset;
		gbpData[4][0] += gisanalyzer.sig_max;
		if (gbpData[4][1] > gisanalyzer.sig_cnt)
			gbpData[4][1] = gisanalyzer.sig_max;
		if (gbpData[4][2] < gisanalyzer.sig_cnt)
			gbpData[4][2] = gisanalyzer.sig_max;
		for (int k = 0; k < 31; k++)
		{
			gbpData[k + 5][0] += gisanalyzer.analysisResult[k];
			if (gbpData[k + 5][1] > gisanalyzer.analysisResult[k])
				gbpData[k + 5][1] = gisanalyzer.analysisResult[k];
			if (gbpData[k + 5][2] < gisanalyzer.analysisResult[k])
				gbpData[k + 5][2] = gisanalyzer.analysisResult[k];
		}

		gbpData[36][0] += gisanalyzer.prob;
		if (gbpData[36][1] > gisanalyzer.prob)
			gbpData[36][1] = gisanalyzer.prob;
		if (gbpData[36][2] < gisanalyzer.prob)
			gbpData[36][2] = gisanalyzer.prob;
		locked = false;
	}

	public int copyValue(int ai[][], int ai1[][])
	{
		int i = 0;
		int j1 = 0;
		do
		{
			if (!locked)
				break;
			try
			{
				Thread.sleep(1L);
			}
			catch (Exception exception)
			{
				exception.printStackTrace();
			}
			if (++i > 5)
				locked = false;
		} while (true);
		locked = true;
		j1 = 0;
		if (totalCnt > 0)
		{
			for (int j = 0; j < 37; j++)
			{
				ai[j][0] = gbpData[j][0] / totalCnt;
				ai[j][1] = gbpData[j][1];
				ai[j][2] = gbpData[j][2];
				gbpData[j][0] = 0;
				gbpData[j][1] = 0x98967f;
				gbpData[j][2] = 0;
			}

			for (int k = 0; k < 64; k++)
			{
				for (int i1 = 0; i1 < 64; i1++)
				{
					if (i1 > 0)
						j1 += periodPRPD[k][i1];
					ai1[k][i1] = periodPRPD[k][i1];
					periodPRPD[k][i1] = 0;
				}

			}

			//int l = totalCnt;
			totalCnt = 0;
		}
		locked = false;
		return j1;
	}
}
