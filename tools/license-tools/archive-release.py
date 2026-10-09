from pathlib import Path
import hashlib,json,zipfile,re,shutil,sys
from datetime import datetime
R=Path(__file__).resolve().parent
D=Path(sys.argv[1]).resolve() if len(sys.argv)>1 else R/'dist'
def sha(path):
    h=hashlib.sha256()
    with path.open('rb') as f:
        for data in iter(lambda:f.read(4*1024*1024),b''):h.update(data)
    return h.hexdigest()
def main():
    version='1.2.0-autoload-20260915';artifacts={}
    for edition in ('customer-windows-x64','customer-linux-x64','internal-windows-x64','internal-linux-x64'):
        root=D/edition;path=D/(f'PMB-{edition}-{version}.zip')
        if path.exists():raise FileExistsError(path)
        files=sorted(p for p in root.rglob('*') if p.is_file() and p != root/'SHA256SUMS.json')
        manifest={p.relative_to(root).as_posix():sha(p) for p in files}
        (root/'SHA256SUMS.json').write_text(json.dumps(manifest,indent=2)+'\n',encoding='utf-8')
        files.append(root/'SHA256SUMS.json')
        for p in files:
            assert p.name not in ('initial-unlock-password.txt','license.prop','request.properties')
        if edition.startswith('customer'):assert not any(p.suffix=='.vault' or p.name=='password.txt' for p in files)
        else:
            assert len([p for p in files if p.suffix=='.vault'])==1
            assert (root/'PMB-LicenseIssuer/password.txt').is_file()
            assert (root/'PMB-LicenseIssuer/issuer.vault').is_file()
        with zipfile.ZipFile(path,'w',zipfile.ZIP_DEFLATED,compresslevel=6) as z:
            for p in files:
                info=zipfile.ZipInfo.from_file(p,p.relative_to(root).as_posix());info.create_system=3
                mode=0o755 if p.name.endswith('.sh') else (0o600 if p.suffix=='.vault' or p.name=='password.txt' else 0o644)
                info.external_attr=(0o100000|mode)<<16;info.compress_type=zipfile.ZIP_DEFLATED
                with p.open('rb') as source,z.open(info,'w',force_zip64=True) as dest:shutil.copyfileobj(source,dest,4*1024*1024)
        with zipfile.ZipFile(path) as z:
            assert z.testzip() is None
            for name,expected in manifest.items():assert hashlib.sha256(z.read(name)).hexdigest()==expected
        artifacts[path.name]={'sha256':sha(path),'bytes':path.stat().st_size,'fileCount':len(files)}
    (D/'RELEASE.json').write_text(json.dumps({'version':version,'createdAt':datetime.now().astimezone().isoformat(),'architectures':['Windows x64','Linux glibc x86_64'],'artifacts':artifacts,'passwordIncludedInInternalPackages':True,'passwordIncludedInCustomerPackages':False,'installerGenerated':False,'automaticLoadingFiles':['PMB-LicenseIssuer/password.txt','PMB-LicenseIssuer/issuer.vault']},indent=2)+'\n',encoding='utf-8')
    (D/'README.md').write_text('''# 授权工具交付

仅将 文件名含 customer的两个 ZIP 按客户平台分发。文件名含 internal的 ZIP 仅供本司人员使用，禁止放入客户安装包。

JavaFX 界面：Windows 文件选择/保存使用原生对话框，导出后可在资源管理器中定位。原有 vault 和密码保留。

Windows：解压完整目录后双击 PMB-MachineInfo.exe / PMB-LicenseIssuer.exe。
Linux：解压后执行对应目录的 ./start.sh；无桌面时使用 --list 或签发命令，详见包内 README。

内部生成器根目录已包含 password.txt 和 issuer.vault，启动自动加载，不用再输入密码或选择材料。内部包仅供本司人员使用，不能发给客户。客户采集包不含密码或授权材料。

工具兼容当前后端 MAC 授权，无到期时间。38 项测试及 Windows 自动加载签发流程检查通过；Linux 脚本和共享 JAR 已核验，尚未在真实 Linux 主机运行。详见 VALIDATION.json。

四个 ZIP 的摘要和大小见 RELEASE.json，包内各文件摘要见 SHA256SUMS.json。
''',encoding='utf-8')
    print(json.dumps(artifacts,indent=2))
if __name__=='__main__':main()
