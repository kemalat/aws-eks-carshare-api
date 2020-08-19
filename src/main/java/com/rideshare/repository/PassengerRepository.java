package com.rideshare.repository;


import com.rideshare.model.Passenger;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PassengerRepository extends JpaRepository<Passenger, Long> {

  Optional<List<Passenger>> findPassengersByRideId(long clientId);

}
