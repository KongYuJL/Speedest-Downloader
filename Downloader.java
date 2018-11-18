import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
// ���ֲ��ֱ�GridLayout�����
// ���Ǽ��������Ҫ����GridBagConstraints
import java.awt.Insets;
//��ȡ�����������ҵĴ�С
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
// ���������
import javax.swing.SwingUtilities;
/**
 * ��ȡ��д������
 *
 */
public class Downloader extends JPanel implements Runnable{
	private static final int BUFFER_SIZE = 1000;//byte����Ĵ�С
	protected URL downloadURL;//��������Դ��URL
	protected InputStream inputStream;//�ֽ���������������ĳ���
	protected OutputStream outputStream;//�ֽ��������������ĳ���
	protected byte[] buffer;//������byte������buffer�У�tcp/ip socketͨ��ʱʹ��

	protected int fileSize;//�ļ��Ĵ�С
	protected int bytesRead;//�Ѿ���ȡ���ֽ���

	protected JLabel urlLabel;//����URL��JLabel
	protected JLabel sizeLabel;//�����ļ���С��JLabel
	protected JLabel completeLabel;//�����Ѿ����ش�С��JLabel
	protected JProgressBar progressBar;//������

	protected boolean stopped = false;//�Ƿ�ֹͣ���صı�־
	protected boolean suspended = false;//�߳��Ƿ����

	protected Thread thisThread;//��ǰ�߳�
	public static ThreadGroup downloaderGroup = new ThreadGroup("Donwload Threads");//�߳���

	public Downloader(URL url, FileOutputStream fos) throws IOException {
		downloadURL = url;
		outputStream = fos;
		bytesRead = 0;
		//URLConnection����һ����ָ�� URL �� URL ���ӡ�
		URLConnection urlConnection = downloadURL.openConnection();
		fileSize = urlConnection.getContentLength();//�ļ�����

		if(fileSize == -1){
			throw new FileNotFoundException(url.toString());
		}
		//�ڴ��� BufferedInputStream ʱ���ᴴ��һ���ڲ����������顣
		inputStream = new BufferedInputStream(urlConnection.getInputStream());
		buffer = new byte[BUFFER_SIZE];
		thisThread = new Thread(downloaderGroup, this);
		buildLayout();
	}

	private void buildLayout() {
		JLabel label;
		setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		//�������ʾ������������������ʾ����Ĵ�Сʱʹ�ô��ֶΡ�HORIZONTAL����ˮƽ��������Ǵ�ֱ�����ϵ��������С��
		gbc.fill = GridBagConstraints.HORIZONTAL;
		//insets���������ʾ�����Ե֮�������С��
		gbc.insets = new Insets(5 ,10, 5, 10);
		//ָ�������������ʾ����ʼ�ߵ�"��Ԫ��"�������еĵ�һ����Ԫ��Ϊ gridx=0��
		gbc.gridx = 0;
		label = new JLabel("��ַ:",  JLabel.LEFT);
		add(label, gbc);

		label = new JLabel("����:",  JLabel.LEFT);
		add(label, gbc);

		label = new JLabel("�Ѿ�����:",  JLabel.LEFT);
		add(label, gbc);

		gbc.gridx = 1;
		//gridwidth:ָ�������ʾ�����ĳһ���еĵ�Ԫ������  REMAINDER:ָ������������л����е����һ�����
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		//weightx:ָ����ηֲ������ˮƽ�ռ䡣
		//����õ��Ĳ�����ˮƽ�����ϱ���Ҫ��������С����ôϵͳ�Ὣ����Ŀռ䰴����Ȩ�ر����ֲ���ÿһ�С�
		//Ȩ��Ϊ����в���õ�����Ŀռ䡣
		gbc.weightx = 1;
		urlLabel = new JLabel(downloadURL.toString());
		add(urlLabel, gbc);

		progressBar = new JProgressBar(0, fileSize);
		// ����һ����СֵΪ0�����ֵΪfilzesize�Ľ�����
		progressBar.setStringPainted(true);
		// which determines whether the progress bar should render a progress string.
		add(progressBar, gbc);

		gbc.gridwidth = 1;
		completeLabel = new JLabel(Integer.toString(bytesRead));
		add(completeLabel, gbc);

		gbc.gridx = 2;
		gbc.weightx = 0;
		//�����С������ʾ����ʱʹ�ô��ֶΡ�
		//������ȷ������ʾ�����з��������λ�á�
		gbc.anchor = GridBagConstraints.EAST;
		label = new JLabel("�ļ���С:", JLabel.LEFT);
		add(label, gbc);
		///ָ�������������ʾ����ʼ�ߵ�"��Ԫ��",�����еĵ�һ����Ԫ��Ϊ gridx=0��
		gbc.gridx = 3;
		gbc.weightx = 1;
		sizeLabel = new JLabel(Integer.toString(fileSize));
		add(sizeLabel, gbc);
	}
	public void run() {
		performDownload();
	}
	/**
	 * ����ִ�����صķ�����
	 */
	private void performDownload() {
		int byteCount;
		//ˢ�½�������completeLabel����AWTʱ���߳��������߳�ͬ��
		Runnable progressBarUpdate = new Runnable(){
			public void run() {
				progressBar.setValue(bytesRead);
				completeLabel.setText(Integer.toString(bytesRead));
			}
		};
		while((bytesRead < fileSize) && (!isStopped())){
			//�Ƿ���ͣ
			try {
				//���������ж�ȡһ���������ֽڣ�������洢�ڻ��������� buffer��
				//��������ʽ����ʵ�ʶ�ȡ���ֽ������洢�ڻ��������� byteCount�С�
				byteCount = inputStream.read(buffer);
				if(byteCount == -1){
					setStopped(true);
					break;
				}else{
					outputStream.write(buffer, 0, byteCount);
					bytesRead += byteCount;
					//���������̣߳��������߳�Ӧ�ó��������Ҫ�޸Ŀ��ӻ���������Ե��õ�SwingUtilities���invokeLater()������invokeAndWait()������
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
			//�Ƿ���ͣ
			synchronized(this){
				if(isSuspended()){
					try {
						//�����̵߳���wait()���������ʽ�ķ�����ص�����Ȩ
						this.wait();
					} catch (InterruptedException e) {
						setStopped(true);
						break;
					}
					setSuspended(false);
				}
			}
			//���Ե�ǰ�߳��Ƿ��Ѿ��жϡ�
			if(Thread.interrupted()){
				setStopped(true);
				break;
			}
		}
		try {
			//�ر������Ͽ����������ļ�������
			inputStream.close();
			outputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		//�Ƿ��������ˣ�
		if(bytesRead == fileSize){
			JOptionPane.showMessageDialog(null,
					"�������",
					"��������ɣ�",
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
		//notify()��notifyAll()�����������õȴ��߳������ظ�ִ�С�
		//�ȴ��߳�Ҫ�ظ�ִ�У��ͱ�����ȡ�����߳�ͬ���Ķ�����
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
		//activeCount()�����߳����л�̵߳ĸ���
		int count = downloaderGroup.activeCount();
		Thread[] threads = new Thread[count];
		//enumerate()��ÿ������̵߳����ô���threads�����У�������threads�����е��߳���
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
	 * ע�⣺Thread���е�suspended()��resume()��stop()���������Ѿ���ʱ�ġ�
	 * ����Ҳû�е��á������ֶ�ʵ�ֶ�Ӧ�Ĺ��ܡ�
	 */
}
