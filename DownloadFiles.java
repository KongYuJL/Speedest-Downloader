import com.sun.org.apache.bcel.internal.generic.RETURN;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Properties;
import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.filechooser.FileSystemView;

import java.util.*;
import javax.swing.*;
import java.awt.*;
import java.awt.image.*;

/**
 * 用于在文本框中输入URL并创建对应的DownloadManager类的实例
 */
public class DownloadFiles extends JPanel{
	private int threadCount = 3;

	protected JPanel listPanel;//放置各个下载的面板
	protected GridBagConstraints constraints;//指定使用 GridBagLayout类布置的组件的约束。
	protected String filepath = "/home/jeremy/Desktop/";//所下载的文件保存的路径
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
		final JButton downloadButton = new JButton("下载");

		ActionListener actionListener = new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				new Thread(){
					public void run() {
						downloadButton.setText("正在连接");
						downloadButton.setEnabled(false);
						if(createDownloader(textField.getText())){
							textField.setText("");
							++taskCount;
							frame.setTitle("共有" + taskCount + "个下载任务");
							revalidate();
							//重新渲染发生变化的组件
						}
						downloadButton.setText("下载");
						downloadButton.setEnabled(true);
					}
				}.start();
			}
		};
		//添加actionListener监听器，Enter一键下载
		textField.addActionListener(actionListener);
		downloadButton.addActionListener(actionListener);

		JButton clearAll = new JButton("清空");
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
			DownloadManager dm = new DownloadManager(downloadURL, fos, url, filepath, listPanel, taskCount);
			listPanel.add(dm, constraints);
			return true;
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, e.getMessage(),
					"无法下载！", JOptionPane.ERROR_MESSAGE);
		}
		return false;
	}

	private void createMenuBar(){
		JMenuItem menu1 = new JMenuItem("选择路径");
		menu1.setHorizontalAlignment(SwingConstants.CENTER);
		menu1.addActionListener(new FileChooserEventListener());

		JMenuItem menu2 = new JMenuItem("删除任务");
		menu2.setHorizontalAlignment(SwingConstants.CENTER);
		menu2.addActionListener(new DeleteEventListener());

		JMenuItem menu3 = new JMenuItem("帮助");
		menu3.setHorizontalAlignment(SwingConstants.CENTER);
		menu3.addActionListener(new HelpEventListener());

		JMenuItem menu4 = new JMenuItem("版权信息");
		menu4.setHorizontalAlignment(SwingConstants.CENTER);
		menu4.addActionListener(new CopyRightEventListener());

		JMenuBar menubar = new JMenuBar();
		menubar.add(menu1);
		menubar.add(menu2);
		menubar.add(menu3);
		menubar.add(menu4);
		frame.setJMenuBar(menubar);
	}

	private void createFileChooser(){
		int result;
		JFileChooser fileChooser = new JFileChooser();
		FileSystemView fsv = FileSystemView.getFileSystemView();
		System.out.println(fsv.getHomeDirectory());
		fileChooser.setCurrentDirectory(fsv.getHomeDirectory());
		fileChooser.setDialogTitle("请选择文件的保存路径...");
		fileChooser.setApproveButtonText("确定");
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		result = fileChooser.showSaveDialog(frame);
		if (JFileChooser.APPROVE_OPTION == result) {
			Properties prop = System.getProperties();
			String os = prop.getProperty("os.name");
			if (os != null && os.toLowerCase().indexOf("linux") > -1)
				filepath=fileChooser.getSelectedFile().getPath()+'/';
			else
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

	private void deleteEvent(){
		if(taskCount==0){
			JOptionPane.showMessageDialog(this, "当前无下载任务",
					"无法删除！", JOptionPane.ERROR_MESSAGE);
			return;
		}
		int index;
		String input = JOptionPane.showInputDialog("请输入要删除的下载任务的序号");
		while(!input.matches("^[0-9_]+$")){
			JOptionPane.showMessageDialog(this, "您输入的序号有误，请重新输入",
					"无法删除！", JOptionPane.ERROR_MESSAGE);
			input = JOptionPane.showInputDialog("请输入要删除的下载任务的序号");
		}
		index = Integer.parseInt(input);

		if(index>taskCount||index<=0){
			JOptionPane.showMessageDialog(this, "您输入的序号超出范围",
					"无法删除！", JOptionPane.ERROR_MESSAGE);
			return;
		}
		listPanel.remove(index-1);
		taskCount--;
		frame.setTitle("共有" + taskCount + "个下载任务");
		repaint();
	}
	class DeleteEventListener implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent e){
			deleteEvent();
		}
	}

	private void helpEvent(){
		JOptionPane.showMessageDialog(this,
				"1.请在下载文件前，确保下载路径无误，软件支持Windows与Linux系统；\n" +
						"2.删除任务时，任务序号从1开始；\n" +
						"3.部分文件可能不支持多线程下载；\n" +
						"4.请输入正确的URL链接\n",
				"帮助信息", JOptionPane.INFORMATION_MESSAGE);
	}
	class HelpEventListener implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent e){
			helpEvent();
		}
	}

	private void copyRightEvent(){
		ImageIcon icon = new ImageIcon("copyright.png");
		icon.setImage(icon.getImage().getScaledInstance(50, 50,Image.SCALE_DEFAULT));
		JOptionPane.showMessageDialog(this,
				"Speedest下载器\n" +
						"江西师范大学计算机信息工程学院-面向对象课程设计\n" +
						"开发小组成员：\n" +
						"刘欣阳 学号-2016262044 班级-计科二班\n" +
						"朱国根 学号-2016262057 班级-计科二班\n" +
						"张宇婷 学号-2016262063 班级-计科二班\n",
				"版权信息", JOptionPane.INFORMATION_MESSAGE, icon);
	}

	class CopyRightEventListener implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent e){
			copyRightEvent();
		}
	}
}