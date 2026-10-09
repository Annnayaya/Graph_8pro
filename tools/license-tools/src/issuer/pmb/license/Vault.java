package pmb.license;

import javax.crypto.*;
import javax.crypto.spec.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.*;
import java.util.*;
import java.util.regex.Pattern;
import java.io.IOException;

/** The shipped executable has no application salt: it must decrypt the separately provisioned vault. */
public final class Vault {
    private static final byte[] MAGIC="PMBVAULT1".getBytes(StandardCharsets.US_ASCII);
    private static final int ITERATIONS=600_000;
    private Vault(){}
    public record Material(String salt,String alphabet) {
        public Material {if(salt.isBlank() || salt.length()>512 || !alphabet.matches("[A-Z0-9]{16,64}") || salt.indexOf('\n')>=0)throw new IllegalArgumentException("Invalid issuer material");}
        @Override public String toString(){return "Material[REDACTED]";}
    }
    public static Material fromBackendSource(Path path)throws IOException {
        String source=Files.readString(path);
        return new Material(constant(source,"SALT"),constant(source,"CHAR_MAP"));
    }
    private static String constant(String source,String name){
        var match=Pattern.compile("private\\s+static\\s+final\\s+String\\s+"+name+"\\s*=\\s*\"([^\"\\\\]+)\"\\s*;").matcher(source);
        if(!match.find())throw new IllegalArgumentException("Backend authorization constant was not found: "+name);
        return match.group(1);
    }
    public static byte[] encrypt(Material material,char[] password)throws GeneralSecurityException {
        if(password==null || password.length<12)throw new IllegalArgumentException("Use an unlock password of at least 12 characters / 解锁密码至少 12 位");
        byte[] salt=new byte[16],iv=new byte[12];var random=new SecureRandom();random.nextBytes(salt);random.nextBytes(iv);
        var header=ByteBuffer.allocate(MAGIC.length+4+salt.length+iv.length).put(MAGIC).putInt(ITERATIONS).put(salt).put(iv).array();
        byte[] plain=(material.salt()+"\n"+material.alphabet()).getBytes(StandardCharsets.UTF_8);
        try {
            var cipher=Cipher.getInstance("AES/GCM/NoPadding");cipher.init(Cipher.ENCRYPT_MODE,key(password,salt,ITERATIONS),new GCMParameterSpec(128,iv));cipher.updateAAD(header);
            byte[] encrypted=cipher.doFinal(plain);return ByteBuffer.allocate(header.length+encrypted.length).put(header).put(encrypted).array();
        }finally{Arrays.fill(plain,(byte)0);}
    }
    public static Material unlock(Path path,char[] password)throws IOException,GeneralSecurityException {
        if(Files.size(path)>8192)throw new IOException("Invalid vault size");
        byte[] bytes=Files.readAllBytes(path);int headerLength=MAGIC.length+4+16+12;
        if(bytes.length<headerLength+16)throw new IOException("Invalid vault format");
        var in=ByteBuffer.wrap(bytes);byte[] magic=new byte[MAGIC.length];in.get(magic);
        if(!Arrays.equals(magic,MAGIC))throw new IOException("Unsupported vault format");
        int iterations=in.getInt();if(iterations!=ITERATIONS)throw new IOException("Unsupported vault parameters");
        byte[] salt=new byte[16],iv=new byte[12];in.get(salt);in.get(iv);
        var cipher=Cipher.getInstance("AES/GCM/NoPadding");cipher.init(Cipher.DECRYPT_MODE,key(password,salt,iterations),new GCMParameterSpec(128,iv));cipher.updateAAD(Arrays.copyOf(bytes,headerLength));
        byte[] plain;
        try{plain=cipher.doFinal(Arrays.copyOfRange(bytes,headerLength,bytes.length));}
        catch(AEADBadTagException invalid){throw new GeneralSecurityException("Incorrect password or damaged vault / 密码错误或授权材料损坏");}
        try{String[] fields=new String(plain,StandardCharsets.UTF_8).split("\n",-1);if(fields.length!=2)throw new IOException("Invalid vault material");return new Material(fields[0],fields[1]);}
        finally{Arrays.fill(plain,(byte)0);}
    }
    private static SecretKey key(char[] password,byte[] salt,int iterations)throws GeneralSecurityException {
        var spec=new PBEKeySpec(password,salt,iterations,256);
        try{byte[] raw=SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();try{return new SecretKeySpec(raw,"AES");}finally{Arrays.fill(raw,(byte)0);}}
        finally{spec.clearPassword();}
    }
    public static void create(Path path,Material material,char[] password)throws IOException,GeneralSecurityException {
        Files.write(path,encrypt(material,password),StandardOpenOption.CREATE_NEW);ownerOnly(path);
    }
    public static void changePassword(Path path,char[] oldPassword,char[] newPassword)throws IOException,GeneralSecurityException {
        byte[] encrypted=encrypt(unlock(path,oldPassword),newPassword);
        Path tmp=Files.createTempFile(path.toAbsolutePath().getParent(),"vault-change-",".tmp");
        try{ownerOnly(tmp);Files.write(tmp,encrypted);Files.move(tmp,path,StandardCopyOption.ATOMIC_MOVE,StandardCopyOption.REPLACE_EXISTING);}
        finally{Files.deleteIfExists(tmp);Arrays.fill(encrypted,(byte)0);}
    }
    static void ownerOnly(Path p)throws IOException {
        var posix=Files.getFileAttributeView(p,java.nio.file.attribute.PosixFileAttributeView.class);
        if(posix!=null)Files.setPosixFilePermissions(p,java.nio.file.attribute.PosixFilePermissions.fromString("rw-------"));
    }
}
