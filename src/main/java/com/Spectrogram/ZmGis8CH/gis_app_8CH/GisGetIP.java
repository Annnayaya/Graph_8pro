package com.Spectrogram.ZmGis8CH.gis_app_8CH;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.nio.charset.StandardCharsets;

import com.alibaba.fastjson.JSONObject;


class GisGetIP extends Thread
{
		// 创建数据包传输对象DatagramSocket绑定端口号
	    DatagramSocket ds;
	    //创建字节数据
	    byte[] data = new byte[1024];
	    // 创建数据包对象，传递字节数组
	    DatagramPacket dp;
	    GisGUI gui;

	    public GisGetIP(int port,GisGUI gisgui){
	        try{
	            ds = new DatagramSocket(port);
	            dp = new DatagramPacket(data, data.length);
	            this.gui=gisgui;
	        }catch (SocketException e) {
	            e.printStackTrace();
	        }
	    }
	
	
	    @Override
	    public void run() {
	        while (true) {
	            try {
	            	
				/*
				 * HbDesktopPane.conButton1.setSelected(true); gui.gm.udp.guRun1 =
				 * HbDesktopPane.conButton1.isSelected(); gui.gm.udp.guRun2 = false;
				 * HbDesktopPane.time.stop();
				 */

	            	
	            	
	            	
	                    ds.receive(dp);
		                //获取发送端的IP地址对象
		                String ip = dp.getAddress().getHostAddress();
		                int port = dp.getPort();
		                String finstr="";
		                
		                finstr = new String(data,StandardCharsets.UTF_8);

		                JSONObject jo = new JSONObject();
		                jo=JSONObject.parseObject(finstr);

		                String devName="";
		                String IP="";
		                String Port="";
		                if (jo.get("devName")!=null)
		                	devName = jo.get("devName").toString();
		                if (jo.get("IP")!=null)
		                 	IP = jo.get("IP").toString();
		                if (jo.get("Port")!=null)
		                 	Port = jo.get("Port").toString();
		                
		                if (jo.get("devName")!=null && jo.get("IP")!=null && jo.get("Port")!=null) {
		                	HbDesktopPane.iptext.setText(IP);
		                	HbDesktopPane.addr=IP;
		                	GisUDP.udpport=Integer.parseInt(Port);
		            		HbDesktopPane.jName.setText("设备名称：" + devName);
		            		HbDesktopPane.jport.setText("端口号：" + Port);
		            		
		            		HbDesktopPane.conButton1.setSelected(true);

		    				gui.gm.udp.guRun1 = HbDesktopPane.conButton1.isSelected();
		    				if (HbDesktopPane.conButton1.isSelected())
		    				{
		    					gui.gm.udp.guRun2 = true;
		    					HbDesktopPane.starttime = System.currentTimeMillis();
		    					HbDesktopPane.time.start();
		    					
		    				} else
		    				{
		    					gui.gm.udp.guRun2 = false;
		    					HbDesktopPane.time.stop();
		    					
		    				}
		                }
		                
	                    System.out.print(finstr);

	            	
	            	
	                
	            } catch (IOException e) {
	                e.printStackTrace();
	            }
	        }
	    }

	
	    public static String convertEncodingFormat(String str, String FormatTo) {
	        String result = null;
	        if (!(str == null || str.length() == 0)) {
	          try {
	            result = new String(str.getBytes(), FormatTo);
	          } catch (UnsupportedEncodingException e) {
	            e.printStackTrace();
	          }
	        }
	        return result;
	      }



	
	
}
