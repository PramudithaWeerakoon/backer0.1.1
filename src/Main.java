import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.*;
import java.util.List;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.event.*;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.*;
import javax.swing.Timer.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

class Product {
    private String name;
    private String description;
    private double price;

    public Product(String name, String description, double price) {
        this.name = name;
        this.description = description;
        this.price = price;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public double getPrice() {
        return price;
    }

    @Override
    public String toString() {
        return getName();
    }
}

class ShoppingCartItem {
    private Product product;
    private int quantity;

    public ShoppingCartItem(Product product, int quantity) {
        this.product = product;
        this.quantity = quantity;
    }

    public Product getProduct() {
        return product;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getTotalPrice() {
        return product.getPrice() * quantity;
    }
}

class User {
    private String username;
    private String password;

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}

class UserDatabase {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/bakery";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "";

    public static void addUser(User user) {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO user (name, password) VALUES (?, ?)")) {
            preparedStatement.setString(1, user.getUsername());
            preparedStatement.setString(2, user.getPassword());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static User getUserByUsername(String username) {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM user WHERE name = ?")) {
            preparedStatement.setString(1, username);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    String password = resultSet.getString("password");
                    return new User(username, password);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}

class Cart {
    private List<ShoppingCartItem> items = new ArrayList<>();

    public List<ShoppingCartItem> getItems() {
        return items;
    }
}

class BakeryManagementApp extends JFrame {
    private List<Product> catalog = new ArrayList<>();
    private Connection connection;
    private JList<String> catalogList;
    private DefaultListModel<String> catalogListModel;
    private JTable cartTable;
    private DefaultTableModel cartTableModel;
    private JLabel totalLabel;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton registerButton;
    private JButton checkoutButton;
    private User currentUser;
    private Cart cart = new Cart();
    private String loggedInUsername;

    private JPanel loginPanel;
    private JPanel mainPanel;
    private JPanel checkoutPanel;
    private JPanel bannerPanel; // New panel to display the banner and "Show All" button
    private CardLayout cardLayout;
    private double totalAmount = 0.0;

    private String[] bannerImages = {
            "C:/Users/Pramuditha/OneDrive/Desktop/New project/277000-easy-vanilla-cake-ddmfs-1X2-0101-8ac1f0aed3294888921a87d9bce9c43c.jpg",
            "C:/Users/Pramuditha/OneDrive/Desktop/New project/Cream-Cheese-Danish-Pastry-11.jpg",
            "C:/Users/Pramuditha/OneDrive/Desktop/New project/hamburger-buns-TRR-1200-19-of-21.jpg",
            "C:/Users/Pramuditha/OneDrive/Desktop/New project/Japanese-Milk-Bread-Shokupan-I-1.jpg",
            "C:/Users/Pramuditha/OneDrive/Desktop/New project/Shortbread-cookies-Recipe-New.jpg"
    };

    public BakeryManagementApp() {
        setTitle("Online Bakery Management System");
        setSize(800, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Create a main panel and set CardLayout
        mainPanel = new JPanel();
        cardLayout = new CardLayout();
        mainPanel.setLayout(cardLayout);

        // Create a login panel
        createLoginPanel();

        // Create the main content panel
        JPanel mainContentPanel = createMainContentPanel();

        // Create the checkout panel
        checkoutPanel = createCheckoutPanel();

        // Create the banner panel
        createBannerPanel();

        // Add all panels to the main panel
        mainPanel.add(loginPanel, "LoginPanel");
        mainPanel.add(mainContentPanel, "MainContentPanel");
        mainPanel.add(checkoutPanel, "CheckoutPanel");
        mainPanel.add(bannerPanel, "BannerPanel");

        // Show the login panel by default
        cardLayout.show(mainPanel, "LoginPanel");

        add(mainPanel, BorderLayout.CENTER);
    }

    private void createLoginPanel() {
        loginPanel = new JPanel() {
            private float alpha = 0.0f;

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                // Load and draw the background image
                ImageIcon backgroundImage = new ImageIcon("C:\\Users\\Pramuditha\\OneDrive\\Desktop\\New project\\anuga_2023_bread_bakery.png");
                g.drawImage(backgroundImage.getImage(), 0, 0, getWidth(), getHeight(), this);

                // Apply transparency for a fade-in effect
                Graphics2D g2d = (Graphics2D) g;
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
                alpha += 0.02f; // Increase alpha for a fade-in effect

                if (alpha >= 1.0f) {
                    alpha = 1.0f; // Limit alpha to 1.0
                }
            }
        };

        // Use a layout manager for the login panel
        loginPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5); // Add spacing between components

        // Create and position the components on the login panel
        JLabel nameLabel = new JLabel("ANUGA BREAD & BAKERY");
        nameLabel.setFont(new Font("Arial", Font.BOLD, 24));
        gbc.gridwidth = 2; // Span 2 columns
        gbc.gridx = 0;
        gbc.gridy = 0;
        loginPanel.add(nameLabel, gbc);

        JLabel usernameLabel = new JLabel("Username:");
        usernameLabel.setFont(new Font("Arial", Font.BOLD, 16));
        gbc.gridwidth = 1; // Reset gridwidth
        gbc.gridy = 1;
        loginPanel.add(usernameLabel, gbc);

        usernameField = new JTextField(20);
        gbc.gridx = 1;
        loginPanel.add(usernameField, gbc);

        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setFont(new Font("Arial", Font.BOLD, 16));
        gbc.gridx = 0;
        gbc.gridy = 2;
        loginPanel.add(passwordLabel, gbc);

        passwordField = new JPasswordField(20);
        gbc.gridx = 1;
        loginPanel.add(passwordField, gbc);

        loginButton = new JButton("Login");
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2; // Span 2 columns
        gbc.fill = GridBagConstraints.HORIZONTAL; // Expand horizontally
        loginPanel.add(loginButton, gbc);

        registerButton = new JButton("Register");
        gbc.gridy = 4;
        loginPanel.add(registerButton, gbc);

        // Customize button appearance
        loginButton.setBackground(new Color(0, 128, 0)); // Dark green
        loginButton.setForeground(Color.WHITE);
        loginButton.setBorderPainted(false); // Remove button border

        registerButton.setBackground(new Color(0, 0, 128)); // Dark blue
        registerButton.setForeground(Color.WHITE);
        registerButton.setBorderPainted(false); // Remove button border

        // Set button background color on hover
        loginButton.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                loginButton.setBackground(new Color(0, 160, 0)); // Light green
            }

            public void mouseExited(MouseEvent e) {
                loginButton.setBackground(new Color(0, 128, 0)); // Dark green
            }
        });

