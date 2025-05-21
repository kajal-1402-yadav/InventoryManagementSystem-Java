import java.util.*;
import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;

public class InventoryManagement {
    public static void main(String[] args) throws Exception {
        // Connection
        Class.forName("com.mysql.cj.jdbc.Driver");
        final Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/Inventorymanagement", "root", "");
        
       /*Tables are already made in phpMyAdmin :    customer (cust_id,cust_pass,cust_name,cust_email,cust_address) 
                                                    transaction (trans_id(A.I),cust_id,owner_id,product_id,quantity,amount,date)
                                                    product (product_id,product_name,price,quantity,owner_id)
                                                    owner (owner_id,owner_pass,owner_name,owner_email,owner_adress)
                                                    updateproductdetails(product_id,old_price,new_price,old_quantity,new_quantity)
        */
        
        Scanner sc = new Scanner(System.in);
        Customer customer = new Customer();
        Owner owner = new Owner();
        ProductLinkedList productList = new ProductLinkedList();
        
        System.out.println("Inventory Management System");
        boolean flag = true;
        while(flag) {
            System.out.println("1. Log In 2. Sign Up 3.Forgot Password 4. Exit");
            int choose = sc.nextInt();
            switch(choose) {
                case 1:
                    login(con, sc, owner, customer, productList);
                    break;
                case 2:
                    signup(con, sc, owner, customer, productList);
                    break;
                case 3:
                    forgotPassword(con, sc);
                    break;
                case 4:
                    System.out.println("Exiting Inventory Management System");
                    flag = false;
                    break;
                default:
                    System.out.println("Enter valid choice.");
                    break;
            }
        }
        sc.close();
        con.close();
    }
    
    static void signup(Connection con, Scanner sc, Owner owner, Customer customer, ProductLinkedList productList) throws Exception {
        System.out.println("\nSign Up Into Inventory Management System");
        System.out.println("1. Customer\n2. Owner");
        int user = sc.nextInt();
        sc.nextLine();
        switch (user) {
            case 1:
                customer.addCustomer(con, sc, productList);
                break;
            case 2:
                owner.addOwner(con, sc, productList);
                break;
            default:
                break;
        }
    }
    
    static void login(Connection con, Scanner sc, Owner owner, Customer customer, ProductLinkedList productList) throws Exception {
        System.out.println("\nLog In Into Inventory Management System");
        System.out.println("1. Customer\n2. Owner");
        int user = sc.nextInt();
        sc.nextLine();
        switch (user) {
            case 1:
                customer.loginCustomer(con, sc, productList);
                break;
            case 2:
                owner.loginOwner(con, sc, productList);
                break;
            default:
                break;
        }
    }

    static void forgotPassword(Connection con, Scanner sc) throws Exception {
        System.out.println("\nForgot Password");
        System.out.println("1. Customer\n2. Owner");
        int user = sc.nextInt();
        sc.nextLine();
        
        System.out.print("Enter your ID: ");
        int id = sc.nextInt();
        sc.nextLine();
        
        System.out.print("Enter your email: ");
        String email = sc.nextLine();
        
        String table, idColumn, emailColumn, passColumn;

        if (user == 1) {
            table = "customer";
            idColumn = "cust_id";
            emailColumn = "cust_email";
            passColumn = "cust_pass";
        } else if (user == 2) {
            table = "owner";
            idColumn = "owner_id";
            emailColumn = "owner_email";
            passColumn = "owner_pass";
        } else {
            System.out.println("Invalid user type.");
            return; 
        }
        
        String query = "SELECT " + idColumn + ", " + emailColumn + " FROM " + table + " WHERE " + idColumn + " = ? AND " + emailColumn + " = ?";
        PreparedStatement pst = con.prepareStatement(query);
        pst.setInt(1, id);
        pst.setString(2, email);
        ResultSet rs = pst.executeQuery();
        
        if (rs.next()) {
            String newPassword = getNewPassword(sc);
            String updateQuery = "UPDATE " + table + " SET " + passColumn + " = ? WHERE " + idColumn + " = ?";
            PreparedStatement updatePst = con.prepareStatement(updateQuery);
            updatePst.setString(1, newPassword);
            updatePst.setInt(2, id);
            int result = updatePst.executeUpdate();
            
            if (result > 0) {
                System.out.println("Your password has been successfully reset.");
            } else {
                System.out.println("Failed to reset password. Please try again.");
            }
        } else {
            System.out.println("No account found with the provided ID and email.");
        }
    }
    
