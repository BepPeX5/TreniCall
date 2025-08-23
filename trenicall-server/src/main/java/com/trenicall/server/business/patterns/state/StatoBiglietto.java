package com.trenicall.server.business.patterns.state;

import com.trenicall.server.domain.entities.Biglietto;


public interface StatoBiglietto {


    void confermaPrenotazione(Biglietto biglietto);


    void utilizzaBiglietto(Biglietto biglietto);


    void rimborsaBiglietto(Biglietto biglietto);


    void modificaBiglietto(Biglietto biglietto);


    void scadenzaBiglietto(Biglietto biglietto);


    String getNomeStato();


    boolean isUtilizzabile();


    boolean isModificabile();


    boolean isRimborsabile();
}