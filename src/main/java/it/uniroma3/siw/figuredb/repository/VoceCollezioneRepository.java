package it.uniroma3.siw.figuredb.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import it.uniroma3.siw.figuredb.model.StatoVoce;
import it.uniroma3.siw.figuredb.model.VoceCollezione;

public interface VoceCollezioneRepository extends CrudRepository<VoceCollezione, Long> {

    boolean existsByUtenteIdAndFigureId(Long utenteId, Long figureId);

    Optional<VoceCollezione> findByUtenteIdAndFigureId(Long utenteId, Long figureId);

    /** Collezione dell'utente con figure, azienda e serie in una sola query. */
    @Query("select v from VoceCollezione v "
         + "join fetch v.figure f "
         + "join fetch f.azienda "
         + "join fetch f.serie "
         + "where v.utente.id = :utenteId "
         + "order by v.dataAggiunta desc")
    List<VoceCollezione> findByUtenteIdConFigure(@Param("utenteId") Long utenteId);

    @Query("select v from VoceCollezione v "
         + "join fetch v.figure f "
         + "join fetch f.azienda "
         + "join fetch f.serie "
         + "where v.utente.id = :utenteId and v.stato = :stato "
         + "order by v.dataAggiunta desc")
    List<VoceCollezione> findByUtenteIdEStato(@Param("utenteId") Long utenteId,
                                              @Param("stato") StatoVoce stato);

    /**
     * Valore complessivo delle figure possedute.
     * Il tipo di ritorno e' Number perche' il tipo effettivo della somma
     * dipende dal dialect; la conversione avviene nel Service Layer.
     */
    @Query("select sum(v.quantita * v.figure.prezzo) from VoceCollezione v "
         + "where v.utente.id = :utenteId and v.stato = :stato")
    Number valoreCollezione(@Param("utenteId") Long utenteId, @Param("stato") StatoVoce stato);
}
