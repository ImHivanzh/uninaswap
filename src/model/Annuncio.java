package model;

import model.enums.Categoria;
import model.enums.TipoAnnuncio;
import java.util.List;
import java.util.ArrayList;

public class Annuncio {
    private int idAnnuncio;
    private int idUtente; // Usiamo l'ID per collegarlo all'utente senza caricare tutto l'oggetto
    private String titolo;
    private String descrizione;
    private Categoria categoria;
    private TipoAnnuncio tipoAnnuncio;
    private boolean stato; // true = attivo, false = concluso/eliminato

    public Annuncio() {
    }

    public Annuncio(int idUtente, String titolo, String descrizione, Categoria categoria, TipoAnnuncio tipoAnnuncio) {
        this.idUtente = idUtente;
        this.titolo = titolo;
        this.descrizione = descrizione;
        this.categoria = categoria;
        this.tipoAnnuncio = tipoAnnuncio;
        this.stato = true; // Attivo di default
    }

    // --- GETTERS & SETTERS ---

    public int getIdAnnuncio() {
        return idAnnuncio;
    }

    public void setIdAnnuncio(int idAnnuncio) {
        this.idAnnuncio = idAnnuncio;
    }

    public int getIdUtente() {
        return idUtente;
    }

    public void setIdUtente(int idUtente) {
        this.idUtente = idUtente;
    }

    public String getTitolo() {
        return titolo;
    }

    public void setTitolo(String titolo) {
        this.titolo = titolo;
    }

    public String getDescrizione() {
        return descrizione;
    }

    public void setDescrizione(String descrizione) {
        this.descrizione = descrizione;
        this.categoria = categoria;
        this.utenteID = utenteID;
        this.tipoAnnuncio = tipoAnnuncio;
        this.immagini = new ArrayList<>();
    }

    // Getters e Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitolo() { return titolo; }
    public void setTitolo(String titolo) { this.titolo = titolo; }

    public String getDescrizione() { return descrizione; }
    public void setDescrizione(String descrizione) { this.descrizione = descrizione; }

    public Categoria getCategoria() { return categoria; }
    public void setCategoria(Categoria categoria) { this.categoria = categoria; }

    public int getUtenteID() { return utenteID; }
    public void setUtenteID(int utenteID) { this.utenteID = utenteID; }
    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    public TipoAnnuncio getTipoAnnuncio() {
        return tipoAnnuncio;
    }

    public TipoAnnuncio getTipoAnnuncio() { return tipoAnnuncio; }
    public void setTipoAnnuncio(TipoAnnuncio tipoAnnuncio) { this.tipoAnnuncio = tipoAnnuncio; }

    public List<Immagini> getImmagini() { return immagini; }
    public void setImmagini(List<Immagini> immagini) { this.immagini = immagini; }

    public void addImmagine(Immagini immagine) {
        if (this.immagini == null) this.immagini = new ArrayList<>();
        this.immagini.add(immagine);
    public boolean isStato() {
        return stato;
    }

    public void setStato(boolean stato) {
        this.stato = stato;
    }

    @Override
    public String toString() {
        return "Annuncio{" +
                "idAnnuncio=" + idAnnuncio +
                ", idUtente=" + idUtente +
                ", titolo='" + titolo + '\'' +
                ", categoria=" + categoria +
                ", tipo=" + tipoAnnuncio +
                '}';
    }
}