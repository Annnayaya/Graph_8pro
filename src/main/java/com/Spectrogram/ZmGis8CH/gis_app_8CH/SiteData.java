package com.Spectrogram.ZmGis8CH.gis_app_8CH;
import java.io.*;
import java.util.*;
import java.util.zip.*;

import javax.swing.DefaultListModel;
import javax.swing.tree.TreeNode;

class SiteData implements TreeNode
{
	public static String pathName = "data"+File.separator;
	int chNo = 8;
	String siteName = "null";
	static Database mainDB;
	long loadedDate = 0L;
	int[] loadedPrpdPos = new int[1440];
	static int loadedHistory[][][][] = new int[1440][8][37][3];
	File loadedFile = null;
	DataInputStream loadedStream = null;
	long savingTime = 0L;
	int savingPrpdPos = 0;
	int[][][] savingHistory = new int[8][37][3];
	int[][][] savingPRPD = new int[8][64][64];
	RandomAccessFile savingRAF = null;
	
	long savedDay[] = new long[0];
	int savingMin = 0;
	GregorianCalendar savingcal = new GregorianCalendar();
	int runcnt =0;
	long prevSavedDate = 0L;
	long craeteTime = 0L;
	long lastSavedTime = 0L;
	long savedCnt = 0L;
	DefaultListModel<PRPSCell> evList= new DefaultListModel<PRPSCell>();
	//DefaultListModel evList= new DefaultListModel();
	GregorianCalendar eventSaveCal = new GregorianCalendar();
	DefaultListModel<ReportCheck> reportList = new DefaultListModel<ReportCheck>();
	//DefaultListModel reportList = new DefaultListModel();
	GregorianCalendar reportSaveCal= new GregorianCalendar();
	Vector<PeriodBlock> periodBlocks = new Vector<PeriodBlock>();
	int oldDayCnt = -1;

	public SiteData(String s, int i)
	{
		chNo = i;
		siteName = s;
	}

	public void load1day(long l)         //memo 20230831 点击数据库加载一天趋势图
	{
		GregorianCalendar gregoriancalendar = new GregorianCalendar();
		loadedDate = l;
		gregoriancalendar.setTimeInMillis(l);
		String s = String.format("%tY"+File.separator+"%tF", new Object[] {gregoriancalendar, gregoriancalendar});
		loadedFile = new File((new StringBuilder()).append(pathName).append(siteName).append(File.separator).append(s).append(".ghd").toString());
		System.out.print((new StringBuilder()).append("loading ").append(s).toString());
		if (!loadedFile.exists())
		{
			for (int k = 0; k < 1440; k++)
			{
				for (int i = 0; i < 8; i++)
				{
					for (int j2 = 0; j2 < 37; j2++)
					{
						for (int i3 = 0; i3 < 3; i3++)
							loadedHistory[k][i][j2][i3] = -1;

					}
				}
			}
			for (int i1 = 0; i1 < 1440; i1++)
				loadedPrpdPos[i1] = -1;

			return;
		}
		System.out.print(" -> trend histroy loaded");
		try
		{
			System.out.println((new StringBuilder()).append("loading ").append(loadedFile).toString());
			DataInputStream datainputstream = new DataInputStream(new BufferedInputStream(new FileInputStream(loadedFile)));
			for (int j1 = 0; j1 < 1440; j1 ++ )
			{
				for (int j = 0; j < 8; j++)      // mana 20230829 修改部分
				{
					for (int k2 = 0; k2 < 37; k2++)
					{
						for (int j3 = 0; j3 < 3; j3++)
						{
							loadedHistory[j1][j][k2][j3] = datainputstream.readInt();    //memo 20230831 没问题
						}

					}
				}
//				datainputstream.skipBytes(272);
				datainputstream.skipBytes(544);
			}

			long l4 = this.loadedFile.length();
			l4 -= 0x5a0000L;              //memo 20230831 0x2d0000L to 0x5a0000L
			int i4 = (int)(l4 / 32770L);
			for (int k1 = 0; k1 < 1440; k1++)
				this.loadedPrpdPos[k1] = -1;

			for (int l1 = 0; l1 < i4; l1++)
			{
				short word0 = datainputstream.readShort();                  //memo 20230901 获取问题
				if (word0 < 1440 && word0 > 0)
					this.loadedPrpdPos[word0] = l1;
				datainputstream.skipBytes(32768);
			}

			System.out.println(i4);
			for (int i2 = 0; i2 < 1440; i2++)           //memo 20230901 去除非放电信号
			{
				if (this.loadedPrpdPos[i2] < 0){
					for(int l2 = 0;l2 < 8; l2++){
						for(int k3 = 0;k3 < 37;k3++){
							for(int l3 = 0;l3 < 3;l3++){
								loadedHistory[i2][l2][k3][l3] = -1;
							}
						}
					}
				}
			}
			datainputstream.close();
			System.out.println((new StringBuilder()).append(" : ").append(i4).append(" prpd data").toString());
		}
		catch (Exception exception)
		{
			exception.printStackTrace();
		}
		System.out.println(" -> prpd history loaded");
	}

