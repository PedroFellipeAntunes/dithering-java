package Windows;

import Dither.TYPE;
import Dither.Operations;

import javax.swing.*;
import javax.swing.plaf.basic.*;
import java.awt.*;
import java.awt.datatransfer.*;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class DropDownWindow {
    private JFrame frame;
    private JLabel dropLabel;
    private JSlider colorSlider, scaleSlider, spreadSlider;
    private JTextField colorField, scaleField, spreadField;
    private JButton grayButton, dynamicButton;
    private JComboBox<TYPE> typeComboBox;

    private boolean rangeQuantization = false;
    private boolean grayScale = false;
    private boolean loading = false;

    private double spread = 0.5;
    private final double minSpread = 0.0;
    private final double maxSpread = 1.0;
    private final double spreadStep = 0.01;

    private int colorLevels = 2;
    private final int minLevels = 2, maxLevels = 256;

    private int scale = 1;
    private final int minScale = 1, maxScale = 5;

    private TYPE operationType = TYPE.Simple;
    private final Font defaultFont = UIManager.getDefaults().getFont("Label.font");

    public DropDownWindow() {
        initFrame();
        initDropLabel();
        initControlPanel();
        initComboBox();
        finalizeFrame();
    }

    private void initFrame() {
        frame = new JFrame("Image Color Quantization");
        
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
    }

    private void initDropLabel() {
        dropLabel = new JLabel("Drop IMAGE files here", SwingConstants.CENTER);
        
        dropLabel.setPreferredSize(new Dimension(300, 200));
        dropLabel.setBorder(BorderFactory.createLineBorder(Color.WHITE));
        dropLabel.setForeground(Color.WHITE);
        dropLabel.setOpaque(true);
        dropLabel.setBackground(Color.BLACK);
        dropLabel.setTransferHandler(createTransferHandler());
        
        frame.add(dropLabel, BorderLayout.CENTER);
    }

    private TransferHandler createTransferHandler() {
        return new TransferHandler() {
            @Override
            public boolean canImport(TransferSupport support) {
                return !loading && support.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
            }

            @Override
            public boolean importData(TransferSupport support) {
                if (!canImport(support)) return false;

                try {
                    List<File> files = (List<File>) support.getTransferable()
                            .getTransferData(DataFlavor.javaFileListFlavor);

                    for (File file : files) {
                        if (!file.getName().matches(".*\\.(png|jpg|jpeg)$")) {
                            showError("Incorrect image format, use: png, jpg or jpeg");
                            
                            return false;
                        }
                    }

                    setLoadingState(true, files.size());
                    processFiles(files);
                    
                    return true;

                } catch (UnsupportedFlavorException | IOException e) {
                    e.printStackTrace();
                    
                    return false;
                }
            }
        };
    }

    private void setLoadingState(boolean state, int fileCount) {
        loading = state;
        
        toggleControls(!state);
        
        frame.repaint();
    }

    private void toggleControls(boolean enabled) {
        scaleSlider.setEnabled(enabled);
        scaleField.setEnabled(enabled);
        colorSlider.setEnabled(enabled);
        colorField.setEnabled(enabled);
        spreadSlider.setEnabled(enabled);
        spreadField.setEnabled(enabled);
        grayButton.setEnabled(enabled);
        dynamicButton.setEnabled(enabled);
    }

    private void processFiles(List<File> files) {
        final int total = files.size();
        
        dropLabel.setText("LOADING (1/" + total + ")");
        setLoadingState(true, total);

        SwingWorker<Void, Integer> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                Operations op = new Operations(
                        colorLevels,
                        scale,
                        spread,
                        rangeQuantization,
                        operationType,
                        grayScale
                );
                
                for (int i = 0; i < total; i++) {
                    op.processFile(files.get(i).getPath());
                    
                    publish(i + 2);
                }
                
                return null;
            }

            @Override
            protected void process(List<Integer> chunks) {
                int done = chunks.get(chunks.size() - 1);
                
                dropLabel.setText("LOADING (" + done + "/" + total + ")");
            }

            @Override
            protected void done() {
                onProcessingComplete();
            }
        };
        
        worker.execute();
    }
    
    private Timer resetTimer;

    private void onProcessingComplete() {
        dropLabel.setText("Images Quantized");

        // Cancel prior reset if it hasn't fired yet
        if (resetTimer != null && resetTimer.isRunning()) {
            resetTimer.stop();
        }

        resetTimer = new Timer(1000, e -> {
            dropLabel.setText("Drop IMAGE files here");
            setLoadingState(false, 0);
        });
        resetTimer.setRepeats(false);
        resetTimer.start();
    }

    private void initControlPanel() {
        JPanel colorPanel = createSliderPanel("Color Levels:", minLevels, maxLevels, colorLevels,
                val -> { colorLevels = val; }, val -> colorSlider.setValue(val));
        colorSlider = (JSlider) colorPanel.getComponent(0);
        colorField = (JTextField) ((JPanel) colorPanel.getComponent(1)).getComponent(0);

        JPanel scalePanel = createSliderPanel("Scale:", minScale, maxScale, scale,
                val -> { scale = val; }, val -> scaleSlider.setValue(val));
        scaleSlider = (JSlider) scalePanel.getComponent(0);
        scaleField = (JTextField) ((JPanel) scalePanel.getComponent(1)).getComponent(0);

        JPanel spreadPanel = createSpreadPanel();

        grayButton = createToggleButton("Gray Scale", () -> grayScale = !grayScale);
        dynamicButton = createToggleButton("Dynamic Range", () -> rangeQuantization = !rangeQuantization);

        JPanel buttonPanel = new JPanel(new GridLayout(2, 1, 0, 5));
        buttonPanel.setBackground(Color.BLACK);
        buttonPanel.add(grayButton);
        buttonPanel.add(dynamicButton);

        JPanel sliderPanel = new JPanel();
        sliderPanel.setLayout(new BoxLayout(sliderPanel, BoxLayout.Y_AXIS));
        sliderPanel.setBackground(Color.BLACK);
        sliderPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
        sliderPanel.add(colorPanel);
        sliderPanel.add(scalePanel);
        sliderPanel.add(spreadPanel);

        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
        controlPanel.setBackground(Color.BLACK);
        controlPanel.setBorder(BorderFactory.createLineBorder(Color.WHITE));
        controlPanel.add(sliderPanel);
        controlPanel.add(buttonPanel);

        frame.add(controlPanel, BorderLayout.SOUTH);
    }

    private JPanel createSliderPanel(String label, int min, int max, int init,
                                     java.util.function.IntConsumer onChange,
                                     java.util.function.IntConsumer onFieldChange) {
        JLabel jLabel = new JLabel(label, SwingConstants.LEFT);
        JTextField textField = new JTextField(String.valueOf(init));
        
        jLabel.setForeground(Color.WHITE);
        jLabel.setBackground(Color.BLACK);
        jLabel.setOpaque(true);

        JSlider slider = new JSlider(min, max, init);
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);
        slider.setBackground(Color.BLACK);
        slider.setForeground(Color.WHITE);
        slider.addChangeListener(e -> {
            if (!loading) {
                int value = slider.getValue();
                onChange.accept(value);
                textField.setText(String.valueOf(value));
            }
        });

        textField.setBackground(Color.BLACK);
        textField.setForeground(Color.WHITE);
        textField.setFont(defaultFont);
        textField.setPreferredSize(new Dimension(50, 20));
        textField.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));

        textField.addActionListener(e -> {
            if (!loading) {
                try {
                    int value = Integer.parseInt(textField.getText().trim());
                    
                    value = Math.max(min, Math.min(max, value));
                    onChange.accept(value);
                    onFieldChange.accept(value);
                } catch (NumberFormatException ex) {
                    textField.setText(String.valueOf(slider.getValue()));
                }
            }
        });

        JPanel fieldPanel = new JPanel(new BorderLayout());
        fieldPanel.setBackground(Color.BLACK);
        fieldPanel.add(textField, BorderLayout.EAST);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.BLACK);
        panel.add(slider, BorderLayout.CENTER);
        panel.add(fieldPanel, BorderLayout.EAST);
        panel.add(jLabel, BorderLayout.NORTH);
        
        return panel;
    }

    private JPanel createSpreadPanel() {
        int sMin = (int) (minSpread / spreadStep);
        int sMax = (int) (maxSpread / spreadStep);
        int sInit = (int) (spread / spreadStep);

        spreadSlider = new JSlider(sMin, sMax, sInit);
        spreadSlider.setPaintTicks(true);
        spreadSlider.setPaintLabels(true);
        spreadSlider.setBackground(Color.BLACK);
        spreadSlider.setForeground(Color.WHITE);
        
        spreadSlider.addChangeListener(e -> {
            if (!loading) {
                spread = spreadSlider.getValue() * spreadStep;
                spreadField.setText(String.format("%.2f", spread));
            }
        });

        spreadField = new JTextField(String.format("%.2f", spread));
        spreadField.setPreferredSize(new Dimension(50, 20));
        spreadField.setBackground(Color.BLACK);
        spreadField.setForeground(Color.WHITE);
        spreadField.setFont(defaultFont);
        spreadField.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
        
        spreadField.addActionListener(e -> {
            if (!loading) {
                String text = spreadField.getText().trim().replace(',', '.');
                try {
                    double value = Double.parseDouble(text);
                    value = Math.max(minSpread, Math.min(maxSpread, value));
                    spread = value;
                    spreadSlider.setValue((int) (value / spreadStep));
                } catch (NumberFormatException ex) {
                    spreadField.setText(String.format("%.2f", spread));
                }
            }
        });

        JLabel label = new JLabel("Spread:", SwingConstants.LEFT);
        label.setForeground(Color.WHITE);
        label.setBackground(Color.BLACK);
        label.setOpaque(true);

        JPanel controls = new JPanel(new BorderLayout(5, 0));
        controls.setBackground(Color.BLACK);
        controls.add(spreadSlider, BorderLayout.CENTER);
        controls.add(spreadField, BorderLayout.EAST);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.BLACK);
        panel.add(label, BorderLayout.NORTH);
        panel.add(controls, BorderLayout.CENTER);
        
        return panel;
    }

    private JButton createToggleButton(String text, Runnable toggleAction) {
        JButton button = new JButton(text);
        
        styleButton(button);
        
        button.addActionListener(e -> {
            if (!loading) {
                toggleAction.run();
                toggleButtonColor(button, text.equals("Gray Scale") ? grayScale : rangeQuantization);
            }
        });
        
        return button;
    }

    private void toggleButtonColor(JButton button, boolean active) {
        if (active) {
            button.setBackground(Color.WHITE);
            button.setForeground(Color.BLACK);
        } else {
            button.setBackground(Color.BLACK);
            button.setForeground(Color.WHITE);
        }
    }

    private void styleButton(JButton button) {
        button.setBackground(Color.BLACK);
        button.setForeground(Color.WHITE);
        button.setBorder(BorderFactory.createLineBorder(Color.WHITE));
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(130, 40));
    }

    private void initComboBox() {
        typeComboBox = new JComboBox<>(TYPE.values());
        
        typeComboBox.setSelectedItem(TYPE.Simple);
        typeComboBox.setBackground(Color.BLACK);
        typeComboBox.setForeground(Color.WHITE);
        typeComboBox.setBorder(BorderFactory.createLineBorder(Color.WHITE));
        
        typeComboBox.addActionListener(e -> {
            if (!loading) {
                operationType = (TYPE) typeComboBox.getSelectedItem();
            }
        });
        
        customizeComboBoxUI(typeComboBox);

        JPanel comboPanel = new JPanel(new BorderLayout());
        comboPanel.setBackground(Color.BLACK);
        comboPanel.add(typeComboBox, BorderLayout.CENTER);
        
        frame.add(comboPanel, BorderLayout.NORTH);
    }

    private void customizeComboBoxUI(JComboBox<TYPE> comboBox) {
        comboBox.setUI(new BasicComboBoxUI() {
            @Override
            protected ComboBoxEditor createEditor() {
                ComboBoxEditor editor = super.createEditor();
                
                Component editorComponent = editor.getEditorComponent();
                editorComponent.setBackground(Color.BLACK);
                editorComponent.setForeground(Color.WHITE);
                
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

                JScrollPane scrollPane = (JScrollPane) popup.getComponent(0);
                
                JScrollBar bar = scrollPane.getVerticalScrollBar();
                bar.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, Color.WHITE));
                
                bar.setUI(new BasicScrollBarUI() {
                    @Override protected JButton createDecreaseButton(int orientation) {
                        return createArrowButton(SwingConstants.NORTH);
                    }
                    
                    @Override protected JButton createIncreaseButton(int orientation) {
                        return createArrowButton(SwingConstants.SOUTH);
                    }
                    
                    @Override protected void configureScrollBarColors() {
                        this.thumbColor = Color.WHITE;
                        this.trackColor = Color.BLACK;
                    }
                });
                
                return popup;
            }
            
            @Override
            protected JButton createArrowButton() {
                BasicArrowButton arrow = new BasicArrowButton(SwingConstants.SOUTH, Color.BLACK, Color.RED, Color.WHITE, Color.BLUE);
                arrow.setBorder(BorderFactory.createEmptyBorder());
                
                return arrow;
            }
            
            protected JButton createArrowButton(int direction) {
                BasicArrowButton arrow = new BasicArrowButton(direction, Color.BLACK, Color.RED, Color.WHITE, Color.BLUE);
                arrow.setBorder(BorderFactory.createEmptyBorder());
                
                return arrow;
            }
        });
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(frame, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void finalizeFrame() {
        frame.pack();
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setLocation((screen.width - frame.getWidth()) / 2, (screen.height - frame.getHeight()) / 2);
        frame.setVisible(true);
    }
}