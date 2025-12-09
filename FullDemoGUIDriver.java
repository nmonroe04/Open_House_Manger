package org.finalproject.system;

import java.time.LocalDateTime;
import javax.swing.SwingUtilities;

public class FullDemoGUIDriver {

    public static void main(String[] args) {

        System.out.println("=== OPEN HOUSE MANAGER FULL DEMO DRIVER ===");

        // -------------------------------------------------
        // 1. Login system + demo agents
        // -------------------------------------------------
        Login login = new Login();

        // NOTE: Login.login(...) currently checks person.getName() as the username.
        // So the "username" to type on the login screen is actually the NAME below.
        Agent alice = new Agent(
                "Alice Agent",          // <-- username in GUI
                "alice@example.com",
                "555-1234",
                "alice",                // stored but not used by Login
                "password123"           // password in GUI
        );

        Agent noah = new Agent(
                "Noah Agent",           // <-- username in GUI
                "noah@example.com",
                "555-9876",
                "noah",
                "noahpass"
        );

        login.addPerson(alice);
        login.addPerson(noah);

        System.out.println("Login with:");
        System.out.println("  Username: Alice Agent   Password: password123");
        System.out.println("  Username: Noah Agent    Password: noahpass");

        // -------------------------------------------------
        // 2. Sample houses
        // -------------------------------------------------
        House house1 = new House(
                "123 Main St",
                500000,
                2000,
                3,
                2,
                1998,
                "Charming home with open floor plan."
        );

        House house2 = new House(
                "456 Oak Ave",
                750000,
                2500,
                4,
                3,
                2005,
                "Luxury home with pool and mountain views."
        );

        House house3 = new House(
                "789 Sunset Blvd",
                650000,
                2100,
                3,
                2,
                2012,
                "Modern home near downtown, great for entertaining."
        );

        house1.addImagePath("/org/finalproject/images/houses/mainhousefront.jpg");
        house1.addImagePath("/org/finalproject/images/houses/mainhouseinside.jpg");
        
        house2.addImagePath("/org/finalproject/images/houses/oakhousefront.jpg");
        house2.addImagePath("/org/finalproject/images/houses/oakhouseinside.jpg");

        // Give Alice two houses, Noah two (house1 & house3)
        alice.addProperty(house1);
        alice.addProperty(house2);

        noah.addProperty(house1);
        noah.addProperty(house3);

        // -------------------------------------------------
        // 3. Sample events with different states
        // -------------------------------------------------
        LocalDateTime now = LocalDateTime.now();

        // Event A (Alice, house1) - ACTIVE (visible in kiosk)
        LocalDateTime eATime = now.plusDays(1)
                                  .withHour(13).withMinute(0)
                                  .withSecond(0).withNano(0);

        Event eventA = alice.createEvent(
                house1,
                eATime,
                5,      // capacity
                1111    // check-in code
        );
        if (alice.validateEvent(eventA)) {
            eventA.schedule();
            eventA.activate();   // <- active & not closed => kiosk will show this
        } else {
            System.out.println("Warning: eventA failed validation.");
        }

        // Event B (Alice, house2) - SCHEDULED ONLY (agent can activate via GUI)
        LocalDateTime eBTime = now.plusDays(2)
                                  .withHour(10).withMinute(30)
                                  .withSecond(0).withNano(0);

        Event eventB = alice.createEvent(
                house2,
                eBTime,
                3,
                2222
        );
        if (alice.validateEvent(eventB)) {
            eventB.schedule();   // not activated yet
        } else {
            System.out.println("Warning: eventB failed validation.");
        }

        // Event C (Noah, house3) - CLOSED (shows in lists but not kiosk)
        LocalDateTime eCTime = now.minusDays(1)
                                  .withHour(14).withMinute(0)
                                  .withSecond(0).withNano(0);

        Event eventC = noah.createEvent(
                house3,
                eCTime,
                10,
                3333
        );
        if (noah.validateEvent(eventC)) {
            eventC.schedule();
            eventC.activate();
            eventC.close();      // closed past event
        } else {
            System.out.println("Warning: eventC failed validation.");
        }

        // -------------------------------------------------
        // 4. Visitors, RSVPs, mailing list flags
        // -------------------------------------------------
        Visitor v1 = new Visitor("Bob Visitor",   "bob@example.com",   "555-1111");
        Visitor v2 = new Visitor("Carol Visitor", "carol@example.com", "555-2222");
        Visitor v3 = new Visitor("Dave Visitor",  "dave@example.com",  "555-3333");
        Visitor v4 = new Visitor("Eve Visitor",   "eve@example.com",   "555-4444");
        Visitor v5 = new Visitor("Frank Visitor", "frank@example.com", "555-5555");

        // Mailing list consent (for Email panel)
        v1.setMailingListConsent(true);
        v2.setMailingListConsent(false);
        v3.setMailingListConsent(true);
        v4.setMailingListConsent(true);
        v5.setMailingListConsent(false);

        // Invite them to events and set RSVPs
        // Event A (active)
        eventA.addInvitee(v1);
        eventA.addInvitee(v2);
        eventA.addInvitee(v3);

        eventA.setRsvp(v1, RSVPStatus.YES);
        eventA.setRsvp(v2, RSVPStatus.MAYBE);
        // v3 stays NO_RESPONSE

        // Event B (scheduled only)
        eventB.addInvitee(v3);
        eventB.addInvitee(v4);
        eventB.addInvitee(v5);

        eventB.setRsvp(v3, RSVPStatus.NO);
        eventB.setRsvp(v4, RSVPStatus.YES);
        eventB.setRsvp(v5, RSVPStatus.YES);   // possibly overbook B (cap=3) if you add more later

        // Event C (closed past event)
        eventC.addInvitee(v1);
        eventC.addInvitee(v2);
        eventC.addInvitee(v5);

        eventC.setRsvp(v1, RSVPStatus.YES);
        eventC.setRsvp(v2, RSVPStatus.NO_RESPONSE);
        eventC.setRsvp(v5, RSVPStatus.MAYBE);

        // -------------------------------------------------
        // 5. Pre-populate some actual check-ins + records
        //    (for Check-in Records panel and attendance stats)
        // -------------------------------------------------
        // For eventA (must be active to accept check-ins)
        eventA.activate();   // just to be sure

        if (eventA.addVisitor(v1)) {
            CheckInRecord rA1 = new CheckInRecord(
                    v1,
                    eventA,
                    eATime.plusMinutes(5)
            );
            v1.addCheckInRecord(rA1);
        }
        if (eventA.addVisitor(v3)) {
            CheckInRecord rA2 = new CheckInRecord(
                    v3,
                    eventA,
                    eATime.plusMinutes(15)
            );
            v3.addCheckInRecord(rA2);
        }

        // For eventC (past event; simulate check-ins happened before closing)
        eventC.activate();   // temporarily, so addVisitor works
        if (eventC.addVisitor(v2)) {
            CheckInRecord rC1 = new CheckInRecord(
                    v2,
                    eventC,
                    eCTime.plusMinutes(10)
            );
            v2.addCheckInRecord(rC1);
        }
        eventC.close();      // now keep it closed as intended

        // -------------------------------------------------
        // 6. Launch GUI
        // -------------------------------------------------
        SwingUtilities.invokeLater(() -> {
            OpenHouseManagerGUI gui = new OpenHouseManagerGUI("OpenHouseManager - Full Demo", login);
            gui.setVisible(true);
        });
    }
}
