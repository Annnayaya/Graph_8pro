using System;
using System.Collections.Generic;
using System.Drawing;
using System.IO;
using System.Security.Cryptography;
using System.Text;
using System.Windows.Forms;

namespace GraphConfigManager
{
    static class Program
    {
        [STAThread]
        static void Main()
        {
            Application.EnableVisualStyles();
            Application.SetCompatibleTextRenderingDefault(false);
            Application.Run(new MainForm());
        }
    }

    public class MainForm : Form
    {
        private static readonly byte[] AesKey = Encoding.UTF8.GetBytes("Graph8Pro#Sec217");
        private static readonly byte[] AesIv = Encoding.UTF8.GetBytes("GisConfigIv@2026");
        private const string EncPrefix = "#-- GRAPH8PRO ENCRYPTED CONFIG --#\nENC:";

        // 默认值常量
        public const string DefaultVersion = "Pro v2.2.8";
        public const string DefaultDeviceIp = "192.168.10.60";
        public const bool DefaultReportData = false;
        public const bool DefaultPersistentPeak = false;
        public const bool DefaultSpectrogramFollow = true;
        public const decimal DefaultEpsilonDb = 0.20m;
        public const decimal DefaultJitterDb = 0.00m;
        public const int DefaultDecimals = 0;
        public const decimal DefaultMaxStepDb = 0.30m;
        public const int DefaultInitialGroups = 3;
        public const int DefaultSwitchJudgeGroups = 3;
        public const bool DefaultSwitchShowDash = false;
        public const bool DefaultSpectrogramRolling = false;
        public const int DefaultIpSwitchMode = 2;
        public const string DefaultPresetIp1 = "192.168.10.60";
        public const string DefaultPresetName1 = "预设1";
        public const string DefaultPresetIp2 = "192.168.10.61";
        public const string DefaultPresetName2 = "预设2";
        public const string DefaultPresetIp3 = "192.168.1.100";
        public const string DefaultPresetName3 = "预设3";
        public const bool DefaultAlarmEnabled = false;
        public const decimal DefaultAlarmThresholdGlobal = -40m;

        // 缓存非直接编辑字段（采集速度、通道报警阈值）以防保存时覆盖丢失
        private int _cachedAcqSpeedDelayMs = 0;
        private string _cachedAcqSpeedMode = "FULL_50HZ";
        private decimal _cachedAlarmThresholdGlobal = -40m;
        private decimal[] _cachedAlarmThresholdChannels = new decimal[8];

        // 当前目标配置文件路径与软件目录
        private string _targetConfigFile = null;
        private string _appDirectory = null;

        // 界面控件
        private Label lblAppDir;
        private Button btnBrowseExe;
        private Label lblLicenseStatus;
        private Button btnImportLicense;
        private bool _isLicensePresent = false;

        private TextBox txtVersion;
        private TextBox txtDeviceIp;
        private ComboBox cmbRefreshMode;
        private CheckBox chkReportData;

        private GroupBox grpPeak;
        private CheckBox chkPersistentPeak;
        private CheckBox chkSpectrogramFollow;
        private NumericUpDown numEpsilon;
        private NumericUpDown numJitter;
        private ComboBox cmbDecimals;
        private NumericUpDown numMaxStep;

        private GroupBox grpSwitch;
        private NumericUpDown numInitialGroups;
        private NumericUpDown numSwitchJudgeGroups;
        private CheckBox chkSwitchShowDash;

        private GroupBox grpIpSwitch;
        private ComboBox cmbIpSwitchMode;
        private TextBox txtPresetName1;
        private TextBox txtPresetIp1;
        private TextBox txtPresetName2;
        private TextBox txtPresetIp2;
        private TextBox txtPresetName3;
        private TextBox txtPresetIp3;

        private GroupBox grpAlarm;
        private CheckBox chkAlarmEnabled;

        private Label lblStatus;
        private Button btnSave;
        private Button btnReset;
        private Button btnReload;

        public MainForm()
        {
            InitializeComponent();
            this.Load += MainForm_Load;
        }

