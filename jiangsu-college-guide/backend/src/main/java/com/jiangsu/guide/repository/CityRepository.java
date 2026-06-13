package com.jiangsu.guide.repository;

import com.jiangsu.guide.entity.City;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CityRepository extends JpaRepository<City, Long> {
    List<City> findAllByOrderBySortOrderAsc();
    Optional<City> findByName(String name);
}
