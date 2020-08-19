package com.rideshare.service;


import com.rideshare.model.Point;
import com.rideshare.exception.ReservationExistException;
import com.rideshare.exception.NoAvailableSeatException;
import com.rideshare.repository.PassengerRepository;
import com.rideshare.model.PublishStatus;
import com.rideshare.repository.RideRepository;
import com.rideshare.model.City;
import com.rideshare.repository.CityRepository;
import com.rideshare.model.Passenger;
import com.rideshare.model.Ride;
import com.rideshare.exception.RideOfferNotFoundException;
import com.rideshare.model.RideStatus;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class RidePlanService {


  @Autowired
  private RideRepository rideRepository;
  @Autowired
  private CityRepository cityRepository;

  @Autowired
  private PassengerRepository passengerRepository;

  /**
   * Sets ride and ride offer publish statuses, calculate routes from departure to arrival
   * by traversing on the matrix from source to destination as JSON serialized {@link List<Point>}
   *
   *
   * @param ride Ride Entity Object
   * @return If Optional<Ride>

   @throws JsonProcessingException if serialization of {@link List<Point>} fails
   */
  public Optional<Ride> saveRidePlan(String s, Ride ride) throws JsonProcessingException {
    ride.setStatus(RideStatus.VACANT);
    ride.setPublishStatus(PublishStatus.PUBLISHED);

    Optional<String> optionalSerializedPointList = null;
    optionalSerializedPointList = calculateRoute(ride.getDeparture(), ride.getArrival());
    if(optionalSerializedPointList.isPresent())
      ride.setRoute(optionalSerializedPointList.get());

    Ride updatedRide = rideRepository.save(ride);
    return Optional.of(updatedRide);

  }

  /**
   * Update the ride publish state enum value and returns number of rows effected.
   *
   * @param rideId as unique long identifier of each ride offer.
   * @return If Optional<Integer>
   */
  public Optional<Integer> publishRide(long rideId) {
     int updated = rideRepository.updateRidePublishState(PublishStatus.PUBLISHED,rideId);
    return Optional.of(updated);
  }

  /**
   * Update the ride publish state enum value and returns number of rows effected.
   *
   * @param rideId as unique long identifier of each ride offer.
   * @return If Optional<Integer>
   */
  public Optional<Integer> unpublishRide(long rideId) {
    int updated = rideRepository.updateRidePublishState(PublishStatus.UNPUBLISHED,rideId);
    return Optional.of(updated);
  }

  /**
   * Query one record from Ride table with departure, arriaval among the published and vaccant offers.
   * Generate List<Point> from the route column value. Generate all subsets of the List<Point>. For each subset performs
   * query on Ride table , adds each returned Ride to List<Ride>
   *
   * @param departure as city name
   * @param arrival as city name
   * @return If Optional<List<Ride>>
   *
   * @throws JsonProcessingException if reading the string value of {@link List<Point>} fails
   */
  public  Optional<List<Ride>>  getRidesByDeptArrival(String departure, String arrival) throws JsonProcessingException {
    List<Ride> rides = rideRepository.findRidesByStartEnd(departure, arrival, PublishStatus.PUBLISHED,RideStatus.VACANT,
        PageRequest.of(0,1));

    if (rides.isEmpty())
      throw new NullPointerException("No content for [departure:{}, arrival:{}]" +departure +","+arrival);
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
    List<Point> pointList = objectMapper.readValue(rides.get(0).getRoute(),new TypeReference<List<Point>>(){});

    List<List<Point>> listOfListofNode = getAllSubsets(pointList);

    List<Ride> allRoutes = new ArrayList<>();
    for (List<Point> listPoint :listOfListofNode) {
      Ride ride = rideRepository.findRidesByRoute(objectMapper.writeValueAsString(listPoint), PublishStatus.PUBLISHED,RideStatus.VACANT);
      if(!Objects.isNull(ride))
        allRoutes.add(ride);
    }
    return Optional.of(allRoutes);

  }

  /**
   * Query Ride table with PK value (rideId), checks the status of Ride Offer, checks whether or not
   * the same user name reserved the ride before. If there is available place inserts record to Passenger table and returns
   * JSON object with information
   *
   * @param rideId as city name
   * @param userName as city name
   * @return If Optional<String> as JSON string
   *
   * @throws NoAvailableSeatException if occupied seats is greater or equals to available seats, updates the vacant status
   */
  public Optional<String> rideJoinRequest(long rideId, String userName) throws NoAvailableSeatException {
    Optional<Ride> optionalRide = rideRepository.findById(rideId);
    if(optionalRide.isEmpty()) {
      throw new RideOfferNotFoundException("No ride plan record found");
    }
    Ride ride = optionalRide.get();
    if(ride.getStatus() != RideStatus.VACANT)
      throw new NoAvailableSeatException("No Available Seat");

    if(ride.getPassengers().stream().filter(passenger -> passenger.getUserName().equals(userName)).findFirst().isPresent())
      throw new ReservationExistException("Already reservation done");

    if(ride.getOccupiedSeat() < ride.getAvailSeat()) {
      rideRepository.addPassenger(rideId);
      Passenger passenger = Passenger.builder().userName(userName).
          rideId(rideId).createdDate(LocalDateTime.now()).build();
      passengerRepository.save(passenger);
      JSONObject jsonObject = new JSONObject();
      jsonObject.appendField("rideShareOfferedBy", ride.getUserName());
      jsonObject.appendField("passenger", userName);
      jsonObject.appendField("reservationDate", LocalDateTime.now().toString());
      return Optional.of(jsonObject.toJSONString());
    }
    else {
      rideRepository.updateRideState(RideStatus.NO_SEAT,rideId);
      throw new NoAvailableSeatException("No Available Seat");
    }

  }

  /**
   * Insert records to CITY table by creating matrix indices and unique City name with given row * column
   *
   * @param row
   * @param column
   * @return Optional<List<City>>
   *
   */
  public Optional<List<City>> creteMap(int row, int column) {

    int[][] maze = new int[row][column];
    int index = 1;
    List<City> cityList = new ArrayList<>();
    for (int i = 0; i < row; i++) {
      for (int j = 0; j < column; j++) {
        maze[i][j] = 1;
        System.out.println(i+","+j);
        cityList.add(City.builder().m(i).n(j).name("City-".concat(String.valueOf(index))).build());
        index++;
      }

    }
    List<City> updated = cityRepository.saveAll(cityList);
    return Optional.of(updated);

  }

  /**
   * Calculates the path from source point to dest point on the matrix, saves each matrix point travelled to list.
   * Converts this list to JSON string for storing the database
   *
   * @param dept
   * @param arrival
   * @return Optional<String> JSON string of List<Point>
   *
   */
  private Optional<String> calculateRoute (String dept, String arrival) throws JsonProcessingException {

    Optional<City> optSource = cityRepository.findById(dept);
    Optional<City> optDest = cityRepository.findById(arrival);

    Point source = new Point(optSource.get().getM(),optSource.get().getN());
    Point dest = new Point(optDest.get().getM(),optDest.get().getN());

    List<Point> list = new ArrayList<>();
    int i = 0;

    if(Math.max(source.x, dest.x) == source.x) {
      for (i = source.x - 1; i >= dest.x; i--) {
        list.add(new Point(i,source.y));
      }
    }
    else {
      for (i = source.x +1; i <= dest.x; i++) {
        list.add(new Point(i,source.y));
      }
    }
    if(Math.max(source.y, dest.y) == source.y) {
      for (i = source.y - 1; i >= dest.y; i--) {
        list.add(new Point(dest.x,i));

      }
    }
    else {
      for (i = source.y + 1; i <= dest.y; i++) {
        list.add(new Point(dest.x,i));
      }
    }

    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
    return Optional.of(objectMapper.writeValueAsString(list));
  }

  /**
   * Calculates the all subsets of the given List<Point> and returns as 2D list array
   *
   * @param input
   * @return List<List<Point>>
   *
   */
  private List<List<Point>> getAllSubsets(List<Point> input) {
    int allMasks = 1 << input.size();
    List<List<Point>> output = new ArrayList<>();
    for(int i=0;i<allMasks;i++) {
      List<Point> sub = new ArrayList<>();
      for(int j=0;j<input.size();j++) {
        if((i & (1 << j)) > 0) {
          sub.add(input.get(j));
        }
      }
      output.add(sub);
    }

    return output;
  }
}