        private void InitializeComponent()
        {
            this.Text = "Graph_8Pro 参数配置管理器";
            try
            {
                string exeDir = AppDomain.CurrentDomain.BaseDirectory;
                string[] iconPaths = new string[] {
                    Path.Combine(exeDir, "gear.ico"),
                    Path.Combine(exeDir, "..", "gear.ico"),
                    Path.Combine(exeDir, "tools", "ConfigManager", "gear.ico"),
                    "gear.ico"
                };
                foreach (string p in iconPaths)
                {
                    if (File.Exists(p))
                    {
                        this.Icon = new Icon(p);
                        break;
                    }
                }
                if (this.Icon == null)
                {
                    this.Icon = Icon.ExtractAssociatedIcon(System.Reflection.Assembly.GetExecutingAssembly().Location);
                }
            }
            catch {}
            this.Size = new Size(980, 980);
            this.MinimumSize = new Size(940, 780);
            this.StartPosition = FormStartPosition.CenterScreen;
            this.Font = new Font("Microsoft YaHei", 9F, FontStyle.Regular, GraphicsUnit.Point, 134);
            this.BackColor = Color.FromArgb(248, 250, 252);

            // 主面板
            Panel mainPanel = new Panel();
            mainPanel.Dock = DockStyle.Fill;
            mainPanel.AutoScroll = true;
            mainPanel.Padding = new Padding(16, 12, 16, 12);
            this.Controls.Add(mainPanel);

            int curY = 10;

            // 1. 顶部标头卡片 (软件路径定位与授权状态)
            GroupBox grpPath = new GroupBox();
            grpPath.Text = "Graph_8Pro 软件目录定位与授权状态";
            grpPath.Font = new Font("Microsoft YaHei", 9F, FontStyle.Bold);
            grpPath.Location = new Point(16, curY);
            grpPath.Size = new Size(900, 102);
            grpPath.Anchor = AnchorStyles.Top | AnchorStyles.Left | AnchorStyles.Right;

            Label lblDirTitle = new Label();
            lblDirTitle.Text = "Graph_8Pro 软件目录:";
            lblDirTitle.Font = new Font("Microsoft YaHei", 9F, FontStyle.Regular);
            lblDirTitle.Location = new Point(15, 27);
            lblDirTitle.Size = new Size(155, 20);
            grpPath.Controls.Add(lblDirTitle);

            lblAppDir = new Label();
            lblAppDir.Text = "未定位";
            lblAppDir.Font = new Font("Microsoft YaHei", 9F, FontStyle.Regular);
            lblAppDir.ForeColor = Color.FromArgb(37, 99, 235);
            lblAppDir.Location = new Point(175, 27);
            lblAppDir.Size = new Size(460, 20);
            lblAppDir.AutoEllipsis = true;
            grpPath.Controls.Add(lblAppDir);

            btnBrowseExe = new Button();
            btnBrowseExe.Text = "选择主程序 (.exe)";
            btnBrowseExe.Font = new Font("Microsoft YaHei", 9F);
            btnBrowseExe.Location = new Point(645, 22);
            btnBrowseExe.Size = new Size(155, 30);
            btnBrowseExe.Anchor = AnchorStyles.Top | AnchorStyles.Right;
            btnBrowseExe.Click += (s, e) => PromptSelectExe(true);
            grpPath.Controls.Add(btnBrowseExe);

            Label lblLicenseTitle = new Label();
            lblLicenseTitle.Text = "授权状态 (lib/license.prop):";
            lblLicenseTitle.Font = new Font("Microsoft YaHei", 9F, FontStyle.Regular);
            lblLicenseTitle.Location = new Point(15, 63);
            lblLicenseTitle.Size = new Size(160, 20);
            grpPath.Controls.Add(lblLicenseTitle);

            lblLicenseStatus = new Label();
            lblLicenseStatus.Text = "未检测到授权文件";
            lblLicenseStatus.Font = new Font("Microsoft YaHei", 9F, FontStyle.Bold);
            lblLicenseStatus.ForeColor = Color.FromArgb(185, 28, 28);
            lblLicenseStatus.Location = new Point(175, 63);
            lblLicenseStatus.Size = new Size(460, 20);
            lblLicenseStatus.AutoEllipsis = true;
            grpPath.Controls.Add(lblLicenseStatus);

            btnImportLicense = new Button();
            btnImportLicense.Text = "导入授权文件";
            btnImportLicense.Font = new Font("Microsoft YaHei", 9F);
            btnImportLicense.Location = new Point(645, 58);
            btnImportLicense.Size = new Size(155, 30);
            btnImportLicense.Anchor = AnchorStyles.Top | AnchorStyles.Right;
            btnImportLicense.Click += (s, e) => ImportLicenseFile();
            grpPath.Controls.Add(btnImportLicense);

            mainPanel.Controls.Add(grpPath);
            curY += 112;

            // 2. 分组: 系统通用设置
            GroupBox grpSys = new GroupBox();
            grpSys.Text = "1. 系统通用设置";
            grpSys.Font = new Font("Microsoft YaHei", 9F, FontStyle.Bold);
            grpSys.Location = new Point(16, curY);
            grpSys.Size = new Size(900, 165);
            grpSys.Anchor = AnchorStyles.Top | AnchorStyles.Left | AnchorStyles.Right;

            Label lblVer = new Label();
            lblVer.Text = "软件版本号 (app.version):";
            lblVer.Font = new Font("Microsoft YaHei", 9F, FontStyle.Regular);
            lblVer.Location = new Point(15, 26);
            lblVer.Size = new Size(240, 20);
            grpSys.Controls.Add(lblVer);

            txtVersion = new TextBox();
            txtVersion.Font = new Font("Microsoft YaHei", 9F, FontStyle.Regular);
            txtVersion.Location = new Point(265, 23);
            txtVersion.Size = new Size(200, 23);
            grpSys.Controls.Add(txtVersion);

            Label lblVerTip = new Label();
            lblVerTip.Text = "注: 界面右下角状态栏展示的版本号";
            lblVerTip.Font = new Font("Microsoft YaHei", 8.5F, FontStyle.Regular);
            lblVerTip.ForeColor = Color.Gray;
            lblVerTip.Location = new Point(480, 26);
            lblVerTip.Size = new Size(320, 20);
            grpSys.Controls.Add(lblVerTip);

            Label lblIp = new Label();
            lblIp.Text = "默认设备 IP (default.ip):";
            lblIp.Font = new Font("Microsoft YaHei", 9F, FontStyle.Regular);
            lblIp.Location = new Point(15, 56);
            lblIp.Size = new Size(240, 20);
            grpSys.Controls.Add(lblIp);

            txtDeviceIp = new TextBox();
            txtDeviceIp.Font = new Font("Microsoft YaHei", 9F, FontStyle.Regular);
            txtDeviceIp.Location = new Point(265, 53);
            txtDeviceIp.Size = new Size(200, 23);
            grpSys.Controls.Add(txtDeviceIp);

            Label lblIpTip = new Label();
            lblIpTip.Text = "注: 采集通信默认目标 IP 地址";
            lblIpTip.Font = new Font("Microsoft YaHei", 8.5F, FontStyle.Regular);
            lblIpTip.ForeColor = Color.Gray;
            lblIpTip.Location = new Point(480, 56);
            lblIpTip.Size = new Size(320, 20);
            grpSys.Controls.Add(lblIpTip);

            Label lblRefresh = new Label();
            lblRefresh.Text = "谱图刷新模式 (spectrogram.rolling.enabled):";
            lblRefresh.Font = new Font("Microsoft YaHei", 9F, FontStyle.Regular);
            lblRefresh.Location = new Point(15, 86);
            lblRefresh.Size = new Size(245, 20);
            grpSys.Controls.Add(lblRefresh);

            cmbRefreshMode = new ComboBox();
            cmbRefreshMode.DropDownStyle = ComboBoxStyle.DropDownList;
            cmbRefreshMode.Font = new Font("Microsoft YaHei", 9F, FontStyle.Regular);
            cmbRefreshMode.Items.AddRange(new object[] {
                "整组80周波跳变 (每1.6秒整屏切换，传统工业对照模式 · 默认)",
                "逐周波平滑滚动 (每20ms连续流动，丝滑瀑布流)"
            });
            cmbRefreshMode.Location = new Point(265, 83);
            cmbRefreshMode.Size = new Size(520, 23);
            grpSys.Controls.Add(cmbRefreshMode);

            chkReportData = new CheckBox();
            chkReportData.Text = "开启原始 UDP 报文保存至 reportdata 目录 (reportdata.enabled)";
            chkReportData.Font = new Font("Microsoft YaHei", 9F, FontStyle.Regular);
            chkReportData.Location = new Point(15, 122);
            chkReportData.Size = new Size(650, 24);
            grpSys.Controls.Add(chkReportData);

            mainPanel.Controls.Add(grpSys);
            curY += 175;

            // 3. 分组: 持久峰值平滑滤波
            grpPeak = new GroupBox();
            grpPeak.Text = "2. 持久峰值平滑滤波与图谱联动";
            grpPeak.Font = new Font("Microsoft YaHei", 9F, FontStyle.Bold);
            grpPeak.Location = new Point(16, curY);
            grpPeak.Size = new Size(900, 215);
            grpPeak.Anchor = AnchorStyles.Top | AnchorStyles.Left | AnchorStyles.Right;

            chkPersistentPeak = new CheckBox();
            chkPersistentPeak.Text = "启用持久测试峰值平滑滤波引擎 (persistent.peak.enabled)";
            chkPersistentPeak.Font = new Font("Microsoft YaHei", 9F, FontStyle.Regular);
            chkPersistentPeak.Location = new Point(15, 24);
            chkPersistentPeak.Size = new Size(600, 22);
            chkPersistentPeak.Checked = DefaultPersistentPeak;
            chkPersistentPeak.CheckedChanged += delegate { UpdateControlsEnableState(); };
            grpPeak.Controls.Add(chkPersistentPeak);

            chkSpectrogramFollow = new CheckBox();
            chkSpectrogramFollow.Text = "谱图峰值实时跟随平滑滤波值 (spectrogram.peak.follow.filtered)";
            chkSpectrogramFollow.Font = new Font("Microsoft YaHei", 9F, FontStyle.Regular);
            chkSpectrogramFollow.Location = new Point(15, 50);
            chkSpectrogramFollow.Size = new Size(600, 22);
            chkSpectrogramFollow.Checked = DefaultSpectrogramFollow;
            grpPeak.Controls.Add(chkSpectrogramFollow);

            // 死区门限
            Label lblEpsilon = new Label();
            lblEpsilon.Text = "变动死区门限 (display.epsilon.db):";
            lblEpsilon.Font = new Font("Microsoft YaHei", 9F, FontStyle.Regular);
            lblEpsilon.Location = new Point(15, 80);
            lblEpsilon.Size = new Size(240, 20);
            grpPeak.Controls.Add(lblEpsilon);

            numEpsilon = new NumericUpDown();
            numEpsilon.DecimalPlaces = 2;
            numEpsilon.Increment = 0.1m;
            numEpsilon.Minimum = 0.0m;
            numEpsilon.Maximum = 50.0m;
            numEpsilon.Location = new Point(265, 78);
            numEpsilon.Size = new Size(90, 23);
            grpPeak.Controls.Add(numEpsilon);

            Label lblEpsilonUnit = new Label();
            lblEpsilonUnit.Text = "dB (默认 0.2 或 10)";
            lblEpsilonUnit.Font = new Font("Microsoft YaHei", 8.5F);
            lblEpsilonUnit.ForeColor = Color.Gray;
            lblEpsilonUnit.Location = new Point(365, 80);
            lblEpsilonUnit.Size = new Size(300, 20);
            grpPeak.Controls.Add(lblEpsilonUnit);

            // 微抖动
            Label lblJitter = new Label();
            lblJitter.Text = "稳定微抖动 (display.jitter.db):";
            lblJitter.Font = new Font("Microsoft YaHei", 9F, FontStyle.Regular);
            lblJitter.Location = new Point(15, 110);
            lblJitter.Size = new Size(240, 20);
            grpPeak.Controls.Add(lblJitter);

            numJitter = new NumericUpDown();
            numJitter.DecimalPlaces = 2;
            numJitter.Increment = 0.1m;
            numJitter.Minimum = 0.0m;
            numJitter.Maximum = 10.0m;
            numJitter.Location = new Point(265, 108);
            numJitter.Size = new Size(90, 23);
            grpPeak.Controls.Add(numJitter);

            Label lblJitterUnit = new Label();
            lblJitterUnit.Text = "dB (0 为关闭波动)";
            lblJitterUnit.Font = new Font("Microsoft YaHei", 8.5F);
            lblJitterUnit.ForeColor = Color.Gray;
            lblJitterUnit.Location = new Point(365, 110);
            lblJitterUnit.Size = new Size(300, 20);
            grpPeak.Controls.Add(lblJitterUnit);

            // 小数位数
            Label lblDecimals = new Label();
            lblDecimals.Text = "数值小数位数 (display.decimals):";
            lblDecimals.Font = new Font("Microsoft YaHei", 9F, FontStyle.Regular);
            lblDecimals.Location = new Point(15, 140);
            lblDecimals.Size = new Size(240, 20);
            grpPeak.Controls.Add(lblDecimals);

            cmbDecimals = new ComboBox();
            cmbDecimals.DropDownStyle = ComboBoxStyle.DropDownList;
            cmbDecimals.Items.AddRange(new object[] {
                "0 (纯整数格式，无小数)",
                "1 (保留 1 位小数，默认)",
                "2 (保留 2 位小数)",
                "3 (保留 3 位小数)",
                "4 (保留 4 位小数)",
                "5 (保留 5 位小数)",
                "6 (保留 6 位小数)"
            });
            cmbDecimals.Location = new Point(265, 137);
            cmbDecimals.Size = new Size(200, 23);
            grpPeak.Controls.Add(cmbDecimals);

            // 幅值步进上限（独立一行，完整显示不遮挡）
            Label lblMaxStep = new Label();
            lblMaxStep.Text = "变动步进上限 (display.max.step.db):";
            lblMaxStep.Font = new Font("Microsoft YaHei", 9F, FontStyle.Regular);
            lblMaxStep.Location = new Point(15, 172);
            lblMaxStep.Size = new Size(240, 20);
            grpPeak.Controls.Add(lblMaxStep);

            numMaxStep = new NumericUpDown();
            numMaxStep.DecimalPlaces = 2;
            numMaxStep.Increment = 0.1m;
            numMaxStep.Minimum = 0.1m;
            numMaxStep.Maximum = 50.0m;
            numMaxStep.Location = new Point(265, 170);
            numMaxStep.Size = new Size(90, 23);
            grpPeak.Controls.Add(numMaxStep);

            Label lblMaxStepUnit = new Label();
            lblMaxStepUnit.Text = "dB (每次变动的最大步长，默认 0.3)";
            lblMaxStepUnit.Font = new Font("Microsoft YaHei", 8.5F);
            lblMaxStepUnit.ForeColor = Color.Gray;
            lblMaxStepUnit.Location = new Point(365, 172);
            lblMaxStepUnit.Size = new Size(350, 20);
            grpPeak.Controls.Add(lblMaxStepUnit);

            mainPanel.Controls.Add(grpPeak);
            curY += 225;

            // 4. 分组: 初始建立与切换保护
            grpSwitch = new GroupBox();
            grpSwitch.Text = "3. 初始建立与信号切换保护";
            grpSwitch.Font = new Font("Microsoft YaHei", 9F, FontStyle.Bold);
            grpSwitch.Location = new Point(16, curY);
            grpSwitch.Size = new Size(900, 125);
            grpSwitch.Anchor = AnchorStyles.Top | AnchorStyles.Left | AnchorStyles.Right;

            Label lblInit = new Label();
            lblInit.Text = "初始建立组数 (initial.groups):";
            lblInit.Font = new Font("Microsoft YaHei", 9F, FontStyle.Regular);
            lblInit.Location = new Point(15, 28);
            lblInit.Size = new Size(240, 20);
            grpSwitch.Controls.Add(lblInit);

            numInitialGroups = new NumericUpDown();
            numInitialGroups.Minimum = 1;
            numInitialGroups.Maximum = 100;
            numInitialGroups.Location = new Point(265, 25);
            numInitialGroups.Size = new Size(90, 23);
            grpSwitch.Controls.Add(numInitialGroups);

            Label lblInitTip = new Label();
            lblInitTip.Text = "组 (默认3组/约4.8秒输出初始最大值)";
            lblInitTip.Font = new Font("Microsoft YaHei", 8.5F);
            lblInitTip.ForeColor = Color.Gray;
            lblInitTip.Location = new Point(365, 28);
            lblInitTip.Size = new Size(350, 20);
            grpSwitch.Controls.Add(lblInitTip);

            Label lblJudge = new Label();
            lblJudge.Text = "切换判断组数 (switch.judge.groups):";
            lblJudge.Font = new Font("Microsoft YaHei", 9F, FontStyle.Regular);
            lblJudge.Location = new Point(15, 58);
            lblJudge.Size = new Size(240, 20);
            grpSwitch.Controls.Add(lblJudge);

            numSwitchJudgeGroups = new NumericUpDown();
            numSwitchJudgeGroups.Minimum = 1;
            numSwitchJudgeGroups.Maximum = 100;
            numSwitchJudgeGroups.Location = new Point(265, 55);
            numSwitchJudgeGroups.Size = new Size(90, 23);
            grpSwitch.Controls.Add(numSwitchJudgeGroups);

            Label lblJudgeTip = new Label();
            lblJudgeTip.Text = "组 (大幅跳变后稳定判定组数，默认3组)";
            lblJudgeTip.Font = new Font("Microsoft YaHei", 8.5F);
            lblJudgeTip.ForeColor = Color.Gray;
            lblJudgeTip.Location = new Point(365, 58);
            lblJudgeTip.Size = new Size(350, 20);
            grpSwitch.Controls.Add(lblJudgeTip);

            chkSwitchShowDash = new CheckBox();
            chkSwitchShowDash.Text = "中途切换信号强度时先显示 --，待稳定后再显示新数据 (switch.show.dash)";
            chkSwitchShowDash.Font = new Font("Microsoft YaHei", 9F, FontStyle.Regular);
            chkSwitchShowDash.Location = new Point(15, 88);
            chkSwitchShowDash.Size = new Size(750, 22);
            grpSwitch.Controls.Add(chkSwitchShowDash);

            mainPanel.Controls.Add(grpSwitch);
            curY += 135;

            // 5. 分组: IP 切换模式与预设地址
            grpIpSwitch = new GroupBox();
            grpIpSwitch.Text = "4. IP 切换模式与预设地址配置";
            grpIpSwitch.Font = new Font("Microsoft YaHei", 9F, FontStyle.Bold);
            grpIpSwitch.Location = new Point(16, curY);
            grpIpSwitch.Size = new Size(900, 160);
            grpIpSwitch.Anchor = AnchorStyles.Top | AnchorStyles.Left | AnchorStyles.Right;

            Label lblIpSwitchMode = new Label();
            lblIpSwitchMode.Text = "IP 切换方式 (ip.switch.mode):";
            lblIpSwitchMode.Font = new Font("Microsoft YaHei", 9F, FontStyle.Regular);
            lblIpSwitchMode.Location = new Point(15, 26);
            lblIpSwitchMode.Size = new Size(240, 20);
            grpIpSwitch.Controls.Add(lblIpSwitchMode);

            cmbIpSwitchMode = new ComboBox();
            cmbIpSwitchMode.DropDownStyle = ComboBoxStyle.DropDownList;
            cmbIpSwitchMode.Font = new Font("Microsoft YaHei", 9F, FontStyle.Regular);
            cmbIpSwitchMode.Items.AddRange(new object[] {
                "0: 仅直接输入 (传统文本框手动输入)",
                "1: 仅 3 个预设按钮 (点击快捷按钮切换 IP)",
                "2: 两种方式并行并存 (输入框 + 3 个预设按钮 · 默认)"
            });
            cmbIpSwitchMode.Location = new Point(265, 23);
            cmbIpSwitchMode.Size = new Size(520, 23);
            grpIpSwitch.Controls.Add(cmbIpSwitchMode);

            Label lblPname1 = new Label();
            lblPname1.Text = "预设 1 文本 (preset.name.1):";
            lblPname1.Font = new Font("Microsoft YaHei", 9F, FontStyle.Regular);
            lblPname1.Location = new Point(15, 56);
            lblPname1.Size = new Size(220, 20);
            grpIpSwitch.Controls.Add(lblPname1);

            txtPresetName1 = new TextBox();
            txtPresetName1.Font = new Font("Microsoft YaHei", 9F, FontStyle.Regular);
            txtPresetName1.Location = new Point(240, 53);
            txtPresetName1.Size = new Size(160, 23);
            grpIpSwitch.Controls.Add(txtPresetName1);

            Label lblPip1 = new Label();
            lblPip1.Text = "预设 IP 1 (preset.ip.1):";
            lblPip1.Font = new Font("Microsoft YaHei", 9F, FontStyle.Regular);
            lblPip1.Location = new Point(430, 56);
            lblPip1.Size = new Size(180, 20);
            grpIpSwitch.Controls.Add(lblPip1);

            txtPresetIp1 = new TextBox();
            txtPresetIp1.Font = new Font("Microsoft YaHei", 9F, FontStyle.Regular);
            txtPresetIp1.Location = new Point(615, 53);
            txtPresetIp1.Size = new Size(240, 23);
            grpIpSwitch.Controls.Add(txtPresetIp1);

            Label lblPname2 = new Label();
            lblPname2.Text = "预设 2 文本 (preset.name.2):";
            lblPname2.Font = new Font("Microsoft YaHei", 9F, FontStyle.Regular);
            lblPname2.Location = new Point(15, 86);
            lblPname2.Size = new Size(220, 20);
            grpIpSwitch.Controls.Add(lblPname2);

            txtPresetName2 = new TextBox();
            txtPresetName2.Font = new Font("Microsoft YaHei", 9F, FontStyle.Regular);
            txtPresetName2.Location = new Point(240, 83);
            txtPresetName2.Size = new Size(160, 23);
            grpIpSwitch.Controls.Add(txtPresetName2);

            Label lblPip2 = new Label();
            lblPip2.Text = "预设 IP 2 (preset.ip.2):";
            lblPip2.Font = new Font("Microsoft YaHei", 9F, FontStyle.Regular);
            lblPip2.Location = new Point(430, 86);
            lblPip2.Size = new Size(180, 20);
            grpIpSwitch.Controls.Add(lblPip2);

            txtPresetIp2 = new TextBox();
            txtPresetIp2.Font = new Font("Microsoft YaHei", 9F, FontStyle.Regular);
            txtPresetIp2.Location = new Point(615, 83);
            txtPresetIp2.Size = new Size(240, 23);
            grpIpSwitch.Controls.Add(txtPresetIp2);

            Label lblPname3 = new Label();
            lblPname3.Text = "预设 3 文本 (preset.name.3):";
            lblPname3.Font = new Font("Microsoft YaHei", 9F, FontStyle.Regular);
            lblPname3.Location = new Point(15, 116);
            lblPname3.Size = new Size(220, 20);
            grpIpSwitch.Controls.Add(lblPname3);

            txtPresetName3 = new TextBox();
            txtPresetName3.Font = new Font("Microsoft YaHei", 9F, FontStyle.Regular);
            txtPresetName3.Location = new Point(240, 113);
            txtPresetName3.Size = new Size(160, 23);
            grpIpSwitch.Controls.Add(txtPresetName3);

            Label lblPip3 = new Label();
            lblPip3.Text = "预设 IP 3 (preset.ip.3):";
            lblPip3.Font = new Font("Microsoft YaHei", 9F, FontStyle.Regular);
            lblPip3.Location = new Point(430, 116);
            lblPip3.Size = new Size(180, 20);
            grpIpSwitch.Controls.Add(lblPip3);

            txtPresetIp3 = new TextBox();
            txtPresetIp3.Font = new Font("Microsoft YaHei", 9F, FontStyle.Regular);
            txtPresetIp3.Location = new Point(615, 113);
            txtPresetIp3.Size = new Size(240, 23);
            grpIpSwitch.Controls.Add(txtPresetIp3);

            mainPanel.Controls.Add(grpIpSwitch);
            curY += 170;

            // 6. 分组: 幅值报警监控配置
            grpAlarm = new GroupBox();
            grpAlarm.Text = "5. 幅值报警监控配置";
            grpAlarm.Font = new Font("Microsoft YaHei", 9F, FontStyle.Bold);
            grpAlarm.Location = new Point(16, curY);
            grpAlarm.Size = new Size(900, 68);
            grpAlarm.Anchor = AnchorStyles.Top | AnchorStyles.Left | AnchorStyles.Right;

            chkAlarmEnabled = new CheckBox();
            chkAlarmEnabled.Text = "启用幅值超限报警监控与专用查看页面 (alarm.enabled)";
            chkAlarmEnabled.Font = new Font("Microsoft YaHei", 9F, FontStyle.Regular);
            chkAlarmEnabled.Location = new Point(15, 24);
            chkAlarmEnabled.Size = new Size(750, 24);
            grpAlarm.Controls.Add(chkAlarmEnabled);

            mainPanel.Controls.Add(grpAlarm);
            curY += 78;

            // 底部添加边距垫片，确保在滚动条出现或拉到底部时留有足够呼吸空间
            Panel spacer = new Panel();
            spacer.Location = new Point(16, curY);
            spacer.Size = new Size(900, 24);
            mainPanel.Controls.Add(spacer);

            // 7. 底部控制栏
            Panel bottomPanel = new Panel();
            bottomPanel.Dock = DockStyle.Bottom;
            bottomPanel.Height = 55;
            bottomPanel.BackColor = Color.FromArgb(241, 245, 249);
            bottomPanel.Padding = new Padding(16, 10, 16, 10);
            this.Controls.Add(bottomPanel);
            bottomPanel.SendToBack();

            lblStatus = new Label();
            lblStatus.Text = "就绪";
            lblStatus.Font = new Font("Microsoft YaHei", 9F);
            lblStatus.ForeColor = Color.FromArgb(71, 85, 105);
            lblStatus.Location = new Point(16, 16);
            lblStatus.Size = new Size(450, 22);
            bottomPanel.Controls.Add(lblStatus);

            btnSave = new Button();
            btnSave.Text = "保存";
            btnSave.Font = new Font("Microsoft YaHei", 9F, FontStyle.Bold);
            btnSave.BackColor = Color.FromArgb(37, 99, 235);
            btnSave.ForeColor = Color.White;
            btnSave.FlatStyle = FlatStyle.Flat;
            btnSave.Location = new Point(500, 11);
            btnSave.Size = new Size(115, 33);
            btnSave.Anchor = AnchorStyles.Top | AnchorStyles.Right;
            btnSave.Click += (s, e) => SaveConfig();
            bottomPanel.Controls.Add(btnSave);

            btnReset = new Button();
            btnReset.Text = "恢复默认值";
            btnReset.Font = new Font("Microsoft YaHei", 9F);
            btnReset.Location = new Point(625, 11);
            btnReset.Size = new Size(115, 33);
            btnReset.Anchor = AnchorStyles.Top | AnchorStyles.Right;
            btnReset.Click += (s, e) => ResetDefaults();
            bottomPanel.Controls.Add(btnReset);

            btnReload = new Button();
            btnReload.Text = "重新加载";
            btnReload.Font = new Font("Microsoft YaHei", 9F);
            btnReload.Location = new Point(750, 11);
            btnReload.Size = new Size(100, 33);
            btnReload.Anchor = AnchorStyles.Top | AnchorStyles.Right;
            btnReload.Click += (s, e) => LoadConfigFromFile();
            bottomPanel.Controls.Add(btnReload);

            SetRollingModeSelected(DefaultSpectrogramRolling);
            UpdateControlsEnableState();
        }

