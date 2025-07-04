/*Assumptions:
-all prices, weights, and quantities input are positive
-expiry date format is assumed to be correct (YYYY-MM-DD)
-non-expirable products have an empty expiry date
-shipping fee is the same regardless of place or order size
-only one customer is tested (single user)
-items that are not shippable are assumed to have weight = 0
*/
import java.util.*;
import java.text.*;
import java.time.*;

class Product {
    String name;
    double price;
    int quantity;
    boolean expirable; // can be expired
    boolean shippable;
    double weight; // if shippable
    String expiryDate; // used only if expirable is true (YYYY-MM-DD)

    public Product(String name, double price, int quantity, boolean expirable, boolean shippable, double weight, String expiryDate) {
        this.name = name;
        this.price = price;
        this.quantity = quantity;
        this.expirable = expirable;
        this.shippable = shippable;
        this.weight = weight;
        this.expiryDate = expiryDate;
    }
}

class CartItem {
    Product P; //product
    int Q; //quantity

    public CartItem(Product P, int Q) {
        this.P = P;
        this.Q = Q;
    }
}

interface ShippableItem {
    String getName();
    double getWeight();
    int getQuantity();
}

class ShippingItem implements ShippableItem {
    String name;
    int quantity;
    double totalWeight;

    public ShippingItem(String name, int quantity, double totalWeight) {
        this.name = name;
        this.quantity = quantity;
        this.totalWeight = totalWeight;
    }

    public String getName() {
        return name;
    }

    public double getWeight() {
        return totalWeight;
    }
    public int getQuantity() {
    return quantity;
}
}

class ShippingService {
    public void shipItems(List<ShippableItem> items) {
        if (items.isEmpty()) return;

        System.out.println("** Shipment notice **");
        double totalWeight = 0;

        for (int i = 0; i < items.size(); i++) {
            ShippableItem item = items.get(i);
            System.out.println(item.getQuantity() + "x " + item.getName() + "     " + item.getWeight() + "kg");
            totalWeight += item.getWeight();
        }

        System.out.printf("Total package weight " + totalWeight + "kg");
    }
}

class Cart {
    List<CartItem> cartItems = new ArrayList<>();
    static int ShippingFee = 30;
    static double customerBalance = 600; // if test case did not set customer balance

    public void addToCart(Product product, int quantity) {
        if (quantity > product.quantity) {
            throw new RuntimeException("Invalid quantity for product: " + product.name);
        } else if (product.expirable && isExpired(product.expiryDate)) {
            throw new RuntimeException(product.name + " is expired.");
        } else {
            cartItems.add(new CartItem(product, quantity));
        }
    }

    public void checkout() {
        if (cartItems.isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        double subtotal = 0;
        List<ShippableItem> toBeShipped = new ArrayList<>();

        for (int i=0;i<cartItems.size();i++) {
            subtotal += cartItems.get(i).P.price * cartItems.get(i).Q;

            if (cartItems.get(i).P.shippable) {
                ShippingItem ship = new ShippingItem(cartItems.get(i).P.name, cartItems.get(i).Q, cartItems.get(i).P.weight * cartItems.get(i).Q);
                toBeShipped.add(ship);
            }
        }

        double total = subtotal + ShippingFee;
        if (total > customerBalance) {
            throw new RuntimeException("Total " + total + " is greater than balance " + customerBalance);
        }

        new ShippingService().shipItems(toBeShipped);

        // checkout receipt
        System.out.println("\n \n ** Checkout receipt **");
        for (int i=0;i<cartItems.size();i++) {
            System.out.println(cartItems.get(i).Q + "x " + cartItems.get(i).P.name + " " + cartItems.get(i).P.price * cartItems.get(i).Q);
        }
        System.out.println("----------------------");
        System.out.println("Subtotal "+ subtotal);
        System.out.println("Shipping "+ ShippingFee);
        System.out.println("Amount "+ total);

        customerBalance -= total;
        System.out.println("Customer Remaining Balance "+ customerBalance);
    }

    boolean isExpired(String expiryDate) {
        LocalDate today = LocalDate.now();
        try {
            LocalDate exp = LocalDate.parse(expiryDate);
            return today.isAfter(exp);
        } catch (Exception e) {
            return false; // if date is malformed or empty, treat as not expired
        }
    }
}

public class Main {
    static void runTest(Runnable test) {
        try {
            test.run();
            System.out.println("\n Checkout passed.");
        } catch (RuntimeException e) {
            System.out.println("Checkout failed: " + e.getMessage());
        }
        System.out.println("------------------------------------------------------");
    }
    public static void main(String[] args) {
        System.out.println("===== TEST CASE 1: Normal purchase =====");
        runTest(() -> {
            Cart.customerBalance = 600;
            Cart myCart = new Cart();
            Product cheese = new Product("Cheese", 100, 5, true, true, 0.2, "2025-07-06");
            Product biscuits = new Product("Biscuits", 150, 3, true, true, 0.7, "2025-07-10");
            Product scratchCard = new Product("Scratch Card", 50, 10, false, false, 0.0, "");
            myCart.addToCart(cheese, 2);
            myCart.addToCart(biscuits, 1);
            myCart.addToCart(scratchCard, 1);
            myCart.checkout();
        });

        System.out.println("\n===== TEST CASE 2: Expired product =====");
        runTest(() -> {
            Cart.customerBalance = 500;
            Cart myCart = new Cart();
            Product expiredMilk = new Product("Milk", 50, 5, true, true, 1.0, "2020-01-01");
            myCart.addToCart(expiredMilk, 1);
            myCart.checkout();
        });

        System.out.println("\n===== TEST CASE 3: Quantity > stock =====");
        runTest(() -> {
            Cart.customerBalance = 500;
            Cart myCart = new Cart();
            Product chips = new Product("Chips", 20, 3, false, false, 0.0, "");
            myCart.addToCart(chips, 5); // Exceeds available
            myCart.checkout();
        });

        System.out.println("\n===== TEST CASE 4: Insufficient balance =====");
        runTest(() -> {
            Cart.customerBalance = 50;
            Cart myCart = new Cart();
            Product tv = new Product("TV", 300, 3, false, true, 5.0, "");
            myCart.addToCart(tv, 1);
            myCart.checkout();
        });

        System.out.println("\n===== TEST CASE 5: Empty cart =====");
        runTest(() -> {
            Cart.customerBalance = 500;
            Cart myCart = new Cart();
            myCart.checkout();
        });

        System.out.println("\n===== TEST CASE 6: Only scratch card (non-shippable) =====");
        runTest(() -> {
            Cart.customerBalance = 100;
            Cart myCart = new Cart();
            Product scratch = new Product("Scratch Card", 50, 10, false, false, 0.0, "");
            myCart.addToCart(scratch, 1);
            myCart.checkout();
        });

        System.out.println("\n===== TEST CASE 7: All items are shippable =====");
        runTest(() -> {
            Cart.customerBalance = 800;
            Cart myCart = new Cart();
            Product phone = new Product("Phone", 200, 3, false, true, 0.4, "");
            Product speaker = new Product("Speaker", 150, 2, false, true, 1.0, "");
            myCart.addToCart(phone, 2);
            myCart.addToCart(speaker, 1);
            myCart.checkout();
        });

        System.out.println("\n===== TEST CASE 8: Exact balance match =====");
        runTest(() -> {
            Cart.customerBalance = 230;
            Cart myCart = new Cart();
            Product laptopBag = new Product("Laptop Bag", 200, 5, false, true, 0.8, "");
            myCart.addToCart(laptopBag, 1); // 200 + 30 = 230
            myCart.checkout();
        });
    }
}

