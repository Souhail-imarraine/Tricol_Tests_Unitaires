package com.tricol.stock.service;

import com.tricol.stock.dto.response.BonSortieResponseDTO;
import com.tricol.stock.entity.BonSortie;
import com.tricol.stock.entity.LigneBonSortie;
import com.tricol.stock.entity.MouvementStock;
import com.tricol.stock.entity.Produit;
import com.tricol.stock.enums.StatutBonSortie;
import com.tricol.stock.mapper.BonSortieMapper;
import com.tricol.stock.repository.BonSortieRepository;
import com.tricol.stock.repository.MouvementStockRepository;
import com.tricol.stock.repository.ProduitRepository;
import com.tricol.stock.service.impl.BonSortieServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BonSortieServiceTest {

    @Mock
    private BonSortieRepository bonSortieRepository;
    
    @Mock
    private MouvementStockRepository mouvementStockRepository;
    
    @Mock
    private ProduitRepository produitRepository;
    
    @Mock
    private FifoStockStrategy fifoStrategy;
    
    @Mock
    private BonSortieMapper bonSortieMapper;

    @InjectMocks
    private BonSortieServiceImpl bonSortieService;

    private BonSortie bonSortie;
    private Produit produit;
    private LigneBonSortie ligne;
    private MouvementStock mouvement;

    @BeforeEach
    void setUp() {
        produit = new Produit();
        produit.setId(1L);
        produit.setNom("Produit Test");
        produit.setStockActuel(100);

        ligne = new LigneBonSortie();
        ligne.setId(1L);
        ligne.setProduit(produit);
        ligne.setQuantite(50);

        bonSortie = new BonSortie();
        bonSortie.setId(1L);
        bonSortie.setNumero("BS-20250101-0001");
        bonSortie.setStatut(StatutBonSortie.BROUILLON);
        bonSortie.setDateCreation(LocalDateTime.now());
        bonSortie.setLignes(Arrays.asList(ligne));
        ligne.setBonSortie(bonSortie);

        mouvement = new MouvementStock();
        mouvement.setQuantite(50);
    }

    @Test
    void testValidationBonSortieBrouillonCreeMouvementsStock() {
        when(bonSortieRepository.findById(1L)).thenReturn(Optional.of(bonSortie));
        when(fifoStrategy.consumeStock(produit, 50)).thenReturn(Arrays.asList(mouvement));
        when(bonSortieRepository.save(any(BonSortie.class))).thenReturn(bonSortie);
        when(bonSortieMapper.toDTO(any(BonSortie.class))).thenReturn(new BonSortieResponseDTO());

        BonSortieResponseDTO result = bonSortieService.valider(1L);

        verify(fifoStrategy).consumeStock(produit, 50);
        verify(mouvementStockRepository).saveAll(any(List.class));
        verify(produitRepository).save(produit);
        assertEquals(50, produit.getStockActuel());
        assertEquals(StatutBonSortie.VALIDE, bonSortie.getStatut());
        assertNotNull(bonSortie.getDateValidation());
    }

    @Test
    void testValidationBonSortieMiseAJourQuantitesRestantes() {
        when(bonSortieRepository.findById(1L)).thenReturn(Optional.of(bonSortie));
        when(fifoStrategy.consumeStock(produit, 50)).thenReturn(Arrays.asList(mouvement));
        when(bonSortieRepository.save(any(BonSortie.class))).thenReturn(bonSortie);
        when(bonSortieMapper.toDTO(any(BonSortie.class))).thenReturn(new BonSortieResponseDTO());

        bonSortieService.valider(1L);

        assertEquals(50, produit.getStockActuel());
        verify(produitRepository).save(produit);
    }

    @Test
    void testValidationBonSortieEnregistreInformationsValidation() {
        when(bonSortieRepository.findById(1L)).thenReturn(Optional.of(bonSortie));
        when(fifoStrategy.consumeStock(produit, 50)).thenReturn(Arrays.asList(mouvement));
        when(bonSortieRepository.save(any(BonSortie.class))).thenReturn(bonSortie);
        when(bonSortieMapper.toDTO(any(BonSortie.class))).thenReturn(new BonSortieResponseDTO());

        bonSortieService.valider(1L);

        assertEquals(StatutBonSortie.VALIDE, bonSortie.getStatut());
        assertNotNull(bonSortie.getDateValidation());
    }

    @Test
    void testValidationBonSortieDejaValideThrowsException() {
        bonSortie.setStatut(StatutBonSortie.VALIDE);
        when(bonSortieRepository.findById(1L)).thenReturn(Optional.of(bonSortie));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> bonSortieService.valider(1L)
        );

        assertEquals("Seuls les bons BROUILLON peuvent être validés", exception.getMessage());
    }
}