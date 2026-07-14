package platinmods.com.dumper.Core;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class ShellUtil {

    public static class ShellResult {
        public final List<String> out;
        public final List<String> err;
        public final int exitCode;

        public ShellResult(List<String> out, List<String> err, int exitCode) {
            this.out = out;
            this.err = err;
            this.exitCode = exitCode;
        }

        public boolean isSuccess() { return exitCode == 0; }

        public List<String> getOut() { return out; }
    }

    public static ShellResult run(String suPath, String command) {
        List<String> out = new ArrayList<>();
        List<String> err = new ArrayList<>();
        int code = -1;
        try {
            Process p = Runtime.getRuntime().exec(new String[]{suPath, "-c", command});
            BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while ((line = br.readLine()) != null) out.add(line);
            BufferedReader er = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            while ((line = er.readLine()) != null) err.add(line);
            code = p.waitFor();
            br.close(); er.close(); p.destroy();
        } catch (Exception e) {
            err.add(e.getMessage());
        }
        return new ShellResult(out, err, code);
    }

    public static boolean checkRoot(String suPath) {
        if (suPath == null || suPath.isEmpty()) return false;
        ShellResult r = run(suPath, "id");
        for (String s : r.out) if (s.contains("uid=0")) return true;
        return r.isSuccess() && !r.out.isEmpty();
    }

    public static String checkRootDetail(String suPath) {
        ShellResult r = run(suPath, "id");
        StringBuilder sb = new StringBuilder();
        sb.append("Exit: ").append(r.exitCode).append("\n");
        for (String s : r.out) sb.append(s).append("\n");
        for (String s : r.err) sb.append("ERR: ").append(s).append("\n");
        boolean ok = false;
        for (String s : r.out) if (s.contains("uid=0")) { ok = true; break; }
        sb.append(ok ? "=> ROOT GRANTED" : "=> ROOT DENIED");
        return sb.toString();
    }
}
