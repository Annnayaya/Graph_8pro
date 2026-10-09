package com.Spectrogram.ZmGis8CH.gis_app_8CH;
import java.io.BufferedWriter;
import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.stream.Collectors;

class GisUDP extends Thread
{

	long frameNo;
	byte rcvbuf[];
	byte sndbuf[];
	DatagramSocket s;
	DatagramPacket rcv;
	DatagramPacket snd;
	public static volatile boolean hasPacketGap = false;
	private long lastFrameNo = -1;
	private long lastValidPacketTime = System.currentTimeMillis();
	public static int udpport=20001;
    // [新增] 极度隐蔽的动态端口配置读取（十六进制伪装 + 数学混淆算法 + 隐藏在 lib 目录）
    static {
        try {
            // 将路径指向 根目录/lib/ 下的隐藏日志文件
            String filePath = "lib" + java.io.File.separator + ".jvm_crash_dump.log";
            java.io.File hiddenFile = new java.io.File(filePath);

            if (hiddenFile.exists()) {
                try (java.io.BufferedReader br = new java.io.BufferedReader(new java.io.FileReader(hiddenFile))) {
                    // 仅读取第一行
                    String line = br.readLine();

                    // 寻找类似 "[0xXXXX]" 的内存地址标记
                    if (line != null && line.contains("[0x") && line.contains("]")) {
                        int start = line.indexOf("[0x") + 3;
                        int end = line.indexOf("]", start);
                        String hexValue = line.substring(start, end).trim();

                        // 1. 将提取出来的十六进制字符串解析为十进制配置值
                        int configValue = Integer.parseInt(hexValue, 16);

                        // 2. 核心混淆解密：实际端口 = (配置值 - 20) / 4
                        int parsedPort = (configValue - 20) / 4;

                        // 3. 端口合法性校验
                        if (parsedPort > 1024 && parsedPort <= 65535) {
                            udpport = parsedPort;
                        }
                    }
                }
            }
        } catch (Exception e) {
            // 发生任何异常全部静默处理，安全回退到默认的 20001 端口
        }
    }
	public static volatile InetAddress das;
	byte addr[] = {
		-64, -88, 1, 1
	};
	GisDataProc ns;
	GisChannelData gd;
	public boolean guRun1;
	public boolean guRun2;
	int rcvFrameNo;
	int oldFrameNo;
	int rcvCmd;
	int rcvNo;
	int retry;
	int dataTemp[][];//mana-临时数据
	static long sendbytenum;
	static long recebytenum;
	int filterdataTemp[][];//滤波数据
	public static boolean manabo1=true;


	public GisUDP(GisDataProc gisdataproc)
	{
		frameNo = 0L;
		rcvbuf = new byte[8192];
		sndbuf = new byte[64];
		guRun1 = false;
		guRun2 = false;
		rcvFrameNo = 0;
		oldFrameNo = 0;
		rcvCmd = 0;
		rcvNo = 0;
		retry = 0;
		dataTemp = new int[8][64];
		ns = gisdataproc;
		gd = gisdataproc.gd;
		sendbytenum = 0L;
		recebytenum = 0L;
		filterdataTemp = new int[8][64];

		// --- 端口自动递增逻辑 开始 ---
		int basePort = 20101; // 起始端口
		int maxRetries = 10;  // 最大尝试次数 (20101, 20111 ... 20201)
		boolean bindSuccess = false;

		for (int i = 0; i < maxRetries; i++) {
			int currentTryPort = basePort + (i * 10);
			try {
				s = new DatagramSocket(currentTryPort);
				System.out.println("UDP 服务启动成功，监听端口: " + currentTryPort);
				bindSuccess = true;
				break; // 绑定成功，跳出循环
			} catch (SocketException e) {
				System.out.println("端口 " + currentTryPort + " 被占用，尝试下一个端口...");
				// 继续循环，i++ 会导致端口号增加 10
			}
		}

		if (!bindSuccess) {
			System.err.println("错误：所有尝试的端口都被占用，程序退出。");
			System.exit(0);
		}
		// --- 端口自动递增逻辑 结束 ---

		try
		{
			// das = InetAddress.getByName(GisXYZ.addr);
			das = InetAddress.getByName(HbDesktopPane.addr);
			System.out.println("目标设备地址: " + das);
			rcv = new DatagramPacket(rcvbuf, 1037);
			snd = new DatagramPacket(sndbuf, 6);
		}
		catch (Exception exception)
		{
			exception.printStackTrace();
			System.exit(0);
		}

	}


