package pmb.license;


import java.nio.file.*;
import java.util.*;


public final class Issuer {
    public static void main(String[] args){
        try{
            if(args.length==0 && FxUi.hasDisplay()){FxIssuer.launchApp(args);return;}
            if(args.length==0 || Arrays.equals(args,new String[]{"--help"})){help();return;}
            if(args.length%2!=0)throw new IllegalArgumentException("Options require values; use --help");
            var options=new HashMap<String,String>();
            var allowed=Set.of("--request","--mac","--output","--customer","--operator","--vault","--password-file","--init-source","--change-password");
            for(int i=0;i<args.length;i+=2){if(!allowed.contains(args[i]) || options.put(args[i],args[i+1])!=null)throw new IllegalArgumentException("Unknown or duplicate option: "+args[i]);}
            Path vault=Path.of(options.getOrDefault("--vault",IssuerSettings.root().resolve("issuer.vault").toString()));
            if(options.containsKey("--init-source")){
                if(options.keySet().stream().anyMatch(k->!Set.of("--init-source","--vault","--password-file").contains(k)))throw new IllegalArgumentException("Do not combine initialization with issuance");
                char[] password=password(options,"New unlock password (at least 12 characters): ");
                try{Vault.create(vault,Vault.fromBackendSource(Path.of(options.get("--init-source"))),password);System.out.println("Encrypted issuer vault created: "+vault.toAbsolutePath());}finally{Arrays.fill(password,'\0');}return;
            }
            if(options.containsKey("--change-password")){
                if(!"true".equals(options.get("--change-password")) || System.console()==null)throw new IllegalArgumentException("Password change requires --change-password true in an interactive terminal");
                char[] old=System.console().readPassword("Current password: "),next=System.console().readPassword("New password: "),again=System.console().readPassword("Repeat new password: ");
                try{if(!Arrays.equals(next,again))throw new IllegalArgumentException("Passwords do not match");Vault.changePassword(vault,old,next);System.out.println("Vault password changed; update root password.txt and other internal copies to match.");}finally{clear(old,next,again);}return;
            }
            if(options.containsKey("--request")==options.containsKey("--mac"))throw new IllegalArgumentException("Use exactly one of --request and --mac");
            String mac=options.containsKey("--request")?Machine.readRequest(Path.of(options.get("--request"))).getProperty("mac"):Machine.normalize(options.get("--mac"));
            Path output=Path.of(required(options,"--output"));String customer=required(options,"--customer"),operator=required(options,"--operator");
            char[] password=password(options,"Issuer unlock password: ");
            try{var result=LicenseWriter.issue(mac,output,customer,operator,Vault.unlock(vault,password));System.out.println("License saved: "+result.license());if(result.auditWarning()!=null)System.err.println(result.auditWarning());}
            finally{clear(password);}
        }catch(Exception e){System.err.println("Error: "+e.getMessage());System.exit(1);}
    }
    private static void help(){System.out.println("PowerMonitoring internal issuer (never distribute to customers)\nDesktop: no arguments\nIssue: --request machine-request.properties --output license.prop --customer NAME --operator NAME\n       or replace --request with --mac AA:BB:CC:DD:EE:FF\nOptional: --vault /secure/issuer.vault\nAutomatically loads password.txt and issuer.vault beside the executable/start.sh.\nAutomation only: --password-file /private/unlock-password.txt (protect OS permissions)\nChange: --change-password true [--vault PATH]\nProvision (company only): --init-source SmartStationMonitorApplication.java --vault issuer.vault");}
    private static String required(Map<String,String> options,String name){String v=options.get(name);if(v==null || v.isBlank())throw new IllegalArgumentException("Missing option "+name);return v;}
    private static char[] password(Map<String,String> options,String prompt)throws Exception {
        if(options.containsKey("--password-file"))return IssuerSettings.readPassword(Path.of(options.get("--password-file")));
        if(!options.containsKey("--init-source"))return IssuerSettings.readPassword(IssuerSettings.root().resolve("password.txt"));
        if(System.console()==null)throw new IllegalStateException("Use an interactive terminal or the desktop application to enter the password");
        char[] p=System.console().readPassword(prompt);if(p==null)throw new IllegalArgumentException("Password input cancelled");return p;
    }
    private static void clear(char[]... values){for(var v:values)if(v!=null)Arrays.fill(v,'\0');}
}
