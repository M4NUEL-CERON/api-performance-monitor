package com.example.apiperformancemonitor.component;

import com.example.apiperformancemonitor.model.Product;
import com.example.apiperformancemonitor.model.Transaction;
import com.example.apiperformancemonitor.repository.ProductRepository;
import com.example.apiperformancemonitor.repository.TransactionRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
public class DataSeeder {

    private final ProductRepository productRepository;
    private final TransactionRepository transactionRepository;
    private final Random random = new Random(42);

    private static final String[] CATEGORIES = {
        "Electronics", "Furniture", "Clothing", "Books", "Sports", "Home", "Toys"
    };
    private static final String[][] PRODUCTS_BY_CATEGORY = {
        {"Laptop Pro", "Wireless Mouse", "Mechanical Keyboard", "4K Monitor", "USB-C Hub",
         "Webcam HD", "Noise Cancelling Headphones", "External SSD", "Smart Speaker", "Gaming GPU",
         "Tablet 10 inch", "Smartwatch Series X", "Bluetooth Earbuds", "Portable Charger", "LED Strip"},
        {"Standing Desk", "Ergonomic Chair", "LED Desk Lamp", "Bookshelf", "Filing Cabinet",
         "Monitor Arm", "Footrest", "Whiteboard", "Office Sofa", "Storage Rack"},
        {"Running Shoes", "Sports Jacket", "Compression Shorts", "Yoga Pants", "Cotton T-Shirt",
         "Winter Coat", "Casual Sneakers", "Hiking Boots", "Swimwear", "Cycling Gloves"},
        {"Clean Code", "The Pragmatic Programmer", "Design Patterns", "Spring Boot in Action",
         "Kubernetes Up & Running", "Java Performance", "Domain-Driven Design", "Refactoring"},
        {"Yoga Mat", "Resistance Bands", "Dumbbell Set", "Pull-up Bar", "Jump Rope",
         "Foam Roller", "Kettlebell 16kg", "Gym Bag", "Water Bottle", "Protein Shaker"},
        {"Air Purifier", "Robot Vacuum", "Coffee Maker", "Blender Pro", "Instant Pot",
         "Toaster Oven", "Electric Kettle", "Dish Rack", "Storage Baskets", "Candle Set"},
        {"LEGO City Set", "Remote Control Car", "Board Game Classic", "Puzzle 1000pc",
         "Action Figure", "Building Blocks", "Toy Piano", "Nerf Blaster"}
    };

    private static final String[] TYPES = {"CREDIT", "DEBIT"};
    private static final String[] CURRENCIES = {"USD", "EUR", "COP", "MXN"};
    private static final String[] STATUSES = {
        "COMPLETED", "COMPLETED", "COMPLETED", "COMPLETED",
        "COMPLETED", "COMPLETED", "COMPLETED",
        "PENDING", "PENDING",
        "FAILED"
    };
    private static final String[] DESCRIPTIONS = {
        "Salary payment", "Rent", "Groceries", "Transfer", "Subscription",
        "Insurance", "Utilities", "Bonus", "Refund", "Invoice payment",
        "Freelance income", "Tax payment", "Dividend", "Loan payment", "ATM withdrawal"
    };

    public DataSeeder(ProductRepository productRepository, TransactionRepository transactionRepository) {
        this.productRepository = productRepository;
        this.transactionRepository = transactionRepository;
    }

    @PostConstruct
    public void seed() {
        seedProducts();
        seedTransactions();
    }

    private void seedProducts() {
        if (productRepository.count() > 0) return;

        List<Product> products = new ArrayList<>();
        for (int c = 0; c < CATEGORIES.length; c++) {
            String[] names = PRODUCTS_BY_CATEGORY[c];
            int repeats = (500 / (CATEGORIES.length * names.length)) + 1;
            for (int r = 0; r < repeats; r++) {
                for (String name : names) {
                    double price = 5 + random.nextDouble() * 1995;
                    int stock = 1 + random.nextInt(500);
                    String productName = r == 0 ? name : name + " v" + (r + 1);
                    products.add(new Product(
                        productName,
                        CATEGORIES[c],
                        BigDecimal.valueOf(price).setScale(2, RoundingMode.HALF_UP),
                        stock,
                        LocalDateTime.now().minusDays(random.nextInt(365))
                    ));
                    if (products.size() >= 500) break;
                }
                if (products.size() >= 500) break;
            }
            if (products.size() >= 500) break;
        }
        productRepository.saveAll(products);
    }

    private void seedTransactions() {
        if (transactionRepository.count() > 0) return;

        List<Transaction> transactions = new ArrayList<>();
        for (int i = 0; i < 2000; i++) {
            double amount = 10 + random.nextDouble() * 49990;
            transactions.add(new Transaction(
                TYPES[random.nextInt(TYPES.length)],
                BigDecimal.valueOf(amount).setScale(2, RoundingMode.HALF_UP),
                CURRENCIES[random.nextInt(CURRENCIES.length)],
                STATUSES[random.nextInt(STATUSES.length)],
                DESCRIPTIONS[random.nextInt(DESCRIPTIONS.length)],
                LocalDateTime.now().minusDays(random.nextInt(90))
            ));
        }
        transactionRepository.saveAll(transactions);
    }
}
