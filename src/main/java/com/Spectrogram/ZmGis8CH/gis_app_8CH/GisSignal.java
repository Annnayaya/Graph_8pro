package com.Spectrogram.ZmGis8CH.gis_app_8CH;
import java.io.IOException;

class GisSignal
{

	static boolean isAuto[] = {
		false, false, false, false,false, false, false, false
	};
	static int savePattern = 4;
	 static int calMin_dBm = -80; 

	//static int calMin_dBm = -75;
	 static int calMid_dBm = -65; 

	//static int calMid_dBm = -73;
	 static int calMax_dBm = -20;      //memo 20230901 修改 dbm 为 -80 到 0 dbm

	//static int calMax_dBm = 0;
	static int calf2_dBm = -80;	
	static double PI = 3.1415926d;
	static double h[] = {.332670552950, .806891509311, .459877502118, -.135011020010, -.085441273882, .035226291882}; 	
	static double g[] = {.035226291882, .085441273882, -.135011020010, -.459877502118, .806891509311, -.332670552950};
	static double h2[]={-0.002,-0.003,0.006,0.006,-0.013,
			-0.012, 0.030, 0.023,-0.078,-0.035,
			0.307,0.542,0.307,-0.035,-0.078,
			0.023,0.030,-0.012,-0.013,0.006,
			0.006,-0.003,-0.002};

	static double g2[]={0.002,-0.003,-0.006,0.006,0.013,-0.012,
			-0.030,0.023,0.078,-0.035,-0.307,0.542,
			-0.307,-0.035,0.078,0.023,-0.030,-0.012,
			0.013,0.006,-0.006,-0.003,0.002};
	
	static int waveletclass = 6;
	static int DD = 23;
	static double low[] ={1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0,1.0};
	static double high[] ={0.1,0.1,0.1,0.1,0.1,0.1,0.1,0.1,0.1,0.1};

	GisSignal()
	{		
	}

	/**
	 * 
	 * @param d消燥的值
	 * @return 返回消燥后的值
	 */
	static double settleAutoOffset(double d)
	{
		if ((double)(calMax_dBm - 2) < d)
			d = calMax_dBm - 2;
		return d;
	}

	/**
	 * 
	 * @param i整数转换成dBm(0-4095)
	 * @return 返回dBm
	 */
	static double Int2dBm(int i)
	{
		if (i < 0)
			i = 0;
		if (4095 < i)
			i = 4095;
		return (double)(i * (calMax_dBm - calMin_dBm)) / 4095.0D + (double)calMin_dBm;
	}
	
	/**
	 * 
	 * @param i整数转换成dBm(这里没有限定整数的范围)
	 * @return 返回dBm
	 */
	static double Int2dBm_NO(int i)
	{
		if (i < 0)
			i = 0;
		return (double)(i * (calMax_dBm - calMin_dBm)) / 4982.5D + (double)calMin_dBm;
	}

	/**
	 * 
	 * @param d需要转换的dBm值 
	 * @return 返回整数0-4095
	 */
	static int dBm2Int(double d)
	{
		int i = (int)((4095D * (d - (double)calMin_dBm)) / (double)(calMax_dBm - calMin_dBm) + 0.5D);
		if (i < 0)
			i = 0;
		if (4095 < i)
			i = 4095;
		return i;
	}
	
	static int translateX (double x, int width) {

        return (int)(x*width/Math.PI/2);

	}

	static int translateY (double y, int height) {

        return (int)(height/2-y*height/Math.PI/2);

	}

	static double sin (double x,double shift) {

        return (3*Math.sin(x-shift));

	}
	
