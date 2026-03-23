package org.example.sct_sd_4;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

@SpringBootApplication
public class WebScraper extends JFrame
{
    private final JTextField urlField;
    private final DefaultTableModel tableModel;

    public WebScraper()
    {
        setTitle("E-Commerce Web Scraper");
        setSize(700, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        // Top Panel: Input and Buttons
        JPanel topPanel = new JPanel(new BorderLayout(5, 5));
        urlField = new JTextField("https://books.toscrape.com/");
        JButton scrapeButton = new JButton("Scrape");
        JButton exportButton = new JButton("Export to CSV");

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(scrapeButton);
        buttonPanel.add(exportButton);

        topPanel.add(new JLabel(" Target URL:"), BorderLayout.WEST);
        topPanel.add(urlField, BorderLayout.CENTER);
        topPanel.add(buttonPanel, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // Center Panel: Table for Results
        String[] columns = {"Product Name", "Price", "Rating"};
        tableModel = new DefaultTableModel(columns, 0);
        JTable resultTable = new JTable(tableModel);
        add(new JScrollPane(resultTable), BorderLayout.CENTER);

        // Action Listeners
        scrapeButton.addActionListener(_ -> scrapeData(urlField.getText()));
        exportButton.addActionListener(_ -> exportCSV());
    }

    private void scrapeData(String url)
    {
        tableModel.setRowCount(0);  // Clear previous results
        try
        {
            // Fetch HTML document
            Document doc = Jsoup.connect(url).timeout(5000).get();

            // NOTE: CSS Selectors here are specific to books.toscrape.com
            // You will need to inspect your target website and change these.
            Elements products = doc.select("article.product_pod");

            if (products.isEmpty())
            {
                JOptionPane.showMessageDialog(this, "No products found, Check URL or CSS selectors.");
            }

            // Extract data and add to table
            for (Element product: products)
            {
                String name = product.select("h3 > a").attr("title");
                String price = product.select(".price_color").text();
                String rating = product.select("p.star-rating").attr("class").replace("star-rating ", "");

                tableModel.addRow(new Object[]{name, price, rating});
            }
        }
        catch (IllegalArgumentException ex)
        {
            JOptionPane.showMessageDialog(this, "Invalid URL format. Please include https://");
        }
        catch (IOException ex)
        {
            JOptionPane.showMessageDialog(this, "Error connecting to the website: " + ex.getMessage());
        }
    }

    private void exportCSV()
    {
        if (tableModel.getRowCount() == 0)
        {
            JOptionPane.showMessageDialog(this, "No data to export!");
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save CSV File");
        int userSelection = fileChooser.showSaveDialog(this);

        if (userSelection == JFileChooser.APPROVE_OPTION)
        {
            File fileToSave = fileChooser.getSelectedFile();
            // Ensure .csv extension
            if (!fileToSave.getName().endsWith(".csv"))
            {
                fileToSave = new File(fileToSave.getAbsolutePath() + ".csv");
            }

            try (FileWriter csvWriter = new FileWriter(fileToSave))
            {
                // Write headers
                csvWriter.append("Product Name, Price, Rating\n");

                // Write data rows
                for (int i = 0; i < tableModel.getRowCount(); i++)
                {
                    csvWriter.append(escapeCSV((String) tableModel.getValueAt(i, 0))).append(",")
                            .append((String) tableModel.getValueAt(i, 1)).append(",")
                            .append((String) tableModel.getValueAt(i, 2)).append("\n");
                }
                JOptionPane.showMessageDialog(this, "Export successful!");
            }
            catch (IOException ex)
            {
                JOptionPane.showMessageDialog(this, "Error writing file: " + ex.getMessage());
            }
        }
    }

    // Helper to handle commas inside product names
    private String escapeCSV(String data)
    {
        if (data.contains(","))
        {
            return "\"" + data.replace("\"", "\"\"") + "\"";
        }
        return data;
    }

    static void main()
    {
        SwingUtilities.invokeLater(() -> new WebScraper().setVisible(true));
    }
}
