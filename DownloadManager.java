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
 * �������أ���ʼ����ͣ��ֹͣ
 */
public class DownloadManager extends JPanel{
	protected Downloader downloader;
	protected JButton startButton;//��ʼ
	protected JButton sleepButton;//��ͣ5��
	protected JButton suspendButton;//��ͣ
	protected JButton resumeButton;//�ָ�
	protected JButton stopButton;//ֹͣ
	
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
		//BevelBorder:����ʵ�ּ򵥵�˫��б��߿� 
		downloader.setBorder(new BevelBorder(BevelBorder.RAISED));
		add(downloader, BorderLayout.CENTER);
		add(getButtonPanel(), BorderLayout.SOUTH);
	}
	//���ð�ť��JPanel
	private JPanel getButtonPanel() {
		JPanel outerPanel;//Ϊ�˵����ò��֡� 
		JPanel innerPanel = new JPanel();
		innerPanel.setLayout(new GridLayout(1, 5 , 10, 0));
		
		startButton = new JButton("��ʼ");
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
		sleepButton = new JButton("�ݶ�5��");
		sleepButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				downloader.setSleepScheduled(true);
			}
			
		});
		innerPanel.add(sleepButton);
		
		suspendButton = new JButton("��ͣ");
		suspendButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				suspendButton.setEnabled(false);
				resumeButton.setEnabled(true);
				stopButton.setEnabled(true);
				downloader.setSuspended(true);
			}
			
		});
		innerPanel.add(suspendButton);
		
		resumeButton = new JButton("�ָ�����");
		resumeButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				resumeButton.setEnabled(false);
				suspendButton.setEnabled(true);
				stopButton.setEnabled(true);
				downloader.resumeDownloader();
			}
			
		});
		innerPanel.add(resumeButton);
		
		stopButton = new JButton("ֹͣ");
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
