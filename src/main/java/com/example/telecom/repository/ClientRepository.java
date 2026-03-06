package com.example.telecom.repository;

import com.example.telecom.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {
    Optional<Client> findByCinNumber(String cinNumber);
    Optional<Client> findByPassportNumber(String passportNumber);
    Optional<Client> findByCustomerId(String customerId);
    boolean existsByCinNumber(String cinNumber);
    boolean existsByPassportNumber(String passportNumber);

    // Récupérer le dernier ID pour générer le prochain customer_id
    @Query("SELECT MAX(c.id) FROM Client c")
    Optional<Long> findMaxId();
}