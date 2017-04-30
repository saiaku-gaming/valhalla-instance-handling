package com.valhallagame.instance_handling.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.valhallagame.instance_handling.model.MesosFramework;

public interface MesosRepository extends JpaRepository<MesosFramework, String> {
	public Optional<MesosFramework> findById(String id);
	
	@Query(value = "SELECT * FROM mesos_framework ORDER BY ts DESC LIMIT 1", nativeQuery = true)
	public Optional<MesosFramework> getLatestMesosFramework();
	
	@Query(value = "SELECT * FROM mesos_framework WHERE EXTRACT(EPOCH FROM (now() - ts)) < :failoverTimeout ORDER BY ts DESC LIMIT 1", nativeQuery = true)
	public Optional<MesosFramework> getLatestValidMesosFramework(@Param("failoverTimeout") double failoverTimeout);
}
