package com.example.jsoup;

import com.mysql.cj.util.StringUtils;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class UiUtil {
    private static UiUtil sUiUtil;
    private JTextArea textArea;

    private UiUtil() {}
    public static UiUtil getInstance() {
        if (sUiUtil == null) {
            sUiUtil = new UiUtil();
        }
        return sUiUtil;
    }
    public void showDialog(String title, CallBack callBack) {
        JFrame frmIpa = new JFrame();
        frmIpa.setTitle(title);
        frmIpa.setBounds(600, 300, 500, 400);
        frmIpa.setLayout(new BorderLayout(0, 0));
        frmIpa.setResizable(true);
        frmIpa.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // 面板1
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout());
        frmIpa.getContentPane().add(panel, BorderLayout.NORTH);
        // 可滚动面板
        JScrollPane scrollPane = new JScrollPane();
        frmIpa.getContentPane().add(scrollPane, BorderLayout.CENTER);
        textArea = new JTextArea();
        textArea.setLineWrap(true);
//		textArea.setFont(new Font("黑体",Font.BOLD,15));
        scrollPane.setViewportView(textArea);
        JButton button = new JButton("选择文件");
        // 监听button的选择路径
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser jfc = new JFileChooser();
                jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);//只能选择目录
//						 jfc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES );
                // 显示打开的文件对话框
                jfc.showSaveDialog(frmIpa);
                try {
                    // 使用文件类获取选择器选择的文件
                    File file = jfc.getSelectedFile();//
                    textArea.setText(file.getPath());
                    if (callBack != null) {
                        callBack.onChooseFileDir(file.getPath());
                    }
                } catch (Exception e2) {
                    JPanel panel3 = new JPanel();
                    JOptionPane.showMessageDialog(panel3, "没有选中任何文件", "提示", JOptionPane.WARNING_MESSAGE);
                }
            }
        });
        JButton button2 = new JButton("开始加载");
        button2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if (callBack != null) {
                    callBack.onStatusChange(true, textArea.getText());
                }
            }
        });
        JButton button3 = new JButton("停止加载");
        button3.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if (callBack != null) {
                    callBack.onStatusChange(false, textArea.getText());
                }
            }
        });
        panel.add(button);
        panel.add(button2);
        panel.add(button3);
        frmIpa.setVisible(true);
        frmIpa.addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent windowEvent) {
            }

            @Override
            public void windowClosing(WindowEvent windowEvent) {
                MyClass.stopEvery();
            }

            @Override
            public void windowClosed(WindowEvent windowEvent) {
            }

            @Override
            public void windowIconified(WindowEvent windowEvent) {
            }

            @Override
            public void windowDeiconified(WindowEvent windowEvent) {
            }

            @Override
            public void windowActivated(WindowEvent windowEvent) {
            }

            @Override
            public void windowDeactivated(WindowEvent windowEvent) {
            }
        });
    }

    public synchronized void setText(String text) {
        if (textArea != null && !StringUtils.isNullOrEmpty(text)) {
            textArea.append("\n" + text);
        }
    }

    public interface CallBack {
        void onChooseFileDir(String path);

        void onStatusChange(boolean start, String path);
    }
}