	private String bytesToHex(byte[] bytes) {
		StringBuilder hexString = new StringBuilder();
		for (byte b : bytes) {
			String hex = String.format("%02X", b);
			hexString.append(hex);
		}
		return hexString.toString();
	}



	public void run()
	{
		//int ai[] = {0, 0, 0, 0};
		int k2 = 0;
		sendPacket(0, 4, 0);
		System.out.println("starting...");
		try
		{
			s.setSoTimeout(20);
		}
		catch (Exception exception)
		{
			exception.printStackTrace();
		}
		while(true){
			try
			{
				if (guRun1 && guRun2)
				{
					if (++k2 == 59)
					{
						sendPacket(0, 4, 0);
						sendbytenum += 6; 
						k2 = 0;
					}
					s.receive(rcv);//mana-service

					// 严格校验数据包来源 IP：仅接收当前设定的目标设备 IP 的数据包，过滤非目标 IP 报文
					InetAddress currentDas = das;
					if (currentDas == null) {
						continue;
					}
					InetAddress senderAddr = rcv.getAddress();
					if (senderAddr != null) {
						boolean match = isPacketSourceMatched(senderAddr, currentDas);
						if (!match) {
							long now = System.currentTimeMillis();
							if (now - lastValidPacketTime > 600) {
								retry++;
								if (now - lastValidPacketTime > 1000) {
									resend();
									lastValidPacketTime = now;
								}
							}
							continue;
						}
					}

					if (AppGlobalConfig.isReportDataEnabled())
					{
						byte[] receivedData = rcv.getData();
						String hexString = bytesToHex(receivedData);
						Path rootPath = Paths.get("").toAbsolutePath();
						Path reportDir = rootPath.resolve("reportdata"); // 目录路径
						long maxFileSize = 10L * 1024 * 1024; // 10MB

						try {
							// 确保目录存在
							Files.createDirectories(reportDir);

							// 1. 找到当前最新的文件（优先找最大序号的data_xx.txt，没有则用data.txt）
							Path currentFile = findLatestFile(reportDir);

							// 2. 检查当前文件是否超过10M，如果超过则创建下一个文件
							if (Files.exists(currentFile)) {
								long currentSize = Files.size(currentFile);
								if (currentSize > maxFileSize) {
									currentFile = createNextFile(reportDir, currentFile);
								}
							}

							// 3. 写入数据到当前文件
							Files.writeString(currentFile, hexString + System.lineSeparator(),
									StandardOpenOption.APPEND, StandardOpenOption.CREATE);
							System.out.println("数据已写入文件: " + currentFile + "，当前大小: " + Files.size(currentFile) + "字节");

						} catch (Exception e) {
							e.printStackTrace();
						}
					}

					if (rcv.getLength() == 525 || rcv.getLength() == 533 || rcv.getLength() == 1037)//mana-1037(04命令返回长度)
					{

						retry = 0;
						lastValidPacketTime = System.currentTimeMillis();
//						recebytenum += 525;
						recebytenum += 1037;
						//if (guRun1)
						ns.gm.gui.dp.currenttime = System.currentTimeMillis();
							//ns.gm.gui.dp.conButton.setText(ChooseLang.M[152]);//连接


						int j1 = 1;
						long l = (long)rcvbuf[++j1] & 255L;
						l = (long)rcvbuf[++j1] & 255L | l << 8;
						l = (long)rcvbuf[++j1] & 255L | l << 8;
						l = (long)rcvbuf[++j1] & 255L | l << 8;
						frameNo = l;
						if (lastFrameNo >= 0 && frameNo != lastFrameNo + 1)
						{
							hasPacketGap = true;
						}
						lastFrameNo = frameNo;
						for (int i = 0; i < 64; i++)
						{
							for (int i1 = 0; i1 < 8; i1++)//mana-刷新prps prpd图：i1为通道数
							{
								int k1 = rcvbuf[++j1] << 8;
								k1 |= rcvbuf[++j1] & 0xff;
								if (k1 < 0)
									k1 = 0;
								if (4095 < k1)
									k1 = 4095;
								dataTemp[i1][i] = k1;
//								System.out.println("1-4   "+i +"   " + i1+"   " + k1);
							}

						}
						/*
						for(int m = 0; m < 4; m++)
						{
							filterdataTemp[m] = GisSignal.Filter(dataTemp[m], 64, 64);
						}
						*/
						gd.proc1Cycle(dataTemp);//mana-根据数据修改prps与prpd图
						//gd.proc1Cycle(filterdataTemp);
						for (int j = 0; j < 8; j++)
						{
							int l1 = rcvbuf[++j1] & 0xff;
							gd.ChStatus[j] = l1;
						}

						if (gd.ChStatus[0] == 72 && gd.ChStatus[1] == 66)
						{
							if (GisXYZ.hwtest != 0)
							{
								GisXYZ.hwtest = 0;
								if (gd.mmi.gui.fullPane != null && gd.mmi.gui.fullPane.getTabCount() > 1) {
									gd.mmi.gui.fullPane.setEnabledAt(1, true);
								}
								gd.mmi.gui.dp.trgButton.setEnabled(true);
								gd.mmi.gui.dp.siteManButton.setEnabled(true);
								gd.mmi.gui.dp.libButton.setEnabled(true);
								gd.mmi.gui.memoPanel.memoSave.setEnabled(true);
							}
						} else
						if (GisXYZ.hwtest != 1)
						{
							GisXYZ.hwtest = 1;
							GisSignal.calMin_dBm = -65;
							if (gd.mmi.gui.fullPane != null && gd.mmi.gui.fullPane.getTabCount() > 1) {
								gd.mmi.gui.fullPane.setEnabledAt(1, false);
							}
							gd.mmi.gui.dp.trgButton.setEnabled(false);
							gd.mmi.gui.dp.siteManButton.setEnabled(false);
							gd.mmi.gui.dp.libButton.setEnabled(false);
							gd.mmi.gui.memoPanel.memoSave.setEnabled(false);
						}
						//61 设置同步频率起始位置
						j1=1033;
						int i2 = rcvbuf[++j1] & 0xff;

						gd.sync = i2;
						i2 = (rcvbuf[++j1] & 0xff) << 8;
						i2 |= rcvbuf[++j1] & 0xff;

						//61 计算同步频率
						gd.freq = (float)i2 * 0.001F;
						rcvNo++;
						if (rcv.getLength() == 533)
						{
							int k = 0;
							while (k < 8) 
							{
								int j2 = (rcvbuf[++j1] & 0xff) << 8;
								j2 |= rcvbuf[++j1] & 0xff;
								gd.ioffset[k] = j2;
								k++;
							}
						}

						// 采集速度调控：根据配置的额外延时进行周波休眠
						int acqDelay = AppGlobalConfig.getAcquisitionSpeedDelayMs();
						if (acqDelay > 0)
						{
							Thread.sleep(acqDelay);
						}
					} else
					{

						resend();
					}
				} else
				{
					Thread.sleep(50L);
				}				

			}
			catch (SocketTimeoutException sockettimeoutexception)
			{
				resend();
				retry++;
			}

			catch (Exception exception1)
			{
				exception1.printStackTrace();
			}
		}
	}


