package pmb.license;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.*;
import java.time.Instant;
import java.util.*;
import java.io.*;

public final class LicenseWriter {
    private LicenseWriter(){}
    static String key(String mac,Vault.Material material)throws GeneralSecurityException {
        byte[] hash=MessageDigest.getInstance("SHA-256").digest((Machine.normalize(mac).replace(":","")+material.salt()).getBytes(StandardCharsets.UTF_8));
        var value=new StringBuilder();
        for(int i=0;i<16;i++){value.append(material.alphabet().charAt(((hash[i]&255)+(hash[i+1]&255))%material.alphabet().length()));if(i%4==3 && i!=15)value.append('-');}
        Arrays.fill(hash,(byte)0);return value.toString();
    }
    public record Result(Path license,Path audit,String auditWarning){}
    public static Result issue(String input,Path output,String customer,String operator,Vault.Material material)throws Exception {
        String mac=Machine.normalize(input);
        if(customer==null || customer.isBlank() || operator==null || operator.isBlank())throw new IllegalArgumentException("Customer and operator are required / 必须填写客户与签发人");
        if(customer.length()>200 || operator.length()>100 || customer.contains("\n") || operator.contains("\n"))throw new IllegalArgumentException("Invalid customer/operator value");
        byte[] bytes=("# PowerMonitoring MAC-bound license; compatible with current backend\nmac="+mac+"\nkey="+key(mac,material)+"\n").getBytes(StandardCharsets.US_ASCII);
        Files.write(output,bytes,StandardOpenOption.CREATE_NEW);
        Path audit=output.resolveSibling(output.getFileName()+".audit.properties");String warning=null;
        var record=new Properties();record.setProperty("issuedAt",Instant.now().toString());record.setProperty("mac",mac);record.setProperty("customer",customer);record.setProperty("operator",operator);
        record.setProperty("format","legacy-mac-v1");record.setProperty("licenseSha256",HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes)));
        try(var stream=Files.newOutputStream(audit,StandardOpenOption.CREATE_NEW)){record.store(stream,"Internal issuance record; retain in company");}
        catch(IOException e){warning="License saved but audit was not saved: "+e.getMessage();audit=null;}
        return new Result(output.toAbsolutePath(),audit,warning);
    }
}
