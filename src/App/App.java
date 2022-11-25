package App;

import java.io.IOException;
import java.net.Socket;

import App.Login.LoginSceneController;
import App.Register.RegisterSceneController;
import App.Search.SearchSceneController;
import Client.Client;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class App extends Application {
    public static Stage LoginStage;
    public static Stage AppStage;
    public static Stage RegisterStage;
    public static Stage SearchStage;
    private static FXMLLoader loginLoader;
    private static FXMLLoader appLoader;
    private static FXMLLoader registerLoader;
    private static FXMLLoader searchLoader;

    @Override
    public void start(Stage primaryStage) {
        LoginStage = primaryStage;
        Parent root;
        try {
            loginLoader = new FXMLLoader(getClass().getResource("./Login/LoginScene.fxml"));
            root = loginLoader.load();
            Scene scene = new Scene(root);
            primaryStage.setScene(scene);
            primaryStage.setTitle("登录");
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        System.exit(0);
    }

    public static void enterApp(String token, Client client) {
        Stage stage = new Stage();
        AppStage = stage;
        Parent root;
        System.out.println("进入应用");
        try {
            appLoader = new FXMLLoader(App.class.getResource("AppScene.fxml"));
            Object loadBuffer = appLoader.load();
            root = (Parent)loadBuffer;
            AppSceneController controller = (AppSceneController)appLoader.getController();
            controller.setClient(client);
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.initStyle(StageStyle.UNDECORATED);
            stage.setTitle("聊天室");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void enterRegister() {
        Stage stage = new Stage();
        RegisterStage = stage;
        Parent root;
        try {
            registerLoader = new FXMLLoader(App.class.getResource("./Register/RegisterScene.fxml"));
            Object loadBuffer = registerLoader.load();
            root = (Parent)loadBuffer;
            // RegisterSceneController controller = (RegisterSceneController)registerLoader.getController();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("注册");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void enterSearch(Client client) {
        Stage stage = new Stage();
        SearchStage = stage;
        Parent root;
        try {
            searchLoader = new FXMLLoader(App.class.getResource("./Search/SearchScene.fxml"));
            Object loadBuffer = searchLoader.load();
            root = (Parent)loadBuffer;
            SearchSceneController controller = (SearchSceneController)searchLoader.getController();
            controller.setClient(client);
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("寻找好友/群聊");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static LoginSceneController getLoginsSceneController() {
        return (LoginSceneController)loginLoader.getController();
    }

    public static AppSceneController getAppSceneController() {
        return (AppSceneController)appLoader.getController();
    }

    public static RegisterSceneController getRegisterSceneController() {
        return (RegisterSceneController)registerLoader.getController();
    }
    
    public static SearchSceneController getSearchSceneController() {
        return (SearchSceneController)searchLoader.getController();
    }

    public static Stage getLoginStage() {
        return LoginStage;
    }

    public static Stage getAppStage() {
        return AppStage;
    }

    public static Stage getRegisterStage() {
        return RegisterStage;
    }

    public static Stage getSearchStage() {
        return SearchStage;
    }

    public static void main(String[] args) {
        launch();
    }
}
