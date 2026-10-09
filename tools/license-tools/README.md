# MAC 申请与内部授权工具（JavaFX 1.2.0 自动加载版）

新版入口位于 `dist-autoload-1.2/`。旧版窗口仍在运行，因此 `dist/` 保留旧版文件；使用自动加载版请从新目录启动。

两个桌面程序已改用 JavaFX 21.0.12。Windows 导入、保存文件使用系统原生对话框；导出后可在资源管理器中定位。Linux 使用对应平台对话框和 xdg-open。

内部生成器已简化为客户、签发人、MAC 三项输入，软件根目录的 password.txt 和 issuer.vault 在启动时自动加载。38 项测试、Windows 自动加载签发流程及 JavaFX 界面检查通过，Linux 实机运行尚未验证。

两个独立程序、四个 x64 发行包：

| 用途 | Windows | Linux |
| --- | --- | --- |
| 客户采集 MAC | customer-windows-x64/PMB-MachineInfo/PMB-MachineInfo.exe | customer-linux-x64/PMB-MachineInfo/start.sh |
| 公司内部签发 | internal-windows-x64/PMB-LicenseIssuer/PMB-LicenseIssuer.exe | internal-linux-x64/PMB-LicenseIssuer/start.sh |

成品在 `dist-autoload-1.2/`，仅把 `customer-*` 发行包交给客户。内部包按当前要求同时包含 password.txt 和 issuer.vault，位于 EXE/start.sh 同级目录，不需要手动配置。原始材料仍在 private/ 保留；该目录已加入 Git 忽略规则。

详细操作分别见 CUSTOMER-README.md、INTERNAL-README.md。本次兼容原有后端授权算法和 `license.prop`，未修改业务入口或后端许可规则。现有格式无到期时间；内部包通过限定分发对象管理使用权限。

## 构建与维护

需要 Windows x64、JDK 21（含 jpackage）、Python 3.12、已核验的 Windows Temurin JRE 目录及 Linux Temurin JRE tar.gz。构建命令：

```powershell
python build.py --jdk 'E:\000_JavaFX\Jdk\JDK21' --windows-runtime 'F:\Test Folder\PMB_KP_Build\payload\runtime\java' --linux-runtime 'dist\customer-linux-x64\PMB-MachineInfo\runtime.tar.gz' --javafx-root 'build\javafx'
# 升级自动保留现有 vault，不要重新初始化密码。
```

build.py 先编译独立代码并调用现有后端的授权校验方法验证兼容性；不会启动 Spring 或业务服务。后端 target/classes 与 target/postgres-classpath.txt 需已由当前项目生成。测试含真实本机 MAC 的隔离许可证，测试目录不能分发。

构建使用 jpackage 的 app-image 模式生成便携程序目录，不生成 MSI、安装器 EXE，也不安装服务。首次构建使用空的 build/test-run、dist 目录；已有产物和授权材料不会自动覆盖。后续构建前将需保留的产物另行归档，严禁把 private 目录作为临时构建缓存删除。

Linux 包在首次运行时验证并解压随附 JRE，支持 glibc Linux x86_64，不覆盖 Alpine/musl 或 ARM64。Windows 编译检查和共同 Java 逻辑验证通过不等于已做 Linux 实机验收，具体证据在 dist/VALIDATION.json。