        private void UpdateControlsEnableState()
        {
            bool isPeakEnabled = _isLicensePresent && (chkPersistentPeak != null && chkPersistentPeak.Checked);

            if (chkPersistentPeak != null) chkPersistentPeak.Enabled = _isLicensePresent;
            if (chkSpectrogramFollow != null) chkSpectrogramFollow.Enabled = isPeakEnabled;
            if (numEpsilon != null) numEpsilon.Enabled = isPeakEnabled;
            if (numJitter != null) numJitter.Enabled = isPeakEnabled;
            if (cmbDecimals != null) cmbDecimals.Enabled = isPeakEnabled;
            if (numMaxStep != null) numMaxStep.Enabled = isPeakEnabled;

            if (grpSwitch != null) grpSwitch.Enabled = isPeakEnabled;
            if (numInitialGroups != null) numInitialGroups.Enabled = isPeakEnabled;
            if (numSwitchJudgeGroups != null) numSwitchJudgeGroups.Enabled = isPeakEnabled;
            if (chkSwitchShowDash != null) chkSwitchShowDash.Enabled = isPeakEnabled;

            if (cmbRefreshMode != null) cmbRefreshMode.Enabled = _isLicensePresent;
            if (chkAlarmEnabled != null) chkAlarmEnabled.Enabled = _isLicensePresent;
            if (grpIpSwitch != null) grpIpSwitch.Enabled = _isLicensePresent;
        }

