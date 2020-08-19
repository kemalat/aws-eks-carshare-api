package com.rideshare.repository;


import com.rideshare.model.PublishStatus;
import com.rideshare.model.Ride;
import com.rideshare.model.RideStatus;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface RideRepository extends JpaRepository<Ride, Long> {

  @Modifying
  @Transactional
  @Query("UPDATE Ride r SET r.publishStatus = :publishStatus WHERE r.id = :id")
  int updateRidePublishState(@Param("publishStatus") PublishStatus publishStatus,
      @Param("id") long id);

  @Modifying
  @Transactional
  @Query("UPDATE Ride r SET r.status = :rideStatus WHERE r.id = :id")
  void updateRideState(@Param("rideStatus") RideStatus rideStatus,
      @Param("id") long id);

  @Modifying
  @Transactional
  @Query("UPDATE Ride r SET r.occupiedSeat = 1 + r.occupiedSeat WHERE r.id = :id")
  void addPassenger(@Param("id") long id);


  @Query("SELECT r FROM Ride r WHERE r.departure = :departure AND r.arrival = :arrival AND r.publishStatus = :publishStatus "
      + "AND r.status = :rideStatus")
  List<Ride> findRidesByStartEnd(
      @Param("departure") String departure,
      @Param("arrival") String arrival,
      @Param("publishStatus") PublishStatus publishStatus,
      @Param("rideStatus") RideStatus rideStatus, Pageable pageable);

  @Query("SELECT r FROM Ride r WHERE r.route = :route AND r.publishStatus = :publishStatus "
      + "AND r.status = :rideStatus")
  Ride findRidesByRoute(
      @Param("route") String route,
      @Param("publishStatus") PublishStatus publishStatus,
      @Param("rideStatus") RideStatus rideStatus);

}


