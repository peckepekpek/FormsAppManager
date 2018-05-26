/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gva.asa.forms.managers;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.stage.Stage;
import javax.swing.JMenu;
import javax.swing.JMenuBar;

/**
 *
 * @author farnauvi
 */
public class Principal extends Application{
    public static int DEFAULT_WIDTH = 1300;
    public final static int DEFAULT_HEIGHT = 900;
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        new Thread(new Splash()).start();
        try {
            Thread.sleep(3000);
            launch(args);
        } catch (InterruptedException ex) {
            Logger.getLogger(Principal.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
//        Toolkit kit = Toolkit.getDefaultToolkit();
//        Dimension tamanoPantalla = kit.getScreenSize();
//        int alturaPantalla = tamanoPantalla.height;
//        int anchuraPantalla = tamanoPantalla.width;
        JMenuBar mb = new JMenuBar();
        JMenu menu1 = new JMenu("File");
        mb.add(menu1);
        JMenu menu2 = new JMenu("Help");
        mb.add(menu2);
        FormsManagerFrame panel = new FormsManagerFrame();
        panel.setJMenuBar(mb);
//        panel.setIconImage(kit.getImage(getClass().getResource(java.util.ResourceBundle.getBundle("gva/asa/forms/resources/Bundle").getString("icoGVA"))));
//        panel.setLocation((anchuraPantalla-DEFAULT_WIDTH)/2,(alturaPantalla-DEFAULT_HEIGHT)/2);
        panel.setVisible(true);
       
    }
    
}
