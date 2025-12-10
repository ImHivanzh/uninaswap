package dao;

import db.dbConnection;
import exception.DatabaseException;
import model.*;
import model.enums.Categoria;
import model.enums.TipoAnnuncio;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AnnuncioDAO {

  private Connection con;

  public AnnuncioDAO() {
    try {
      // Tentativo di connessione gestendo l'eccezione personalizzata DatabaseException
      this.con = dbConnection.getInstance().getConnection();
    } catch (DatabaseException e) {
      System.err.println("Errore di connessione al database: " + e.getMessage());
      // In caso di errore la connessione resta null
    }
  }

  /**
   * Inserisce un nuovo annuncio nel database.
   * Gestisce i campi specifici (prezzo, oggetto_richiesto) tramite controlli instanceof.
   * * @throws DatabaseException se si verifica un errore SQL o di connessione.
   */
  public boolean pubblicaAnnuncio(Annuncio annuncio) throws DatabaseException {
    if (con == null) {
      throw new DatabaseException("Connessione al database non disponibile.");
    }
   * Pubblica un nuovo annuncio nel database.
   * Gestisce diversamente gli annunci di tipo VENDITA salvando il prezzo.
   * Imposta lo stato dell'annuncio a 'true' (attivo) di default.
   *
   * @param annuncio L'oggetto Annuncio da salvare.
   * @return true se l'inserimento va a buon fine, false altrimenti.
   * @throws DatabaseException in caso di errore SQL.
   */
  public boolean pubblicaAnnuncio(Annuncio annuncio) throws DatabaseException {
    String sql = "INSERT INTO annuncio(titolo, descrizione, prezzo, tipoannuncio, idutente, stato, categoria) VALUES (?, ?, ?, ?, ?, ?, ?)";

    // Query generica che include tutti i possibili campi della tabella
    String sql = "INSERT INTO annuncio (titolo, descrizione, categoria, utente_id, tipo_annuncio, prezzo, oggetto_richiesto) VALUES (?, ?, ?, ?, ?, ?, ?)";

    try (PreparedStatement ps = con.prepareStatement(sql)) {
      ps.setString(1, annuncio.getTitolo());
      ps.setString(2, annuncio.getDescrizione());
      ps.setString(3, annuncio.getCategoria().toString());
      ps.setInt(4, annuncio.getUtenteID());
      ps.setString(5, annuncio.getTipoAnnuncio().toString());

      // Gestione PREZZO (Solo per Vendita)
      if (annuncio instanceof Vendita) {
        ps.setDouble(6, ((Vendita) annuncio).getPrezzo());
      } else {
        ps.setNull(6, java.sql.Types.DOUBLE);
      }

      // Gestione OGGETTO RICHIESTO (Solo per Scambio)
      if (annuncio instanceof Scambio) {
        ps.setString(7, ((Scambio) annuncio).getOggettoRichiesto());
      // Gestione del prezzo solo se è una Vendita
      if (annuncio.getTipoAnnuncio() == TipoAnnuncio.VENDITA) {
        // Controllo se l'oggetto è effettivamente istanza di Vendita per recuperare il prezzo
        if (annuncio instanceof Vendita) {
          ps.setDouble(3, ((Vendita) annuncio).getPrezzo());
        } else {
          ps.setNull(3, Types.NUMERIC);
        }
      } else {
        ps.setNull(7, java.sql.Types.VARCHAR);
      }

      int rows = ps.executeUpdate();
      return rows > 0;

    } catch (SQLException e) {
      // Lancia l'eccezione personalizzata invece di stampare e ritornare false
      throw new DatabaseException("Errore durante l'inserimento dell'annuncio: " + e.getMessage());
    }
  }

  /**
   * Recupera tutti gli annunci dal database.
   * Ricostruisce l'oggetto corretto (Vendita, Scambio o Regalo) in base alla colonna 'tipo_annuncio'.
   */
  public List<Annuncio> findAll() {
    List<Annuncio> annunci = new ArrayList<>();
    if (con == null) return annunci;

    String sql = "SELECT * FROM annuncio";

    try (Statement stmt = con.createStatement();
         ResultSet rs = stmt.executeQuery(sql)) {

      while (rs.next()) {
        Annuncio a = mapResultSetToAnnuncio(rs);
        if (a != null) {
          annunci.add(a);
        }
      }
    } catch (SQLException e) {
      System.err.println("Errore durante il recupero degli annunci: " + e.getMessage());
      e.printStackTrace();
    }
    return annunci;
  }

  /**
   * Recupera un singolo annuncio per ID.
   */
  public Annuncio findById(int id) {
    if (con == null) return null;

    String sql = "SELECT * FROM annuncio WHERE id = ?";
    try (PreparedStatement ps = con.prepareStatement(sql)) {
      ps.setInt(1, id);
      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          return mapResultSetToAnnuncio(rs);
        }
      }
      // Nota: nel DB usiamo la stringa dell'enum (es. "vendita", "scambio")
      ps.setString(4, annuncio.getTipoAnnuncio().name());

      // Uso getIdUtente()
      ps.setInt(5, annuncio.getIdUtente());

      // Stato attivo di default
      ps.setBoolean(6, true);

      // Categoria
      ps.setString(7, annuncio.getCategoria().name());

      int rowsAffected = ps.executeUpdate();
      return rowsAffected > 0;

    } catch (SQLException e) {
      System.err.println("Errore durante la ricerca dell'annuncio per ID: " + e.getMessage());
      e.printStackTrace();
    }
    return null;
  }

  /**
   * Metodo helper privato per mappare una riga del ResultSet nell'oggetto Java corretto.
   */
  private Annuncio mapResultSetToAnnuncio(ResultSet rs) throws SQLException {
    int id = rs.getInt("id");
    String titolo = rs.getString("titolo");
    String descrizione = rs.getString("descrizione");

    // Gestione Enum Categoria con fallback
    Categoria categoria;
    try {
      categoria = Categoria.valueOf(rs.getString("categoria").toUpperCase());
    } catch (IllegalArgumentException | NullPointerException e) {
      categoria = Categoria.ALTRO;
    }

    int utenteId = rs.getInt("utente_id");
    String tipoString = rs.getString("tipo_annuncio");
    TipoAnnuncio tipo;
    try {
      tipo = TipoAnnuncio.valueOf(tipoString.toUpperCase());
    } catch (IllegalArgumentException | NullPointerException e) {
      tipo = TipoAnnuncio.VENDITA; // Default fallback
    }

    // Creazione dell'istanza specifica in base al tipo
    switch (tipo) {
      case VENDITA:
        double prezzo = rs.getDouble("prezzo");
        return new Vendita(id, titolo, descrizione, categoria, utenteId, prezzo);

      case SCAMBIO:
        String oggettoRichiesto = rs.getString("oggetto_richiesto");
        return new Scambio(id, titolo, descrizione, categoria, utenteId, oggettoRichiesto);

      case REGALO:
        return new Regalo(id, titolo, descrizione, categoria, utenteId);

      default:
        // Fallback di sicurezza
        return new Vendita(id, titolo, descrizione, categoria, utenteId, 0.0);
    }
  }

  /**
   * Recupera tutti gli annunci pubblicati da uno specifico utente.
   * Fondamentale per la scheda Profilo.
   *
   * @param idUtente L'ID dell'utente.
   * @return Lista di annunci.
   * @throws DatabaseException in caso di errore SQL.
   */
  public List<Annuncio> findAllByUtente(int idUtente) throws DatabaseException {
    String sql = "SELECT * FROM annuncio WHERE idutente = ?";
    List<Annuncio> annunci = new ArrayList<>();

    try (Connection conn = dbConnection.getInstance().getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {

      ps.setInt(1, idUtente);

      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          annunci.add(mapResultSetToAnnuncio(rs));
        }
      }
    } catch (SQLException e) {
      throw new DatabaseException("Errore nel recupero annunci utente", e);
    }
    return annunci;
  }

  /**
   * Recupera un singolo annuncio tramite il suo ID.
   *
   * @param idAnnuncio L'ID univoco dell'annuncio.
   * @return L'oggetto Annuncio trovato, o null se non esiste.
   * @throws DatabaseException in caso di errore SQL.
   */
  public Annuncio findById(int idAnnuncio) throws DatabaseException {
    String sql = "SELECT * FROM annuncio WHERE idannuncio = ?";

    try (Connection conn = dbConnection.getInstance().getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {

      ps.setInt(1, idAnnuncio);

      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          return mapResultSetToAnnuncio(rs);
        }
      }
    } catch (SQLException e) {
      throw new DatabaseException("Errore nel recupero dell'annuncio", e);
    }
    return null;
  }

  /**
   * Elimina un annuncio dal database.
   *
   * @param idAnnuncio L'ID dell'annuncio da eliminare.
   * @return true se l'eliminazione ha avuto successo.
   * @throws DatabaseException in caso di errore SQL.
   */
  public boolean deleteAnnuncio(int idAnnuncio) throws DatabaseException {
    String sql = "DELETE FROM annuncio WHERE idannuncio = ?";

    try (Connection conn = dbConnection.getInstance().getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {

      ps.setInt(1, idAnnuncio);

      int rowsAffected = ps.executeUpdate();
      return rowsAffected > 0;

    } catch (SQLException e) {
      throw new DatabaseException("Errore durante l'eliminazione dell'annuncio", e);
    }
  }

  /**
   * Metodo helper privato per mappare una riga del ResultSet in un oggetto Annuncio.
   * Gestisce il polimorfismo per la classe Vendita.
   */
  private Annuncio mapResultSetToAnnuncio(ResultSet rs) throws SQLException {
    Annuncio a;
    String tipoStr = rs.getString("tipoannuncio");
    TipoAnnuncio tipo;

    try {
      tipo = TipoAnnuncio.valueOf(tipoStr.toUpperCase());
    } catch (IllegalArgumentException | NullPointerException e) {
      tipo = TipoAnnuncio.VENDITA; // Default fallback
    }

    // Se è una vendita, istanzio la classe specifica e setto il prezzo
    if (tipo == TipoAnnuncio.VENDITA) {
      Vendita v = new Vendita();
      v.setPrezzo(rs.getDouble("prezzo"));
      a = v;
    } else {
      a = new Annuncio();
    }

    // Popolo i campi comuni
    a.setIdAnnuncio(rs.getInt("idannuncio"));
    a.setIdUtente(rs.getInt("idutente"));
    a.setTitolo(rs.getString("titolo"));
    a.setDescrizione(rs.getString("descrizione"));
    a.setTipoAnnuncio(tipo);
    a.setStato(rs.getBoolean("stato"));

    try {
      String cat = rs.getString("categoria");
      if (cat != null) a.setCategoria(Categoria.valueOf(cat.toUpperCase()));
    } catch (IllegalArgumentException e) {
      // Gestione errore categoria non valida
    }

    return a;
  }
}