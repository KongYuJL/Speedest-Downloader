import java.awt.*;
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
	protected JPanel listPanel = new JPanel();
	protected JButton [] buttons= new JButton[5];
	protected JButton startButton;//开始
	protected JButton suspendButton;//暂停
	protected JButton resumeButton;//恢复
	protected JButton stopButton;//停止
	protected JButton deleteButton;//删除任务
	protected JButton mulStartButton; // 高速开始
	protected MuchThreadDown MuchThreadDownloader;
	protected int taskCount;
	private String path = null;
	private String filepath = null;

	public DownloadManager(URL url, FileOutputStream fos, String path, String filepath, JPanel lp, int tc) throws IOException{
		listPanel = lp;
		taskCount= tc;
		this.path = path;
		this.filepath = filepath;
		setLayout(new BorderLayout());
		//BevelBorder:该类实现简单的双线斜面边框。
		add(getButtonPanel(), BorderLayout.SOUTH);

		downloader = new Downloader(url, fos, buttons);
		MuchThreadDownloader = new MuchThreadDown(path, filepath, 5, buttons, downloader);

		add(downloader, BorderLayout.CENTER);
		downloader.setBorder(new BevelBorder(BevelBorder.RAISED));
		Border border = new BevelBorder(BevelBorder.RAISED);
		String name = url.toString();
		int index = name.lastIndexOf('/');
		border = new TitledBorder(border, name.substring(index + 1));
		setBorder(border);
	}

	//放置按钮的JPanel
	private JPanel getButtonPanel() {
		JPanel outerPanel;//为了调整好布局。
		JPanel innerPanel = new JPanel();
		innerPanel.setLayout(new GridLayout(1, 5 , 10, 0));
		startButton = new JButton("开始");
		buttons[0] = startButton;
		startButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				startButton.setEnabled(false);
				resumeButton.setEnabled(false);
				suspendButton.setEnabled(true);
				stopButton.setEnabled(true);
				downloader.startDownload();
			}
		});
		innerPanel.add(startButton);

		mulStartButton = new JButton("高速开始");
		buttons[1] = mulStartButton;
		mulStartButton.addActionListener((new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				startButton.setEnabled(false);
				mulStartButton.setEnabled(false);
				stopButton.setEnabled(false);
				resumeButton.setEnabled(false);
				try {
					MuchThreadDownloader.download();
				} catch (Exception err) {
					err.printStackTrace();
				}
			}
		}));
		innerPanel.add(mulStartButton);

		suspendButton = new JButton("暂停");
		buttons[2] = suspendButton;
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
		buttons[3] = resumeButton;
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
		buttons[4] = stopButton;
		stopButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				stopButton.setEnabled(false);
				suspendButton.setEnabled(false);
				resumeButton.setEnabled(false);
				startButton.setEnabled(true);
				mulStartButton.setEnabled(true);
				downloader.stopDownload();
			}

		});
		innerPanel.add(stopButton);
		outerPanel = new JPanel();
		outerPanel.add(innerPanel);
		return outerPanel;
	}

}


//https://ss1.baidu.com/-4o3dSag_xI4khGko9WTAnF6hhy/image/h%3D300/sign=8000a165df1b0ef473e89e5eedc551a1/b151f8198618367afe76969623738bd4b21ce5fa.jpg
//https://ss0.baidu.com/-Po3dSag_xI4khGko9WTAnF6hhy/image/h%3D300/sign=e6cb69522534349b6b066885f9eb1521/91ef76c6a7efce1b5ef04082a251f3deb58f659b.jpg
//https://ss3.baidu.com/-fo3dSag_xI4khGko9WTAnF6hhy/image/h%3D300/sign=97520644516034a836e2be81fb1249d9/7c1ed21b0ef41bd5a31967b15cda81cb39db3d28.jpg
//https://ss1.baidu.com/-4o3dSag_xI4khGko9WTAnF6hhy/image/h%3D300/sign=b7b87a749f45d688bc02b4a494c37dab/4b90f603738da9774d57356cbd51f8198618e379.jpg
//https://ss1.baidu.com/-4o3dSag_xI4khGko9WTAnF6hhy/image/h%3D300/sign=a79a84689e13b07ea2bd56083cd69113/35a85edf8db1cb13153c3f5ad054564e92584b25.jpg
