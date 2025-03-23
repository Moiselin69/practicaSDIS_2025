package sdis.broker.client.unit;

import sdis.broker.common.MensajeProtocolo;
import sdis.broker.common.Primitiva;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;

public class ReadQ {
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
        try {
            mensajeProtocolo = (MensajeProtocolo) ois.readObject();
            oos.writeObject(new MensajeProtocolo(Primitiva.READQ, "Cola_1"));
            mensajeProtocolo = (MensajeProtocolo) ois.readObject();
            if (mensajeProtocolo.getPrimitiva() != Primitiva.NOTAUTH)
                System.out.println("ERROR, no se ha recibido NOTAUTH");
            oos.writeObject(new MensajeProtocolo(Primitiva.XAUTH, "admin", "$%&/()="));
            mensajeProtocolo = (MensajeProtocolo) ois.readObject();
            if (mensajeProtocolo.getPrimitiva() != Primitiva.XAUTH)
                System.out.println("ERROR, no se ha recibido XAUTH");
            oos.writeObject(new MensajeProtocolo(Primitiva.READQ, "Cola_1"));
            mensajeProtocolo = (MensajeProtocolo) ois.readObject();
            if (mensajeProtocolo.getPrimitiva() != Primitiva.MSG)
                System.out.println("ERROR, no se ha recibido MSG");
            oos.writeObject(new MensajeProtocolo(Primitiva.READQ, "Cola_3"));
            mensajeProtocolo = (MensajeProtocolo) ois.readObject();
            if (mensajeProtocolo.getPrimitiva() != Primitiva.EMPTY)
                System.out.println("ERROR, no se ha recibido EMPTY");
            oos.writeObject(new MensajeProtocolo(Primitiva.READQ, null));
            mensajeProtocolo = (MensajeProtocolo) ois.readObject();
            if (mensajeProtocolo.getPrimitiva() != Primitiva.BADCODE)
                System.out.println("ERROR, no se ha recibido BADCODE");
            System.out.println("Se ha ejcutado todos los casos, si no han surgido errores, significa que esta todo correcto");
            oos.writeObject(new MensajeProtocolo(Primitiva.EXIT));
        }catch (Exception e){
            System.out.println(e.getLocalizedMessage());
        }
    }
}
