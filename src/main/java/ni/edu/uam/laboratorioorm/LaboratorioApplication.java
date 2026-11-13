package ni.edu.uam.laboratorioorm;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import ni.edu.uam.laboratorioorm.database.JPAUtil;

import java.io.IOException;

public class LaboratorioApplication extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(LaboratorioApplication.class.getResource("laboratorio-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("Control de Equipos de Laboratorio - Hibernate ORM");
        stage.setScene(scene);
        stage.show();

        stage.setMinWidth(stage.getWidth());
        stage.setMinHeight(stage.getHeight());
    }

    @Override
    public void stop() {
        JPAUtil.cerrar();
    }
}