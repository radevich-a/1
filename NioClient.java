import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;


public class NioClient implements Runnable{
    private final SocketChannel client;
    private Selector selector;
    private ByteBuffer buf = ByteBuffer.allocate(256);



    public NioClient() throws IOException {
        this.client = SocketChannel.open();
        this.client.configureBlocking(false);
        this.selector = Selector.open();
        this.client.register(selector, SelectionKey.OP_CONNECT);
        this.client.connect(new InetSocketAddress("localhost", 8189));

    }

    @Override
    public void run() {
        BlockingQueue<String> queue = new ArrayBlockingQueue<>(2);
        new Thread(() -> {
            Scanner scanner = new Scanner(System.in);
            while (true) {
                String msg = scanner.nextLine();
                if ("q".equals(msg)){

                }
                try {
                    queue.put(msg);
                } catch (InterruptedException e){
                    e.printStackTrace();
                }
                SelectionKey key = client.keyFor(selector);
                key.interestOps(SelectionKey.OP_WRITE);
                selector.wakeup();
            }
        }).start();

        while (true) {
            try {
                selector.select();
                for (SelectionKey selectionKey : selector.selectedKeys()) {
                    if (selectionKey.isConnectable()) {
                        client.finishConnect();
                        selectionKey.interestOps(SelectionKey.OP_WRITE);
                    }
                    if (selectionKey.isWritable()) {
                    String msg = queue.poll();
                    if (msg != null){
                        client.write(ByteBuffer.wrap(msg.getBytes()));
                    }
                    selectionKey.interestOps(SelectionKey.OP_READ);
                    }
                    if (selectionKey.isReadable()) {
                        StringBuilder sb = new StringBuilder();

                        buf.clear();
                        int read = 0;
                        while ((read = client.read(buf)) > 0) {
                            buf.flip();
                            byte[] bytes = new byte[buf.limit()];
                            buf.get(bytes);
                            sb.append(new String(bytes));
                            buf.clear();
                        }
                        String message;
                        message = sb.toString();
                        System.out.println(message);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public static void main (String[]args) throws Exception {
        new Thread(new NioClient()).start();
    }
}


