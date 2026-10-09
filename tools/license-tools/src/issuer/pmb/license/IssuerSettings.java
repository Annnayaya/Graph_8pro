package pmb.license;

import java.nio.file.*;
import java.io.IOException;
import java.util.Arrays;

/** Company-only settings, stored beside the EXE/start.sh rather than in its app subdirectory. */
public final class IssuerSettings {
    private IssuerSettings(){}
    public static Path root(){
        Path home=Machine.home().toAbsolutePath().normalize();
        if(System.getProperty("pmb.tool.home")!=null)return home;
        if(home.getFileName()!=null && home.getFileName().toString().equals("app") && Files.isRegularFile(home.getParent().resolve("PMB-LicenseIssuer.exe")))return home.getParent();
        return home;
    }
    public static char[] readPassword(Path path)throws IOException {
        if(!Files.isRegularFile(path))throw new IOException("软件根目录缺少 password.txt，请放回密码文件后重新加载。");
        if(Files.size(path)>4096)throw new IOException("password.txt 文件过大。");
        String text=Files.readString(path);if(text.startsWith("\uFEFF"))text=text.substring(1);
        text=text.replaceFirst("[\\r\\n]+$","");
        if(text.isEmpty() || text.contains("\n") || text.contains("\r"))throw new IOException("password.txt 必须只包含一行非空密码。");
        return text.toCharArray();
    }
    public static Vault.Material load()throws Exception{return load(root());}
    public static Vault.Material load(Path directory)throws Exception {
        Path vault=directory.resolve("issuer.vault");
        if(!Files.isRegularFile(vault))throw new IOException("软件根目录缺少 issuer.vault，请放回授权材料后重新加载。");
        char[] password=readPassword(directory.resolve("password.txt"));
        try{return Vault.unlock(vault,password);}finally{Arrays.fill(password,'\0');}
    }
}