        private void MainForm_Load(object sender, EventArgs e)
        {
            LocateConfigFile();
        }

        /// <summary>
        /// 核心定位逻辑:
        /// 1. 检查当前运行根目录下是否存在 /lib/.config
        /// 2. 若存在，直接加载
        /// 3. 若不存在，弹出提示要求用户选择谱图软件所在位置（选择谱图软件.exe）
        /// </summary>
        private void LocateConfigFile()
        {
            string currentBase = AppDomain.CurrentDomain.BaseDirectory;
            string candidateLibConfig = Path.Combine(currentBase, "lib", ".config");

            // 检查当前根目录/lib/.config
            if (File.Exists(candidateLibConfig))
            {
                _appDirectory = currentBase;
                _targetConfigFile = candidateLibConfig;
                UpdatePathDisplay();
                LoadConfigFromFile();
                return;
            }

            // 检查上一级目录的 lib/.config (适配工具放在 tools/ 目录下的情况)
            DirectoryInfo parentInfo = Directory.GetParent(currentBase);
            string parentBase = parentInfo != null ? parentInfo.FullName : null;
            if (!string.IsNullOrEmpty(parentBase))
            {
                string parentCandidate = Path.Combine(parentBase, "lib", ".config");
                if (File.Exists(parentCandidate))
                {
                    _appDirectory = parentBase;
                    _targetConfigFile = parentCandidate;
                    UpdatePathDisplay();
                    LoadConfigFromFile();
                    return;
                }
            }

            // 没有检测到，要求用户选择谱图软件所在位置 (选择谱图软件.exe)
            PromptSelectExe(false);
        }

