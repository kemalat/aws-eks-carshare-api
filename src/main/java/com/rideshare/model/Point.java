package com.rideshare.model;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;

@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Point implements Serializable {

  public int x;
  public int y;

  @Override
  public boolean equals(Object obj) {
    if( this.x == ((Point)obj).x && this.y == ((Point)obj).y)
      return true;
    else
      return false;
  }
}
