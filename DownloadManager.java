import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
 
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
/**
 * 控制下载：开始、暂停、停止
 */
public class DownloadManager extends JPanel{
	protected Downloader downloader;
	protected JButton startButton;//开始
	protected JButton sleepButton;//暂停5秒
	protected JButton suspendButton;//暂停
	protected JButton resumeButton;//恢复
	protected JButton stopButton;//停止
	
	public DownloadManager(URL url, FileOutputStream fos) throws IOException{ 
		downloader = new Downloader(url, fos);
		buildLayout();
		Border border = new BevelBorder(BevelBorder.RAISED);
		String name = url.toString();
		int index = name.lastIndexOf('/');
		border = new TitledBorder(border, name.substring(index + 1)); 
		setBorder(border);
	}
	private void buildLayout() {
		setLayout(new BorderLayout());
		//BevelBorder:该类实现简单的双线斜面边框。 
		downloader.setBorder(new BevelBorder(BevelBorder.RAISED));
		add(downloader, BorderLayout.CENTER);
		add(getButtonPanel(), BorderLayout.SOUTH);
	}
	//放置按钮的JPanel
	private JPanel getButtonPanel() {
		JPanel outerPanel;//为了调整好布局。 
		JPanel innerPanel = new JPanel();
		innerPanel.setLayout(new GridLayout(1, 5 , 10, 0));
		
		startButton = new JButton("开始");
		startButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				startButton.setEnabled(false);
				sleepButton.setEnabled(true);
				resumeButton.setEnabled(false);
				suspendButton.setEnabled(true);
				stopButton.setEnabled(true);
				downloader.startDownload();
			}
		});
		innerPanel.add(startButton);
		sleepButton = new JButton("暂定5秒");
		sleepButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				downloader.setSleepScheduled(true);
			}
			
		});
		innerPanel.add(sleepButton);
		
		suspendButton = new JButton("暂停");
		suspendButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				suspendButton.setEnabled(false);
				resumeButton.setEnabled(true);
				stopButton.setEnabled(true);
				downloader.setSuspended(true);
			}
			
		});
		innerPanel.add(suspendButton);
		
		resumeButton = new JButton("恢复下载");
		resumeButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				resumeButton.setEnabled(false);
				suspendButton.setEnabled(true);
				stopButton.setEnabled(true);
				downloader.resumeDownloader();
			}
			
		});
		innerPanel.add(resumeButton);
		
		stopButton = new JButton("停止");
		stopButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				stopButton.setEnabled(false);
				sleepButton.setEnabled(false);
				suspendButton.setEnabled(false);
				resumeButton.setEnabled(false);
				startButton.setEnabled(true);
				downloader.stopDownload();
			}
			
		});
		innerPanel.add(stopButton);
		
		outerPanel = new JPanel();
		outerPanel.add(innerPanel);
		return outerPanel;
	}
}
