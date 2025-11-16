package com.tricol.stock.specification;

import com.tricol.stock.entity.MouvementStock;
import com.tricol.stock.enums.TypeMouvement;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

public class MouvementStockSpecification {

    public static Specification<MouvementStock> hasDateBetween(LocalDate dateDebut, LocalDate dateFin) {
        return (root, query, criteriaBuilder) -> {
            if (dateDebut != null && dateFin != null) {
                return criteriaBuilder.between(root.get("dateMouvement"), dateDebut, dateFin);
            } else if (dateDebut != null) {
                return criteriaBuilder.greaterThanOrEqualTo(root.get("dateMouvement"), dateDebut);
            } else if (dateFin != null) {
                return criteriaBuilder.lessThanOrEqualTo(root.get("dateMouvement"), dateFin);
            }
            return null;
        };
    }

    public static Specification<MouvementStock> hasProduitId(Long produitId) {
        return (root, query, criteriaBuilder) -> 
            produitId == null ? null : criteriaBuilder.equal(root.get("produit").get("id"), produitId);
    }

    public static Specification<MouvementStock> hasProduitReference(String reference) {
        return (root, query, criteriaBuilder) -> 
            reference == null ? null : criteriaBuilder.equal(root.get("produit").get("reference"), reference);
    }

    public static Specification<MouvementStock> hasTypeMouvement(TypeMouvement type) {
        return (root, query, criteriaBuilder) -> 
            type == null ? null : criteriaBuilder.equal(root.get("typeMouvement"), type);
    }

    public static Specification<MouvementStock> hasNumeroLot(String numeroLot) {
        return (root, query, criteriaBuilder) -> 
            numeroLot == null ? null : criteriaBuilder.equal(root.get("lot").get("numeroLot"), numeroLot);
    }
}