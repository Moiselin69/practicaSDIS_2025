package sdis.utils;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class MultiMap {
    private ConcurrentHashMap<String, ConcurrentLinkedQueue<String>> multiMapa = new ConcurrentHashMap<>();

    public MultiMap(){}
    public MultiMap(ConcurrentHashMap<String, ConcurrentLinkedQueue<String>> multiMapa){
        this.multiMapa = multiMapa;
    }

    /**
     * Metodo para introducir un mensaje a una cola de mensajes, si la cola no existe la crea.
     * @param clave nombre de la cola de mensajes
     * @param mensaje el mensaje que quieres añadir a la cola
     */
    public void push(String clave, String mensaje){
        if (!multiMapa.containsKey(clave))
            multiMapa.put(clave, new ConcurrentLinkedQueue<String>());
        multiMapa.get(clave).add(mensaje);
    }

    /**
     * Metodo para saber el útlimo mensaje de una cola de mensajes, si la cola no existe devuelve null
     * @param clave  Nombre de la cola que deseas extraer el ultimo mensaje
     * @return Devuelve el ultimo mensaje de la cola eliminandolo de la cola de mensajes
     */
    public String pull(String clave){
        if (!multiMapa.containsKey(clave))
                return null;
        else if (multiMapa.containsKey(clave) && multiMapa.get(clave).isEmpty())
            return null;
        else return multiMapa.get(clave).remove();
    }

    /**
     * Metodo para saber si existe una cola de mensajes
     * @param clave Como parametro pasamos un string que sea el identificador de la cola
     * @return Devuelve true si la cola existe, false si no existe.
     */
    public boolean contains(String clave){
        return multiMapa.containsKey(clave);
    }

    /**
     * Metodo para borrar una cola de mensajes
     * @param clave Como parametro pasamos un string que sea el identificador de la cola
     */
    public void delCola(String clave){
        multiMapa.remove(clave);
    }
}
