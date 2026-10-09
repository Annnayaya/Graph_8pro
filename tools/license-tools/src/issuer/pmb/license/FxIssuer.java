package pmb.license;

import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.nio.file.Path;

public final class FxIssuer extends Application {
    private Path exported;
    private Vault.Material material;
    public static void launchApp(String[] args){launch(FxIssuer.class,args);}
    @Override public void start(Stage stage){
        var customer=new TextField();customer.setPromptText("填写客户或项目名称");
        var operator=new TextField(System.getProperty("user.name"));var mac=new TextField();mac.setPromptText("导入申请文件，或填写目标 MAC");
        var form=FxUi.form();FxUi.row(form,0,"客户 / 项目",customer);FxUi.row(form,1,"签发人",operator);FxUi.row(form,2,"目标 MAC",mac);
        var card=new VBox(form);card.getStyleClass().add("card");
        var status=FxUi.label("正在自动加载软件根目录的授权文件…","status");status.setMaxWidth(Double.MAX_VALUE);status.setId("issuer-status");
        var load=FxUi.button("导入申请文件",false);var reload=FxUi.button("重新加载",false);var reveal=FxUi.button("在文件夹中显示",false);reveal.setDisable(true);var issue=FxUi.button("生成授权文件",true);issue.setDisable(true);issue.setId("issue-license");
        var actions=new HBox(10,load,reload,reveal,issue);actions.setAlignment(Pos.CENTER_RIGHT);
        var page=FxUi.page(FxUi.header("内部授权生成器","导入客户申请，填写信息，即可生成授权文件。"),card,status,actions);
        Runnable initialize=()->{
            material=null;issue.setDisable(true);reload.setDisable(true);status.setText("正在自动加载授权文件…");
            var task=new Task<Vault.Material>(){protected Vault.Material call()throws Exception{return IssuerSettings.load();}};
            task.setOnSucceeded(e->{material=task.getValue();issue.setDisable(false);reload.setDisable(false);status.setText("授权文件已自动加载，可以生成许可证。");});
            task.setOnFailed(e->{reload.setDisable(false);Throwable error=task.getException();status.setText("加载失败："+error.getMessage()+"\n文件位置："+IssuerSettings.root());});
            var thread=new Thread(task,"issuer-auto-load");thread.setDaemon(true);thread.start();
        };
        reload.setOnAction(e->initialize.run());
        load.setOnAction(e->{try{var file=FxUi.open(stage,"导入客户机器申请","机器申请文件","*.properties");if(file!=null){var request=Machine.readRequest(file);mac.setText(request.getProperty("mac"));}}catch(Exception ex){FxUi.error(stage,ex);}});
        reveal.setOnAction(e->FxUi.reveal(stage,exported));
        issue.setOnAction(e->{try{
            if(material==null)throw new IllegalStateException("授权文件尚未加载，请检查根目录的 password.txt 和 issuer.vault。");
            String normalized=Machine.normalize(mac.getText()),c=customer.getText().trim(),o=operator.getText().trim();if(c.isEmpty()||o.isEmpty())throw new IllegalArgumentException("请填写客户和签发人。");
            Path output=FxUi.save(stage,"保存软件授权文件","license.prop","授权文件","*.prop");if(output==null)return;
            Vault.Material loaded=material;
            FxUi.background(page,()->LicenseWriter.issue(normalized,output,c,o,loaded),result->{exported=result.license();reveal.setDisable(false);status.setText("已生成："+exported);if(result.auditWarning()!=null)FxUi.info(stage,result.auditWarning());},stage);
        }catch(Exception ex){FxUi.error(stage,ex);}});
        FxUi.show(stage,"PowerMonitoring · 内部授权生成器",page,980,550);initialize.run();
    }
}
