package pmb.license;

import java.net.NetworkInterface;
import java.nio.file.*;
import java.io.*;
import java.time.Instant;
import java.util.*;

public final class Machine {
    private Machine() {}
    public record Adapter(String name, String description, String mac, boolean up, boolean virtual) {}
    public static String normalize(String value) {
        String v=Objects.requireNonNull(value,"MAC is required").trim();
        if(!v.matches("(?i)([0-9a-f]{12}|([0-9a-f]{2}:){5}[0-9a-f]{2}|([0-9a-f]{2}-){5}[0-9a-f]{2})"))
            throw new IllegalArgumentException("MAC must contain six hexadecimal bytes / MAC 必须为 6 字节地址");
        String hex=v.replace(":", "").replace("-", "").toUpperCase(Locale.ROOT);
        if(hex.equals("000000000000") || (Integer.parseInt(hex.substring(0,2),16)&1)!=0)
            throw new IllegalArgumentException("Invalid zero or multicast MAC / 不接受全零或组播 MAC");
        return String.join(":",hex.split("(?<=\\G..)(?!$)"));
    }
    public static List<Adapter> adapters() throws IOException {
        var result=new ArrayList<Adapter>();
        var all=NetworkInterface.getNetworkInterfaces();
        if(all==null)return List.of();
        for(var ni:Collections.list(all)) {
            try {
                byte[] bytes=ni.getHardwareAddress();
                if(ni.isLoopback() || bytes==null || bytes.length!=6)continue;
                result.add(new Adapter(ni.getName(),ni.getDisplayName(),normalize(HexFormat.of().formatHex(bytes)),ni.isUp(),ni.isVirtual()));
            }catch(IllegalArgumentException ignored){}
        }
        result.sort(Comparator.comparing(Adapter::virtual).thenComparing(a->!a.up()).thenComparing(Adapter::name));
        return List.copyOf(result);
    }
    public static Properties request(Adapter a) {
        var p=new Properties();p.setProperty("request.format","pmb-machine-v1");
        p.setProperty("mac",a.mac());p.setProperty("interface.name",a.name());
        p.setProperty("interface.description",Objects.toString(a.description(),""));
        p.setProperty("interface.up",Boolean.toString(a.up()));p.setProperty("interface.virtual",Boolean.toString(a.virtual()));
        p.setProperty("os",System.getProperty("os.name"));p.setProperty("arch",System.getProperty("os.arch"));
        p.setProperty("createdAt",Instant.now().toString());return p;
    }
    public static Properties readRequest(Path path) throws IOException {
        if(Files.size(path)>65536)throw new IOException("Request file exceeds 64 KiB");
        Properties p=new Properties(){@Override public synchronized Object put(Object k,Object v){if(containsKey(k))throw new IllegalArgumentException("Duplicate request field");return super.put(k,v);}};
        try(var in=Files.newInputStream(path)){p.load(in);}
        if(!"pmb-machine-v1".equals(p.getProperty("request.format")))throw new IOException("Unsupported machine request format");
        var result=new Properties();result.putAll(p);result.setProperty("mac",normalize(p.getProperty("mac")));return result;
    }
    public static void saveRequest(Path path,Adapter adapter) throws IOException {
        var out=new ByteArrayOutputStream();request(adapter).store(out,"PowerMonitoring machine request; contains no license secret");
        Files.write(path,out.toByteArray(),StandardOpenOption.CREATE_NEW);
    }
    public static Path home(){
        String configured=System.getProperty("pmb.tool.home");
        if(configured!=null)return Path.of(configured).toAbsolutePath();
        try{return Path.of(Machine.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParent();}
        catch(Exception e){throw new IllegalStateException("Cannot locate application directory",e);}
    }
}
