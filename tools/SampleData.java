import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Lab-01 sample data: java tools/SampleData.java [month] [days] [rows] [seed]
 * Writes data/<month>/<BRANCH>-<date>.csv with two deliberately bad rows.
 */
void main(String[] args) throws IOException {
    var month = YearMonth.parse(args.length > 0 ? args[0] : "2026-09");
    int days = args.length > 1 ? Integer.parseInt(args[1]) : 5;
    int rowsPerDay = args.length > 2 ? Integer.parseInt(args[2]) : 200;
    var random = new Random(args.length > 3 ? Long.parseLong(args[3]) : 42);
    String[][] products = {
        {"SKU-1001", "Jasmine Rice 5kg", "Grocery", "6.50"},
        {"SKU-1002", "Fish Sauce 700ml", "Grocery", "1.80"},
        {"SKU-2001", "Mineral Water 1.5L", "Beverages", "0.45"},
        {"SKU-2002", "Iced Coffee Can", "Beverages", "0.90"},
        {"SKU-3001", "Dish Soap 500ml", "Household", "1.25"},
        {"SKU-3002", "Laundry Powder 1kg", "Household", "2.95"},
        {"SKU-4001", "Toothpaste 150g", "Personal Care", "1.60"},
        {"SKU-4002", "Shampoo 400ml", "Personal Care", "3.40"},
        {"SKU-5001", "USB-C Cable 1m", "Electronics", "4.50"},
        {"SKU-5002", "Power Bank 10000mAh", "Electronics", "18.00"},
        {"SKU-6001", "Notebook A5", "Stationery", "0.75"},
        {"SKU-6002", "Ballpoint Pen Blue", "Stationery", "0.30"}};
    String[] payments = {"CASH", "CARD", "KHQR", "KHQR"};
    Path dir = Files.createDirectories(Path.of("data", month.toString()));
    for (String branch : List.of("PNH", "REP", "BTB")) {
        int receipt = 0;
        for (int d = 1; d <= days; d++) {
            LocalDate date = month.atDay(d);
            var lines = new ArrayList<String>();
            lines.add("branch,date,receipt_no,sku,product_name,category,"
                    + "quantity,unit_price,discount,payment_method");
            String payment = "CASH";
            for (int r = 0; r < rowsPerDay; r++) {
                // a new receipt roughly every third line
                if (r == 0 || random.nextInt(3) == 0) {
                    receipt++;
                    payment = payments[random.nextInt(payments.length)];
                }
                String[] p = products[random.nextInt(products.length)];
                int qty = 1 + random.nextInt(4);
                String discount = random.nextInt(10) == 0 ? "0.10" : "0.00";
                lines.add(String.join(",", branch, date.toString(),
                        "%s-%06d".formatted(branch, receipt), p[0], p[1], p[2],
                        String.valueOf(qty), p[3], discount, payment));
            }
            if (branch.equals("REP") && d == 2) {
                lines.set(10, lines.get(10).replaceFirst(",\\d,", ",two,"));
            }
            if (branch.equals("BTB") && d == 4) {
                lines.set(20, lines.get(20).replaceAll(
                        "(CASH|CARD|KHQR)$", "PAYPAL"));
            }
            Files.write(dir.resolve(branch + "-" + date + ".csv"), lines);
        }
    }
    System.out.println("Wrote " + 3 * days + " files, " + 3 * days * rowsPerDay
            + " rows, to " + dir);
}