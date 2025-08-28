package com.trenicall.server.grpc.impl;

import com.trenicall.server.business.patterns.builder.RicercaBiglietti;
import com.trenicall.server.business.services.BiglietteriaService;
import com.trenicall.server.domain.entities.Biglietto;
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

    public BiglietteriaServiceImpl(BiglietteriaService biglietteriaService) {
        this.biglietteriaService = biglietteriaService;
    }

    @Override
    public void ricercaBiglietti(RicercaBigliettiRequest request,
                                 StreamObserver<RicercaBigliettiResponse> responseObserver) {
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
    }

    @Override
    public void acquistaBiglietto(AcquistaBigliettoRequest request,
                                  StreamObserver<BigliettoResponse> responseObserver) {
        Biglietto biglietto = biglietteriaService.acquista(
                request.getClienteId(),
                TipoBiglietto.valueOf(request.getTipoBiglietto()),
                request.getPartenza(),
                request.getArrivo(),
                java.time.LocalDateTime.parse(request.getDataViaggio()),
                request.getDistanzaKm()
        );
        responseObserver.onNext(toResponse(biglietto));
        responseObserver.onCompleted();
    }

    @Override
    public void modificaBiglietto(ModificaBigliettoRequest request,
                                  StreamObserver<BigliettoResponse> responseObserver) {
        Biglietto biglietto = biglietteriaService.getArchivioBiglietti().stream()
                .filter(b -> b.getId().equals(request.getBigliettoId()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Biglietto non trovato"));

        Biglietto modificato = biglietteriaService.modifica(
                biglietto,
                java.time.LocalDateTime.parse(request.getNuovaData())
        );
        responseObserver.onNext(toResponse(modificato));
        responseObserver.onCompleted();
    }

    @Override
    public void listaBigliettiCliente(ListaBigliettiClienteRequest request,
                                      StreamObserver<ListaBigliettiClienteResponse> responseObserver) {
        ListaBigliettiClienteResponse.Builder builder = ListaBigliettiClienteResponse.newBuilder();
        biglietteriaService.getArchivioBiglietti().stream()
                .filter(b -> b.getClienteId().equals(request.getClienteId()))
                .forEach(b -> builder.addBiglietti(toResponse(b)));
        responseObserver.onNext(builder.build());
        responseObserver.onCompleted();
    }

    private BigliettoResponse toResponse(Biglietto b) {
        return BigliettoResponse.newBuilder()
                .setId(b.getId())
                .setClienteId(b.getClienteId())
                .setTipo(b.getTipo().name())
                .setStato(b.getStato().getNomeStato())
                .setPartenza(b.getPartenza())
                .setArrivo(b.getArrivo())
                .setDataViaggio(b.getDataViaggio().toString())
                .setDistanzaKm(b.getDistanzaKm())
                .setPrezzo(b.getPrezzo())
                .build();
    }
}

