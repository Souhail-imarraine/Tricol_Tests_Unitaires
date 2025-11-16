package com.tricol.stock.service;

import com.tricol.stock.dto.response.*;
import com.tricol.stock.enums.TypeMouvement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface StockService {
    List<EtatStockDTO> getEtatGlobalStock();
    DetailStockProduitDTO getDetailStockProduit(Long produitId);
    List<MouvementStockDTO> getHistoriqueMouvements();
    List<MouvementStockDTO> getMouvementsProduit(Long produitId);
    List<EtatStockDTO> getProduitsEnAlerte();
    ValorisationStockDTO getValorisationStock();
    Page<MouvementStockDTO> searchMouvements(LocalDate dateDebut, LocalDate dateFin, 
                                           Long produitId, String reference, 
                                           TypeMouvement type, String numeroLot, 
                                           Pageable pageable);
}
