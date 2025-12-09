package org.finalproject.system;
//Hi this is a comment
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javax.swing.plaf.ButtonUI;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.LinkedHashMap;

//added by Noah
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

/*
 * Main GUI for the Open House Manager.
 *
 * Screens (CardLayout):
 *  - Welcome
 *  - Login
 *  - Create Account
 *  - Dashboard
 *  - Houses
 *  - Events
 *  - Check-in Records
 *  - Send Email
 */
public class OpenHouseManagerGUI extends JFrame {

    // ---------- Model references ----------
    private Login loginModel;
    private Agent currentAgent;

    // ---------- CardLayout stuff ----------
    private CardLayout cardLayout;
    private JPanel mainPanel;

    private static final String CARD_WELCOME    = "WELCOME";
    private static final String CARD_LOGIN      = "LOGIN";
    private static final String CARD_CREATE     = "CREATE";
    private static final String CARD_DASHBOARD  = "DASHBOARD";
    private static final String CARD_HOUSES     = "HOUSES";
    private static final String CARD_EVENTS     = "EVENTS";
    private static final String CARD_CHECKINS   = "CHECKINS";
    private static final String CARD_EMAIL      = "EMAIL";
    private static final String CARD_KIOSK      = "KIOSK";   // added 12/8
    private static final String CARD_VISITORS   = "VISITORS";

    // Panels
    private WelcomePanel welcomePanel;
    private LoginPanel loginPanel;
    private CreateAccountPanel createAccountPanel;
    private DashboardPanel dashboardPanel;
    private HousesPanel housesPanel;
    private EventsPanel eventsPanel;
    private CheckInPanel checkInPanel;
    private EmailPanel emailPanel;
    private KioskPanel kioskPanel;
    private VisitorsPanel visitorsPanel;
    
    private AnimatedBackgroundPanel backgroundPanel;
    
    private static final String prelogin_bg = "/org/finalproject/images/background.gif";
    private static final String postlogin_bg = "/org/finalproject/images/postlogin.gif";

    // ======================================================
    // Constructor
    // ======================================================
    public OpenHouseManagerGUI(String title, Login loginModel) {
        super(title);
        this.loginModel = loginModel;

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        
        mainPanel.setOpaque(false);
        
        backgroundPanel = new AnimatedBackgroundPanel(prelogin_bg);
        setContentPane(backgroundPanel);

        // Create screens
        welcomePanel       = new WelcomePanel(this);
        loginPanel         = new LoginPanel(this);
        createAccountPanel = new CreateAccountPanel(this);
        dashboardPanel     = new DashboardPanel(this);
        housesPanel        = new HousesPanel(this);
        eventsPanel        = new EventsPanel(this);
        checkInPanel       = new CheckInPanel(this);
        emailPanel         = new EmailPanel(this);
        kioskPanel         = new KioskPanel(this);
        visitorsPanel      = new VisitorsPanel(this);
        
        
        // Register cards
        mainPanel.add(welcomePanel,       CARD_WELCOME);
        mainPanel.add(loginPanel,         CARD_LOGIN);
        mainPanel.add(createAccountPanel, CARD_CREATE);
        mainPanel.add(dashboardPanel,     CARD_DASHBOARD);
        mainPanel.add(housesPanel,        CARD_HOUSES);
        mainPanel.add(eventsPanel,        CARD_EVENTS);
        mainPanel.add(checkInPanel,       CARD_CHECKINS);
        mainPanel.add(emailPanel,         CARD_EMAIL);
        mainPanel.add(kioskPanel,         CARD_KIOSK);
        mainPanel.add(visitorsPanel,      CARD_VISITORS);
        //setContentPane(mainPanel);
        
        backgroundPanel.add(mainPanel, BorderLayout.CENTER);

        setSize(900, 550);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        showScreen(CARD_WELCOME);
        setVisible(true);
    }

    // ======================================================
    // Navigation helpers
    // ======================================================
    public void showScreen(String cardName) {
        // protect everything except welcome/login/create/kiosk if not logged in
        if (!CARD_WELCOME.equals(cardName)
                && !CARD_LOGIN.equals(cardName)
                && !CARD_CREATE.equals(cardName)
                && !CARD_KIOSK.equals(cardName)) {   // <-- allow kiosk without login
            if (currentAgent == null) {
                JOptionPane.showMessageDialog(
                        this,
                        "You must log in first.",
                        "Access Denied",
                        JOptionPane.WARNING_MESSAGE
                );
                cardLayout.show(mainPanel, CARD_LOGIN);
                return;
            }
        }

        // Refresh panels that depend on currentAgent
        if (CARD_DASHBOARD.equals(cardName)) {
            dashboardPanel.refresh();
        } else if (CARD_HOUSES.equals(cardName)) {
            housesPanel.refresh();
        } else if (CARD_EVENTS.equals(cardName)) {
            eventsPanel.refresh();
        } else if (CARD_CHECKINS.equals(cardName)) {
            checkInPanel.refresh();
        } else if (CARD_EMAIL.equals(cardName)) {
            emailPanel.refresh();
        } else if (CARD_KIOSK.equals(cardName)) {
            kioskPanel.refresh();
        }else if (CARD_VISITORS.equals(cardName)) {   
            visitorsPanel.refresh();
        }

        cardLayout.show(mainPanel, cardName);
    }


    public void setCurrentAgent(Agent agent) {
    	this.currentAgent = agent;

        if (backgroundPanel != null) {
            if (agent != null) {
               
                backgroundPanel.setBackgroundResource(postlogin_bg);
            } else {
               
                backgroundPanel.setBackgroundResource(prelogin_bg);
            }
        }

        dashboardPanel.refresh();
    }

    public Agent getCurrentAgent() {
        return currentAgent;
    }

    public Login getLoginModel() {
        return loginModel;
    }

    public void logout() {
        setCurrentAgent(null);        
        showScreen(CARD_WELCOME);
    }
    
    
    
    
    
    
    
    
    // ======================================================
    //  WELCOME PANEL
    // ======================================================
    private static class WelcomePanel extends JPanel {
    	public WelcomePanel(OpenHouseManagerGUI parent) {
            setOpaque(false); // let the GIF show around the edges
            setLayout(new GridBagLayout()); // center the card
            

            // Card panel that holds title + subtitle + buttons
            JPanel card = new JPanel();
            card.setOpaque(true);
            card.setBackground(new Color(255, 255, 255, 190)); // white with transparency
            card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
            card.setBorder(BorderFactory.createEmptyBorder(30, 60, 30, 60));

            JLabel title = new JLabel("Open House Manager");
            title.setFont(new Font("Century Gothic", Font.BOLD, 32));
            title.setAlignmentX(Component.CENTER_ALIGNMENT);

            JLabel subtitle = new JLabel("Real Estate Open House Management System");
            subtitle.setFont(new Font("CG Omega", Font.PLAIN, 16));
            subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

            JButton loginButton = createPrimaryButton("Login");
            JButton createAccountButton = createSecondaryButton("Create Account");
            JButton kioskButton = createSecondaryButton("Visitor Check-In");

            loginButton.setAlignmentX(Component.CENTER_ALIGNMENT);
            createAccountButton.setAlignmentX(Component.CENTER_ALIGNMENT);
            kioskButton.setAlignmentX(Component.CENTER_ALIGNMENT); 

            loginButton.addActionListener(e -> parent.showScreen(CARD_LOGIN));
            createAccountButton.addActionListener(e -> parent.showScreen(CARD_CREATE));
            kioskButton.addActionListener(e -> parent.showScreen(CARD_KIOSK));
            

            card.add(title);
            card.add(Box.createRigidArea(new Dimension(0, 10)));
            card.add(subtitle);
            card.add(Box.createRigidArea(new Dimension(0, 25)));
            card.add(loginButton);
            card.add(Box.createRigidArea(new Dimension(0, 10)));
            card.add(createAccountButton);
            card.add(Box.createRigidArea(new Dimension(0, 10)));  
            card.add(kioskButton);  

            // Center the card on the screen
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = 0;
            add(card, gbc);
        }
    }

    
    
    
    
    
    
    // ======================================================
    //  LOGIN PANEL
    // ======================================================
    private static class LoginPanel extends JPanel {
        private JTextField usernameField;
        private JPasswordField passwordField;