        registerButton.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                registerButton.setBackground(new Color(0, 0, 160)); // Light blue
            }

            public void mouseExited(MouseEvent e) {
                registerButton.setBackground(new Color(0, 0, 128)); // Dark blue
            }
        });

        loginButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String username = usernameField.getText();
                char[] passwordChars = passwordField.getPassword();
                String password = new String(passwordChars);

                if (username.isEmpty() || password.isEmpty()) {
                    JOptionPane.showMessageDialog(BakeryManagementApp.this, "Please enter both username and password.");
                    return;
                }
                User user = UserDatabase.getUserByUsername(username);

                if (user != null && user.getPassword().equals(password)) {
                    currentUser = user;
                    loggedInUsername = username; // Set loggedInUsername when the user logs in successfully
                    cardLayout.show(mainPanel, "BannerPanel"); // Switch to the banner panel
                } else {
                    JOptionPane.showMessageDialog(BakeryManagementApp.this, "Invalid username or password.");
                }

                usernameField.setText("");
                passwordField.setText("");
            }
        });

        registerButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String username = usernameField.getText();
                char[] passwordChars = passwordField.getPassword();
                String password = new String(passwordChars);

                if (username.isEmpty() || password.isEmpty()) {
                    JOptionPane.showMessageDialog(BakeryManagementApp.this, "Please enter both username and password.");
                    return;
                }

                User existingUser = UserDatabase.getUserByUsername(username);

                if (existingUser != null) {
                    JOptionPane.showMessageDialog(BakeryManagementApp.this, "Username already exists.");
                } else {
                    User newUser = new User(username, password);
                    UserDatabase.addUser(newUser);
                    JOptionPane.showMessageDialog(BakeryManagementApp.this, "Registration successful!");
                }

                usernameField.setText("");
                passwordField.setText("");
            }
        });

        // Add the login panel to the main panel
        mainPanel.add(loginPanel, "LoginPanel");

        javax.swing.Timer timer = new javax.swing.Timer(50, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loginPanel.repaint();
            }
        });
        timer.start();
    }


    private JPanel createMainContentPanel() {
        JPanel mainContentPanel = new JPanel(new BorderLayout());

        catalogListModel = new DefaultListModel<>();
        catalogList = new JList<>(catalogListModel);
        JScrollPane catalogScrollPane = new JScrollPane(catalogList);
        JLabel mainTotalLabel = new JLabel("Total: $0.00");
        mainContentPanel.add(catalogScrollPane, BorderLayout.CENTER);

        catalog.add(new Product("Chocolate Cake", "<html><img src='file:C:\\Users\\Pramuditha\\OneDrive\\Desktop\\New project\\triple-chocolate-cake-4.jpg' width='50' height='50'> Chocolate Cake - $15.99</html>", 15.99));
        catalog.add(new Product("Blueberry Muffin", "<html><img src='file:C:\\Users\\Pramuditha\\OneDrive\\Desktop\\New project\\Sour-Cream-Blueberry-Muffins-Recipe-4.jpg' width='50' height='50'> Blueberry Muffin - $2.99</html>", 2.99));
        catalog.add(new Product("Croissant", "<html><img src='file:C:\\Users\\Pramuditha\\OneDrive\\Desktop\\New project\\The-Ultimate-Croissant-Sandwich-Recipe-SQUARE2.jpg' width='50' height='50'> Croissant - $3.49</html>", 3.49));

        for (Product product : catalog) {
            catalogListModel.addElement(product.getDescription());
        }

        catalogList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JButton addToCartButton = new JButton("Add to Cart");
        mainContentPanel.add(addToCartButton, BorderLayout.SOUTH);

        cartTableModel = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 2; // Only allow editing quantity column
            }
        };

        cartTableModel.addColumn("Product");
        cartTableModel.addColumn("Price");
        cartTableModel.addColumn("Quantity");
        cartTableModel.addColumn("Total");

        cartTable = new JTable(cartTableModel);
        cartTable.getColumnModel().getColumn(2).setCellRenderer(new ButtonRenderer());
        cartTable.getColumnModel().getColumn(2).setCellEditor(new ButtonEditor(new JCheckBox()));
        JScrollPane cartScrollPane = new JScrollPane(cartTable);
        mainContentPanel.add(cartScrollPane, BorderLayout.EAST);

        totalLabel = new JLabel("Total: $0.00");
        mainContentPanel.add(totalLabel, BorderLayout.NORTH);

        addToCartButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int selectedIndex = catalogList.getSelectedIndex();
                if (selectedIndex >= 0) {
                    Product selectedProduct = catalog.get(selectedIndex);

                    // Ask for quantity
                    String quantityString = JOptionPane.showInputDialog("Enter quantity:");
                    if (quantityString != null && !quantityString.isEmpty()) {
                        try {
                            int quantity = Integer.parseInt(quantityString);
                            if (quantity > 0) {
                                // Check if the product is already in the cart
                                boolean productExistsInCart = false;
                                for (ShoppingCartItem item : cart.getItems()) {
                                    if (item.getProduct().getName().equals(selectedProduct.getName())) {
                                        item.setQuantity(item.getQuantity() + quantity);
                                        productExistsInCart = true;
                                        break;
                                    }
                                }

                                if (!productExistsInCart) {
                                    ShoppingCartItem newItem = new ShoppingCartItem(selectedProduct, quantity);
                                    cart.getItems().add(newItem);
                                }

                                updateCartTable();
                                updateTotal();
                            } else {
                                JOptionPane.showMessageDialog(BakeryManagementApp.this, "Quantity must be greater than zero.");
                            }
                        } catch (NumberFormatException ex) {
                            JOptionPane.showMessageDialog(BakeryManagementApp.this, "Invalid quantity format.");
                        }
                    }
                }
                updateTotal();
                mainTotalLabel.setText("Total: $" + String.format("%.2f", totalAmount));
            }
        });

        checkoutButton = new JButton("Checkout");
        mainContentPanel.add(checkoutButton, BorderLayout.WEST);

        try {
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/bakery", "root", "");
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle database connection error
        }

        checkoutButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (!cart.getItems().isEmpty()) {
                    // Extract the total amount from the total label
                    String totalLabelText = totalLabel.getText();
                    String totalAmountString = totalLabelText.replace("Total: $", "");
                    double totalAmount = Double.parseDouble(totalAmountString);

                    String username = loggedInUsername; // Use the currently logged-in username

                    try {
                        for (ShoppingCartItem item : cart.getItems()) {
                            String productName = item.getProduct().getName();
                            double price = item.getProduct().getPrice();
                            int quantity = item.getQuantity();
                            double totalPrice = item.getTotalPrice();

                            // Check if the product already exists in the database for this user
                            PreparedStatement checkStatement = connection.prepareStatement(
                                    "SELECT * FROM product WHERE username = ? AND product = ?"
                            );
                            checkStatement.setString(1, username);
                            checkStatement.setString(2, productName);
                            if (checkStatement.executeQuery().next()) {
                                // Update the existing entry
                                PreparedStatement updateStatement = connection.prepareStatement(
                                        "UPDATE product SET price = ?, quantity = ?, total = ? WHERE username = ? AND product= ?"
                                );
                                updateStatement.setDouble(1, price);
                                updateStatement.setInt(2, quantity);
                                updateStatement.setDouble(3, totalAmount); // Use the extracted total amount
                                updateStatement.setString(4, username);
                                updateStatement.setString(5, productName);
                                updateStatement.executeUpdate();
                            } else {
                                // Insert a new entry
                                PreparedStatement insertStatement = connection.prepareStatement(
                                        "INSERT INTO product (username, product, price, quantity, total) VALUES (?, ?, ?, ?, ?)"
                                );
                                insertStatement.setString(1, username);
                                insertStatement.setString(2, productName);
                                insertStatement.setDouble(3, price);
                                insertStatement.setInt(4, quantity);
                                insertStatement.setDouble(5, totalAmount); // Use the extracted total amount
                                insertStatement.executeUpdate();
                            }
                        }
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                        // Handle SQL error
                    }

                    CardLayout cardLayout = (CardLayout) mainPanel.getLayout();
                    cardLayout.show(mainPanel, "CheckoutPanel");
                } else {
                    JOptionPane.showMessageDialog(BakeryManagementApp.this, "Cart is empty. Add items to the cart first.");
                }
            }
        });


        // Set background and foreground colors for components
        mainContentPanel.setBackground(Color.WHITE);
        catalogList.setBackground(Color.WHITE);
        catalogList.setSelectionBackground(Color.ORANGE);
        addToCartButton.setBackground(Color.ORANGE);
        addToCartButton.setForeground(Color.WHITE);
        cartTable.setBackground(Color.WHITE);
        checkoutButton.setBackground(Color.RED);
        checkoutButton.setForeground(Color.WHITE);

        return mainContentPanel;
    }

    private void updateCartTable() {
        cartTableModel.setRowCount(0);
        for (ShoppingCartItem item : cart.getItems()) {
            Product product = item.getProduct();
            int quantity = item.getQuantity();
            double totalPrice = product.getPrice() * quantity;
            cartTableModel.addRow(new Object[]{product.getName(), product.getPrice(), quantity, totalPrice});
        }
    }

    private void updateTotal() {
        double total = 0;
        for (ShoppingCartItem item : cart.getItems()) {
            total += item.getTotalPrice();
        }
        totalLabel.setText("Total: $" + String.format("%.2f", total));
    }
    private void createBannerPanel() {
        bannerPanel = new JPanel(new BorderLayout());

        // Create a banner label with the banner image
        ImageIcon bannerIcon = new ImageIcon("C:\\Users\\Pramuditha\\OneDrive\\Desktop\\New project\\12.jpeg");
        JLabel bannerLabel = new JLabel(bannerIcon);

        bannerPanel.add(bannerLabel, BorderLayout.WEST); // Place the banner label on the left

        // Set background color for the banner panel
        bannerPanel.setBackground(Color.WHITE);

        // Add a mouse listener to the banner label
        bannerLabel.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int x = e.getX();
                int y = e.getY();
                // Check if the click is within the specified bounds (35, 227, 265, 70)
                if (x >= 35 && x <= 300 && y >= 227 && y <= 297) {
                    cardLayout.show(mainPanel, "MainContentPanel"); // Show the main panel
                }
            }
        });

        // Create a list of slideshow image paths (Replace with your image paths)
        String[] slideshowImages = {
                "C:\\Users\\Pramuditha\\OneDrive\\Desktop\\New project\\Cream-Cheese-Danish-Pastry-11.jpg",
                "C:\\Users\\Pramuditha\\OneDrive\\Desktop\\New project\\hamburger-buns-TRR-1200-19-of-21.jpg",
                "C:\\Users\\Pramuditha\\OneDrive\\Desktop\\New project\\Japanese-Milk-Bread-Shokupan-I-1.jpg",
                "C:\\Users\\Pramuditha\\OneDrive\\Desktop\\New project\\Shortbread-cookies-Recipe-New.jpg",
                "C:\\Users\\Pramuditha\\OneDrive\\Desktop\\New project\\Sour-Cream-Blueberry-Muffins-Recipe-4.jpg",
                "C:\\Users\\Pramuditha\\OneDrive\\Desktop\\New project\\The-Ultimate-Croissant-Sandwich-Recipe-SQUARE2.jpg",
                "C:\\Users\\Pramuditha\\OneDrive\\Desktop\\New project\\triple-chocolate-cake-4.jpg",
                // Add more image paths as needed
        };

        JPanel slideshowPanel = new JPanel(new BorderLayout());
        JLabel slideshowLabel = new JLabel();

        javax.swing.Timer slideshowTimer = new javax.swing.Timer(0, null); // Create an initially stopped timer

        slideshowTimer.addActionListener(new ActionListener() {
            private int currentIndex = 0;

            @Override
            public void actionPerformed(ActionEvent e) {
                // Load the current slideshow image and set it as the label's icon
                ImageIcon slideshowIcon = new ImageIcon(slideshowImages[currentIndex]);
                slideshowLabel.setIcon(slideshowIcon);
                currentIndex++;

                // Restart the slideshow if we've reached the end of the images
                if (currentIndex >= slideshowImages.length) {
                    currentIndex = 0;
                }
            }
        });

        slideshowPanel.add(slideshowLabel, BorderLayout.CENTER);

        // Add the slideshowPanel to the bannerPanel on the right
        bannerPanel.add(slideshowPanel, BorderLayout.CENTER);

        // Add the bannerPanel to the main panel
        mainPanel.add(bannerPanel, "BannerPanel");

        // Start the slideshow timer as soon as the panel loads
        slideshowTimer.setDelay(4000); // Delay in milliseconds
        slideshowTimer.setInitialDelay(0); // Start immediately
        slideshowTimer.start();

        // Create an "About Us" button
        JButton aboutUsButton = new JButton("About Us");
        aboutUsButton.setFont(new Font("Arial", Font.PLAIN, 16));
        aboutUsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cardLayout.show(mainPanel, "AboutUsPanel"); // Switch to the About Us panel
            }
        });

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setOpaque(false); // Make the button panel transparent
        buttonPanel.add(aboutUsButton);
        bannerPanel.add(buttonPanel, BorderLayout.SOUTH);

        mainPanel.add(bannerPanel, "BannerPanel");

        // Create an "About Us" panel
        JPanel aboutUsPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                try {
                    BufferedImage backgroundImage = ImageIO.read(new File("C:\\Users\\Pramuditha\\OneDrive\\Desktop\\New project\\aboutusbackground.jpg"));
                    g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };

        aboutUsPanel.setLayout(new BorderLayout());

        // Create a label with black text for the "About Us" content
        JLabel aboutUsTextLabel = new JLabel("<html><body><div style='text-align: center; color: black; font-size: 14px;'>"
                + "Welcome to ANUGA BREAD & BAKERY!<br><br>"
                + "We take pride in creating delicious pastries and baked goods "
                + "with the finest ingredients. Our bakery is located in the heart of Europe, "
                + "where we have been serving our customers for decades. "
                + "We are committed to delivering quality and taste in every bite. "
                + "Visit us today and experience the joy of our delectable treats!<br><br>"
                + "Here's what sets us apart:<br>"
                + "<ul>"
                + "<li>Handcrafted with love and care</li>"
                + "<li>Wide variety of freshly baked bread</li>"
                + "<li>Specialty cakes for every occasion</li>"
                + "<li>Seasonal and local ingredients</li>"
                + "</ul>"
                + "</div></body></html>");


        aboutUsTextLabel.setHorizontalAlignment(SwingConstants.CENTER); // Center the text
        aboutUsPanel.add(aboutUsTextLabel, BorderLayout.CENTER);

        // Create a back button with custom styling
        JButton backButton = new JButton("Back");
        backButton.setFont(new Font("Arial", Font.BOLD, 18)); // Bold text
        backButton.setForeground(Color.black); // Set text color to white
        backButton.setBackground(Color.white); // Set the background color to black
        backButton.setBorder(BorderFactory.createLineBorder(Color.black, 2)); // Add a slightly larger rectangular border
        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cardLayout.show(mainPanel, "BannerPanel"); // Switch back to the banner panel
            }
        });

        // Create a panel to contain the back button and position it in the top-left corner
        JPanel backButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        backButtonPanel.setOpaque(false); // Make the panel transparent
        backButtonPanel.add(backButton);
        aboutUsPanel.add(backButtonPanel, BorderLayout.NORTH);

        mainPanel.add(aboutUsPanel, "AboutUsPanel");
    }

    private JPanel createCheckoutPanel() {
        JPanel checkoutPanel = new JPanel(new BorderLayout());

        // Payment Method
        JPanel paymentPanel = new JPanel(new GridLayout(2, 2));
        JLabel paymentLabel = new JLabel("Payment Method:");
        JRadioButton cardRadio = new JRadioButton("Card");
        JRadioButton cashRadio = new JRadioButton("Cash on Delivery");

        // Address Details
        JPanel addressPanel = new JPanel(new GridLayout(3, 2));
        JLabel nameLabel = new JLabel("Full Name:");
        JTextField nameField = new JTextField(20);
        JLabel phoneLabel = new JLabel("Phone Number:");
        JTextField phoneField = new JTextField(20);
        JLabel promoLabel = new JLabel("Promo Code:");
        JTextField promoField = new JTextField(20);

        // Group the payment radio buttons
        ButtonGroup paymentGroup = new ButtonGroup();
        paymentGroup.add(cardRadio);
        paymentGroup.add(cashRadio);

        paymentPanel.add(paymentLabel);
        paymentPanel.add(cardRadio);
        paymentPanel.add(new JLabel("")); // Empty label for spacing
        paymentPanel.add(cashRadio);

        addressPanel.add(nameLabel);
        addressPanel.add(nameField);
        addressPanel.add(phoneLabel);
        addressPanel.add(phoneField);
        addressPanel.add(promoLabel);
        addressPanel.add(promoField);

        JTextArea detailsTextArea = new JTextArea();
        detailsTextArea.setEditable(false);
        JScrollPane detailsScrollPane = new JScrollPane(detailsTextArea);

        // Total Label with initial text
        JLabel totalCheckoutLabel = new JLabel("Total: Calculating...");
        checkoutPanel.add(totalCheckoutLabel, BorderLayout.NORTH);

        // Back Button
        JButton backButton = new JButton("Back to Cart");
        checkoutPanel.add(backButton, BorderLayout.WEST);

        // Confirm Checkout Button
        JButton confirmButton = new JButton("Confirm Checkout");
        checkoutPanel.add(paymentPanel, BorderLayout.CENTER);
        checkoutPanel.add(addressPanel, BorderLayout.SOUTH);
        checkoutPanel.add(detailsScrollPane, BorderLayout.CENTER);
        checkoutPanel.add(confirmButton, BorderLayout.EAST);

        confirmButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (!cart.getItems().isEmpty()) {
                    // Get the selected payment method
                    String paymentMethod = cardRadio.isSelected() ? "Card" : "Cash on Delivery";

                    // Get address details
                    String fullName = nameField.getText();
                    String phoneNumber = phoneField.getText();
                    String promoCode = promoField.getText();

                    // Calculate and set the total sum
                    double total = calculateTotal(cart.getItems());
                    totalCheckoutLabel.setText("Total: $" + String.format("%.2f", total));

                    // Save cart details to the database
                    saveCartToDatabase(currentUser.getUsername(), cart.getItems(), paymentMethod, fullName, phoneNumber, promoCode);
                    JOptionPane.showMessageDialog(BakeryManagementApp.this, "Checkout successful. Cart details saved to the database.");
                    cart.getItems().clear();
                    updateCartTable();
                    updateTotal();
                    cardLayout.show(mainPanel, "MainContentPanel");
                } else {
                    JOptionPane.showMessageDialog(BakeryManagementApp.this, "Cart is empty. Add items to the cart first.");
                }
            }
        });

        // Back Button Action
        backButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                cardLayout.show(mainPanel, "MainContentPanel");
            }
        });

        // Set background and foreground colors for components
        checkoutPanel.setBackground(Color.WHITE);
        paymentPanel.setBackground(Color.WHITE);
        addressPanel.setBackground(Color.WHITE);
        cardRadio.setBackground(Color.WHITE);
        cashRadio.setBackground(Color.WHITE);
        backButton.setBackground(Color.BLUE);
        backButton.setForeground(Color.WHITE);
        confirmButton.setBackground(Color.GREEN);
        confirmButton.setForeground(Color.WHITE);

        return checkoutPanel;
    }

    private double calculateTotal(List<ShoppingCartItem> items) {
        double total = 0;
        for (ShoppingCartItem item : items) {
            total += item.getTotalPrice();
        }
        return total;
    }

    private void saveCartToDatabase(String customerUsername, List<ShoppingCartItem> cartItems, String paymentMethod, String fullName, String phoneNumber, String promoCode) {
        String DB_URL = "jdbc:mysql://localhost:3306/bakery";
        String DB_USER = "root";
        String DB_PASSWORD = "";

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement preparedStatement = connection.prepareStatement(
                     "INSERT INTO bill (customer_ID, name_of_products, Total, payment_method, full_name, phone_number, promo_code) VALUES (?, ?, ?, ?, ?, ?, ?)")) {

            // Create a comma-separated list of product names and quantities from the cart
            StringBuilder productsStringBuilder = new StringBuilder();
            double total = 0;

            for (ShoppingCartItem item : cartItems) {
                Product product = item.getProduct();
                int quantity = item.getQuantity();

                if (productsStringBuilder.length() > 0) {
                    productsStringBuilder.append(", ");
                }

                productsStringBuilder.append(product.getName()).append(" x ").append(quantity);
                total += product.getPrice() * quantity;
            }

            String nameOfProducts = productsStringBuilder.toString();

            preparedStatement.setString(1, customerUsername);
            preparedStatement.setString(2, nameOfProducts);
            preparedStatement.setDouble(3, total);
            preparedStatement.setString(4, paymentMethod);
            preparedStatement.setString(5, fullName);
            preparedStatement.setString(6, phoneNumber);
            preparedStatement.setString(7, promoCode);

            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Custom ButtonRenderer class for rendering buttons in the quantity column
    class ButtonRenderer extends DefaultTableCellRenderer {
        private JButton button = new JButton("Edit");

        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            if (isSelected) {
                button.setForeground(table.getSelectionForeground());
                button.setBackground(table.getSelectionBackground());
            } else {
                button.setForeground(table.getForeground());
                button.setBackground(UIManager.getColor("Button.background"));
            }
            return button;
        }
    }

    // Custom ButtonEditor class for editing buttons in the quantity column
    class ButtonEditor extends DefaultCellEditor {
        private JButton button;
        private String label;
        private boolean isClicked;
        private JTable table;
        private int row;
        private int column;

        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton();
            button.setOpaque(true);
            button.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    String currentQuantity = (String) button.getText();
                    String newQuantity = JOptionPane.showInputDialog(BakeryManagementApp.this, "Enter new quantity:", currentQuantity);
                    if (newQuantity != null) {
                        if ("0".equals(newQuantity)) {
                            // Delete the whole line
                            cart.getItems().remove(row);
                            cartTableModel.removeRow(row);
                        } else {
                            button.setText(newQuantity);
                            // Update the quantity in the cart
                            int newQuantityInt;
                            try {
                                newQuantityInt = Integer.parseInt(newQuantity);
                            } catch (NumberFormatException ex) {
                                // Handle invalid input
                                return;
                            }
                            ShoppingCartItem item = cart.getItems().get(row);
                            item.setQuantity(newQuantityInt);
                            // Calculate and update the total for this specific item
                            double itemTotal = item.getProduct().getPrice() * newQuantityInt;
                            cartTableModel.setValueAt(itemTotal, row, 3); // Update the total column
                            // Recalculate and update the overall total
                            updateTotal();
                        }
                    }
                }
            });
        }

        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            this.table = table;
            this.row = row;
            this.column = column;

            if (isSelected) {
                button.setForeground(table.getSelectionForeground());
                button.setBackground(table.getSelectionBackground());
            } else {
                button.setForeground(table.getForeground());
                button.setBackground(table.getBackground());
            }
            button.setText((value == null) ? "" : value.toString());
            return button;
        }

        public Object getCellEditorValue() {
            return button.getText();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                BakeryManagementApp app = new BakeryManagementApp();
                app.setVisible(true);
            }
        });
    }
}

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            BakeryManagementApp app = new BakeryManagementApp();
            app.setVisible(true);
        });
    }
}