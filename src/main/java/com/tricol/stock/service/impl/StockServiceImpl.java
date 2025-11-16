package com.tricol.stock.service.impl;

import com.tricol.stock.dto.response.*;
import com.tricol.stock.entity.LotStock;
import com.tricol.stock.entity.MouvementStock;
import com.tricol.stock.entity.Produit;
import com.tricol.stock.enums.TypeMouvement;
import com.tricol.stock.exception.ResourceNotFoundException;
import com.tricol.stock.mapper.LotStockMapper;
import com.tricol.stock.mapper.MouvementStockMapper;
import com.tricol.stock.repository.LotStockRepository;
import com.tricol.stock.repository.MouvementStockRepository;
import com.tricol.stock.repository.ProduitRepository;
import com.tricol.stock.service.StockService;
import com.tricol.stock.specification.MouvementStockSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StockServiceImpl implements StockService {
    
    private final ProduitRepository produitRepository;
    private final LotStockRepository lotStockRepository;
    private final MouvementStockRepository mouvementStockRepository;
    private final LotStockMapper lotStockMapper;
    private final MouvementStockMapper mouvementStockMapper;
    
    @Override
    public List<EtatStockDTO> getEtatGlobalStock() {
        List<Produit> produits = produitRepository.findAll();

        return produits.stream().map(produit -> {
            BigDecimal valeurStock = calculerValeurStockProduit(produit.getId());
            boolean enAlerte = produit.getStockActuel() <= produit.getPointCommande();
            
            return new EtatStockDTO(
                produit.getId(),
                produit.getNom(),
                produit.getReference(),
                produit.getStockActuel(),
                produit.getPointCommande(),
                enAlerte,
                valeurStock
            );
        }).collect(Collectors.toList());
    }
    
    @Override
    public DetailStockProduitDTO getDetailStockProduit(Long produitId) {
        Produit produit = produitRepository.findById(produitId)
            .orElseThrow(() -> new ResourceNotFoundException("Produit non trouvé: " + produitId));
        
        List<LotStock> lots = lotStockRepository
            .findByProduitIdAndQuantiteRestanteGreaterThanOrderByDateEntreeAsc(produitId, 0);
        
        List<LotStockDTO> lotsDTO = lotStockMapper.toDTOList(lots);
        BigDecimal valeurTotale = calculerValeurStockProduit(produitId);
        
        return new DetailStockProduitDTO(
            produit.getId(),
            produit.getNom(),
            produit.getReference(),
            produit.getStockActuel(),
            produit.getPointCommande(),
            valeurTotale,
            lotsDTO
        );
    }
    
    @Override
    public List<MouvementStockDTO> getHistoriqueMouvements() {
        List<MouvementStock> mouvements = mouvementStockRepository.findAllByOrderByDateMouvementDesc();
        return mouvementStockMapper.toDTOList(mouvements);
    }
    
    @Override
    public List<MouvementStockDTO> getMouvementsProduit(Long produitId) {
        List<MouvementStock> mouvements = mouvementStockRepository.findByProduitIdOrderByDateMouvementDesc(produitId);
        return mouvementStockMapper.toDTOList(mouvements);
    }

    @Override
    public List<EtatStockDTO> getProduitsEnAlerte() {
        List<Produit> produits = produitRepository.findProduitsEnAlerte();
        
        return produits.stream().map(produit -> {
            BigDecimal valeurStock = calculerValeurStockProduit(produit.getId());
            
            return new EtatStockDTO(
                produit.getId(),
                produit.getNom(),
                produit.getReference(),
                produit.getStockActuel(),
                produit.getPointCommande(),
                true,
                valeurStock
            );
        }).collect(Collectors.toList());
    }

    @Override
    public ValorisationStockDTO getValorisationStock() {
        List<LotStock> lots = lotStockRepository.findAll();

        BigDecimal valeurTotale = BigDecimal.ZERO;
        int quantiteTotale = 0;

        for (LotStock lot : lots) {
            BigDecimal valeurLot = lot.getPrixUnitaire()
                    .multiply(BigDecimal.valueOf(lot.getQuantiteRestante()));
            valeurTotale = valeurTotale.add(valeurLot);
            quantiteTotale += lot.getQuantiteRestante();
        }

        int nombreProduits = (int) produitRepository.count();

        return new ValorisationStockDTO(valeurTotale, nombreProduits, quantiteTotale);
    }


    @Override
    public Page<MouvementStockDTO> searchMouvements(LocalDate dateDebut, LocalDate dateFin, 
                                                  Long produitId, String reference, 
                                                  TypeMouvement type, String numeroLot, 
                                                  Pageable pageable) {
        Specification<MouvementStock> spec = Specification.where(null);
        
        if (dateDebut != null || dateFin != null) {
            spec = spec.and(MouvementStockSpecification.hasDateBetween(dateDebut, dateFin));
        }
        
        if (produitId != null) {
            spec = spec.and(MouvementStockSpecification.hasProduitId(produitId));
        }
        
        if (reference != null) {
            spec = spec.and(MouvementStockSpecification.hasProduitReference(reference));
        }
        
        if (type != null) {
            spec = spec.and(MouvementStockSpecification.hasTypeMouvement(type));
        }
        
        if (numeroLot != null) {
            spec = spec.and(MouvementStockSpecification.hasNumeroLot(numeroLot));
        }
        
        Page<MouvementStock> mouvements = mouvementStockRepository.findAll(spec, pageable);
        return mouvements.map(mouvementStockMapper::toDTO);
    }

    private BigDecimal calculerValeurStockProduit(Long produitId) {
        List<LotStock> lots = lotStockRepository
            .findByProduitIdAndQuantiteRestanteGreaterThanOrderByDateEntreeAsc(produitId, 0);
        
        return lots.stream()
            .map(lot -> lot.getPrixUnitaire().multiply(BigDecimal.valueOf(lot.getQuantiteRestante())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
