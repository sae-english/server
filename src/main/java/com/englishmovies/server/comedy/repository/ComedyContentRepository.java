package com.englishmovies.server.comedy.repository;

import com.englishmovies.server.comedy.domain.entity.ComedyContentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ComedyContentRepository extends JpaRepository<ComedyContentEntity, Long> {
    List<ComedyContentEntity> findByComedySpecialIdOrderByPosition(Long comedySpecialId);

    Optional<ComedyContentEntity> findByComedySpecialIdAndBlockId(Long comedySpecialId, String blockId);
}