    static String getNewPassword(Scanner sc) {
        while (true) {
            System.out.print("Enter new password (must be 8 characters long with 4 digits): ");
            String newPassword = sc.nextLine();
            
            int digitCount = 0;
            for (char c : newPassword.toCharArray()) {
                if (Character.isDigit(c)) {
                    digitCount++;
                }
            }
            
            if (newPassword.length() == 8 && digitCount == 4) {
                return newPassword;
            } else {
                System.out.println("Password must be 8 characters long with 4 digits. Please try again.");
            }
        }
    }
}

class Customer {
    void addCustomer(Connection con, Scanner sc, ProductLinkedList productList) throws Exception {
        System.out.print("Enter Id: ");
        int id = sc.nextInt();
        sc.nextLine();
        String pass = getPassword(sc);
        System.out.print("Enter Name: ");
        String name = sc.nextLine();
        System.out.print("Enter Email: ");
        String email = sc.nextLine();
        System.out.print("Enter Address: ");
        String address = sc.nextLine();
        String insertCust = "INSERT INTO customer VALUES (?,?,?,?,?)";
        PreparedStatement pst = con.prepareStatement(insertCust);
        pst.setInt(1, id);
        pst.setString(2, pass);
        pst.setString(3, name);
        pst.setString(4, email);
        pst.setString(5, address);
        int r = 0;
        try {
            r = pst.executeUpdate();
        } catch (Exception e) {
            System.out.println("Something went wrong. Please Retry.");
            addCustomer(con, sc, productList);
        }
        if (r > 0) {
            System.out.println("\nCustomer Signed Up Successfully.");
            customerOptions(con, sc, id, productList);
        }
    }

    String getPassword(Scanner sc) {
        boolean flag = true;
        String pass = "";

        while (flag) {
            System.out.print("Enter Password: ");
            pass = sc.nextLine();
            int digitCount = 0;
            for (char c : pass.toCharArray()) {
                if (Character.isDigit(c)) {
                    digitCount++;
                }
            }
            if (pass.length() == 8 && digitCount == 4) {
                flag = false;
            } else {
                System.out.println("Password must be 8 Characters Long with 4 Digits");
            }
        }
        return pass;
    }

    void loginCustomer(Connection con, Scanner sc, ProductLinkedList productList) throws Exception {
        System.out.print("Enter Id: ");
        int id = sc.nextInt();
        sc.nextLine();
        System.out.print("Enter Password: ");
        String pass = sc.nextLine();
        String find = "SELECT cust_id, cust_pass FROM customer WHERE cust_id=? AND cust_pass=?";
        PreparedStatement pst = con.prepareStatement(find);
        pst.setInt(1, id);
        pst.setString(2, pass);
        ResultSet rs = pst.executeQuery();
        if (rs.next()) {
            System.out.println("\nLogged In Successfully.");
            customerOptions(con, sc, id, productList);
        } else {
            System.out.println("Customer not found.");
        }
    }

    void customerOptions(Connection con, Scanner sc, int cust_id, ProductLinkedList productList) throws Exception {
        Transaction transaction = new Transaction();
        boolean flag = true;
        while (flag) {
            System.out.println("\n1. View Product\n2. Make Transaction\n3. Cancel Transaction\n4. View Transaction\n5. Exit");
            int ch = sc.nextInt();
            switch (ch) {
                case 1:
                    transaction.viewProduct(con, productList);
                    break;
                case 2:
                    transaction.makeTransaction(con, sc, cust_id);
                    break;
                case 3:
                    transaction.delTransaction(con, sc);
                    break;
                case 4:
                    transaction.viewTransaction(con, cust_id);
                    break;
                case 5:
                    flag = false;
                    System.out.println("Exiting");
                    break;
                default:
                    System.out.println("Enter valid choice.");
            }
        }
    }
}

class Transaction {
    void viewProduct(Connection con, ProductLinkedList productList) throws SQLException {
        String getProducts = "SELECT * FROM product";
        PreparedStatement pst = con.prepareStatement(getProducts);
        ResultSet rs = pst.executeQuery();
        while (rs.next()) {
            Product p = new Product(rs.getInt(1), rs.getString(2), rs.getDouble(3), rs.getInt(4));
            productList.add(p);
        }
        productList.sortByQuantity();
        if (productList.isEmpty()) {
            System.out.println("No Product Available.");
        } else {
            System.out.println("Products:");
            for (int i = 0; i < productList.size(); i++) {
                Product product = productList.get(i);
                System.out.println("Id: " + product.id);
                System.out.println("Name: " + product.name);
                System.out.println("Price: " + product.price);
                System.out.println("Quantity: " + product.quantity);
                System.out.println();
            }
            productList.clear();
        }
    }

