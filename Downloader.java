import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
// 这种布局比GridLayout更灵活
// 但是加入组件需要借助GridBagConstraints
import java.awt.Insets;
//获取窗口上下左右的大小
import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
// 进度条组件
import javax.swing.SwingUtilities;
/**
 * 读取并写入数据
 *
 */
public class Downloader extends JPanel implements Runnable{
	private static final int BUFFER_SIZE = 1000;//byte数组的大小
	protected URL downloadURL;//所下载资源的URL
	protected InputStream inputStream;//字节输入流的所有类的超类
	protected OutputStream outputStream;//字节输出流的所有类的超类
	protected byte[] buffer;//缓冲区byte型数组buffer中，tcp/ip socket通信时使用

	protected int fileSize;//文件的大小
	protected int bytesRead;//已经读取的字节数

	protected JLabel urlLabel;//放置URL的JLabel
	protected JLabel sizeLabel;//放置文件大小的JLabel
	protected JLabel completeLabel;//放置已经下载大小的JLabel
	protected JProgressBar progressBar;//进度条

	protected boolean stopped = false;//是否停止下载的标志
	protected boolean suspended = false;//线程是否挂起

	protected Thread thisThread;//当前线程
	public static ThreadGroup downloaderGroup = new ThreadGroup("Donwload Threads");//线程组

	public Downloader(URL url, FileOutputStream fos) throws IOException {
		downloadURL = url;
		outputStream = fos;
		bytesRead = 0;
		//URLConnection构造一个到指定 URL 的 URL 连接。
		URLConnection urlConnection = downloadURL.openConnection();
		fileSize = urlConnection.getContentLength();//文件长度

		if(fileSize == -1){
			throw new FileNotFoundException(url.toString());
		}
		//在创建 BufferedInputStream 时，会创建一个内部缓冲区数组。
		inputStream = new BufferedInputStream(urlConnection.getInputStream());
		buffer = new byte[BUFFER_SIZE];
		thisThread = new Thread(downloaderGroup, this);
		buildLayout();
	}