	public void getPRPD(int i, int j, int ai[][])
	{
		if (i < 0)
			i = 0;
		if (i >= 1439)
			i = 1439;
		// memo j&=3 ➡ j&=7
		j &= 7;
		try
		{
			if (this.loadedPrpdPos[i] < 0)  throw new Exception("prpd load exception");
			this.loadedStream = new DataInputStream(new BufferedInputStream(new FileInputStream(loadedFile)));
			this.loadedStream.skipBytes(0x5a0000);        //memo 20230831 0x2d0000 to 0x5a0000
			this.loadedStream.skipBytes(32770 * this.loadedPrpdPos[i] + 2 + j * 8192);   //memo 20230831 20230901修改（未修改）
			for (int k = 0; k < 64; k++)
			{
				for (int i1 = 0; i1 < 64; i1++){
					ai[k][i1] += this.loadedStream.readShort();
				}
			}

			this.loadedStream.close();
		}
		catch (Exception exception)
		{
			System.out.println((new StringBuilder()).append("exception : PRPD not found : ").append(i).append(" / ").append(this.loadedPrpdPos[i]).toString());
			for (int l = 0; l < 64; l++)
			{
				for (int j1 = 0; j1 < 64; j1++)
					ai[l][j1] = -1;

			}

		}
	}

	public static SiteData load(String s)
	{
		File file = new File((new StringBuilder()).append(pathName).append(s).toString());
		if (!file.exists() || !file.isDirectory())
		{
			System.out.println((new StringBuilder()).append("creating ").append(s).toString());
			return createSite(s, 4);
		} else
		{
			SiteData sitedata = new SiteData(s, 4);
			sitedata.reloadDayList();
			sitedata.getReportList();
			sitedata.load1day(System.currentTimeMillis());
			return sitedata;
		}
	}

