import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

public class NioServer implements Runnable{
    private final ServerSocketChannel serverSocketChannel;
    private  Selector selector;
    private final ByteBuffer buf = ByteBuffer.allocate(256);
    private int acceptedClientIndex = 1;
    private final ByteBuffer welcomeBuf = ByteBuffer.wrap("Добро пожаловать в чат!\n".getBytes());


    public NioServer() throws IOException {
        this.serverSocketChannel = ServerSocketChannel.open();
        this.serverSocketChannel.socket().bind(new InetSocketAddress(8189));
        this.serverSocketChannel.configureBlocking(false);

        this.selector = Selector.open();
        this.serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT, "Server");

    }

    @Override
    public void run() {
        try {
            System.out.println("Сервер запущен (Порт: 8189)");
            Iterator<SelectionKey> iter;
            SelectionKey key;
            while (this.serverSocketChannel.isOpen()){
                selector.select();
                iter = this.selector.selectedKeys().iterator();
                while (iter.hasNext()) {
                    key = iter.next();
                    iter.remove();
                    if (key.isAcceptable()) {
                        processAccept(key);
                    }
                    if (key.isReadable()) {
                        System.out.println("Readable");
                        handleRead(key);
                    }
                    if (key.isWritable()) {
                        System.out.println("Writable");
                    }
               }
            }

            } catch (IOException e){
                e.printStackTrace();
            }
        }

        private void processAccept(SelectionKey key) throws IOException{
            SocketChannel socketChannel = ((ServerSocketChannel) key
                    .channel())
                    .accept();
            String clientName = "Клиент #" + acceptedClientIndex;
            System.out.println("Подключился новый клиент "
                    + clientName
                    + socketChannel.getRemoteAddress());
            acceptedClientIndex++;

            socketChannel.configureBlocking(false);
            socketChannel.register(selector, SelectionKey.OP_READ, clientName);
            socketChannel.write(welcomeBuf);//*****
            welcomeBuf.rewind();
        }


    private void handleRead(SelectionKey key) throws IOException {
        SocketChannel ch = (SocketChannel) key.channel();
        StringBuilder sb = new StringBuilder();

        buf.clear();
        int read = 0;
        while ((read = ch.read(buf)) > 0) {
            buf.flip();
            byte[] bytes = new byte[buf.limit()];
            buf.get(bytes);
            sb.append(new String(bytes));
            buf.clear();
        }
        String msg;
        msg = key.attachment() + ": " + sb.toString();
        ch.write(ByteBuffer.wrap(sb.toString().getBytes()));

        System.out.println(msg);
    }


    public static void main(String[] args) throws IOException{
        new Thread(new NioServer()).start();

    }

}
