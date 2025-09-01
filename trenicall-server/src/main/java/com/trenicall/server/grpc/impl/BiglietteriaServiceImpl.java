package com.trenicall.server.grpc.impl;

import com.trenicall.server.business.patterns.builder.RicercaBiglietti;
import com.trenicall.server.business.services.BiglietteriaService;
import com.trenicall.server.business.patterns.state.states.StatoPagato;
import com.trenicall.server.business.patterns.state.states.StatoRimborsato;
import com.trenicall.server.domain.entities.Biglietto;
import com.trenicall.server.domain.repositories.BigliettoRepository;
import com.trenicall.server.domain.valueobjects.TipoBiglietto;
import com.trenicall.server.grpc.biglietteria.BiglietteriaServiceGrpc;
import com.trenicall.server.grpc.biglietteria.BigliettoResponse;
import com.trenicall.server.grpc.biglietteria.RicercaBigliettiRequest;
import com.trenicall.server.grpc.biglietteria.RicercaBigliettiResponse;
import com.trenicall.server.grpc.biglietteria.AcquistaBigliettoRequest;
import com.trenicall.server.grpc.biglietteria.ModificaBigliettoRequest;
import com.trenicall.server.grpc.biglietteria.ListaBigliettiClienteRequest;
import com.trenicall.server.grpc.biglietteria.ListaBigliettiClienteResponse;
import io.grpc.stub.StreamObserver;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class BiglietteriaServiceImpl extends BiglietteriaServiceGrpc.BiglietteriaServiceImplBase {

    private final BiglietteriaService biglietteriaService;
    private final BigliettoRepository bigliettoRepository;

    public BiglietteriaServiceImpl(BiglietteriaService biglietteriaService, BigliettoRepository bigliettoRepository) {
        this.biglietteriaService = biglietteriaService;
        this.bigliettoRepository = bigliettoRepository;
    }

    @Override
    public void ricercaBiglietti(RicercaBigliettiRequest request,
                                 StreamObserver<RicercaBigliettiResponse> responseObserver) {
        try {
            RicercaBiglietti ricerca = new RicercaBiglietti.Builder()
                    .partenza(request.getPartenza())
                    .arrivo(request.getArrivo())
                    .dataViaggio(LocalDateTime.parse(request.getDataViaggio()))
                    .classeServizio(request.getClasseServizio())
                    .soloAltaVelocita(request.getSoloAltaVelocita())
                    .includiPromozioni(request.getIncludiPromozioni())
                    .build();

            List<Biglietto> risultati = biglietteriaService.ricerca(ricerca);

            RicercaBigliettiResponse.Builder response = RicercaBigliettiResponse.newBuilder();
            risultati.forEach(b -> response.addRisultati(toResponse(b)));
            responseObserver.onNext(response.build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Errore nella ricerca: " + e.getMessage())
                    .withCause(e)
                    .asRuntimeException());
        }
    }

    @Override
    public void acquistaBiglietto(AcquistaBigliettoRequest request,
                                  StreamObserver<BigliettoResponse> responseObserver) {
        try {
            Biglietto biglietto = biglietteriaService.acquista(
                    request.getClienteId(),
                    TipoBiglietto.valueOf(request.getTipoBiglietto()),
                    request.getPartenza(),
                    request.getArrivo(),
                    LocalDateTime.parse(request.getDataViaggio()),
                    request.getDistanzaKm()
            );
            biglietto.setStato(new StatoPagato());
            bigliettoRepository.save(biglietto);
            responseObserver.onNext(toResponse(biglietto));
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Errore nell'acquisto: " + e.getMessage())
                    .withCause(e)
                    .asRuntimeException());
        }
    }

    @Override
    public void modificaBiglietto(ModificaBigliettoRequest request,
                                  StreamObserver<BigliettoResponse> responseObserver) {
        try {
            Biglietto biglietto = bigliettoRepository.findById(request.getBigliettoId())
                    .orElseThrow(() -> new IllegalArgumentException("Biglietto non trovato"));

            if ("RIMBORSA".equals(request.getNuovaData())) {
                biglietto.setStato(new StatoRimborsato());
            } else {
                biglietto.setDataViaggio(LocalDateTime.parse(request.getNuovaData()));
            }

            bigliettoRepository.save(biglietto);
            responseObserver.onNext(toResponse(biglietto));
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Errore nella modifica: " + e.getMessage())
                    .withCause(e)
                    .asRuntimeException());
        }
    }

    @Override
    public void listaBigliettiCliente(ListaBigliettiClienteRequest request,
                                      StreamObserver<ListaBigliettiClienteResponse> responseObserver) {
        try {
            List<Biglietto> biglietti = bigliettoRepository.findByClienteId(request.getClienteId());
            ListaBigliettiClienteResponse.Builder builder = ListaBigliettiClienteResponse.newBuilder();

            biglietti.forEach(b -> builder.addBiglietti(toResponse(b)));

            responseObserver.onNext(builder.build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Errore nel recupero lista: " + e.getMessage())
                    .withCause(e)
                    .asRuntimeException());
        }
    }

    private BigliettoResponse toResponse(Biglietto b) {
        String stato = "PAGATO";
        if (b.getStato() != null) {
            stato = b.getStato().getNomeStato();
        }

        return BigliettoResponse.newBuilder()
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
    }
}
