package org.finalproject.system;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.io.Serializable;


public class Event implements Serializable{

    private String eventId;
    private LocalDateTime startTime;
    private Agent agent;
    private House house;
    private int capacity;
    private int attendance;
    private List<Visitor> visitors;
    private int checkInCode;

    // simple flags instead of state enums
    private boolean scheduled;
    private boolean active;
    private boolean closed;

    // RSVP tracking
    private Map<Visitor, RSVPStatus> rsvps;

    public Event(String eventId, LocalDateTime startTime,
                 Agent agent, House house, int capacity, int checkInCode) {
        this.eventId = eventId;
        this.startTime = startTime;
        this.agent = agent;
        this.house = house;
        this.capacity = capacity;
        this.checkInCode = checkInCode;
        this.attendance = 0;
        this.visitors = new ArrayList<>();
        this.scheduled = true;
        this.active = false;
        this.closed = false;
        this.rsvps = new LinkedHashMap<>();
    }

    public boolean isFull() {
        return attendance >= capacity;
    }

    public double getAttendanceRate() {
        if (visitors.isEmpty()) return 0.0;
        return (double) attendance / visitors.size();
    }

    public boolean addVisitor(Visitor visitor) {
        if (visitor == null) return false;
        if (!active || closed) {
            System.out.println("Event is not open for check-in.");
            return false;
        }
        if (isFull()) {
            System.out.println("Event capacity full.");
            return false;
        }
        if (!visitors.contains(visitor)) {
            visitors.add(visitor);
        }
        attendance++;
        
        // Auto-update RSVP to YES on successful check-in
        if (getRsvpStatus(visitor) != RSVPStatus.YES) {
            setRsvp(visitor, RSVPStatus.YES);
        }
        
        return true;
    }

    public void schedule() {
        this.scheduled = true;
    }

    public void activate() {
        // Allow re-opening a previously closed event
        if (scheduled) {
            this.active = true;
            this.closed = false;   // <-- reset closed so kiosk can see it again
        }
    }

    public void close() {
        this.active = false;
        this.closed = true;
    }

    // --- Getters ---

    public String getEventId() {
        return eventId;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public Agent getAgent() {
        return agent;
    }

    public House getHouse() {
        return house;
    }

    public int getCapacity() {
        return capacity;
    }

    public int getAttendance() {
        return attendance;
    }

    public List<Visitor> getVisitors() {
        return new ArrayList<>(visitors);
    }

    public int getCheckInCode() {
        return checkInCode;
    }

    public boolean isScheduled() {
        return scheduled;
    }

    public boolean isActive() {
        return active;
    }

    public boolean isClosed() {
        return closed;
    }

    public LocalDateTime getTime() {
        return startTime;
    }

    public String getDate() {
        return startTime.toLocalDate().toString();
    }

    public String getAddress() {
        return house != null ? house.getAddress() : "";
    }

    // --- RSVP Methods ---

    public void addInvitee(Visitor visitor) {
        if (visitor != null && !closed) {
            rsvps.putIfAbsent(visitor, RSVPStatus.NO_RESPONSE);
        }
    }

    public void setRsvp(Visitor visitor, RSVPStatus status) {
        if (visitor == null || status == null || closed) {
            return;
        }
        rsvps.put(visitor, status);
        
        // Check for overbooking warning
        if (status == RSVPStatus.YES && getRsvpCount(RSVPStatus.YES) > capacity) {
            System.out.println("WARNING: Event " + eventId + " is overbooked! " 
                + getRsvpCount(RSVPStatus.YES) + " YES RSVPs for " + capacity + " capacity.");
        }
    }

    public RSVPStatus getRsvpStatus(Visitor visitor) {
        return rsvps.getOrDefault(visitor, RSVPStatus.NO_RESPONSE);
    }

    public int getRsvpCount(RSVPStatus status) {
        int count = 0;
        for (RSVPStatus s : rsvps.values()) {
            if (s == status) {
                count++;
            }
        }
        return count;
    }

    public List<Visitor> getRsvpList(RSVPStatus status) {
        List<Visitor> result = new ArrayList<>();
        for (Map.Entry<Visitor, RSVPStatus> entry : rsvps.entrySet()) {
            if (entry.getValue() == status) {
                result.add(entry.getKey());
            }
        }
        return result;
    }

    public Map<Visitor, RSVPStatus> getAllRsvps() {
        return new LinkedHashMap<>(rsvps);
    }

    public void removeRsvp(Visitor visitor) {
        if (!closed) {
            rsvps.remove(visitor);
        }
    }

    public boolean isOverbooked() {
        return getRsvpCount(RSVPStatus.YES) > capacity;
    }

    
}
