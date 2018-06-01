package gva.asa.forms.managers.util;

/**
 * 
 * @author Paco Arnau
 */
    public interface ServerListener
{
    public void cambioEstadoProducido (String tipo); // Sirve para informar de algún evento producido en el hilo
    public void ficheroEditado (String text);
}
