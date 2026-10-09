package pmb.license;

import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public final class ToolTests {
    private static int passed;
    private static void check(boolean condition,String name){if(!condition)throw new AssertionError(name);passed++;System.out.println("PASS "+name);}
    private static void rejects(Checked operation,String name)throws Exception{try{operation.run();}catch(Exception expected){passed++;System.out.println("PASS "+name);return;}throw new AssertionError("Expected rejection: "+name);}
    @FunctionalInterface interface Checked {void run()throws Exception;}
    public static void main(String[] args)throws Exception {
        Path source=Path.of(args[0]),root=Path.of(args[1]);Files.createDirectories(root);
        check(Machine.normalize("02-ab-cd-12-34-56").equals("02:AB:CD:12:34:56"),"MAC normalization");
        check(Machine.normalize("02ABCD123456").equals("02:AB:CD:12:34:56"),"MAC compact input");
        Path fileWithSpaces=root.resolve("文件夹 with spaces/授权文件.prop");
        var explorer=FxUi.revealCommand(fileWithSpaces,"Windows 11","C:\\Windows");
        check(explorer.size()==3 && explorer.get(0).equals("C:\\Windows\\explorer.exe") && explorer.get(1).equals("/select,") && explorer.get(2).equals(fileWithSpaces.toAbsolutePath().normalize().toString()),"native Explorer selection keeps path as separate argument");
        check(FxUi.revealCommand(fileWithSpaces,"Linux","").equals(List.of("xdg-open",fileWithSpaces.toAbsolutePath().getParent().toString())),"Linux reveal opens containing directory");
        for(String invalid:List.of("", "000000000000","FFFFFFFFFFFF","01:00:00:00:00:00","02:AB-CD:12:34:56","02:AB:CD:12:34:56\nkey=bad"))rejects(()->Machine.normalize(invalid),"invalid MAC rejected");
        var adapters=Machine.adapters();check(!adapters.isEmpty(),"native adapter enumeration");
        var adapter=adapters.getFirst();Path request=root.resolve("machine-request.properties");Machine.saveRequest(request,adapter);
        check(Machine.readRequest(request).getProperty("mac").equals(adapter.mac()),"request roundtrip");
        rejects(()->Machine.saveRequest(request,adapter),"request overwrite refused");
        Path duplicate=root.resolve("duplicate.properties");Files.writeString(duplicate,"request.format=pmb-machine-v1\nmac=02:AB:CD:12:34:56\nmac=02:00:00:00:00:02\n");
        rejects(()->Machine.readRequest(duplicate),"duplicate request fields rejected");
        Path large=root.resolve("large.properties");Files.write(large,new byte[65537]);rejects(()->Machine.readRequest(large),"oversized request rejected");
        var material=Vault.fromBackendSource(source);char[] password="test-only-long-password-123456".toCharArray();Path vault=root.resolve("issuer.vault");Vault.create(vault,material,password);
        check(Vault.unlock(vault,password).equals(material),"encrypted vault roundtrip");
        Path autoload=root.resolve("autoload");Files.createDirectories(autoload);
        rejects(()->IssuerSettings.load(autoload),"missing root vault rejected");
        Files.copy(vault,autoload.resolve("issuer.vault"));
        rejects(()->IssuerSettings.load(autoload),"missing root password rejected");
        Path autoPassword=autoload.resolve("password.txt");Files.writeString(autoPassword,"wrong");
        rejects(()->IssuerSettings.load(autoload),"wrong root password rejected");
        Files.writeString(autoPassword,"\uFEFF"+new String(password)+"\r\n");
        check(IssuerSettings.load(autoload).equals(material),"root auto load accepts UTF8 BOM and CRLF");
        Files.writeString(autoPassword,"\n");rejects(()->IssuerSettings.load(autoload),"empty root password rejected");
        Files.writeString(autoPassword,"first\nsecond");rejects(()->IssuerSettings.load(autoload),"multiline root password rejected");
        Files.writeString(autoPassword,new String(password));
        String oldHome=System.getProperty("pmb.tool.home");
        try{System.setProperty("pmb.tool.home",autoload.toString());check(IssuerSettings.load().equals(material),"automatic root resolution ignores working directory");}
        finally{if(oldHome==null)System.clearProperty("pmb.tool.home");else System.setProperty("pmb.tool.home",oldHome);}
        check(!new String(Files.readAllBytes(vault),StandardCharsets.ISO_8859_1).contains(material.salt()),"vault has no plaintext application salt");
        rejects(()->Vault.unlock(vault,"wrong-password".toCharArray()),"wrong password rejected");
        rejects(()->Vault.create(vault,material,password),"vault overwrite refused");
        rejects(()->Vault.encrypt(material,"short".toCharArray()),"short password refused");
        byte[] tampered=Files.readAllBytes(vault);tampered[tampered.length-1]^=1;Path broken=root.resolve("tampered.vault");Files.write(broken,tampered);
        rejects(()->Vault.unlock(broken,password),"modified vault rejected");
        Path issued=root.resolve("issued.prop");var result=LicenseWriter.issue(adapter.mac(),issued,"Test fixture","test-runner",Vault.unlock(vault,password));
        check(result.audit()!=null && Files.exists(result.audit()),"issuance audit saved");
        var prop=new Properties();try(var in=Files.newInputStream(issued)){prop.load(in);}
        check(prop.size()==2 && prop.getProperty("key").matches("[A-Z0-9]{4}(-[A-Z0-9]{4}){3}"),"legacy properties format");
        byte[] original=Files.readAllBytes(issued);rejects(()->LicenseWriter.issue(adapter.mac(),issued,"Test","operator",material),"license overwrite refused");
        check(Arrays.equals(original,Files.readAllBytes(issued)),"existing license unchanged");
        rejects(()->LicenseWriter.issue(adapter.mac(),root.resolve("missing-customer.prop"),"","operator",material),"customer required");
        char[] next="changed-test-password-123456".toCharArray();Vault.changePassword(vault,password,next);
        rejects(()->Vault.unlock(vault,password),"old password rejected after change");
        check(Vault.unlock(vault,next).equals(material),"new password accepted");
        // Invoke the unchanged application's license verifier only. Never call main or SpringApplication.
        var app=Class.forName("com.zust.monitor.SmartStationMonitorApplication");var verify=app.getDeclaredMethod("verifyLicense");verify.setAccessible(true);
        Path live=Path.of("license.prop");if(Files.exists(live))throw new IllegalStateException("Use an empty isolated working directory for backend compatibility test");
        try{
            Files.write(live,original,StandardOpenOption.CREATE_NEW);check((Boolean)verify.invoke(null),"unchanged backend accepts generated local-MAC license");
            Files.writeString(live,"mac="+adapter.mac()+"\nkey=INVALID\n");check(!(Boolean)verify.invoke(null),"unchanged backend rejects altered license");
            String remote="02:00:00:00:00:01";Set<String> locals=new HashSet<>();for(var a:adapters)locals.add(a.mac());
            for(int i=2;locals.contains(remote);i++)remote=String.format("02:00:00:00:%02X:%02X",i/256,i%256);
            Files.writeString(live,"mac="+remote+"\nkey="+LicenseWriter.key(remote,material)+"\n");check(!(Boolean)verify.invoke(null),"unchanged backend rejects nonlocal MAC with valid key");
        }finally{Files.deleteIfExists(live);Arrays.fill(password,'\0');Arrays.fill(next,'\0');}
        Files.writeString(root.resolve("test-result.json"),"{\"passed\":"+passed+",\"failed\":0,\"backendVerifier\":\"unchanged\",\"applicationStarted\":false}\n");
        System.out.println("Tests passed: "+passed);
    }
}
