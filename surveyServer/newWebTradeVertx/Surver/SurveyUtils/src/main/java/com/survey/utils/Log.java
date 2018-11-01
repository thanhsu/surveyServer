package com.survey.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;
import com.systekit.common.sys.sysTrace;
import io.vertx.core.json.JsonObject;

public class Log {
	private static Vector linkedList = new Vector();
	private static String path = "./logs";
	private static sysTrace mvTrace;
	private static final SimpleDateFormat mvSimpleDateFormat = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss:SSS");

	public static final int ERROR_LOG = 0;
	public static final int ACCESS_LOG = 1;
	public static final int TRANSACTION_LOG = 2;
	public static final int URL_SEND_LOG = 3;
	public static final int URL_RECEIVED_LOG = 4;
	public static final int DEBUG_LOG = 5;
	public static final int ALERT_LOG = 6;

	public static boolean mvDebugOn = false;

	private static JsonObject mvConfig;

	// set Log Path
	public static void setPath(String Path) {
		path = Path;
	}

	public static void print(String pMessage) {
		print(pMessage, ACCESS_LOG);
	}

	public static void print(String pMessage, int pLogType) {
		if (pLogType == DEBUG_LOG && !mvDebugOn) {
			return;
		}
		if (pMessage != null) {
			linkedList.add(new LogNode(Thread.currentThread().getName(), pMessage, pLogType, new Date()));
			Object node = linkedList.remove(0);
			logActionPerform(node);
		}
	}

	public static void print(Throwable pException, int pLogType) {
		if (pLogType == DEBUG_LOG && !mvDebugOn) {
			return;
		}
		linkedList.add(new LogNode(Thread.currentThread().getName(), pException, pLogType, new Date()));
		Object node = linkedList.remove(0);
		logActionPerform(node);
	}

	public static void println(Throwable pException, int pLogType) {
		try {
			println(pException.toString(), pLogType);
		} catch (Exception e) {
			e.getMessage();
		}
	}

	public static void println(String pMessage, int pLogType) {
		print(pMessage, pLogType);
	}

