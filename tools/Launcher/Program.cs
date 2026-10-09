using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.IO;
using System.Text;
using System.Windows.Forms;

namespace GraphApp8Pro.Launcher
{
    static class Program
    {
        private const string AppTitle = "Graph_8Pro";
        private const string MainClass = "com.Spectrogram.ZmGis8CH.gis_app_8CH.GisXYZ";

        [STAThread]
        static void Main(string[] args)
        {
            try
            {
                string exeDir = AppDomain.CurrentDomain.BaseDirectory;
                string appDir = exeDir;

                // 若当前目录无 lib 或 pics，检查上一级目录（适配 exe 存放在专属输出目录如 output 的情况）
                if (!Directory.Exists(Path.Combine(appDir, "lib")) || !Directory.Exists(Path.Combine(appDir, "pics")))
                {
                    string parent = Path.GetFullPath(Path.Combine(appDir, ".."));
                    if (Directory.Exists(Path.Combine(parent, "lib")) && Directory.Exists(Path.Combine(parent, "pics")))
                    {
                        appDir = parent;
                    }
                }
                Directory.SetCurrentDirectory(appDir);

                // 1. 寻找可用 Java 运行时 (优先使用配套的轻量化 JDK11，支持当前目录及父目录)
                string javaExe = FindJavaExecutable(appDir);
                if (string.IsNullOrEmpty(javaExe) || !File.Exists(javaExe))
                {
                    MessageBox.Show(
                        "未找到兼容的 Java 运行环境（JDK 11 及以上）！\n\n" +
                        "请确认软件目录下存在配套的:\n" +
                        "\"jdk11-轻量化版本（适配当前项目）\" 文件夹，\n" +
                        "或者系统中已配置正确指向 JDK 11 的 JAVA_HOME 环境变量。",
                        "Graph_8Pro 启动失败 - 缺少 Java 环境",
                        MessageBoxButtons.OK,
                        MessageBoxIcon.Error
                    );
                    return;
                }

                // 2. 寻找与同步主程序 JAR 包
                string mainJar = ResolveAndSyncMainJar(appDir);
                if (string.IsNullOrEmpty(mainJar) || !File.Exists(mainJar))
                {
                    MessageBox.Show(
                        "未找到 Graph_8Pro 谱图软件主程序核心文件！\n\n" +
                        "请确保软件目录下的 lib 文件夹中存在 gis-app-8CH.jar，\n" +
                        "或 target 目录下已编译生成对应的 jar 包。",
                        "Graph_8Pro 启动失败 - 缺少主程序包",
                        MessageBoxButtons.OK,
                        MessageBoxIcon.Error
                    );
                    return;
                }

                // 3. 构建 ClassPath
                string classpath = BuildClassPath(appDir, mainJar);

                // 4. 组装 JVM 启动参数
                StringBuilder jvmArgs = new StringBuilder();
                jvmArgs.Append("-Xms256m -Xmx1024m -XX:+UseG1GC -Dfile.encoding=UTF-8 ");
                jvmArgs.Append("-cp \"").Append(classpath).Append("\" ");
                jvmArgs.Append(MainClass);

                if (args != null && args.Length > 0)
                {
                    foreach (string arg in args)
                    {
                        if (arg.Contains(" "))
                        {
                            jvmArgs.Append(" \"").Append(arg).Append("\"");
                        }
                        else
                        {
                            jvmArgs.Append(" ").Append(arg);
                        }
                    }
                }

                // 5. 启动主程序进程
                ProcessStartInfo psi = new ProcessStartInfo();
                psi.FileName = javaExe;
                psi.Arguments = jvmArgs.ToString();
                psi.WorkingDirectory = appDir;
                psi.UseShellExecute = false;
                psi.CreateNoWindow = true;

                Process.Start(psi);
            }
            catch (Exception ex)
            {
                MessageBox.Show(
                    "启动 Graph_8Pro 主程序时发生异常:\n\n" + ex.Message + "\n\n" + ex.StackTrace,
                    "Graph_8Pro 启动异常",
                    MessageBoxButtons.OK,
                    MessageBoxIcon.Error
                );
            }
        }

