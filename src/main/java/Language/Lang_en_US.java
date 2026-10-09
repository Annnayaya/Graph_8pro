package Language;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.ListResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Lang_en_US extends ListResourceBundle {
	// 1. 配置文件路径（项目根目录），可根据实际路径调整
	private static final String CONFIG_PATH = "Lang_en.cfg";
	// 2. 存储最终使用的键值对（外部文件读取成功则用文件数据，失败则用默认数据）
	private Object[][] data;
	// 3. 日志对象（用于打印状态和异常信息）
	private static final Logger LOGGER = Logger.getLogger(Lang_en_US.class.getName());
	// 4. 默认硬编码数据（与原data数组完全一致，文件不存在时兜底）
	private static final Object[][] DEFAULT_DATA = {
			{"0","English"},{"1",""},{"2","Language:"},{"3","Language has changed, please restart!"},
			{"4","Channel count changed, please restart!"},{"5",""},{"6",""},{"7",""},{"8",""},{"9",""},
			{"10","Void"},{"11","Particle"},{"12","Floating"},{"13","Corona"},
			{"14","（Low Pattern Recog％）"},{"15","Noise"},{"16","Medium discharge"},{"17",""},{"18",""},
			{"19",""},{"20","Stop"},{"21","Realtime"},{"22","Accumulate"},{"23","NoiseGate"},{"24","MeasureNoise"},
			{"25","A"},{"26","B"},{"27","C"},{"28","Data Explorer"},{"29",""},{"30","Connect"},
			{"31","Re-Con!"},{"32","On-L"},{"33","Zoom"},{"34",""},{"35",""},{"36",""},
			{"37",""},{"38",""},{"39",""},{"40","RMS："},{"41","Peak："},{"42","Frequency components 1："},
			{"43","Frequency components 2："},{"44","Gain"},{"45",""},{"46",""},{"47",""},{"48",""},{"49",""},
			{"50","Wireless Sync"},{"51",""},{"52",""},{"53",""},{"54",""},{"55",""},{"56",""},{"57",""},
			{"58",""},{"59",""},{"60","Unit:"},{"61","Atlas Database"},{"62",""},{"63",""},{"64",""},
			{"65",""},{"66",""},{"67",""},{"68",""},{"69",""},{"70",""},{"71",""},{"72",""},
			{"73",""},{"74",""},{"75",""},{"76",""},{"77",""},{"78",""},{"79",""},{"80",""},
			{"81",""},{"82",""},{"83",""},{"84",""},{"85",""},{"86",""},{"87",""},{"88",""},
			{"89",""},{"90",""},{"91",""},{"92",""},{"93",""},{"94",""},{"95",""},{"96",""},
			{"97",""},{"98",""},{"99",""},{"100","Site Manager"},{"101","Site name"},
			{"102","First data acquisition"},{"103","Last data acquisition"},{"104","Acquisition period list"},
			{"105","Open Site"},{"106","Add a Site"},{"107","Enter the name of new site"},{"108","Delete"},
			{"109",""},{"110",""},{"111",""},{"112",""},{"113",""},{"114","Diag Person"},
			{"115","Diag Place"},{"116","Select Print Channel"},{"117","3D PRPS Print"},{"118","Black and White Print"},
			{"119","one-day Report"},{"120","short-term Report"},{"121","Writing details"},{"122","Diag Time"},
			{"123","Diag Person"},{"124","Diag Place"},{"125","Diag Point"},{"126","Print Option"},
			{"127","Post Script"},{"128","Save to File"},{"129","Print to Printer"},{"130","Complete"},{"131","Print report"},
			{"132","Ch1"},{"133","Ch2"},{"134","Ch3"},{"135","Ch4"},{"136","Ch5"},
			{"137","Ch6"},{"138","Ch7"},{"139","Ch8"},{"140","Memo"},{"141","Save"},
			{"142","Diag Person"},{"143","Diag Place"},{"144","Show Memo"},{"145","Current Time:"},
			{"146",""},{"147",""},{"148",""},{"149",""},{"150","Library/Data Window"},{"151","Stand-by"},
			{"152","Connect"},{"153","All Ch"},{"154","Data Retention"},{"155","Site Manage"},{"156","Off Line"},
			{"157","Re-Con!"},{"158",""},{"159",""},{"160","PD-decision（Total）"},
			{"161","Void"},{"162","Particle"},{"163","Floating"},{"164","Corona"},
			{"165","Test Report"},{"166","Diag Date"},{"167","Diag Point"},{"168","Diag Person"},
			{"169","Print Date"},{"170","Post Script"},{"171","Max Diag_Value"},{"172",""},{"173",""},
			{"174",""},{"175",""},{"176",""},{"177",""},{"178",""},{"179",""},{"180","Jan"},
			{"181","Feb"},{"182","Mar"},{"183","Apr"},{"184","May"},{"185","Jun"},
			{"186","Jul"},{"187","Aug"},{"188","Sep"},{"189","Oct"},{"190","Nov"},
			{"191","Dec"},{"192","Su"},{"193","M"},{"194","Tu"},{"195","W"},{"196","Th"},
			{"197","F"},{"198","Sa"},{"199",""},{"200","0% gated"},{"201",""},{"202","many"},
			{"203","some"},{"204","normal"},{"205","a little"},{"206","little"},{"207","Save"},
			{"208","Select time"},{"209","Timing save"},{"210","No save"},{"211","Save Settings"},
			{"212","Set PRPS unit"},{"213","Option"},{"214","30 sec"},{"215","1 min"},{"216","5 min"},
			{"217","10 min"},{"218","30 min"},{"219","1 hour"},{"220","All Time"},
			{"221","Select the Continuous Save Time"},{"222","Select AutoSave"},{"223","AutoSave was Completed."},
			{"224",""},{"225",""},{"226",""},{"227",""},{"228",""},{"229",""},{"230","PhaseShift"},
			{"231",""},{"232","Channel 1"},{"233","Channel 2"},{"234","Channel 3"},{"235","Channel 4"},
			{"236","All Channels"},{"237","gated"},{"238",""},{"239",""},{"240","Signal Peak"},
			{"241","Signal Average"},{"242","Signal Count"},{"243","Void"},{"244","Particle"},
			{"245","Floating"},{"246","Corona"},{"247","Unknown"},{"248",""},{"249","Graph Drawings"},
			{"250","Tools"},{"251","Print PRPD"},{"252","Print Event"},{"253","Save PRPD"},
			{"254","Save PRPS"},{"255","Capture PRPD"},{"256","Capture PRPS"},{"257","Print 1day report"},
			{"258","Print part report"},{"259","Outside Noise"},{"260","Shift"},{"261",""},
			{"262","Library"},{"263",""},{"264","(please, select Site)"},{"265","Enter the name of new PD pattern"},
			{"266",""},{"267","Database"},{"268",""},{"269",""},{"270","GraphColor"},{"271","Black"},
			{"272","White"},{"273","Channel selection"},{"274",""},{"275",""},{"276",""},{"277",""},
			{"278",""},{"279",""},{"280","Discharge times"},{"281",""},{"282",""},{"283",""},
			{"284",""},{"285",""},{"286","Channel 5"},{"287","Channel 6"},{"288","Channel 7"},{"289","Channel 8"},
			{"290",""},{"291",""},{"292",""},{"293",""},{"294",""},{"295",""},{"296",""},
			{"297",""},{"298",""},{"299",""},{"300","Channel 1 View"},{"301","Channel 2 View"},
			{"302","Channel 3 View"},{"303","Channel 4 View"},{"304","PRPS"},{"305","PRPD"},{"306","OPTION"},
			{"307","Site Info/Add/Change"},{"308","Library View"},{"309","Frequency"},{"310","Phase Shift"},
			{"311","A/B/C"},{"312","Base Level Adj."},{"313","Auto/Manual"},{"314","Threshold"},
			{"315",""},{"316",""},{"317",""},{"318",""},{"319",""},{"320","PRPD List"},
			{"321","PRPS List"},{"322",""},{"323",""},{"324",""},{"325",""},{"326",""},
			{"327",""},{"328",""},{"329",""},{"330","Flight map"},{"331","Continuous map"},{"332","Ultrasound"},
			{"333","Waveforms"},{"334","Amplitude Phase"},{"335",""},{"336",""},{"337",""},{"338",""},
			{"339",""},{"340","Test Mode"},{"341","Run Mode"},{"342","Test Time"},{"343","Export Data"},
			{"344","Import Data"},{"345",""},{"346",""},{"347",""},{"348",""},{"349",""},{"350",""},
			{"351",""},{"352",""},{"353",""},{"354",""},{"355",""},{"356",""},{"357",""},
			{"358",""},{"359",""},{"360","Password:"},{"361","Administrator"},{"362","Password error!"},
			{"363","Please input password!"},{"364",""},{"365",""},{"366",""},{"367",""},{"368",""},
			{"369",""},{"370","Advanced Settings"},{"371","Advanced Admin Window"},{"372",""},{"373",""},
			{"374",""},{"375",""},{"376",""},{"377",""},{"378",""},{"379",""},{"380","1 Channel"},
			{"381","2 Channels"},{"382","2 Channels"},{"383","4 Channels"},{"384","Channels:"},
			{"385",""},{"386",""},{"387",""},{"388",""},{"389",""},{"390",""},{"391",""},
			{"392",""},{"393",""},{"394",""},{"395",""},{"396",""},{"397",""},{"398",""},
			{"399",""},{"400","Max"},{"401","Avg"},{"402","amplitude"},{"403","phase"},{"404","time"},
			{"405","Select 100 (Single Select only)"},{"406","Check All"},{"407","Uncheck All"},
			{"408","Start continuous data acquisition"},{"409","Acquire one screen of data"},{"410","Please select a start timestamp"},
			{"411","Persistent Test"},{"412","Waiting for Acq"},{"413","Warming Up"},{"414","Initializing"},{"415","Testing"},
			{"416","Result Held"},{"417","Persistent Peak"},{"418","CSV Error"},{"419","Open Trend Chart"}
	};

	// 构造方法：初始化时优先读取外部文件，失败则用默认数据
	public Lang_en_US() {
		super();
		Object[][] fileData = loadConfigFromFile();
		// 判断文件读取结果：非空则用文件数据，空则用默认数据
		this.data = (fileData != null && fileData.length > 0) ? fileData : DEFAULT_DATA;
	}

	/**
	 * 从外部文件读取配置，解析为键值对数组
	 * @return 解析后的数组（失败则返回null）
	 */
	private Object[][] loadConfigFromFile() {
		List<Object[]> keyValueList = new ArrayList<>();
		File configFile = new File(CONFIG_PATH);

		// 1. 检查文件是否存在
		if (!configFile.exists()) {
			return null;
		}

		// 2. 读取并解析文件
		try (InputStreamReader isr = new InputStreamReader(new FileInputStream(configFile), "UTF-8");
			 BufferedReader br = new BufferedReader(isr)) {

			String line;
			int lineNum = 0;
			while ((line = br.readLine()) != null) {
				lineNum++;
				String trimmedLine = line.trim();
				// 跳过空行和注释（#开头）
				if (trimmedLine.isEmpty() || trimmedLine.startsWith("#")) {
					continue;
				}
				// 按第一个"="分割（避免值中包含"="）
				String[] keyValue = trimmedLine.split("=", 2);
				if (keyValue.length != 2) {
					LOGGER.log(Level.WARNING, "Format error at line {0} (Key=Value required): {1}, ignored",
							new Object[]{lineNum, trimmedLine});
					continue;
				}
				// 提取键值对（去空格）
				String key = keyValue[0].trim();
				String value = keyValue[1].trim();
				keyValueList.add(new Object[]{key, value});
			}

			// 3. 转换为二维数组并返回
			Object[][] result = new Object[keyValueList.size()][2];
			for (int i = 0; i < keyValueList.size(); i++) {
				result[i] = keyValueList.get(i);
			}
			LOGGER.log(Level.INFO, "Loaded config successfully: {0}, count: {1}",
					new Object[]{CONFIG_PATH, result.length});
			return result;

		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, "IO Error reading config file: " + CONFIG_PATH, e);
			LOGGER.log(Level.WARNING, "Using default hardcoded data");
			return null;
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Unknown error parsing config file: " + CONFIG_PATH, e);
			LOGGER.log(Level.WARNING, "Using default hardcoded data");
			return null;
		}
	}

	/**
	 * 重写ListResourceBundle核心方法，返回最终使用的键值对
	 */
	@Override
	protected Object[][] getContents() {
		return this.data;
	}

	// 保留原有的setter和getter，保持兼容性
	public void setData(Object data[][]) {
		this.data = data;
	}

	public Object[][] getData() {
		return data;
	}
}