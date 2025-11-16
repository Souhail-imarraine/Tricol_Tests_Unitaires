package com.tricol.stock.controller;

import com.tricol.stock.dto.response.*;
import com.tricol.stock.enums.TypeMouvement;
import com.tricol.stock.service.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/stock")
@RequiredArgsConstructor
public class StockController {
    
    private final StockService stockService;
    
    @GetMapping
    public ResponseEntity<List<EtatStockDTO>> getEtatGlobalStock() {
        return ResponseEntity.ok(stockService.getEtatGlobalStock());
    }
    
    @GetMapping("/produit/{id}")
    public ResponseEntity<DetailStockProduitDTO> getDetailStockProduit(@PathVariable Long id) {
        return ResponseEntity.ok(stockService.getDetailStockProduit(id));
    }
    
    @GetMapping("/mouvements")
    public ResponseEntity<Page<MouvementStockDTO>> getHistoriqueMouvements(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin,
            @RequestParam(required = false) Long produitId,
            @RequestParam(required = false) String reference,
            @RequestParam(required = false) TypeMouvement type,
            @RequestParam(required = false) String numeroLot,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<MouvementStockDTO> result = stockService.searchMouvements(
                dateDebut, dateFin, produitId, reference, type, numeroLot, pageable);
        
        return ResponseEntity.ok(result);
    }
    
    @GetMapping("/mouvements/produit/{id}")
    public ResponseEntity<List<MouvementStockDTO>> getMouvementsProduit(@PathVariable Long id) {
        return ResponseEntity.ok(stockService.getMouvementsProduit(id));
    }
    
    @GetMapping("/alertes")
    public ResponseEntity<List<EtatStockDTO>> getProduitsEnAlerte() {
        return ResponseEntity.ok(stockService.getProduitsEnAlerte());
    }
    
    @GetMapping("/valorisation")
    public ResponseEntity<ValorisationStockDTO> getValorisationStock() {
        return ResponseEntity.ok(stockService.getValorisationStock());
    }
}