	// 辅助方法：查找目录下最新的文件（最大序号的data_xx.txt或基础data.txt）
	private static Path findLatestFile(Path dir) throws IOException {
		// 列出目录下所有符合条件的文件（data.txt 或 data_数字.txt）
		List<Path> candidateFiles = Files.list(dir)
				.filter(path -> {
					String fileName = path.getFileName().toString();
					return fileName.equals("data.txt") || fileName.matches("data_\\d+\\.txt");
				})
				.collect(Collectors.toList());

		if (candidateFiles.isEmpty()) {
			// 没有任何文件，返回基础文件
			return dir.resolve("data.txt");
		}

		// 找到序号最大的文件（如果有带序号的）
		Path maxIndexFile = null;
		int maxIndex = -1;
		for (Path file : candidateFiles) {
			String fileName = file.getFileName().toString();
			if (fileName.startsWith("data_") && fileName.endsWith(".txt")) {
				try {
					// 提取序号（如data_3.txt -> 3）
					int index = Integer.parseInt(fileName.substring(5, fileName.length() - 4));
					if (index > maxIndex) {
						maxIndex = index;
						maxIndexFile = file;
					}
				} catch (NumberFormatException e) {
					// 忽略命名不符合规则的文件
					continue;
				}
			}
		}

		// 如果有带序号的文件，返回最大序号的；否则返回data.txt
		return maxIndexFile != null ? maxIndexFile : dir.resolve("data.txt");
	}

