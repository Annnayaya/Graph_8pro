# 内部授权生成器 1.2.0

解压后直接启动。程序自动加载根目录的密码和授权材料，不用输入密码或选择材料。

```text
PMB-LicenseIssuer/
  PMB-LicenseIssuer.exe   ← Windows 启动入口
  start.sh                ← Linux 版本的启动入口
  password.txt            ← 密码，UTF-8 单行文本
  issuer.vault            ← 原有加密授权材料
  ...其他运行文件
```

Windows 双击 EXE；Linux 执行 `./start.sh`。只需导入客户申请或填写 MAC，填写客户及签发人，点击“生成授权文件”，选择保存位置。导出后可点击“在文件夹中显示”。只有 `license.prop` 交付客户，签发记录留在公司。

加载失败时检查根目录的 `password.txt` 和 `issuer.vault` 是否配套，修正后点击“重新加载”。文件路径始终相对软件目录，与从哪个工作目录启动无关。现有授权材料和密码保持不变；仅编辑 password.txt 不会更改 vault 的加密密码。

内部目录和内部 ZIP 都包含密码及授权材料，**仅限本司人员使用，不能发给客户**。客户只接收独立的 MAC 采集工具及签发后的 license.prop。

命令行同样自动加载：

```bat
cli.cmd --request machine-request.properties --output license.prop --customer "客户名称" --operator "签发人"
```

```bash
./start.sh --request machine-request.properties --output license.prop --customer '客户名称' --operator '签发人'
```

沿用当前后端 MAC 授权格式，不新增到期时间。Windows/Linux x64 均附带 Java 21 和 JavaFX；Linux 需 glibc、Bash、tar、sha256sum，有桌面需 GTK 3，文件定位需 xdg-open。Linux 实机运行尚待验证。
