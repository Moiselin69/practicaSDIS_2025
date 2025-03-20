package sdis.broker.server;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import sdis.broker.server.Sirviente;
import sdis.utils.MultiMap;

import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.concurrent.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Servidor implements Runnable {
    private int numHilos; // se utilizará para saber cuantos hilos maximos lanzamos en el pool
    private int puertoAbrir; // se utilizará para saber que puerto abrir
    private ConcurrentHashMap<Integer, Sirviente> mapaSirvientes = new ConcurrentHashMap<Integer, Sirviente>(); // sirve para depuración de hilos lanzados
    private ConcurrentHashMap<String, String> usuariosHashMap; // se utilizará para verificar usuarios con la base de datos
    private ConcurrentHashMap<String, ContadorAddRead> mapaMensajesAddRead;
    private MultiMap multiMap;
    private ObjectMapper mapper = new ObjectMapper(); // se utilizará para verificar usuarios con la base de datos
    private int idSirviente = 0; // se utilizrá para saber que sirviente ha fallado
    private ServerSocket socketServidor;
    private Socket socketParaCliente;
    BlackListManager listaIps = new BlackListManager(3);
    BlackListManager listaLogginsIncorrectos = new BlackListManager(2);
    private ObjectOutputStream oos;

    public Servidor(int numHilos, int puertoAbrir) throws IOException {
        this.numHilos = numHilos;
        this.puertoAbrir = puertoAbrir;
        socketServidor = new ServerSocket(this.puertoAbrir);
        try{
            usuariosHashMap = mapper.readValue(new File("src/sdis/broker/server/usuariosContras.json"),
                                                new TypeReference<ConcurrentHashMap<String, String>>() {});
            ConcurrentHashMap<String, ConcurrentLinkedQueue<String>> temp = mapper.readValue(new File("src/sdis/broker/server/colasMensajeria.json"),
                                                new TypeReference<ConcurrentHashMap<String, ConcurrentLinkedQueue<String>>>(){});
            mapaMensajesAddRead = mapper.readValue(new File("src/sdis/broker/server/depuracionColasMensajeria.json"),
                    new TypeReference<ConcurrentHashMap<String, ContadorAddRead>>() {} );
            multiMap = new MultiMap(temp);
            /*
            A la funcion readValue hay que pasarle como argumentos, la direccion absoluta o relativa
            del fichero que vamos a leer, y como segundo argumento hay que pasarlo el tipo exacto de
            HashMap que vamos a utilizar en este caso <String, String>, donde el primer String es el
            nombre del usuario y el segundo la contraseña
             */
        }catch (Exception e){
            System.out.println("Ha surgido un problema en el servidor padre al leer el archivo .json"
                                +e.getLocalizedMessage());
        }
    }
    public void run(){ // en las siguientes lineas se lanzará el Pool de hilos
        ExecutorService executor = Executors.newFixedThreadPool(numHilos);
        while (true){
            try {
                System.out.println("----Server Waiting For Client----");
                socketParaCliente = socketServidor.accept(); // aceptamos el socket que nos llega siempre
                Sirviente sirvienteX = new Sirviente(socketParaCliente, idSirviente, usuariosHashMap, listaIps, listaLogginsIncorrectos, numHilos, mapaSirvientes, mapaMensajesAddRead, multiMap, (ThreadPoolExecutor) executor); // crear el nuevo hilo sirviente
                mapaSirvientes.put(idSirviente, sirvienteX); // guardamos el sirviente por mera depuración
                executor.submit(sirvienteX); // lanzamos el hilo
                idSirviente++;

            } catch (Exception e) {
                System.err.println("ERROR SERVIDOR PADRE: "+e.toString());
            }
        }
    }
}
