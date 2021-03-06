import javax.swing.*;
import java.io.File;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * 多线程下载  断点续传
 *
 */
public class MuchThreadDown {

    private String path = null;
    private String targetFilePath="/";  // 下载文件存放目录
    private int threadCount = 5;    // 线程数量
    private int completeThread = 0; // 完成下载的线程数量
    protected JButton[] buttons= new JButton[5];
    protected Downloader downloader = null;
    protected int total;//已经读取的字节数
    Thread[] threads = new Thread[threadCount+1];

    /**
     * @param path 文件 url
     * @param targetFilePath 保存下载文件的目录
     * @param threadCount 开启线程的数量，默认为 3
     */
    public MuchThreadDown(String path, String targetFilePath, int threadCount, JButton[] bs, Downloader downloader) {
        this.path = path;
        this.targetFilePath = targetFilePath;
        this.threadCount = threadCount;
        buttons = bs;
        this.downloader = downloader;
    }

    /**
     * 下载文件
     */
    public void download() throws Exception{
        // 连接资源
        URL url = new URL(path);

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(10000);

        int code = connection.getResponseCode();
        if(code == 200){
            //  获取资源大小
            int connectionLength = connection.getContentLength();
            System.out.println(connectionLength);
            //在本地创建一个与资源同样大小的文件来占位
            RandomAccessFile randomAccessFile = new RandomAccessFile(new File(targetFilePath, getFileName(url)), "rw");
            randomAccessFile.setLength(connectionLength);
            randomAccessFile.close();
            /*
             * 将下载任务分配给每个线程
             */
            int blockSize = connectionLength/threadCount;// 计算每个线程理论上下载的数量
            for(int threadId = 0; threadId < threadCount; threadId++){// 为每个线程分配任务
                int startIndex = threadId * blockSize; // 线程开始下载的位置
                int endIndex = (threadId+1) * blockSize -1; // 线程结束下载的位置
                if(threadId == (threadCount - 1)){  // 如果是最后一个线程，剩下的文件全部交给这个线程
                    endIndex = connectionLength - 1;
                }

                new DownloadThread(threadId, startIndex, endIndex).start();// 开启线程下载

            }
            threads[threadCount] = new DownloadThread(threadCount, 0, 0);
        }

    }

    // 下载的线程
    private class DownloadThread extends Thread{

        private int threadId = 0;
        private int startIndex = 0;
        private int endIndex = 0;

        public DownloadThread(int threadId, int startIndex, int endIndex) {
            this.threadId = threadId;
            this.startIndex = startIndex;
            this.endIndex = endIndex;
        }

        @Override
        public void run() {
            int i;
            System.out.println("线程"+ threadId + "开始下载");
//            int total = 0;// 记录本次下载文件的大小
            Runnable progressBarUpdate = new Runnable(){
                public void run() {
                    downloader.getProgressBar().setValue(total);
                    downloader.getCompleteLabel().setText(Integer.toString(total));
                }
            };
            try {
                // 分段请求网络连接，分段保存到本地
                URL url = new URL(path);

                // 加载下载位置的文件
                File downThreadFile = new File(targetFilePath,"downThread_" + threadId+".dt");
                RandomAccessFile downThreadStream = null;
                if(downThreadFile.exists()){// 如果文件已存在
                    downThreadStream = new RandomAccessFile(downThreadFile,"rwd");
                    String startIndex_str = downThreadStream.readLine();
                    if(null == startIndex_str || "".equals(startIndex_str)){
                        this.startIndex = startIndex;
                    }else{
                        this.startIndex = Integer.parseInt(startIndex_str)-1;// 下载起点
                    }
                }else{
                    downThreadStream = new RandomAccessFile(downThreadFile,"rwd");
                }

                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(10000);

                // 设置分段下载的头信息  Range:分段数据请求使用 格式: Range bytes=0-1024 or bytes:0-1024
                connection.setRequestProperty("Range", "bytes="+ startIndex + "-" + endIndex);

                System.out.println("线程_"+threadId + "的下载起点是 " + startIndex + "  下载终点是 " + endIndex);

                if(connection.getResponseCode() == 206){// 200 请求全部资源成功 206 部分资源请求成功
                    InputStream inputStream = connection.getInputStream();// 获取流
                    RandomAccessFile randomAccessFile = new RandomAccessFile(new File(targetFilePath, getFileName(url)), "rw");//获取前面已创建的文件
                    randomAccessFile.seek(startIndex);//文件写入的开始位置


                    /*
                     * 将网络流中的文件写入本地
                     */
                    byte[] buffer = new byte[1024];
                    int length = -1;
                    while((length = inputStream.read(buffer)) > 0){
                        randomAccessFile.write(buffer, 0, length);
                        total += length;
                        System.out.println("total: " + total);
                        SwingUtilities.invokeLater(progressBarUpdate);
                        /*
                         * 将当前下载到的位置保存到文件中
                         */
                        downThreadStream.seek(0);
                        downThreadStream.write((startIndex + total + "").getBytes("UTF-8"));

                        // 是否暂停
                        synchronized(this){
                            if(downloader.isSuspended()){
                                try {
                                    //下载线程调用wait()方法后会隐式的放弃监控的所有权
                                    this.wait();
                                } catch (Exception e) {
                                    downloader.setStopped(true);
                                    break;
                                }
                            }
                        }
                    }

                    downThreadStream.close();
                    inputStream.close();
                    randomAccessFile.close();
                    cleanTemp(downThreadFile);
                    System.out.println("线程"+ threadId + "下载完毕");
                    completeThread++;
                    if (completeThread == threadCount) {
                        JOptionPane.showMessageDialog(null, "下载完成！");
                        for(i=0;i<buttons.length;i++)
                            buttons[i].setEnabled(false);
                    }
                }else{
                    JOptionPane.showMessageDialog(null, "响应码是" +connection.getResponseCode() + ". 服务器不支持多线程下载",
                            "无法下载！", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    // 删除线程产生的临时文件
    private synchronized void cleanTemp(File file){
        file.delete();
    }

    // 恢复下载
    public synchronized void resumeDownloader() {
        //notify()和notifyAll()方法并不会让等待线程立即回复执行。
        //等待线程要回复执行，就必须先取得与线程同步的对象监控
        Thread notifyer = new Thread("notifyer");
        notifyer.start();
        synchronized (notifyer) {
            notifyer.notifyAll();
        }
    }

    // 获取下载文件的名称
    private String getFileName(URL url){
        String filename = url.getFile();
        return filename.substring(filename.lastIndexOf("/")+1);
    }
}