import java.io.FileOutputStream;
import java.net.InetSocketAddress;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;
import java.nio.ByteBuffer;

public class Client {
    public static void zerocopy(long fileSize,SocketChannel clientChannel,FileChannel fileChannel){


        try{

            long chuckSize = 1024 * 1024;
            ByteBuffer buffer = ByteBuffer.allocate((int) chuckSize);
            long bytesRead = 0;
            long start = System.currentTimeMillis();
            
            /* 
            while (bytesRead < fileSize){
               buffer.clear();
               int bytesReadThisTime = clientChannel.read(buffer); 
                
               buffer.flip();
               fileChannel.write(buffer);
               //System.out.println(bytesReadThisTime);
               bytesRead += bytesReadThisTime;

             }
            */ 

            
            long size = 0;  
            while (size < fileSize){
                long remain = fileSize - size; 
                long byterecive = Math.min(remain, chuckSize);
                long receive = fileChannel.transferFrom(clientChannel, size, byterecive);
                size += receive;

            }
            
           
            long end = System.currentTimeMillis();
            long time = end-start;
            System.out.println(time + "Millisec");
            
        }catch(Exception e){
            System.out.println(e);
        }
    }
    public static void main(String[] args) {
        try {
            InetSocketAddress serverAddress = new InetSocketAddress("localhost", 8899);
            SocketChannel clientChannel = SocketChannel.open(serverAddress);

            String filePath =  args[0];

            // Send the file path to the server
            ByteBuffer pathBuffer = ByteBuffer.wrap(filePath.getBytes());
            clientChannel.write(pathBuffer);

            // Receive the file size from the server
            ByteBuffer sizeBuffer = ByteBuffer.allocate(8);
            clientChannel.read(sizeBuffer);
            sizeBuffer.flip();
            long fileSize = sizeBuffer.getLong();
            System.out.println("Received file size: " + fileSize);

            FileOutputStream fileOutputStream = new FileOutputStream(args[1]);
            FileChannel fileChannel = fileOutputStream.getChannel();

            // Transfer the file using zero-copy
            zerocopy(fileSize,clientChannel,fileChannel);
            // Close the file channels and output stream
            fileChannel.close();
            fileOutputStream.close();
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
