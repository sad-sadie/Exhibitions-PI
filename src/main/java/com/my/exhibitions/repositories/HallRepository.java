package com.my.exhibitions.repositories;

import com.my.exhibitions.entities.Hall;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;

@Repository
public interface HallRepository extends JpaRepository<Hall, Long> {
    Hall findById(long id);
    boolean existsByName(String name);

    @Modifying
    @Transactional
    @Query(nativeQuery = true,
            value = "DELETE FROM expositions WHERE hall_id = ?1")
    void deleteExpositionsWithHallId(long id);
}
