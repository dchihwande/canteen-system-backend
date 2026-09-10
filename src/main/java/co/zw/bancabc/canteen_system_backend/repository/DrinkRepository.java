package co.zw.bancabc.canteen_system_backend.repository;

import co.zw.bancabc.canteen_system_backend.model.Drink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DrinkRepository extends JpaRepository<Drink, Long> {

    List<Drink> findByAvailableTrue();

    boolean existsByName(String name);
}