	public void reloadDayList()
	{
		File file = new File((new StringBuilder()).append(pathName).append(this.siteName).toString());
		if (!file.exists() || !file.isDirectory()) {
			this.savedDay = new long[0];
			return;
		}
		String[] as3 = file.list();
		if (as3 == null || as3.length == 0) {
			this.savedDay = new long[0];
			return;
		}
		int i2 = as3.length;
		int i1 = 0;
		for (int j1 = 0; j1 < i2; j1++)
		{
			File subDir = new File((new StringBuilder()).append(pathName).append(this.siteName).append(File.separator).append(as3[j1]).toString());
			if (subDir.isDirectory())
			{
				String[] as1 = subDir.list();
				if (as1 != null) {
					int k = as1.length;
					for (int i = 0; i < k; i++){
						if (as1[i] != null && as1[i].endsWith(".ghd") && getLongFromDateStr(as1[i]) > 0L){
							i1++;
						}
					}
				}
			}
		}

		this.savedDay = new long[i1];
		i1 = -1;
		for (int k1 = 0; k1 < i2; k1++)
		{
			File subDir = new File((new StringBuilder()).append(pathName).append(this.siteName).append(File.separator).append(as3[k1]).toString());
			if (subDir.isDirectory())
			{
				String[] as2 = subDir.list();
				if (as2 != null) {
					int l = as2.length;
					for (int j = 0; j < l; j++)
					{
						if (as2[j] != null && as2[j].endsWith(".ghd")){
							long l1 = getLongFromDateStr(as2[j]);
							if (l1 > 0L && ++i1 < this.savedDay.length){
								this.savedDay[i1] = l1;
							}
						}
					}
				}
			}
		}

		checkNodes();
	}

	public void prepareSavingFile(long l) throws Exception//�洢�ļ�
	{
		if (this.savingRAF != null)
			this.savingRAF.close();
		this.savingTime = l;
		String s = String.format("%tY"+File.separator+"%tF", new Object[] {
				this.savingcal, this.savingcal
		});
		(new File((new StringBuilder()).append(pathName).append(this.siteName).append(File.separator).append(String.format("%tY", new Object[] {
				this.savingcal
		})).toString())).mkdir();
		File file = new File((new StringBuilder()).append(pathName).append(this.siteName).append(File.separator).append(s).append(".ghd").toString());
		this.savingRAF = new RandomAccessFile(file, "rw");
		long l1 = this.savingRAF.length();
		if (l1 == 0L)
		{
			System.out.println("created new file for save");
			this.savingRAF.seek(0x59fffeL);            //memo 20230831 0x2cffffL to 0x59ffffL
			this.savingRAF.writeByte(0);
		}
		l1 = this.savingRAF.length();
		this.savingPrpdPos = (int)((l1 - 0x5A0000L) / 32770L);            //memo 20230831 0x2d0000L to 0x5A0000L
		System.out.println((new StringBuilder()).append("starting -> prpd pos : ").append(this.savingPrpdPos).toString());
	}

	public void savePeriod(GisPeriodBuffer[] agisperiodbuffer, long l)// memo20230831 更新.ghd
	{
		int j3;
		this.savingcal.setTimeInMillis(l);
		this.savingMin = this.savingcal.get(11) * 60 + this.savingcal.get(12);
		j3 = 0;
		for (int i = 0; i < 8; i++)
		{
			int k1 = agisperiodbuffer[i].copyValue(this.savingHistory[i], savingPRPD[i]);
			if (j3 < k1)
				j3 = k1;
		}

		if (++this.runcnt == 1)
			return;
		try
		{
			//System.out.println("l;:                 "+l );

			if (!sameDayCheck(l, this.savingTime))
				prepareSavingFile(l);
			System.out.println((new StringBuilder()).append("saving min : ").append(this.savingMin).append(" / sig cnt : ").append(j3).toString());
			this.savingRAF.seek(this.savingMin * 4096);
			for (int j = 0; j < 8; j++)
			{
				for (int l1 = 0; l1 < 37; l1++)
				{
					for (int l2 = 0; l2 < 3; l2++)
						this.savingRAF.writeInt(this.savingHistory[j][l1][l2]);//���ĸ��ֽ�д����ļ����ȸ��ֽ�

				}

			}

			for (int k = 0; k < 8; k++)
			{
				for (int i2 = 5; i2 < 13; i2++){
					System.out.print((new StringBuilder()).append(this.savingHistory[k][i2][1]).append("\t").toString());
				}
				System.out.println();
			}

			System.out.println((new StringBuilder()).append("\t prpd pos : ").append(this.savingPrpdPos).toString());
			this.savingRAF.seek(0x5a0000 + 32770 * this.savingPrpdPos);   //memo 20230831 0x2d0000L to 0x5A0000L
			this.savingRAF.writeShort((short)this.savingMin);//�������ֽڽ�shortд����ļ����ȸ��ֽ�
			for (int k3 = 0; k3 < 8; k3++)
			{
				for (int i1 = 0; i1 < 64; i1++)
				{
					for (int j2 = 0; j2 < 64; j2++){
						this.savingRAF.writeShort((short)this.savingPRPD[k3][i1][j2]);//�洢PRPD���
					}
				}

			}

			this.savingPrpdPos++;
			if (sameDayCheck(this.loadedDate, l))
			{
				System.out.println("updating loaded data");//�������
				for (int j1 = 0; j1 < 8; j1++)
				{
					for (int k2 = 0; k2 < 37; k2++)
					{
						for (int i3 = 0; i3 < 3; i3++){
							loadedHistory[this.savingMin][j1][k2][i3] = this.savingHistory[j1][k2][i3];
						}
					}

				}

				this.loadedPrpdPos[this.savingMin] = this.savingPrpdPos - 1;
			}
			if (!sameDayCheck(this.prevSavedDate, l))
			{
				prevSavedDate = l;
				reloadDayList();
				System.out.println("reloaded day list");//����PRPD�б����
			}
		}
		catch (Exception exception)
		{
			exception.printStackTrace();
		}
		return;
	}

