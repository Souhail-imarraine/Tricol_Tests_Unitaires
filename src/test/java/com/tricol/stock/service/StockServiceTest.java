package com.tricol.stock.service;

import com.tricol.stock.dto.response.ValorisationStockDTO;
import com.tricol.stock.entity.LotStock;
import com.tricol.stock.entity.Produit;
import com.tricol.stock.repository.LotStockRepository;
import com.tricol.stock.repository.ProduitRepository;
import com.tricol.stock.service.impl.StockServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StockServiceTest {

    @Mock
    private ProduitRepository produitRepository;
    
    @Mock
    private LotStockRepository lotStockRepository;

    @InjectMocks
    private StockServiceImpl stockService;

    private Produit produit1, produit2;
    private LotStock lot1, lot2, lot3;

    @BeforeEach
    void setUp() {
        produit1 = new Produit();
        produit1.setId(1L);
        produit1.setNom("Produit 1");

        produit2 = new Produit();
        produit2.setId(2L);
        produit2.setNom("Produit 2");

        lot1 = new LotStock();
        lot1.setId(1L);
        lot1.setNumeroLot("LOT-001");
        lot1.setDateEntree(LocalDate.of(2025, 1, 1));
        lot1.setQuantiteRestante(50);
        lot1.setPrixUnitaire(new BigDecimal("10.00"));
        lot1.setProduit(produit1);

        lot2 = new LotStock();
        lot2.setId(2L);
        lot2.setNumeroLot("LOT-002");
        lot2.setDateEntree(LocalDate.of(2025, 1, 5));
        lot2.setQuantiteRestante(30);
        lot2.setPrixUnitaire(new BigDecimal("15.00"));
        lot2.setProduit(produit1);

        lot3 = new LotStock();
        lot3.setId(3L);
        lot3.setNumeroLot("LOT-003");
        lot3.setDateEntree(LocalDate.of(2025, 1, 10));
        lot3.setQuantiteRestante(20);
        lot3.setPrixUnitaire(new BigDecimal("20.00"));
        lot3.setProduit(produit2);
    }

    @Test
    void testCalculValorisationStockAvecPlusieurLotsPrixDifferents() {
        when(lotStockRepository.findAll()).thenReturn(Arrays.asList(lot1, lot2, lot3));
        when(produitRepository.count()).thenReturn(2L);

        ValorisationStockDTO valorisation = stockService.getValorisationStock();

        BigDecimal valeurAttendue = new BigDecimal("10.00").multiply(BigDecimal.valueOf(50))
                .add(new BigDecimal("15.00").multiply(BigDecimal.valueOf(30)))
                .add(new BigDecimal("20.00").multiply(BigDecimal.valueOf(20)));

        assertEquals(valeurAttendue, valorisation.getValeurTotale());
        assertEquals(2, valorisation.getNombreProduits());
        assertEquals(100, valorisation.getQuantiteTotale());
    }

    @Test
    void testCalculValorisationStockMethodeFifo() {
        when(lotStockRepository.findAll()).thenReturn(Arrays.asList(lot1, lot2));
        when(produitRepository.count()).thenReturn(1L);

        ValorisationStockDTO valorisation = stockService.getValorisationStock();
        BigDecimal valeurAttendue = new BigDecimal("10.00").multiply(BigDecimal.valueOf(50))
                .add(new BigDecimal("15.00").multiply(BigDecimal.valueOf(30)));

        assertEquals(valeurAttendue, valorisation.getValeurTotale());
    }

    @Test
    void testCalculValorisationStockVide() {
        when(lotStockRepository.findAll()).thenReturn(Arrays.asList());
        when(produitRepository.count()).thenReturn(0L);

        ValorisationStockDTO valorisation = stockService.getValorisationStock();

        assertEquals(BigDecimal.ZERO, valorisation.getValeurTotale());
        assertEquals(0, valorisation.getNombreProduits());
        assertEquals(0, valorisation.getQuantiteTotale());
    }
}