        public LoginPanel(OpenHouseManagerGUI parent) {
            setOpaque(false);
            setLayout(new GridBagLayout()); // center card

            JPanel card = createCardPanel();

            JLabel title = new JLabel("Login");
            title.setFont(new Font("Century Gothic", Font.BOLD, 24));
            title.setAlignmentX(Component.CENTER_ALIGNMENT);

            usernameField = new JTextField(15);
            passwordField = new JPasswordField(15);

            JPanel userRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
            userRow.setOpaque(false);
            userRow.add(new JLabel("Username:"));
            userRow.add(usernameField);

            JPanel passRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
            passRow.setOpaque(false);
            passRow.add(new JLabel("Password:"));
            passRow.add(passwordField);

            JButton backButton  = createSecondaryButton("Back");
            JButton loginButton = createPrimaryButton("Login");

            backButton.addActionListener(e -> parent.showScreen(CARD_WELCOME));
            loginButton.addActionListener(e -> {
                String username = usernameField.getText().trim();
                String password = new String(passwordField.getPassword());
                Person p = parent.getLoginModel().login(username, password);

                if (p instanceof Agent) {
                    parent.setCurrentAgent((Agent) p);
                    parent.showScreen(CARD_DASHBOARD);
                } else {
                    JOptionPane.showMessageDialog(parent, "Invalid login.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            });

            card.add(title);
            card.add(Box.createRigidArea(new Dimension(0, 15)));
            card.add(userRow);
            card.add(passRow);
            card.add(Box.createRigidArea(new Dimension(0, 15)));
            card.add(backButton);
            card.add(Box.createRigidArea(new Dimension(0, 8)));
            card.add(loginButton);
                                          

            // center on screen
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = 0;
            add(card, gbc);
        }
    }

    
    
    
    
    
    // ======================================================
    //  CREATE ACCOUNT PANEL
    // ======================================================
    private static class CreateAccountPanel extends JPanel {
        private JTextField nameField;
        private JTextField emailField;
        private JTextField phoneField;
        private JTextField usernameField;
        private JPasswordField passwordField;

        public CreateAccountPanel(OpenHouseManagerGUI parent) {
            setOpaque(false);                 // allow GIF background
            setLayout(new GridBagLayout());    // center the card

            // ----- Translucent card -----
            JPanel card = createCardPanel();   // already translucent + padded

            JLabel title = new JLabel("Create Account");
            title.setFont(new Font("Century Gothic", Font.BOLD, 24));
            title.setAlignmentX(Component.CENTER_ALIGNMENT);

            JTextField nameField = new JTextField(15);
            JTextField emailField = new JTextField(15);
            JTextField phoneField = new JTextField(15);
            JTextField usernameField = new JTextField(15);
            JPasswordField passwordField = new JPasswordField(15);

            card.add(title);
            card.add(Box.createVerticalStrut(15));

            // ----- Name row -----
            JPanel nameRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
            nameRow.setOpaque(false);
            nameRow.add(new JLabel("Name:"));
            nameRow.add(nameField);
            card.add(nameRow);

            // ----- Email row -----
            JPanel emailRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
            emailRow.setOpaque(false);
            emailRow.add(new JLabel("Email:"));
            emailRow.add(emailField);
            card.add(emailRow);

            // ----- Phone row -----
            JPanel phoneRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
            phoneRow.setOpaque(false);
            phoneRow.add(new JLabel("Phone:"));
            phoneRow.add(phoneField);
            card.add(phoneRow);

            // ----- Username row -----
            JPanel userRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
            userRow.setOpaque(false);
            userRow.add(new JLabel("Username:"));
            userRow.add(usernameField);
            card.add(userRow);

            // ----- Password row -----
            JPanel passRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
            passRow.setOpaque(false);
            passRow.add(new JLabel("Password:"));
            passRow.add(passwordField);
            card.add(passRow);

            card.add(Box.createVerticalStrut(15));

            // ----- Buttons -----
            JButton backButton = createSecondaryButton("Back");
            JButton createButton = createPrimaryButton("Create Account");

            backButton.addActionListener(e -> parent.showScreen(CARD_WELCOME));

            createButton.addActionListener(e -> {
                String name = nameField.getText().trim();
                String email = emailField.getText().trim();
                String phone = phoneField.getText().trim();
                String username = usernameField.getText().trim();
                String password = new String(passwordField.getPassword());

                if (name.isEmpty() || email.isEmpty() || phone.isEmpty()
                        || username.isEmpty() || password.isEmpty()) {
                    JOptionPane.showMessageDialog(
                            parent,
                            "All fields are required.",
                            "Input Error",
                            JOptionPane.ERROR_MESSAGE
                    );
                    return;
                }

                Agent newAgent = new Agent(name, email, phone, username, password);
                parent.getLoginModel().addPerson(newAgent);

                JOptionPane.showMessageDialog(
                        parent,
                        "Account created successfully.",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE
                );

                parent.setCurrentAgent(newAgent);
                parent.showScreen(CARD_DASHBOARD);
            });

            JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
            buttonRow.setOpaque(false);
            buttonRow.add(backButton);
            buttonRow.add(createButton);

            card.add(buttonRow);

            // ----- Center card -----
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = 0;
            add(card, gbc);
        }

    }

 // ======================================================
//  KIOSK PANEL (Visitor-side Check-in)
// ======================================================
private static class KioskPanel extends JPanel {
    private OpenHouseManagerGUI parent;

    private JComboBox<String> eventCombo;
    private java.util.List<Event> eventObjects = new ArrayList<>();

    private JTextField nameField;
    private JTextField emailField;
    private JTextField phoneField;
    private JCheckBox mailingConsentBox;
    private JTextField codeField;
    private JTextArea messageArea;

    public KioskPanel(OpenHouseManagerGUI parent) {
        this.parent = parent;
        setOpaque(false);
        setLayout(new GridBagLayout());

        JPanel card = new JPanel(new BorderLayout(10, 10));
        card.setOpaque(true);
        card.setBackground(new Color(255, 255, 255, 230));
        card.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        // ----- HEADER -----
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel title = new JLabel("Visitor Check-In Kiosk");
        title.setFont(new Font("Century Gothic", Font.BOLD, 20));

        JButton backButton = createSecondaryButton("Back to Welcome");
        backButton.addActionListener(e -> parent.showScreen(CARD_WELCOME));

        JPanel headerButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        headerButtons.setOpaque(false);
        headerButtons.add(backButton);

        header.add(title, BorderLayout.WEST);
        header.add(headerButtons, BorderLayout.EAST);

        card.add(header, BorderLayout.NORTH);

        // ----- CENTER FORM -----
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 4, 6, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;

        // Event selector
        form.add(new JLabel("Select Event:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        eventCombo = new JComboBox<>();
        eventCombo.setPrototypeDisplayValue("Choose an open house...");
        form.add(eventCombo, gbc);

        // Name
        gbc.gridy++;
        gbc.gridx = 0;
        gbc.weightx = 0;
        form.add(new JLabel("Name:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        nameField = new JTextField(20);
        form.add(nameField, gbc);

        // Email
        gbc.gridy++;
        gbc.gridx = 0;
        gbc.weightx = 0;
        form.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        emailField = new JTextField(20);
        form.add(emailField, gbc);

        // Phone
        gbc.gridy++;
        gbc.gridx = 0;
        gbc.weightx = 0;
        form.add(new JLabel("Phone:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        phoneField = new JTextField(20);
        form.add(phoneField, gbc);

        // Mailing list consent
        gbc.gridy++;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        mailingConsentBox = new JCheckBox("I agree to receive follow-up emails about this property.");
        mailingConsentBox.setOpaque(false);
        form.add(mailingConsentBox, gbc);

        // Check-in code
        gbc.gridy++;
        gbc.gridx = 0;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        form.add(new JLabel("Check-in Code:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        codeField = new JTextField(10);
        form.add(codeField, gbc);

        // Message area
        gbc.gridy++;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;

        messageArea = new JTextArea(5, 40);
        messageArea.setEditable(false);
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);
        JScrollPane msgScroll = new JScrollPane(messageArea);
        form.add(msgScroll, gbc);

        card.add(form, BorderLayout.CENTER);

        // ----- FOOTER (Check-in Button) -----
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER));
        footer.setOpaque(false);

        JButton checkInButton = createPrimaryButton("Check In");
        checkInButton.addActionListener(e -> handleCheckIn());

        footer.add(checkInButton);
        card.add(footer, BorderLayout.SOUTH);

        // center card
        GridBagConstraints outer = new GridBagConstraints();
        outer.gridx = 0;
        outer.gridy = 0;
        add(card, outer);
    }

    /** Called from showScreen(CARD_KIOSK). Populate the event list. */
    public void refresh() {
        eventCombo.removeAllItems();
        eventObjects.clear();
        messageArea.setText("");
        nameField.setText("");
        emailField.setText("");
        phoneField.setText("");
        codeField.setText("");
        mailingConsentBox.setSelected(false);

        Login login = parent.getLoginModel();
        if (login == null) {
            messageArea.setText("System not initialized.");
            return;
        }

        List<Person> people = login.getAllPeople();
        if (people.isEmpty()) {
            messageArea.setText("No agents configured yet.");
            return;
        }

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMM dd yyyy HH:mm");
        boolean foundEvents = false;

       
        java.util.Set<String> seenEventIds = new java.util.HashSet<>();

        for (Person p : people) {
            if (p instanceof Agent) {
                Agent agent = (Agent) p;

                List<House> houses = agent.getProperties();
                if (houses == null) continue;

                for (House h : houses) {
                    List<Event> evs = h.getEvents();
                    if (evs == null) continue;

                    for (Event e : evs) {
                        if (!e.isActive() || e.isClosed()) continue;

                        // 🔍 Skip duplicates by eventId
                        if (!seenEventIds.add(e.getEventId())) {
                            continue;
                        }

                        foundEvents = true;
                        eventObjects.add(e);

                        String label = e.getEventId()
                                + " - " + h.getAddress()
                                + " - " + e.getStartTime().format(fmt);

                        eventCombo.addItem(label);
                    }
                }
            }
        }

        if (!foundEvents) {
            messageArea.setText(
                "No active events available for check-in.\n" +
                "Please ask an agent to activate an event."
            );
        }
    }



    /** Handle the visitor pressing "Check In". */
    /** Handle the visitor pressing "Check In". */
    private void handleCheckIn() {
        messageArea.setText("");

        // 1) Make sure an event is selected
        int idx = eventCombo.getSelectedIndex();
        if (idx < 0 || idx >= eventObjects.size()) {
            messageArea.setText("Please select an event.");
            return;
        }
        Event event = eventObjects.get(idx);

        // 2) Read user input
        String name  = nameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        String code  = codeField.getText().trim();
        boolean consent = mailingConsentBox.isSelected();

        if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || code.isEmpty()) {
            messageArea.setText("All fields (name, email, phone, and code) are required.");
            return;
        }

        // 3) Parse and validate check-in code
        int codeInt;
        try {
            codeInt = Integer.parseInt(code);
        } catch (NumberFormatException ex) {
            messageArea.setText("Check-in code must be a valid integer.");
            return;
        }

        if (codeInt != event.getCheckInCode()) {
            messageArea.setText("Incorrect check-in code for this event.");
            return;
        }

        // 4) Make sure event is actually open
        if (!event.isActive() || event.isClosed()) {
            messageArea.setText("This event is not currently open for check-in.");
            return;
        }
        if (event.isFull()) {
            messageArea.setText("This event is at capacity and cannot accept more visitors.");
            return;
        }

        // 5) Look for an existing visitor with the same email
        Visitor visitor = null;
        for (Visitor existing : event.getVisitors()) {  // copy of list, but same Visitor objects
            if (existing.getEmail().equalsIgnoreCase(email)) {
                visitor = existing;
                break;
            }
        }

        // 6) Create or update the Visitor
        if (visitor == null) {
            visitor = new Visitor(name, email, phone);
        }
        // Update consent (and optionally name/phone if you later add setters in Person)
        visitor.setMailingListConsent(consent);

        // 7) Register visitor to the event using the Event logic
        boolean added = event.addVisitor(visitor);  // uses active/closed/capacity + RSVP auto-YES
        if (!added) {
            messageArea.setText("Could not check in (event may not be open or is full).");
            return;
        }

        // 8) Create a CheckInRecord and attach it to the visitor
        CheckInRecord record = new CheckInRecord(visitor, event, LocalDateTime.now());
        visitor.addCheckInRecord(record);
        final Visitor finalVisitor = visitor;

        // 9) Optionally capture the Visitor's console output into the GUI
        String info = parent.captureConsoleOutput(() -> {
            finalVisitor.printCheckInRecord();  // your existing method
        });

        messageArea.setText("Check-in successful!\n\n" + info);
    }

}

    
    
    
    
    
    // ======================================================
    //  DASHBOARD PANEL
    // ======================================================
    private static class DashboardPanel extends JPanel {
        private OpenHouseManagerGUI parent;
        private JLabel welcomeLabel;

        public DashboardPanel(OpenHouseManagerGUI parent) {
            this.parent = parent;
            setOpaque(false);
            setLayout(new GridBagLayout());
            

            JPanel card = createCardPanel();

            welcomeLabel = new JLabel("Welcome");
            welcomeLabel.setFont(new Font("Century Gothic", Font.BOLD, 24));
            welcomeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

            JButton checkinButton = createPrimaryButton("Check-in Records");
            JButton eventsButton  = createPrimaryButton("Events");
            JButton housesButton  = createPrimaryButton("Houses");
            JButton visitorsButton = createPrimaryButton("Visitors");
            JButton emailButton   = createPrimaryButton("Send Email");
            JButton logoutButton  = createSecondaryButton("Logout");

            checkinButton.setAlignmentX(Component.CENTER_ALIGNMENT);
            eventsButton.setAlignmentX(Component.CENTER_ALIGNMENT);
            housesButton.setAlignmentX(Component.CENTER_ALIGNMENT);
            visitorsButton.setAlignmentX(Component.CENTER_ALIGNMENT);
            emailButton.setAlignmentX(Component.CENTER_ALIGNMENT);
            logoutButton.setAlignmentX(Component.CENTER_ALIGNMENT);

            checkinButton.addActionListener(e -> parent.showScreen(CARD_CHECKINS));
            eventsButton.addActionListener(e -> parent.showScreen(CARD_EVENTS));
            housesButton.addActionListener(e -> parent.showScreen(CARD_HOUSES));
            visitorsButton.addActionListener(e -> parent.showScreen(CARD_VISITORS));
            emailButton.addActionListener(e -> parent.showScreen(CARD_EMAIL));
            logoutButton.addActionListener(e -> parent.logout());

            card.add(welcomeLabel);
            card.add(Box.createRigidArea(new Dimension(0, 15)));
            card.add(checkinButton);
            card.add(Box.createRigidArea(new Dimension(0, 10)));
            card.add(eventsButton);
            card.add(Box.createRigidArea(new Dimension(0, 10)));
            card.add(housesButton);
            card.add(Box.createRigidArea(new Dimension(0, 10)));
            card.add(visitorsButton); 
            card.add(Box.createRigidArea(new Dimension(0, 10)));
            card.add(emailButton);
            card.add(Box.createRigidArea(new Dimension(0, 15)));
            card.add(logoutButton);

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = 0;
            add(card, gbc);
        }
        
        
        
        public void refresh() {
            Agent a = parent.getCurrentAgent();
            welcomeLabel.setText("Welcome, " + (a != null ? a.getName() : ""));
        }
    }

    
    
    
    
    
    
    // ======================================================
    //  HOUSES PANEL
    // ======================================================
   private static class HousesPanel extends JPanel {
        private OpenHouseManagerGUI parent;
        private JList<String> houseList;
        private DefaultListModel<String> listModel;

        private JTextArea detailsArea;
        private JLabel photoLabel;
        private JButton prevPhotoButton;
        private JButton nextPhotoButton;

        // We still cache ImageIcons per house, but the source of truth is House.imagePaths
        private Map<House, java.util.List<ImageIcon>> housePhotos = new HashMap<>();
        private java.util.List<ImageIcon> currentPhotos = java.util.Collections.emptyList();
        private int currentPhotoIndex = -1;

        public HousesPanel(OpenHouseManagerGUI parent) {
            this.parent = parent;
            setOpaque(false);
            setLayout(new BorderLayout(10, 10));  // fill most of the window

            // ---------- HEADER (top full width) ----------
            JPanel header = new JPanel(new BorderLayout());
            header.setOpaque(true);
            header.setBackground(new Color(255, 255, 255, 220)); // soft white bar
            header.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));

            JLabel title = new JLabel("Houses");
            title.setFont(new Font("Century Gothic", Font.BOLD, 20));
            title.setForeground(new Color(40, 40, 40)); // dark gray text

            JPanel headerButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
            headerButtons.setOpaque(false);

            JButton addHouseButton = createPrimaryButton("Add House");
            JButton backButton     = createSecondaryButton("Back");

            backButton.addActionListener(e -> parent.showScreen(CARD_DASHBOARD));
            addHouseButton.addActionListener(e -> addNewHouse());

            headerButtons.add(addHouseButton);
            headerButtons.add(backButton);

            header.add(title, BorderLayout.WEST);
            header.add(headerButtons, BorderLayout.EAST);

            add(header, BorderLayout.NORTH);

            // ---------- LEFT SIDE: HOUSE LIST ----------
            listModel = new DefaultListModel<>();
            houseList = new JList<>(listModel);
            houseList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            houseList.setVisibleRowCount(18);

            JScrollPane listScroll = new JScrollPane(houseList);

            // ---------- RIGHT SIDE: PHOTOS + DETAILS ----------
            JPanel rightPanel = new JPanel(new BorderLayout(5, 5));
            rightPanel.setOpaque(false);

            // Photo area at top
            JPanel photoPanel = new JPanel(new BorderLayout());
            photoPanel.setOpaque(false);

            photoLabel = new JLabel("No photos", SwingConstants.CENTER);
            photoLabel.setPreferredSize(new Dimension(420, 260));
            photoLabel.setOpaque(true);
            photoLabel.setBackground(new Color(255, 255, 255, 220));

            JPanel photoControls = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 4));
            photoControls.setOpaque(false);

            prevPhotoButton = createSecondaryButton("◀");
            nextPhotoButton = createSecondaryButton("▶");
            JButton addPhotosButton = createSecondaryButton("Add Photos");

            prevPhotoButton.addActionListener(e -> showPhoto(currentPhotoIndex - 1));
            nextPhotoButton.addActionListener(e -> showPhoto(currentPhotoIndex + 1));
            addPhotosButton.addActionListener(e -> addPhotosToSelectedHouse());

            photoControls.add(prevPhotoButton);
            photoControls.add(addPhotosButton);
            photoControls.add(nextPhotoButton);

            photoPanel.add(photoLabel, BorderLayout.CENTER);
            photoPanel.add(photoControls, BorderLayout.SOUTH);

            // Details area under photos, scrollable
            detailsArea = new JTextArea();
            detailsArea.setEditable(false);
            detailsArea.setLineWrap(true);
            detailsArea.setWrapStyleWord(true);
            detailsArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            JScrollPane detailsScroll = new JScrollPane(detailsArea);

            rightPanel.add(photoPanel, BorderLayout.NORTH);
            rightPanel.add(detailsScroll, BorderLayout.CENTER);

            // ---------- SPLIT PANE ----------
            JSplitPane split = new JSplitPane(
                    JSplitPane.HORIZONTAL_SPLIT,
                    listScroll,
                    rightPanel
            );
            split.setDividerLocation(260);  // left column width
            split.setResizeWeight(0.3);     // 30% left, 70% right
            split.setContinuousLayout(true);

            add(split, BorderLayout.CENTER);

            // selection listener
            houseList.addListSelectionListener(e -> {
                if (!e.getValueIsAdjusting()) {
                    showHouseDetails(houseList.getSelectedIndex());
                }
            });

            updatePhotoControls();
        }

        // Refresh list from the current agent
        public void refresh() {
            listModel.clear();
            detailsArea.setText("");
            photoLabel.setIcon(null);
            photoLabel.setText("No photos");
            currentPhotos = java.util.Collections.emptyList();
            currentPhotoIndex = -1;
            updatePhotoControls();

            Agent agent = parent.getCurrentAgent();
            if (agent == null) return;

            java.util.List<House> houses = agent.getProperties();
            if (houses == null) return;

            for (House h : houses) {
                listModel.addElement(h.getAddress());

                // Build ImageIcons from any stored image paths
                java.util.List<String> paths = h.getImagePaths();
                java.util.List<ImageIcon> icons = new ArrayList<>();
                for (String path : paths) {
                    ImageIcon icon = loadIcon(path);
                    if (icon != null) {
                        icons.add(icon);
                    }
                }
                if (!icons.isEmpty()) {
                    housePhotos.put(h, icons);
                }
            }
        }

        // Show info + photos for selected house
        private void showHouseDetails(int index) {
            Agent agent = parent.getCurrentAgent();
            if (agent == null || index < 0) {
                detailsArea.setText("");
                currentPhotos = java.util.Collections.emptyList();
                currentPhotoIndex = -1;
                updatePhotoControls();
                return;
            }

            java.util.List<House> houses = agent.getProperties();
            if (houses == null || index >= houses.size()) {
                detailsArea.setText("");
                currentPhotos = java.util.Collections.emptyList();
                currentPhotoIndex = -1;
                updatePhotoControls();
                return;
            }

            House h = houses.get(index);

            StringBuilder sb = new StringBuilder();
            sb.append("Address: ").append(h.getAddress()).append("\n");
            sb.append("Price: $").append(h.getPrice()).append("\n");
            sb.append("Size: ").append(h.getSqft()).append(" sqft\n");
            sb.append("Beds/Baths: ").append(h.getBeds()).append("/")
              .append(h.getBaths()).append("\n");
            sb.append("Year Built: ").append(h.getYearBuilt()).append("\n\n");
            sb.append("Description:\n").append(h.getDescription()).append("\n\n");
            sb.append("For Sale: ").append(h.isStillForSale()).append("\n\n");

            java.util.List<Event> events = h.getEvents();
            if (events != null && !events.isEmpty()) {
                sb.append("Events for this property:\n");
                for (Event e : events) {
                    sb.append(" • ").append(e.getEventId())
                      .append(" at ").append(e.getStartTime())
                      .append(" (Capacity: ").append(e.getCapacity()).append(")\n");
                }
            } else {
                sb.append("No events for this property.\n");
            }

            detailsArea.setText(sb.toString());
            detailsArea.setCaretPosition(0);

            // photos for this house (from cache; if missing, build from paths)
            java.util.List<ImageIcon> icons = housePhotos.get(h);
            if (icons == null) {
                icons = new ArrayList<>();
                for (String path : h.getImagePaths()) {
                    ImageIcon icon = loadIcon(path);
                    if (icon != null) icons.add(icon);
                }
                if (!icons.isEmpty()) {
                    housePhotos.put(h, icons);
                }
            }

            currentPhotos = icons != null ? icons : java.util.Collections.emptyList();
            if (currentPhotos.isEmpty()) {
                currentPhotoIndex = -1;
                photoLabel.setIcon(null);
                photoLabel.setText("No photos");
            } else {
                currentPhotoIndex = 0;
                showPhoto(currentPhotoIndex);
            }
            updatePhotoControls();
        }

        // Add house (with optional photos) — all in GUI
        private void addNewHouse() {
            Agent agent = parent.getCurrentAgent();
            if (agent == null) {
                JOptionPane.showMessageDialog(
                        parent,
                        "No agent logged in.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                );
                return;
            }

            String address = JOptionPane.showInputDialog(parent, "Address:");
            if (address == null || address.trim().isEmpty()) return;

            String priceStr = JOptionPane.showInputDialog(parent, "Price (number):");
            if (priceStr == null) return;

            String sqftStr  = JOptionPane.showInputDialog(parent, "Size in sqft (number):");
            if (sqftStr == null) return;

            String bedsStr  = JOptionPane.showInputDialog(parent, "Number of bedrooms:");
            if (bedsStr == null) return;

            String bathsStr = JOptionPane.showInputDialog(parent, "Number of bathrooms:");
            if (bathsStr == null) return;

            String yearStr  = JOptionPane.showInputDialog(parent, "Year built:");
            if (yearStr == null) return;

            String desc     = JOptionPane.showInputDialog(parent, "Short description:");
            if (desc == null) desc = "";

            try {
                int price = Integer.parseInt(priceStr.trim());
                int sqft  = Integer.parseInt(sqftStr.trim());
                int beds  = Integer.parseInt(bedsStr.trim());
                int baths = Integer.parseInt(bathsStr.trim());
                int year  = Integer.parseInt(yearStr.trim());

                House newHouse = new House(
                        address.trim(),
                        price,
                        sqft,
                        beds,
                        baths,
                        year,
                        desc.trim()
                );

                // optional: choose photos
                int choice = JOptionPane.showConfirmDialog(
                        parent,
                        "Do you want to add photos for this house now?",
                        "Add Photos",
                        JOptionPane.YES_NO_OPTION
                );

                if (choice == JOptionPane.YES_OPTION) {
                    JFileChooser chooser = new JFileChooser();
                    chooser.setMultiSelectionEnabled(true);
                    int result = chooser.showOpenDialog(parent);
                    if (result == JFileChooser.APPROVE_OPTION) {
                        File[] files = chooser.getSelectedFiles();
                        java.util.List<ImageIcon> photos = new ArrayList<>();
                        for (File f : files) {
                            String path = f.getAbsolutePath();
                            newHouse.addImagePath(path);              // <-- store path in model
                            photos.add(new ImageIcon(path));          // cache icon
                        }
                        if (!photos.isEmpty()) {
                            housePhotos.put(newHouse, photos);
                        }
                    }
                }

                // add to agent's list
                agent.getProperties().add(newHouse);

                refresh();
                if (listModel.size() > 0) {
                    houseList.setSelectedIndex(listModel.size() - 1);
                }

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(
                        parent,
                        "Invalid numeric value entered.",
                        "Input Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        }

        // show specific photo index
        private void showPhoto(int index) {
            if (currentPhotos == null || currentPhotos.isEmpty()) {
                photoLabel.setIcon(null);
                photoLabel.setText("No photos");
                currentPhotoIndex = -1;
                updatePhotoControls();
                return;
            }

            if (index < 0 || index >= currentPhotos.size()) {
                return; // ignore out-of-range
            }

            currentPhotoIndex = index;
            ImageIcon raw = currentPhotos.get(index);

            int w = photoLabel.getWidth();
            int h = photoLabel.getHeight();
            if (w <= 0) w = 420;
            if (h <= 0) h = 260;

            Image scaled = raw.getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH);
            photoLabel.setIcon(new ImageIcon(scaled));
            photoLabel.setText(null);

            updatePhotoControls();
        }

        private void updatePhotoControls() {
            boolean hasPhotos = currentPhotos != null && !currentPhotos.isEmpty() && currentPhotoIndex >= 0;
            prevPhotoButton.setEnabled(hasPhotos && currentPhotoIndex > 0);
            nextPhotoButton.setEnabled(hasPhotos && currentPhotoIndex < currentPhotos.size() - 1);
        }

        // Add photos to an existing house from JFileChooser
        private void addPhotosToSelectedHouse() {
            Agent agent = parent.getCurrentAgent();
            if (agent == null) {
                JOptionPane.showMessageDialog(parent, "No agent logged in.",
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int index = houseList.getSelectedIndex();
            if (index < 0) {
                JOptionPane.showMessageDialog(parent, "Please select a house first.",
                        "No House Selected", JOptionPane.WARNING_MESSAGE);
                return;
            }

            java.util.List<House> houses = agent.getProperties();
            if (houses == null || index >= houses.size()) return;

            House h = houses.get(index);

            JFileChooser chooser = new JFileChooser();
            chooser.setMultiSelectionEnabled(true);
            int result = chooser.showOpenDialog(parent);
            if (result != JFileChooser.APPROVE_OPTION) {
                return;
            }

            File[] files = chooser.getSelectedFiles();
            java.util.List<ImageIcon> photos = housePhotos.getOrDefault(h, new ArrayList<>());

            for (File f : files) {
                String path = f.getAbsolutePath();
                h.addImagePath(path);                     // <-- store path
                photos.add(new ImageIcon(path));          // cache icon
            }

            if (!photos.isEmpty()) {
                housePhotos.put(h, photos);
                currentPhotos = photos;
                currentPhotoIndex = 0;
                showPhoto(currentPhotoIndex);
            }
        }

        /** Helper: load an icon from either a classpath resource or a file path. */
        private ImageIcon loadIcon(String path) {
            if (path == null || path.trim().isEmpty()) return null;
            path = path.trim();

            // classpath resource (e.g. "/org/finalproject/images/houses/main_front.jpg")
            if (path.startsWith("/")) {
                java.net.URL url = getClass().getResource(path);
                if (url != null) {
                    return new ImageIcon(url);
                }
            }

            // else treat as file path
            File f = new File(path);
            if (f.exists()) {
                return new ImageIcon(f.getAbsolutePath());
            }

            return null;
        }
    }
    
    
    
    
    
    
 // ======================================================
//  EVENTS PANEL
// ======================================================
private static class EventsPanel extends JPanel {
    private OpenHouseManagerGUI parent;
    private JList<String> eventList;
    private DefaultListModel<String> listModel;
    private JTextArea detailsArea;
    private java.util.List<Event> eventObjects = new ArrayList<>();

    // --- RSVP UI fields ---
    private DefaultListModel<String> rsvpListModel;
    private JList<String> rsvpList;
    private java.util.List<Visitor> rsvpVisitors = new ArrayList<>();
    private JComboBox<RSVPStatus> rsvpStatusCombo;
    private JTextField inviteeNameField;
    private JTextField inviteeEmailField;
    private JTextField inviteePhoneField;

    public EventsPanel(OpenHouseManagerGUI parent) {
        this.parent = parent;
        setOpaque(false);
        setLayout(new BorderLayout(10, 10));

        // --------- HEADER BAR ----------
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(true);
        header.setBackground(new Color(255, 255, 255, 220));
        header.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));

        JLabel title = new JLabel("Events");
        title.setFont(new Font("Century Gothic", Font.BOLD, 20));
        title.setForeground(new Color(40, 40, 40));

        JPanel headerButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        headerButtons.setOpaque(false);

        // Create Event button
        JButton createEventButton = createSecondaryButton("Create Event");
        createEventButton.addActionListener(e -> parent.createEventFromGui());
        headerButtons.add(createEventButton);

        JButton backButton = createSecondaryButton("Back");
        backButton.addActionListener(e -> parent.showScreen(CARD_DASHBOARD));
        headerButtons.add(backButton);

        header.add(title, BorderLayout.WEST);
        header.add(headerButtons, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);

        // --------- LEFT: EVENT LIST ----------
        listModel = new DefaultListModel<>();
        eventList = new JList<>(listModel);
        eventList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        eventList.setVisibleRowCount(18);

        JScrollPane listScroll = new JScrollPane(eventList);

        // --------- RIGHT: DETAILS + RSVP MANAGEMENT ----------
        detailsArea = new JTextArea();
        detailsArea.setEditable(false);
        detailsArea.setLineWrap(true);
        detailsArea.setWrapStyleWord(true);
        detailsArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        JScrollPane detailsScroll = new JScrollPane(detailsArea);

        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setOpaque(false);

        // --- RSVP panel (NEW) ---
        JPanel rsvpPanel = buildRsvpPanel();

        // Split details (top) and RSVP panel (bottom)
        JSplitPane rightSplit = new JSplitPane(
                JSplitPane.VERTICAL_SPLIT,
                detailsScroll,
                rsvpPanel
        );
        rightSplit.setResizeWeight(0.6);
        rightSplit.setContinuousLayout(true);

        rightPanel.add(rightSplit, BorderLayout.CENTER);

        // --- Event actions: Activate / Close ---
        JPanel eventActions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        eventActions.setOpaque(false);

        JButton activateButton = createSecondaryButton("Activate");
        JButton closeButton    = createSecondaryButton("Close");

        activateButton.addActionListener(e -> changeEventStatus(true));
        closeButton.addActionListener(e -> changeEventStatus(false));

        eventActions.add(activateButton);
        eventActions.add(closeButton);

        rightPanel.add(eventActions, BorderLayout.SOUTH);

        // --- Main split: events list (left) + rightPanel (right) ---
        JSplitPane split = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                listScroll,
                rightPanel
        );
        split.setDividerLocation(260);
        split.setResizeWeight(0.3);
        split.setContinuousLayout(true);

        add(split, BorderLayout.CENTER);

        // selection listener
        eventList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int index = eventList.getSelectedIndex();
                showEventDetails(index);
            }
        });
    }

    // ------------------------------------------------------
    // Build RSVP panel UI
    // ------------------------------------------------------
    private JPanel buildRsvpPanel() {
        JPanel rsvpPanel = new JPanel(new BorderLayout(5, 5));
        rsvpPanel.setOpaque(false);
        rsvpPanel.setBorder(
                BorderFactory.createTitledBorder("Invitees & RSVPs")
        );

        // -------- Invitee list (top) --------
        rsvpListModel = new DefaultListModel<>();
        rsvpList = new JList<>(rsvpListModel);
        rsvpList.setVisibleRowCount(8);

        JScrollPane rsvpScroll = new JScrollPane(rsvpList);
        rsvpPanel.add(rsvpScroll, BorderLayout.CENTER);

        // -------- Bottom area: 2 columns --------
        JPanel bottom = new JPanel(new GridLayout(1, 2, 12, 0));
        bottom.setOpaque(false);

        /* ================= LEFT COLUMN ================= */
        JPanel leftCol = new JPanel();
        leftCol.setOpaque(false);
        leftCol.setLayout(new BoxLayout(leftCol, BoxLayout.Y_AXIS));

        leftCol.add(new JLabel("Name:"));
        inviteeNameField = new JTextField(14);
        leftCol.add(inviteeNameField);
        leftCol.add(Box.createVerticalStrut(8));

        leftCol.add(new JLabel("Email:"));
        inviteeEmailField = new JTextField(14);
        leftCol.add(inviteeEmailField);
        leftCol.add(Box.createVerticalStrut(8));

        leftCol.add(new JLabel("Phone:"));
        inviteePhoneField = new JTextField(14);
        leftCol.add(inviteePhoneField);

        /* ================= RIGHT COLUMN ================= */
        JPanel rightCol = new JPanel();
        rightCol.setOpaque(false);
        rightCol.setLayout(new BoxLayout(rightCol, BoxLayout.Y_AXIS));

        JButton addInviteeButton = createSecondaryButton("Add Invitee");
        addInviteeButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        addInviteeButton.addActionListener(e -> addInviteeForSelectedEvent());

        JPanel rsvpRow = new JPanel(new BorderLayout(6, 0));
        rsvpRow.setOpaque(false);
        rsvpRow.add(new JLabel("Set RSVP:"), BorderLayout.WEST);
        rsvpStatusCombo = new JComboBox<>(RSVPStatus.values());
        rsvpRow.add(rsvpStatusCombo, BorderLayout.CENTER);

        JButton updateRsvpButton =
                createPrimaryButton("Update RSVP for Selected Invitee");
        updateRsvpButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        updateRsvpButton.addActionListener(e -> updateRsvpForSelectedInvitee());

        rightCol.add(addInviteeButton);
        rightCol.add(Box.createVerticalStrut(12));
        rightCol.add(rsvpRow);
        rightCol.add(Box.createVerticalStrut(12));
        rightCol.add(updateRsvpButton);

        // Add columns to bottom
        bottom.add(leftCol);
        bottom.add(rightCol);

        rsvpPanel.add(bottom, BorderLayout.SOUTH);
        return rsvpPanel;
    }


    // ------------------------------------------------------
    // Refresh entire events list
    // ------------------------------------------------------
    public void refresh() {
        listModel.clear();
        detailsArea.setText("");
        eventObjects.clear();
        clearRsvpPanel();

        Agent agent = parent.getCurrentAgent();
        if (agent == null) return;

        java.util.List<House> houses = agent.getProperties();
        if (houses == null) return;

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMM dd yyyy HH:mm");

        for (House h : houses) {
            java.util.List<Event> evs = h.getEvents();
            if (evs == null) continue;
            for (Event e : evs) {
                eventObjects.add(e);
                String label = e.getEventId()
                        + " | " + h.getAddress()
                        + " | " + (e.getStartTime() != null ? e.getStartTime().format(fmt) : "No time");
                listModel.addElement(label);
            }
        }

        if (eventObjects.isEmpty()) {
            listModel.addElement("(No events yet)");
        }
    }

    // ------------------------------------------------------
    // Show event details + its RSVPs
    // ------------------------------------------------------
    private void showEventDetails(int index) {
        if (index < 0 || index >= eventObjects.size()) {
            detailsArea.setText("");
            clearRsvpPanel();
            return;
        }

        Event e = eventObjects.get(index);
        StringBuilder sb = new StringBuilder();
        sb.append("Event ID: ").append(e.getEventId()).append("\n");
        sb.append("Address: ").append(e.getAddress()).append("\n");
        sb.append("Start Time: ").append(e.getStartTime()).append("\n\n");

        sb.append("Capacity: ").append(e.getCapacity()).append("\n");
        sb.append("Attendance: ").append(e.getAttendance())
          .append(" / ").append(e.getCapacity()).append("\n");
        sb.append("Attendance Rate: ").append(e.getAttendanceRate()).append("\n\n");

        sb.append("Status:\n");
        sb.append("  Scheduled: ").append(e.isScheduled()).append("\n");
        sb.append("  Active: ").append(e.isActive()).append("\n");
        sb.append("  Closed: ").append(e.isClosed()).append("\n\n");

        sb.append("RSVP Summary:\n");
        sb.append("  YES: ").append(e.getRsvpCount(RSVPStatus.YES)).append("\n");
        sb.append("  NO: ").append(e.getRsvpCount(RSVPStatus.NO)).append("\n");
        sb.append("  MAYBE: ").append(e.getRsvpCount(RSVPStatus.MAYBE)).append("\n");
        sb.append("  NO RESPONSE: ").append(e.getRsvpCount(RSVPStatus.NO_RESPONSE)).append("\n");

        detailsArea.setText(sb.toString());
        detailsArea.setCaretPosition(0);

        // also refresh RSVP list for this event
        refreshRsvpList(e);
    }

    // ------------------------------------------------------
    // RSVP data helpers
    // ------------------------------------------------------
    private void clearRsvpPanel() {
        if (rsvpListModel != null) {
            rsvpListModel.clear();
        }
        rsvpVisitors.clear();
        if (inviteeNameField != null) inviteeNameField.setText("");
        if (inviteeEmailField != null) inviteeEmailField.setText("");
        if (inviteePhoneField != null) inviteePhoneField.setText("");
    }

    private void refreshRsvpList(Event e) {
        rsvpListModel.clear();
        rsvpVisitors.clear();

        if (e == null) return;

        java.util.Map<Visitor, RSVPStatus> all = e.getAllRsvps();
        for (java.util.Map.Entry<Visitor, RSVPStatus> entry : all.entrySet()) {
            Visitor v = entry.getKey();
            RSVPStatus status = entry.getValue();
            rsvpVisitors.add(v);

            String label = v.getName() + " <" + v.getEmail() + "> - " + status;
            rsvpListModel.addElement(label);
        }
    }

    // ------------------------------------------------------
    // Add invitee using addInvitee(...)
    // ------------------------------------------------------
    private void addInviteeForSelectedEvent() {
        int eventIndex = eventList.getSelectedIndex();
        if (eventIndex < 0 || eventIndex >= eventObjects.size()) {
            JOptionPane.showMessageDialog(
                    parent,
                    "Please select an event first.",
                    "No Event Selected",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        Event e = eventObjects.get(eventIndex);

        // 🚫 Block closed events with a popup
        if (e.isClosed()) {
            JOptionPane.showMessageDialog(
                    parent,
                    "This event is closed and cannot accept new invitees.",
                    "Event Closed",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        // Optional: if you *also* want to enforce scheduled/active rule in the GUI:
        if (!e.isScheduled() && !e.isActive()) {
            JOptionPane.showMessageDialog(
                    parent,
                    "This event is not yet scheduled or active.\n" +
                    "You can only add invitees to scheduled or open events.",
                    "Event Not Ready",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        String name = inviteeNameField.getText().trim();
        String email = inviteeEmailField.getText().trim();
        String phone = inviteePhoneField.getText().trim();

        if (name.isEmpty() || email.isEmpty()) {
            JOptionPane.showMessageDialog(
                    parent,
                    "Name and email are required for an invitee.",
                    "Input Error",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        Visitor v = new Visitor(name, email, phone);
        e.addInvitee(v);

        refreshRsvpList(e);

        inviteeNameField.setText("");
        inviteeEmailField.setText("");
        inviteePhoneField.setText("");
    }

    // ------------------------------------------------------
    // Update RSVP using setRsvp(...)
    // ------------------------------------------------------
    private void updateRsvpForSelectedInvitee() {
        int eventIndex = eventList.getSelectedIndex();
        if (eventIndex < 0 || eventIndex >= eventObjects.size()) {
            JOptionPane.showMessageDialog(
                    parent,
                    "Please select an event first.",
                    "No Event Selected",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        Event e = eventObjects.get(eventIndex);

        int inviteeIndex = rsvpList.getSelectedIndex();
        if (inviteeIndex < 0 || inviteeIndex >= rsvpVisitors.size()) {
            JOptionPane.showMessageDialog(
                    parent,
                    "Please select an invitee from the list.",
                    "No Invitee Selected",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        Visitor v = rsvpVisitors.get(inviteeIndex);
        RSVPStatus status = (RSVPStatus) rsvpStatusCombo.getSelectedItem();
        if (status == null) {
            JOptionPane.showMessageDialog(
                    parent,
                    "Please choose an RSVP status.",
                    "No Status Selected",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        e.setRsvp(v, status);
        refreshRsvpList(e);
        showEventDetails(eventIndex); // refresh summary counts too
    }
    private void changeEventStatus(boolean activate) {
        int index = eventList.getSelectedIndex();
        if (index < 0 || index >= eventObjects.size()) {
            JOptionPane.showMessageDialog(
                    parent,
                    "Please select an event first.",
                    "No Event Selected",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        Event e = eventObjects.get(index);

        if (activate) {
            // Re-open or activate the event
            e.schedule();   // ensure scheduled flag is true
            e.activate();   // sets active=true and (with your change) closed=false
        } else {
            // Permanently (or temporarily) close
            e.close();
        }

        // ✅ Refresh the displayed info
        showEventDetails(index);

        // ✅ Keep kiosk + check-ins in sync
        if (parent.kioskPanel != null) {
            parent.kioskPanel.refresh();
        }
        if (parent.checkInPanel != null) {
            parent.checkInPanel.refresh();
        }
    }

}

    
    
    
  //CREATES EVENT LOGIC FOR ADD EVENT BOX
private void createEventFromGui() {
    Agent agent = getCurrentAgent();
    if (agent == null) {
        JOptionPane.showMessageDialog(
                this,
                "No agent is currently logged in.",
                "Error",
                JOptionPane.ERROR_MESSAGE
        );
        return;
    }

    java.util.List<House> houses = agent.getProperties();
    if (houses == null || houses.isEmpty()) {
        JOptionPane.showMessageDialog(
                this,
                "You must add at least one house before creating an event.",
                "No Houses",
                JOptionPane.WARNING_MESSAGE
        );
        return;
    }

    // ---------- Build a single form panel ----------
    JPanel form = new JPanel(new GridBagLayout());
    form.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(4, 4, 4, 4);
    gbc.fill = GridBagConstraints.HORIZONTAL;

    // House selector
    gbc.gridx = 0;
    gbc.gridy = 0;
    form.add(new JLabel("House:"), gbc);

    gbc.gridx = 1;
    String[] houseOptions = new String[houses.size()];
    for (int i = 0; i < houses.size(); i++) {
        houseOptions[i] = houses.get(i).getAddress();
    }
    JComboBox<String> houseCombo = new JComboBox<>(houseOptions);
    houseCombo.setSelectedIndex(0);
    form.add(houseCombo, gbc);

    // Start date/time
    gbc.gridx = 0;
    gbc.gridy++;
    form.add(new JLabel("Start (yyyy-MM-dd HH:mm):"), gbc);

    gbc.gridx = 1;
    DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    // default suggestion: tomorrow at 13:00
    LocalDateTime defaultStart = LocalDateTime.now()
            .plusDays(1)
            .withHour(13)
            .withMinute(0)
            .withSecond(0)
            .withNano(0);
    JTextField dateTimeField = new JTextField(defaultStart.format(fmt), 18);
    form.add(dateTimeField, gbc);

    // Capacity
    gbc.gridx = 0;
    gbc.gridy++;
    form.add(new JLabel("Capacity:"), gbc);

    gbc.gridx = 1;
    JTextField capacityField = new JTextField("20", 10);
    form.add(capacityField, gbc);

    // Check-in code
    gbc.gridx = 0;
    gbc.gridy++;
    form.add(new JLabel("Check-in Code:"), gbc);

    gbc.gridx = 1;
    JTextField codeField = new JTextField("1234", 10);
    form.add(codeField, gbc);

    // ---------- Show dialog ----------
    int result = JOptionPane.showConfirmDialog(
            this,
            form,
            "Create Event",
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.PLAIN_MESSAGE
    );

    if (result != JOptionPane.OK_OPTION) {
        // User cancelled
        return;
    }

    // ---------- Read & validate inputs ----------
    String houseChoice = (String) houseCombo.getSelectedItem();
    if (houseChoice == null || houseChoice.trim().isEmpty()) {
        JOptionPane.showMessageDialog(
                this,
                "Please select a house.",
                "Input Error",
                JOptionPane.ERROR_MESSAGE
        );
        return;
    }

    House selectedHouse = null;
    for (House h : houses) {
        if (h.getAddress().equals(houseChoice)) {
            selectedHouse = h;
            break;
        }
    }
    if (selectedHouse == null) {
        JOptionPane.showMessageDialog(
                this,
                "Unable to find the selected house.",
                "Error",
                JOptionPane.ERROR_MESSAGE
        );
        return;
    }

    String dateTimeStr = dateTimeField.getText().trim();
    if (dateTimeStr.isEmpty()) {
        JOptionPane.showMessageDialog(
                this,
                "Start date/time cannot be empty.",
                "Input Error",
                JOptionPane.ERROR_MESSAGE
        );
        return;
    }

    LocalDateTime startTime;
    try {
        startTime = LocalDateTime.parse(dateTimeStr, fmt);
    } catch (DateTimeParseException ex) {
        JOptionPane.showMessageDialog(
                this,
                "Invalid date/time format. Please use yyyy-MM-dd HH:mm.",
                "Input Error",
                JOptionPane.ERROR_MESSAGE
        );
        return;
    }

    int capacity;
    try {
        capacity = Integer.parseInt(capacityField.getText().trim());
        if (capacity <= 0) throw new NumberFormatException("capacity <= 0");
    } catch (NumberFormatException ex) {
        JOptionPane.showMessageDialog(
                this,
                "Capacity must be a positive integer.",
                "Input Error",
                JOptionPane.ERROR_MESSAGE
        );
        return;
    }

    int checkInCode;
    try {
        checkInCode = Integer.parseInt(codeField.getText().trim());
    } catch (NumberFormatException ex) {
        JOptionPane.showMessageDialog(
                this,
                "Check-in code must be a valid integer.",
                "Input Error",
                JOptionPane.ERROR_MESSAGE
        );
        return;
    }

    // ---------- Create event via Agent ----------
    Event newEvent;
    try {
        newEvent = agent.createEvent(selectedHouse, startTime, capacity, checkInCode);
        if (newEvent == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "The event could not be created (Agent returned null).",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }
    } catch (Exception ex) {
        JOptionPane.showMessageDialog(
                this,
                "An unexpected error occurred while creating the event:\n" + ex.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE
        );
        ex.printStackTrace();
        return;
    }

    // ---------- Capture Agent printout (optional, same as before) ----------
    String info = captureConsoleOutput(() ->
            agent.printRsvpSummary(newEvent)
    );

    JTextArea infoArea = new JTextArea(info, 15, 50);
    infoArea.setEditable(false);
    infoArea.setLineWrap(true);
    infoArea.setWrapStyleWord(true);

    JScrollPane scroll = new JScrollPane(infoArea);
    scroll.setPreferredSize(new Dimension(500, 300));

    JOptionPane.showMessageDialog(
            this,
            scroll,
            "Event Created: " + newEvent.getEventId(),
            JOptionPane.INFORMATION_MESSAGE
    );

    // Refresh Events panel so new event appears
    if (eventsPanel != null) {
        eventsPanel.refresh();
    }
}



    
    
    // ======================================================
    //  CHECK-IN RECORDS PANEL
    // ======================================================
    private static class CheckInPanel extends JPanel {
        private OpenHouseManagerGUI parent;
        private JList<String> eventList;
        private DefaultListModel<String> listModel;
        private JTextArea detailsArea;
        private java.util.List<Event> eventObjects = new ArrayList<>();

        public CheckInPanel(OpenHouseManagerGUI parent) {
            this.parent = parent;
            setOpaque(false);
            setLayout(new BorderLayout(10, 10));

            // --------- HEADER ----------
            JPanel header = new JPanel(new BorderLayout());
            header.setOpaque(true);
            header.setBackground(new Color(255, 255, 255, 220));
            header.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));

            JLabel title = new JLabel("Check-in Records");
            title.setFont(new Font("Century Gothic", Font.BOLD, 20));
            title.setForeground(new Color(40, 40, 40));

            JPanel headerButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
            headerButtons.setOpaque(false);

            JButton backButton = createSecondaryButton("Back");
            backButton.addActionListener(e -> parent.showScreen(CARD_DASHBOARD));
            headerButtons.add(backButton);

            header.add(title, BorderLayout.WEST);
            header.add(headerButtons, BorderLayout.EAST);

            add(header, BorderLayout.NORTH);

            // --------- LEFT: EVENT LIST ----------
            listModel = new DefaultListModel<>();
            eventList = new JList<>(listModel);
            eventList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            eventList.setVisibleRowCount(18);

            JScrollPane listScroll = new JScrollPane(eventList);

            // --------- RIGHT: CHECK-IN DETAILS ----------
            detailsArea = new JTextArea();
            detailsArea.setEditable(false);
            detailsArea.setLineWrap(true);
            detailsArea.setWrapStyleWord(true);
            detailsArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));

            JScrollPane detailsScroll = new JScrollPane(detailsArea);

            JPanel rightPanel = new JPanel(new BorderLayout());
            rightPanel.setOpaque(false);
            rightPanel.add(detailsScroll, BorderLayout.CENTER);

            JSplitPane split = new JSplitPane(
                    JSplitPane.HORIZONTAL_SPLIT,
                    listScroll,
                    rightPanel
            );
            split.setDividerLocation(260);
            split.setResizeWeight(0.3);
            split.setContinuousLayout(true);

            add(split, BorderLayout.CENTER);

            eventList.addListSelectionListener(e -> {
                if (!e.getValueIsAdjusting()) {
                    showCheckIns(eventList.getSelectedIndex());
                }
            });
        }

        public void refresh() {
            listModel.clear();
            detailsArea.setText("");
            eventObjects.clear();

            Agent agent = parent.getCurrentAgent();
            if (agent == null) return;

            java.util.List<House> houses = agent.getProperties();
            if (houses == null) return;

            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMM dd yyyy HH:mm");

            for (House h : houses) {
                java.util.List<Event> evs = h.getEvents();
                if (evs == null) continue;
                for (Event e : evs) {
                    eventObjects.add(e);
                    String label = e.getEventId()
                            + " | " + h.getAddress()
                            + " | " + (e.getStartTime() != null ? e.getStartTime().format(fmt) : "No time");
                    listModel.addElement(label);
                }
            }

            if (eventObjects.isEmpty()) {
                listModel.addElement("(No check-ins yet)");
            }
        }

        private void showCheckIns(int index) {
            if (index < 0 || index >= eventObjects.size()) {
                detailsArea.setText("");
                return;
            }

            Event e = eventObjects.get(index);
            StringBuilder sb = new StringBuilder();
            sb.append("Event: ").append(e.getEventId())
              .append(" at ").append(e.getAddress()).append("\n\n");

            java.util.List<Visitor> visitors = e.getVisitors();
            if (visitors == null || visitors.isEmpty()) {
                sb.append("No visitors have checked in for this event.\n");
                detailsArea.setText(sb.toString());
                return;
            }

            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMM dd yyyy HH:mm");

            for (Visitor v : visitors) {
                java.util.List<CheckInRecord> history = v.getCheckInHistory();
                for (CheckInRecord record : history) {
                    if (record.getEvent() == e) {
                        sb.append("Visitor: ").append(v.getName())
                          .append("\n   Time: ").append(record.getTimestamp().format(fmt))
                          .append("\n   Email: ").append(v.getEmail())
                          .append("\n   Phone: ").append(v.getPhone())
                          .append("\n\n");
                    }
                }
            }

            detailsArea.setText(sb.toString());
            detailsArea.setCaretPosition(0);
        }
    }

    
    
    
    // ======================================================
    //  VISITORS PANEL (Directory + Check-in History)
    // ======================================================
    private static class VisitorsPanel extends JPanel {
        private OpenHouseManagerGUI parent;
        private JList<String> visitorList;
        private DefaultListModel<String> listModel;
        private JTextArea detailsArea;

        private java.util.List<Visitor> visitorObjects = new ArrayList<>();
        private JCheckBox mailingListBox;
        private boolean suppressMailingListEvents = false;

        public VisitorsPanel(OpenHouseManagerGUI parent) {
            this.parent = parent;
            setOpaque(false);
            setLayout(new BorderLayout(10, 10));

            // ----- HEADER -----
            JPanel header = new JPanel(new BorderLayout());
            header.setOpaque(true);
            header.setBackground(new Color(255, 255, 255, 220));
            header.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));

            JLabel title = new JLabel("Visitors");
            title.setFont(new Font("Century Gothic", Font.BOLD, 20));
            title.setForeground(new Color(40, 40, 40));

            JPanel headerButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
            headerButtons.setOpaque(false);
            JButton backButton = createSecondaryButton("Back");
            backButton.addActionListener(e -> parent.showScreen(CARD_DASHBOARD));
            headerButtons.add(backButton);

            header.add(title, BorderLayout.WEST);
            header.add(headerButtons, BorderLayout.EAST);

            add(header, BorderLayout.NORTH);

            // ----- LEFT: VISITOR LIST -----
            listModel = new DefaultListModel<>();
            visitorList = new JList<>(listModel);
            visitorList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            JScrollPane listScroll = new JScrollPane(visitorList);

            // ----- RIGHT: DETAILS + MAILING LIST CHECKBOX -----
            detailsArea = new JTextArea();
            detailsArea.setEditable(false);
            detailsArea.setLineWrap(true);
            detailsArea.setWrapStyleWord(true);
            detailsArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            JScrollPane detailsScroll = new JScrollPane(detailsArea);

            JPanel rightPanel = new JPanel(new BorderLayout());
            rightPanel.setOpaque(false);
            rightPanel.add(detailsScroll, BorderLayout.CENTER);

            JPanel mailingPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            mailingPanel.setOpaque(false);
            mailingListBox = new JCheckBox("Subscribed to mailing list");
            mailingListBox.setOpaque(false);
            mailingPanel.add(mailingListBox);
            rightPanel.add(mailingPanel, BorderLayout.SOUTH);

            // ----- SPLIT PANE -----
            JSplitPane split = new JSplitPane(
                    JSplitPane.HORIZONTAL_SPLIT,
                    listScroll,
                    rightPanel
            );
            split.setDividerLocation(260);
            split.setResizeWeight(0.3);
            split.setContinuousLayout(true);

            add(split, BorderLayout.CENTER);

            // List selection -> update details
            visitorList.addListSelectionListener(e -> {
                if (!e.getValueIsAdjusting()) {
                    showVisitorDetails(visitorList.getSelectedIndex());
                }
            });

            // Checkbox -> update mailing-list consent on Visitor
            mailingListBox.addActionListener(e -> {
                if (suppressMailingListEvents) return;
                int index = visitorList.getSelectedIndex();
                if (index < 0 || index >= visitorObjects.size()) return;
                Visitor v = visitorObjects.get(index);
                // adjust method name if different in your Visitor class
                v.setMailingListConsent(mailingListBox.isSelected());
            });
        }

        /** Rebuild list of unique visitors for the current agent. */
        public void refresh() {
            listModel.clear();
            detailsArea.setText("");
            visitorObjects.clear();

            mailingListBox.setSelected(false);
            mailingListBox.setEnabled(false);
            visitorList.setEnabled(true);

            Agent agent = parent.getCurrentAgent();
            if (agent == null) {
                listModel.addElement("(No agent logged in)");
                visitorList.setEnabled(false);
                return;
            }

            java.util.List<House> houses = agent.getProperties();
            if (houses == null || houses.isEmpty()) {
                listModel.addElement("(No houses / visitors yet)");
                visitorList.setEnabled(false);
                return;
            }

            // Use email as a key to dedupe visitors
            Map<String, Visitor> byEmail = new LinkedHashMap<>();

            for (House h : houses) {
                java.util.List<Event> evs = h.getEvents();
                if (evs == null) continue;
                for (Event e : evs) {
                    java.util.List<Visitor> vs = e.getVisitors();
                    if (vs == null) continue;
                    for (Visitor v : vs) {
                        if (v == null) continue;
                        String email = v.getEmail() == null ? "" : v.getEmail();
                        if (!byEmail.containsKey(email)) {
                            byEmail.put(email, v);
                        }
                    }
                }
            }

            if (byEmail.isEmpty()) {
                listModel.addElement("(No visitors yet)");
                visitorList.setEnabled(false);
                return;
            }

            mailingListBox.setEnabled(true);

            for (Visitor v : byEmail.values()) {
                visitorObjects.add(v);
                String label = v.getName() + " <" + v.getEmail() + ">";
                listModel.addElement(label);
            }

            // Optionally auto-select first visitor
            if (!visitorObjects.isEmpty()) {
                visitorList.setSelectedIndex(0);
                showVisitorDetails(0);
            }
        }

        /** Show details + history for the selected visitor. */
        private void showVisitorDetails(int index) {
            if (index < 0 || index >= visitorObjects.size()) {
                detailsArea.setText("");
                suppressMailingListEvents = true;
                mailingListBox.setSelected(false);
                suppressMailingListEvents = false;
                return;
            }

            Visitor v = visitorObjects.get(index);

            StringBuilder sb = new StringBuilder();
            sb.append("Name: ").append(v.getName()).append("\n");
            sb.append("Email: ").append(v.getEmail()).append("\n");
            sb.append("Phone: ").append(v.getPhone()).append("\n");

            // Adjust this getter name if your Visitor uses something different
            boolean onList = false;
            try {
                onList = v.hasMailingListConsent();   // <--- change to match your Visitor API if needed
            } catch (Exception ex) {
                // If no getter exists, just leave onList = false or replace with your own.
            }
            sb.append("Mailing List Consent: ").append(onList ? "Yes" : "No").append("\n\n");

            sb.append("Check-in History:\n");
            java.util.List<CheckInRecord> history = v.getCheckInHistory();
            if (history == null || history.isEmpty()) {
                sb.append("  (No check-in records)\n");
            } else {
                DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMM dd yyyy HH:mm");
                for (CheckInRecord rec : history) {
                    Event e = rec.getEvent();
                    sb.append("  • ")
                      .append(e.getEventId())
                      .append(" at ").append(e.getAddress())
                      .append(" on ").append(rec.getTimestamp().format(fmt))
                      .append("\n");
                }
            }

            detailsArea.setText(sb.toString());
            detailsArea.setCaretPosition(0);

            // Sync checkbox without firing the listener
            suppressMailingListEvents = true;
            mailingListBox.setSelected(onList);
            suppressMailingListEvents = false;
        }
    }

    
    
    // ======================================================
    //  EMAIL PANEL
    // ======================================================
    private static class EmailPanel extends JPanel {
    	private OpenHouseManagerGUI parent;

    	private JComboBox<String> eventCombo;
    	private java.util.List<Event> eventObjects = new ArrayList<>();

    	private JTextField subjectField;
    	private JTextArea bodyArea;

    	// NEW: recipients on the right
    	private DefaultListModel<String> recipientListModel;
    	private JList<String> recipientList;
    	private java.util.List<Visitor> recipientObjects = new ArrayList<>();
    	private JLabel recipientCountLabel;

    	public EmailPanel(OpenHouseManagerGUI parent) {
    	this.parent = parent;
    	setOpaque(false);
    	setLayout(new GridBagLayout()); // center the card

    	JPanel card = new JPanel(new BorderLayout(10, 10));
    	card.setOpaque(true);
    	card.setBackground(new Color(255, 255, 255, 230));
    	card.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

    	// ---------- HEADER ----------
    	JPanel header = new JPanel(new BorderLayout());
    	header.setOpaque(false);

    	JLabel title = new JLabel("Send Email to Event Visitors");
    	title.setFont(new Font("Century Gothic", Font.BOLD, 18));

    	JPanel headerButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
    	headerButtons.setOpaque(false);

    	JButton backButton = createSecondaryButton("Back");
    	backButton.addActionListener(e -> parent.showScreen(CARD_DASHBOARD));

    	headerButtons.add(backButton);

    	header.add(title, BorderLayout.WEST);
    	header.add(headerButtons, BorderLayout.EAST);

    	card.add(header, BorderLayout.NORTH);

    	// ---------- CENTER: LEFT FORM + RIGHT RECIPIENT LIST ----------
    	JPanel centerPanel = new JPanel(new BorderLayout(10, 0));
    	centerPanel.setOpaque(false);

    	// ---- LEFT: form ----
    	JPanel form = new JPanel();
    	form.setOpaque(false);
    	form.setLayout(new GridBagLayout());
    	GridBagConstraints gbc = new GridBagConstraints();
    	gbc.insets = new Insets(6, 4, 6, 4);
    	gbc.fill = GridBagConstraints.HORIZONTAL;
    	gbc.gridx = 0;
    	gbc.gridy = 0;
    	gbc.weightx = 0;

    	// Event selector
    	form.add(new JLabel("Event:"), gbc);
    	gbc.gridx = 1;
    	gbc.weightx = 1.0;

    	eventCombo = new JComboBox<>();
    	eventCombo.setPrototypeDisplayValue("Select an event...");
    	form.add(eventCombo, gbc);

    	// Subject
    	gbc.gridy++;
    	gbc.gridx = 0;
    	gbc.weightx = 0;
    	form.add(new JLabel("Subject:"), gbc);

    	gbc.gridx = 1;
    	gbc.weightx = 1.0;
    	subjectField = new JTextField();
    	form.add(subjectField, gbc);

    	// Body
    	gbc.gridy++;
    	gbc.gridx = 0;
    	gbc.gridwidth = 2;
    	gbc.weightx = 1.0;
    	gbc.fill = GridBagConstraints.BOTH;
    	gbc.weighty = 1.0;

    	bodyArea = new JTextArea(8, 40);
    	bodyArea.setLineWrap(true);
    	bodyArea.setWrapStyleWord(true);
    	JScrollPane bodyScroll = new JScrollPane(bodyArea);
    	form.add(bodyScroll, gbc);

    	centerPanel.add(form, BorderLayout.CENTER);

    	// ---- RIGHT: recipients ----
    	JPanel recipientsPanel = new JPanel(new BorderLayout(5, 5));
    	recipientsPanel.setOpaque(false);

    	JLabel recipientsTitle = new JLabel("Recipients");
    	recipientsTitle.setFont(new Font("Century Gothic", Font.BOLD, 14));
    	recipientsPanel.add(recipientsTitle, BorderLayout.NORTH);

    	recipientListModel = new DefaultListModel<>();
    	recipientList = new JList<>(recipientListModel);
    	recipientList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

    	JScrollPane recipientsScroll = new JScrollPane(recipientList);
    	recipientsScroll.setPreferredSize(new Dimension(260, 200));
    	recipientsPanel.add(recipientsScroll, BorderLayout.CENTER);

    	// bottom: select all / clear + count
    	JPanel recipientsBottom = new JPanel(new BorderLayout());
    	recipientsBottom.setOpaque(false);

    	JPanel recipientButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
    	recipientButtons.setOpaque(false);
    	JButton selectAllButton = createSecondaryButton("Select All");
    	JButton clearSelectionBtn = createSecondaryButton("Clear");

    	selectAllButton.addActionListener(e -> {
    	int size = recipientListModel.getSize();
    	if (size > 0) {
    	recipientList.setSelectionInterval(0, size - 1);
    	}
    	});

    	clearSelectionBtn.addActionListener(e -> recipientList.clearSelection());

    	recipientButtons.add(selectAllButton);
    	recipientButtons.add(clearSelectionBtn);

    	recipientCountLabel = new JLabel("Selected 0 of 0");
    	recipientCountLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));

    	recipientsBottom.add(recipientButtons, BorderLayout.WEST);
    	recipientsBottom.add(recipientCountLabel, BorderLayout.EAST);

    	recipientsPanel.add(recipientsBottom, BorderLayout.SOUTH);

    	centerPanel.add(recipientsPanel, BorderLayout.EAST);

    	card.add(centerPanel, BorderLayout.CENTER);

    	// ---------- FOOTER: SEND BUTTON ----------
    	JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    	footer.setOpaque(false);

    	JButton sendButton = createPrimaryButton("Prepare & Send Emails");
    	sendButton.addActionListener(e -> sendEmails());
    	footer.add(sendButton);

    	card.add(footer, BorderLayout.SOUTH);

    	// Center card in this panel
    	GridBagConstraints outer = new GridBagConstraints();
    	outer.gridx = 0;
    	outer.gridy = 0;
    	add(card, outer);

    	// When event changes, refresh recipients
    	eventCombo.addActionListener(e -> loadRecipientsForSelectedEvent());

    	// Update count when selection changes
    	recipientList.addListSelectionListener(e -> {
    	if (!e.getValueIsAdjusting()) {
    	updateRecipientCount();
    	}
    	});
    	}

    	// Called from showScreen(CARD_EMAIL)
    	public void refresh() {
    	eventCombo.removeAllItems();
    	eventObjects.clear();

    	recipientListModel.clear();
    	recipientObjects.clear();
    	updateRecipientCount();

    	Agent agent = parent.getCurrentAgent();
    	if (agent == null) return;

    	java.util.List<House> houses = agent.getProperties();
    	if (houses == null) return;

    	DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMM dd yyyy HH:mm");

    	for (House h : houses) {
    	java.util.List<Event> evs = h.getEvents();
    	if (evs == null) continue;
    	for (Event e : evs) {
    	eventObjects.add(e);
    	String label = e.getEventId()
    	+ " - " + h.getAddress()
    	+ " - " + (e.getStartTime() != null ? e.getStartTime().format(fmt) : "No time");
    	eventCombo.addItem(label);
    	}
    	}

    	// Auto-load recipients for first event, if any
    	if (!eventObjects.isEmpty()) {
    	eventCombo.setSelectedIndex(0);
    	loadRecipientsForSelectedEvent();
    	}
    	}

    	private void loadRecipientsForSelectedEvent() {
    	recipientListModel.clear();
    	recipientObjects.clear();

    	int idx = eventCombo.getSelectedIndex();
    	if (idx < 0 || idx >= eventObjects.size()) {
    	updateRecipientCount();
    	return;
    	}

    	Event event = eventObjects.get(idx);
    	java.util.List<Visitor> visitors = event.getVisitors();
    	if (visitors == null) {
    	updateRecipientCount();
    	return;
    	}

    	for (Visitor v : visitors) {
    	// If you later split first/last name, change this line accordingly
    	String display = v.getName() + " - " + v.getEmail();
    	recipientObjects.add(v);
    	recipientListModel.addElement(display);
    	}

    	// By default, select all recipients for convenience
    	if (!recipientObjects.isEmpty()) {
    	recipientList.setSelectionInterval(0, recipientObjects.size() - 1);
    	}

    	updateRecipientCount();
    	}

    	private void updateRecipientCount() {
    	int total = recipientListModel.getSize();
    	int selected = recipientList.getSelectedIndices().length;
    	recipientCountLabel.setText("Selected " + selected + " of " + total);
    	}

    	private void sendEmails() {
    	int eventIndex = eventCombo.getSelectedIndex();
    	if (eventIndex < 0 || eventIndex >= eventObjects.size()) {
    	JOptionPane.showMessageDialog(
    	parent,
    	"Please select an event.",
    	"Error",
    	JOptionPane.ERROR_MESSAGE
    	);
    	return;
    	}

    	String subject = subjectField.getText().trim();
    	String bodyTemplate = bodyArea.getText().trim();

    	if (subject.isEmpty() || bodyTemplate.isEmpty()) {
    	JOptionPane.showMessageDialog(
    	parent,
    	"Subject and body cannot be empty.",
    	"Error",
    	JOptionPane.ERROR_MESSAGE
    	);
    	return;
    	}

    	Agent agent = parent.getCurrentAgent();
    	if (agent == null) {
    	JOptionPane.showMessageDialog(
    	parent,
    	"No agent logged in.",
    	"Error",
    	JOptionPane.ERROR_MESSAGE
    	);
    	return;
    	}

    	// Use only the selected visitors
    	int[] selectedIdx = recipientList.getSelectedIndices();
    	if (selectedIdx.length == 0) {
    	JOptionPane.showMessageDialog(
    	parent,
    	"Please select at least one recipient.",
    	"No Recipients Selected",
    	JOptionPane.WARNING_MESSAGE
    	);
    	return;
    	}

    	java.util.List<Visitor> selectedVisitors = new ArrayList<>();
    	for (int i : selectedIdx) {
    	if (i >= 0 && i < recipientObjects.size()) {
    	selectedVisitors.add(recipientObjects.get(i));
    	}
    	}

    	Event event = eventObjects.get(eventIndex);

    	// NOTE: You need an overload in Agent like:
    	// ArrayList<Email> prepareEmailsForEvent(Event e, String sub, String body, List<Visitor> recipients)
    	ArrayList<Email> emails =
    	agent.prepareEmailsForEvent(event, subject, bodyTemplate, selectedVisitors);

    	MessagingService ms = new MessagingService();
    	ms.enqueueEmails(emails);
    	ms.sendAll();

    	JOptionPane.showMessageDialog(
    	parent,
    	"Prepared and sent " + emails.size() + " emails.",
    	"Emails Sent",
    	JOptionPane.INFORMATION_MESSAGE
    	);
    	}
    	}

    
    
    //Buttons that look cleaner than default swing buttons
    
    
    private static JButton createPrimaryButton(String text) {
        Color base = new Color(70, 110, 160);  // matte blue
        JButton btn = new JButton(text);
        btn.setBackground(base);
        btn.setForeground(Color.WHITE);
        styleButtonBase(btn, base);
        return btn;
    }
    
    
    

    private static JButton createSecondaryButton(String text) {
        Color base = new Color(255, 255, 255, 230); // soft white
        JButton btn = new JButton(text);
        btn.setBackground(base);
        btn.setForeground(new Color(40, 40, 40));
        styleButtonBase(btn, base);
        return btn;
    }
    
    
    
    //shared background panel for login and dashboard
    private static JPanel createCardPanel() {
        JPanel card = new JPanel();
        card.setOpaque(true);
        card.setBackground(new Color(255, 255, 255, 190)); // translucent white
        card.setBorder(BorderFactory.createEmptyBorder(25, 40, 25, 40));
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        return card;
    }

    /**
     * Capture anything printed to System.out while the given Runnable runs,
     * and return it as a String. System.out is restored afterwards. This is
     * the same type of strategy used in HW 4. (streamlined it)
     */
    public String captureConsoleOutput(Runnable printer) {
        PrintStream originalOut = System.out;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        System.setOut(ps);
        try {
            printer.run();
        } finally {
            System.setOut(originalOut);
            ps.close();
        }
        return baos.toString();
    }

    
    private static void styleButtonBase(JButton btn, Color baseColor) {
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(true);
        btn.setOpaque(true);
        btn.setFont(new Font("Century Gothic", Font.BOLD, 14));
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0, 0, 0, 40), 1, true),
                BorderFactory.createEmptyBorder(8, 24, 8, 24)
        ));

        // hover effect, always relative to *baseColor*
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                btn.setBackground(baseColor.brighter());
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                btn.setBackground(baseColor); // hard reset to base
            }
        });
    }
}