        private void PromptSelectExe(bool isManualSwitch)
        {
            if (!isManualSwitch)
            {
                DialogResult tipRes = MessageBox.Show(
                    "在当前运行目录下未检测到配置文件。\n\n请点击【确定】选择谱图软件主程序（请选择谱图软件 .exe）。",
                    "请选择谱图软件所在位置",
                    MessageBoxButtons.OKCancel,
                    MessageBoxIcon.Information
                );
                if (tipRes != DialogResult.OK)
                {
                    lblStatus.Text = "未指定谱图软件目录";
                    return;
                }
            }

            using (OpenFileDialog dlg = new OpenFileDialog())
            {
                dlg.Title = "请选择 Graph_8Pro 主程序 (例如: Graph_8Pro.exe / 谱图软件.exe)";
                dlg.Filter = "Graph_8Pro 主程序 (*.exe)|Graph_8Pro.exe;谱图软件.exe;GraphApp8Pro.exe;*.exe|所有文件 (*.*)|*.*";
                dlg.CheckFileExists = true;
                if (!string.IsNullOrEmpty(_appDirectory) && Directory.Exists(_appDirectory))
                {
                    dlg.InitialDirectory = _appDirectory;
                }
                else
                {
                    dlg.InitialDirectory = AppDomain.CurrentDomain.BaseDirectory;
                }

                if (dlg.ShowDialog(this) == DialogResult.OK)
                {
                    string selectedExe = dlg.FileName;
                    string exeDir = Path.GetDirectoryName(selectedExe);
                    string targetLib = Path.Combine(exeDir, "lib");
                    if (!Directory.Exists(targetLib))
                    {
                        Directory.CreateDirectory(targetLib);
                    }
                    string cfgPath = Path.Combine(targetLib, ".config");

                    _appDirectory = exeDir;
                    _targetConfigFile = cfgPath;
                    UpdatePathDisplay();

                    if (File.Exists(cfgPath))
                    {
                        LoadConfigFromFile();
                    }
                    else
                    {
                        // 目标目录下尚无配置文件，引导创建
                        DialogResult crtRes = MessageBox.Show(
                            "所选谱图软件目录下尚未生成配置文件。\n\n是否加载出厂默认参数，并在点击【保存】后写入？",
                            "未检测到配置文件",
                            MessageBoxButtons.YesNo,
                            MessageBoxIcon.Question
                        );
                        if (crtRes == DialogResult.Yes)
                        {
                            ResetDefaults();
                            lblStatus.Text = "已载入默认模板，点击【保存】以创建";
                        }
                    }
                }
            }
        }

        private void UpdatePathDisplay()
        {
            lblAppDir.Text = string.IsNullOrEmpty(_appDirectory) ? "未指定" : _appDirectory;
            UpdateLicenseStatus();
        }