	/******************************************************************
	 * 
	 * @param array 注入数据阵列4x64
	 * @param phase 相位数
	 * @param harmoney 谐波次数
	 * @return
	 ******************************************************************/
	static int[] Filter(int array[], int phase, int harmoney)
	{

		double Real[];//dft变换实部
		double Imag[];//dft变换虚部
		double iReal[];//idft变换实部
		double iImag[];//idft变换虚部
		double outdft[];//变换与逆变换结果
		double d[];
		double temparray[];
		int outdwt[];//小波变换结果
		int sca[];
		
		Real = new double[harmoney];//dft
		Imag = new double[harmoney];//dft
		iReal = new double[64];//idft
		iImag = new double[64];//idft
		outdft = new double[64];
		d = new double[64];
		outdwt = new int[64];
		sca = new int[20];
		temparray = new double[64];

		int i = 0;
		for(; i < 64; i++)
		{
			iReal[i] = 0.0d;
			iImag[i] = 0.0d;
			Real[i] = 0.0d;
			Imag[i] = 0.0d;
			outdft[i] = 0;
		}
		i = 0;
		//dft
		for(i = 0; i < phase; i++)
		{
			for(int j = 0; j < harmoney; j++)
			{
			
				Real[i] += array[j]*(Math.cos((2*PI*i*(j))/phase));//实部
				Imag[i] += array[j]*(Math.sin((2*PI*i*(j))/phase));//虚部				
			}
			
		}
		/*
		 * 当dft变换过后的绝对值
		*/
		/*
		for(int x = 0; x < phase; x++)
		{
			if(Math.abs(Real[x]) < 10.0d)
			{
				Real[x] = 0.0d;
				Imag[x] = 0.0d;
			}
		}
		*/
		//idft
		for(int n = 0; n < phase; n++)
		{
			for(int m = 0; m < harmoney; m++)
			{
				
				iReal[n] += (Real[m] / (harmoney/2)) * Math.cos((2*PI*n*(m))/phase/2);
				iImag[n] += (Imag[m] / (harmoney/2)) * Math.sin((2*PI*n*(m))/phase/2);
				
				//dfttemp[m] += (Real[m] / (harmoney/2)) * Math.cos((2*PI*n*(m+1))/phase);
				//dfttemp[m] += (Imag[m] / (harmoney/2)) * Math.sin((2*PI*n*(m+1))/phase);
				
			}
			//dfttemp[n] = (int) (Math.sqrt((Real[n] * Real[n])/phase + (Imag[n] * Imag[n])/phase));
			
		}
		
		iReal[0] = Real[0] / harmoney;
		iImag[63] = Imag[harmoney/2] / harmoney;
		
		for(int l = 0; l < phase; l++ )
		{
			outdft[l] = (int)Math.sqrt(iReal[l] * iReal[l] + iImag[l] * iImag[l])/2;
		}
		
		//小波变换
		int jw = phase/2;
		for(int w = 0; w <= waveletclass; w++)
		{
			sca[w] = jw;
			jw = jw/4;
		}
		
		//DWT(6,outdft,d,waveletclass,sca);
		//WaveK(outdft,d,waveletclass,32);
		//IDWT(6,outdft,d,waveletclass,sca);
		
		
		//DWT1D(outdft, outdwt, temp, phase, 6);
		//return outdwt;
		/*
		for(int y = 0; y < 64; y++)
		{
			outdwt[y] = (int) outdft[y];
		}
		return outdwt;
		*/
		for(int x = 0; x < 64; x++)
		{
			temparray[x] = (double)(array[x]);
		}
		DWT(6,temparray,d,waveletclass,sca);
		WaveK(temparray,d,waveletclass,32);
		IDWT(6,temparray,d,waveletclass,sca);
		
		//DWT1(temparray,d,waveletclass,sca);
		//WaveK(temparray,d,waveletclass,32);
		//IDWT1(temparray,d,waveletclass,sca);
		
		for(int y = 0; y < 64; y++)
		{
			outdwt[y] = (int) temparray[y];
		}
		return outdwt;
	}
	
