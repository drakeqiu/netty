package com.jarvis.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.CountDownLatch;

/**
 * Created by jarvis on 19/02/2017.
 */
public class PlainNio2EchoServer {
    public void server(int port) throws IOException {
        System.out.println("Listening for connections on port " + port);
        final AsynchronousServerSocketChannel serverChannel = AsynchronousServerSocketChannel.open();
        InetSocketAddress address = new InetSocketAddress(port);
        serverChannel.bind(address);
        final CountDownLatch latch = new CountDownLatch(1);
        serverChannel.accept(null, new CompletionHandler<AsynchronousSocketChannel, Object>() {
            /**
             * Invoked when an operation has completed.
             *
             * @param channel     The result of the I/O operation.
             * @param attachment
             */
            @Override
            public void completed(AsynchronousSocketChannel channel, Object attachment) {
                serverChannel.accept(null, this);
                ByteBuffer buffer = ByteBuffer.allocate(100);
                channel.read(buffer, buffer, new EchoCompletionHandler(channel));
            }

            /**
             * Invoked when an operation fails.
             *
             * @param exc        The exception to indicate why the I/O operation failed
             * @param attachment
             */
            @Override
            public void failed(Throwable exc, Object attachment) {
                try {
                    serverChannel.close();
                } catch (IOException e) {
                    // ignore the case
                } finally {
                    latch.countDown();
                }
            }
        });

        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

    }

    private final class EchoCompletionHandler implements CompletionHandler<Integer, ByteBuffer> {

        private final  AsynchronousSocketChannel channel;

        public EchoCompletionHandler(AsynchronousSocketChannel channel) {
            this.channel = channel;
        }

        /**
         * Invoked when an operation has completed.
         *
         * @param result     The result of the I/O operation.
         * @param buffer
         */
        @Override
        public void completed(Integer result, final ByteBuffer buffer) {
            buffer.flip();
            channel.write(buffer, buffer, new CompletionHandler<Integer, ByteBuffer>() {
                @Override
                public void completed(Integer result, ByteBuffer attachment) {
                    if (buffer.hasRemaining()) {
                        channel.write(buffer, buffer, this);
                    } else {
                        buffer.compact();
                        channel.read(buffer, buffer, EchoCompletionHandler.this);
                    }
                }

                @Override
                public void failed(Throwable exc, ByteBuffer attachment) {
                    try {
                        channel.close();
                    } catch (IOException e) {
                        // ignore on close
                    }
                }
            });
        }

        /**
         * Invoked when an operation fails.
         *
         * @param exc        The exception to indicate why the I/O operation failed
         * @param attachment
         */
        @Override
        public void failed(Throwable exc, ByteBuffer attachment) {

        }
    }
}

