package j8583.example;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.text.ParseException;
import java.util.Date;
import java.util.Iterator;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.solab.iso8583.IsoMessage;
import com.solab.iso8583.IsoType;
import com.solab.iso8583.MessageFactory;
import com.solab.iso8583.parse.ConfigParser;

/**
 * This example uses java.nio instead of java.io to read and write the messages
 * to the clients.
 * 
 * @author Enrique Zamudio
 */
public class ChannelServer implements Runnable {

	private static final Log log = LogFactory.getLog(ChannelServer.class);
	private static MessageFactory mfact;
	private static ScheduledExecutorService threadPool = Executors
			.newScheduledThreadPool(5);

	private SocketChannel socket;
	private byte[] data;

	public ChannelServer(SocketChannel chan, byte[] buf) {
		socket = chan;
		data = buf;
	}

	public void run() {
		try {
			log
					.debug(String.format("Parsing incoming: '%s'", new String(
							data)));
			IsoMessage incoming = mfact.parseMessage(data, 12);
			// Create a response
			IsoMessage response = mfact.createResponse(incoming);
			response.setField(11, incoming.getField(11));
			response.setField(7, incoming.getField(7));
			response.setValue(38, System.currentTimeMillis() % 1000000,
					IsoType.NUMERIC, 6);
			response.setValue(39, 0, IsoType.NUMERIC, 2);
			response.setValue(61, String.format(
					"Dynamic data generated at %TT", new Date()),
					IsoType.LLLVAR, 0);
			log.debug(String.format("Sending response conf %s", response
					.getField(38)));
			socket.write(response.writeToBuffer(2));
		} catch (ParseException ex) {
			log.error(String.format("Parsing request %s", new String(data)));
		} catch (IOException ex) {
			log.error("Writing response", ex);
		}
	}

	public static void main(String[] args) throws Exception {
		mfact = ConfigParser
				.createFromClasspathConfig("j8583/example/config.xml");
		log.info("Setting up server socket...");
		ServerSocketChannel server = ServerSocketChannel.open();
		Selector selector = Selector.open();
		server.socket().bind(new InetSocketAddress(9999));
		server.configureBlocking(false);
		server.register(selector, SelectionKey.OP_ACCEPT);
		log.info("Waiting for connections...");
		while (true) {
			selector.select();
			for (Iterator<SelectionKey> iter = selector.selectedKeys()
					.iterator(); iter.hasNext();) {
				SelectionKey skey = iter.next();
				if ((skey.readyOps() & SelectionKey.OP_ACCEPT) == SelectionKey.OP_ACCEPT) {
					// Accept a new connection
					SocketChannel socket = server.accept();
					log.info(String.format("New connection from %s:%d", socket
							.socket().getInetAddress(), socket.socket()
							.getPort()));
					socket.configureBlocking(false);
					DataReader reader = new DataReader();
					reader.reset();
					socket.register(selector, SelectionKey.OP_READ, reader);
				} else if ((skey.readyOps() & SelectionKey.OP_READ) == SelectionKey.OP_READ) {
					DataReader reader = (DataReader) skey.attachment();
					try {
						byte[] data = reader.read((SocketChannel) skey
								.channel());
						if (data != null) {
							reader.reset();
							// Set a job to parse the message and respond
							// Delay it a bit to pretend we're doing something
							// important
							threadPool.schedule(new ChannelServer(
									(SocketChannel) skey.channel(), data), 400,
									TimeUnit.MILLISECONDS);
						}
					} catch (IOException ex) {
						skey.channel().close();
					}
				}
				iter.remove();
			}
		}
	}

	/** Reads data from a channel. One instance per connection is used. */
	private static class DataReader {

		private int state = 0;
		private ByteBuffer buf = ByteBuffer.allocate(1024);

		/** Read from the channel until we have a full message. */
		protected byte[] read(SocketChannel chan) throws IOException {
			byte[] data = null;
			if (state == 0) {
				if (chan.read(buf) == -1) {
					log.error("EOF while reading header");
					chan.close();
				} else if (buf.remaining() == 0) {
					// read header
					state++;
					buf.clear();
					buf.limit(((buf.get(0) & 0xff) << 8) | (buf.get(1) & 0xff));
				}
			} else if (state == 1) {
				if (chan.read(buf) == -1) {
					log.error("EOF while reading data");
					chan.close();
				} else if (buf.remaining() == 0) {
					// process
					state++;
					buf.flip();
					data = new byte[buf.limit()];
					System.arraycopy(buf.array(), 0, data, 0, data.length);
				}
			}
			return data;
		}

		protected void reset() {
			state = 0;
			buf.clear();
			buf.limit(2);
		}

	}

}