        private void UpdateLicenseStatus()
        {
            if (lblLicenseStatus == null) return;
            string licDir = !string.IsNullOrEmpty(_appDirectory)
                ? Path.Combine(_appDirectory, "lib")
                : Path.Combine(AppDomain.CurrentDomain.BaseDirectory, "lib");
            string licPath = Path.Combine(licDir, "license.prop");
            string licSpecialPath = Path.Combine(licDir, "license_special.prop");

            string activeLic = null;
            if (File.Exists(licPath)) activeLic = licPath;
            else if (File.Exists(licSpecialPath)) activeLic = licSpecialPath;

            if (activeLic != null)
            {
                _isLicensePresent = true;
                bool isSpecial = CheckIfSpecialLicense(activeLic);
                if (isSpecial)
                {
                    lblLicenseStatus.Text = "已加载: 特殊授权文件 (仅供调试使用)";
                    lblLicenseStatus.ForeColor = Color.FromArgb(217, 119, 6);
                }
                else
                {
                    lblLicenseStatus.Text = "已加载 (" + Path.GetFileName(activeLic) + ")";
                    lblLicenseStatus.ForeColor = Color.FromArgb(22, 101, 52);
                }
            }
            else
            {
                _isLicensePresent = false;
                lblLicenseStatus.Text = "未检测到授权文件 (高级功能已锁定)";
                lblLicenseStatus.ForeColor = Color.FromArgb(185, 28, 28);
            }
            UpdateControlsEnableState();
        }

        private bool CheckIfSpecialLicense(string filePath)
        {
            try
            {
                string[] lines = File.ReadAllLines(filePath);
                foreach (string line in lines)
                {
                    string trimmed = line.Trim();
                    if (string.IsNullOrEmpty(trimmed) || trimmed.StartsWith("#") || trimmed.StartsWith(";")) continue;
                    int idx = trimmed.IndexOf('=');
                    if (idx > 0)
                    {
                        string k = trimmed.Substring(0, idx).Trim().ToLowerInvariant();
                        string v = trimmed.Substring(idx + 1).Trim();
                        if ((k == "special" && (v.Equals("true", StringComparison.OrdinalIgnoreCase) || v == "1"))
                            || (k == "debug" && v.Equals("true", StringComparison.OrdinalIgnoreCase))
                            || (k == "type" && (v.Equals("special", StringComparison.OrdinalIgnoreCase) || v.Equals("debug", StringComparison.OrdinalIgnoreCase)))
                            || (k == "mode" && (v.Equals("special", StringComparison.OrdinalIgnoreCase) || v.Equals("debug", StringComparison.OrdinalIgnoreCase)))
                            || (k == "mac" && (v.Equals("SPECIAL", StringComparison.OrdinalIgnoreCase) || v.Equals("DEBUG", StringComparison.OrdinalIgnoreCase) || v == "*" || v == "00:00:00:00:00:00" || v == "FF:FF:FF:FF:FF:FF"))
                            || (k == "key" && (v.Equals("SPECIAL", StringComparison.OrdinalIgnoreCase) || v.Equals("DEBUG", StringComparison.OrdinalIgnoreCase))))
                        {
                            return true;
                        }
                    }
                }
            }
            catch { }
            return false;
        }

        private void ImportLicenseFile()
        {
            if (string.IsNullOrEmpty(_appDirectory))
            {
                MessageBox.Show("请先选择或定位主程序软件目录！", "提示", MessageBoxButtons.OK, MessageBoxIcon.Warning);
                return;
            }

            using (OpenFileDialog ofd = new OpenFileDialog())
            {
                ofd.Title = "选择授权文件 (.prop 或 .properties)";
                ofd.Filter = "授权文件 (*.prop;*.properties)|*.prop;*.properties|所有文件 (*.*)|*.*";
                if (ofd.ShowDialog(this) == DialogResult.OK)
                {
                    try
                    {
                        string targetDir = Path.Combine(_appDirectory, "lib");
                        if (!Directory.Exists(targetDir))
                        {
                            Directory.CreateDirectory(targetDir);
                        }
                        string targetLic = Path.Combine(targetDir, "license.prop");
                        File.Copy(ofd.FileName, targetLic, true);
                        UpdateLicenseStatus();
                        MessageBox.Show("授权文件导入成功！\n文件已保存至: " + targetLic, "导入成功", MessageBoxButtons.OK, MessageBoxIcon.Information);
                    }
                    catch (Exception ex)
                    {
                        MessageBox.Show("导入授权文件失败: " + ex.Message, "错误", MessageBoxButtons.OK, MessageBoxIcon.Error);
                    }
                }
            }
        }

        private void LoadConfigFromFile()
        {
            if (string.IsNullOrEmpty(_targetConfigFile) || !File.Exists(_targetConfigFile))
            {
                lblStatus.Text = "配置文件不存在";
                return;
            }

            try
            {
                string raw = File.ReadAllText(_targetConfigFile, Encoding.UTF8);
                string plain = DecryptString(raw);
                Dictionary<string, string> dict = ParseProperties(plain);

                // 赋值到界面
                txtVersion.Text = dict.ContainsKey("app.version") ? dict["app.version"] : DefaultVersion;
                txtDeviceIp.Text = dict.ContainsKey("default.ip") ? dict["default.ip"] : (dict.ContainsKey("device.ip") ? dict["device.ip"] : DefaultDeviceIp);
                bool rolling = ParseBool(dict, "spectrogram.rolling.enabled", DefaultSpectrogramRolling);
                SetRollingModeSelected(rolling);
                chkReportData.Checked = ParseBool(dict, "reportdata.enabled", DefaultReportData);

                chkPersistentPeak.Checked = ParseBool(dict, "persistent.peak.enabled", DefaultPersistentPeak);
                chkSpectrogramFollow.Checked = ParseBool(dict, "spectrogram.peak.follow.filtered", DefaultSpectrogramFollow);
                numEpsilon.Value = ParseDecimal(dict, "display.epsilon.db", DefaultEpsilonDb, 0.0m, 50.0m);
                numJitter.Value = ParseDecimal(dict, "display.jitter.db", DefaultJitterDb, 0.0m, 10.0m);

                int dec = ParseInt(dict, "display.decimals", DefaultDecimals, 0, 6);
                cmbDecimals.SelectedIndex = Math.Min(Math.Max(0, dec), cmbDecimals.Items.Count - 1);
                numMaxStep.Value = ParseDecimal(dict, "display.max.step.db", DefaultMaxStepDb, 0.1m, 50.0m);

                numInitialGroups.Value = ParseInt(dict, "initial.groups", DefaultInitialGroups, 1, 100);
                numSwitchJudgeGroups.Value = ParseInt(dict, "switch.judge.groups", DefaultSwitchJudgeGroups, 1, 100);
                chkSwitchShowDash.Checked = ParseBool(dict, "switch.show.dash", DefaultSwitchShowDash);

                int mode = ParseInt(dict, "ip.switch.mode", DefaultIpSwitchMode, 0, 2);
                cmbIpSwitchMode.SelectedIndex = Math.Min(Math.Max(0, mode), cmbIpSwitchMode.Items.Count - 1);
                txtPresetName1.Text = dict.ContainsKey("preset.name.1") ? dict["preset.name.1"] : DefaultPresetName1;
                txtPresetIp1.Text = dict.ContainsKey("preset.ip.1") ? dict["preset.ip.1"] : DefaultPresetIp1;
                txtPresetName2.Text = dict.ContainsKey("preset.name.2") ? dict["preset.name.2"] : DefaultPresetName2;
                txtPresetIp2.Text = dict.ContainsKey("preset.ip.2") ? dict["preset.ip.2"] : DefaultPresetIp2;
                txtPresetName3.Text = dict.ContainsKey("preset.name.3") ? dict["preset.name.3"] : DefaultPresetName3;
                txtPresetIp3.Text = dict.ContainsKey("preset.ip.3") ? dict["preset.ip.3"] : DefaultPresetIp3;

                chkAlarmEnabled.Checked = ParseBool(dict, "alarm.enabled", DefaultAlarmEnabled);

                // 缓存非直接编辑字段（采集速度、通道报警阈值）以防保存时覆盖丢失
                _cachedAcqSpeedDelayMs = ParseInt(dict, "acquisition.speed.delay.ms", 0, 0, 10000);
                _cachedAcqSpeedMode = dict.ContainsKey("acquisition.speed.mode") ? dict["acquisition.speed.mode"] : "FULL_50HZ";
                _cachedAlarmThresholdGlobal = ParseDecimal(dict, "alarm.threshold.global", DefaultAlarmThresholdGlobal, -200m, 200m);
                for (int i = 0; i < 8; i++)
                {
                    string k = "alarm.threshold.ch" + (i + 1);
                    _cachedAlarmThresholdChannels[i] = ParseDecimal(dict, k, _cachedAlarmThresholdGlobal, -200m, 200m);
                }

                lblStatus.Text = "配置文件读取成功 (AES 解密生效)";
                UpdateControlsEnableState();
            }
            catch (Exception ex)
            {
                MessageBox.Show("读取并解密配置文件失败:\n" + ex.Message, "错误", MessageBoxButtons.OK, MessageBoxIcon.Error);
                lblStatus.Text = "加载失败: " + ex.Message;
            }
        }

