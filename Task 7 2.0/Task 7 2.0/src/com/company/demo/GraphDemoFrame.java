package com.company.demo;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import com.util.SwingUtils;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.parse.Parser;
import org.apache.batik.anim.dom.SAXSVGDocumentFactory;
import org.apache.batik.bridge.*;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.util.XMLResourceDescriptor;
import org.w3c.dom.svg.SVGDocument;
import com.company.Graph;
import com.company.GraphUtils;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class GraphDemoFrame extends JFrame {
    private JTabbedPane tabbedPaneMain;
    private JPanel panelMain;
    private JPanel panelGraphTab;

    private JTextArea textAreaSystemOut;
    private JPanel panelGraphPainterContainer;
    private JButton buttonLoadGraphFromFile;
    private JTextArea textAreaGraphFile;
    private JComboBox comboBoxGraphType;
    private JSplitPane splitPaneGraphTab1;
    private JSplitPane splitPaneGraphTab2;
    private JButton buttonSaveGraphToFile;

    private JButton buttonSaveGraphSvgToFile;

    private JButton countButton;
    private JTextArea textAreaInputNumOfVertex;
    private JList jListOfCycles;
    private JPanel panelListOfCycles;

    private JFileChooser fileChooserTxtOpen;
    private JFileChooser fileChooserDotOpen;
    private JFileChooser fileChooserTxtSave;
    private JFileChooser fileChooserDotSave;
    private JFileChooser fileChooserImgSave;

    private Graph graph = null;

    private SvgPanel panelGraphPainter;
    private ArrayList<ArrayList<Integer>> listOfCycles = null;

    private static class SvgPanel extends JPanel {
        private String svg = null;
        private GraphicsNode svgGraphicsNode = null;

        public void paint(String svg) throws IOException {
            String xmlParser = XMLResourceDescriptor.getXMLParserClassName();
            SAXSVGDocumentFactory df = new SAXSVGDocumentFactory(xmlParser);
            SVGDocument doc = df.createSVGDocument(null, new StringReader(svg));
            UserAgent userAgent = new UserAgentAdapter();
            DocumentLoader loader = new DocumentLoader(userAgent);
            BridgeContext ctx = new BridgeContext(userAgent, loader);
            ctx.setDynamicState(BridgeContext.DYNAMIC);
            GVTBuilder builder = new GVTBuilder();
            svgGraphicsNode = builder.build(ctx, doc);

            this.svg = svg;
            repaint();
        }

        @Override
        public void paintComponent(Graphics gr) {
            super.paintComponent(gr);

            if (svgGraphicsNode == null) {
                return;
            }

            double scaleX = this.getWidth() / svgGraphicsNode.getPrimitiveBounds().getWidth();
            double scaleY = this.getHeight() / svgGraphicsNode.getPrimitiveBounds().getHeight();
            double scale = Math.min(scaleX, scaleY);
            AffineTransform transform = new AffineTransform(scale, 0, 0, scale, 0, 0);
            svgGraphicsNode.setTransform(transform);
            Graphics2D g2d = (Graphics2D) gr;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            svgGraphicsNode.paint(g2d);
        }
    }


    public GraphDemoFrame() {
        this.setTitle("Графы");
        this.setContentPane(panelMain);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.pack();


        splitPaneGraphTab1.setBorder(null);
        splitPaneGraphTab2.setBorder(null);

        fileChooserTxtOpen = new JFileChooser();
        fileChooserDotOpen = new JFileChooser();
        fileChooserTxtSave = new JFileChooser();
        fileChooserDotSave = new JFileChooser();
        fileChooserImgSave = new JFileChooser();
        fileChooserTxtOpen.setCurrentDirectory(new File("./files/input"));
        fileChooserDotOpen.setCurrentDirectory(new File("./files/input"));
        fileChooserTxtSave.setCurrentDirectory(new File("./files/input"));
        fileChooserDotSave.setCurrentDirectory(new File("./files/input"));
        fileChooserImgSave.setCurrentDirectory(new File("./files/output"));
        FileFilter txtFilter = new FileNameExtensionFilter("Text files (*.txt)", "txt");
        FileFilter dotFilter = new FileNameExtensionFilter("DOT files (*.dot)", "dot");
        FileFilter svgFilter = new FileNameExtensionFilter("SVG images (*.svg)", "svg");
        //FileFilter pngFilter = new FileNameExtensionFilter("PNG images (*.png)", "png");

        fileChooserTxtOpen.addChoosableFileFilter(txtFilter);
        fileChooserDotOpen.addChoosableFileFilter(dotFilter);
        fileChooserTxtSave.addChoosableFileFilter(txtFilter);
        fileChooserDotSave.addChoosableFileFilter(dotFilter);
        fileChooserImgSave.addChoosableFileFilter(svgFilter);
        //fileChooserImgSave.addChoosableFileFilter(pngFilter);

        fileChooserTxtSave.setAcceptAllFileFilterUsed(false);
        fileChooserTxtSave.setDialogType(JFileChooser.SAVE_DIALOG);
        fileChooserTxtSave.setApproveButtonText("Save");
        fileChooserDotSave.setAcceptAllFileFilterUsed(false);
        fileChooserDotSave.setDialogType(JFileChooser.SAVE_DIALOG);
        fileChooserDotSave.setApproveButtonText("Save");
        fileChooserImgSave.setAcceptAllFileFilterUsed(false);
        fileChooserImgSave.setDialogType(JFileChooser.SAVE_DIALOG);
        fileChooserImgSave.setApproveButtonText("Save");

        panelGraphPainterContainer.setLayout(new BorderLayout());
        panelGraphPainter = new SvgPanel();
        panelGraphPainterContainer.add(new JScrollPane(panelGraphPainter));


        buttonLoadGraphFromFile.addActionListener(e -> {
            if (fileChooserTxtOpen.showOpenDialog(GraphDemoFrame.this) == JFileChooser.APPROVE_OPTION) {
                try (Scanner sc = new Scanner(fileChooserTxtOpen.getSelectedFile())) {
                    sc.useDelimiter("\\Z");
                    textAreaGraphFile.setText(sc.next());
                } catch (Exception exc) {
                    SwingUtils.showErrorMessageBox(exc);
                }
            }
        });
        buttonSaveGraphToFile.addActionListener(e -> {
            if (fileChooserTxtSave.showSaveDialog(GraphDemoFrame.this) == JFileChooser.APPROVE_OPTION) {
                String filename = fileChooserTxtSave.getSelectedFile().getPath();
                if (!filename.toLowerCase().endsWith(".txt")) {
                    filename += ".txt";
                }
                try (FileWriter wr = new FileWriter(filename)) {
                    wr.write(textAreaGraphFile.getText());
                } catch (Exception exc) {
                    SwingUtils.showErrorMessageBox(exc);
                }
            }
        });


        buttonSaveGraphSvgToFile.addActionListener(e -> {
            if (panelGraphPainter.svg == null) {
                return;
            }
            if (fileChooserImgSave.showSaveDialog(GraphDemoFrame.this) == JFileChooser.APPROVE_OPTION) {
                String filename = fileChooserImgSave.getSelectedFile().getPath();
                if (!filename.toLowerCase().endsWith(".svg")) {
                    filename += ".svg";
                }
                try (FileWriter wr = new FileWriter(filename)) {
                    wr.write(panelGraphPainter.svg);
                } catch (Exception exc) {
                    SwingUtils.showErrorMessageBox(exc);
                }
            }
        });


        countButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    panelGraphPainter.removeAll();
                    int numOfVertex = Integer.parseInt(textAreaInputNumOfVertex.getText());

                    int[][] graphInArray = GraphUtils.makeGraph(numOfVertex);
                    Graph graph = GraphUtils.fromArr(graphInArray);

                    List<int[]> cycles = new ArrayList<>();
                    SolveTask gr = new SolveTask(graphInArray, cycles);

                    listOfCycles = gr.findCycles();


                    textAreaSystemOut.setText(Integer.toString(listOfCycles.size()));


                    panelGraphPainter.paint(dotToSvg(graph.toDot()));

                    DefaultListModel dlm = new DefaultListModel();
                    for (int i = 0; i < listOfCycles.size(); i++) {
                        dlm.addElement(listOfCycles.get(i));
                    }

                    jListOfCycles.setModel(dlm);

                    jListOfCycles.addMouseListener(new MouseAdapter() {
                        @Override
                        public void mouseClicked(MouseEvent e) {
                            try {
                                GraphDemoFrame.this.graph = graph;

                                panelGraphPainter.paint(dotToSvg(graph.toDotWithPath(listOfCycles.get(jListOfCycles.getSelectedIndex()))));
                            } catch (IOException ioException) {
                                ioException.printStackTrace();
                            }

                        }
                    });



                } catch (Exception exc) {
                    SwingUtils.showErrorMessageBox(exc);
                }
            }
        });




    }

    /**
     * Преобразование dot-записи в svg-изображение (с помощью Graphviz)
     *
     * @param dotSrc dot-запись
     * @return svg
     * @throws IOException
     */
    private static String dotToSvg(String dotSrc) throws IOException {
        MutableGraph g = new Parser().read(dotSrc);
        return Graphviz.fromGraph(g).render(Format.SVG).toString();
    }


    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        panelMain = new JPanel();
        panelMain.setLayout(new GridLayoutManager(1, 1, new Insets(10, 10, 10, 10), 10, 10));
        panelMain.setInheritsPopupMenu(true);
        tabbedPaneMain = new JTabbedPane();
        tabbedPaneMain.setName("");
        panelMain.add(tabbedPaneMain, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(200, 200), null, 0, false));
        panelGraphTab = new JPanel();
        panelGraphTab.setLayout(new GridLayoutManager(1, 1, new Insets(10, 10, 10, 10), 10, 10));
        tabbedPaneMain.addTab("Граф", null, panelGraphTab, "Демонстрация работы с графами");
        splitPaneGraphTab1 = new JSplitPane();
        splitPaneGraphTab1.setOrientation(0);
        splitPaneGraphTab1.setResizeWeight(0.75);
        panelGraphTab.add(splitPaneGraphTab1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(200, 200), null, 0, false));
        splitPaneGraphTab2 = new JSplitPane();
        splitPaneGraphTab2.setResizeWeight(0.0);
        splitPaneGraphTab1.setLeftComponent(splitPaneGraphTab2);
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(3, 1, new Insets(0, 0, 0, 0), -1, -1));
        splitPaneGraphTab2.setLeftComponent(panel1);
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        panel1.add(panel2, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        buttonLoadGraphFromFile = new JButton();
        buttonLoadGraphFromFile.setText("Загрузить из файла");
        panel2.add(buttonLoadGraphFromFile, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        buttonSaveGraphToFile = new JButton();
        buttonSaveGraphToFile.setText("Сохранить в файл");
        panel2.add(buttonSaveGraphToFile, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel1.add(panel3, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        countButton = new JButton();
        countButton.setText("Посчитать количество циклов в  полном графе");
        panel3.add(countButton, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        panelListOfCycles = new JPanel();
        panelListOfCycles.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel3.add(panelListOfCycles, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JScrollPane scrollPane1 = new JScrollPane();
        panelListOfCycles.add(scrollPane1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        jListOfCycles = new JList();
        scrollPane1.setViewportView(jListOfCycles);
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel1.add(panel4, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        textAreaInputNumOfVertex = new JTextArea();
        panel4.add(textAreaInputNumOfVertex, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(150, 50), null, 0, false));
        final JPanel panel5 = new JPanel();
        panel5.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel4.add(panel5, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JLabel label1 = new JLabel();
        label1.setText("Введите количество вершин:");
        panel5.add(label1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel6 = new JPanel();
        panel6.setLayout(new GridLayoutManager(2, 2, new Insets(0, 0, 0, 0), -1, -1));
        splitPaneGraphTab2.setRightComponent(panel6);
        buttonSaveGraphSvgToFile = new JButton();
        buttonSaveGraphSvgToFile.setText("Сохранить в файл");
        panel6.add(buttonSaveGraphSvgToFile, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        panelGraphPainterContainer = new JPanel();
        panelGraphPainterContainer.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel6.add(panelGraphPainterContainer, new GridConstraints(0, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        panel6.add(spacer1, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JPanel panel7 = new JPanel();
        panel7.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        splitPaneGraphTab1.setRightComponent(panel7);
        final JLabel label2 = new JLabel();
        label2.setText("Количество циклов:");
        panel7.add(label2, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        textAreaSystemOut = new JTextArea();
        panel7.add(textAreaSystemOut, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(150, 50), null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return panelMain;
    }

}
