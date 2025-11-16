package com.tricol.stock.service;

import com.tricol.stock.entity.*;
import com.tricol.stock.enums.StatutCommande;
import com.tricol.stock.enums.TypeMouvement;
import com.tricol.stock.mapper.CommandeMapper;
import com.tricol.stock.repository.*;
import com.tricol.stock.service.impl.ReceptionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LotCreationTest {

    @Mock
    private CommandeRepository commandeRepository;
    
    @Mock
    private LotStockRepository lotStockRepository;
    
    @Mock
    private MouvementStockRepository mouvementStockRepository;
    
    @Mock
    private ProduitRepository produitRepository;
    
    @Mock
    private CommandeMapper commandeMapper;

    @InjectMocks
    private ReceptionServiceImpl receptionService;

    private Commande commande;
    private Produit produit1, produit2;
    private LigneCommande ligne1, ligne2;

    @BeforeEach
    void setUp() {
        produit1 = new Produit();
        produit1.setId(1L);
        produit1.setNom("Produit 1");
        produit1.setStockActuel(0);

        produit2 = new Produit();
        produit2.setId(2L);
        produit2.setNom("Produit 2");
        produit2.setStockActuel(5);

        ligne1 = new LigneCommande();
        ligne1.setId(1L);
        ligne1.setProduit(produit1);
        ligne1.setQuantite(100);
        ligne1.setPrixUnitaire(new BigDecimal("25.50"));

        ligne2 = new LigneCommande();
        ligne2.setId(2L);
        ligne2.setProduit(produit2);
        ligne2.setQuantite(50);
        ligne2.setPrixUnitaire(new BigDecimal("18.75"));

        commande = new Commande();
        commande.setId(1L);
        commande.setNumero("CMD-20250101-001");
        commande.setStatut(StatutCommande.VALIDEE);
        commande.setLignes(Arrays.asList(ligne1, ligne2));
    }

    @Test
    void testCreationAutomatiqueLotPourChaqueReception() {
        when(commandeRepository.findById(1L)).thenReturn(Optional.of(commande));
        when(lotStockRepository.count()).thenReturn(10L);
        when(lotStockRepository.save(any(LotStock.class))).thenAnswer(invocation -> {
            LotStock lot = invocation.getArgument(0);
            lot.setId(System.currentTimeMillis());
            return lot;
        });
        when(commandeRepository.save(any(Commande.class))).thenReturn(commande);

        receptionService.receptionnerCommande(1L);

        ArgumentCaptor<LotStock> lotCaptor = ArgumentCaptor.forClass(LotStock.class);
        verify(lotStockRepository, times(2)).save(lotCaptor.capture());

        assertEquals(2, lotCaptor.getAllValues().size());
        
        LotStock lot1 = lotCaptor.getAllValues().get(0);
        assertEquals(100, lot1.getQuantiteInitiale());
        assertEquals(100, lot1.getQuantiteRestante());
        assertEquals(new BigDecimal("25.50"), lot1.getPrixUnitaire());
        assertEquals(produit1, lot1.getProduit());
        assertEquals(commande, lot1.getCommande());
        assertEquals(LocalDate.now(), lot1.getDateEntree());
        assertTrue(lot1.getNumeroLot().startsWith("LOT-"));

        LotStock lot2 = lotCaptor.getAllValues().get(1);
        assertEquals(50, lot2.getQuantiteInitiale());
        assertEquals(50, lot2.getQuantiteRestante());
        assertEquals(new BigDecimal("18.75"), lot2.getPrixUnitaire());
        assertEquals(produit2, lot2.getProduit());
    }

    @Test
    void testGenerationNumeroLotUnique() {
        when(commandeRepository.findById(1L)).thenReturn(Optional.of(commande));
        when(lotStockRepository.count()).thenReturn(42L);
        when(lotStockRepository.save(any(LotStock.class))).thenAnswer(invocation -> {
            LotStock lot = invocation.getArgument(0);
            lot.setId(System.currentTimeMillis());
            return lot;
        });
        when(commandeRepository.save(any(Commande.class))).thenReturn(commande);

        receptionService.receptionnerCommande(1L);

        ArgumentCaptor<LotStock> lotCaptor = ArgumentCaptor.forClass(LotStock.class);
        verify(lotStockRepository, times(2)).save(lotCaptor.capture());

        String expectedPattern = "LOT-" + LocalDate.now().toString().replace("-", "") + "-0043";
        assertEquals(expectedPattern, lotCaptor.getAllValues().get(0).getNumeroLot());
        assertEquals(expectedPattern, lotCaptor.getAllValues().get(1).getNumeroLot());
    }

    @Test
    void testEnregistrementPrixAchatUnitaire() {
        when(commandeRepository.findById(1L)).thenReturn(Optional.of(commande));
        when(lotStockRepository.count()).thenReturn(0L);
        when(lotStockRepository.save(any(LotStock.class))).thenAnswer(invocation -> {
            LotStock lot = invocation.getArgument(0);
            lot.setId(System.currentTimeMillis());
            return lot;
        });
        when(commandeRepository.save(any(Commande.class))).thenReturn(commande);

        receptionService.receptionnerCommande(1L);

        ArgumentCaptor<LotStock> lotCaptor = ArgumentCaptor.forClass(LotStock.class);
        verify(lotStockRepository, times(2)).save(lotCaptor.capture());

        assertEquals(new BigDecimal("25.50"), lotCaptor.getAllValues().get(0).getPrixUnitaire());
        assertEquals(new BigDecimal("18.75"), lotCaptor.getAllValues().get(1).getPrixUnitaire());
    }

    @Test
    void testLienLotReceptionFournisseur() {
        when(commandeRepository.findById(1L)).thenReturn(Optional.of(commande));
        when(lotStockRepository.count()).thenReturn(0L);
        when(lotStockRepository.save(any(LotStock.class))).thenAnswer(invocation -> {
            LotStock lot = invocation.getArgument(0);
            lot.setId(System.currentTimeMillis());
            return lot;
        });
        when(commandeRepository.save(any(Commande.class))).thenReturn(commande);

        receptionService.receptionnerCommande(1L);

        ArgumentCaptor<LotStock> lotCaptor = ArgumentCaptor.forClass(LotStock.class);
        verify(lotStockRepository, times(2)).save(lotCaptor.capture());

        lotCaptor.getAllValues().forEach(lot -> {
            assertEquals(commande, lot.getCommande());
        });

        ArgumentCaptor<MouvementStock> mouvementCaptor = ArgumentCaptor.forClass(MouvementStock.class);
        verify(mouvementStockRepository, times(2)).save(mouvementCaptor.capture());

        mouvementCaptor.getAllValues().forEach(mouvement -> {
            assertEquals(TypeMouvement.ENTREE, mouvement.getTypeMouvement());
            assertEquals("CMD-20250101-001", mouvement.getReference());
        });
    }
}