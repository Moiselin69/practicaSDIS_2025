package Main;

import sdis.broker.server.ContadorAddRead;
import sdis.broker.server.Servidor;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

public class Main {
    public static void main(String[] args){
        ConcurrentHashMap<String, String> usuariosHashMap = new ConcurrentHashMap<>();
        ObjectMapper mapper = new ObjectMapper();
        usuariosHashMap.put("Manoli", "1234");
        usuariosHashMap.put("Paqui", "4567");
        usuariosHashMap.put("Luisma", "1234");
        usuariosHashMap.put("Barajas", "4567");
        usuariosHashMap.put("cllamas", "qwerty");
        usuariosHashMap.put("hector", "lkjlkj");
        usuariosHashMap.put("sdis", "987123");
        usuariosHashMap.put("admin", "$%&/()=");
        ConcurrentHashMap<String, ConcurrentLinkedDeque<String>> colasMensajes = new ConcurrentHashMap<>();
        colasMensajes.put("Cola_1", new ConcurrentLinkedDeque<String>());
        colasMensajes.get("Cola_1").add("Hola");
        colasMensajes.get("Cola_1").add("Que tal estamos");
        colasMensajes.get("Cola_1").add("Mi gente");
        colasMensajes.put("Cola_2", new ConcurrentLinkedDeque<String>());
        colasMensajes.get("Cola_2").add("Adios");
        colasMensajes.get("Cola_2").add("Que hayais pasado un buen dia");
        ConcurrentHashMap<String, ContadorAddRead> mapaMensajesAddRead = new ConcurrentHashMap<>();
        mapaMensajesAddRead.put("Cola_1", new ContadorAddRead());
        mapaMensajesAddRead.get("Cola_1").setContadorAdd(3);
        mapaMensajesAddRead.put("Cola_2", new ContadorAddRead());
        mapaMensajesAddRead.get("Cola_2").setContadorAdd(3);
        try{
            mapper.writeValue(new File("src/sdis/broker/server/usuariosContras.json"), usuariosHashMap);
            mapper.writeValue(new File("src/sdis/broker/server/colasMensajeria.json"), colasMensajes);
            mapper.writeValue(new File("src/sdis/broker/server/depuracionColasMensajeria.json"), mapaMensajesAddRead);
            Servidor servidorPadre = new Servidor(5, 2000);
            servidorPadre.run();
        }
        catch (Exception e){
            System.out.println(("Ha surgido un error en el main"+e.toString()));
        }
    }
}
