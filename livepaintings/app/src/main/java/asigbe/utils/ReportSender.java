package asigbe.utils;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Field;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

/**
 * This class catches exception and sends a mail if there was an error.
 * 
 * @author Delali Zigah
 */
public class ReportSender implements Thread.UncaughtExceptionHandler {

    private Thread.UncaughtExceptionHandler defaultUEH;

    private Context                         context = null;

    /**
     * Creates a default report sender on the given context.
     */
    public ReportSender(Context context) {
	this.defaultUEH = Thread.getDefaultUncaughtExceptionHandler();
	this.context = context;
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
	final Writer result = new StringWriter();
	final PrintWriter printWriter = new PrintWriter(result);
	e.printStackTrace(printWriter);
	String report = e.toString() + "\n\n";
	report += "--------- Device Information ---------\n\n";
	try {
	    try {
		Field manufacturer = android.os.Build.class
		        .getField("MANUFACTURER");
		report += "Manufacturer : " + manufacturer.get(new Object())
		        + "\n";
	    } catch (NoSuchFieldException nsme) {
	    }
	    report += "Brand : " + android.os.Build.BRAND + "\n";
	    report += "Model : " + android.os.Build.MODEL + "\n";
	    report += "Device : " + android.os.Build.DEVICE + "\n";
	    report += "Product : " + android.os.Build.PRODUCT + "\n";
	    report += "Version : " + android.os.Build.VERSION.RELEASE + "\n";
	    try {
		Field manufacturer = android.os.Build.VERSION.class
		        .getField("CODENAME");
		report += "Codename : " + manufacturer.get(new Object())
		        + "\n";
	    } catch (NoSuchFieldException nsme) {
	    }
	} catch (Exception exc) {
	}
	report += "-------------------------------\n\n";
	report += "--------- Software Information ---------\n\n";
	try {
	    PackageManager pm = this.context.getPackageManager();
	    PackageInfo pi;
	    pi = pm.getPackageInfo(this.context.getPackageName(), 0);
	    report += "Package name : " + pi.packageName + "\n";
	    report += "Version name : " + pi.versionName + "\n";
	} catch (Exception exc) {
	}
	report += "-------------------------------\n\n";
	report += "--------- Context ---------\n\n";
	try {
	    report += this.context.toString();
	} catch (Exception exc) {
	}
	report += "-------------------------------\n\n";
	report += "--------- Stack trace ---------\n\n";
	report += result;
	report += "-------------------------------\n\n";

	// If the exception was thrown in a background thread inside
	// AsyncTask, then the actual exception can be found with getCause
	report += "--------- Cause ---------\n\n";
	Throwable cause = e.getCause();
	if (cause != null) {
	    report += cause.toString() + "\n\n";
	    cause.printStackTrace(printWriter);
	    report += result;
	}
	report += "-------------------------------\n\n";

	try {
	    FileOutputStream trace = context.openFileOutput("stack.trace",
		    Context.MODE_PRIVATE);
	    trace.write(report.getBytes());
	    trace.close();
	} catch (IOException ioe) {
	    // ...
	}

	defaultUEH.uncaughtException(t, e);
    }

    /**
     * Sends a crash report if a crash previously occured.
     */
    public void sendCrashReportIfExist() {
	String trace = null;
	String line = null;
	try {
	    BufferedReader reader = new BufferedReader(new InputStreamReader(
		    this.context.openFileInput("stack.trace")));
	    while ((line = reader.readLine()) != null) {
		trace += line + "\n";
	    }
	} catch (FileNotFoundException fnfe) {
	    return;
	} catch (IOException ioe) {
	    return;
	}

	String packageName = "";
	try {
	    PackageManager pm = this.context.getPackageManager();
	    PackageInfo pi;
	    pi = pm.getPackageInfo(this.context.getPackageName(), 0);
	    packageName = pi.packageName;
	} catch (Exception exc) {
	}

	Intent sendIntent = new Intent(Intent.ACTION_SEND);
	String subject = packageName + " : error report";
	String body = trace + "\n\n";

	sendIntent.putExtra(Intent.EXTRA_EMAIL,
	        new String[] { "asigbe@gmail.com" });
	sendIntent.putExtra(Intent.EXTRA_TEXT, body);
	sendIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
	sendIntent.setType("message/rfc822");

	Intent createChooser = Intent.createChooser(sendIntent, "Title:");
	createChooser.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

	this.context.deleteFile("stack.trace");
	this.context.startActivity(createChooser);
    }

    /**
     * Installs the report sender, should be called at the beginning of an
     * activity or a service.
     */
    public static void install(Context context) {
	ReportSender topExceptionHandler = new ReportSender(context);
	topExceptionHandler.sendCrashReportIfExist();
	Thread.setDefaultUncaughtExceptionHandler(topExceptionHandler);
    }
}