        private static string FindJavaExecutable(string appDir)
        {
            // 候选目录列表：【最优先严格查找 jdk11 与 jdk 目录】
            List<string> candidateDirs = new List<string>();

            // 1. 最优先：当前目录及上级目录中的 jdk11、jdk
            candidateDirs.Add(Path.Combine(appDir, "jdk11"));
            candidateDirs.Add(Path.Combine(appDir, "jdk"));
            candidateDirs.Add(Path.GetFullPath(Path.Combine(appDir, @"..\jdk11")));
            candidateDirs.Add(Path.GetFullPath(Path.Combine(appDir, @"..\jdk")));

            // 2. 扫描以 jdk 开头的其他目录 (如 jdk-11, jdk11-light, jdk11-轻量化版本 等)
            TryAddMatchingDirs(candidateDirs, appDir, "jdk*");
            TryAddMatchingDirs(candidateDirs, Path.GetFullPath(Path.Combine(appDir, "..")), "jdk*");

            // 3. 次级候选：jre11 与 jre 目录
            candidateDirs.Add(Path.Combine(appDir, "jre11"));
            candidateDirs.Add(Path.Combine(appDir, "jre"));
            candidateDirs.Add(Path.GetFullPath(Path.Combine(appDir, @"..\jre11")));
            candidateDirs.Add(Path.GetFullPath(Path.Combine(appDir, @"..\jre")));
            TryAddMatchingDirs(candidateDirs, appDir, "jre*");
            TryAddMatchingDirs(candidateDirs, Path.GetFullPath(Path.Combine(appDir, "..")), "jre*");

            // 遍历所有目录，优先寻找 javaw.exe，其次 java.exe
            foreach (string dir in candidateDirs)
            {
                if (!Directory.Exists(dir)) continue;

                string javaw = Path.Combine(dir, @"bin\javaw.exe");
                if (File.Exists(javaw)) return javaw;

                string java = Path.Combine(dir, @"bin\java.exe");
                if (File.Exists(java)) return java;
            }

            // 检查 JAVA_HOME 环境变量
            string javaHome = Environment.GetEnvironmentVariable("JAVA_HOME");
            if (!string.IsNullOrEmpty(javaHome) && Directory.Exists(javaHome))
            {
                string javawInHome = Path.Combine(javaHome, @"bin\javaw.exe");
                if (File.Exists(javawInHome)) return javawInHome;

                string javaInHome = Path.Combine(javaHome, @"bin\java.exe");
                if (File.Exists(javaInHome)) return javaInHome;
            }

            // 检查 PATH 系统路径中的 javaw / java
            string pathEnv = Environment.GetEnvironmentVariable("PATH");
            if (!string.IsNullOrEmpty(pathEnv))
            {
                string[] paths = pathEnv.Split(Path.PathSeparator);
                foreach (string p in paths)
                {
                    try
                    {
                        if (string.IsNullOrEmpty(p) || !Directory.Exists(p)) continue;
                        string targetJavaw = Path.Combine(p, "javaw.exe");
                        if (File.Exists(targetJavaw)) return targetJavaw;
                        string targetJava = Path.Combine(p, "java.exe");
                        if (File.Exists(targetJava)) return targetJava;
                    }
                    catch
                    {
                        // 忽略单个路径访问异常
                    }
                }
            }

            return null;
        }

        private static void TryAddMatchingDirs(List<string> list, string parentDir, string pattern)
        {
            try
            {
                if (Directory.Exists(parentDir))
                {
                    string[] matched = Directory.GetDirectories(parentDir, pattern, SearchOption.TopDirectoryOnly);
                    foreach (string d in matched)
                    {
                        string full = Path.GetFullPath(d);
                        if (!list.Contains(full))
                        {
                            list.Add(full);
                        }
                    }
                }
            }
            catch
            {
            }
        }

        private static string ResolveAndSyncMainJar(string appDir)
        {
            string targetJar = Path.Combine(appDir, @"target\gis-app-8CH-0.0.1-SNAPSHOT.jar");
            if (!File.Exists(targetJar))
            {
                string parentTarget = Path.Combine(appDir, @"..\target\gis-app-8CH-0.0.1-SNAPSHOT.jar");
                if (File.Exists(parentTarget)) targetJar = Path.GetFullPath(parentTarget);
            }
            string libJar = Path.Combine(appDir, @"lib\gis-app-8CH.jar");
            string libGraphJar = Path.Combine(appDir, @"lib\Graph_8Pro.jar");
            string rootJar = Path.Combine(appDir, @"gis-app-8CH.jar");
            string rootGraphJar = Path.Combine(appDir, @"Graph_8Pro.jar");
            string outArtifactJar = Path.Combine(appDir, @"out\artifacts\gis_app_8CH_jar\gis-app-8CH.jar");

            // 如果 target 中有最新构建的 jar，自动同步到 lib/gis-app-8CH.jar
            if (File.Exists(targetJar))
            {
                try
                {
                    bool needCopy = false;
                    if (!File.Exists(libJar))
                    {
                        needCopy = true;
                    }
                    else
                    {
                        DateTime tTime = File.GetLastWriteTime(targetJar);
                        DateTime lTime = File.GetLastWriteTime(libJar);
                        if (tTime > lTime)
                        {
                            needCopy = true;
                        }
                    }

                    if (needCopy)
                    {
                        string libDir = Path.GetDirectoryName(libJar);
                        if (!Directory.Exists(libDir)) Directory.CreateDirectory(libDir);
                        File.Copy(targetJar, libJar, true);
                    }
                }
                catch
                {
                    // 若文件被占用等，忽略复制错误，直接使用已有版本
                }
                return libJar;
            }

            if (File.Exists(libJar)) return libJar;
            if (File.Exists(libGraphJar)) return libGraphJar;
            if (File.Exists(rootGraphJar)) return rootGraphJar;
            if (File.Exists(rootJar)) return rootJar;
            if (File.Exists(outArtifactJar)) return outArtifactJar;

            return null;
        }

        private static string BuildClassPath(string appDir, string mainJar)
        {
            List<string> cpList = new List<string>();
            cpList.Add(mainJar);

            // 扫描 lib 目录下的依赖 jar
            string libDir = Path.Combine(appDir, "lib");
            if (Directory.Exists(libDir))
            {
                string[] libJars = Directory.GetFiles(libDir, "*.jar", SearchOption.TopDirectoryOnly);
                foreach (string j in libJars)
                {
                    if (!j.Equals(mainJar, StringComparison.OrdinalIgnoreCase) && !cpList.Contains(j))
                    {
                        cpList.Add(j);
                    }
                }
            }

            // 扫描 libs 目录下的依赖 jar
            string libsDir = Path.Combine(appDir, "libs");
            if (Directory.Exists(libsDir))
            {
                string[] libsJars = Directory.GetFiles(libsDir, "*.jar", SearchOption.TopDirectoryOnly);
                foreach (string j in libsJars)
                {
                    if (!cpList.Contains(j))
                    {
                        cpList.Add(j);
                    }
                }
            }

            return string.Join(";", cpList.ToArray());
        }
    }
}