    void makeTransaction(Connection con, Scanner sc, int cust_id) throws Exception {
        System.out.print("Enter Product Id: ");
        int pid = sc.nextInt();
        sc.nextLine();
        String find = "SELECT product_id, quantity, price, owner_id FROM product WHERE product_id=?";
        PreparedStatement pt = con.prepareStatement(find);
        pt.setInt(1, pid);
        ResultSet rs = pt.executeQuery();
        if (rs.next()) {
            System.out.print("Enter quantity: ");
            int buy_quantity = sc.nextInt();
            if (rs.getInt(2) < buy_quantity) {
                System.out.println("Insufficient Quantity.");
            } else {
                String update = "UPDATE product SET quantity = quantity - ? WHERE product_id = ?";
                PreparedStatement pst = con.prepareStatement(update);
                pst.setInt(1, buy_quantity);
                pst.setInt(2, pid);
                int r = pst.executeUpdate();
                if (r > 0) {
                    LocalDate localdate = LocalDate.now();
                    Date d = Date.valueOf(localdate);
                    String addTransaction = "INSERT INTO transaction (cust_id, owner_id, product_id, quantity, amount, date) VALUES (?, ?, ?, ?, ?, ?)";
                    PreparedStatement ps = con.prepareStatement(addTransaction, Statement.RETURN_GENERATED_KEYS);
                    ps.setInt(1, cust_id);
                    ps.setInt(2, rs.getInt(4));
                    ps.setInt(3, pid);
                    ps.setInt(4, buy_quantity);
                    ps.setDouble(5, rs.getDouble(3) * buy_quantity);
                    ps.setDate(6, d);
                    int t = ps.executeUpdate();
                    
                    if (t > 0) {
                        ResultSet generatedKeys = ps.getGeneratedKeys();
                        if (generatedKeys.next()) {
                            int transId = generatedKeys.getInt(1);
                            System.out.println("Transaction Successful. Your Transaction ID is: " + transId);
                        }
                    } else {
                        System.out.println("Transaction Failed.");
                    }
                }
            }
        } else {
            System.out.println("Product ID not Found.");
        }
    }
    

    void delTransaction(Connection con, Scanner sc) throws Exception {
        System.out.print("Enter Transaction ID to delete: ");
        int transId = sc.nextInt();
        String find = "SELECT * FROM transaction WHERE trans_id=?";
        PreparedStatement pt = con.prepareStatement(find);
        pt.setInt(1, transId);
        ResultSet rs = pt.executeQuery();
        if (rs.next()) {
            String delete = "DELETE FROM transaction WHERE trans_id=?";
            PreparedStatement pst = con.prepareStatement(delete);
            pst.setInt(1, transId);
            int r = pst.executeUpdate();
            if (r > 0) {
                System.out.println("Transaction Deleted.");
            } else {
                System.out.println("Deletion Failed.");
            }
        } else {
            System.out.println("Transaction ID not Found.");
        }
    }

    void viewTransaction(Connection con, int cust_id) throws SQLException {
        String getTransactions = "SELECT * FROM transaction WHERE cust_id=?";
        PreparedStatement pst = con.prepareStatement(getTransactions);
        pst.setInt(1, cust_id);
        ResultSet rs = pst.executeQuery();
        boolean hasTransaction = false;
        while (rs.next()) {
            hasTransaction = true;
            System.out.println("Transaction ID: " + rs.getInt(1));
            System.out.println("Product ID: " + rs.getInt(4));
            System.out.println("Quantity: " + rs.getInt(5));
            System.out.println("Amount: " + rs.getDouble(6));
            System.out.println("Date: " + rs.getDate(7));
            System.out.println();
        }
        if (!hasTransaction) {
            System.out.println("No Transaction Available.");
        }
    }
}

