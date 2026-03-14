package com.englishmovies.server.comedy.repository;

import com.englishmovies.server.comedy.domain.entity.ComedySpecialEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface ComedySpecialRepository extends JpaRepository<ComedySpecialEntity, Long> {
    @Query(value = "SELECT * FROM englishmovies.comedy_specials ORDER BY RANDOM() LIMIT :limit", nativeQuery = true)
    List<ComedySpecialEntity> findRandomSpecials(int limit);

    Optional<ComedySpecialEntity> findByContentKey(String contentKey);
}
