package com.Spectrogram.ZmGis8CH.gis_app_8CH;
import java.awt.BorderLayout;
import java.awt.Color;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import javax.swing.JFrame;
import javax.swing.JProgressBar;

public class GisDataImport extends JFrame {

	private static final long serialVersionUID = 6263779153114315128L;

	class UNZIPThread extends Thread
	{
		public void run()
		{
			gisdataimportframe.setEnabled(false);
			jpb = new JProgressBar(0,1);
			jpb.setStringPainted(true);
			jpb.setBorderPainted(false);
			jpb.setSize(300, 50);
			jframe = new JFrame("Import...");
			jframe.setDefaultCloseOperation(0);
			jframe.setLayout(new BorderLayout());
			jframe.setSize(300, 30);
			jframe.setAlwaysOnTop(true);
			jframe.add(jpb,"Center");
			jframe.setLocationRelativeTo(gisdataimportframe);
			jframe.setResizable(false);
			jframe.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
			jframe.setUndecorated(true);
			jframe.setVisible(true);
			jpb.setMinimum(0);
			//jpb.setMaximum(filesum);
			
			try {
				unZip(zip, des);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			
			/*
			for(int k = 0; k < filesum +1; k++)
			{
				jpb.setValue(k);
				try {
					Thread.sleep(1L);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			*/
			GisDataImport.setisComplete(true);
			jframe.dispose();
			jframe = null;
			gisdataimportframe.setEnabled(true);
			removeNotify();			
		}
	}
	
	private static final int buffer = 4096;  //memo 20230831 2048 to 4096
	GisDataImport gisdataimportframe;
	UNZIPThread unzipthread;
	JProgressBar jpb;
	JFrame jframe;
	private static long filesum;
	private static long count;
	private static String zip;
	private static String des;
	static boolean isComplete = false;
	GisExplorer gisex;
	Database db;
	
	public GisDataImport(String zipPath, String descDir)
	{
		GisDataImport.setisComplete(false);
		unzipthread = new UNZIPThread();
		gisdataimportframe = this;
		zip = zipPath;
		des = descDir;
		unzipthread.start();
		/*
		try {
			unZip(zipPath, descDir);
		} catch (IOException e) {
			e.printStackTrace();
		}
		*/
	}
	
	@SuppressWarnings({ "resource", "static-access" })
	public void unZip(String zipPath, String descDir) throws IOException
    {  
		File onepathFile = new File(descDir);//实例化目的文件
		String allpath = null;
		if(onepathFile.isDirectory())
		{
			allpath = onepathFile.getAbsolutePath() + File.separator;
			
		}
		File pathFile = new File(allpath.substring(0, allpath.lastIndexOf(File.separator)));
		if(!pathFile.exists())//如果不存在，创建文件夹
		{
			pathFile.mkdirs();
		}
		//else
		//{
			//return;
		//}
		File zipFile = new File(zipPath);
		ZipFile zip = new ZipFile(zipFile);
		filesum = zip.size();//文件的条目数
		jpb.setMaximum((int)filesum);
		ZipEntry entry = null;
		FileInputStream fis = new FileInputStream(zipPath);
		ZipInputStream zis = new ZipInputStream(new BufferedInputStream(fis));
		//unzipthread.start();
		count=0;
		while((entry = zis.getNextEntry()) != null)
		{	
			String zipEntryName = entry.getName();
			InputStream in = zip.getInputStream(entry);
			//String outPath = (descDir + zipEntryName).replace("\\", "/");
			String outPath = allpath + zipEntryName;
			File file = new File(outPath.substring(0, outPath.lastIndexOf(File.separator)));
			if(!file.exists())
			{
				file.mkdirs();
			}
			else
			{
				//JOptionPane.showMessageDialog(null, "文件夹已存在请重命名！");
				//return;
			}
			
			if(new File(outPath).isDirectory())
			{
				continue;
			}
			
			OutputStream out = new FileOutputStream(outPath);
			byte[] buf1 = new byte[buffer];
			int len;
			while((len = in.read(buf1)) > 0)
			{
				out.write(buf1, 0, len);
			}
			in.close();
			out.close();			
			jpb.setValue((int)count);
			count++;
			
			double per = jpb.getPercentComplete();
			double nper = 100.0d*per;
			String s1 = "";
			if(per <= 0.3333D)
			{					
				jpb.setForeground(new Color(0xED1235));
				s1 = (new StringBuilder()).append("正在导入数据，请稍后...  ").append(String.format("%.1f", new Object[]{
						Double.valueOf(nper)
				})).append("%").toString();
				jpb.setString(s1);
			}
			if(0.3333D < per && per <= 0.6666D)
			{
				jpb.setForeground(new Color(0xCF9E01));
				s1 = (new StringBuilder()).append("正在导出数据，请稍后...  ").append(String.format("%.1f", new Object[]{
						Double.valueOf(nper)
				})).append("%").toString();
				jpb.setString(s1);
			}
			if(0.6666D < per)
			{
				jpb.setForeground(new Color(0x119D4F));
				s1 = (new StringBuilder()).append("即将完成...  ").append(String.format("%.1f", new Object[]{
						Double.valueOf(nper)
				})).append("%").toString();
				jpb.setString(s1);
			}
			
			try {
				unzipthread.sleep(1L);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
		}
		//isComplete = true;
		/*
		db = new Database("data");
		gisex = new GisExplorer(db);
		DefaultTreeModel dt = new DefaultTreeModel(db);
		dt.reload();
		gisex.dataTree.setModel(dt);
		gisex.dataTree.updateUI();
		*/
    }  
      
    public static boolean isPics(String filename)  
    {  
        boolean flag = false;  
          
        if(filename.endsWith(".jpg") || filename.endsWith(".gif")  || filename.endsWith(".bmp") || filename.endsWith(".png"))  
            flag = true;  
          
        return flag;  
    } 
    
    public static void setisComplete(boolean isComplete)
    {
    	GisDataImport.isComplete = isComplete;
    }
    
    public static boolean getisComplete()
    {
    	return isComplete;
    }
}
