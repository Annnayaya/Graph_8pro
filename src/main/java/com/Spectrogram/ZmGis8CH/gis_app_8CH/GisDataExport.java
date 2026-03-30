package com.Spectrogram.ZmGis8CH.gis_app_8CH;


import java.awt.BorderLayout;
import java.awt.Color;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.swing.JFrame;
import javax.swing.JProgressBar;

public class GisDataExport extends JFrame{

	private static final long serialVersionUID = -360915145219738052L;
	GisDataExport gisdataexportframe;
	ZIPThread zipthread;
	long filesum;
	private static String source;
	private static String zip;
	long count;
	JProgressBar jpb;
	JFrame jframe;
	
	class ZIPThread extends Thread
	{
		public void run(){
			jpb = new JProgressBar(0,1);
			jpb.setValue(0);
			jpb.setStringPainted(true);
			jpb.setBorderPainted(false);
			jpb.setSize(300, 50);
			jframe = new JFrame("Export...");
			jframe.setDefaultCloseOperation(0);
			jframe.setLayout(new BorderLayout());
			jframe.setSize(300, 30);
			jframe.setAlwaysOnTop(true);
			jframe.add(jpb,"Center");
			jframe.setLocationRelativeTo(gisdataexportframe);
			jframe.setResizable(false);
			jframe.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
			jframe.setUndecorated(true);
			jframe.setVisible(true);		  
			jpb.setMinimum(0);
			//jpb.setMaximum(filesum);
			filesum = getCount(source);
			jpb.setMaximum((int)filesum);

			try {
				Zip(source, zip);
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
			jframe.dispose();
			jframe = null;
			removeNotify();
		}
	}
		
	public GisDataExport(String sourceDir, String zipFilePath)
	{		
		gisdataexportframe = this;
		zipthread = new ZIPThread();
		source = sourceDir;
		zip = zipFilePath;
		zipthread.start();
		/*
		try {
			
			Zip(sourceDir, zipFilePath);			
		} catch (IOException e) {
			e.printStackTrace();
		}
		*/
	}
	
	public void Zip(String sourceDir, String zipFilePath) throws IOException 
	{			  
		File file = new File(sourceDir);
		File zipFile = new File(zipFilePath);
		ZipOutputStream zos = null;
		try 
		{
				// 创建写出流操作
				OutputStream os = new FileOutputStream(zipFile);
				BufferedOutputStream bos = new BufferedOutputStream(os);
				zos = new ZipOutputStream(bos);  
				String basePath = null;
   
				// 获取目录
				if(file.isDirectory()) 
				{
					basePath = file.getPath();
				}
				else 
				{
					basePath = file.getParent();
				}
				count = 0;
				zipFile(file, basePath, zos);
		}
		finally 
		{
			if(zos != null) 
			{
				zos.closeEntry();
				zos.close();
			}
		}
		//return zipFile;
	}
	
	@SuppressWarnings({ "static-access", "resource" })
	private void zipFile(File source, String basePath, ZipOutputStream zos) throws IOException
	{
		File[] files = null;
		if (source.isDirectory()) 
		{
			files = source.listFiles();
			//filesum = files.length;
			//jpb.setMaximum(filesum);
		} 
		else 
		{
			files = new File[1];
			files[0] = source;
		}
  
		InputStream is = null;
		String pathName;
		byte[] buf = new byte[1024];
		int length = 0;
		
		try
		{
			
			for(File file : files) 
			{
				if(file.isDirectory()) 
				{
					pathName = file.getPath().substring(basePath.length() + 1) + File.separator;
					zos.putNextEntry(new ZipEntry(pathName));
					zipFile(file, basePath, zos);
				}
				else 
				{
					pathName = file.getPath().substring(basePath.length() + 1);
					is = new FileInputStream(file);
					BufferedInputStream bis = new BufferedInputStream(is);
					zos.putNextEntry(new ZipEntry(pathName));
					while ((length = bis.read(buf)) > 0) 
					{
						zos.write(buf, 0, length);
					}
				}
				//jpb.setString("正在导出数据,请稍后...");
				/*
				String s1 = (new StringBuilder()).append(s).append(String.format("%.1fpC", new Object[] {
						Double.valueOf(d)
					})).toString();
					*/
				jpb.setValue((int)count);
				count++;
				double per = jpb.getPercentComplete();
				double nper = 100.0d*per;
				String s1 = "";
				if(per <= 0.3333D)
				{					
					jpb.setForeground(new Color(0xED1235));
					s1 = (new StringBuilder()).append("正在导出数据，请稍后...  ").append(String.format("%.1f", new Object[]{
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
					zipthread.sleep(1L);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		finally 
		{
			if(is != null) 
			{
				is.close();
			}
		}

	}

	//获取文件个数
	private int getCount(String source)
	{
		File files = new File(source);
		int size = 0;
		
		File f[] = files.listFiles();
		size = f.length;
		for(int i = 0; i < f.length; i++)
		{
			if(f[i].isDirectory())
			{
				size = size + getCount(f[i].toString());
				size--;
			}
		}
		return size;
	}

}
