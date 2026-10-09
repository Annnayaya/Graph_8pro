package pmb.license;



import java.nio.file.*;
import java.util.*;



public final class Collector {
    public static void main(String[] args){
        try {
            if(args.length==0 && FxUi.hasDisplay()){FxCollector.launchApp(args);return;}
            var adapters=Machine.adapters();
            if(args.length==0 || Arrays.equals(args,new String[]{"--list"})){
                for(var a:adapters)System.out.printf("%s\t%s\tup=%s\tvirtual=%s\t%s%n",a.mac(),a.name(),a.up(),a.virtual(),a.description());
                if(adapters.isEmpty())throw new IllegalStateException("No usable MAC found");return;
            }
            if(args.length==4 && args[0].equals("--mac") && args[2].equals("--output")){
                String mac=Machine.normalize(args[1]);var adapter=adapters.stream().filter(a->a.mac().equals(mac)).findFirst().orElseThrow(()->new IllegalArgumentException("Selected MAC does not belong to this running environment"));
                Machine.saveRequest(Path.of(args[3]),adapter);System.out.println("Request saved: "+Path.of(args[3]).toAbsolutePath());return;
            }
            System.out.println("Usage: --list | --mac AA:BB:CC:DD:EE:FF --output machine-request.properties\nNo arguments: desktop GUI; headless Linux: list adapters.");
            if(!Arrays.equals(args,new String[]{"--help"}))System.exit(2);
        }catch(Exception e){System.err.println("Error: "+e.getMessage());System.exit(1);}
    }
}
