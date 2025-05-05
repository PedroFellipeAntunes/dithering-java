package Windows;

import Dither.TYPE;
import Dither.Operations;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.*;
import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.swing.plaf.basic.BasicArrowButton;
import javax.swing.plaf.basic.BasicComboPopup;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.plaf.basic.ComboPopup;

public class DropDownWindow {
    private Operations op = new Operations();
    
    private JFrame frame;
    private JLabel dropLabel;
    private JSlider slider;
    private JTextField valueField;
    private JSlider scaleSlider;
    private JTextField scaleField;
    private JButton grayButton;
    private JButton dynamicButton;
    
    private boolean rangeQ = false;
    private boolean gray = false;
    boolean loading = false;
    private int colorLevels = 2;
    private final int minLevels = 2, maxLevels = 256;
    private int scale = 1;
    private final int minScale = 1, maxScale = 5;
    private TYPE operation = TYPE.Simple;
    
    private JComboBox<TYPE> typeComboBox;
    
    private Font defaultFont = UIManager.getDefaults().getFont("Label.font");
    
    public DropDownWindow() {
        frame = new JFrame("Image Color Quantization");
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        frame.setLayout(new BorderLayout());
        
        dropLabel = new JLabel("Drop IMAGE files here", SwingConstants.CENTER);
        dropLabel.setPreferredSize(new Dimension(300, 200));
        dropLabel.setBorder(BorderFactory.createLineBorder(Color.WHITE));
        dropLabel.setForeground(Color.WHITE);
        dropLabel.setOpaque(true);
        dropLabel.setBackground(Color.BLACK);
        dropLabel.setTransferHandler(new TransferHandler() {
            public boolean canImport(TransferHandler.TransferSupport support) {
                if (!loading && support.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                    return true;
                }
                
                return false;
            }
            
            public boolean importData(TransferHandler.TransferSupport support) {
                if (!canImport(support)) {
                    return false;
                }
                
                try {
                    Transferable transferable = support.getTransferable();
                    List<File> files = (List<File>) transferable.getTransferData(DataFlavor.javaFileListFlavor);
                    
                    for (File file : files) {
                        if (!file.getName().toLowerCase().endsWith(".png")
                                && !file.getName().toLowerCase().endsWith(".jpg")
                                && !file.getName().toLowerCase().endsWith(".jpeg")) {
                            JOptionPane.showMessageDialog(frame, "Incorrect image format, use: png, jpg or jpeg", "Error", JOptionPane.ERROR_MESSAGE);
                            
                            return false;
                        }
                    }
                    
                    dropLabel.setText("LOADING (1/" + files.size() + ")");
                    loading = true;
                    scaleSlider.setEnabled(false);
                    scaleField.setEnabled(false);
                    slider.setEnabled(false);
                    valueField.setEnabled(false);
                    ableOrDisableButton(grayButton);
                    ableOrDisableButton(dynamicButton);
                    
                    frame.repaint();
                    
                    new Thread(() -> {
                        int filesProcessed = 1;
                        for (File file : files) {
                            op.processFile(file.getPath(), colorLevels, scale, rangeQ, operation, gray);
                            filesProcessed++;
                            
                            final int finalFilesProcessed = filesProcessed;
                            SwingUtilities.invokeLater(() -> {
                                dropLabel.setText("LOADING (" + finalFilesProcessed + "/" + files.size() + ")");
                            });
                        }
                        
                        SwingUtilities.invokeLater(() -> {
                            dropLabel.setText("Images Quantized");
                            
                            Timer resetTimer = new Timer(1000, e2 -> {
                                dropLabel.setText("Drop IMAGE files here");
                                loading = false;
                                slider.setEnabled(true);
                                valueField.setEnabled(true);
                                scaleSlider.setEnabled(true);
                                scaleField.setEnabled(true);
                                ableOrDisableButton(grayButton);
                                ableOrDisableButton(dynamicButton);
                            });
                            
                            resetTimer.setRepeats(false);
                            resetTimer.start();
                        });
                    }).start();
                    
                    return true;
                } catch (UnsupportedFlavorException | IOException e) {
                    e.printStackTrace();
                    return false;
                }
            }
        });
        
        //Slider
        slider = new JSlider(JSlider.HORIZONTAL, minLevels, maxLevels, colorLevels);
        slider.setMajorTickSpacing(50);
        slider.setMinorTickSpacing(maxLevels / 10);
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);
        slider.setBackground(Color.BLACK);
        slider.setForeground(Color.WHITE);
        slider.setValue(colorLevels);
        
        //Value of slider
        valueField = new JTextField();
        valueField.setForeground(Color.WHITE);
        valueField.setBackground(Color.BLACK);
        valueField.setFont(defaultFont);
        valueField.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
        valueField.setText(String.valueOf(slider.getValue()));
        valueField.setPreferredSize(new Dimension(50, 20));
        