	static boolean sameDayCheck(long l, long l1)//��ͬ����
	{
		GregorianCalendar gregoriancalendar = new GregorianCalendar();
		GregorianCalendar gregoriancalendar1 = new GregorianCalendar();
		gregoriancalendar.setTimeInMillis(l);
		gregoriancalendar1.setTimeInMillis(l1);
		if (gregoriancalendar.get(1) != gregoriancalendar1.get(1))//年
			return false;
		if (gregoriancalendar.get(5) != gregoriancalendar1.get(5))//日
			return false;
		return gregoriancalendar.get(2) == gregoriancalendar1.get(2);//月
	}

	static long getLongFromDateStr(String s)
	{
		s = s.substring(0, 10);
		if (s.charAt(4) != '-')
			return 0L;
		if (s.charAt(7) != '-')
		{
			return 0L;
		} else
		{
			int i = Integer.valueOf(s.substring(0, 4)).intValue();//年
			int j = Integer.valueOf(s.substring(5, 7)).intValue() - 1;//月
			int k = Integer.valueOf(s.substring(8, 10)).intValue();//日
			return (new GregorianCalendar(i, j, k)).getTimeInMillis();
		}
	}

	public static SiteData createSite(String s, int i)
	{
		File file = new File((new StringBuilder()).append(pathName).append(s).toString());
		File file1 = new File((new StringBuilder()).append("report"+File.separator).append(s).toString());
		if (!file.exists() || !file.isDirectory())
		{
			file.mkdir();
			file1.mkdir();
		}
		return new SiteData(s, 8);
	}


	public void getEventList(long l, long l1)
	{
		GregorianCalendar gregoriancalendar = new GregorianCalendar();
		evList.clear();
		do
		{
			gregoriancalendar.setTimeInMillis(l);
			String s = (new StringBuilder()).append(pathName).append(siteName).append(File.separator).append(String.format("%tY"+File.separator+"%tF", new Object[] {gregoriancalendar, gregoriancalendar})).toString();
			String[] as = (new File(s)).list();
			if (as == null)
				return;
			int k = as.length;
			int i = 0;
			for (; i < k; i++)
			{
				if (!as[i].endsWith(".ge4"))
					continue;
				PRPSCell prpscell = getCellFromEvName(as[i]);
				int j;
				for (j = 0; j < evList.size() && ((PRPSCell)evList.get(j)).time <= prpscell.time; j++);
				evList.add(j, prpscell);
			}

			gregoriancalendar.setTimeInMillis(l + 0x5265c00L);
			gregoriancalendar.set(11, 0);
			gregoriancalendar.set(12, 0);
			gregoriancalendar.set(13, 0);
			gregoriancalendar.set(14, 0);
			l = gregoriancalendar.getTimeInMillis();
		} while (l <= l1);
	}

