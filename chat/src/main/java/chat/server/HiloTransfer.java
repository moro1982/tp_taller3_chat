package chat.server;

import java.net.Socket;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class HiloTransfer extends Thread {

    private final Socket cDataSocket;
    private final String archivoEnviar;
    private String carpetaDestino;
    private boolean esEnvio;

    // Constructor para recepcion de archivos
    public HiloTransfer( Socket cDataSocket, String carpetaDestino ) {
        this.cDataSocket = cDataSocket;
        this.carpetaDestino = carpetaDestino;
        this.archivoEnviar = null;
        this.esEnvio = false;
    }

    // Constructor para envio de archivos
    public HiloTransfer( Socket cDataSocket, String rutaOrigenArchivo, boolean esEnvio ) {
        this.cDataSocket = cDataSocket;
        this.archivoEnviar = rutaOrigenArchivo;
        this.esEnvio = esEnvio;
    }

    @Override
    public void run() {
        try {
            if (esEnvio) {
                this.enviarArchivo();
            } else {
                this.recibirArchivo();
            }
        } catch ( Exception e ) {
            System.err.println("Error al iniciar manejo de archivos: " + e.getMessage());
        }
    }

    private void enviarArchivo() {
        try {
            System.out.println("Iniciando envío del archivo...");

            DataOutputStream dos = new DataOutputStream(this.cDataSocket.getOutputStream());
            FileInputStream fis = new FileInputStream(this.archivoEnviar);
            File localFile = new File(this.archivoEnviar);
            
            // Enviar nombre del archivo
            dos.writeUTF(this.archivoEnviar);
            
            // Obtener y enviar tamaño del archivo
            long fileSize = localFile.length();
            dos.writeLong(fileSize);

            // Enviar datos del archivo
            int bytes;
            byte[] buffer = new byte[8 * 1024];
            while ((bytes = fis.read(buffer)) != -1) {
                dos.write(buffer, 0, bytes);
            }

            dos.flush();
            fis.close();
            System.out.println("Archivo enviado: " + this.archivoEnviar);

        } catch (Exception e) {
            System.err.println("Error durante el envio del archivo desde el Servidor: " + e.getMessage());
        }
    }

    private void recibirArchivo() {
        try {
            System.out.println("Iniciando recepcion del archivo...");
            DataInputStream dis = new DataInputStream(this.cDataSocket.getInputStream());

            // Recibimos el nombre del archivo y creamos el FileOutputStream
            String rutaOrigenArchivoRecibido = dis.readUTF();
            System.out.println(rutaOrigenArchivoRecibido);
            String nombreRecibido = rutaOrigenArchivoRecibido.substring( rutaOrigenArchivoRecibido.lastIndexOf("/") + 1, rutaOrigenArchivoRecibido.lastIndexOf("."));
            System.out.println(nombreRecibido);
            String extension = rutaOrigenArchivoRecibido.substring(rutaOrigenArchivoRecibido.lastIndexOf("."));
            System.out.println(extension);
            String nombreCopia = this.carpetaDestino + nombreRecibido + "_copia" + extension;
            System.out.println("Ruta local del archivo recibido: " + nombreCopia);
            FileOutputStream fos = new FileOutputStream(nombreCopia);
            
            // Recibimos tamaño del archivo
            long fileSize = dis.readLong();
            
            // Recibimos datos del archivo 
            int bytes;
            byte[] buffer = new byte[8 * 1024];
            while (fileSize > 0 && (bytes = dis.read(buffer, 0, (int) Math.min(buffer.length, fileSize))) != -1) {
                fos.write(buffer, 0, bytes);
                fileSize -= bytes;
            }

            // fos.flush(); /// --> Si hay close, no hace falta el flush
            fos.close(); /// --> Hacerlo dentro de un finally{}
            
            System.out.println("Archivo recibido: " + nombreCopia);
            
        } catch (Exception e) {
            System.err.println("Error en la recepcion del archivo: " + e.getMessage());
        }
    }
}
