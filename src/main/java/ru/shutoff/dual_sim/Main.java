package ru.shutoff.dual_sim;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.Set;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

@SuppressWarnings("unused")
public class Main implements IXposedHookLoadPackage {

    static void log(Object... params) {
        StringBuilder builder = new StringBuilder();
        Date d = new Date();
        builder.append(d.toLocaleString());
        for (int i = 0; i < params.length; i++) {
            builder.append(" ");
            if (params[i] != null)
                builder.append(params[i].toString());
        }
        File logFile = Environment.getExternalStorageDirectory();
        logFile = new File(logFile, "dual_sim.log");
        if (!logFile.exists()) {
            try {
                if (!logFile.createNewFile())
                    return;
            } catch (IOException e) {
                // ignore
            }
        }
        try {
            //BufferedWriter for performance, true to set append to file flag
            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
            buf.append(builder.toString());
            buf.newLine();
            buf.close();
        } catch (IOException e) {
            // ignore
        }
    }

    public void handleLoadPackage(final LoadPackageParam lpparam) throws Throwable {
        findAndHookMethod("android.app.Activity", lpparam.classLoader, "startActivity", "android.content.Intent", "android.os.Bundle", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                Intent intent = (Intent) param.args[0];
                if (Intent.ACTION_CALL.equals(intent.getAction())) {
                    log("Action call");
                    Bundle bundle = intent.getExtras();
                    Set<String> keys = bundle.keySet();
                    for (String key : keys) {
                        Object o = bundle.get(key);
                        log(key, o);
                    }
                }
                super.beforeHookedMethod(param);
            }
        });

    }

}
