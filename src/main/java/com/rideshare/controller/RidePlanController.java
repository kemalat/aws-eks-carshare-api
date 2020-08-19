package com.rideshare.controller;

import com.rideshare.exception.NoAvailableSeatException;
import com.rideshare.exception.ReservationExistException;
import com.rideshare.exception.RideOfferNotFoundException;
import com.rideshare.model.City;
import com.rideshare.model.Passenger;
import com.rideshare.model.Ride;
import com.rideshare.service.RidePlanService;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@Slf4j
public class RidePlanController {

    @Autowired
    private RidePlanService ridePlanService;

    /**
     * Persist new ride share offer by user to database.
     *
     *
     * @param ride Ride Entity Object
     * @return If saved successfully, returns {@link Ride}, and HttpStatus.OK
     *         If parameter is not valid (such as string instead int), returns Null, and HttpStatus.BAD_REQUEST
     *         If not found (empty Product), returns Null, and HttpStatus.NOT_FOUND
     *         If any exception occurs, returns Null, and HttpStatus.EXPECTATION_FAILED

     @throws org.springframework.http.converter.HttpMessageNotReadableException if required request body is missing
     */
    @PostMapping("/ride-share/plans")
    public ResponseEntity<Ride> createRide(@RequestBody Ride ride) {

        log.info("Save [ride:{}]", ride);

        AtomicReference<ResponseEntity<Ride>> result = new AtomicReference<>();

        try {

            ridePlanService.saveRidePlan(String.valueOf(ride.getId()), ride)
                .ifPresent(updatedProduct -> {
                    log.info("Saved : {}", updatedProduct);

                    result.set(new ResponseEntity<>(updatedProduct, HttpStatus.OK));
                });

            return result.get();

        } catch (IllegalArgumentException iae) {
            log.error("Bad request [ride:{}]", ride);

            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (Exception e) {

            log.error("Exception [ride:{}] : {}", ride, e.getMessage());

            return new ResponseEntity<>(HttpStatus.EXPECTATION_FAILED);
        }
    }

    /**
     * Updates ride share offer publish status
     *
     *
     * @param rideId rideId as unique identifying id of each ride offer record.
     * @return If updated successfully, returns HttpStatus.OK
     *         If parameter is not valid, returns HttpStatus.BAD_REQUEST
     *         If not found (update is not successful), returns HttpStatus.NOT_FOUND
     *         If any exception occurs, returns Null, and HttpStatus.EXPECTATION_FAILED

     @throws org.springframework.web.method.annotation.MethodArgumentTypeMismatchException if failed to convert path variable
     */
    @PutMapping("ride-share/{rideId}/publish")
    public ResponseEntity<Void> publishRide(@PathVariable("rideId") long rideId) {

        AtomicReference<ResponseEntity<Void>> result = new AtomicReference<>();
        try {

            Optional.of(rideId)
                .ifPresent(id1 -> {
                    ridePlanService.publishRide(rideId).ifPresent(updated ->{
                        log.info("Updated : {}", updated);
                        if(updated > 0)
                            result.set(new ResponseEntity<>(HttpStatus.OK));
                        else
                            result.set(new ResponseEntity<>(HttpStatus.NOT_FOUND));
                    });
                });

            return result.get();

        } catch (IllegalArgumentException iae) {
            log.error("Bad request [rideId:{}]", rideId);

            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

        } catch (Exception e) {
            log.error("Exception [rideId:{}] : {}", rideId, e.getMessage());

            return new ResponseEntity<>(HttpStatus.EXPECTATION_FAILED);
        }

    }

    /**
     * Updates ride share offer publish status
     *
     *
     * @param rideId rideId as unique identifying id of each ride offer record.
     * @return If updated successfully, returns HttpStatus.OK
     *         If parameter is not valid, returns HttpStatus.BAD_REQUEST
     *         If not found (update is not successful), returns HttpStatus.NOT_FOUND
     *         If any exception occurs, returns HttpStatus.EXPECTATION_FAILED

     @throws org.springframework.web.method.annotation.MethodArgumentTypeMismatchException if failed to convert path variable
     */
    @PutMapping("ride-share/{rideId}/unpublish")
    public ResponseEntity<Void> unpublishRide(@PathVariable("rideId") long rideId) {
        AtomicReference<ResponseEntity<Void>> result = new AtomicReference<>();
        try {

            Optional.of(rideId)
                .ifPresent(id1 -> {
                    ridePlanService.unpublishRide(rideId).ifPresent(updated ->{
                        log.info("Updated : {}", updated);
                        if(updated > 0)
                            result.set(new ResponseEntity<>(HttpStatus.OK));
                        else
                            result.set(new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR));
                    });
                });

            return result.get();

        } catch (IllegalArgumentException iae) {
            log.error("Bad request [rideId:{}]", rideId);

            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

        } catch (Exception e) {
            log.error("Exception [rideId:{}] : {}", rideId, e.getMessage());

            return new ResponseEntity<>(HttpStatus.EXPECTATION_FAILED);
        }

    }

