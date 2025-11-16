package com.tricol.stock.service;

import com.tricol.stock.entity.LotStock;
import com.tricol.stock.entity.MouvementStock;
import com.tricol.stock.entity.Produit;
import com.tricol.stock.enums.TypeMouvement;
import com.tricol.stock.exception.InsufficientStockException;
import com.tricol.stock.repository.LotStockRepository;
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
class FifoStockStrategyTest {

    @Mock
    private LotStockRepository lotStockRepository;

    @InjectMocks
    private FifoStockStrategy fifoStockStrategy;

    private Produit produit;
    private LotStock lot1, lot2, lot3;

    @BeforeEach
    void setUp() {
        produit = new Produit();
        produit.setId(1L);
        produit.setNom("Produit Test");

        lot1 = new LotStock();
        lot1.setId(1L);
        lot1.setNumeroLot("LOT-001");
        lot1.setDateEntree(LocalDate.of(2025, 1, 1));
        lot1.setQuantiteRestante(50);
        lot1.setPrixUnitaire(new BigDecimal("10.00"));
        lot1.setProduit(produit);

        lot2 = new LotStock();
        lot2.setId(2L);
        lot2.setNumeroLot("LOT-002");
        lot2.setDateEntree(LocalDate.of(2025, 1, 5));
        lot2.setQuantiteRestante(30);
        lot2.setPrixUnitaire(new BigDecimal("12.00"));
        lot2.setProduit(produit);

        lot3 = new LotStock();
        lot3.setId(3L);
        lot3.setNumeroLot("LOT-003");
        lot3.setDateEntree(LocalDate.of(2025, 1, 10));
        lot3.setQuantiteRestante(20);
        lot3.setPrixUnitaire(new BigDecimal("15.00"));
        lot3.setProduit(produit);
    }

    @Test
    void testSortieSimpleConsommantPartiellementUnSeulLot() {
        when(lotStockRepository.findByProduitIdAndQuantiteRestanteGreaterThanOrderByDateEntreeAsc(1L, 0))
                .thenReturn(Arrays.asList(lot1, lot2, lot3));

        List<MouvementStock> mouvements = fifoStockStrategy.consumeStock(produit, 30);

        assertEquals(1, mouvements.size());
        MouvementStock mouvement = mouvements.get(0);
        assertEquals(30, mouvement.getQuantite());
        assertEquals(TypeMouvement.SORTIE, mouvement.getTypeMouvement());
        assertEquals(new BigDecimal("10.00"), mouvement.getPrixUnitaire());
        assertEquals(lot1, mouvement.getLot());
        assertEquals(20, lot1.getQuantiteRestante());
    }

    @Test
    void testSortieNecessitantConsommationPlusieurLotsSuccessifs() {
        when(lotStockRepository.findByProduitIdAndQuantiteRestanteGreaterThanOrderByDateEntreeAsc(1L, 0))
                .thenReturn(Arrays.asList(lot1, lot2, lot3));

        List<MouvementStock> mouvements = fifoStockStrategy.consumeStock(produit, 70);

        assertEquals(2, mouvements.size());
        
        MouvementStock mouvement1 = mouvements.get(0);
        assertEquals(50, mouvement1.getQuantite());
        assertEquals(lot1, mouvement1.getLot());
        assertEquals(0, lot1.getQuantiteRestante());
        
        MouvementStock mouvement2 = mouvements.get(1);
        assertEquals(20, mouvement2.getQuantite());
        assertEquals(lot2, mouvement2.getLot());
        assertEquals(10, lot2.getQuantiteRestante());
    }

    @Test
    void testSortieAvecStockInsuffisant() {
        when(lotStockRepository.findByProduitIdAndQuantiteRestanteGreaterThanOrderByDateEntreeAsc(1L, 0))
                .thenReturn(Arrays.asList(lot1, lot2, lot3));

        InsufficientStockException exception = assertThrows(
                InsufficientStockException.class,
                () -> fifoStockStrategy.consumeStock(produit, 150)
        );

        assertEquals("Stock insuffisant pour Produit Test", exception.getMessage());
    }

    @Test
    void testSortieEpuisantExactementLeStockDisponible() {
        when(lotStockRepository.findByProduitIdAndQuantiteRestanteGreaterThanOrderByDateEntreeAsc(1L, 0))
                .thenReturn(Arrays.asList(lot1, lot2, lot3));

        List<MouvementStock> mouvements = fifoStockStrategy.consumeStock(produit, 100);

        assertEquals(3, mouvements.size());
        assertEquals(0, lot1.getQuantiteRestante());
        assertEquals(0, lot2.getQuantiteRestante());
        assertEquals(0, lot3.getQuantiteRestante());
        
        int totalQuantite = mouvements.stream().mapToInt(MouvementStock::getQuantite).sum();
        assertEquals(100, totalQuantite);
    }
}