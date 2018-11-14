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
 * �������ı���������URL��������Ӧ��DownloadManager���ʵ��
 */
public class DownloadFiles extends JPanel{
	protected JPanel listPanel;//���ø������ص����
	protected GridBagConstraints constraints;//ָ��ʹ�� GridBagLayout�಼�õ������Լ����
	protected final String filepath = "G:/";//�����ص��ļ������·��
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
			
			File file=new File(filepath+url.substring(index + 1));
			if(file.exists()){
				JOptionPane.showMessageDialog(this, "���ļ��Ѿ�����",
						"�޷�����", JOptionPane.ERROR_MESSAGE);
				return false;
			}
			FileOutputStream fos = new FileOutputStream(file);
			
			//BufferedOutputStream bos = new BufferedOutputStream(fos);
			DownloadManager dm = new DownloadManager(downloadURL, fos);
			listPanel.add(dm, constraints);
			return true;
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, "����Դ�޷����ء�",
					"�޷����أ�", JOptionPane.ERROR_MESSAGE);
		}
		return false;
	}
}
