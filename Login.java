package org.finalproject.system;

import java.util.*;
import java.io.Serializable;


public class Login implements Serializable {

  ArrayList<Person> allPeople = new ArrayList<>();

  private String username;
  private String password;

  private String email;

  public void addPerson(Person person) {
    if (person != null) {
      allPeople.add(person);
    }
  }

  public Person login(String username, String password) {
    for (Person person : allPeople) {
      if (person.getName().equals(username) && person.getPassword().equals(password)) {
        System.out.println("Login successful!");
        return person;
      }
    }
    System.out.println("Invalid username or password.");
    return null;
  }
  
  //added this for GUI help
  public List<Person> getAllPeople() {
      return new ArrayList<>(allPeople);
  }
  
}