class Owner {
    void addOwner(Connection con, Scanner sc, ProductLinkedList productList) throws Exception {
        System.out.print("Enter Id: ");
        int id = sc.nextInt();
        sc.nextLine();
        String pass = getPassword(sc);
        System.out.print("Enter Name: ");
        String name = sc.nextLine();
        System.out.print("Enter Email: ");
        String email = sc.nextLine();
        System.out.print("Enter Address: ");
        String address = sc.nextLine();
        String insertOwner = "INSERT INTO owner VALUES (?,?,?,?,?)";
        PreparedStatement pst = con.prepareStatement(insertOwner);
        pst.setInt(1, id);
        pst.setString(2, pass);
        pst.setString(3, name);
        pst.setString(4, email);
        pst.setString(5, address);
        int r = 0;
        try {
            r = pst.executeUpdate();
        } catch (Exception e) {
            System.out.println("Something went wrong. Please Retry.");
            addOwner(con, sc, productList);
        }
        if (r > 0) {
            System.out.println("\nOwner Signed Up Successfully.");
            ownerOptions(con, sc, id, productList);
        }
        
    }

    String getPassword(Scanner sc) {
        boolean flag = true;
        String pass = "";

        while (flag) {
            System.out.print("Enter Password: ");
            pass = sc.nextLine();
            int digitCount = 0;
            for (char c : pass.toCharArray()) {
                if (Character.isDigit(c)) {
                    digitCount++;
                }
            }
            if (pass.length() == 8 && digitCount == 4) {
                flag = false;
            } else {
                System.out.println("Password must be 8 Characters Long with 4 Digits");
            }
        }
        return pass;
    }

    void loginOwner(Connection con, Scanner sc, ProductLinkedList productList) throws Exception {
        System.out.print("Enter Id: ");
        int id = sc.nextInt();
        sc.nextLine();
        System.out.print("Enter Password: ");
        String pass = sc.nextLine();
        String find = "SELECT owner_id, owner_pass FROM owner WHERE owner_id=? AND owner_pass=?";
        PreparedStatement pst = con.prepareStatement(find);
        pst.setInt(1, id);
        pst.setString(2, pass);
        ResultSet rs = pst.executeQuery();
        if (rs.next()) {
            System.out.println("\nLogged In Successfully.");
            ownerOptions(con, sc, id, productList);
        } else {
            System.out.println("Owner not found.");
        }
    }

    void ownerOptions(Connection con, Scanner sc, int owner_id, ProductLinkedList productList) throws Exception {
        boolean flag = true;
        while (flag) {
            System.out.println("\n1. Add Product\n2. Update Product\n3. Delete Product\n4. View Products\n5. Display Customers\n6. Exit");
            int ch = sc.nextInt();
            switch (ch) {
                case 1:
                    addProduct(con, sc, owner_id);
                    break;
                case 2:
                    updateProduct(con, sc);
                    break;    
                case 3:
                    deleteProduct(con, sc);
                    break;
                case 4:
                    viewProduct(con, productList);
                    break;
                case 5:
                    displayCustomer(con);
                    break;
                case 6:
                    flag = false;
                    System.out.println("Exiting");
                    break;
                default:
                    System.out.println("Enter valid choice.");
            }
        }
    }

    void addProduct(Connection con, Scanner sc, int owner_id) throws Exception {
        System.out.print("Enter Product Id: ");
        int id = sc.nextInt();
        sc.nextLine();
        System.out.print("Enter Product Name: ");
        String name = sc.nextLine();
        System.out.print("Enter Product Price: ");
        double price = sc.nextDouble();
        System.out.print("Enter Product Quantity: ");
        int quantity = sc.nextInt();
        String insertProduct = "INSERT INTO product VALUES (?,?,?,?,?)";
        PreparedStatement pst = con.prepareStatement(insertProduct);
        pst.setInt(1, id);
        pst.setString(2, name);
        pst.setDouble(3, price);
        pst.setInt(4, quantity);
        pst.setInt(5, owner_id);
        int r = 0;
        try {
            r = pst.executeUpdate();
        } catch (Exception e) {
            System.out.println("Duplicate Id! Please try again.");
        }
        if (r > 0) {
            System.out.println("Product Added Successfully.");
        }
    }

    void updateProduct(Connection con, Scanner sc) throws Exception {
        System.out.print("Enter Product Id to Update : ");
        int id = sc.nextInt();
        sc.nextLine();
        System.out.print("Enter New Price : ");
        double price = sc.nextDouble();
        System.out.print("Enter New Quantity : ");
        int quantity = sc.nextInt();
        String updateProduct = "UPDATE product SET price = ?, quantity = ? WHERE product_id = ?";
        PreparedStatement pst = con.prepareStatement(updateProduct);
        pst.setDouble(1, price);
        pst.setInt(2, quantity);
        pst.setInt(3, id);
        int r = 0;
        try {
            r = pst.executeUpdate();
        } catch (Exception e) {
            System.out.println("Something went wrong.");
        }
        if (r > 0) {
            System.out.println("Product Updated Successfully.");
        } else {
            System.out.println("Product Update Failed.");
        }
    }

