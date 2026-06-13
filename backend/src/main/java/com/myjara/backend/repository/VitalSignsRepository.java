package com.myjara.backend.repository;

import com.myjara.backend.entity.VitalSigns;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VitalSignsRepository extends JpaRepository<VitalSigns, Long> {

    List<VitalSigns> findByEpisodeIdOrderByRecordedAtDesc(Long episodeId);
}