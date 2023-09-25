import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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
}

class ShoppingCart {
    private List<Product> items = new ArrayList<>();

    public void addItem(Product product) {
        items.add(product);
    }

    public void removeItem(Product product) {
        items.remove(product);
    }

    public List<Product> getItems() {
        return items;
    }

    public double getTotalPrice() {
        double total = 0;
        for (Product product : items) {
            total += product.getPrice();
        }
        return total;
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

class BakeryManagementApp extends JFrame {
    private List<Product> catalog = new ArrayList<>();
    private ShoppingCart cart = new ShoppingCart();
    private JList<String> catalogList;
    private DefaultListModel<String> catalogListModel;
    private JList<String> cartList;
    private DefaultListModel<String> cartListModel;
    private JLabel totalLabel;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton registerButton;
    private User currentUser;

    private JPanel loginPanel;
    private JPanel mainPanel;
    private CardLayout cardLayout;

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

        // Add both panels to the main panel
        mainPanel.add(loginPanel, "LoginPanel");
        mainPanel.add(mainContentPanel, "MainContentPanel");

        // Show the login panel by default
        cardLayout.show(mainPanel, "LoginPanel");

        add(mainPanel, BorderLayout.CENTER);
    }

    private void createLoginPanel() {
        loginPanel = new JPanel(new GridLayout(4, 2));
        usernameField = new JTextField(20);
        passwordField = new JPasswordField(20);
        loginButton = new JButton("Login");
        registerButton = new JButton("Register");

        loginPanel.add(new JLabel("Username:"));
        loginPanel.add(usernameField);
        loginPanel.add(new JLabel("Password:"));
        loginPanel.add(passwordField);
        loginPanel.add(loginButton);
        loginPanel.add(registerButton);

        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = usernameField.getText();
                char[] passwordChars = passwordField.getPassword();
                String password = new String(passwordChars);

                User user = UserDatabase.getUserByUsername(username);

                if (user != null && user.getPassword().equals(password)) {
                    currentUser = user;
                    cardLayout.show(mainPanel, "MainContentPanel"); // Switch to the main content panel
                } else {
                    JOptionPane.showMessageDialog(BakeryManagementApp.this, "Invalid username or password.");
                }

                usernameField.setText("");
                passwordField.setText("");
            }
        });

        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = usernameField.getText();
                char[] passwordChars = passwordField.getPassword();
                String password = new String(passwordChars);

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
    }

    private JPanel createMainContentPanel() {
        JPanel mainContentPanel = new JPanel(new BorderLayout());

        catalogListModel = new DefaultListModel<>();
        catalogList = new JList<>(catalogListModel);
        JScrollPane catalogScrollPane = new JScrollPane(catalogList);
        mainContentPanel.add(catalogScrollPane, BorderLayout.CENTER);

        catalog.add(new Product("Chocolate Cake", "Delicious chocolate cake", 15.99));
        catalog.add(new Product("Blueberry Muffin", "Freshly baked blueberry muffin", 2.99));
        catalog.add(new Product("Croissant", "Flaky and buttery croissant", 3.49));

        for (Product product : catalog) {
            catalogListModel.addElement(product.getName() + " - $" + product.getPrice());
        }

        catalogList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JButton addToCartButton = new JButton("Add to Cart");
        mainContentPanel.add(addToCartButton, BorderLayout.SOUTH);

        cartListModel = new DefaultListModel<>();
        cartList = new JList<>(cartListModel);
        JScrollPane cartScrollPane = new JScrollPane(cartList);
        mainContentPanel.add(cartScrollPane, BorderLayout.EAST);

        totalLabel = new JLabel("Total: $0.00");
        mainContentPanel.add(totalLabel, BorderLayout.NORTH);

        addToCartButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedIndex = catalogList.getSelectedIndex();
                if (selectedIndex >= 0) {
                    Product selectedProduct = catalog.get(selectedIndex);
                    cart.addItem(selectedProduct);
                    cartListModel.addElement(selectedProduct.getName() + " - $" + selectedProduct.getPrice());
                    updateTotal();
                }
            }
        });

        return mainContentPanel;
    }

    private void updateTotal() {
        double total = cart.getTotalPrice();
        totalLabel.setText("Total: $" + String.format("%.2f", total));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                BakeryManagementApp bakeryApp = new BakeryManagementApp();
                bakeryApp.setVisible(true);
            }
        });
    }
}