	/****************************************************************** 
	*一维卷积函数
	* 
	*说明: 循环卷积,卷积结果的长度与输入信号的长度相同
	*
	*输入参数: data[],输入信号; core[],卷积核; cov[],卷积结果; 
	* n,输入信号长度; m,卷积核长度. 
	*
	******************************************************************/ 
	static void Covlution(int data[], double core[], double cov[], int n, int m) 
	{ 

		int i = 0; 
		int j = 0; 
		int k = 0; 
		//将cov[]清零
		
		for(i = 0; i < n; i++) 
		{ 
			cov[i] = 0;
		} 
		
		for(k = 0; k < 64; k++)
		{
			if(data[k] < 600)
			{
				data[k] = 0;
			}
		}
		
		//前m/2+1行
		i = 0; 
		for(j = 0; j < m/2; j++, i++) 
		{ 
			for(k = m/2-j; k < m; k++ ) 
			{ 
				cov[i] += data[k-(m/2-j)] * core[k];//k针对core[k] 
			} 
		
			for(k = n-m/2+j; k < n; k++ ) 
			{ 
				cov[i] += data[k] * core[k-(n-m/2+j)];//k针对data[k] 
			} 
		} 
		
		//中间的n-m行
		for( i = m/2; i <= (n-m)+m/2; i++)  
		{ 
			for( j = 0; j < m; j++) 
			{ 
				cov[i] += data[i-m/2+j] * core[j]; 
			} 
		} 
		//最后m/2-1行
		i = (n - m) + m/2 + 1; 
		for(j = 1; j < m/2; j++, i++) 
		{ 
			for(k = 0; k < j; k++) 
			{ 
				cov[i] += data[k] * core[m-j-k];//k针对data[k]
			} 
			for(k = 0; k < m-j; k++) 
			{ 
				cov[i] += core[k] * data[n-(m-j)+k];//k针对core[k] 
			} 
		} 
	}
	
	/****************************************************************** 
	*一维小波变换函数 
	*  
	*说明:一维小波变换只变换一次
	* 
	*输入参数: input[],输入信号; output[],小波变换结果，包括尺度系数和
	*小波系数两部分; temp[],存放中间结果;h[],Daubechies小波基低通滤波器系数;  
	*g[],Daubechies小波基高通滤波器系数;n,输入信号长度; m,Daubechies小波基紧支集长度. 
	*   
	******************************************************************/ 
	static void DWT1D(int input[], int output[], double temp[], int n, int m) 
	// double temp[LENGTH] = {0};//????????????? 
	{
		int i = 0; 		
		//尺度系数和小波系数放在一起		
		Covlution(input, h, temp, n, m); 
		
		for(i = 0; i < n; i += 2) 
		{ 
			output[i] = (int)temp[i]; 
		} 
		Covlution(input, g, temp, n, m); 
		
		for(i = 1; i < n; i += 2) 
		{ 
			output[i] = (int)temp[i]; 
		} 
		/*
		//尺度系数和小波系数分开
		Covlution(input, h, temp, n, m); 
		for(i = 0; i < n; i += 2) 
		{ 
			output[i/2] = (int)temp[i];//尺度系数
		} 
		
		Covlution(input, g, temp, n, m); 
		for(i = 1; i < n; i += 2) 
		{ 
			output[n/2+i/2] = (int)temp[i];//小波系数
		} 
		*/
	} 

	//小波分解
	static void DWT(int wlen, double c[], double d[], int m, int sca[])
	{
		int i,j,k,mid;
		int flag[];
		double p,q;
		
		flag = new int[20];
		
		for(flag[0]=0,i=0;i<m;i++)
		{
			flag[i+1]=flag[i]+sca[i];
		}
		
		for(j=1;j<=m;j++)
		{
			for(i=0;i<sca[j];i++)
			{
				p=0;
				q=0;
				
				for(k=0;k<wlen;k++)
				{
					mid=k+2*i;
					if(mid>=sca[j-1])  mid=mid-sca[j-1];
					
					p=p+h[k]*c[flag[j-1]+mid];
					q=q+g[k]*c[flag[j-1]+mid];
				}
				
				c[flag[j]+i]=p;
				d[flag[j]+i]=q;
			}
		}
		
	}

	//小波重构
	static void IDWT(int wlen, double c[], double d[], int m, int sca[])
	{
		int i,j,k,mid;
		int flag[];
		double p,q;
		
		flag = new int[20];

		for(flag[0]=0,i=0;i<m;i++)
		{
			flag[i+1]=flag[i]+sca[i];
		}
		
		for(k=m;k>0;k--)
		{
			for(i=0;i<sca[k];i++)
			{
				p=0;
				q=0;
				
				for(j=0;j<wlen/2;j++)
				{
					mid=i-j;
					if(mid<0)  mid=sca[k]+(i-j);
					
					p+=h[2*j]*c[flag[k]+mid]+g[2*j]*d[flag[k]+mid];
					q+=h[2*j+1]*c[flag[k]+mid]+g[2*j+1]*d[flag[k]+mid];
				}
				
				c[flag[k-1]+2*i]=p;
				c[flag[k-1]+2*i+1]=q;
			}
		}
		
	}
	
