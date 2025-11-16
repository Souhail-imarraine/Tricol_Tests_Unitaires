package com.tricol.stock.service;

import com.tricol.stock.dto.response.CommandeResponseDTO;
import com.tricol.stock.entity.*;
import com.tricol.stock.enums.StatutCommande;
import com.tricol.stock.enums.TypeMouvement;
import com.tricol.stock.mapper.CommandeMapper;
import com.tricol.stock.repository.*;
import com.tricol.stock.service.impl.ReceptionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
class ReceptionServiceTest {

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
    private Produit produit;
    private LigneCommande ligne;
    private Fournisseur fournisseur;

    @BeforeEach
    void setUp() {
        fournisseur = new Fournisseur();
        fournisseur.setId(1L);
        fournisseur.setRaisonSociale("Fournisseur Test");

        produit = new Produit();
        produit.setId(1L);
        produit.setNom("Produit Test");
        produit.setStockActuel(10);

        ligne = new LigneCommande();
        ligne.setId(1L);
        ligne.setProduit(produit);
        ligne.setQuantite(50);
        ligne.setPrixUnitaire(new BigDecimal("15.00"));

        commande = new Commande();
        commande.setId(1L);
        commande.setNumero("CMD-001");
        commande.setStatut(StatutCommande.VALIDEE);
        commande.setFournisseur(fournisseur);
        commande.setLignes(Arrays.asList(ligne));
        ligne.setCommande(commande);
    }

    @Test
    void testReceptionCommandeValideeCreeLotAutomatiquement() {
        when(commandeRepository.findById(1L)).thenReturn(Optional.of(commande));
        when(lotStockRepository.count()).thenReturn(0L);
        when(lotStockRepository.save(any(LotStock.class))).thenAnswer(invocation -> {
            LotStock lot = invocation.getArgument(0);
            lot.setId(1L);
            return lot;
        });
        when(commandeRepository.save(any(Commande.class))).thenReturn(commande);
        when(commandeMapper.toDTO(any(Commande.class))).thenReturn(new CommandeResponseDTO());

        CommandeResponseDTO result = receptionService.receptionnerCommande(1L);

        verify(lotStockRepository).save(argThat(lot -> 
            lot.getQuantiteInitiale().equals(50) &&
            lot.getQuantiteRestante().equals(50) &&
            lot.getPrixUnitaire().equals(new BigDecimal("15.00")) &&
            lot.getProduit().equals(produit) &&
            lot.getCommande().equals(commande) &&
            lot.getNumeroLot().startsWith("LOT-") &&
            lot.getDateEntree().equals(LocalDate.now())
        ));
    }

    @Test
    void testReceptionCommandeGenereNumeroLotCorrect() {
        when(commandeRepository.findById(1L)).thenReturn(Optional.of(commande));
        when(lotStockRepository.count()).thenReturn(5L);
        when(lotStockRepository.save(any(LotStock.class))).thenAnswer(invocation -> {
            LotStock lot = invocation.getArgument(0);
            lot.setId(1L);
            return lot;
        });
        when(commandeRepository.save(any(Commande.class))).thenReturn(commande);
        when(commandeMapper.toDTO(any(Commande.class))).thenReturn(new CommandeResponseDTO());

        receptionService.receptionnerCommande(1L);

        String expectedPattern = "LOT-" + LocalDate.now().toString().replace("-", "") + "-0006";
        verify(lotStockRepository).save(argThat(lot -> 
            lot.getNumeroLot().equals(expectedPattern)
        ));
    }

    @Test
    void testReceptionCommandeVerifieLienLotReception() {
        when(commandeRepository.findById(1L)).thenReturn(Optional.of(commande));
        when(lotStockRepository.count()).thenReturn(0L);
        when(lotStockRepository.save(any(LotStock.class))).thenAnswer(invocation -> {
            LotStock lot = invocation.getArgument(0);
            lot.setId(1L);
            return lot;
        });
        when(commandeRepository.save(any(Commande.class))).thenReturn(commande);
        when(commandeMapper.toDTO(any(Commande.class))).thenReturn(new CommandeResponseDTO());

        receptionService.receptionnerCommande(1L);

        verify(lotStockRepository).save(argThat(lot -> 
            lot.getCommande().equals(commande)
        ));
        verify(mouvementStockRepository).save(argThat(mouvement ->
            mouvement.getTypeMouvement().equals(TypeMouvement.ENTREE) &&
            mouvement.getReference().equals("CMD-001")
        ));
    }

    @Test
    void testReceptionCommandeNonValideeThrowsException() {
        commande.setStatut(StatutCommande.EN_ATTENTE);
        when(commandeRepository.findById(1L)).thenReturn(Optional.of(commande));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> receptionService.receptionnerCommande(1L)
        );

        assertTrue(exception.getMessage().contains("Seules les commandes VALIDÉES peuvent être réceptionnées"));
    }
}