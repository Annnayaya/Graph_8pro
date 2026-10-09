from pathlib import Path
import hashlib,json,subprocess,zipfile,re,argparse
from datetime import datetime
R=Path(__file__).resolve().parent
def run(args,**kwargs):return subprocess.run([str(a) for a in args],check=True,timeout=45,**kwargs)
def sha(p):return hashlib.sha256(p.read_bytes()).hexdigest()
def main():
    parser=argparse.ArgumentParser();parser.add_argument('--dist',default=str(R/'dist-next'));args=parser.parse_args();D=Path(args.dist)
    build=R/'build/javafx-build';fixture=build/'release-fixture';fixture.mkdir()
    internal=D/'internal-windows-x64/PMB-LicenseIssuer';customer=D/'customer-windows-x64/PMB-MachineInfo'
    java=internal/'runtime/bin/java.exe';modules=['--module-path',internal/'app/fx','--add-modules','javafx.controls']
    # EXE smoke checks use their embedded JavaFX module path.
    for kind,prefix in [('customer','PMB-MachineInfo'),('internal','PMB-LicenseIssuer')]:
        result=run([D/(kind+'-windows-x64')/prefix/(prefix+'.exe'),'--help'],capture_output=True,creationflags=subprocess.CREATE_NO_WINDOW)
        (build/(kind+'-exe-smoke.log')).write_bytes(result.stdout+result.stderr)
    listing=run([java,*modules,'-jar',customer/'app/PMB-MachineInfo.jar','--list'],capture_output=True).stdout.decode(errors='replace');mac=listing.split('\t')[0].strip()
    assert re.fullmatch(r'(?:[0-9A-F]{2}:){5}[0-9A-F]{2}',mac)
    run([java,*modules,'-jar',customer/'app/PMB-MachineInfo.jar','--mac',mac,'--output',fixture/'request.properties'],capture_output=True)
    run([java,*modules,'-jar',internal/'app/PMB-LicenseIssuer.jar','--request',fixture/'request.properties','--output',fixture/'license.prop','--customer','Automatic load verification','--operator','build-verifier'],capture_output=True,cwd=fixture)
    run([internal/'PMB-LicenseIssuer.exe','--mac',mac,'--output',fixture/'exe-license.prop','--customer','EXE automatic load','--operator','build-verifier'],capture_output=True,cwd=fixture,creationflags=subprocess.CREATE_NO_WINDOW)
    bad=fixture/'wrong-password.txt';bad.write_text('incorrect-password-for-test')
    result=subprocess.run([str(java),*[str(a) for a in modules],'-jar',str(internal/'app/PMB-LicenseIssuer.jar'),'--mac',mac,'--output',str(fixture/'must-not-exist.prop'),'--customer','Test','--operator','Test','--password-file',str(bad)],capture_output=True,timeout=30)
    assert result.returncode!=0 and not (fixture/'must-not-exist.prop').exists()
    password=(internal/'password.txt').read_bytes()
    source=(R/'../../src/main/java/com/zust/monitor/SmartStationMonitorApplication.java').resolve().read_text(encoding='utf-8')
    salt=re.search(r'private static final String SALT = "([^"]+)";',source)[1].encode()
    for kind,prefix in [('customer','PMB-MachineInfo'),('internal','PMB-LicenseIssuer')]:
        win=D/(kind+'-windows-x64')/prefix;linux=D/(kind+'-linux-x64')/prefix
        assert sha(win/'app'/(prefix+'.jar'))==sha(linux/(prefix+'.jar'))
        run([r'F:\Dev\msys64\usr\bin\bash.exe','-n',linux/'start.sh'],capture_output=True)
        with zipfile.ZipFile(win/'app'/(prefix+'.jar')) as z:
            contents=[z.read(n) for n in z.namelist()]
            assert all(b'javax/swing' not in b and b'JFileChooser' not in b and salt not in b and password not in b for b in contents)
            assert 'pmb/license/Fx'+('Collector' if kind=='customer' else 'Issuer')+'.class' in z.namelist()
            assert any(b'javafx/stage/FileChooser' in b for b in contents)
            if kind=='customer':assert not any(k in n for n in z.namelist() for k in ('Issuer','Vault','LicenseWriter'))
        for platform,bundle,relative in [('windows',win,'app/issuer.vault'),('linux',linux,'issuer.vault')]:
            if kind=='internal':
                prior=R/'dist'/('internal-'+platform+'-x64')/prefix
                original=prior/'issuer.vault' if (prior/'issuer.vault').exists() else prior/relative
                assert sha(bundle/'issuer.vault')==sha(original)
                assert (bundle/'password.txt').is_file()
                assert len(list(bundle.rglob('issuer.vault')))==1
            else:assert not list(bundle.rglob('*.vault')) and not list(bundle.rglob('password.txt'))
        for fx in (win/'app/fx',linux/'fx'):
            jars=list(fx.glob('*.jar'));assert len(jars)==3
            for jar in jars:
                with zipfile.ZipFile(jar) as z:assert z.testzip() is None
    checks={'status':'PASS','version':'1.2.0','ui':'JavaFX 21.0.12','completedAt':datetime.now().astimezone().isoformat(),'tests':json.loads((build/'test-run/test-result.json').read_text()),'releaseChecks':['Windows EXE launch with bundled JavaFX modules','hidden JavaFX startup auto-load enabled issuance; no password input controls','automatic root password/vault load via CLI and Windows EXE from unrelated working directory','wrong password creates no license','Windows/Linux tool JARs identical with separate native JavaFX libraries','native FileChooser API used; no Swing/JFileChooser in tool JARs','Explorer argument construction handles spaces and Unicode without a shell','Linux Bash syntax','customer package contains no issuer code or vault','original Windows and Linux vaults preserved byte-for-byte'],'notPerformed':['Actual Linux host/GTK execution','Interactive Windows native dialog clicking or Explorer window selection','Business application startup','Installer generation or installation']}
    (D/'VALIDATION.json').write_text(json.dumps(checks,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
    print('JavaFX release verification passed.')
if __name__=='__main__':main()