	//小波系数
	static void WaveK(double c[], double d[], int m,int DataLength)
	{
		int tmpDataLength,tmp;
		int i;
		tmp = 1;
		for(int k = 0; k < m; k++)
		{
			if(low[k]!=1.0||high[k]!=1.0)
			{
				tmpDataLength=0;
				for(i=1;i<=k+1;i++)
				{
					tmp=1<<i;
					tmpDataLength+=DataLength*2/tmp;
				}
				for (i = 0; i <DataLength/tmp; i ++)
				{	
					c[i+tmpDataLength]=c[i+tmpDataLength]*low[k];
					d[i+tmpDataLength]=d[i+tmpDataLength]*high[k];
					
				}  
			}	
		}
	}

	static int edge_function(int x,int xsize)
	{
	    if(x<0)
			x=-x;
	    if(x>=xsize)
	        x=xsize*2-x-2;
	    return(x);
	}
	
	static void DWT1(double c[], double d[], int m, int sca[])
	{
		int i,j,k;
		int flag[];
		double p,q;
		
		flag = new int[20];
		
		for(flag[0]=0,i=0;i<m;i++)
		{
			flag[i+1]=flag[i]+sca[i];
		}
		
		for(k=1;k<=m;k++)
		{
			for(i=0;i<sca[k-1];i+=2)
			{
				p=0;
				q=0;
				for(j=-(DD-1)/2;j<=(DD-1)/2;j++)
					p=p+h2[j+(DD-1)/2]*c[flag[k-1]+edge_function(i+j,sca[k-1])];
				for(j=-(DD-1)/2+1;j<=(DD-1)/2+1;j++)
					q=q+g2[j+(DD-1)/2-1]*c[flag[k-1]+edge_function(i+j,sca[k-1])];
			
				c[flag[k]+i/2]=p;
				d[flag[k]+i/2]=q;
			}

		}
		
	}

	static void IDWT1(double c[], double d[], int m, int sca[])
	{
		int i,j,k,n;
		int flag[];
		double temp1,temp2;
		double ctemp[],dtemp[];
		
		flag = new int[20];
		ctemp = new double[64];
		dtemp = new double[64];
		
		for(flag[0]=0,i=0;i<m;i++)
		{
			flag[i+1]=flag[i]+sca[i];
		}
		
		for(k=m;k>0;k--)
		{
			for(n=0;n<sca[k];n++)
			{
				ctemp[flag[k-1]+n*2]=c[flag[k]+n];
				ctemp[flag[k-1]+n*2+1]=0;

				dtemp[flag[k-1]+n*2]=d[flag[k]+n];
				dtemp[flag[k-1]+n*2+1]=0;
			}
			for(i=0;i<sca[k-1];i++)
			{
				temp1=0;
				for(j=-(DD-1)/2;j<=(DD-1)/2;j++)
					temp1=temp1+h2[j+(DD-1)/2]*ctemp[flag[k-1]+edge_function(i+j,sca[k-1])];
				c[flag[k-1]+i]=temp1;
			}
			for(i=0;i<sca[k-1];i++)
			{
				temp1=0;
				for(j=-(DD-1)/2-1;j<=(DD-1)/2-1;j++)
					temp1=temp1+g2[j+(DD-1)/2+1]*dtemp[flag[k-1]+edge_function(i+j,sca[k-1])];
				temp2=temp1+c[flag[k-1]+i];
				c[flag[k-1]+i]=temp2;
				
				c[flag[k-1]+i]=2*c[flag[k-1]+i];
			}
			
		}
		
	}

	//调用屏幕键盘
	static void OpenOSK()
	{
		try {
			Runtime.getRuntime().exec("cmd /c osk.exe");
			 
		} catch (IOException e1) {
			e1.printStackTrace();
		}	
	}
	
	//dft变换
	static void dftn(int array[], int temp[],int len, int i)
	{			
		double Rf[],If[];
		Rf = new double[10];
		If = new double[10];
		for(int k = 0; k < len; k++)
		{
			Rf[i] += (double)array[k]*Math.cos(2*PI*i*k/len);
			If[i] += (double)array[k]*Math.sin(2*PI*i*k/len);			
		}
		
		temp[i] = (int) (10.0D*(Math.sqrt(Rf[i]*Rf[i] + If[i]*If[i])/len/2));
	}
}
