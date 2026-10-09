package pmb.license;

import javafx.application.Application;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.geometry.Pos;
import javafx.stage.Stage;
import java.nio.file.Path;
import java.util.function.Function;

public final class FxCollector extends Application {
    private Path exported;
    public static void launchApp(String[] args){launch(FxCollector.class,args);}
    @Override public void start(Stage stage){
        var table=new TableView<Machine.Adapter>();table.setPlaceholder(new Label("未找到可用网卡，请检查网络设备后刷新"));
        table.getColumns().add(column("MAC 地址",Machine.Adapter::mac,180));table.getColumns().add(column("接口",Machine.Adapter::name,130));
        table.getColumns().add(column("网卡说明",Machine.Adapter::description,330));table.getColumns().add(column("状态",a->a.up()?"已连接":"未连接",90));
        table.getColumns().add(column("虚拟子接口",a->a.virtual()?"是":"否",100));table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        var status=FxUi.label("选择长期使用的网卡，导出申请文件后发送给本司授权人员。","status");status.setMaxWidth(Double.MAX_VALUE);
        var refresh=FxUi.button("刷新网卡",false);var copy=FxUi.button("复制 MAC",false);var reveal=FxUi.button("在文件夹中显示",false);var save=FxUi.button("导出申请文件",true);reveal.setDisable(true);
        Runnable reload=()->{try{table.getItems().setAll(Machine.adapters());if(!table.getItems().isEmpty())table.getSelectionModel().selectFirst();}catch(Exception e){FxUi.error(stage,e);}};
        refresh.setOnAction(e->reload.run());copy.setOnAction(e->{var a=table.getSelectionModel().getSelectedItem();if(a!=null){var content=new ClipboardContent();content.putString(a.mac());Clipboard.getSystemClipboard().setContent(content);status.setText("已复制 MAC："+a.mac());}});
        reveal.setOnAction(e->FxUi.reveal(stage,exported));
        save.setOnAction(e->{try{
            var selected=table.getSelectionModel().getSelectedItem();if(selected==null)throw new IllegalArgumentException("请先选择网卡。");
            var path=FxUi.save(stage,"导出机器申请文件","machine-request.properties","机器申请文件","*.properties");
            if(path==null)return;Machine.saveRequest(path,selected);exported=path;reveal.setDisable(false);status.setText("已导出："+path.toAbsolutePath());
        }catch(Exception ex){FxUi.error(stage,ex);}});
        var actions=new HBox(10,refresh,copy,reveal,save);actions.setAlignment(Pos.CENTER_RIGHT);
        var page=FxUi.page(FxUi.header("机器信息采集","仅采集本机 MAC 与申请所需信息 · 不修改网络 · 不联网"),table,status,actions);VBox.setVgrow(table,Priority.ALWAYS);
        FxUi.show(stage,"PowerMonitoring · 机器信息采集",page,1020,610);reload.run();
    }
    private static TableColumn<Machine.Adapter,String> column(String title,Function<Machine.Adapter,String> value,double width){var c=new TableColumn<Machine.Adapter,String>(title);c.setCellValueFactory(d->new ReadOnlyStringWrapper(value.apply(d.getValue())));c.setPrefWidth(width);return c;}
}
