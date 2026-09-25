package com.nyrds.platform.app;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.util.Util;
import com.watabou.pixeldungeon.utils.GLog;
import fi.iki.elonen.NanoHTTPD;
import java.io.File;
import java.io.FileInputStream;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import org.json.JSONObject;

/**
 * APK tampering telemetry: signing cert + installer source + packer fingerprints.
 * Telemetry only - never gates game functionality (bd 2q3: jiagu-repacked store
 * APKs crash inside ad SDK code, we need to see how many installs do that).
 */
public class AppIntegrity {

    public static final String PACKER_NONE = "none";

    public static class Report {
        public final String sigSha256;
        public final boolean sigTrusted;
        public final boolean sigConfigured;
        public final String installer;
        public final String packer;
        public final boolean repacked;

        Report(String sigSha256, boolean sigTrusted, boolean sigConfigured, String installer, String packer) {
            this.sigSha256 = sigSha256;
            this.sigTrusted = sigTrusted;
            this.sigConfigured = sigConfigured;
            this.installer = installer;
            this.packer = packer;
            this.repacked = deriveRepacked(packer, sigConfigured, sigTrusted);
        }

        public JSONObject json() {
            Map<String, Object> data = new HashMap<>();
            data.put("sig_sha256", sigSha256);
            data.put("sig_trusted", sigTrusted);
            data.put("sig_configured", sigConfigured);
            data.put("installer", installer);
            data.put("packer", packer);
            data.put("repacked", repacked);
            data.put("debug", Util.isDebug());
            return new JSONObject(data);
        }
    }

    @Nullable
    private static volatile Report sReport;

    @NonNull
    public static Report report() {
        Report report = sReport;
        if (report == null) {
            return new Report("unknown", false, false, "unknown", PACKER_NONE);
        }
        return report;
    }

    public static void collect(Context context) {
        try {
            sReport = doCollect(context);
            GLog.debug("AppIntegrity: %s", sReport.json().toString());
        } catch (Throwable e) {
            GLog.w("AppIntegrity failed: %s", e.getMessage());
        }

        BaseWebServer.registerDebugEndpoint("/debug/integrity",
                session -> NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.OK,
                        "application/json", report().json().toString()));
    }

    private static Report doCollect(Context context) {
        String pkg = context.getPackageName();
        PackageManager pm = context.getPackageManager();

        String sigSha256;
        try {
            sigSha256 = signerSha256(pm, pkg);
        } catch (Throwable e) {
            GLog.w("AppIntegrity: no signature: %s", e.getMessage());
            sigSha256 = "unknown";
        }

        boolean sigTrusted = RemixedDungeonApp.checkOwnSignature();
        boolean sigConfigured = !context.getResources().getString(R.string.ownSignature).isEmpty();

        String installer = "unknown";
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                installer = pm.getInstallSourceInfo(pkg).getInstallingPackageName();
            } else {
                @SuppressLint("WrongConstant")
                String legacy = pm.getInstallerPackageName(pkg);
                installer = legacy;
            }
        } catch (Throwable ignored) {
        }
        if (installer == null || installer.isEmpty()) {
            installer = "none";
        }

        String packer = probePacker(context, pkg, pm);
        return new Report(sigSha256, sigTrusted, sigConfigured, installer, packer);
    }

    private static String signerSha256(PackageManager pm, String pkg) throws Exception {
        Signature[] sigs;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            PackageInfo info = pm.getPackageInfo(pkg, PackageManager.GET_SIGNING_CERTIFICATES);
            sigs = info.signingInfo.getApkContentsSigners();
        } else {
            @SuppressLint("WrongConstant")
            PackageInfo info = pm.getPackageInfo(pkg, PackageManager.GET_SIGNATURES);
            sigs = info.signatures;
        }
        if (sigs == null || sigs.length == 0) {
            return "none";
        }
        return sha256Hex(sigs[0].toByteArray());
    }

    private static String probePacker(Context context, String pkg, PackageManager pm) {
        try {
            String appClass = pm.getApplicationInfo(pkg, 0).className;
            if (appClass != null && !appClass.isEmpty() && !appClass.equals(RemixedDungeonApp.class.getName())) {
                return "appclass:" + appClass;
            }
        } catch (Throwable ignored) {
        }

        try {
            Class.forName("com.stub.StubApp");
            return "stubapp";
        } catch (ClassNotFoundException ignored) {
        }

        try {
            if (fileContains(new File("/proc/self/maps"), "libjiagu")) {
                return "jiagu:maps";
            }
        } catch (Throwable ignored) {
        }

        try {
            if (new File(context.getApplicationInfo().dataDir, ".jiagu").exists()) {
                return "jiagu:datadir";
            }
        } catch (Throwable ignored) {
        }

        return PACKER_NONE;
    }

    private static boolean fileContains(File file, String needle) {
        if (!file.exists() || !file.canRead()) {
            return false;
        }
        try (FileInputStream in = new FileInputStream(file); Scanner scanner = new Scanner(in, "UTF-8")) {
            while (scanner.hasNextLine()) {
                if (scanner.nextLine().contains(needle)) {
                    return true;
                }
            }
        } catch (Exception ignored) {
        }
        return false;
    }

    static boolean deriveRepacked(String packer, boolean sigConfigured, boolean sigTrusted) {
        if (packer != null && !PACKER_NONE.equals(packer)) {
            return true;
        }
        // without the cert configured (dev builds) the signature verdict would be noise
        return sigConfigured && !sigTrusted;
    }

    static String sha256Hex(byte[] data) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(data);
            StringBuilder hex = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (Exception e) {
            return "unknown";
        }
    }
}