        private void SaveConfig()
        {
            if (string.IsNullOrEmpty(_targetConfigFile))
            {
                MessageBox.Show("未指定目标配置文件，请先选择谱图软件主程序！", "提示", MessageBoxButtons.OK, MessageBoxIcon.Warning);
                PromptSelectExe(false);
                if (string.IsNullOrEmpty(_targetConfigFile)) return;
            }

            try
            {
                string ver = txtVersion.Text.Trim();
                if (string.IsNullOrEmpty(ver)) ver = DefaultVersion;

                string ip = txtDeviceIp.Text.Trim();
                if (string.IsNullOrEmpty(ip)) ip = DefaultDeviceIp;

                string pName1 = txtPresetName1.Text.Trim();
                if (string.IsNullOrEmpty(pName1)) pName1 = DefaultPresetName1;
                string pIp1 = txtPresetIp1.Text.Trim();
                if (string.IsNullOrEmpty(pIp1)) pIp1 = DefaultPresetIp1;

                string pName2 = txtPresetName2.Text.Trim();
                if (string.IsNullOrEmpty(pName2)) pName2 = DefaultPresetName2;
                string pIp2 = txtPresetIp2.Text.Trim();
                if (string.IsNullOrEmpty(pIp2)) pIp2 = DefaultPresetIp2;

                string pName3 = txtPresetName3.Text.Trim();
                if (string.IsNullOrEmpty(pName3)) pName3 = DefaultPresetName3;
                string pIp3 = txtPresetIp3.Text.Trim();
                if (string.IsNullOrEmpty(pIp3)) pIp3 = DefaultPresetIp3;

                StringBuilder sb = new StringBuilder();
                sb.AppendLine("# ===================================================================");
                sb.AppendLine("# Graph_8Pro 系统全局功能配置 (.config)");
                sb.AppendLine("# ===================================================================");
                sb.AppendLine();
                sb.AppendLine("# 1. 是否开启原始UDP报文写入reportdata目录 (true/false, 默认关闭)");
                sb.AppendLine("reportdata.enabled=" + (chkReportData.Checked ? "true" : "false"));
                sb.AppendLine();
                sb.AppendLine("# 2. 是否开启持久峰值平滑滤波功能 (true/false, 默认关闭)");
                sb.AppendLine("persistent.peak.enabled=" + (chkPersistentPeak.Checked ? "true" : "false"));
                sb.AppendLine();
                sb.AppendLine("# 3. 持久峰值界面数值更新最小波动门限/死区 (单位: dB, 默认 0.2)");
                sb.AppendLine("display.epsilon.db=" + numEpsilon.Value.ToString("F2"));
                sb.AppendLine();
                sb.AppendLine("# 4. 持久峰值最大值微波动/微抖动范围 (单位: dB, 默认 0 表示不开启, 设置为如 2 则在稳定值基础上计算 ±(0-2) 随机波动)");
                sb.AppendLine("display.jitter.db=" + numJitter.Value.ToString("F2"));
                sb.AppendLine();
                sb.AppendLine("# 5. 谱图峰值是否跟随平滑滤波峰值 (true/false, 默认开启)");
                sb.AppendLine("spectrogram.peak.follow.filtered=" + (chkSpectrogramFollow.Checked ? "true" : "false"));
                sb.AppendLine();
                sb.AppendLine("# 6. 持久性测试数值显示与记录的小数点后位数 (0-6, 默认 1; 0 表示无小数点后内容/整数显示)");
                sb.AppendLine("display.decimals=" + cmbDecimals.SelectedIndex);
                sb.AppendLine();
                sb.AppendLine("# 7. 软件版本号显示 (若配置则覆盖默认版本号)");
                sb.AppendLine("app.version=" + ver);
                sb.AppendLine();
                sb.AppendLine("# 8. 持久峰值每次变动幅值上限/步进上限 (单位: dB, 默认 0.3; 若设置为如 10，则在输入大幅突变且均值稳定后以 10dBm 为单位快速变动)");
                sb.AppendLine("display.max.step.db=" + numMaxStep.Value.ToString("F2"));
                sb.AppendLine();
                sb.AppendLine("# 9. 持久峰值初始建立所需的组数 (默认 3，即第 3 组/约 4.8 秒后输出初始最大值)");
                sb.AppendLine("initial.groups=" + (int)numInitialGroups.Value);
                sb.AppendLine();
                sb.AppendLine("# 10. 中途切换输入信号强度的判断组数 (默认 3，即检测到信号大幅切换后，需连续采集几组来判定并稳定新信号强度)");
                sb.AppendLine("switch.judge.groups=" + (int)numSwitchJudgeGroups.Value);
                sb.AppendLine();
                sb.AppendLine("# 11. 中途切换信号强度时是否先将最大值显示为--，待稳定后再显示数据 (true/false, 默认 false)");
                sb.AppendLine("switch.show.dash=" + (chkSwitchShowDash.Checked ? "true" : "false"));
                sb.AppendLine();
                sb.AppendLine("# 12. 默认设备 IP 地址 (默认 192.168.10.60)");
                sb.AppendLine("default.ip=" + ip);
                sb.AppendLine();
                sb.AppendLine("# 13. 谱图动态刷新机制 (true: 逐周波平滑滚动(瀑布流), false: 整组80周波跳变(传统模式), 默认 false)");
                sb.AppendLine("spectrogram.rolling.enabled=" + (IsRollingModeSelected() ? "true" : "false"));
                sb.AppendLine();
                sb.AppendLine("# 14. 采集速度控制延时毫秒数 (0 为 50Hz 全速, 20 为 25Hz 等) 与模式");
                sb.AppendLine("acquisition.speed.delay.ms=" + _cachedAcqSpeedDelayMs);
                sb.AppendLine("acquisition.speed.mode=" + _cachedAcqSpeedMode);
                sb.AppendLine();
                sb.AppendLine("# 15. IP 切换方式 (0: 直接输入, 1: 3个预设按钮, 2: 并行并存模式) 与 3 个预设 IP 及按钮文本");
                sb.AppendLine("ip.switch.mode=" + cmbIpSwitchMode.SelectedIndex);
                sb.AppendLine("preset.ip.1=" + pIp1);
                sb.AppendLine("preset.name.1=" + pName1);
                sb.AppendLine("preset.ip.2=" + pIp2);
                sb.AppendLine("preset.name.2=" + pName2);
                sb.AppendLine("preset.ip.3=" + pIp3);
                sb.AppendLine("preset.name.3=" + pName3);
                sb.AppendLine();
                sb.AppendLine("# 16. 幅值报警开关与阈值 (dBm)");
                sb.AppendLine("alarm.enabled=" + (chkAlarmEnabled.Checked ? "true" : "false"));
                sb.AppendLine("alarm.threshold.global=" + Math.Round(_cachedAlarmThresholdGlobal).ToString("F0"));
                for (int i = 0; i < 8; i++)
                {
                    sb.AppendLine(string.Format("alarm.threshold.ch{0}={1}", i + 1, Math.Round(_cachedAlarmThresholdChannels[i]).ToString("F0")));
                }

                string plainText = sb.ToString();
                string encrypted = EncryptString(plainText);

                string dir = Path.GetDirectoryName(_targetConfigFile);
                if (!Directory.Exists(dir))
                {
                    Directory.CreateDirectory(dir);
                }

                // 若已存在，先清除隐藏属性避免 Windows 报拒绝访问
                if (File.Exists(_targetConfigFile))
                {
                    File.SetAttributes(_targetConfigFile, File.GetAttributes(_targetConfigFile) & ~FileAttributes.Hidden);
                }

                File.WriteAllText(_targetConfigFile, encrypted, Encoding.UTF8);

                // 设置隐藏文件属性
                File.SetAttributes(_targetConfigFile, File.GetAttributes(_targetConfigFile) | FileAttributes.Hidden);

                lblStatus.Text = "参数配置已成功保存";
                MessageBox.Show(
                    "参数配置已成功保存！",
                    "保存成功",
                    MessageBoxButtons.OK,
                    MessageBoxIcon.Information
                );
            }
            catch (Exception ex)
            {
                MessageBox.Show("保存配置失败:\n" + ex.Message, "错误", MessageBoxButtons.OK, MessageBoxIcon.Error);
                lblStatus.Text = "保存失败: " + ex.Message;
            }
        }

