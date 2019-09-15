import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Scanner;

public class Dispositivo implements Runnable {

	private static Integer bpm = 100;
	private static Integer ox = 98;
	private static Double temp = 36.5;
	private static String pos = "N";
	private static String device = null;
	private static PrintWriter writer = null;
	private static BufferedReader bis = null;
	private static Socket cliente = null;
	private static String localhost = "127.0.0.1";

	@Override
	public void run() {
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
		try {
			while (true) {
				Thread.sleep(1000);
				if (device != null && cliente == null) {
					cliente = new Socket(localhost, 8085);
					writer = new PrintWriter(cliente.getOutputStream());
					bis = new BufferedReader(new InputStreamReader(cliente.getInputStream()));
				}
				if (device != null && !device.isEmpty() && cliente != null) {
					System.out.println(sdf.format(Calendar.getInstance().getTime()) + " - " + "bpm = " + bpm + "; ox = "
							+ ox + "; temp = " + temp + "; pos = " + pos + "; device = " + device);
					writer.println(bpm + ";" + ox + ";" + pos + ";" + temp + ";" + device);
					writer.flush();
					while (!"next".equals(bis.readLine())) {
						Thread.sleep(100);
					}
				}
				if ("X".equals(device)) {
					writer.close();
					cliente.close();
					break;
				}
			}
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				cliente.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static void main(String[] args) {

		Thread input = new Thread(new InputReceiver());
		Thread device = new Thread(new Dispositivo());
		input.start();
		device.start();
	}

	private static class InputReceiver implements Runnable {
		@Override
		public void run() {
			Scanner scanner = new Scanner(System.in);
			while (scanner.hasNext()) {
				String msg = scanner.nextLine();
				System.out.println(msg);
				if (msg != null) {
					String[] tokens = msg.split("=");
					if (tokens != null) {
						String param = tokens[0];
						String val = tokens[1];
						if ("bpm".equals(param)) {
							bpm = Integer.valueOf(val);
						} else if ("ox".equals(param)) {
							ox = Integer.valueOf(val);
						} else if ("temp".equals(param)) {
							temp = Double.valueOf(val);
						} else if ("pos".equals(param)) {
							pos = val;
						} else if ("device".equals(param)) {
							device = val;
							if ("X".equals(val)) {
								break;
							}
						}
					}
				}
			}
			scanner.close();
		}
	}

}
