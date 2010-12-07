package sharewithall.client;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.GridBagLayout;
import javax.swing.JProgressBar;
import java.awt.GridBagConstraints;
import javax.swing.JLabel;
import java.awt.Insets;
import java.awt.Font;
import java.io.File;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class FileGraphicalInterface extends JFrame
{
    private JPanel contentPane;
    private SWAClient client;
    private JLabel L_Saving;
    private JProgressBar PB_Progress;
    private JLabel L_Size;
    private JLabel L_Sender;
    private JButton B_Stop;
    private JButton B_Open;
    private JCheckBox CB_Open;
    private boolean[] stopper;
    private boolean finished = false;
    
    private String getSizeFromBytes(int bytes) {
        if (bytes < 1024.0)
            return bytes + " bytes";
        
        double dbytes = bytes;
        dbytes /= 1024;
        if (dbytes < 1024.0)
            return (int)dbytes + "," + ((int)(dbytes*100))%100 + " KB";
        
        dbytes /= 1024;
        if (dbytes < 1024.0)
            return (int)dbytes + "," + ((int)(dbytes*100))%100 + " MB";
        
        dbytes /= 1024;
        return (int)dbytes + "," + ((int)(dbytes*100))%100 + " GB";
    }
    
    /**
     * Create the frame.
     */
    public FileGraphicalInterface(SWAClient cli, String sender, String path, int bytes, boolean open, boolean[] stopper)
    {
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent arg0) {
                if (finished) client.program.finishedDownload(FileGraphicalInterface.this);
            }
        });

        client = cli;
        this.stopper = stopper;
        setTitle("Receiving file");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setBounds(100, 100, 373, 199);
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);
        GridBagLayout gbl_contentPane = new GridBagLayout();
        gbl_contentPane.columnWidths = new int[]{100, 0, 0};
        gbl_contentPane.rowHeights = new int[]{0, 0, 0, 30, 30, 0, 0};
        gbl_contentPane.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
        gbl_contentPane.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
        contentPane.setLayout(gbl_contentPane);
        
        JLabel lblReceivingFile = new JLabel("Saving file to:");
        lblReceivingFile.setFont(new Font("SansSerif", Font.BOLD, 12));
        GridBagConstraints gbc_lblReceivingFile = new GridBagConstraints();
        gbc_lblReceivingFile.anchor = GridBagConstraints.WEST;
        gbc_lblReceivingFile.insets = new Insets(0, 0, 5, 5);
        gbc_lblReceivingFile.gridx = 0;
        gbc_lblReceivingFile.gridy = 0;
        contentPane.add(lblReceivingFile, gbc_lblReceivingFile);
        
        L_Saving = new JLabel("");
        L_Saving.setText(path);
        L_Saving.setFont(new Font("SansSerif", Font.PLAIN, 12));
        GridBagConstraints gbc_L_Saving = new GridBagConstraints();
        gbc_L_Saving.fill = GridBagConstraints.HORIZONTAL;
        gbc_L_Saving.insets = new Insets(0, 0, 5, 0);
        gbc_L_Saving.gridx = 1;
        gbc_L_Saving.gridy = 0;
        contentPane.add(L_Saving, gbc_L_Saving);
        
        JLabel lblFrom = new JLabel("Sender:");
        lblFrom.setFont(new Font("SansSerif", Font.BOLD, 12));
        GridBagConstraints gbc_lblFrom = new GridBagConstraints();
        gbc_lblFrom.anchor = GridBagConstraints.WEST;
        gbc_lblFrom.insets = new Insets(0, 0, 5, 5);
        gbc_lblFrom.gridx = 0;
        gbc_lblFrom.gridy = 1;
        contentPane.add(lblFrom, gbc_lblFrom);
        
        L_Sender = new JLabel("");
        L_Sender.setText(sender);
        L_Sender.setFont(new Font("SansSerif", Font.PLAIN, 12));
        GridBagConstraints gbc_L_Sender = new GridBagConstraints();
        gbc_L_Sender.fill = GridBagConstraints.HORIZONTAL;
        gbc_L_Sender.insets = new Insets(0, 0, 5, 0);
        gbc_L_Sender.gridx = 1;
        gbc_L_Sender.gridy = 1;
        contentPane.add(L_Sender, gbc_L_Sender);
        
        JLabel lblFileSize = new JLabel("File size:");
        lblFileSize.setFont(new Font("SansSerif", Font.BOLD, 12));
        GridBagConstraints gbc_lblFileSize = new GridBagConstraints();
        gbc_lblFileSize.anchor = GridBagConstraints.WEST;
        gbc_lblFileSize.insets = new Insets(0, 0, 5, 5);
        gbc_lblFileSize.gridx = 0;
        gbc_lblFileSize.gridy = 2;
        contentPane.add(lblFileSize, gbc_lblFileSize);
        
        L_Size = new JLabel("");
        L_Size.setText(getSizeFromBytes(bytes));
        L_Size.setFont(new Font("SansSerif", Font.PLAIN, 12));
        GridBagConstraints gbc_L_Size = new GridBagConstraints();
        gbc_L_Size.fill = GridBagConstraints.HORIZONTAL;
        gbc_L_Size.insets = new Insets(0, 0, 5, 0);
        gbc_L_Size.gridx = 1;
        gbc_L_Size.gridy = 2;
        contentPane.add(L_Size, gbc_L_Size);
        
        JLabel lblProgress = new JLabel("Progress:");
        lblProgress.setFont(new Font("SansSerif", Font.BOLD, 12));
        GridBagConstraints gbc_lblProgress = new GridBagConstraints();
        gbc_lblProgress.anchor = GridBagConstraints.WEST;
        gbc_lblProgress.insets = new Insets(0, 0, 5, 5);
        gbc_lblProgress.gridx = 0;
        gbc_lblProgress.gridy = 3;
        contentPane.add(lblProgress, gbc_lblProgress);
        
        PB_Progress = new JProgressBar();
        PB_Progress.setMaximum(bytes);
        GridBagConstraints gbc_PB_Progress = new GridBagConstraints();
        gbc_PB_Progress.fill = GridBagConstraints.HORIZONTAL;
        gbc_PB_Progress.insets = new Insets(0, 0, 5, 0);
        gbc_PB_Progress.gridx = 1;
        gbc_PB_Progress.gridy = 3;
        contentPane.add(PB_Progress, gbc_PB_Progress);
        
        CB_Open = new JCheckBox("Open when complete");
        CB_Open.setSelected(open);
        GridBagConstraints gbc_CB_Open = new GridBagConstraints();
        gbc_CB_Open.anchor = GridBagConstraints.WEST;
        gbc_CB_Open.insets = new Insets(0, 0, 5, 0);
        gbc_CB_Open.gridx = 1;
        gbc_CB_Open.gridy = 4;
        contentPane.add(CB_Open, gbc_CB_Open);
        
        JPanel panel = new JPanel();
        GridBagConstraints gbc_panel = new GridBagConstraints();
        gbc_panel.fill = GridBagConstraints.HORIZONTAL;
        gbc_panel.gridx = 1;
        gbc_panel.gridy = 5;
        contentPane.add(panel, gbc_panel);
        GridBagLayout gbl_panel = new GridBagLayout();
        gbl_panel.columnWidths = new int[]{59, 97, 0};
        gbl_panel.rowHeights = new int[]{23, 0};
        gbl_panel.columnWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
        gbl_panel.rowWeights = new double[]{0.0, Double.MIN_VALUE};
        panel.setLayout(gbl_panel);
        
        B_Open = new JButton("Open");
        B_Open.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                openFile();
            }
        });
        B_Open.setEnabled(false);
        GridBagConstraints gbc_B_Open = new GridBagConstraints();
        gbc_B_Open.anchor = GridBagConstraints.NORTHWEST;
        gbc_B_Open.insets = new Insets(0, 0, 0, 5);
        gbc_B_Open.gridx = 0;
        gbc_B_Open.gridy = 0;
        panel.add(B_Open, gbc_B_Open);
        
        B_Stop = new JButton("Stop transfer");
        B_Stop.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                stopDownload();
            }
        });
        GridBagConstraints gbc_B_Stop = new GridBagConstraints();
        gbc_B_Stop.anchor = GridBagConstraints.NORTHWEST;
        gbc_B_Stop.gridx = 1;
        gbc_B_Stop.gridy = 0;
        panel.add(B_Stop, gbc_B_Stop);
        
        setVisible(true);
    }
    
    private void stopDownload() {
        FileGraphicalInterface.this.stopper[0] = true;
        client.program.finishedDownload(this);
    }
    
    public void setProgress(int progress) {
        PB_Progress.setValue(progress);
    }
    
    public void finishedDownload() {
        finished = true;
        B_Stop.setEnabled(false);
        if (CB_Open.isSelected()) openFile();
        else B_Open.setEnabled(true);
    }
    
    private void openFile() {
        String path = L_Saving.getText();
        boolean ok = false;
        java.awt.Desktop desktop = null;
        if (java.awt.Desktop.isDesktopSupported() ) {
            desktop = java.awt.Desktop.getDesktop();
            ok = desktop.isSupported(java.awt.Desktop.Action.OPEN);
        }
        if (ok) {
            try {
                File f = new File(path);
                desktop.open(f);
            }
            catch ( Exception e ) {
                ok = false;
            }
        }
        if (!ok) {
            client.program.showErrorMessage("Open File error", "Cannot open the file " + path);
        }
        client.program.finishedDownload(this);
    }
    
    public JLabel getL_Saving() {
        return L_Saving;
    }
    public JProgressBar getPB_Progress() {
        return PB_Progress;
    }
    public JLabel getL_Size() {
        return L_Size;
    }
    public JLabel getL_Sender() {
        return L_Sender;
    }
    public JButton getB_Stop() {
        return B_Stop;
    }
    public JButton getB_Open() {
        return B_Open;
    }
    public JCheckBox getCB_Open() {
        return CB_Open;
    }
}
