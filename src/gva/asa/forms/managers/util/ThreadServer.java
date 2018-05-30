/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gva.asa.forms.managers.util;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import gva.asa.forms.ssh.SSHConnector;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
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
    public static final String RUTA_WEBUTIL = "/srv_apl/forms11g/middleware/forms_reports/config/FormsComponent/forms/server/";
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
    public void creaAmbito (String text) {
        ERROR=0;
        subeFichero(text);
        cambiaPropietarioForms();
        grabaFicheroForms();
        borraTemporalUser();
        CompruebaLibrerias();
        if (ERROR==0) {
            escribeLog("Server:"+HOST+"->"+FILE+" Guardado OK"+ENTER_KEY);
            informaEstado("Server:"+HOST+"->"+FILE+" Guardado OK"+ENTER_KEY);
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
        System.out.println(message);
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
    
    public void informaCambioFile(String texto)
	{
		int listenerSize = ServerListenerList.size();
		for (int n=0; n<listenerSize; n++) {
			ServerListener listener = (ServerListener)ServerListenerList.get(n);
                        listener.ficheroEditado(texto);
		}
	}
    
    public void abreFichero () {      
        CLASSPATH_ENV="";
        preCopiaFichero();
        cambiaPropietarioDownload();
        informaEstado("Abriendo fichero:"+FILE+ENTER_KEY);
        cogeFichero("/tmp/"+FILE);
        borraTemporal();
        informaEstado("Fin de proceso. Fichero abierto:"+FILE+ENTER_KEY);
    }
    
     public void preCopiaFichero () {
        String remoteFile=RUTA_FILE+FILE;
        String command="cp "+remoteFile+ " /tmp/"+FILE;
        runAsUsername(command, USERNAME_FORMS11); 
    }
    
    private void borraTemporal() {
        String command="rm -rf /tmp/"+FILE;
        runAsUsername(command,USERNAME_FORMS11);
    }
     
     public void cambiaPropietarioDownload() {
        String command="chmod 777 /tmp/"+FILE;
        runAsUsername(command,USERNAME_FORMS11);
    }
     
     public void cambiaPropietarioForms() {
        String command="sudo chown forms11g:forms11g /tmp/"+FILE;
        runAsUsername(command,USERNAME);
    }
    
    public void grabaFicheroForms() {
        String remoteFile=RUTA_FILE+FILE;
        String command="cp /tmp/"+FILE+" "+remoteFile;
        runAsUsername(command,USERNAME_FORMS11);
    }
    
    public void borraTemporalUser() {
        String command="rm -rf /tmp/"+FILE;
        runAsUsername(command,USERNAME);
    }
    
    // Contribution by Jorge Peña (The Bash Crack)
    public void CompruebaLibrerias () {
        String command="for class in `echo \""+CLASSPATH_ENV+"\" | sed -e \"s/:/ /g\"`; do [ -f $class ] || echo \"$class no existe\" ; done";
        runAsUsername(command,USERNAME_FORMS11);
    }
    
    public void guardar(String text) {
        ERROR=0;
        informaEstado("Server:"+HOST+" Haciendo copia de fichero:"+FILE+ENTER_KEY);
        backupFicheroForms();
        informaEstado("Server:"+HOST+" Subiendo fichero con cambios:"+FILE+ENTER_KEY);
        subeFichero(text);
        informaEstado("Server:"+HOST+" Aplicando permisos Forms:"+FILE+ENTER_KEY);
        cambiaPropietarioForms();
        informaEstado("Server:"+HOST+" Grabando fichero en destino:"+FILE+ENTER_KEY);
        grabaFicheroForms();
        informaEstado("Comprobando ficheros del ClassPath. Los siguientes ficheros no están:"+ENTER_KEY);
        borraTemporalUser();
        CompruebaLibrerias();
        if (ERROR==0) {
            escribeLog("Server:"+HOST+" Guardado OK"+ENTER_KEY);
            informaEstado("Server:"+HOST+" Guardado OK"+ENTER_KEY);
        }
    
    }
    
     public void backupFicheroForms () {
        String remoteFile=RUTA_FILE+FILE;
        String command="cp "+remoteFile+ " "+RUTA_AMBITOS+"backup/";
        runAsUsername(command,USERNAME_FORMS11); 
    }
    
     public void subeFichero(String text) {             
         try
            {
                InputStream in= new ByteArrayInputStream(text.getBytes());
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                String line;
                while ((line = reader.readLine()) != null) 
                {
                    if (line.startsWith("CLASSPATH=")) {CLASSPATH_ENV=line.substring(10);}
                }       
                JSch jsch = new JSch();
                Session session = jsch.getSession(USERNAME, HOST, PORT);
                session.setPassword(PASSWORD);
                session.setConfig("StrictHostKeyChecking", "no");
                System.out.println("Establishing Connection...");
                session.connect();
                System.out.println("Connection established with USER."+USERNAME);
                System.out.println("Creating SFTP Channel.");
                ChannelSftp sftpChannel = (ChannelSftp) session.openChannel("sftp");
                System.out.println("Ususario sftp:"+session.getUserName());
                sftpChannel.connect();
                System.out.println("SFTP Channel created.");
                in= new ByteArrayInputStream(text.getBytes());
                sftpChannel.cd("/tmp");
                sftpChannel.put(in,FILE);
                sftpChannel.chmod(509, "/tmp/"+FILE);
                sftpChannel.disconnect();
                session.disconnect();  
            }
            catch(JSchException | SftpException | IOException ex)
            {
                System.out.println(ex);
                escribeLog(ex.getMessage()+":"+ex);
                ERROR++;
            }
    
    }
    
     public void runAsUsername (String command, String useras) {
        try
            {
                JSch jsch = new JSch();
                Session session = jsch.getSession(USERNAME, HOST, PORT);
                session.setPassword(PASSWORD);
                session.setConfig("StrictHostKeyChecking", "no");
                informaEstado("Establishing Connection...");
                session.connect();
                informaEstado("Connection established with USER."+USERNAME);
                // Abrimos un canal SSH. Es como abrir una consola.
                ChannelExec channelExec = (ChannelExec) session.
                    openChannel("exec");
                InputStream in = channelExec.getInputStream();
                OutputStream out=channelExec.getOutputStream();
                // Ejecutamos el comando para ser forms11g.
                ((ChannelExec) channelExec).setPty(true);
                ((ChannelExec)channelExec).setCommand("sudo su - "+useras+" -c '"+command+"'");
                channelExec.connect();
                out.write((PASSWORD+"\n").getBytes());
                out.flush();
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                StringBuilder builder = new StringBuilder();
                String linea;
                while ((linea = reader.readLine()) != null) {
                    builder.append(linea);
                    builder.append(ENTER_KEY);
                    informaEstado(linea+ENTER_KEY);
                }
                // Cerramos el canal SSH.
            channelExec.disconnect();
            System.out.println("Connection closed with USER."+USERNAME);
         }
        catch(JSchException | IOException ex)
        {
            System.out.println(ex);
            escribeLog(ex.getMessage()+":"+ex);
            ERROR++;
        }
        
    }
     
     public void cogeFichero(String remoteFile) {
         try
            {
                JSch jsch = new JSch();
                Session session = jsch.getSession(USERNAME, HOST, PORT);
                session.setPassword(PASSWORD);
                session.setConfig("StrictHostKeyChecking", "no");
                System.out.println("Establishing Connection...");
                session.connect();
                System.out.println("Connection established.");
                System.out.println("Creating SFTP Channel.");
                ChannelSftp sftpChannel = (ChannelSftp) session.openChannel("sftp");
                sftpChannel.connect();
                System.out.println("SFTP Channel created.");
                InputStream out= null;
                out= sftpChannel.get(remoteFile);
                BufferedReader br = new BufferedReader(new InputStreamReader(out));
                String line;
                while ((line = br.readLine()) != null) 
                {
                    informaCambioFile(line+ENTER_KEY);
                }
                br.close();
                sftpChannel.disconnect();
                session.disconnect();
            }
            catch(JSchException | SftpException | IOException e)
            {
                System.out.println(e);
                escribeLog(e.getMessage());   
                ERROR++;
            }
    }

    @Override
    public void ficheroEditado(String text) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
