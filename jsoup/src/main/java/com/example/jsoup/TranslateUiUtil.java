package com.example.jsoup;

import com.mysql.cj.util.StringUtils;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileSystemView;

public class TranslateUiUtil {
    private static TranslateUiUtil sUiUtil;
    private JTextArea textArea;
    private Font fontAwesome;

    private TranslateUiUtil() {
        try {
            fontAwesome = Font.createFont(Font.TRUETYPE_FONT, new File("font-awesome-4.2.0\\fonts\\fontawesome-webfont.ttf"));
            fontAwesome = fontAwesome.deriveFont(Font.PLAIN, 100);
        } catch (FontFormatException | IOException e) {
            e.printStackTrace();
        }

    }

    public static TranslateUiUtil getInstance() {
        if (sUiUtil == null) {
            sUiUtil = new TranslateUiUtil();
        }
        return sUiUtil;
    }

    public void showDialog(final CallBack callBack) {
        final JFrame frmIpa = new JFrame();
        frmIpa.setTitle("翻译文案替换");
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
        textArea = new JTextArea() {
            @Override
            protected void paintComponent(Graphics graphics) {
                Graphics2D graphics2d = (Graphics2D) graphics;
                graphics2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                super.paintComponent(graphics);
            }
        };
        textArea.setFont(fontAwesome);
        textArea.setLineWrap(true);
//		textArea.setFont(new Font("黑体",Font.BOLD,15));
        scrollPane.setViewportView(textArea);
        JButton button = new JButton("选择源文件") {
            @Override
            protected void paintComponent(Graphics graphics) {
                Graphics2D graphics2d = (Graphics2D) graphics;
                graphics2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                super.paintComponent(graphics);
            }
        };
        button.setFont(fontAwesome);
        // 监听button的选择路径
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser jfc = new JFileChooser();
                UIManager.put(jfc, fontAwesome);
                jfc.addChoosableFileFilter(new FileFilter() {
                    @Override
                    public boolean accept(File file) {
                        String name = file.getName();
                        return file.isDirectory() || name.toLowerCase().endsWith(".xls") || name.toLowerCase().endsWith(".xlsx");  // 仅显示目录和xls、xlsx文件
                    }

                    @Override
                    public String getDescription() {
                        return "*.xls;*.xlsx";
                    }
                });
                FileSystemView fsv = FileSystemView.getFileSystemView();
                jfc.setCurrentDirectory(fsv.getHomeDirectory());
                jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);//只能选择文件
//						 jfc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES );
                // 显示打开的文件对话框
                jfc.showSaveDialog(frmIpa);
                try {
                    // 使用文件类获取选择器选择的文件
                    File file = jfc.getSelectedFile();//
                    if (file.getPath().endsWith(".xls") || file.getPath().endsWith("xlsx")) {
                        textArea.append(file.getPath() + "\n");
                        if (callBack != null) {
                            callBack.onChooseFile(file.getPath());
                        }
                    } else {
                        JPanel panel3 = new JPanel();
                        JOptionPane.showMessageDialog(panel3, "选中文件格式有误", "提示", JOptionPane.WARNING_MESSAGE);
                    }
                } catch (Exception e2) {
                    JPanel panel3 = new JPanel();
                    JOptionPane.showMessageDialog(panel3, "没有选中任何文件", "提示", JOptionPane.WARNING_MESSAGE);
                }
            }
        });
        JButton button2 = new JButton("选择目标文件夹") {
            @Override
            protected void paintComponent(Graphics graphics) {
                Graphics2D graphics2d = (Graphics2D) graphics;
                graphics2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                super.paintComponent(graphics);
            }
        };
        button2.setFont(fontAwesome);
        button2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                JFileChooser jfc = new JFileChooser();
                FileSystemView fsv = FileSystemView.getFileSystemView();
                jfc.setCurrentDirectory(fsv.getHomeDirectory());
                jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);//只能选择目录
//						 jfc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES );
                // 显示打开的文件对话框
                jfc.showSaveDialog(frmIpa);
                try {
                    // 使用文件类获取选择器选择的文件
                    File file = jfc.getSelectedFile();//
                    textArea.append(file.getPath() + "\n");
                    if (callBack != null) {
                        callBack.onChooseFileDir(file.getPath());
                    }
                } catch (Exception e2) {
                    JPanel panel3 = new JPanel();
                    JOptionPane.showMessageDialog(panel3, "没有选中任何文件夹", "提示", JOptionPane.WARNING_MESSAGE);
                }
            }
        });
        JButton button3 = new JButton("开始添加文案") {
            @Override
            protected void paintComponent(Graphics graphics) {
                Graphics2D graphics2d = (Graphics2D) graphics;
                graphics2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                super.paintComponent(graphics);
            }
        };
        button3.setFont(fontAwesome);
        button3.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                setText("写入中...");
                if (callBack != null) {
                    callBack.startTranslate();
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
            textArea.selectAll();
            textArea.setCaretPosition(textArea.getSelectedText().length());
            textArea.requestFocus();
        }
    }

    public interface CallBack {
        void onChooseFileDir(String path);

        void startTranslate();

        void onChooseFile(String path);
    }
}
