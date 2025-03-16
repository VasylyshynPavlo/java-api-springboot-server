package org.example.repository;

import org.example.entities.ProductImageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface IProductImageRepository extends JpaRepository<ProductImageEntity, Integer> {

    @Modifying
    @Transactional
    @Query("DELETE FROM ProductImageEntity p WHERE p.name = :name")
    void deleteByName(String name);

    @Query("SELECT MAX(p.priority) FROM ProductImageEntity p WHERE p.product.id = :productId")
    Integer findMaxPriorityByProductId(Integer productId);

    @Modifying
    @Transactional
    @Query("UPDATE ProductImageEntity p SET p.priority = :priority WHERE p.name = :name")
    void updatePriorityByName(@Param("name") String name, @Param("priority") Integer priority);
}