	private static void logActionPerform(Object pNode) {
		if (pNode == null) {
			return;
		}
		try {

			LogNode logNode = (LogNode) pNode;
			int type = logNode.logType;
			String outputDir;
			String pathname = path;

			if (mvTrace == null) {
				setSysTrace();
			}

			int lvLogLevel;
			boolean lvIsDefaultValue = false;

			switch (type) {
			case ERROR_LOG:
				lvLogLevel = sysTrace.TRACE_LEVEL_ERROR;
				break;
			case ACCESS_LOG:
				lvLogLevel = sysTrace.TRACE_LEVEL_INFO;
				break;
			case TRANSACTION_LOG:
				lvLogLevel = sysTrace.TRACE_LEVEL_MESSAGE;
				break;
			case URL_SEND_LOG:
				lvLogLevel = sysTrace.TRACE_LEVEL_NETWORK;
				break;
			case URL_RECEIVED_LOG:
				lvLogLevel = sysTrace.TRACE_LEVEL_NETWORK;
				break;
			case DEBUG_LOG:
				lvLogLevel = sysTrace.TRACE_LEVEL_DEBUG;
				break;
			case ALERT_LOG:
				lvLogLevel = sysTrace.TRACE_LEVEL_WARNING;
				break;
			default:
				mvTrace.trace("", logNode.mvThreadName, "Incorrect Log Type - [" + String.valueOf(type) + "]",
						sysTrace.TRACE_LEVEL_WARNING);
				lvLogLevel = sysTrace.TRACE_LEVEL_WARNING;
				break;
			}
			// write message if it is a message
			if (logNode.data instanceof String) {
				// out.write(logNode.data.toString().getBytes());
				try {
					mvTrace.trace("", logNode.mvThreadName, logNode.data.toString(), lvLogLevel);

				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				Throwable ex = (Throwable) logNode.data;

				if (lvLogLevel == sysTrace.TRACE_LEVEL_WARNING)
					mvTrace.trace("", logNode.mvThreadName, ex, lvLogLevel);
				else
					mvTrace.trace("", logNode.mvThreadName, ex, sysTrace.TRACE_LEVEL_ERROR);
			}

		} catch (Exception ioex) {
			ioex.printStackTrace();
		}
	}

	public static void setSysTrace() {
		if (Log.mvConfig == null) {
			return;
		}
		try {
			mvTrace = new sysTrace();

			// get Config
			String lvDayRetain = Log.mvConfig.getString("log.DayRetain");
			String lyFileNum = Log.mvConfig.getString("log.FileNum");
			String lvFileMaxSize = Log.mvConfig.getString("log.FileMaxSize");
			String lvSMTPRecipients = Log.mvConfig.getString("log.SMTPRecipients");
			String lvSMTPMessageInterval = Log.mvConfig.getString("log.SMTPMessageInterval");
			String lvSMTPHost = Log.mvConfig.getString("log.SMTPHost");
			String lvSMTPUser = Log.mvConfig.getString("log.SMTPUser");
			String lvSMTPPassword = Log.mvConfig.getString("log.SMTPPassword");
			String lvDir = Log.mvConfig.getString("log.dir");
			boolean lvSystemSetErr = Log.mvConfig.getString("log.SystemSetErr").equals("false") ? false : true;
			sysTrace.svApplicationName = Log.mvConfig.getString("log.FileName");

			// Set log path : main path + Services name
			sysTrace.setLogPath(lvDir);
			Log.path = lvDir;
			sysTrace.setLog(Integer.parseInt(lvDayRetain), Integer.parseInt(lyFileNum), Long.parseLong(lvFileMaxSize));
			File dir = new File(Log.path);
			if (!dir.exists()) {
				if (!dir.exists()) {
					throw new IOException("Cannot create directory: " + dir.toString());
				}
			}

			mvTrace.setTraceLevel(Integer.parseInt(Log.mvConfig.getString("log.Level")));
			mvTrace.initStatic(true, true, false);
			mvTrace.setsvTraceThreadPriority(Integer.parseInt(Log.mvConfig.getString("log.ThreadPriority")));
			// TODO setup send mail
			// if (lvSMTPRecipients != null && !lvSMTPRecipients.equalsIgnoreCase("")) {
			// SendMail sm = new SendMail(lvSMTPHost, lvSMTPUser, lvSMTPPassword);
			// mvTrace.setMail("", lvSMTPRecipients.split(","),
			// Integer.parseInt(lvSMTPMessageInterval));
			// } else {
			// mvTrace.setMail(null, null, Integer.parseInt(lvSMTPMessageInterval));
			// }

			try {

				// Vector lvFilterVector = new Vector();
				String lvEmailContentFilterList = Log.mvConfig.getString("log.EmailContentFilterList");

				java.util.ArrayList lvEmailContentFilterArrayList = new java.util.ArrayList();

				String[] lvEmailContentFilterArray = lvEmailContentFilterList.split(",");

				for (int i = 0; i < lvEmailContentFilterArray.length; i++) {
					String lvEmailContentFilter = (String) lvEmailContentFilterArray[i];

					lvEmailContentFilterArrayList.add(lvEmailContentFilter.trim());
				}

				String lvEmailSubject = Log.mvConfig.getString("log.EmailSubject");
				String lvErrorNotifyicationCommand = Log.mvConfig.getString("log.ErrorNotificationCommand");

				// BEGIN - Task #: TTL-HK-WLCW-00969. - Walter Lau 20100202 Email Sender
				String lvEmailSender = Log.mvConfig.getString("log.EmailSender");
				// END - Task #: TTL-HK-WLCW-00969. - Walter Lau 20100202 Email Sender

				mvTrace.setEmailSubject(lvEmailSubject);
				// mvTrace.setEmailContentFilter(lvEmailContentFilterArrayList);
				mvTrace.setErrorNotificationCommand(lvErrorNotifyicationCommand);
				// BEGIN - Task #: TTL-HK-WLCW-00969. - Walter Lau 20100202 Email Sender
				mvTrace.setEmailSender(lvEmailSender);
				// END - Task #: TTL-HK-WLCW-00969. - Walter Lau 20100202 Email Sender
			} catch (Exception e) {
				// e.printStackTrace();
			}

			if (System.getProperty("LogException") == null
					|| System.getProperty("LogException").equalsIgnoreCase("Y")) {
				try {
					java.text.SimpleDateFormat lvDateFormat = new java.text.SimpleDateFormat("yyyyMMdd");
					String lvDate = lvDateFormat.format(new Date());
					if (lvSystemSetErr)
						System.setErr(new ITradePrintStream(
								new FileOutputStream(lvDir + "/ITrade-" + lvDate + "-EXCEPTION.log", true), true));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		} catch (Exception pException) {
			pException.printStackTrace();
		}
	}

	public static JsonObject getMvConfig() {
		return mvConfig;
	}

	public static void setMvConfig(JsonObject mvConfig) {
		Log.mvConfig = mvConfig;
	}
}

/**
 * The ITradePrintStream class defined methods that are print stream about
 * ITrade.
 * 
 * @author
 */
class ITradePrintStream extends PrintStream {
	/**
	 * This method print OutputStream.
	 * 
	 * @param pOutputStream
	 *            The output byte stream.
	 */
	public ITradePrintStream(OutputStream pOutputStream) {
		super(pOutputStream);
	}

	/**
	 * This method print OutputStream,and auto flush.
	 * 
	 * @param pOutputStream
	 *            The output byte stream.
	 * @param pAutoFlush
	 *            is auto flush or not.
	 */
	public ITradePrintStream(OutputStream pOutputStream, boolean pAutoFlush) {
		super(pOutputStream, pAutoFlush);
	}

	/**
	 * This method to print specified object.
	 * 
	 * @param pObject
	 *            the object to be print.
	 */
	public void println(Object pObject) {
		if (pObject instanceof Throwable) {
			super.println("[TIME] " + new Date());
		}
		super.println(pObject);
	}
}

/**
 * This class will store data for a linked list node
 */

class LogNode {
	public String mvThreadName;
	public Object data;
	public int logType;
	public Date logTime;

	/**
	 * Constructor for LogNode class.
	 * 
	 * @param pThreadName
	 *            The thread name.
	 * @param pObject
	 *            An Object.
	 * @param pType
	 *            Log type.
	 * @param pTime
	 *            The log date.
	 */
	LogNode(String pThreadName, Object pObject, int pType, Date pTime) {
		mvThreadName = pThreadName;
		data = pObject;
		logType = pType;
		logTime = pTime;
	}
}