	private void buildLayout() {
		JLabel label;
		setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		//组件的显示区域大于它所请求的显示区域的大小时使用此字段。HORIZONTAL：在水平方向而不是垂直方向上调整组件大小。
		gbc.fill = GridBagConstraints.HORIZONTAL;
		//insets组件与其显示区域边缘之间间距的最小量
		gbc.insets = new Insets(5 ,10, 5, 10);
		//指定包含组件的显示区域开始边的"单元格"，其中行的第一个单元格为 gridx=0。
		gbc.gridx = 0;
		label = new JLabel("地址:",  JLabel.LEFT);
		add(label, gbc);

		label = new JLabel("进度:",  JLabel.LEFT);
		add(label, gbc);

		label = new JLabel("已经下载:",  JLabel.LEFT);
		add(label, gbc);

		gbc.gridx = 1;
		//gridwidth:指定组件显示区域的某一行中的单元格数。  REMAINDER:指定此组件是其行或列中的最后一个组件
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		//weightx:指定如何分布额外的水平空间。
		//如果得到的布局在水平方向上比需要填充的区域小，那么系统会将额外的空间按照其权重比例分布到每一列。
		//权重为零的列不会得到额外的空间。
		gbc.weightx = 1;
		urlLabel = new JLabel(downloadURL.toString());
		add(urlLabel, gbc);

		progressBar = new JProgressBar(0, fileSize);
		// 创建一个最小值为0，最大值为filzesize的进度条
		progressBar.setStringPainted(true);
		// which determines whether the progress bar should render a progress string.
		add(progressBar, gbc);

		gbc.gridwidth = 1;
		completeLabel = new JLabel(Integer.toString(bytesRead));
		add(completeLabel, gbc);

		gbc.gridx = 2;
		gbc.weightx = 0;
		//当组件小于其显示区域时使用此字段。
		//它可以确定在显示区域中放置组件的位置。
		gbc.anchor = GridBagConstraints.EAST;
		label = new JLabel("文件大小:", JLabel.LEFT);
		add(label, gbc);
		///指定包含组件的显示区域开始边的"单元格",其中行的第一个单元格为 gridx=0。
		gbc.gridx = 3;
		gbc.weightx = 1;
		sizeLabel = new JLabel(Integer.toString(fileSize));
		add(sizeLabel, gbc);
	}
	public void run() {
		performDownload();
	}
	/**
	 * 负责执行下载的方法。
	 */
	private void performDownload() {
		int byteCount;
		//刷新进度条和completeLabel：是AWT时间线程与下载线程同步
		Runnable progressBarUpdate = new Runnable(){
			public void run() {
				progressBar.setValue(bytesRead);
				completeLabel.setText(Integer.toString(bytesRead));
			}
		};
		while((bytesRead < fileSize) && (!isStopped())){
			//是否暂停
			try {
				//从输入流中读取一定数量的字节，并将其存储在缓冲区数组 buffer中
				//以整数形式返回实际读取的字节数。存储在缓冲区整数 byteCount中。
				byteCount = inputStream.read(buffer);
				if(byteCount == -1){
					setStopped(true);
					break;
				}else{
					outputStream.write(buffer, 0, byteCount);
					bytesRead += byteCount;
					//进度条的线程（创建多线程应用程序如果需要修改可视化组件，可以调用的SwingUtilities类的invokeLater()方法和invokeAndWait()方法）
					SwingUtilities.invokeLater(progressBarUpdate);
				}
			} catch (IOException e) {
				setStopped(true);
				JOptionPane.showMessageDialog(this,
						e.getMessage(),
						"I/O Error",
						JOptionPane.ERROR_MESSAGE);
				break;
			}
			//是否暂停
			synchronized(this){
				if(isSuspended()){
					try {
						//下载线程调用wait()方法后会隐式的放弃监控的所有权
						this.wait();
					} catch (InterruptedException e) {
						setStopped(true);
						break;
					}
					setSuspended(false);
				}
			}
			//测试当前线程是否已经中断。
			if(Thread.interrupted()){
				setStopped(true);
				break;
			}
		}
		try {
			//关闭流，断开与所下载文件的连接
			inputStream.close();
			outputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		//是否下载完了？
		if(bytesRead == fileSize){
			JOptionPane.showMessageDialog(null,
					"完成下载",
					"已下载完成！",
					JOptionPane.INFORMATION_MESSAGE);
			//System.exit(1);
		}
	}
	public synchronized void startDownload() {
		thisThread.start();
	}
	public synchronized void stopDownload() {
		thisThread.interrupt();
	}
	public synchronized void resumeDownloader() {
		//notify()和notifyAll()方法并不会让等待线程立即回复执行。
		//等待线程要回复执行，就必须先取得与线程同步的对象监控
		this.notify();
	}
	public synchronized void setStopped(boolean stopped) {
		this.stopped = stopped;
	}
	public synchronized boolean isStopped() {
		return stopped;
	}
	public synchronized void setSuspended(boolean suspended) {
		this.suspended = suspended;
	}
	public synchronized boolean isSuspended() {
		return suspended;
	}
	public static void cancelAllAndWait(){
		//activeCount()返回线程组中活动线程的个数
		int count = downloaderGroup.activeCount();
		Thread[] threads = new Thread[count];
		//enumerate()将每个活动的线程的引用存入threads数组中，并返回threads数组中的线程数
		count = downloaderGroup.enumerate(threads);

		downloaderGroup.interrupt();
		for(int i = 0; i < count; i++){
			try {
				threads[i].join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	/*
	 * 注意：Thread类中的suspended()、resume()、stop()方法都是已经过时的。
	 * 这里也没有调用。而是手动实现对应的功能。
	 */
}