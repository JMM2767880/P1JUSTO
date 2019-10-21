package com.example.p1aadjustomotamarquez;

public class Contacto {

    protected String nombre;
    protected long id;
    protected String numero;

    public Contacto() {
    }

    public Contacto(String nombre, long id, String numero) {
        this.nombre = nombre;
        this.id = id;
        this.numero = numero;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    @Override
    public String toString() {
        return "Contacto{" +
                "nombre='" + nombre + '\'' +
                ", id=" + id +
                ", numero='" + numero + '\'' +
                '}';
    }

    public String toCSV(){
        return id + "," + nombre + "," + numero;
    }
}
