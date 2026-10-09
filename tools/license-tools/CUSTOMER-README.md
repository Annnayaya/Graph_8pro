# PowerMonitoring 机器信息采集工具

本工具可交付给客户，仅采集本机网卡 MAC 和申请所需的系统信息，不生成授权、不联网、不上传、不修改网络。支持 Windows x64、Linux x64。两种发行目录均附带 Java 21 运行时，无需另装 Java。

## JavaFX 界面与原生文件操作

桌面界面已更新为 JavaFX 21。Windows 文件导入与保存调用系统原生对话框；导出后点击“在文件夹中显示”，在 Windows 资源管理器中定位文件。Linux 使用平台文件对话框和 xdg-open。

## Windows

完整解压客户包，双击 `PMB-MachineInfo.exe`。选择长期使用的网卡，点击“导出申请文件”，把生成的 `machine-request.properties` 发给本司授权人员。不能只复制 EXE，它需要旁边的 app/runtime 目录。

命令行：

```bat
cli.cmd --list
cli.cmd --mac 02:AB:CD:12:34:56 --output machine-request.properties
```

导出的 MAC 必须属于当前运行环境；示例 MAC 需要替换为实际网卡地址。

## Linux

适用于常规 glibc 的 Linux x86_64，需 Bash、tar、sha256sum。首次启动会校验并解压随附运行时，需要工具目录可写。Alpine/musl 和 ARM64 不在本次版本范围内。

```bash
chmod +x start.sh
./start.sh --list
./start.sh --mac 02:AB:CD:12:34:56 --output machine-request.properties
```

有桌面环境时直接 `./start.sh` 打开图形界面；没有图形显示时默认列出网卡。图形界面需要系统桌面及字体库。申请文件不含授权算法、授权材料或解锁密码。

## 网卡选择和许可安装

优先选择主板有线网卡等稳定接口，避免临时 USB 网卡、随机 MAC 的 Wi-Fi 或会重建的虚拟接口。“虚拟子接口”字段来自 Java 网卡 API，不能识别所有虚拟化网卡，请按设备情况确认。

Windows 在实际运行后端的主机采集。Linux 原生运行在实际主机采集；Linux Docker 安装时，将同一个授权 MAC 配置为后端容器固定 MAC。本项目 Linux 安装工具会从许可证读取该 MAC；MAC 绑定不等于不可克隆的硬件指纹。

收到 `license.prop` 后，Windows 通过项目 `First-Run.ps1 -LicenseFile` 导入；Linux 按部署说明使用 `prepare.py --license`，容器内最终路径为 `/pm/license.prop`。本工具不会自动覆盖授权文件，已有同名输出时会报错。