	public PRPSCell getCellFromEvName(String s)
	{
		String s1 = s.substring(0, 19);
		if (s1.charAt(4) != '-')
			return null;
		if (s1.charAt(7) != '-')
			return null;
		if (s1.charAt(10) != '_')
			return null;
		if (s1.charAt(13) != ';')
			return null;
		if (s1.charAt(16) != ';')
		{
			return null;
		} else
		{
			int i = Integer.valueOf(s1.substring(0, 4)).intValue();//年
			int j = Integer.valueOf(s1.substring(5, 7)).intValue() - 1;//月
			int k = Integer.valueOf(s1.substring(8, 10)).intValue();//日
			int l = Integer.valueOf(s1.substring(11, 13)).intValue();//时
			int i1 = Integer.valueOf(s1.substring(14, 16)).intValue();//分
			int j1 = Integer.valueOf(s1.substring(17, 19)).intValue();//秒
			return new PRPSCell((new GregorianCalendar(i, j, k, l, i1, j1)).getTimeInMillis(), l, i1, j1);
		}
	}

	public void getEventValue(long l, int ai[])
	{
		GregorianCalendar gregoriancalendar = new GregorianCalendar();
		gregoriancalendar.setTimeInMillis(l);
		String s = (new StringBuilder()).append(pathName).append(siteName).append(File.separator).append(String.format("%tY"+File.separator+"%tF", new Object[] {
			gregoriancalendar, gregoriancalendar
		})).toString();
		String s1 = String.format("%tF_%tT", new Object[] {
			gregoriancalendar, gregoriancalendar
		}).replace(':', ';');
		try
		{
			FileInputStream fileinputstream = new FileInputStream(new File((new StringBuilder()).append(s).append(File.separator).append(s1).append(".ge4").toString()));
			ZipInputStream zipinputstream = new ZipInputStream(fileinputstream);
			zipinputstream.getNextEntry();
			DataInputStream datainputstream = new DataInputStream(zipinputstream);
			datainputstream.skip(40960L);
			for (int i = 0; i < 280; i++)
				ai[i] = datainputstream.readShort();

			datainputstream.close();
		}
		catch (Exception exception)
		{
			System.out.println("old file type (SiteData.getEvent(..))");
		}
	}

	public void getEvent(long l, int ai[], int ai1[], int ai2[], int ai3[], int ai4[], int ai5[], int ai6[], int ai7[], int ai8[])//��ȡPRPS���
	{
		GregorianCalendar gregoriancalendar = new GregorianCalendar();
		gregoriancalendar.setTimeInMillis(l);
		String s = (new StringBuilder()).append(pathName).append(siteName).append(File.separator).append(String.format("%tY"+File.separator+"%tF", new Object[] {
			gregoriancalendar, gregoriancalendar
		})).toString();
		String s1 = String.format("%tF_%tT", new Object[] {
			gregoriancalendar, gregoriancalendar
		}).replace(':', ';');
		try
		{
			FileInputStream fileinputstream = new FileInputStream(new File((new StringBuilder()).append(s).append(File.separator).append(s1).append(".ge4").toString()));
			ZipInputStream zipinputstream = new ZipInputStream(fileinputstream);
			zipinputstream.getNextEntry();
			DataInputStream datainputstream = new DataInputStream(zipinputstream);

			for (int i = 0; i < 5120; i++)
				ai[i] = datainputstream.readShort();

			for (int j = 0; j < 5120; j++)
				ai1[j] = datainputstream.readShort();

			for (int k = 0; k < 5120; k++)
				ai2[k] = datainputstream.readShort();

			for (int i1 = 0; i1 < 5120; i1++)
				ai3[i1] = datainputstream.readShort();
			
			for (int i1 = 0; i1 < 5120; i1++)
				ai4[i1] = datainputstream.readShort();
			
			for (int i1 = 0; i1 < 5120; i1++)
				ai5[i1] = datainputstream.readShort();
			
			for (int i1 = 0; i1 < 5120; i1++)
				ai6[i1] = datainputstream.readShort();
			
			for (int i1 = 0; i1 < 5120; i1++)
				ai7[i1] = datainputstream.readShort();

			for (int j1 = 0; j1 < 280; j1++)
				ai8[j1] = datainputstream.readShort();

			datainputstream.close();
		}
		catch (Exception exception)
		{
			System.out.println("old file type (SiteData.getEvent(..))");
		}
	}

