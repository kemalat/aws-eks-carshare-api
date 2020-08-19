package com.rideshare;

import java.util.ArrayList;
import java.util.List;
import lombok.ToString;

// Java program to find path between two
// cell in matrix
class Path {

  @ToString
  static class Node {

    int x;
    int y;

    Node(int x, int y) {
      this.x = x;
      this.y = y;
    }

    @Override
    public boolean equals(Object obj) {
      if( this.x == ((Node)obj).x && this.y == ((Node)obj).y)
        return true;
      else
        return false;
    }
  }
  static int matrix[][] = {
      { 1, 3, 3,3 },
      { 3, 3, 3,3 },
      { 1, 3, 3,3 },
      { 3, 3, 3,3  } };



  public static void main(String[] args)
  {
    int[][] maze = new int[3][4];
    for (int i = 0; i < 3; i++) {
      for (int j = 0; j < 4; j++) {
        maze[i][j] = 1;
        System.out.println(i+","+j);
      }
    }
    
    //(0,0) - 0,1 - 0,2 - 1,2 - 2,2 - 3,2
    //(3,1) - 2,1- 1,1 - 0,1 - 0,0
    Node dest = new Node(3,4);
    Node source = new Node(2,1);

    List<Node> subset = new ArrayList<Node>();
    subset.add(new Node(0,3));
    subset.add(new Node(0,1));

    List<Node> list = new ArrayList<Node>();
    int i = 0;

    if(Math.max(source.x, dest.x) == source.x) {
      for (i = source.x - 1; i >= dest.x; i--) {
        System.out.println(i + "," + source.y);
        list.add(new Node(i,source.y));
      }
    }
    else {
      for (i = source.x +1; i <= dest.x; i++) {
        System.out.println(i + "," + source.y);
        list.add(new Node(i,source.y));
      }
    }
    if(Math.max(source.y, dest.y) == source.y) {
      for (i = source.y - 1; i >= dest.y; i--) {
        System.out.println(dest.x + "," + i);
        list.add(new Node(dest.x,i));

      }
    }
    else {
      for (i = source.y + 1; i <= dest.y; i++) {
        System.out.println(dest.x + "," + i);
        list.add(new Node(dest.x,i));
      }
    }

    System.out.println(list);
    System.out.println(subset);

    boolean contains = true;
    int l1 = list.size(), l2 = subset.size();
    int currIndex = 0;
    for(int j=0;j<l2;j++) {
      Node node2 = subset.get(j);
      for(i=currIndex;i<l1;i++) {
        if(node2.equals(list.get(i))) {
          break;
        }
      }
      if(i == l1) {
        contains = false;
        break;
      }
      currIndex++;
    }
    System.out.println(contains);
  }

}

/* This code is contributed by Madhu Priya */