        private void ResetDefaults()
        {
            txtVersion.Text = DefaultVersion;
            txtDeviceIp.Text = DefaultDeviceIp;
            SetRollingModeSelected(DefaultSpectrogramRolling);
            chkReportData.Checked = DefaultReportData;
            chkPersistentPeak.Checked = DefaultPersistentPeak;
            chkSpectrogramFollow.Checked = DefaultSpectrogramFollow;
            numEpsilon.Value = DefaultEpsilonDb;
            numJitter.Value = DefaultJitterDb;
            cmbDecimals.SelectedIndex = DefaultDecimals;
            numMaxStep.Value = DefaultMaxStepDb;
            numInitialGroups.Value = DefaultInitialGroups;
            numSwitchJudgeGroups.Value = DefaultSwitchJudgeGroups;
            chkSwitchShowDash.Checked = DefaultSwitchShowDash;
            cmbIpSwitchMode.SelectedIndex = DefaultIpSwitchMode;
            txtPresetName1.Text = DefaultPresetName1;
            txtPresetIp1.Text = DefaultPresetIp1;
            txtPresetName2.Text = DefaultPresetName2;
            txtPresetIp2.Text = DefaultPresetIp2;
            txtPresetName3.Text = DefaultPresetName3;
            txtPresetIp3.Text = DefaultPresetIp3;
            chkAlarmEnabled.Checked = DefaultAlarmEnabled;
            _cachedAcqSpeedDelayMs = 0;
            _cachedAcqSpeedMode = "FULL_50HZ";
            _cachedAlarmThresholdGlobal = DefaultAlarmThresholdGlobal;
            for (int i = 0; i < 8; i++) _cachedAlarmThresholdChannels[i] = DefaultAlarmThresholdGlobal;
            lblStatus.Text = "已恢复为默认参数";
            UpdateControlsEnableState();
        }

        private bool IsRollingModeSelected()
        {
            if (cmbRefreshMode == null) return DefaultSpectrogramRolling;
            return cmbRefreshMode.SelectedIndex == 1;
        }

        private void SetRollingModeSelected(bool rolling)
        {
            if (cmbRefreshMode == null) return;
            cmbRefreshMode.SelectedIndex = rolling ? 1 : 0;
        }

        #region 加解密算法 (与 Java 对应)
        public static string EncryptString(string plainText)
        {
            if (string.IsNullOrEmpty(plainText)) return "";
            byte[] plainBytes = Encoding.UTF8.GetBytes(plainText);
            using (RijndaelManaged aes = new RijndaelManaged())
            {
                aes.KeySize = 128;
                aes.BlockSize = 128;
                aes.Mode = CipherMode.CBC;
                aes.Padding = PaddingMode.PKCS7;
                aes.Key = AesKey;
                aes.IV = AesIv;

                using (ICryptoTransform encryptor = aes.CreateEncryptor())
                {
                    byte[] cipherBytes = encryptor.TransformFinalBlock(plainBytes, 0, plainBytes.Length);
                    return EncPrefix + Convert.ToBase64String(cipherBytes);
                }
            }
        }

        public static string DecryptString(string cipherText)
        {
            if (string.IsNullOrEmpty(cipherText)) return "";
            string trimmed = cipherText.Trim();
            if (!trimmed.Contains("ENC:"))
            {
                return cipherText; // 原样返回明文
            }
            int idx = trimmed.IndexOf("ENC:");
            string b64 = trimmed.Substring(idx + 4).Trim();
            byte[] cipherBytes = Convert.FromBase64String(b64);

            using (RijndaelManaged aes = new RijndaelManaged())
            {
                aes.KeySize = 128;
                aes.BlockSize = 128;
                aes.Mode = CipherMode.CBC;
                aes.Padding = PaddingMode.PKCS7;
                aes.Key = AesKey;
                aes.IV = AesIv;

                using (ICryptoTransform decryptor = aes.CreateDecryptor())
                {
                    byte[] plainBytes = decryptor.TransformFinalBlock(cipherBytes, 0, cipherBytes.Length);
                    return Encoding.UTF8.GetString(plainBytes);
                }
            }
        }
        #endregion

        #region 属性解析辅助
        private static Dictionary<string, string> ParseProperties(string text)
        {
            Dictionary<string, string> map = new Dictionary<string, string>(StringComparer.OrdinalIgnoreCase);
            if (string.IsNullOrEmpty(text)) return map;

            using (StringReader sr = new StringReader(text))
            {
                string line;
                while ((line = sr.ReadLine()) != null)
                {
                    line = line.Trim();
                    if (line.StartsWith("#") || line.StartsWith("!") || string.IsNullOrEmpty(line))
                        continue;
                    int eqIdx = line.IndexOf('=');
                    if (eqIdx > 0)
                    {
                        string k = line.Substring(0, eqIdx).Trim();
                        string v = line.Substring(eqIdx + 1).Trim();
                        map[k] = v;
                    }
                }
            }
            return map;
        }

        private static bool ParseBool(Dictionary<string, string> dict, string key, bool defVal)
        {
            if (dict.ContainsKey(key))
            {
                string v = dict[key].ToLower();
                if (v == "true" || v == "1" || v == "yes") return true;
                if (v == "false" || v == "0" || v == "no") return false;
            }
            return defVal;
        }

        private static decimal ParseDecimal(Dictionary<string, string> dict, string key, decimal defVal, decimal min, decimal max)
        {
            if (dict.ContainsKey(key))
            {
                decimal d;
                if (decimal.TryParse(dict[key], out d))
                {
                    if (d < min) return min;
                    if (d > max) return max;
                    return d;
                }
            }
            return defVal;
        }

        private static int ParseInt(Dictionary<string, string> dict, string key, int defVal, int min, int max)
        {
            if (dict.ContainsKey(key))
            {
                int i;
                if (int.TryParse(dict[key], out i))
                {
                    if (i < min) return min;
                    if (i > max) return max;
                    return i;
                }
            }
            return defVal;
        }
        #endregion
    }
}
