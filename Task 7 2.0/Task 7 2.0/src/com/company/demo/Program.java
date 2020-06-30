package com.company.demo;

import static guru.nidi.graphviz.attribute.Records.rec;

import static guru.nidi.graphviz.model.Factory.graph;
import static guru.nidi.graphviz.model.Factory.node;
import static guru.nidi.graphviz.model.Factory.to;
import static guru.nidi.graphviz.model.Factory.port;

import java.awt.EventQueue;

import static java.awt.Frame.MAXIMIZED_BOTH;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import javax.swing.JFrame;
import javax.swing.UIManager;

import com.company.GraphUtils;
import com.util.SwingUtils;
import guru.nidi.graphviz.model.Graph;

public class Program {

    /**
     * Основная функция программы
     *
     * @param args Параметры командной строки
     * @throws Exception
     */

    public static void main(String[] args) throws Exception {
        Locale.setDefault(Locale.ROOT);
//        int[][] graph = GraphUtils.makeGraph(6);
//
//
//        List<int[]> cycles = new ArrayList<int[]>();
//
//        SolveTask gr = new SolveTask(graph, cycles);
//        ArrayList<ArrayList<Integer>> listOfCycles = gr.dfg();
//        for (int i = 0; i < listOfCycles.size(); i++) {
//            System.out.println(listOfCycles.get(i));
//        }
//        System.out.println(listOfCycles.size());


//        trying(8);
        //SwingUtils.setLookAndFeelByName("Windows");
        //UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        //SwingUtils.setDefaultFont(null, 20);
        UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        SwingUtils.setDefaultFont("Arial", 20);

        EventQueue.invokeLater(() -> {
            try {
                JFrame mainFrame = new GraphDemoFrame();
                mainFrame.setVisible(true);
                mainFrame.setExtendedState(MAXIMIZED_BOTH);
            } catch (Exception ex) {
                SwingUtils.showErrorMessageBox(ex);
            }
        });
    }


}
