import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.net.URLConnection;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;

/**
 * �������ı���������URL��������Ӧ��DownloadManager���ʵ��
 */
public class DownloadFiles extends JPanel{
	protected JPanel listPanel;//���ø������ص����
	protected GridBagConstraints constraints;//ָ��ʹ�� GridBagLayout�಼�õ������Լ����
	protected String filepath = "G:/";//�����ص��ļ������·��
	private int taskCount = 0;
	static JFrame frame;
	public static void main(String[] args){
		frame = new JFrame("Speedest Downloader");
		DownloadFiles df = new DownloadFiles();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().add(df);
		frame.setSize(700, 400);		
		frame.setVisible(true);
	}
	public DownloadFiles(){
		try {
			UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
			//UIManager.setLookAndFeel("com.sun.java.swing.plaf.motif.MotifLookAndFeel");
			//UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
		} catch (Exception e) {
			e.printStackTrace();
		}
		setLayout(new BorderLayout());
		listPanel = new JPanel();
		listPanel.setLayout(new GridBagLayout());
		constraints = new GridBagConstraints();
		constraints.gridx = 0;
		constraints.weightx = 1;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.anchor = GridBagConstraints.NORTH;
		JScrollPane jsp = new JScrollPane(listPanel);
		JScrollBar b = jsp.getVerticalScrollBar();
		b.setUnitIncrement(50);
		createMenuBar();
		add(jsp, BorderLayout.CENTER);
		add(getAddURLPanel(), BorderLayout.SOUTH);
	}
	//��ַ��������ť
	private JPanel getAddURLPanel() {
		JPanel panel = new JPanel();
		JLabel label = new JLabel("URL");
		final JTextField textField = new JTextField(30);
		final JButton downloadButton = new JButton("�������");

		ActionListener actionListener = new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				new Thread(){
					public void run() {
						downloadButton.setText("��������");
						downloadButton.setEnabled(false);
						if(createDownloader(textField.getText())){
							textField.setText("");
							++taskCount;
							frame.setTitle("���У�" + taskCount + "����������");
							revalidate();
							//������Ⱦ�����仯�����
						}
						downloadButton.setText("�������");
						downloadButton.setEnabled(true);
					}
				}.start();

			}
		};
		//���actionListener��������Enterһ������
		textField.addActionListener(actionListener);
		downloadButton.addActionListener(actionListener);

		JButton clearAll = new JButton("�������");
		clearAll.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				Downloader.cancelAllAndWait();
				listPanel.removeAll();
				revalidate();
				repaint();
				frame.setTitle("Ŀǰ����������");
				taskCount = 0;
			}
		});

		panel.add(label);
		panel.add(textField);
		panel.add(downloadButton);
		panel.add(clearAll);
		return panel;
	}

	private boolean createDownloader(String url) {
		try {
			URL downloadURL = new URL(url);
			URLConnection urlconnection = downloadURL.openConnection();
			int length = urlconnection.getContentLength();
			if(length < 0){
				throw new Exception("�޷�ȷ���������ļ��ĳ���!");
			}
			int index = url.lastIndexOf('/');

			File file = new File(filepath+url.substring(index + 1));

			if(file.exists()){
			    int flag;
                flag = JOptionPane.showConfirmDialog(this, "�Ƿ񸲸�ԭ�ļ��������أ�",
                        "���ļ��Ѿ�����", JOptionPane.YES_NO_OPTION);
                // 0�����ǣ�1�����
                if(flag==1)
                    return false;
			}
			FileOutputStream fos = new FileOutputStream(file);

			//BufferedOutputStream bos = new BufferedOutputStream(fos);
			DownloadManager dm = new DownloadManager(downloadURL, fos);
			listPanel.add(dm, constraints);
			return true;
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, e.getMessage(),
					"�޷����أ�", JOptionPane.ERROR_MESSAGE);
		}
		return false;
	}
	
	private void createMenuBar(){
		JMenu menu1 = new JMenu("�ļ�");
		JMenuItem item1= new JMenuItem("ѡ��·��");
		item1.addActionListener(new FileChooserEventListener());
		menu1.add(item1);
		JMenuBar menubar = new JMenuBar();
		menubar.add(menu1);
		frame.setJMenuBar(menubar);
	}
	
	private void createFileChooser(){
		int result;
		JFileChooser fileChooser = new JFileChooser();
		FileSystemView fsv = FileSystemView.getFileSystemView();
		System.out.println(fsv.getHomeDirectory());                //�õ�����·��
		fileChooser.setCurrentDirectory(fsv.getHomeDirectory());
		fileChooser.setDialogTitle("��ѡ���ļ��ı���·��...");
		fileChooser.setApproveButtonText("ȷ��");
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		result = fileChooser.showSaveDialog(frame);
		if (JFileChooser.APPROVE_OPTION == result) {
	    	   filepath=fileChooser.getSelectedFile().getPath()+'\\';
	    	   System.out.println("path: " + filepath);
		}
	}
	
	class FileChooserEventListener implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent e){
			createFileChooser();	
		}
	}
}
