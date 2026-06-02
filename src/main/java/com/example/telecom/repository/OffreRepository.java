package com.example.telecom.repository;

import com.example.telecom.entity.Offre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OffreRepository extends JpaRepository<Offre, Long> {

    @Query("""
            select distinct o
            from Offre o
            left join fetch o.planTarifaire
            left join fetch o.services
            order by o.id desc
            """)
    List<Offre> findAllWithCatalogDetails();
}
