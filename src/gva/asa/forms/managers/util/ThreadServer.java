/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gva.asa.forms.managers.util;

import com.jcraft.jsch.JSchException;
import gva.asa.forms.ssh.SSHConnector;
import java.io.IOException;
import java.util.ArrayList;

/**
 *
 * @author farnauvi
 */
public class ThreadServer implements ServerListener {
    
    public static String USERNAME = "";
    public static final String USERNAME_FORMS11 = "forms11g";
    public static String HOST = "";
    public static final int PORT = 22;
    public static String PASSWORD = "";
    public String FILE = ""; 
    public static final String RUTA_AMBITOS = "/aplicaciones/forms11g/conf/";
    public static final String RUTA_FORMSWEB = "/srv_apl/forms11g/middleware/user_projects/domains/dominioForms/config/fmwconfig/servers/WLS_FORMS/applications/formsapp_11.1.2/config/";
    public static final String RUTA_TNSNAMES = "/srv_apl/forms11g/middleware/forms_reports/config/";
    public String RUTA_FILE = "";
    public ArrayList<String> FilesEncontrados = new ArrayList<String>();
    public static final String ENTER_KEY = "\n";
    public String CLASSPATH_ENV="";
    public int ERROR=0;
    private ListenerList ServerListenerList = new ListenerList();
   
    
    @Override
    public void cambioEstadoProducido(String tipo) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    public void consultaAmbitos(String host,String user, String pass) {
        try {
            informaEstado("Interrogando a "+host);
            HOST = host;
            USERNAME = user;
            PASSWORD = pass;
            SSHConnector sshConnector = new SSHConnector();
            sshConnector.connect(USERNAME, PASSWORD, HOST, PORT);
            String result = sshConnector.executeCommand("ls "+RUTA_AMBITOS+"*.env");
            sshConnector.disconnect();
            filesEncontrados(result);
        } catch (JSchException | IllegalAccessException | IOException ex) {
            escribeLog(ex.getMessage());
            System.out.println(ex.getMessage());
        }
    
    }
    
     private void filesEncontrados (String sTexto) {
            FilesEncontrados = new ArrayList<>();
	    // Ruta conocida
	    String sTextoBuscado = RUTA_AMBITOS;

	    while (sTexto.indexOf(sTextoBuscado) > -1) {
	      sTexto = sTexto.substring(sTexto.indexOf(sTextoBuscado)+sTextoBuscado.length(),sTexto.length());
              String fichero = sTexto.substring(0,sTexto.indexOf("\n\r"));
              FilesEncontrados.add(fichero);
            }    
    }

    private void escribeLog(String message) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    public void addServerListener(ServerListener listener)
       {
               ServerListenerList.add(listener);
       }
    
    public void informaEstado(String texto)
	{
		int listenerSize = ServerListenerList.size();
		for (int n=0; n<listenerSize; n++) {
			ServerListener listener = (ServerListener)ServerListenerList.get(n);
                        listener.cambioEstadoProducido(texto);
		}
	}
}