	public void saveEvent(long l, int ai[], int ai1[], int ai2[], int ai3[], int ai5[], int ai6[], int ai7[], int ai8[], int i, int ai4[])//�洢PRPS���
	{	
		eventSaveCal.setTimeInMillis(l);
		String s = (new StringBuilder()).append(pathName).append(siteName).append(File.separator).append(String.format("%tY"+File.separator+"%tF", new Object[] {
			eventSaveCal, eventSaveCal
		})).toString();
		String s1 = String.format("%tF_%tT", new Object[] {eventSaveCal, eventSaveCal}).replace(':', ';');
		ZipEntry zipentry = new ZipEntry(s1);
		System.out.println((new StringBuilder()).append("saving event : ").append(s1).toString());
		(new File((new StringBuilder()).append(pathName).append(siteName).append(File.separator).append(String.format("%tY", new Object[] {
			eventSaveCal
		})).toString())).mkdir();
		(new File(s)).mkdir();
		try
		{
			(new File(s)).mkdir();
			ZipOutputStream zipoutputstream = new ZipOutputStream(new FileOutputStream(new File((new StringBuilder()).append(s).append(File.separator).append(s1).append(".ge4").toString())));
			zipoutputstream.setLevel(1);
			zipoutputstream.putNextEntry(zipentry);
			DataOutputStream dataoutputstream = new DataOutputStream(zipoutputstream);
			int l1 = ((i - 79) + 128) * 64;
			for (int j = 0; j < 5120; j++)
				dataoutputstream.writeShort((short)ai[(l1 + j) % GisXYZ.bufferLen]);//存储第一个通道数据

			for (int k = 0; k < 5120; k++)
				dataoutputstream.writeShort((short)ai1[(l1 + k) % GisXYZ.bufferLen]);//存储第二个通道数据

			for (int i1 = 0; i1 < 5120; i1++)
				dataoutputstream.writeShort((short)ai2[(l1 + i1) % GisXYZ.bufferLen]);//存储第三个通道数据

			for (int j1 = 0; j1 < 5120; j1++)
				dataoutputstream.writeShort((short)ai3[(l1 + j1) % GisXYZ.bufferLen]);//存储第四个通道数据

			for (int j3 = 0; j3 < 5120; j3++)
				dataoutputstream.writeShort((short)ai5[(l1 + j3) % GisXYZ.bufferLen]);//存储第五个通道数据

			for (int k3 = 0; k3 < 5120; k3++)
				dataoutputstream.writeShort((short)ai6[(l1 + k3) % GisXYZ.bufferLen]);//存储第六个通道数据

			for (int i13 = 0; i13 < 5120; i13++)
				dataoutputstream.writeShort((short)ai7[(l1 + i13) % GisXYZ.bufferLen]);//存储第七个通道数据

			for (int j13 = 0; j13 < 5120; j13++)
				dataoutputstream.writeShort((short)ai8[(l1 + j13) % GisXYZ.bufferLen]);//存储第八个通道数据

			
			for (int k1 = 0; k1 < 280; k1++)
				dataoutputstream.writeShort(ai4[k1]);//存储判断的结果

//			for (int k1p = 0; k1p < 280;k1p++)
//				System.out.println(k1p+ " 结果 ：" + ai4[k1p]);

			zipoutputstream.closeEntry();
			zipoutputstream.finish();
			dataoutputstream.close();
		}
		catch (Exception exception)
		{
			exception.printStackTrace();
		}
		if (sameDayCheck(l, loadedDate))
		{
			evList.addElement(getCellFromEvName(s1));
			System.out.println((new StringBuilder()).append("added [ ").append(evList.lastElement()).append(" ] to eventlist").toString());
		}
	}

