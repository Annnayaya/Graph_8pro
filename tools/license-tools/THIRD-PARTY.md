# 运行时来源

Windows x64 随附 Eclipse Temurin Java 21.0.12.1+1，复用已核验的项目部署运行时。Linux x64 随附相同系列的官方 JRE：

https://github.com/adoptium/temurin21-binaries/releases/tag/jdk-21.0.12.1%2B1

Linux 归档 SHA-256：`2413149700df0f7d440500a84a8f764c535f21e5a5e87d38328b64eec2c5b500`，与 Adoptium 官方 API 提供摘要核对。

Java 运行时保留原发行版的 legal/、NOTICE 等材料，Linux 材料在随附 runtime.tar.gz 内。运行时适用 GPLv2 with Classpath Exception 等各组件原许可；对应源码由上述 Temurin 发布页提供。Windows EXE 入口由本机 JDK 21 jpackage 生成，图形界面使用下述 JavaFX 模块。

JavaFX 1.1.0 工具使用 OpenJFX 21.0.12（base、graphics、controls），按 Windows/Linux 分别携带原生库。来源：https://repo.maven.apache.org/maven2/org/openjfx/ ，发布摘要与本地 SHA-256 见 JAVAFX-SOURCES.json。许可文本单独附在 legal/javafx，对应源码：https://github.com/openjdk/jfx21u/tree/21.0.12%2B3 。界面已从 Swing 替换为 JavaFX。