    /**
     * Get offered ride list by departure and arrival points including other
     * ride routes that are on the way of searched route
     *
     * @param departure departure city name
     * @param arrival arrival city name
     * @return If ride found on requested route, returns {@link List<Ride>}, and HttpStatus.OK
     *         If no record found, returns null, and HttpStatus.NO_CONTENT
     *         If any exception occurs, returns null, and HttpStatus.EXPECTATION_FAILED
     * @throws org.springframework.web.bind.MissingServletRequestParameterException if Required request parameter(s) not present
     */
    @GetMapping("/ride-share/proposed-plans")
    public ResponseEntity<List<Ride>> searchRideByDeptArrival(@RequestParam("departure") String departure,@RequestParam("arrival") String arrival) {
        try {

            AtomicReference<ResponseEntity<List<Ride>>> result = new AtomicReference<>();

            ridePlanService.getRidesByDeptArrival(departure,arrival).ifPresent(rides -> {
                log.info("Returned [ departure:{}, arrival:{}] : {}"
                    , departure,arrival,rides);

                result.set(new ResponseEntity<>(rides, HttpStatus.OK));

            });
            return result.get();

        } catch (NullPointerException npe) {
            log.warn(npe.getMessage());
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            log.error("Exception [departure:{}, arrival:{}] : {}", departure, arrival, e.getMessage());

            return new ResponseEntity<>(HttpStatus.EXPECTATION_FAILED);
        }
    }


    /**
     * Get username and rideId, finds out the ride offer record. if it is vacant and published status, add record to
     * {@link Passenger} table, increases the occupied seat column value and returns JSON object as string
     *
     * @param jsonString Json String which has username and rideId
     * @return If passenger is added to ride successfully,  returns JSON string, and HttpStatus.OK
     *         If no ride found, returns null, and HttpStatus.EXPECTATION_FAILED
     *         If no available seat, returns null, and HttpStatus.EXPECTATION_FAILED
     *         If already reservation exists, returns null, and HttpStatus.EXPECTATION_FAILED
     *         If JSON string has a bad format, returns null, and HttpStatus.BAD_REQUEST
     @throws org.springframework.http.converter.HttpMessageNotReadableException if required request body is missing
     */
    @PostMapping(value="ride-share/join", produces= MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String>  joinRequest(@RequestBody String jsonString) {
        AtomicReference<ResponseEntity<String>> result = new AtomicReference<>();
        JSONParser parser = new JSONParser(JSONParser.DEFAULT_PERMISSIVE_MODE);
        try {
            JSONObject json = (JSONObject) parser.parse(jsonString);
            String userName = json.getAsString("userName");
            long rideId = json.getAsNumber("rideId").longValue();
            Optional<String> reservationResult = ridePlanService.rideJoinRequest(rideId,userName);
            reservationResult.ifPresent(jsonResult ->{
                log.info("Saved : {}", jsonResult);
                result.set(new ResponseEntity<>(jsonResult, HttpStatus.OK));

            });
            return result.get();

        } catch (ParseException e) {
            log.error("Bad request [jsonString:{}] :{}", jsonString,e);
            return new ResponseEntity<>(e.getMessage(),HttpStatus.BAD_REQUEST);
        } catch (NoAvailableSeatException | ReservationExistException | RideOfferNotFoundException e) {
            log.error(e.getMessage(),e);
            JSONObject jsonObject = new JSONObject();
            jsonObject.appendField("error", e.getMessage());
            return new ResponseEntity<>(jsonObject.toJSONString(),HttpStatus.EXPECTATION_FAILED);
        }
    }

    /**
     * Creates matrix which is the presentation of island country with cities.
     * {@link City} table updated with unique City names and city coordinates as matrix element indices(m,n)
     *
     * @param jsonString Json String which holds row and column
     * @return If JSON is valid and matrix records created, returns HttpStatus.OK
     *         If already reservation exists, returns null, and HttpStatus.EXPECTATION_FAILED
     *         If JSON string has a bad format, returns HttpStatus.BAD_REQUEST
     *         If any exception occurs, returns  HttpStatus.EXPECTATION_FAILED
     @throws org.springframework.http.converter.HttpMessageNotReadableException if required request body is missing
     */
    @PostMapping("ride-share/create-map")
    public ResponseEntity<Void>  createMap(@RequestBody String jsonString) {
        AtomicReference<ResponseEntity<Void>> result = new AtomicReference<>();
        JSONParser parser = new JSONParser(JSONParser.DEFAULT_PERMISSIVE_MODE);
        try {
            JSONObject json = (JSONObject) parser.parse(jsonString);
            int row = (int)json.getAsNumber("row");
            int column = (int)json.getAsNumber("column");
            ridePlanService.creteMap(row, column).ifPresent(cities ->{
                log.info("Saved : {}", cities);
                result.set(new ResponseEntity<>(HttpStatus.OK));

            });
            return result.get();

        } catch (ParseException e) {
            log.error("Bad request [jsonString:{}]", jsonString);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (NumberFormatException  e) {
            log.error("Exception [jsonString:{}] : {}", jsonString, e.getMessage());
            log.error(e.getMessage(),e);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (Exception  e) {
            log.error("Exception [jsonString:{}] : {}", jsonString, e.getMessage());
            return new ResponseEntity<>(HttpStatus.EXPECTATION_FAILED);
        }

    }

    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<String> handleNullPointerException(NullPointerException npe) {
        return new ResponseEntity<>(npe.getMessage(),HttpStatus.EXPECTATION_FAILED);
    }


}
