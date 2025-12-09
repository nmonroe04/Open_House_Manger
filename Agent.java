package org.finalproject.system;

	import java.util.ArrayList;
	import java.util.*;
	import java.time.LocalDateTime;
	import java.io.Serializable;

	public class Agent extends Person implements Serializable {


	  //private String name;
	  //private String email;
	  //private int phoneNumber;
	  private ArrayList<House> properties;
	  private ArrayList<Event> events;

	  public Agent() {
	    super();
	    this.properties = new ArrayList<House>();
	    this.events = new ArrayList<Event>();
	    
	  }

	  public Agent(String name, String email, String phone, String username, String password) {
	    super(name, email, phone, username, password);
	    this.properties = new ArrayList<House>();
	    this.events = new ArrayList<Event>();
	  }



	  

	public ArrayList<House> getProperties() {
	        return properties;
	}


	public void setProperties(ArrayList<House> properties) {
	        this.properties = properties;
	}

	public void addProperty(House house) {
	  if (house != null && !this.properties.contains(house)){
	    this.properties.add(house);
	  }
	}

	public Event createEvent(House house,
	                           LocalDateTime startTime,
	                           int capacity,
	                           int checkInCode) {

	      String eventId = "EVT-" + (events.size() + 1);
	      Event event = new Event(eventId, startTime, this,           house, capacity, checkInCode);
	      events.add(event);
	      house.addEvent(event);
	      return event;
	  }



	  
	public void addEvent(Event event) {  //Action USE 1
	  if (event != null && !this.events.contains(event)){
	    for (Event e : this.events){
	      if (event.getTime() == e.getTime()){
	        System.out.println("There is already an event at this time.");
	        return;
	      }
	    }
	    this.events.add(event);
	  } else {
	    System.out.println("Invalid event or event already exists.");
	  }
	}

	public boolean login(String username, String password){
	  if (this.getName().equals(username) && this.getPassword().equals(password)) {    
	      System.out.println("Login successful!");
	      return true;
	  }
	  else {
	      System.out.println("Invalid username or password.");
	      return false;
	  }
	} 


	public ArrayList<Email> prepareEmailsForEvent(Event event, String subject, String bodyTemplate) {
		ArrayList<Email> emails = new ArrayList<>();

		if (event == null) {
		System.out.println("Invalid event.");
		return emails;
		}

		int counter = 1;
		for (Visitor visitor : event.getVisitors()) {
		// Condition 1: consent
		if (visitor.isMailingConsent()) {
		String id = event.getEventId() + "-MSG-" + counter++;

		String personalizedBody = bodyTemplate.replace("{name}", visitor.getName());

		Email email = new Email(
		id,
		this.getEmail(), // from agent
		visitor.getEmail(), // to visitor
		subject,
		personalizedBody
		);
		emails.add(email);
		}
		}

		return emails;
		}

		// NEW: send to only SELECTED visitors who ALSO consent
		public ArrayList<Email> prepareEmailsForEvent(Event event, String subject, String bodyTemplate, java.util.List<Visitor> recipients) {
		ArrayList<Email> emails = new ArrayList<>();

		if (event == null) {
		System.out.println("Invalid event.");
		return emails;
		}
		if (recipients == null || recipients.isEmpty()) {
		System.out.println("No recipients selected.");
		return emails;
		}

		int counter = 1;
		for (Visitor visitor : recipients) {
		// Condition 1: they were selected (because they're in `recipients`)
		// Condition 2: they have consent
		if (visitor.isMailingConsent()) {
		String id = event.getEventId() + "-MSG-" + counter++;

		String personalizedBody = bodyTemplate.replace("{name}", visitor.getName());

		Email email = new Email(
		id,
		this.getEmail(), // from agent
		visitor.getEmail(), // to visitor
		subject,
		personalizedBody
		);
		emails.add(email);
		}
		}

		return emails;
		}



	  public void sendEmailtoVisitors(Event event, String subject, String bodyTemplate) {
	      if (event == null) {
	          System.out.println("Invalid event.");
	          return;
	      }

	      System.out.println("Preparing emails for visitors of event at " 
	              + event.getAddress() + " at " + event.getTime() + " on " + event.getDate() + ".");

	      // 1) Prepare Email objects
	      ArrayList<Email> emails = prepareEmailsForEvent(event, subject, bodyTemplate);

	      // 2) "Send" them (here just print + update visitor status)
	      for (Email email : emails) {
	          System.out.println("Sending email to: " + email.getTo());
	          // Find the corresponding Visitor so we can set their mail status
	          for (Visitor visitor : event.getVisitors()) {
	              if (visitor.getEmail().equals(email.getTo())) {
	                  visitor.setMailStatus("Sent");
	              }
	          }
	      }

	      // Handle visitors who opted out (optional, but keeps your old behavior)
	      for (Visitor visitor : event.getVisitors()) {
	          if (!visitor.isMailingConsent()) {
	              System.out.println("Visitor: " + visitor.getName() + " has opted out of mailing list.");
	          }
	      }
	  }

	  public boolean validateEvent(Event event) {
	    if (event == null) {
	      return false;
	    }
	    if (event.getHouse() == null) {
	      return false;
	    }
	    if (event.getCapacity() <= 0) {
	      return false;
	    }
	    return true;
	  }

	  // --- RSVP Methods ---

	  public Email prepareRsvpConfirmationEmail(Event event, Visitor visitor, RSVPStatus status, 
	                                             String subject, String bodyTemplate) {
	    if (event == null || visitor == null || status == null) {
	      return null;
	    }

	    String id = event.getEventId() + "-RSVP-" + visitor.getName().replace(" ", "");
	    String personalizedBody = bodyTemplate
	        .replace("{name}", visitor.getName())
	        .replace("{status}", status.toString())
	        .replace("{event}", event.getAddress())
	        .replace("{date}", event.getDate())
	        .replace("{time}", event.getTime().toLocalTime().toString());

	    return new Email(id, this.getEmail(), visitor.getEmail(), subject, personalizedBody);
	  }

	  public ArrayList<Email> prepareReminderEmails(Event event, String subject, String bodyTemplate) {
	    ArrayList<Email> emails = new ArrayList<>();
	    
	    if (event == null) {
	      System.out.println("Invalid event.");
	      return emails;
	    }

	    // Send reminders to MAYBE and NO_RESPONSE visitors
	    List<Visitor> maybeList = event.getRsvpList(RSVPStatus.MAYBE);
	    List<Visitor> noResponseList = event.getRsvpList(RSVPStatus.NO_RESPONSE);

	    int counter = 1;
	    for (Visitor visitor : maybeList) {
	      if (visitor.hasMailingListConsent()) {
	        String id = event.getEventId() + "-REMINDER-" + counter++;
	        String personalizedBody = bodyTemplate
	            .replace("{name}", visitor.getName())
	            .replace("{event}", event.getAddress())
	            .replace("{date}", event.getDate())
	            .replace("{time}", event.getTime().toLocalTime().toString());
	        emails.add(new Email(id, this.getEmail(), visitor.getEmail(), subject, personalizedBody));
	      }
	    }

	    for (Visitor visitor : noResponseList) {
	      if (visitor.hasMailingListConsent()) {
	        String id = event.getEventId() + "-REMINDER-" + counter++;
	        String personalizedBody = bodyTemplate
	            .replace("{name}", visitor.getName())
	            .replace("{event}", event.getAddress())
	            .replace("{date}", event.getDate())
	            .replace("{time}", event.getTime().toLocalTime().toString());
	        emails.add(new Email(id, this.getEmail(), visitor.getEmail(), subject, personalizedBody));
	      }
	    }

	    return emails;
	  }

	  public void printRsvpSummary(Event event) {
	    if (event == null) {
	      System.out.println("Invalid event.");
	      return;
	    }

	    System.out.println("\n=== RSVP Summary for Event " + event.getEventId() + " ===");
	    System.out.println("Event: " + event.getAddress() + " on " + event.getDate());
	    System.out.println("Capacity: " + event.getCapacity());
	    System.out.println("YES: " + event.getRsvpCount(RSVPStatus.YES));
	    System.out.println("NO: " + event.getRsvpCount(RSVPStatus.NO));
	    System.out.println("MAYBE: " + event.getRsvpCount(RSVPStatus.MAYBE));
	    System.out.println("NO_RESPONSE: " + event.getRsvpCount(RSVPStatus.NO_RESPONSE));
	    System.out.println("Total Invites: " + event.getAllRsvps().size());
	    if (event.isOverbooked()) {
	      System.out.println("*** OVERBOOKED ***");
	    }
	    System.out.println("=====================================");
	  }

	}

