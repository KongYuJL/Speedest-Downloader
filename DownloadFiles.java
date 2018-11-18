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
 * 用于在文本框中输入URL并创建对应的DownloadManager类的实例
 */
public class DownloadFiles extends JPanel{
	protected JPanel listPanel;//放置各个下载的面板
	protected GridBagConstraints constraints;//指定使用 GridBagLayout类布置的组件的约束。
	protected String filepath = "G:/";//所下载的文件保存的路径
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
	//地址栏、两按钮
	private JPanel getAddURLPanel() {
		JPanel panel = new JPanel();
		JLabel label = new JLabel("URL");
		final JTextField textField = new JTextField(30);
		final JButton downloadButton = new JButton("点击下载");

		ActionListener actionListener = new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				new Thread(){
					public void run() {
						downloadButton.setText("正在连接");
						downloadButton.setEnabled(false);
						if(createDownloader(textField.getText())){
							textField.setText("");
							++taskCount;
							frame.setTitle("共有：" + taskCount + "个下载任务");
							revalidate();
							//重新渲染发生变化的组件
						}
						downloadButton.setText("点击下载");
						downloadButton.setEnabled(true);
					}
				}.start();

			}
		};
		//添加actionListener监听器，Enter一键下载
		textField.addActionListener(actionListener);
		downloadButton.addActionListener(actionListener);

		JButton clearAll = new JButton("清除所有");
		clearAll.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				Downloader.cancelAllAndWait();
				listPanel.removeAll();
				revalidate();
				repaint();
				frame.setTitle("目前无下载任务");
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
				throw new Exception("无法确定所下载文件的长度!");
			}
			int index = url.lastIndexOf('/');

			File file = new File(filepath+url.substring(index + 1));

			if(file.exists()){
			    int flag;
                flag = JOptionPane.showConfirmDialog(this, "是否覆盖原文件继续下载？",
                        "该文件已经存在", JOptionPane.YES_NO_OPTION);
                // 0代表是，1代表否
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
					"无法下载！", JOptionPane.ERROR_MESSAGE);
		}
		return false;
	}
	
	private void createMenuBar(){
		JMenu menu1 = new JMenu("文件");
		JMenuItem item1= new JMenuItem("选择路径");
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
		System.out.println(fsv.getHomeDirectory());                //得到桌面路径
		fileChooser.setCurrentDirectory(fsv.getHomeDirectory());
		fileChooser.setDialogTitle("请选择文件的保存路径...");
		fileChooser.setApproveButtonText("确定");
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
