import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.net.URLConnection;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.UIManager;
/**
 * 用于在文本框中输入URL并创建对应的DownloadManager类的实例
 */
public class DownloadFiles extends JPanel{
	protected JPanel listPanel;//放置各个下载的面板
	protected GridBagConstraints constraints;//指定使用 GridBagLayout类布置的组件的约束。
	protected final String filepath = "G:/";//所下载的文件保存的路径
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

			File file=new File(filepath+url.substring(index + 1));
			if(file.exists()){
				JOptionPane.showMessageDialog(this, "该文件已经存在",
						"无法下载", JOptionPane.ERROR_MESSAGE);
				return false;
			}
			FileOutputStream fos = new FileOutputStream(file);

			//BufferedOutputStream bos = new BufferedOutputStream(fos);
			DownloadManager dm = new DownloadManager(downloadURL, fos);
			listPanel.add(dm, constraints);
			return true;
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, "该资源无法下载。",
					"无法下载！", JOptionPane.ERROR_MESSAGE);
		}
		return false;
	}
}
