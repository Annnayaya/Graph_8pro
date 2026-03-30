package com.Spectrogram.ZmGis8CH.gis_app_8CH;

class GisFlyBuffer {

	int flydata[];
	int accflydata[];//飞行数据
	int dt[];//计算数据数组
	int ph[];//相位
	int calph[];
	boolean refreshflydata;
	
	public GisFlyBuffer(int ai[])
	{
		accflydata = new int[640];
		flydata = ai;
		dt = new int[2005120];
		ph = new int[640];
		calph = new int[640];
		refreshflydata = false;
				
	}
	public void fbpro()
	{
		//if(!refreshflydata)
		//{
			for(int kk = 0; kk < 640; kk++)
			{
				accflydata[kk] = 0;
			}
		//}
		/*
		int k = 0;
		int k1 = 0;
		int n = 576;
						
		for(; ;)
		{					
			flydata[k1++] = conflydata[n];
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
		int i4 = 0;//最大值
		int phase = 0;//相位
		for(int i = 0; i < flydata.length; i++)
		{						
			int i3 = flydata[i];
			if(i4 < i3)
			{
				i4 = i3;
				phase = i;
				ph[i] = i4;
			}
			if((i & 0x3f) == 0x3f)
			{
				//ph[phase] = i4;
				i4 = 0;
			}
			//System.out.println("ph "+ i + " = " + ph[i]);
			//System.out.println("放电相位= " + phase);
		}
		
		int j = -1;
		for(int p = 0; p < flydata.length; p++)
		{
			if (ph[p] > 0)
			{
				j++;
				calph[j] = p;//放电的相位				
			}
			//System.out.println(p+" 放电相位：" + calph[p]);
		}
		
		int l = -1;
		for(int i1 = 0; i1 < j-1; i1++)
		{
			for(int j1 = i1 + 1; j1 < j; j1++)
			{
				++l;
				dt[l] = Math.abs(calph[i1] - calph[j1]);//运算后的放电相位
				if(ph[calph[i1]] >= ph[calph[j1]])
				{

					accflydata[dt[l]] = ph[calph[i1]];
				}
				else
				{
					accflydata[dt[l]] = 0;
				}
			}
		}
		//l=-1;
	}
}
