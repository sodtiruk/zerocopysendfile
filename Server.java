import java.io.FileInputStream;
import java.net.InetSocketAddress;
import java.nio.channels.FileChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.ByteBuffer;

class Server {
    public static void zerocopy(long fileSize, FileChannel fileChannel, SocketChannel socketchannel) {
        try {
            long chunkSize = 1024 * 1024; // 1 MB
            long bytesTransferred = 0;

            while (bytesTransferred < fileSize) {
                long remainingBytes = fileSize - bytesTransferred;
                long bytesToSend = Math.min(remainingBytes, chunkSize);

                //System.out.println(bytesToSend);
                long transferred = fileChannel.transferTo(bytesTransferred, bytesToSend, socketchannel);

                bytesTransferred += transferred;
            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public static void main(String args[]) {
        try {
            ServerSocketChannel serversocketchannel = ServerSocketChannel.open();
            serversocketchannel.socket().bind(new InetSocketAddress("localhost", 8899));

            while (true) {
                System.out.println("Waiting for a client...");
                SocketChannel socketchannel = serversocketchannel.accept();
                System.out.println(socketchannel.socket().getInetAddress() + " client connected");

                ByteBuffer pathBuffer = ByteBuffer.allocate(1024);
                int bytesRead = socketchannel.read(pathBuffer);
                pathBuffer.flip();
                String filePath = new String(pathBuffer.array(), pathBuffer.position(), pathBuffer.remaining());
                System.out.println("Received file path: " + filePath);

                // Open the file and create a FileChannel
                //File file = new File(filePath);
                //FileChannel fileChannel = FileChannel.open(file.toPath());
                System.out.println(filePath);
                FileInputStream file = new FileInputStream(filePath);
                FileChannel fileChannel = file.getChannel();


                // Send the file size to the client
                long fileSize = fileChannel.size();
                ByteBuffer sizeBuffer = ByteBuffer.allocate(8);
                sizeBuffer.putLong(fileSize);
                sizeBuffer.flip();
                socketchannel.write(sizeBuffer);               


                zerocopy(fileSize, fileChannel, socketchannel);
                //fileChannel.close();
                //serversocketchannel.close();

            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