        valueField.addActionListener(e -> {
            if (!loading) {
                String text = valueField.getText();
                
                if (!text.isEmpty()) {
                    text = text.substring(0, Math.min(text.length(), 3));
                    
                    int value = Integer.parseInt(text);
                    
                    value = Math.max(minLevels, Math.min(maxLevels, value));
                    
                    slider.setValue(value);
                    valueField.setText(String.valueOf(value));
                } else {
                    valueField.setText(String.valueOf(slider.getValue()));
                }
                
                valueField.transferFocus();
            }
        });
        
        slider.addChangeListener(e -> {
            if (!loading) {
                colorLevels = slider.getValue();
                valueField.setText(String.valueOf(slider.getValue()));
            }
        });
        
        //Panel for colorLevel and slider
        JPanel colorLevelPanel = new JPanel(new BorderLayout());
        colorLevelPanel.add(slider, BorderLayout.WEST);
        colorLevelPanel.add(valueField, BorderLayout.EAST);
        
        //Slider scale
        scaleSlider = new JSlider(JSlider.HORIZONTAL, minScale, maxScale, scale);
        scaleSlider.setMajorTickSpacing(1);
        scaleSlider.setPaintTicks(true);
        scaleSlider.setPaintLabels(true);
        scaleSlider.setBackground(Color.BLACK);
        scaleSlider.setForeground(Color.WHITE);
        
        //Value of scaleSlider
        scaleField = new JTextField();
        scaleField.setForeground(Color.WHITE);
        scaleField.setBackground(Color.BLACK);
        scaleField.setFont(defaultFont);
        scaleField.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
        scaleField.setText(String.valueOf(scaleSlider.getValue()));
        scaleField.setPreferredSize(new Dimension(50, 20));
        
        scaleField.addActionListener(e -> {
            if (!loading) {
                String text = scaleField.getText();
                
                if (!text.isEmpty()) {
                    text = text.substring(0, Math.min(text.length(), 3));
                    
                    int value = Integer.parseInt(text);
                    
                    value = Math.max(minScale, Math.min(maxScale, value));
                    
                    scaleSlider.setValue(value);
                    scaleField.setText(String.valueOf(value));
                } else {
                    scaleField.setText(String.valueOf(scaleSlider.getValue()));
                }
                
                scaleField.transferFocus();
            }
        });
        
        scaleSlider.addChangeListener(e -> {
            if (!loading) {
                scale = scaleSlider.getValue();
                scaleField.setText(String.valueOf(scaleSlider.getValue()));
            }
        });
        
        //Panel for scale and slider
        JPanel scalePanel = new JPanel(new BorderLayout());
        scalePanel.add(scaleSlider, BorderLayout.WEST);
        scalePanel.add(scaleField, BorderLayout.EAST);
        
        //Panel for sliders
        // hgap = 0, vgap = 10
        JPanel sliderPanel = new JPanel(new BorderLayout(0, 5));
        sliderPanel.setBackground(Color.BLACK);
        sliderPanel.add(colorLevelPanel, BorderLayout.NORTH);
        sliderPanel.add(scalePanel, BorderLayout.SOUTH);
        
        // Color Level
        JLabel colorLevelLabel = new JLabel("Color Levels:");
        colorLevelLabel.setHorizontalAlignment(SwingConstants.LEFT);
        colorLevelLabel.setForeground(Color.WHITE);
        colorLevelLabel.setBackground(Color.BLACK);
        colorLevelLabel.setOpaque(true);

        JPanel colorLevelSingle = new JPanel(new BorderLayout());
        colorLevelSingle.setBackground(Color.BLACK);
        colorLevelSingle.add(colorLevelLabel, BorderLayout.NORTH);
        colorLevelSingle.add(slider, BorderLayout.CENTER);

        colorLevelPanel.add(colorLevelSingle, BorderLayout.WEST);

        // Scale
        JLabel scaleLabel = new JLabel("Scale:");
        scaleLabel.setHorizontalAlignment(SwingConstants.LEFT);
        scaleLabel.setForeground(Color.WHITE);
        scaleLabel.setBackground(Color.BLACK);
        scaleLabel.setOpaque(true);

        JPanel scaleSingle = new JPanel(new BorderLayout());
        scaleSingle.setBackground(Color.BLACK);
        scaleSingle.add(scaleLabel, BorderLayout.NORTH);
        scaleSingle.add(scaleSlider, BorderLayout.CENTER);

        scalePanel.add(scaleSingle, BorderLayout.WEST);
        
        //GrayScale button
        grayButton = new JButton("Gray Scale");
        setButtonsVisuals(grayButton);
        
        grayButton.addActionListener(e -> {
            if (!loading) {
                if (gray == true) {
                    resetButton(grayButton);
                    gray = false;
                } else {
                    grayButton.setBackground(Color.WHITE);
                    grayButton.setForeground(Color.BLACK);
                    gray = true;
                }
            }
        });
        
