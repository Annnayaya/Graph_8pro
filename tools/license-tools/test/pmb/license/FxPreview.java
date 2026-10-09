package pmb.license;
import javafx.application.Platform;
import javafx.scene.image.WritableImage;
import javafx.stage.Stage;
import java.nio.file.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.util.concurrent.*;

/** Snapshot only the tools' own hidden JavaFX scenes, never the user's desktop. */
public final class FxPreview {
    public static void main(String[] args)throws Exception{
        System.setProperty("pmb.ui.preview","true");Path root=Path.of(args[0]);Files.createDirectories(root);
        var finished=new CompletableFuture<Void>();
        Platform.startup(()->{try{
            Platform.setImplicitExit(false);
            var stages=new java.util.LinkedHashMap<String,Stage>();
            for(var app:java.util.List.of(new FxCollector(),new FxIssuer())){
                var stage=new Stage();app.start(stage);if(stage.isShowing())throw new AssertionError("Preview must remain hidden");
                stages.put(app.getClass().getSimpleName(),stage);
            }
            var timer=new javafx.animation.Timeline();final int[] tries={0};
            timer.getKeyFrames().add(new javafx.animation.KeyFrame(javafx.util.Duration.millis(100),event->{try{
                var issuer=stages.get("FxIssuer");var button=(javafx.scene.control.Button)issuer.getScene().lookup("#issue-license");
                if(button.isDisabled()){if(++tries[0]>200)throw new AssertionError("GUI automatic load did not enable issuance");return;}
                if(!issuer.getScene().getRoot().lookupAll(".password-field").isEmpty())throw new AssertionError("Simplified UI must not prompt for a password");
                for(var entry:stages.entrySet()){
                var stage=entry.getValue();var scene=stage.getScene();scene.getRoot().applyCss();scene.getRoot().layout();WritableImage fx=scene.snapshot(null);
                var image=new BufferedImage((int)fx.getWidth(),(int)fx.getHeight(),BufferedImage.TYPE_INT_ARGB);
                for(int y=0;y<image.getHeight();y++)for(int x=0;x<image.getWidth();x++)image.setRGB(x,y,fx.getPixelReader().getArgb(x,y));
                ImageIO.write(image,"png",root.resolve(entry.getKey()+".png").toFile());stage.close();
                }
                timer.stop();finished.complete(null);
            }catch(Throwable e){timer.stop();finished.completeExceptionally(e);}}));
            timer.setCycleCount(javafx.animation.Animation.INDEFINITE);timer.play();
        }catch(Throwable e){finished.completeExceptionally(e);}});
        try{finished.get(45,TimeUnit.SECONDS);System.out.println("Hidden JavaFX scenes rendered successfully.");}finally{Platform.exit();}
    }
}
