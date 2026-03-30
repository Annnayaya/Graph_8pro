package com.Spectrogram.ZmGis8CH.gis_app_8CH;
import java.io.IOException;
import java.util.Scanner;

//无用类
class CPUID {
	String serial;
	public CPUID()
	{		
		serial = new String();
	}
	
	public String GetCPUID(){
		long start = System.currentTimeMillis();
	
		  Process process = null;
		try {
			process = Runtime.getRuntime().exec(
			    new String[] { "wmic", "cpu", "get", "ProcessorId" });
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
		  try {
			process.getOutputStream().close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
		  @SuppressWarnings("resource")
		Scanner sc = new Scanner(process.getInputStream());
	
		  String property = sc.next();
	
		  serial = sc.next();
	
		  System.out.println(property + ": " + serial);
	
		  System.out.println("time:" + (System.currentTimeMillis() - start));
		  return serial;
	}
}