        // Dynamic button
        dynamicButton = new JButton("Dynamic Range");
        setButtonsVisuals(dynamicButton);
        dynamicButton.addActionListener(e -> {
            if (!loading) {
                rangeQ = !rangeQ;
                if (rangeQ) {
                    dynamicButton.setBackground(Color.WHITE);
                    dynamicButton.setForeground(Color.BLACK);
                } else {
                    resetButton(dynamicButton);
                }
            }
        });
        
        // Panel vertical para os botões Gray Scale e Dynamic
        JPanel buttonPanel = new JPanel(new GridLayout(2, 1, 0, 5));
        buttonPanel.setBackground(Color.BLACK);
        buttonPanel.add(grayButton);
        buttonPanel.add(dynamicButton);
        
        //Bottom panel
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
        controlPanel.setBackground(Color.BLACK);
        controlPanel.setBorder(BorderFactory.createLineBorder(Color.WHITE));
        controlPanel.add(sliderPanel);
        controlPanel.add(buttonPanel);
        
        typeComboBox = new JComboBox<>(TYPE.values());
        typeComboBox.setSelectedItem(TYPE.Simple);

        typeComboBox.addActionListener(e -> {
            if (!loading) {
                operation = (TYPE) typeComboBox.getSelectedItem();
            }
        });
        
        // ComboBox
        typeComboBox.setBackground(Color.BLACK);
        typeComboBox.setForeground(Color.WHITE);
        typeComboBox.setBorder(BorderFactory.createLineBorder(Color.WHITE));

        // ComboBox visuals
        typeComboBox.setUI(new javax.swing.plaf.basic.BasicComboBoxUI() {
            @Override
            protected ComboBoxEditor createEditor() {
                ComboBoxEditor editor = super.createEditor();
                editor.getEditorComponent().setBackground(Color.BLACK);
                editor.getEditorComponent().setForeground(Color.WHITE);
                
                return editor;
            }

            @Override
            protected ComboPopup createPopup() {
                BasicComboPopup popup = (BasicComboPopup) super.createPopup();
                
                popup.getList().setBackground(Color.BLACK);
                popup.getList().setForeground(Color.WHITE);

                popup.getList().setSelectionBackground(Color.WHITE);
                popup.getList().setSelectionForeground(Color.BLACK);
                
                popup.setBorder(BorderFactory.createLineBorder(Color.WHITE));
                
                JScrollPane scroll = (JScrollPane) popup.getComponent(0);
                
                JScrollBar bar = scroll.getVerticalScrollBar();
                bar.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, Color.WHITE));
                
                bar.setUI(new BasicScrollBarUI() {
                    @Override
                    protected JButton createDecreaseButton(int orientation) {
                        return arrowButtonVisual();
                    }

                    @Override
                    protected JButton createIncreaseButton(int orientation) {
                        return arrowButtonVisual();
                    }

                    @Override
                    protected void configureScrollBarColors() {
                        thumbColor = Color.WHITE;
                        trackColor = Color.BLACK;
                    }
                });
                
                return popup;
            }
            
            @Override
            protected JButton createArrowButton() {
                return arrowButtonVisual();
            }
        });
        
        // Panel for comboBox
        JPanel typePanel = new JPanel(new BorderLayout());
        typePanel.setBackground(Color.BLACK);

        typeComboBox.setPreferredSize(new Dimension(0, 30));
        typePanel.add(typeComboBox, BorderLayout.CENTER);
        
        frame.add(controlPanel, BorderLayout.SOUTH);
        frame.add(dropLabel, BorderLayout.CENTER);
        frame.add(typePanel, BorderLayout.NORTH);
        
        frame.pack();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int xPos = (screenSize.width - frame.getWidth()) / 2;
        int yPos = (screenSize.height - frame.getHeight()) / 2;
        frame.setLocation(xPos, yPos);
        
        frame.setVisible(true);
    }
    
    private void setButtonsVisuals(JButton button) {
        button.setBackground(Color.BLACK);
        button.setForeground(Color.WHITE);
        button.setBorder(BorderFactory.createLineBorder(Color.WHITE));
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(130, 40));
    }
    
    void resetButton(JButton button) {
        button.setBackground(Color.BLACK);
        button.setForeground(Color.WHITE);
    }
    
    private void ableOrDisableButton(JButton button) {
        button.setEnabled(!loading);
    }
    
    private BasicArrowButton arrowButtonVisual() {
        BasicArrowButton bab = new BasicArrowButton(
                SwingConstants.SOUTH,
                Color.BLACK,
                Color.WHITE,
                Color.WHITE,
                Color.BLACK
        );

        bab.setBorder(BorderFactory.createEmptyBorder());

        return bab;
    }
}