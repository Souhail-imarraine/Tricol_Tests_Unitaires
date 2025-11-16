package com.tricol.stock.service;

import com.tricol.stock.dto.response.MouvementStockDTO;
import com.tricol.stock.entity.MouvementStock;
import com.tricol.stock.enums.TypeMouvement;
import com.tricol.stock.mapper.MouvementStockMapper;
import com.tricol.stock.repository.MouvementStockRepository;
import com.tricol.stock.service.impl.StockServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StockServiceAdvancedSearchTest {

    @Mock
    private MouvementStockRepository mouvementStockRepository;
    
    @Mock
    private MouvementStockMapper mouvementStockMapper;

    @InjectMocks
    private StockServiceImpl stockService;

    @Test
    void testSearchMouvementsParPeriode() {
        LocalDate dateDebut = LocalDate.of(2025, 1, 1);
        LocalDate dateFin = LocalDate.of(2025, 1, 31);
        Pageable pageable = PageRequest.of(0, 10);
        
        MouvementStock mouvement = new MouvementStock();
        Page<MouvementStock> mockPage = new PageImpl<>(Arrays.asList(mouvement));
        MouvementStockDTO dto = new MouvementStockDTO();
        
        when(mouvementStockRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(mockPage);
        when(mouvementStockMapper.toDTO(mouvement)).thenReturn(dto);

        Page<MouvementStockDTO> result = stockService.searchMouvements(
                dateDebut, dateFin, null, null, null, null, pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(mouvementStockRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void testSearchMouvementsParProduitId() {
        Long produitId = 123L;
        Pageable pageable = PageRequest.of(0, 10);
        
        MouvementStock mouvement = new MouvementStock();
        Page<MouvementStock> mockPage = new PageImpl<>(Arrays.asList(mouvement));
        MouvementStockDTO dto = new MouvementStockDTO();
        
        when(mouvementStockRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(mockPage);
        when(mouvementStockMapper.toDTO(mouvement)).thenReturn(dto);

        Page<MouvementStockDTO> result = stockService.searchMouvements(
                null, null, produitId, null, null, null, pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(mouvementStockRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void testSearchMouvementsParTypeMouvement() {
        TypeMouvement type = TypeMouvement.SORTIE;
        Pageable pageable = PageRequest.of(0, 10);
        
        MouvementStock mouvement = new MouvementStock();
        Page<MouvementStock> mockPage = new PageImpl<>(Arrays.asList(mouvement));
        MouvementStockDTO dto = new MouvementStockDTO();
        
        when(mouvementStockRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(mockPage);
        when(mouvementStockMapper.toDTO(mouvement)).thenReturn(dto);

        Page<MouvementStockDTO> result = stockService.searchMouvements(
                null, null, null, null, type, null, pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(mouvementStockRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void testSearchMouvementsParNumeroLot() {
        String numeroLot = "LOT-2025-001";
        Pageable pageable = PageRequest.of(0, 10);
        
        MouvementStock mouvement = new MouvementStock();
        Page<MouvementStock> mockPage = new PageImpl<>(Arrays.asList(mouvement));
        MouvementStockDTO dto = new MouvementStockDTO();
        
        when(mouvementStockRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(mockPage);
        when(mouvementStockMapper.toDTO(mouvement)).thenReturn(dto);

        Page<MouvementStockDTO> result = stockService.searchMouvements(
                null, null, null, null, null, numeroLot, pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(mouvementStockRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void testSearchMouvementsCombineMultiCriteres() {
        LocalDate dateDebut = LocalDate.of(2025, 1, 1);
        String reference = "PROD001";
        TypeMouvement type = TypeMouvement.ENTREE;
        Pageable pageable = PageRequest.of(0, 20);
        
        MouvementStock mouvement = new MouvementStock();
        Page<MouvementStock> mockPage = new PageImpl<>(Arrays.asList(mouvement));
        MouvementStockDTO dto = new MouvementStockDTO();
        
        when(mouvementStockRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(mockPage);
        when(mouvementStockMapper.toDTO(mouvement)).thenReturn(dto);

        Page<MouvementStockDTO> result = stockService.searchMouvements(
                dateDebut, null, null, reference, type, null, pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(mouvementStockRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void testSearchMouvementsAvecPagination() {
        Pageable pageable = PageRequest.of(1, 5);
        
        List<MouvementStock> mouvements = Arrays.asList(
                new MouvementStock(), new MouvementStock(), new MouvementStock());
        Page<MouvementStock> mockPage = new PageImpl<>(mouvements, pageable, 15);
        
        when(mouvementStockRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(mockPage);
        when(mouvementStockMapper.toDTO(any(MouvementStock.class)))
                .thenReturn(new MouvementStockDTO());

        Page<MouvementStockDTO> result = stockService.searchMouvements(
                null, null, null, null, null, null, pageable);

        assertNotNull(result);
        assertEquals(3, result.getContent().size());
        assertEquals(15, result.getTotalElements());
        assertEquals(1, result.getNumber());
        assertEquals(5, result.getSize());
    }
}