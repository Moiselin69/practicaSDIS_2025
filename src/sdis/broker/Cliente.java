package sdis.broker;

import sdis.broker.common.MensajeProtocolo;
import sdis.broker.common.Primitiva;

import java.io.IOException;
import java.net.Socket;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.util.Scanner;

public class Cliente {
    private static final int PUERTO = 2000;
    private static ObjectInputStream ois;
    private static ObjectOutputStream oos;
    private static MensajeProtocolo mensajeProtocolo;

    public static void main(String[] args) throws IOException {
            String mensajeServidor, mensajeEnviar, mensajeEnviarNombre, mensajeEnviarContra;
            Socket socket = new Socket("localhost", PUERTO);
            Scanner scanner = new Scanner(System.in);
            oos = new ObjectOutputStream(socket.getOutputStream());
            ois = new ObjectInputStream(socket.getInputStream());
            try{
                mensajeProtocolo = (MensajeProtocolo) ois.readObject();
                System.out.println(mensajeProtocolo.getMensaje());
                if (mensajeProtocolo.getMensaje().equals("Err Max Number of connections reached.") ||
                        mensajeProtocolo.getMensaje().equals("Err Max Number of login attempts reached.")){
                    oos.close();
                    ois.close();
                    socket.close();
                    return;
                }
                while (true){
                    System.out.println("¿Que desea realizar usted ahora?");
                    mensajeEnviar = scanner.nextLine();
                    if (mensajeEnviar.equals("LOGGIN")){
                        System.out.println("Escriba su nombre usuario: ");
                        mensajeEnviarNombre = scanner.nextLine();
                        System.out.println("Escriba su contraseña de usuario: ");
                        mensajeEnviarContra = scanner.nextLine();
                        mensajeProtocolo = new MensajeProtocolo(Primitiva.XAUTH, mensajeEnviarNombre, mensajeEnviarContra);
                        oos.writeObject(mensajeProtocolo);
                        mensajeProtocolo = (MensajeProtocolo) ois.readObject();
                        if (mensajeProtocolo.getPrimitiva() == Primitiva.XAUTH)
                            System.out.println(mensajeProtocolo.getMensaje());
                        else if (mensajeProtocolo.getPrimitiva() == Primitiva.ERROR) {
                            System.out.println(mensajeProtocolo.getMensaje());
                        } else{
                            mensajeProtocolo = new MensajeProtocolo(Primitiva.BADCODE);
                            oos.writeObject(mensajeProtocolo);
                        }
                    }else if (mensajeEnviar.equals("ADDMSG")){
                        System.out.println("Escriba el nombre de la cola a la que quieras enviar un mensaje: ");
                        mensajeEnviarNombre = scanner.nextLine();
                        System.out.println("Escriba el mensaje que quieras enviar: ");
                        mensajeEnviarContra = scanner.nextLine();
                        mensajeProtocolo = new MensajeProtocolo(Primitiva.ADDMSG, mensajeEnviarNombre, mensajeEnviarContra);
                        oos.writeObject(mensajeProtocolo);
                        mensajeProtocolo = (MensajeProtocolo) ois.readObject();
                        if(mensajeProtocolo.getPrimitiva() == Primitiva.ADDED){
                            System.out.println("Mensaje Enviado Correctamente");
                        }else if (mensajeProtocolo.getPrimitiva() == Primitiva.NOTAUTH) {
                            System.out.println(mensajeProtocolo.getMensaje());
                        }else{
                            mensajeProtocolo = new MensajeProtocolo(Primitiva.BADCODE);
                            oos.writeObject(mensajeProtocolo);
                        }
                    } else if (mensajeEnviar.equals("READMSG")) {
                        System.out.println("Escriba el nombre de la cola de la que quieras recibir un mensaje: ");
                        mensajeEnviarNombre = scanner.nextLine();
                        mensajeProtocolo = new MensajeProtocolo(Primitiva.READQ, mensajeEnviarNombre);
                        oos.writeObject(mensajeProtocolo);
                        mensajeProtocolo = (MensajeProtocolo) ois.readObject();
                        if (mensajeProtocolo.getPrimitiva() == Primitiva.MSG){
                            System.out.println("El mensaje recibido es: ");
                            System.out.println(mensajeProtocolo.getIdCola());
                        } else if (mensajeProtocolo.getPrimitiva() == Primitiva.NOTAUTH) {
                            System.out.println(mensajeProtocolo.getMensaje());
                        } else if (mensajeProtocolo.getPrimitiva() == Primitiva.EMPTY) {
                            System.out.println("La cola esta vacia o no existe");
                        }else{
                            mensajeProtocolo = new MensajeProtocolo(Primitiva.BADCODE);
                            oos.writeObject(mensajeProtocolo);
                        }
                    } else if (mensajeEnviar.equals("EXIT")) {
                        mensajeProtocolo = new MensajeProtocolo(Primitiva.EXIT);
                        oos.writeObject(mensajeProtocolo);
                        return;
                    } else if (mensajeEnviar.equals("STATE")) {
                        System.out.println("Cola que deseas ver: ");
                        mensajeEnviarNombre = scanner.nextLine();
                        mensajeProtocolo = new MensajeProtocolo(Primitiva.STATE, mensajeEnviarNombre);
                        oos.writeObject(mensajeProtocolo);
                        mensajeProtocolo = (MensajeProtocolo) ois.readObject();
                        if (mensajeProtocolo.getPrimitiva() == Primitiva.INFO)
                            System.out.println(mensajeProtocolo.getMensaje());
                        else if (mensajeProtocolo.getPrimitiva() == Primitiva.NOTAUTH) {
                            System.out.println(mensajeProtocolo.getMensaje());
                        } else{
                            mensajeProtocolo = new MensajeProtocolo(Primitiva.BADCODE);
                            oos.writeObject(mensajeProtocolo);
                        }
                    } else if (mensajeEnviar.equals("DELETE")) {
                        System.out.println("Cola que deseas borrar: ");
                        mensajeEnviarNombre = scanner.nextLine();
                        mensajeProtocolo = new MensajeProtocolo(Primitiva.DELETEQ, mensajeEnviarNombre);
                        oos.writeObject(mensajeProtocolo);
                        mensajeProtocolo = (MensajeProtocolo) ois.readObject();
                        if (mensajeProtocolo.getPrimitiva() == Primitiva.DELETED)
                            System.out.println("Cola borrada con exito");
                        else if (mensajeProtocolo.getPrimitiva() == Primitiva.EMPTY)
                            System.out.println("La cola no existe");
                        else if (mensajeProtocolo.getPrimitiva() == Primitiva.NOTAUTH)
                            System.out.println(mensajeProtocolo.getMensaje());
                         else{
                            mensajeProtocolo = new MensajeProtocolo(Primitiva.BADCODE);
                            oos.writeObject(mensajeProtocolo);
                        }
                    }else{
                        System.out.println("No se ha entendido el comando, vuelva a escribir el comando");
                    }
                }
            }catch (Exception e){
                System.out.println(e.getLocalizedMessage());
            }

    }
}
