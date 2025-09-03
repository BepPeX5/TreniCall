package com.trenicall.server.business.patterns.factory;

import com.trenicall.server.domain.entities.Biglietto;
import com.trenicall.server.domain.entities.Treno;
import com.trenicall.server.domain.repositories.TrenoRepository;
import com.trenicall.server.domain.valueobjects.TipoBiglietto;
import com.trenicall.server.business.patterns.state.states.StatoPrenotato;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Component
public class BigliettoFactoryImpl implements BigliettoFactory {

    @Autowired
    private TrenoRepository trenoRepository;

    @Override
    public Biglietto creaBiglietto(TipoBiglietto tipo, String partenza, String arrivo,
                                   LocalDateTime dataViaggio, Integer distanzaKm, String clienteId) {

        Treno trenoReale = trovaTrenoRealePerTratta(tipo, partenza, arrivo);

        if (trenoReale == null) {
            throw new IllegalStateException("Nessun treno " + tipo + " disponibile per " + partenza + " → " + arrivo);
        }

        String bigliettoId = "BIG-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        Biglietto biglietto = new Biglietto(
                bigliettoId,
                clienteId,
                new StatoPrenotato(),
                tipo,
                partenza,
                arrivo,
                dataViaggio,
                distanzaKm
        );

        biglietto.setTrenoAssociato(trenoReale.getId());

        System.out.println("✅ Biglietto " + bigliettoId + " associato al treno " + trenoReale.getId() +
                " per cliente " + clienteId);

        return biglietto;
    }

    private Treno trovaTrenoRealePerTratta(TipoBiglietto tipo, String partenza, String arrivo) {
        List<Treno> tuttiTreni = trenoRepository.findAll();

        String prefisso = getTipoPrefisso(tipo);

        return tuttiTreni.stream()
                .filter(t -> t.getId().startsWith(prefisso))
                .filter(t -> t.getTratta() != null)
                .filter(t -> t.getTratta().getStazionePartenza().equals(partenza))
                .filter(t -> t.getTratta().getStazioneArrivo().equals(arrivo))
                .findFirst()
                .orElse(null);
    }

    private String getTipoPrefisso(TipoBiglietto tipo) {
        switch (tipo) {
            case REGIONALE: return "REG";
            case INTERCITY: return "IC";
            case FRECCIA_ROSSA: return "FR";
            default: return "TR";
        }
    }
}