    void deleteProduct(Connection con, Scanner sc) throws Exception {
        System.out.print("Enter Product Id to Delete: ");
        int id = sc.nextInt();
        String deleteProduct = "DELETE FROM product WHERE product_id=?";
        PreparedStatement pst = con.prepareStatement(deleteProduct);
        pst.setInt(1, id);
        int r = 0;
        try {
            r = pst.executeUpdate();
        } catch (Exception e) {
            System.out.println("Something went wrong.");
        }
        if (r > 0) {
            System.out.println("Product Deleted Successfully.");
        } else {
            System.out.println("Deletion Failed.");
        }
    }

    void displayCustomer(Connection con) throws SQLException {
        String getCustomers = "SELECT * FROM customer";
        PreparedStatement pst = con.prepareStatement(getCustomers);
        ResultSet rs = pst.executeQuery();
        boolean hasCustomer = false;
        while (rs.next()) {
            hasCustomer = true;
            System.out.println("Customer ID: " + rs.getInt(1));
            System.out.println("Name: " + rs.getString(3));
            System.out.println("Email: " + rs.getString(4));
            System.out.println("Address: " + rs.getString(5));
            System.out.println();
        }
        if (!hasCustomer) {
            System.out.println("No Customers Available.");
        }
    }

    void viewProduct(Connection con, ProductLinkedList productList) throws SQLException {
        //CALLABLE STATEMNET
        String getProducts = "call selectData()";
        PreparedStatement pst = con.prepareStatement(getProducts);
        ResultSet rs = pst.executeQuery();
        while (rs.next()) {
            Product p = new Product(rs.getInt(1), rs.getString(2), rs.getDouble(3), rs.getInt(4));
            productList.add(p);
        }
        productList.sortByQuantity();
        if (productList.isEmpty()) {
            System.out.println("No Product Available.");
        } else {
            System.out.println("Products:");
            for (int i = 0; i < productList.size(); i++) {
                Product product = productList.get(i);
                System.out.println("Id: " + product.id);
                System.out.println("Name: " + product.name);
                System.out.println("Price: " + product.price);
                System.out.println("Quantity: " + product.quantity);
                System.out.println();
            }
            productList.clear();
        }
    }
}

class Product {
    int id;
    String name;
    double price;
    int quantity;

    public Product(int id, String name, double price, int quantity) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
    }

    public int getQuantity() {
        return quantity;
    }
}

class ProductLinkedList {
    Node first;
    int size;

    class Node {
        Product product;
        Node next;

        Node(Product product) {
            this.product = product;
            this.next = null;
        }
    }

    public ProductLinkedList() {
        first = null;
        size = 0;
    }

    public void add(Product p) {
        Node n = new Node(p);
        if (first == null) {
            first = n;
        } else {
            Node temp = first;
            while (temp.next != null) {
                temp = temp.next;
            }
            temp.next = n;
        }
        size++;
    }

    public Product get(int index) {
        if (index >= size || index < 0) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }
        Node temp = first;
        for (int i = 0; i < index; i++) {
            temp = temp.next;
        }
        return temp.product;
    }

    public void clear() {
        first = null;
        size = 0;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public int size() {
        return size;
    }
    
    public void display() {
        Node temp = first;
        if (temp == null) {
            System.out.println("No products available.");
        } else {
            while (temp != null) {
                Product p = temp.product;
                System.out.println("Id: " + p.id);
                System.out.println("Name: " + p.name);
                System.out.println("Price: " + p.price);
                System.out.println("Quantity: " + p.quantity);
                System.out.println();
                temp = temp.next;
            }
        }
    }

    public void sortByQuantity() {
        if (first == null || first.next == null) {
            return; 
        }

        List<Product> tempList = new ArrayList<>();
        Node temp = first;
        while (temp != null) {
            tempList.add(temp.product);
            temp = temp.next;
        }

        tempList.sort(Comparator.comparing(Product::getQuantity).reversed());
        clear();
        for (Product product : tempList) {
            add(product);
        }
    }
}