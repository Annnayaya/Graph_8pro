package com.Spectrogram.ZmGis8CH.gis_app_8CH;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;


public class GisZipFile{
	
	private static final int buffer = 4096;
	//压缩文件
	public File Zip(String sourceDir, String zipFilePath) throws IOException 
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
		return zipFile;
	}
	@SuppressWarnings("resource")
	private void zipFile(File source, String basePath, ZipOutputStream zos) throws IOException
	{
		File[] files = null;
		if (source.isDirectory()) 
		{
			files = source.listFiles();
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
	
	//解压缩文件
	@SuppressWarnings("resource")
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
			ZipEntry entry = null;
			FileInputStream fis = new FileInputStream(zipPath);
			ZipInputStream zis = new ZipInputStream(new BufferedInputStream(fis));
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
			}
			
	    }  
	      
	    public static boolean isPics(String filename)  
	    {  
	        boolean flag = false;  
	          
	        if(filename.endsWith(".jpg") || filename.endsWith(".gif")  || filename.endsWith(".bmp") || filename.endsWith(".png"))  
	            flag = true;  
	          
	        return flag;  
	    } 
}
