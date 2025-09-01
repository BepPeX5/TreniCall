package com.trenicall.server.grpc.impl;

import com.trenicall.server.business.services.BiglietteriaService;
import com.trenicall.server.business.services.PrenotazioneService;
import com.trenicall.server.business.services.ClienteService;
import com.trenicall.server.domain.entities.Biglietto;
import com.trenicall.server.domain.entities.Cliente;
import com.trenicall.server.domain.entities.Prenotazione;
import com.trenicall.server.domain.valueobjects.TipoBiglietto;
import com.trenicall.server.grpc.biglietteria.BigliettoResponse;
import com.trenicall.server.grpc.prenotazione.*;
import io.grpc.stub.StreamObserver;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collection;

@Service
public class PrenotazioneServiceImpl extends PrenotazioneServiceGrpc.PrenotazioneServiceImplBase {

    private final PrenotazioneService prenotazioneService;
    private final BiglietteriaService biglietteriaService;
    private final ClienteService clienteService;

    public PrenotazioneServiceImpl(PrenotazioneService prenotazioneService,
                                   BiglietteriaService biglietteriaService,
                                   ClienteService clienteService) {
        this.prenotazioneService = prenotazioneService;
        this.biglietteriaService = biglietteriaService;
        this.clienteService = clienteService;
    }

    @Override
    public void creaPrenotazione(CreaPrenotazioneRequest request,
                                 StreamObserver<PrenotazioneResponse> responseObserver) {
        try {
            Prenotazione p = prenotazioneService.creaPrenotazione(
                    request.getClienteId(),
                    TipoBiglietto.valueOf(request.getTipoBiglietto()),
                    request.getPartenza(),
                    request.getArrivo(),
                    LocalDateTime.parse(request.getDataViaggio()),
                    request.getDistanzaKm(),
                    request.getMinutiValidita()
            );

            PrenotazioneResponse response = PrenotazioneResponse.newBuilder()
                    .setId(p.getId())
                    .setClienteId(p.getCliente().getId())
                    .setBigliettoId(p.getBiglietto().getId())
                    .setDataCreazione(p.getDataCreazione().toString())
                    .setScadenza(p.getScadenza().toString())
                    .setAttiva(p.isAttiva())
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (Exception e) {
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Errore nella creazione prenotazione: " + e.getMessage())
                    .withCause(e)
                    .asRuntimeException());
        }
    }

    @Override
    public void confermaAcquisto(ConfermaAcquistoRequest request,
                                 StreamObserver<BigliettoResponse> responseObserver) {
        try {
            Biglietto b = prenotazioneService.confermaAcquisto(request.getPrenotazioneId(), biglietteriaService);

            String stato = "UNKNOWN";
            if (b.getStato() != null) {
                stato = b.getStato().getNomeStato();
            }

            BigliettoResponse response = BigliettoResponse.newBuilder()
                    .setId(b.getId())
                    .setClienteId(b.getClienteId())
                    .setTipo(b.getTipo().name())
                    .setStato(stato)
                    .setPartenza(b.getPartenza())
                    .setArrivo(b.getArrivo())
                    .setDataViaggio(b.getDataViaggio().toString())
                    .setDistanzaKm(b.getDistanzaKm())
                    .setPrezzo(b.getPrezzo())
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (Exception e) {
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Errore nella conferma acquisto: " + e.getMessage())
                    .withCause(e)
                    .asRuntimeException());
        }
    }

    @Override
    public void listaPrenotazioniAttive(ListaPrenotazioniRequest request,
                                        StreamObserver<ListaPrenotazioniResponse> responseObserver) {
        try {
            Collection<Prenotazione> attive;
            if (request.getClienteId() != null && !request.getClienteId().isEmpty()) {
                attive = prenotazioneService.getPrenotazioniAttiveByCliente(request.getClienteId());
            } else {
                attive = prenotazioneService.getPrenotazioniAttive();
            }

            ListaPrenotazioniResponse.Builder builder = ListaPrenotazioniResponse.newBuilder();

            attive.forEach(p -> {
                try {
                    PrenotazioneResponse response = PrenotazioneResponse.newBuilder()
                            .setId(p.getId())
                            .setClienteId(p.getCliente().getId())
                            .setBigliettoId(p.getBiglietto().getId())
                            .setDataCreazione(p.getDataCreazione().toString())
                            .setScadenza(p.getScadenza().toString())
                            .setAttiva(p.isAttiva())
                            .build();
                    builder.addPrenotazioni(response);
                } catch (Exception e) {
                }
            });

            responseObserver.onNext(builder.build());
            responseObserver.onCompleted();

        } catch (Exception e) {
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Errore nel recupero prenotazioni: " + e.getMessage())
                    .withCause(e)
                    .asRuntimeException());
        }
    }
}


