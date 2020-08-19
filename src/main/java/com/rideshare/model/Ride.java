package com.rideshare.model;


import java.time.LocalDateTime;
import java.util.List;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.OneToMany;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.springframework.format.annotation.DateTimeFormat;

@Entity
@Getter
@Setter
@ToString
@Builder(toBuilder = true)
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)

public class Ride {

  @Id
  @GeneratedValue(strategy= GenerationType.IDENTITY)
  private Long id;
  private String userName;
  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  private LocalDateTime rideDate;
  @Lob
  private String details;
  private int availSeat;
  private int occupiedSeat;
  @Enumerated(EnumType.STRING)
  private RideStatus status;

  @Enumerated(EnumType.STRING)
  private PublishStatus publishStatus;

  @OneToMany(mappedBy = "rideId",fetch = FetchType.LAZY, orphanRemoval = true)
  @Cascade(CascadeType.ALL)
  private List<Passenger> passengers;

  private String departure;

  private String arrival;

  @Lob
  private String route;

}