	public void saveReport(ReportCheck reportcheck)
	{
		reportSaveCal.setTimeInMillis(System.currentTimeMillis());
		String s = (new StringBuilder()).append(pathName).append(siteName).append(File.separator).append(String.format("%tY"+File.separator+"Report", new Object[] {
			reportSaveCal, reportSaveCal
		})).toString();
		String s1 = String.format("%tF_%tT", new Object[] {
			reportSaveCal, reportSaveCal
		}).replace(':', ';');
		System.out.println((new StringBuilder()).append("saving report : ").append(s1).toString());
		(new File((new StringBuilder()).append(pathName).append(siteName).append(File.separator).append(String.format("%tY", new Object[] {
			eventSaveCal
		})).toString())).mkdir();
		try
		{
			(new File(s)).mkdir();
			DataOutputStream dataoutputstream = new DataOutputStream(new FileOutputStream(new File((new StringBuilder()).append(s).append(File.separator).append(s1).append(".ge4").toString())));
			dataoutputstream.close();
		}
		catch (Exception exception)
		{
			exception.printStackTrace();
		}
		reportList.add(reportList.size(), reportcheck);
	}

	public ReportCheck getReportCheck(int i)
	{
		return (ReportCheck)reportList.get(i);
	}

	private void getReportList()
	{
	}

	@SuppressWarnings("rawtypes")
	public Enumeration children()
	{
		checkNodes();
		return new Enumeration() 
		{
			int p = 0;
			public boolean hasMoreElements()
			{
				return periodBlocks.size() > p;
			}

			public TreeNode nextElement()
			{
				return (TreeNode)periodBlocks.get(p++);
			}
		};
	}

	public boolean getAllowsChildren()
	{
		return true;
	}

	public TreeNode getChildAt(int i)
	{
		checkNodes();
		return (TreeNode)periodBlocks.get(i);
	}

	public int getChildCount()
	{
		checkNodes();
		return periodBlocks.size();
	}

	public int getIndex(TreeNode treenode)
	{
		checkNodes();
		return periodBlocks.indexOf(treenode);
	}

	public TreeNode getParent()
	{
		return mainDB;
	}

	public boolean isLeaf()
	{
		return false;
	}

	private void checkNodes()
	{
		if (oldDayCnt == savedDay.length)
			return;
		System.out.println((new StringBuilder()).append(siteName).append(" - daycnt : ").append(oldDayCnt).append(" -> ").append(savedDay.length).toString());
		oldDayCnt = savedDay.length;
		long l = 0L;
		long l1 = 0L;
		for (int i = 0; i < oldDayCnt; i++)
		{
			if (l == 0L && l1 == 0L)
			{
				l = savedDay[i];
				l1 = savedDay[i];
				continue;
			}
			if (savedDay[i] - l1 <= 0x5265c00L)
			{
				l1 = savedDay[i];
			} else
			{
				periodBlocks.add(new PeriodBlock(this, l, (l1 + 0x5265c00L) - 1L));
				l = savedDay[i];
				l1 = savedDay[i];
			}
		}

		if (oldDayCnt > 0)
			periodBlocks.add(new PeriodBlock(this, l, (l1 + 0x5265c00L) - 1L));
	}

	public String toString()
	{
		return siteName;
	}

}