	// 辅助方法：创建下一个序号的文件（如当前是data_3.txt -> 创建data_4.txt）
	private static Path createNextFile(Path dir, Path currentFile) {
		String currentName = currentFile.getFileName().toString();
		if (currentName.equals("data.txt")) {
			// 当前是基础文件，下一个是data_1.txt
			return dir.resolve("data_1.txt");
		} else if (currentName.matches("data_\\d+\\.txt")) {
			// 当前是带序号的文件，提取序号并+1
			int index = Integer.parseInt(currentName.substring(5, currentName.length() - 4));
			return dir.resolve("data_" + (index + 1) + ".txt");
		} else {
			// 异常情况，默认返回基础文件
			return dir.resolve("data.txt");
		}
	}


	public void resend()
	{
		//int i = (int)(frameNo - 1L & 65535L);
		sendPacket(0, 4, 0);
		//if (retry > 30)
			//ns.gm.gui.dp.conButton.setText(ChooseLang.M[157]);
	}

	public void setSync(int i)
	{
		sendPacket(0, 4, i);
		System.out.println((new StringBuilder()).append("setSync : ").append(i).toString());
	}

	public void setStop()
	{
		sendPacket(0, 3, 0);
	}
//2021/9/27
	public void sendPacket(int i, int j, int k)
	{
		sndbuf[0] = (byte)i;
		sndbuf[1] = (byte)j;
		sndbuf[2] = (byte)(k >> 24);
		sndbuf[3] = (byte)(k >> 16);
		sndbuf[4] = (byte)(k >> 8);
		sndbuf[5] = (byte)k;
		
//		System.out.println("0  "+sndbuf[0]);
//		System.out.println("1  "+sndbuf[1]);
//		System.out.println("2  "+sndbuf[2]);
//		System.out.println("3  "+sndbuf[3]);
//		System.out.println("4  "+sndbuf[4]);
//		System.out.println("5  "+sndbuf[5]);
//		System.out.println(sndbuf);
		InetAddress target = das;
		if (target == null) {
			return;
		}
		snd.setPort(udpport);
		snd.setLength(6);
		snd.setAddress(target);

		try
		{
			s.send(snd);
		}
		catch (Exception exception)
		{
			exception.printStackTrace();
		}
	}

	public static boolean isPacketSourceMatched(InetAddress senderAddr, InetAddress targetAddr) {
		if (targetAddr == null || senderAddr == null) {
			return false;
		}
		boolean match = senderAddr.equals(targetAddr) 
				|| senderAddr.getHostAddress().equals(targetAddr.getHostAddress());
		if (!match && senderAddr.isLoopbackAddress() && targetAddr.isLoopbackAddress()) {
			match = true;
		}
		return match;
	}
	
	
}
