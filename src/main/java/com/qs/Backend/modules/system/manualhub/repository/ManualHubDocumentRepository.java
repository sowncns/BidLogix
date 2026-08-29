package com.qs.Backend.modules.system.manualhub.repository;

import com.qs.Backend.modules.system.manualhub.entity.ManualHubDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ManualHubDocumentRepository extends JpaRepository<ManualHubDocument, Long> {

    // :keyword is cast explicitly because Postgres can't infer a type for an
    // untyped NULL bind parameter used inside concat()/lower() — without the
    // cast, listing with no keyword filter (the common case) fails with
    // "function lower(bytea) does not exist" the moment :keyword is null.
    @Query("""
            SELECT d FROM ManualHubDocument d
            WHERE (:keyword IS NULL OR lower(d.title) LIKE lower(concat('%', CAST(:keyword AS string), '%')))
              AND (:status IS NULL OR d.status = :status)
              AND (:productId IS NULL OR d.productId = :productId)
              AND (:parentId IS NULL OR d.parentId = :parentId)
              AND (:authorId IS NULL OR d.authorId = :authorId)
            """)
    Page<ManualHubDocument> search(
            @Param("keyword") String keyword,
            @Param("status") String status,
            @Param("productId") Long productId,
            @Param("parentId") Long parentId,
            @Param("authorId") Long authorId,
            Pageable pageable);

    List<ManualHubDocument> findByParentIdAndIsCurrentTrue(Long parentId);

    long countByStatus(String status);
}
