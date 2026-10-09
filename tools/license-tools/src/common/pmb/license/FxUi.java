package pmb.license;

import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

public final class FxUi {
    private FxUi(){}
    public static boolean hasDisplay(){return System.getProperty("os.name").toLowerCase(Locale.ROOT).startsWith("windows") || System.getenv("DISPLAY")!=null || System.getenv("WAYLAND_DISPLAY")!=null;}
    public static Button button(String text,boolean primary){var b=new Button(text);b.getStyleClass().add(primary?"primary":"secondary");return b;}
    public static Label label(String text,String style){var l=new Label(text);l.getStyleClass().add(style);l.setWrapText(true);return l;}
    public static VBox header(String title,String description){return new VBox(8,label("POWERMONITORING","brand"),label(title,"heading"),label(description,"muted"));}
    public static void show(Stage stage,String title,Region body,double width,double height){
        var scene=new Scene(body,width,height);scene.getStylesheets().add(Objects.requireNonNull(FxUi.class.getResource("tools.css")).toExternalForm());
        stage.setTitle(title);stage.setScene(scene);stage.setMinWidth(850);stage.setMinHeight(550);
        if(!Boolean.getBoolean("pmb.ui.preview"))stage.show();
    }
    public static FileChooser chooser(String title,String filename,String description,String pattern){
        var chooser=new FileChooser();chooser.setTitle(title);
        chooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter(description,pattern),new FileChooser.ExtensionFilter("所有文件","*.*"));
        if(filename!=null)chooser.setInitialFileName(filename);return chooser;
    }
    public static Path open(Window owner,String title,String description,String pattern){var file=chooser(title,null,description,pattern).showOpenDialog(owner);return file==null?null:file.toPath();}
    public static Path save(Window owner,String title,String filename,String description,String pattern){var file=chooser(title,filename,description,pattern).showSaveDialog(owner);return file==null?null:file.toPath();}
    public static List<String> revealCommand(Path file,String os,String windowsRoot){
        Path absolute=file.toAbsolutePath().normalize();
        if(os.toLowerCase(Locale.ROOT).startsWith("windows"))return List.of(Path.of(windowsRoot,"explorer.exe").toString(),"/select,",absolute.toString());
        return List.of("xdg-open",absolute.getParent().toString());
    }
    public static void reveal(Window owner,Path file){try{
        if(file==null || !Files.isRegularFile(file))throw new IllegalArgumentException("文件不存在，请先完成导出。");
        new ProcessBuilder(revealCommand(file,System.getProperty("os.name"),System.getenv().getOrDefault("SystemRoot","C:\\Windows"))).start();
    }catch(Exception e){error(owner,e);}}
    public static void error(Window owner,Throwable failure){Throwable e=failure;while(e.getCause()!=null)e=e.getCause();var alert=new Alert(Alert.AlertType.ERROR);alert.initOwner(owner);alert.setTitle("操作未完成");alert.setHeaderText("请检查输入后重试");alert.setContentText(Objects.toString(e.getMessage(),e.getClass().getSimpleName()));alert.showAndWait();}
    public static void info(Window owner,String text){var alert=new Alert(Alert.AlertType.INFORMATION);alert.initOwner(owner);alert.setTitle("操作完成");alert.setHeaderText(null);alert.setContentText(text);alert.showAndWait();}
    public static boolean confirm(Window owner,String text){var alert=new Alert(Alert.AlertType.CONFIRMATION,text,ButtonType.OK,ButtonType.CANCEL);alert.initOwner(owner);alert.setTitle("确认签发");alert.setHeaderText("请核对本次授权信息");return alert.showAndWait().orElse(ButtonType.CANCEL)==ButtonType.OK;}
    public static <T> void background(Region controls,Callable<T> work,Consumer<T> done,Window owner){
        controls.setDisable(true);var task=new Task<T>(){protected T call()throws Exception{return work.call();}};
        task.setOnSucceeded(e->{controls.setDisable(false);done.accept(task.getValue());});task.setOnFailed(e->{controls.setDisable(false);error(owner,task.getException());});
        // Finish an in-flight file operation even if the window is closed.
        var thread=new Thread(task,"pmb-tool-work");thread.setDaemon(false);thread.start();
    }
    public static GridPane form(){var grid=new GridPane();grid.setHgap(16);grid.setVgap(16);var label=new ColumnConstraints(105);var field=new ColumnConstraints();field.setHgrow(Priority.ALWAYS);grid.getColumnConstraints().addAll(label,field);return grid;}
    public static void row(GridPane form,int row,String title,javafx.scene.Node field){form.add(new Label(title),0,row);form.add(field,1,row);}
    public static VBox page(javafx.scene.Node... children){var page=new VBox(22,children);page.setPadding(new Insets(28));return page;}
}
