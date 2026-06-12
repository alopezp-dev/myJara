package com.myjara.backend.repository;

import com.myjara.backend.entity.Cie10Catalog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface Cie10CatalogRepository extends JpaRepository<Cie10Catalog, String> {

    // Búsqueda por código o descripción
    @Query("SELECT c FROM Cie10Catalog c WHERE " +
            "LOWER(c.code) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
            "LOWER(c.description) LIKE LOWER(CONCAT('%', :term, '%'))")
    List<Cie10Catalog> search(@Param("term") String term);

    List<Cie10Catalog> findByCategory(String category);
}