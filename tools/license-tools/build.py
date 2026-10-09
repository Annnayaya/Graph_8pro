"""Build separate customer/internal applications; does not create setup installers or provision secrets."""
from pathlib import Path
import argparse, hashlib, json, os, shutil, subprocess, zipfile

ROOT=Path(__file__).resolve().parent
BACKEND=ROOT.parent.parent

def run(args, **kw):
    subprocess.run([str(a) for a in args],check=True,**kw)

def main():
    parser=argparse.ArgumentParser()
    parser.add_argument('--jdk',required=True)
    parser.add_argument('--windows-runtime',required=True)
    parser.add_argument('--linux-runtime',required=True)
    parser.add_argument('--javafx-root',required=True)
    parser.add_argument('--output',default=str(ROOT/'dist-next'))
    args=parser.parse_args()
    jdk=Path(args.jdk);build=ROOT/'build/javafx-build';dist=Path(args.output).resolve();fx=Path(args.javafx_root).resolve()
    build.mkdir(parents=True,exist_ok=True);dist.mkdir(exist_ok=False)
    classes=build/'classes';classes.mkdir(exist_ok=True)
    sources=list((ROOT/'src').rglob('*.java'))+list((ROOT/'test').rglob('*.java'))
    modules=['--module-path',fx/'win','--add-modules','javafx.controls']
    run([jdk/'bin/javac.exe',*modules,'-encoding','UTF-8','--release','21','-d',classes,*sources])
    shutil.copy2(ROOT/'src/common/pmb/license/tools.css',classes/'pmb/license/tools.css')
    # Use current application's unchanged verifier in an isolated cwd; no Spring startup.
    testdir=build/'test-run';testdir.mkdir(exist_ok=True)
    cp=os.pathsep.join([str(classes),str(BACKEND/'target/classes'),(BACKEND/'target/postgres-classpath.txt').read_text().strip()])
    with (build/'tests.log').open('w',encoding='utf-8') as log:
        run([jdk/'bin/java.exe',*modules,'-cp',cp,'pmb.license.ToolTests',BACKEND/'src/main/java/com/zust/monitor/SmartStationMonitorApplication.java',testdir],cwd=testdir,stdout=log,stderr=subprocess.STDOUT)
    with (build/'fx-preview.log').open('w',encoding='utf-8') as log:
        run([jdk/'bin/java.exe',*modules,'-Dpmb.tool.home='+str(testdir/'autoload'),'-Djavafx.cachedir='+str(build/'fx-cache'),'-cp',classes,'pmb.license.FxPreview',build/'ui'],stdout=log,stderr=subprocess.STDOUT)
    for kind,mainclass,prefix in [('customer','Collector','PMB-MachineInfo'),('internal','Issuer','PMB-LicenseIssuer')]:
        inputs=build/(kind+'-input');inputs.mkdir(exist_ok=True)
        jar=inputs/(prefix+'.jar')
        names=['Machine','FxUi',mainclass,'Fx'+mainclass]+(['Vault','LicenseWriter','IssuerSettings'] if kind=='internal' else [])
        with zipfile.ZipFile(jar,'w',zipfile.ZIP_DEFLATED) as z:
            z.writestr('META-INF/MANIFEST.MF',f'Manifest-Version: 1.0\nMain-Class: pmb.license.{mainclass}\n\n')
            for p in (classes/'pmb/license').glob('*.class'):
                if p.stem.split('$')[0] in names:z.write(p,p.relative_to(classes).as_posix())
            z.write(classes/'pmb/license/tools.css','pmb/license/tools.css')
        with zipfile.ZipFile(jar) as z:
            if kind=='customer': assert not any('Issuer' in n or 'Vault' in n or 'LicenseWriter' in n for n in z.namelist())
        winparent=dist/(kind+'-windows-x64');winparent.mkdir(exist_ok=True)
        shutil.copytree(fx/'win',inputs/'fx')
        run([jdk/'bin/jpackage.exe','--type','app-image','--input',inputs,'--dest',winparent,'--name',prefix,'--main-jar',jar.name,'--main-class','pmb.license.'+mainclass,'--app-version','1.2.0','--vendor','PowerMonitoring','--runtime-image',args.windows_runtime,'--java-options','-Dfile.encoding=UTF-8','--java-options','--module-path=$APPDIR/fx','--java-options','--add-modules=javafx.controls'])
        win=winparent/prefix
        (win/'cli.cmd').write_text('@echo off\r\n"%~dp0runtime\\bin\\java.exe" --module-path "%~dp0app\\fx" --add-modules javafx.controls -Dfile.encoding=UTF-8 -jar "%~dp0app\\'+jar.name+'" %*\r\nexit /b %errorlevel%\r\n',encoding='ascii')
        linux=dist/(kind+'-linux-x64')/prefix;linux.mkdir(parents=True)
        shutil.copy2(jar,linux/jar.name)
        shutil.copytree(fx/'linux',linux/'fx')
        if kind=='internal':
            for platform,bundle,relative in [('windows',win,'app/issuer.vault'),('linux',linux,'issuer.vault')]:
                prior=ROOT/'dist'/('internal-'+platform+'-x64')/prefix
                original=prior/'issuer.vault' if (prior/'issuer.vault').exists() else prior/relative
                shutil.copy2(original,bundle/'issuer.vault')
                password=prior/'password.txt' if (prior/'password.txt').exists() else ROOT/'private/initial-unlock-password.txt'
                shutil.copyfile(password,bundle/'password.txt')
                assert original.read_bytes()==(bundle/'issuer.vault').read_bytes()
        runtime=Path(args.linux_runtime);shutil.copy2(runtime,linux/'runtime.tar.gz')
        (linux/'runtime.sha256').write_text(hashlib.sha256(runtime.read_bytes()).hexdigest()+'  runtime.tar.gz\n',encoding='ascii')
        launcher='''#!/usr/bin/env bash
set -euo pipefail
base=$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)
if [[ ! -x "$base/runtime/bin/java" ]]; then
  [[ ! -e "$base/runtime" ]] || { echo 'Incomplete runtime directory; inspect before retrying.' >&2; exit 1; }
  mkdir -- "$base/.runtime-unpack-lock" || { echo 'Another extraction is running, or its lock remains; inspect .runtime-unpack-lock.' >&2; exit 1; }
  temp=$(mktemp -d "$base/.runtime-XXXXXX")
  cleanup() { if [[ "$temp" == "$base"/.runtime-* && -d "$temp" ]]; then rm -rf -- "$temp"; fi; rmdir -- "$base/.runtime-unpack-lock"; }
  trap cleanup EXIT
  (cd -- "$base" && sha256sum --check runtime.sha256)
  tar -xzf "$base/runtime.tar.gz" --strip-components=1 --no-same-owner -C "$temp"
  test -x "$temp/bin/java"
  mv -- "$temp" "$base/runtime"
  rmdir -- "$base/.runtime-unpack-lock"
  trap - EXIT
fi
exec "$base/runtime/bin/java" --module-path "$base/fx" --add-modules javafx.controls -Dfile.encoding=UTF-8 -jar "$base/@@JAR@@" "$@"
'''
        (linux/'start.sh').write_text(launcher.replace('@@JAR@@',jar.name),encoding='utf-8',newline='\n')
        (linux/'start.sh').chmod(0o755)
        for bundle in (win,linux):
            shutil.copy2(ROOT/('CUSTOMER-README.md' if kind=='customer' else 'INTERNAL-README.md'),bundle/'README.md')
            shutil.copy2(ROOT/'THIRD-PARTY.md',bundle/'THIRD-PARTY.md')
            shutil.copytree(ROOT/'legal/javafx',bundle/'legal/javafx')
            shutil.copy2(fx/'sources.json',bundle/'JAVAFX-SOURCES.json')
    shutil.copy2(fx/'sources.json',dist/'JAVAFX-SOURCES.json')
    print('Both JavaFX platform editions built. Existing per-platform issuer vaults preserved byte-for-byte.')

if __name__=='__main__':main()
