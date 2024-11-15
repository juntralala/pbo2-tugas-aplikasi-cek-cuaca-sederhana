/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ujun;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.table.DefaultTableModel;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class WeatherApp extends JFrame {

    private JComboBox<String> locationComboBox;
    private JButton checkButton;
    private JButton saveButton;
    private JButton exportButton;
    private JLabel weatherLabel;
    private JLabel imageLabel;
    private JTable weatherHistoryTable;
    private ArrayList<String> favoriteLocations;
    private static final String API_KEY = "e8e1d3a5dfca785effa389c489922179"; // Ganti dengan API key OpenWeatherMap Anda

    public WeatherApp() {
        setTitle("Aplikasi Cek Cuaca");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        // Initialize components
        initializeComponents();

        // Load favorite locations
        favoriteLocations = loadFavoriteLocations();

        // Setup layout
        setupLayout();

        // Add event listeners
        addEventListeners();
    }

    private void initializeComponents() {
        locationComboBox = new JComboBox<>();
        locationComboBox.setEditable(true);
        checkButton = new JButton("Cek Cuaca");
        saveButton = new JButton("Simpan ke Favorit");
        exportButton = new JButton("Export CSV");
        weatherLabel = new JLabel("Silakan pilih lokasi");
        imageLabel = new JLabel();

        // Initialize table
        String[] columnNames = {"Lokasi", "Suhu", "Kondisi", "Waktu"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0);
        weatherHistoryTable = new JTable(model);
    }

    private void setupLayout() {
        // North panel
        JPanel northPanel = new JPanel(new FlowLayout());
        northPanel.add(new JLabel("Lokasi:"));
        northPanel.add(locationComboBox);
        northPanel.add(checkButton);
        northPanel.add(saveButton);
        add(northPanel, BorderLayout.NORTH);

        // Center panel
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(weatherLabel, BorderLayout.NORTH);
        centerPanel.add(imageLabel, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);

        // South panel
        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.add(new JScrollPane(weatherHistoryTable), BorderLayout.CENTER);
        southPanel.add(exportButton, BorderLayout.SOUTH);
        add(southPanel, BorderLayout.SOUTH);
    }

    private void addEventListeners() {
        checkButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String location = (String) locationComboBox.getSelectedItem();
                checkWeather(location);
            }
        });

        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String location = (String) locationComboBox.getSelectedItem();
                saveFavoriteLocation(location);
            }
        });

        exportButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                exportToCSV();
            }
        });

        locationComboBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    String location = (String) e.getItem();
                    checkWeather(location);
                }
            }
        });
    }

    private void checkWeather(String location) {
        try {
            String encodedLocation = URLEncoder.encode(location, "UTF-8");
            String apiUrl = String.format(
                    "https://api.openweathermap.org/data/2.5/weather?q=%s&lang=id&appid=%s&units=metric",
                    encodedLocation, API_KEY
            );

            URL url = new URL(apiUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream())
            );
            StringBuilder response = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            // Parse JSON response
            JSONParser parser = new JSONParser();
            JSONObject jsonResponse = (JSONObject) parser.parse(response.toString());
            JSONObject main = (JSONObject) jsonResponse.get("main");
            JSONObject weather = (JSONObject) ((java.util.List<?>) jsonResponse.get("weather")).get(0);

            double temp = (double) main.get("temp");
            String condition = (String) weather.get("main");

            // Update UI
            updateWeatherDisplay(location, temp, condition);
            addWeatherRecord(location, temp, condition);
//            JOptionPane.showMessageDialog(null, response.toString());
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error mengambil data cuaca: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void updateWeatherDisplay(String location, double temp, String condition) {
        weatherLabel.setText(String.format(
                "Cuaca di %s: %.1f°C, %s",
                location, temp, condition
        ));

        // Update weather image
        ImageIcon icon = getWeatherIcon(condition);
        if (icon != null) {
            imageLabel.setIcon(icon);
        }
    }

    private ImageIcon getWeatherIcon(String condition) {
        String iconPath = "";

        // Menentukan icon berdasarkan kondisi cuaca
        switch (condition.toLowerCase()) {
            case "clear":
                iconPath = "/icons/clear.png";
                break;
            case "clouds":
                iconPath = "/icons/clouds.png";
                break;
            case "rain":
                iconPath = "/icons/rain.png";
                break;
            case "drizzle":
                iconPath = "/icons/drizzle.png";
                break;
            case "thunderstorm":
                iconPath = "/icons/thunderstorm.png";
                break;
            case "snow":
                iconPath = "/icons/snow.png";
                break;
            case "mist":
            case "fog":
            case "haze":
                iconPath = "/icons/mist.png";
                break;
            default:
                iconPath = "/icons/default.png";
                break;
        }

        try {
            // Mengambil gambar dari resources
//            JOptionPane.showMessageDialog(null, this.getClass().getResource("resources" + iconPath));
            ImageIcon icon = new ImageIcon(getClass().getResource("resources" + iconPath));

            // Resize gambar ke ukuran yang sesuai (misalnya 50x50 pixel)
            Image img = icon.getImage();
            Image resizedImg = img.getScaledInstance(50, 50, Image.SCALE_SMOOTH);
            return new ImageIcon(resizedImg);

        } catch (Exception e) {
            System.err.println("Error loading weather icon: " + e.getMessage());
            
            e.printStackTrace();
            return null;
        }
    }

    private void saveFavoriteLocation(String location) {
        if (!favoriteLocations.contains(location)) {
            favoriteLocations.add(location);
            locationComboBox.addItem(location);
            saveFavoriteLocationsToFile();
        }
    }

    private ArrayList<String> loadFavoriteLocations() {
        ArrayList<String> locations = new ArrayList<>();
        try {
            File file = new File("favorite_locations.txt");
            if (file.exists()) {
                BufferedReader reader = new BufferedReader(new FileReader(file));
                String line;
                while ((line = reader.readLine()) != null) {
                    locations.add(line);
                    locationComboBox.addItem(line);
                }
                reader.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return locations;
    }

    private void saveFavoriteLocationsToFile() {
        try {
            FileWriter writer = new FileWriter("favorite_locations.txt");
            for (String location : favoriteLocations) {
                writer.write(location + "\n");
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addWeatherRecord(String location, double temp, String condition) {
        DefaultTableModel model
                = (DefaultTableModel) weatherHistoryTable.getModel();
        model.addRow(new Object[]{
            location,
            String.format("%.1f°C", temp),
            condition,
            new java.util.Date().toString()
        });
    }

    private void exportToCSV() {
        try {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Simpan File CSV");
            fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                    "CSV files (*.csv)", "csv"));

            if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                if (!file.getName().toLowerCase().endsWith(".csv")) {
                    file = new File(file.getParentFile(), file.getName() + ".csv");
                }

                FileWriter writer = new FileWriter(file);
                for (int i = 0; i < weatherHistoryTable.getRowCount(); i++) {
                    for (int j = 0; j < weatherHistoryTable.getColumnCount(); j++) {
                        writer.write(weatherHistoryTable.getValueAt(i, j).toString());
                        if (j < weatherHistoryTable.getColumnCount() - 1) {
                            writer.write(",");
                        }
                    }
                    writer.write("\n");
                }
                writer.close();
                JOptionPane.showMessageDialog(this,
                        "Data berhasil diekspor ke CSV",
                        "Sukses",
                        JOptionPane.INFORMATION_MESSAGE
                );
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                    "Error menyimpan file: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new WeatherApp().setVisible(true);
            }
        });
    }
}
